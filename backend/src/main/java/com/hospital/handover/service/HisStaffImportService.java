package com.hospital.handover.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hospital.handover.dto.ImportResultDto;
import com.hospital.handover.entity.*;
import com.hospital.handover.repository.*;
import com.hospital.handover.util.EntityFieldMapper;
import com.hospital.handover.util.FieldMappingProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class HisStaffImportService {

    private static final Logger logger = LoggerFactory.getLogger(HisStaffImportService.class);
    private static final int BATCH_SIZE = 1000;
    private static final String CONFIG_CODE = "HIS_STAFF_SYNC";

    private final HisStaffRepository hisStaffRepository;
    private final InterfaceConfigRepository configRepository;
    private final InterfaceMappingTableRepository mappingTableRepository;
    private final InterfaceFieldMappingRepository fieldMappingRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TransactionTemplate transactionTemplate;

    public HisStaffImportService(HisStaffRepository hisStaffRepository,
                                  InterfaceConfigRepository configRepository,
                                  InterfaceMappingTableRepository mappingTableRepository,
                                  InterfaceFieldMappingRepository fieldMappingRepository,
                                  UserRepository userRepository,
                                  RoleRepository roleRepository,
                                  TransactionTemplate transactionTemplate) {
        this.hisStaffRepository = hisStaffRepository;
        this.configRepository = configRepository;
        this.mappingTableRepository = mappingTableRepository;
        this.fieldMappingRepository = fieldMappingRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public ImportResultDto importFromCsv(MultipartFile file) {
        ImportResultDto result = new ImportResultDto();

        if (file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }

        List<InterfaceFieldMapping> fieldMappings = getFieldMappings();
        if (fieldMappings.isEmpty()) {
            throw new RuntimeException("未找到人员信息映射配置，请联系管理员");
        }

        List<Map<String, String>> records = parseCsv(file, fieldMappings);
        if (records.isEmpty()) {
            return result;
        }

        result.setTotalCount(records.size());
        logger.info("开始导入 {} 条人员信息数据", records.size());

        ObjectMapper objectMapper = new ObjectMapper();
        Role doctorRole = getDoctorRole();

        for (int batchStart = 0; batchStart < records.size(); batchStart += BATCH_SIZE) {
            int batchEnd = Math.min(batchStart + BATCH_SIZE, records.size());
            List<Map<String, String>> batch = new ArrayList<>(records.subList(batchStart, batchEnd));

            final int startIdx = batchStart;
            final List<InterfaceFieldMapping> finalFieldMappings = fieldMappings;
            final ObjectMapper finalMapper = objectMapper;
            final Role finalRole = doctorRole;
            final ImportResultDto finalResult = result;

            transactionTemplate.executeWithoutResult(status -> {
                for (int i = 0; i < batch.size(); i++) {
                    Map<String, String> row = batch.get(i);
                    int lineNumber = startIdx + i + 2;

                    try {
                        processSingleRecord(row, lineNumber, finalFieldMappings, finalMapper, finalRole, finalResult);
                    } catch (Exception e) {
                        logger.error("Error processing record at line {}: {}", lineNumber, e.getMessage());
                        finalResult.addError(lineNumber, "处理异常: " + e.getMessage());
                        finalResult.setFailCount(finalResult.getFailCount() + 1);
                    }
                }
            });

            logger.info("已处理 {} / {} 条数据", batchEnd, records.size());
        }

        logger.info("导入完成: 总计={}, 新增={}, 更新={}, 失败={}", 
                    result.getTotalCount(), result.getInsertCount(), 
                    result.getUpdateCount(), result.getFailCount());

        return result;
    }

    private void processSingleRecord(Map<String, String> row, 
                                      int lineNumber,
                                      List<InterfaceFieldMapping> fieldMappings,
                                      ObjectMapper objectMapper,
                                      Role doctorRole,
                                      ImportResultDto result) {
        
        ObjectNode jsonNode = convertToJsonNode(row, fieldMappings, objectMapper);
        Map<String, Object> mappedData = FieldMappingProcessor.processRecord(jsonNode, fieldMappings);

        String staffCode = (String) mappedData.get("staff_code");
        String codeUser = (String) mappedData.get("code_user");

        if (staffCode == null || staffCode.trim().isEmpty()) {
            result.addError(lineNumber, "人员编码(staff_code)不能为空");
            result.setFailCount(result.getFailCount() + 1);
            return;
        }

        if (codeUser == null || codeUser.trim().isEmpty()) {
            result.addError(lineNumber, "CODE_USER不能为空");
            result.setFailCount(result.getFailCount() + 1);
            return;
        }

        staffCode = staffCode.trim();
        String employmentStatus = (String) mappedData.get("employment_status");
        String employmentStatusCode = (String) mappedData.get("employment_status_code");
        boolean isActive = isActiveStaff(employmentStatus, employmentStatusCode);

        Optional<HisStaff> existingOpt = hisStaffRepository.findByStaffCode(staffCode);
        HisStaff staff;

        if (existingOpt.isPresent()) {
            staff = existingOpt.get();
            EntityFieldMapper.mapFields(staff, mappedData);
            staff.setSyncTime(LocalDateTime.now());
            hisStaffRepository.save(staff);
            result.setUpdateCount(result.getUpdateCount() + 1);
        } else {
            staff = new HisStaff();
            EntityFieldMapper.mapFields(staff, mappedData);
            if (staff.getStaffCode() == null) {
                staff.setStaffCode(staffCode);
            }
            if (staff.getName() == null || staff.getName().isEmpty()) {
                staff.setName(staffCode);
            }
            staff.setSyncTime(LocalDateTime.now());
            hisStaffRepository.save(staff);
            result.setInsertCount(result.getInsertCount() + 1);
        }

        syncUserFromHisStaff(staff, isActive, doctorRole);
    }

    private Role getDoctorRole() {
        Role doctorRole = roleRepository.findByCode("DOCTOR");
        if (doctorRole == null) {
            doctorRole = roleRepository.findAll().stream()
                    .filter(r -> "DOCTOR".equalsIgnoreCase(r.getCode()) || "医生".equals(r.getName()))
                    .findFirst()
                    .orElse(null);
        }
        return doctorRole;
    }

    private List<InterfaceFieldMapping> getFieldMappings() {
        InterfaceConfig config = configRepository.findByConfigCode(CONFIG_CODE);
        if (config == null) {
            return Collections.emptyList();
        }

        List<InterfaceMappingTable> mappingTables = mappingTableRepository.findByConfigId(config.getId());
        if (mappingTables.isEmpty()) {
            return Collections.emptyList();
        }

        InterfaceMappingTable mainMapping = mappingTables.stream()
                .filter(t -> t.getParentMappingId() == null)
                .findFirst()
                .orElse(null);

        if (mainMapping == null) {
            return Collections.emptyList();
        }

        return fieldMappingRepository.findByMappingTableId(mainMapping.getId());
    }

    private List<Map<String, String>> parseCsv(MultipartFile file, 
                                                List<InterfaceFieldMapping> fieldMappings) {
        List<Map<String, String>> records = new ArrayList<>();

        try {
            byte[] bytes = file.getBytes();
            String content = new String(bytes, StandardCharsets.UTF_8);

            content = content.replace("\r\n", "\n").replace("\r", "\n");

            String[] lines = content.split("\n");
            if (lines.length == 0) {
                throw new RuntimeException("文件为空");
            }

            String[] headers = lines[0].split(",");
            Map<String, Integer> headerIndexMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                String header = headers[i].replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();
                headerIndexMap.put(header, i);
            }

            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                if (line == null || line.trim().isEmpty()) continue;

                String[] values = parseCsvLine(line);
                Map<String, String> row = new HashMap<>();

                for (InterfaceFieldMapping mapping : fieldMappings) {
                    String sourceField = mapping.getSourceField().toLowerCase();
                    Integer idx = headerIndexMap.get(sourceField);
                    if (idx != null && idx < values.length) {
                        String value = values[idx].trim().replaceAll("^\"|\"$", "");
                        row.put(sourceField, value);
                    }
                }

                records.add(row);
            }

        } catch (Exception e) {
            throw new RuntimeException("CSV文件解析失败：" + e.getMessage());
        }

        return records;
    }

    private String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        values.add(current.toString());

        return values.toArray(new String[0]);
    }

    private ObjectNode convertToJsonNode(Map<String, String> row,
                                          List<InterfaceFieldMapping> fieldMappings,
                                          ObjectMapper objectMapper) {
        ObjectNode jsonNode = objectMapper.createObjectNode();

        for (InterfaceFieldMapping mapping : fieldMappings) {
            String sourceField = mapping.getSourceField();
            String value = row.get(sourceField.toLowerCase());
            if (value != null) {
                jsonNode.put(sourceField, value);
            }
        }

        return jsonNode;
    }

    private boolean isActiveStaff(String employmentStatus, String employmentStatusCode) {
        if ("离职".equals(employmentStatus)) {
            return false;
        }
        if (employmentStatusCode != null && !employmentStatusCode.isEmpty()) {
            return "0".equals(employmentStatusCode);
        }
        return true;
    }

    private void syncUserFromHisStaff(HisStaff staff, boolean isActive, Role doctorRole) {
        String username = staff.getNameUser();
        if (username == null || username.isEmpty()) {
            username = staff.getStaffCode();
        }

        User existingByStaffId = userRepository.findByHisStaffId(staff.getId());

        if (existingByStaffId != null) {
            existingByStaffId.setEnabled(isActive);
            existingByStaffId.setUsercode(staff.getCodeUser());
            userRepository.save(existingByStaffId);
            return;
        }

        User existingByUsername = userRepository.findByUsername(username);
        if (existingByUsername != null) {
            logger.debug("User {} already exists, skipping", username);
            return;
        }

        if (doctorRole == null) {
            logger.warn("Doctor role not found, cannot create user for staff {}", username);
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setUsercode(staff.getCodeUser());
        user.setPassword("123456");
        user.setHisStaffId(staff.getId());
        user.setRoleId(doctorRole.getId());
        user.setEnabled(isActive);
        user.setIsSuperAdmin(false);
        userRepository.save(user);
    }
}