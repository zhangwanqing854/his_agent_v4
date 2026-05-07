package com.hospital.handover.service;

import com.hospital.handover.dto.ImportResultDto;
import com.hospital.handover.entity.*;
import com.hospital.handover.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class DoctorDepartmentImportService {

    private static final Logger logger = LoggerFactory.getLogger(DoctorDepartmentImportService.class);
    private static final int BATCH_SIZE = 1000;
    private static final String CONFIG_CODE = "HIS_DOCTOR_DEPT_SYNC";

    private final DoctorDepartmentRepository doctorDepartmentRepository;
    private final HisStaffRepository hisStaffRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final InterfaceConfigRepository configRepository;
    private final InterfaceMappingTableRepository mappingTableRepository;
    private final InterfaceFieldMappingRepository fieldMappingRepository;
    private final TransactionTemplate transactionTemplate;

    public DoctorDepartmentImportService(DoctorDepartmentRepository doctorDepartmentRepository,
                                          HisStaffRepository hisStaffRepository,
                                          UserRepository userRepository,
                                          DepartmentRepository departmentRepository,
                                          InterfaceConfigRepository configRepository,
                                          InterfaceMappingTableRepository mappingTableRepository,
                                          InterfaceFieldMappingRepository fieldMappingRepository,
                                          TransactionTemplate transactionTemplate) {
        this.doctorDepartmentRepository = doctorDepartmentRepository;
        this.hisStaffRepository = hisStaffRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.configRepository = configRepository;
        this.mappingTableRepository = mappingTableRepository;
        this.fieldMappingRepository = fieldMappingRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public ImportResultDto importFromCsv(MultipartFile file) {
        ImportResultDto result = new ImportResultDto();

        if (file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }

        List<InterfaceFieldMapping> fieldMappings = getFieldMappings();
        if (fieldMappings.isEmpty()) {
            throw new RuntimeException("未找到科室人员关系映射配置，请联系管理员");
        }

        List<Map<String, String>> records = parseCsv(file, fieldMappings);
        if (records.isEmpty()) {
            return result;
        }

        result.setTotalCount(records.size());
        logger.info("开始导入 {} 条科室人员关系数据", records.size());

        for (int batchStart = 0; batchStart < records.size(); batchStart += BATCH_SIZE) {
            int batchEnd = Math.min(batchStart + BATCH_SIZE, records.size());
            List<Map<String, String>> batch = new ArrayList<>(records.subList(batchStart, batchEnd));

            final int startIdx = batchStart;
            final ImportResultDto finalResult = result;

            transactionTemplate.executeWithoutResult(status -> {
                for (int i = 0; i < batch.size(); i++) {
                    Map<String, String> row = batch.get(i);
                    int lineNumber = startIdx + i + 2;

                    try {
                        processSingleRecord(row, lineNumber, finalResult);
                    } catch (Exception e) {
                        logger.error("Error processing record at line {}: {}", lineNumber, e.getMessage());
                        finalResult.addError(lineNumber, "处理异常: " + e.getMessage());
                        finalResult.setFailCount(finalResult.getFailCount() + 1);
                    }
                }
            });

            logger.info("已处理 {} / {} 条数据", batchEnd, records.size());
        }

        logger.info("导入完成: 总计={}, 新增={}, 跳过={}, 失败={}", 
                    result.getTotalCount(), result.getInsertCount(), 
                    result.getSkipCount(), result.getFailCount());

        return result;
    }

    private void processSingleRecord(Map<String, String> row, 
                                      int lineNumber,
                                      ImportResultDto result) {
        
        String codeUser = row.get("code_user");
        String codeDept = row.get("code_dept");

        if (codeUser == null || codeUser.trim().isEmpty()) {
            result.addError(lineNumber, "CODE_USER不能为空");
            result.setFailCount(result.getFailCount() + 1);
            return;
        }

        if (codeDept == null || codeDept.trim().isEmpty()) {
            result.addError(lineNumber, "CODE_DEPT不能为空");
            result.setFailCount(result.getFailCount() + 1);
            return;
        }

        codeUser = codeUser.trim();
        codeDept = codeDept.trim();

        Optional<HisStaff> hisStaffOpt = hisStaffRepository.findByCodeUser(codeUser);
        if (!hisStaffOpt.isPresent()) {
            result.addError(lineNumber, "人员编码不存在: " + codeUser);
            result.setFailCount(result.getFailCount() + 1);
            return;
        }

        User user = userRepository.findByHisStaffId(hisStaffOpt.get().getId());
        if (user == null) {
            result.addError(lineNumber, "人员未关联用户账号: " + codeUser);
            result.setFailCount(result.getFailCount() + 1);
            return;
        }

        Long doctorId = user.getId();

        Optional<Department> deptOpt = departmentRepository.findByCode(codeDept);
        if (!deptOpt.isPresent()) {
            result.addError(lineNumber, "科室编码不存在: " + codeDept);
            result.setFailCount(result.getFailCount() + 1);
            return;
        }

        Long departmentId = deptOpt.get().getId();

        Optional<DoctorDepartment> existing = doctorDepartmentRepository.findByDoctorIdAndDepartmentId(doctorId, departmentId);

        if (existing.isPresent()) {
            result.setSkipCount(result.getSkipCount() + 1);
        } else {
            DoctorDepartment dd = new DoctorDepartment();
            dd.setDoctorId(doctorId);
            dd.setDepartmentId(departmentId);
            dd.setIsPrimary(false);
            doctorDepartmentRepository.save(dd);
            result.setInsertCount(result.getInsertCount() + 1);
        }
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
}