package com.hospital.handover.service;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VisitImportService {

    private static final Logger logger = LoggerFactory.getLogger(VisitImportService.class);
    private static final int MAX_ROWS = 1000;
    private static final String CONFIG_CODE = "HIS_VISIT_SYNC";

    private final VisitRepository visitRepository;
    private final PatientRepository patientRepository;
    private final DepartmentRepository departmentRepository;
    private final InterfaceConfigRepository configRepository;
    private final InterfaceMappingTableRepository mappingTableRepository;
    private final InterfaceFieldMappingRepository fieldMappingRepository;

    public VisitImportService(VisitRepository visitRepository,
                              PatientRepository patientRepository,
                              DepartmentRepository departmentRepository,
                              InterfaceConfigRepository configRepository,
                              InterfaceMappingTableRepository mappingTableRepository,
                              InterfaceFieldMappingRepository fieldMappingRepository) {
        this.visitRepository = visitRepository;
        this.patientRepository = patientRepository;
        this.departmentRepository = departmentRepository;
        this.configRepository = configRepository;
        this.mappingTableRepository = mappingTableRepository;
        this.fieldMappingRepository = fieldMappingRepository;
    }

    @Transactional
    public ImportResultDto importFromCsv(MultipartFile file) {
        ImportResultDto result = new ImportResultDto();

        if (file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }

        List<InterfaceFieldMapping> fieldMappings = getFieldMappings();
        if (fieldMappings.isEmpty()) {
            throw new RuntimeException("未找到就诊信息映射配置，请联系管理员");
        }

        List<Map<String, String>> records = parseCsv(file, fieldMappings, result);
        if (records.isEmpty()) {
            return result;
        }

        if (records.size() > MAX_ROWS) {
            throw new RuntimeException("单次导入最多支持" + MAX_ROWS + "条数据，请分批导入");
        }

        result.setTotalCount(records.size());

        ObjectMapper objectMapper = new ObjectMapper();

        Set<String> existingVisitNos = getExistingVisitNos(records, fieldMappings);

        for (int i = 0; i < records.size(); i++) {
            Map<String, String> row = records.get(i);
            int lineNumber = i + 2;

            try {
                String codeEnt = row.get("code_ent");
                String codePat = row.get("code_pat");
                String namePat = row.get("name_pat");
                String idPat = row.get("id_pat");
                String codeDepPhy = row.get("code_dep_phy");
                String nameDepPhy = row.get("name_dep_phy");
                String sdLevelNur = row.get("sd_level_nur");
                String nameLevelNur = row.get("name_level_nur");
                String dtAcpt = row.get("dt_acpt");

                if (codeEnt == null || codeEnt.trim().isEmpty()) {
                    result.addError(lineNumber, "CODE_ENT不能为空");
                    continue;
                }

                if (codePat == null || codePat.trim().isEmpty()) {
                    result.addError(lineNumber, "CODE_PAT不能为空");
                    continue;
                }

                if (namePat == null || namePat.trim().isEmpty()) {
                    result.addError(lineNumber, "NAME_PAT不能为空");
                    continue;
                }

                if (idPat == null || idPat.trim().isEmpty()) {
                    result.addError(lineNumber, "ID_PAT不能为空");
                    continue;
                }

                if (codeDepPhy == null || codeDepPhy.trim().isEmpty()) {
                    result.addError(lineNumber, "CODE_DEP_PHY不能为空");
                    continue;
                }

                if (nameDepPhy == null || nameDepPhy.trim().isEmpty()) {
                    result.addError(lineNumber, "NAME_DEP_PHY不能为空");
                    continue;
                }

                if (sdLevelNur == null || sdLevelNur.trim().isEmpty()) {
                    result.addError(lineNumber, "SD_LEVEL_NUR不能为空");
                    continue;
                }

                if (nameLevelNur == null || nameLevelNur.trim().isEmpty()) {
                    result.addError(lineNumber, "NAME_LEVEL_NUR不能为空");
                    continue;
                }

                if (dtAcpt == null || dtAcpt.trim().isEmpty()) {
                    result.addError(lineNumber, "DT_ACPT不能为空");
                    continue;
                }

                LocalDateTime admissionDatetime = FieldMappingProcessor.parseDateTime(dtAcpt.trim());
                if (admissionDatetime == null) {
                    result.addError(lineNumber, "DT_ACPT日期格式错误");
                    continue;
                }

                String visitNo = codeEnt.trim();

                ObjectNode jsonNode = convertToJsonNode(row, fieldMappings, objectMapper);
                Map<String, Object> mappedData = FieldMappingProcessor.processRecord(jsonNode, fieldMappings);

                Visit visit;
                if (existingVisitNos.contains(visitNo)) {
                    Optional<Visit> existingOpt = visitRepository.findByVisitNo(visitNo);
                    if (existingOpt.isPresent()) {
                        visit = existingOpt.get();
                        EntityFieldMapper.mapFields(visit, mappedData);
                        lookupAndSetPatient(visit, codePat);
                        lookupAndSetDepartment(visit, codeDepPhy);
                        visit.setSyncTime(LocalDateTime.now());
                        visit.setFgIp("Y");
                        visit.setPatientStatus("在院");
                        visit.setNurseLevel(FieldMappingProcessor.mapNurseLevel(sdLevelNur.trim()));
                        visitRepository.save(visit);
                        result.setUpdateCount(result.getUpdateCount() + 1);
                    } else {
                        result.addError(lineNumber, "数据库查询异常");
                        continue;
                    }
                } else {
                    visit = new Visit();
                    EntityFieldMapper.mapFields(visit, mappedData);
                    lookupAndSetPatient(visit, codePat);
                    lookupAndSetDepartment(visit, codeDepPhy);
                    if (visit.getVisitNo() == null) {
                        visit.setVisitNo(visitNo);
                    }
                    if (visit.getPatientName() == null) {
                        visit.setPatientName(namePat.trim());
                    }
                    if (visit.getPatientNo() == null) {
                        visit.setPatientNo(codePat.trim());
                    }
                    if (visit.getDeptName() == null) {
                        visit.setDeptName(nameDepPhy.trim());
                    }
                    if (visit.getNurseLevel() == null) {
                        visit.setNurseLevel(FieldMappingProcessor.mapNurseLevel(sdLevelNur.trim()));
                    }
                    if (visit.getNurseLevelCode() == null) {
                        visit.setNurseLevelCode(sdLevelNur.trim());
                    }
                    if (visit.getAdmissionDatetime() == null) {
                        visit.setAdmissionDatetime(admissionDatetime);
                    }
                    visit.setSyncTime(LocalDateTime.now());
                    visit.setFgIp("Y");
                    visit.setPatientStatus("在院");
                    visit.setIsCritical(false);
                    visitRepository.save(visit);
                    result.setInsertCount(result.getInsertCount() + 1);
                }

            } catch (Exception e) {
                logger.error("Error processing record at line {}: {}", lineNumber, e.getMessage());
                result.addError(lineNumber, "处理异常: " + e.getMessage());
            }
        }

        return result;
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
                                                List<InterfaceFieldMapping> fieldMappings,
                                                ImportResultDto result) {
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

            for (int i = 1; i < lines.length && i <= MAX_ROWS; i++) {
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

    private Set<String> getExistingVisitNos(List<Map<String, String>> records,
                                             List<InterfaceFieldMapping> fieldMappings) {
        String visitNoSourceField = null;
        for (InterfaceFieldMapping mapping : fieldMappings) {
            if ("visit_no".equals(mapping.getTargetField())) {
                visitNoSourceField = mapping.getSourceField().toLowerCase();
                break;
            }
        }

        if (visitNoSourceField == null) {
            visitNoSourceField = "code_ent";
        }

        Set<String> visitNosInFile = new HashSet<>();
        for (Map<String, String> row : records) {
            String visitNo = row.get(visitNoSourceField);
            if (visitNo != null && !visitNo.trim().isEmpty()) {
                visitNosInFile.add(visitNo.trim());
            }
        }

        Set<String> existingVisitNos = new HashSet<>();
        for (String visitNo : visitNosInFile) {
            if (visitRepository.findByVisitNo(visitNo).isPresent()) {
                existingVisitNos.add(visitNo);
            }
        }

        return existingVisitNos;
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

    private void lookupAndSetDepartment(Visit visit, String codeDepPhy) {
        if (visit.getDepartmentId() == null && codeDepPhy != null && !codeDepPhy.isEmpty()) {
            String deptCode = codeDepPhy.trim();
            Optional<Department> deptOpt = departmentRepository.findByCode(deptCode);
            if (deptOpt.isPresent()) {
                visit.setDepartmentId(deptOpt.get().getId());
            } else {
                visit.setDepartmentId(0L);
            }
        }
        if (visit.getDepartmentId() == null) {
            visit.setDepartmentId(0L);
        }
    }

    private void lookupAndSetPatient(Visit visit, String codePat) {
        if (visit.getPatientId() == null && codePat != null && !codePat.isEmpty()) {
            String patientNo = codePat.trim();
            Optional<Patient> patientOpt = patientRepository.findByPatientNo(patientNo);
            if (patientOpt.isPresent()) {
                visit.setPatientId(patientOpt.get().getId());
            } else {
                visit.setPatientId(0L);
            }
        }
        if (visit.getPatientId() == null) {
            visit.setPatientId(0L);
        }
    }
}