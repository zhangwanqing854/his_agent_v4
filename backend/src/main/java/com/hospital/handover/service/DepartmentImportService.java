package com.hospital.handover.service;

import com.hospital.handover.dto.ImportResultDto;
import com.hospital.handover.entity.Department;
import com.hospital.handover.entity.InterfaceConfig;
import com.hospital.handover.entity.InterfaceFieldMapping;
import com.hospital.handover.entity.InterfaceMappingTable;
import com.hospital.handover.repository.DepartmentRepository;
import com.hospital.handover.repository.InterfaceConfigRepository;
import com.hospital.handover.repository.InterfaceFieldMappingRepository;
import com.hospital.handover.repository.InterfaceMappingTableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class DepartmentImportService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentImportService.class);
    private static final int BATCH_SIZE = 1000;
    private static final String CONFIG_CODE = "HIS_DEPT_SYNC";

    private final DepartmentRepository departmentRepository;
    private final InterfaceConfigRepository configRepository;
    private final InterfaceMappingTableRepository mappingTableRepository;
    private final InterfaceFieldMappingRepository fieldMappingRepository;
    private final TransactionTemplate transactionTemplate;

    public DepartmentImportService(DepartmentRepository departmentRepository,
                                    InterfaceConfigRepository configRepository,
                                    InterfaceMappingTableRepository mappingTableRepository,
                                    InterfaceFieldMappingRepository fieldMappingRepository,
                                    TransactionTemplate transactionTemplate) {
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

        Map<String, String> fieldMapping = getFieldMapping();
        if (fieldMapping.isEmpty()) {
            throw new RuntimeException("未找到科室信息映射配置，请联系管理员");
        }

        List<Map<String, String>> records = parseCsv(file);
        if (records.isEmpty()) {
            return result;
        }

        result.setTotalCount(records.size());
        logger.info("开始导入 {} 条科室信息数据", records.size());

        for (int batchStart = 0; batchStart < records.size(); batchStart += BATCH_SIZE) {
            int batchEnd = Math.min(batchStart + BATCH_SIZE, records.size());
            List<Map<String, String>> batch = new ArrayList<>(records.subList(batchStart, batchEnd));

            final int startIdx = batchStart;
            final Map<String, String> finalFieldMapping = fieldMapping;
            final ImportResultDto finalResult = result;

            transactionTemplate.executeWithoutResult(status -> {
                for (int i = 0; i < batch.size(); i++) {
                    Map<String, String> row = batch.get(i);
                    int lineNumber = startIdx + i + 2;

                    try {
                        processSingleRecord(row, lineNumber, finalFieldMapping, finalResult);
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
                                      Map<String, String> fieldMapping,
                                      ImportResultDto result) {
        
        String code = getValue(row, fieldMapping, "code");
        String name = getValue(row, fieldMapping, "name");

        if (code == null || code.trim().isEmpty()) {
            result.addError(lineNumber, "科室编码不能为空");
            result.setFailCount(result.getFailCount() + 1);
            return;
        }

        if (name == null || name.trim().isEmpty()) {
            result.addError(lineNumber, "科室名称不能为空");
            result.setFailCount(result.getFailCount() + 1);
            return;
        }

        code = code.trim();
        name = name.trim();

        Optional<Department> existingOpt = departmentRepository.findByCode(code);

        if (existingOpt.isPresent()) {
            Department dept = existingOpt.get();
            dept.setName(name);
            departmentRepository.save(dept);
            result.setUpdateCount(result.getUpdateCount() + 1);
        } else {
            Department dept = new Department();
            dept.setCode(code);
            dept.setName(name);
            dept.setBedCount(0);
            departmentRepository.save(dept);
            result.setInsertCount(result.getInsertCount() + 1);
        }
    }

    private Map<String, String> getFieldMapping() {
        Map<String, String> mapping = new HashMap<>();

        InterfaceConfig config = configRepository.findByConfigCode(CONFIG_CODE);
        if (config == null) {
            return mapping;
        }

        List<InterfaceMappingTable> mappingTables = mappingTableRepository.findByConfigId(config.getId());
        if (mappingTables.isEmpty()) {
            return mapping;
        }

        InterfaceMappingTable mainMapping = mappingTables.stream()
                .filter(t -> t.getParentMappingId() == null)
                .findFirst()
                .orElse(null);

        if (mainMapping == null) {
            return mapping;
        }

        List<InterfaceFieldMapping> fieldMappings = fieldMappingRepository.findByMappingTableId(mainMapping.getId());
        for (InterfaceFieldMapping fm : fieldMappings) {
            String targetField = fm.getTargetField();
            if ("code".equals(targetField) || "name".equals(targetField)) {
                mapping.put(fm.getSourceField().toLowerCase(), targetField);
            }
        }

        return mapping;
    }

    private List<Map<String, String>> parseCsv(MultipartFile file) {
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

                for (Map.Entry<String, Integer> entry : headerIndexMap.entrySet()) {
                    String header = entry.getKey();
                    int idx = entry.getValue();
                    if (idx < values.length) {
                        String value = values[idx].trim().replaceAll("^\"|\"$", "");
                        row.put(header, value);
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

    private String getValue(Map<String, String> row, Map<String, String> fieldMapping, String targetField) {
        for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
            if (entry.getValue().equals(targetField)) {
                return row.get(entry.getKey());
            }
        }
        return row.get(targetField);
    }
}