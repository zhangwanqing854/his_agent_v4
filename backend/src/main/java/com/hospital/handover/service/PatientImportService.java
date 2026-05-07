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
public class PatientImportService {

    private static final Logger logger = LoggerFactory.getLogger(PatientImportService.class);
    private static final int MAX_ROWS = 1000;
    private static final String CONFIG_CODE = "HIS_PATIENT_SYNC";

    private final PatientRepository patientRepository;
    private final InterfaceConfigRepository configRepository;
    private final InterfaceMappingTableRepository mappingTableRepository;
    private final InterfaceFieldMappingRepository fieldMappingRepository;

    public PatientImportService(PatientRepository patientRepository,
                                InterfaceConfigRepository configRepository,
                                InterfaceMappingTableRepository mappingTableRepository,
                                InterfaceFieldMappingRepository fieldMappingRepository) {
        this.patientRepository = patientRepository;
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
            throw new RuntimeException("未找到患者信息映射配置，请联系管理员");
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

        Set<String> existingPatientNos = getExistingPatientNos(records, fieldMappings);

        for (int i = 0; i < records.size(); i++) {
            Map<String, String> row = records.get(i);
            int lineNumber = i + 2;

            try {
                String code = row.get("code");
                String namePat = row.get("name_pat");
                String sdSex = row.get("sd_sex");
                String nameSex = row.get("name_sex");
                String sdCountry = row.get("sd_country");
                String nameCountry = row.get("name_country");

                if (code == null || code.trim().isEmpty()) {
                    result.addError(lineNumber, "CODE不能为空");
                    continue;
                }

                if (namePat == null || namePat.trim().isEmpty()) {
                    result.addError(lineNumber, "NAME_PAT不能为空");
                    continue;
                }

                if (sdSex == null || sdSex.trim().isEmpty()) {
                    result.addError(lineNumber, "SD_SEX不能为空");
                    continue;
                }

                if (nameSex == null || nameSex.trim().isEmpty()) {
                    result.addError(lineNumber, "NAME_SEX不能为空");
                    continue;
                }

                String patientNo = code.trim();
                
                ObjectNode jsonNode = convertToJsonNode(row, fieldMappings, objectMapper);
                Map<String, Object> mappedData = FieldMappingProcessor.processRecord(jsonNode, fieldMappings);

                Patient patient;
                if (existingPatientNos.contains(patientNo)) {
                    Optional<Patient> existingOpt = patientRepository.findByPatientNo(patientNo);
                    if (existingOpt.isPresent()) {
                        patient = existingOpt.get();
                        EntityFieldMapper.mapFields(patient, mappedData);
                        patient.setSyncTime(LocalDateTime.now());
                        patientRepository.save(patient);
                        result.setUpdateCount(result.getUpdateCount() + 1);
                    } else {
                        result.addError(lineNumber, "数据库查询异常");
                        continue;
                    }
                } else {
                    patient = new Patient();
                    EntityFieldMapper.mapFields(patient, mappedData);
                    if (patient.getPatientNo() == null) {
                        patient.setPatientNo(patientNo);
                    }
                    if (patient.getName() == null) {
                        patient.setName(namePat.trim());
                    }
                    patient.setSyncTime(LocalDateTime.now());
                    patientRepository.save(patient);
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

            for (InterfaceFieldMapping mapping : fieldMappings) {
                String sourceField = mapping.getSourceField().toLowerCase();
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

    private Set<String> getExistingPatientNos(List<Map<String, String>> records,
                                               List<InterfaceFieldMapping> fieldMappings) {
        String patientNoSourceField = null;
        for (InterfaceFieldMapping mapping : fieldMappings) {
            if ("patient_no".equals(mapping.getTargetField())) {
                patientNoSourceField = mapping.getSourceField().toLowerCase();
                break;
            }
        }

        if (patientNoSourceField == null) {
            patientNoSourceField = "code";
        }

        Set<String> patientNosInFile = new HashSet<>();
        for (Map<String, String> row : records) {
            String patientNo = row.get(patientNoSourceField);
            if (patientNo != null && !patientNo.trim().isEmpty()) {
                patientNosInFile.add(patientNo.trim());
            }
        }

        Set<String> existingPatientNos = new HashSet<>();
        for (String patientNo : patientNosInFile) {
            if (patientRepository.findByPatientNo(patientNo).isPresent()) {
                existingPatientNos.add(patientNo);
            }
        }

        return existingPatientNos;
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
}