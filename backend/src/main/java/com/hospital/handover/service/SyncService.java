package com.hospital.handover.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hospital.handover.dto.BatchSyncResultDto;
import com.hospital.handover.dto.SyncItemResultDto;
import com.hospital.handover.dto.SyncResultDto;
import com.hospital.handover.entity.*;
import com.hospital.handover.repository.*;
import com.hospital.handover.util.EntityFieldMapper;
import com.hospital.handover.util.FieldMappingProcessor;
import com.hospital.handover.util.SoapClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SyncService {

    private static final Logger logger = LoggerFactory.getLogger(SyncService.class);
    private static final int TIMEOUT_MS = 30000;

    private final InterfaceConfigRepository configRepository;
    private final InterfaceMappingTableRepository mappingTableRepository;
    private final InterfaceFieldMappingRepository fieldMappingRepository;
    private final SyncRecordRepository syncRecordRepository;
    private final SyncRecordService syncRecordService;
    private final DepartmentRepository departmentRepository;
    private final HisStaffRepository hisStaffRepository;
    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;
    private final TransferRecordRepository transferRecordRepository;
    private final DiagnosisMainRepository diagnosisMainRepository;
    private final DiagnosisItemRepository diagnosisItemRepository;
    private final OrderMainRepository orderMainRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DoctorDepartmentRepository doctorDepartmentRepository;

    public SyncService(InterfaceConfigRepository configRepository,
                       InterfaceMappingTableRepository mappingTableRepository,
                       InterfaceFieldMappingRepository fieldMappingRepository,
                       SyncRecordRepository syncRecordRepository,
                       SyncRecordService syncRecordService,
                       DepartmentRepository departmentRepository,
                       HisStaffRepository hisStaffRepository,
                       PatientRepository patientRepository,
                       VisitRepository visitRepository,
                       TransferRecordRepository transferRecordRepository,
                       DiagnosisMainRepository diagnosisMainRepository,
                       DiagnosisItemRepository diagnosisItemRepository,
                       OrderMainRepository orderMainRepository,
                       OrderItemRepository orderItemRepository,
                       UserRepository userRepository,
                       RoleRepository roleRepository,
                       DoctorDepartmentRepository doctorDepartmentRepository) {
        this.configRepository = configRepository;
        this.mappingTableRepository = mappingTableRepository;
        this.fieldMappingRepository = fieldMappingRepository;
        this.syncRecordRepository = syncRecordRepository;
        this.syncRecordService = syncRecordService;
        this.departmentRepository = departmentRepository;
        this.hisStaffRepository = hisStaffRepository;
        this.patientRepository = patientRepository;
        this.visitRepository = visitRepository;
        this.transferRecordRepository = transferRecordRepository;
        this.diagnosisMainRepository = diagnosisMainRepository;
        this.diagnosisItemRepository = diagnosisItemRepository;
        this.orderMainRepository = orderMainRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.doctorDepartmentRepository = doctorDepartmentRepository;
    }

    public SyncResultDto executeSync(Long configId, String deptCode) {
        SyncResultDto result = new SyncResultDto();
        LocalDateTime startTime = LocalDateTime.now();
        long startMillis = System.currentTimeMillis();
        
        int totalCount = 0;
        int insertCount = 0;
        int updateCount = 0;
        int skipCount = 0;
        int failCount = 0;
        String errorMessage = null;
        String requestData = null;

        try {
            Optional<InterfaceConfig> configOpt = configRepository.findById(configId);
            if (!configOpt.isPresent()) {
                result.setSuccess(false);
                result.setMessage("接口配置不存在");
                return result;
            }

            InterfaceConfig config = configOpt.get();
            
            List<InterfaceMappingTable> mappingTables = mappingTableRepository.findByConfigId(configId);
            if (mappingTables.isEmpty()) {
                result.setSuccess(false);
                result.setMessage("未配置字段映射");
                return result;
            }

            String soapParams = buildSoapParams(config, deptCode);
            requestData = soapParams;

            JsonNode responseData = SoapClient.callSoapService(
                config.getUrl(),
                config.getSoapAction(),
                config.getSoapNamespace(),
                soapParams,
                TIMEOUT_MS
            );

            if (responseData == null || !responseData.isArray()) {
                result.setSuccess(false);
                result.setMessage("接口返回数据格式错误");
                return result;
            }

            InterfaceMappingTable mainMapping = mappingTables.stream()
                .filter(t -> t.getParentMappingId() == null)
                .findFirst()
                .orElse(null);

            if (mainMapping == null) {
                result.setSuccess(false);
                result.setMessage("未配置主表映射");
                return result;
            }

            List<InterfaceFieldMapping> mainFieldMappings = fieldMappingRepository.findByMappingTableId(mainMapping.getId());

            SyncStats stats = processSyncData(config.getDataType(), responseData, mainMapping, mainFieldMappings, mappingTables);
            
            if ("STAFF".equals(config.getDataType())) {
                disableInactiveStaffUsers();
            }
            
            totalCount = stats.totalCount;
            insertCount = stats.insertCount;
            updateCount = stats.updateCount;
            skipCount = stats.skipCount;
            failCount = stats.failCount;

            config.setLastSyncTime(LocalDateTime.now());
            config.setLastSyncStatus("SUCCESS");
            config.setLastSyncCount(totalCount);
            if (config.getIsFirstSync()) {
                config.setIsFirstSync(false);
            }
            configRepository.save(config);

            result.setSuccess(true);
            result.setMessage("同步成功");

        } catch (Exception e) {
            logger.error("同步失败", e);
            result.setSuccess(false);
            result.setMessage("同步失败：" + e.getMessage());
            errorMessage = e.getMessage();
            failCount = 1;
        }

        long durationMs = System.currentTimeMillis() - startMillis;
        
        syncRecordService.saveSyncRecord(configId, startTime, LocalDateTime.now(), totalCount, insertCount, updateCount, skipCount, failCount, errorMessage, requestData, durationMs);

        result.setTotalCount(totalCount);
        result.setInsertCount(insertCount);
        result.setUpdateCount(updateCount);
        result.setSkipCount(skipCount);
        result.setFailCount(failCount);
        result.setDurationMs(durationMs);
        result.setErrorMessage(errorMessage);

        return result;
    }

    private String buildSoapParams(InterfaceConfig config, String deptCode) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime;

        if (config.getIsFirstSync() != null && config.getIsFirstSync() && config.getFirstSyncDays() != null) {
            startTime = endTime.minusDays(config.getFirstSyncDays());
        } else if (config.getIncrementalSyncHours() != null) {
            startTime = endTime.minusHours(config.getIncrementalSyncHours());
        } else {
            startTime = endTime.minusHours(24);
        }

        String existingParams = config.getSoapParams();
        String deptParam = config.getDeptParam();
        String startParam = config.getSyncTimeParamStart();
        String endParam = config.getSyncTimeParamEnd();
        
        if (existingParams != null && !existingParams.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode paramsNode = mapper.readTree(existingParams);
                if (paramsNode.isArray()) {
                    ArrayNode arrayNode = (ArrayNode) paramsNode;
                    for (int i = 0; i < arrayNode.size(); i++) {
                        JsonNode param = arrayNode.get(i);
                        String name = param.has("name") ? param.get("name").asText() : "";
                        ObjectNode paramNode = (ObjectNode) param;
                        
                        if (deptParam != null && name.equals(deptParam) && deptCode != null) {
                            paramNode.put("value", deptCode);
                        } else if (startParam != null && name.equals(startParam)) {
                            paramNode.put("value", startTime.format(DateTimeFormatter.ofPattern(config.getSyncTimeFormat())));
                        } else if (endParam != null && name.equals(endParam)) {
                            paramNode.put("value", endTime.format(DateTimeFormatter.ofPattern(config.getSyncTimeFormat())));
                        }
                    }
                    return arrayNode.toString();
                }
            } catch (Exception e) {
                logger.warn("Failed to parse existing soapParams: {}", existingParams);
            }
        }

        return SoapClient.buildTimeParams(
            config.getSyncTimeParamStart(),
            config.getSyncTimeParamEnd(),
            startTime,
            endTime,
            config.getSyncTimeFormat()
        );
    }

    private SyncStats processSyncData(String dataType, JsonNode responseData, 
                                        InterfaceMappingTable mainMapping,
                                        List<InterfaceFieldMapping> mainFieldMappings,
                                        List<InterfaceMappingTable> allMappings) {
        SyncStats stats = new SyncStats();
        String targetTable = mainMapping.getTargetTable();

        for (JsonNode record : responseData) {
            stats.totalCount++;
            
            try {
                Map<String, Object> mappedData = FieldMappingProcessor.processRecord(record, mainFieldMappings);
                
                switch (targetTable) {
                    case "department":
                        processDepartment(mappedData, stats);
                        break;
                    case "his_staff":
                        processHisStaff(mappedData, stats);
                        break;
                    case "patient":
                        processPatient(mappedData, stats);
                        break;
                    case "visit":
                        processVisit(mappedData, stats);
                        break;
                    case "transfer_record":
                        processTransferRecord(mappedData, stats);
                        break;
                    case "diagnosis_main":
                        processDiagnosisMain(record, mappedData, stats, allMappings);
                        break;
                    case "order_main":
                        processOrderMain(record, mappedData, stats, allMappings);
                        break;
                    case "doctor_department":
                        processDoctorDepartment(mappedData, stats);
                        break;
                    default:
                        logger.warn("Unknown target table: {}", targetTable);
                        stats.skipCount++;
                }
            } catch (Exception e) {
                logger.error("Error processing record: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
                stats.failCount++;
            }
        }

        if ("doctor_department".equals(targetTable)) {
            cleanupDoctorDepartments();
        }

        return stats;
    }

    private void processDepartment(Map<String, Object> data, SyncStats stats) {
        String code = (String) data.get("code");
        if (code == null || code.isEmpty()) {
            stats.skipCount++;
            return;
        }

        Optional<Department> existing = departmentRepository.findByCode(code);

        if (existing.isPresent()) {
            Department dept = existing.get();
            EntityFieldMapper.mapFields(dept, data);
            departmentRepository.save(dept);
            stats.updateCount++;
        } else {
            Department dept = new Department();
            EntityFieldMapper.mapFields(dept, data);
            if (dept.getCode() == null) {
                dept.setCode(code);
            }
            if (dept.getBedCount() == null) {
                dept.setBedCount(0);
            }
            departmentRepository.save(dept);
            stats.insertCount++;
        }
    }

    private void processHisStaff(Map<String, Object> data, SyncStats stats) {
        String staffCode = (String) data.get("staff_code");
        if (staffCode == null || staffCode.isEmpty()) {
            stats.skipCount++;
            return;
        }

        String employmentStatus = (String) data.get("employment_status");
        String employmentStatusCode = (String) data.get("employment_status_code");
        boolean isActive = isActiveStaff(employmentStatus, employmentStatusCode);

        Optional<HisStaff> existing = hisStaffRepository.findByStaffCode(staffCode);

        HisStaff staff;
        if (existing.isPresent()) {
            staff = existing.get();
            EntityFieldMapper.mapFields(staff, data);
            if (staff.getName() == null || staff.getName().isEmpty()) {
                staff.setName(staffCode);
            }
            staff.setSyncTime(LocalDateTime.now());
            hisStaffRepository.save(staff);
            stats.updateCount++;
        } else {
            staff = new HisStaff();
            EntityFieldMapper.mapFields(staff, data);
            if (staff.getStaffCode() == null) {
                staff.setStaffCode(staffCode);
            }
            if (staff.getName() == null || staff.getName().isEmpty()) {
                staff.setName(staffCode);
            }
            staff.setSyncTime(LocalDateTime.now());
            hisStaffRepository.save(staff);
            stats.insertCount++;
        }

        syncUserFromHisStaff(staff, isActive);
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
    
    private void syncUserFromHisStaff(HisStaff staff, boolean isActive) {
        // username 用于显示名称，使用 his_staff.name_user (HIS用户姓名)
        // 如果 name_user 为空，则使用 staff_code 作为备选
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
            logger.info("User with username {} already exists but not linked to his_staff, skipping", username);
            return;
        }
        
        Role doctorRole = roleRepository.findByCode("DOCTOR");
        if (doctorRole == null) {
            doctorRole = roleRepository.findAll().stream()
                .filter(r -> "DOCTOR".equalsIgnoreCase(r.getCode()) || "医生".equals(r.getName()))
                .findFirst()
                .orElse(null);
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
        
        logger.info("Created user {} for his_staff {}, active={}", username, staff.getId(), isActive);
    }

    private void processPatient(Map<String, Object> data, SyncStats stats) {
        String patientNo = (String) data.get("patient_no");
        if (patientNo == null || patientNo.isEmpty()) {
            stats.skipCount++;
            return;
        }

        Optional<Patient> existing = patientRepository.findByPatientNo(patientNo);

        if (existing.isPresent()) {
            Patient patient = existing.get();
            EntityFieldMapper.mapFields(patient, data);
            patient.setSyncTime(LocalDateTime.now());
            patientRepository.save(patient);
            stats.updateCount++;
        } else {
            Patient patient = new Patient();
            EntityFieldMapper.mapFields(patient, data);
            if (patient.getPatientNo() == null) {
                patient.setPatientNo(patientNo);
            }
            patient.setSyncTime(LocalDateTime.now());
            patientRepository.save(patient);
            stats.insertCount++;
        }
    }

    private void processVisit(Map<String, Object> data, SyncStats stats) {
        String visitNo = (String) data.get("visit_no");
        if (visitNo == null || visitNo.isEmpty()) {
            logger.warn("Visit sync skipped: visit_no is null");
            stats.skipCount++;
            return;
        }

        Optional<Visit> existing = visitRepository.findByVisitNo(visitNo);
        
        Visit visit;
        if (existing.isPresent()) {
            visit = existing.get();
            EntityFieldMapper.mapFields(visit, data);
        } else {
            visit = new Visit();
            EntityFieldMapper.mapFields(visit, data);
            if (visit.getVisitNo() == null) {
                visit.setVisitNo(visitNo);
            }
        }

        // 处理 dept_id：根据 dept_his_id (科室编码) 查询 Department
        String deptHisId = (String) data.get("dept_his_id");
        if (visit.getDepartmentId() == null && deptHisId != null && !deptHisId.isEmpty()) {
            Optional<Department> deptOpt = departmentRepository.findByCode(deptHisId);
            if (deptOpt.isPresent()) {
                visit.setDepartmentId(deptOpt.get().getId());
                logger.debug("Found department {} for code {}", deptOpt.get().getId(), deptHisId);
            } else {
                logger.warn("Department not found for code: {}", deptHisId);
            }
        }

        // 处理 patient_id：如果缺失，根据 patient_his_id 创建或查找患者
        if (visit.getPatientId() == null) {
            String patientHisId = (String) data.get("patient_his_id");
            String patientNo = (String) data.get("patient_no");
            String patientName = (String) data.get("patient_name");
            
            // 尝试根据 patient_no 查找患者
            if (patientNo != null && !patientNo.isEmpty()) {
                Optional<Patient> patientOpt = patientRepository.findByPatientNo(patientNo);
                if (patientOpt.isPresent()) {
                    visit.setPatientId(patientOpt.get().getId());
                } else {
                    // 创建新患者
                    Patient newPatient = new Patient();
                    newPatient.setPatientNo(patientNo);
                    newPatient.setName(patientName != null ? patientName : "未知");
                    newPatient.setGender("未知");
                    newPatient.setSyncTime(LocalDateTime.now());
                    Patient savedPatient = patientRepository.save(newPatient);
                    visit.setPatientId(savedPatient.getId());
                    logger.info("Created new patient {} with patient_no {}", savedPatient.getId(), patientNo);
                }
            } else if (patientHisId != null && !patientHisId.isEmpty()) {
                // 如果只有 patient_his_id，用它作为 patient_no 创建患者
                Optional<Patient> patientOpt = patientRepository.findByPatientNo(patientHisId);
                if (patientOpt.isPresent()) {
                    visit.setPatientId(patientOpt.get().getId());
                } else {
                    Patient newPatient = new Patient();
                    newPatient.setPatientNo(patientHisId);
                    newPatient.setName(patientName != null ? patientName : "未知");
                    newPatient.setGender("未知");
                    newPatient.setSyncTime(LocalDateTime.now());
                    Patient savedPatient = patientRepository.save(newPatient);
                    visit.setPatientId(savedPatient.getId());
                    logger.info("Created new patient {} with patient_his_id {}", savedPatient.getId(), patientHisId);
                }
            }
        }

        // 处理 admission_datetime 默认值
        if (visit.getAdmissionDatetime() == null) {
            visit.setAdmissionDatetime(LocalDateTime.now());
            logger.debug("Set default admission_datetime for visit {}", visitNo);
        }

        // 处理 dept_name 默认值
        if (visit.getDeptName() == null || visit.getDeptName().isEmpty()) {
            visit.setDeptName("未知科室");
        }

        visit.setSyncTime(LocalDateTime.now());
        visitRepository.save(visit);
        
        if (existing.isPresent()) {
            stats.updateCount++;
        } else {
            stats.insertCount++;
        }
    }

    private void processTransferRecord(Map<String, Object> data, SyncStats stats) {
        String visitNo = (String) data.get("visit_no");
        if (visitNo == null || visitNo.isEmpty()) {
            stats.skipCount++;
            return;
        }

        List<TransferRecord> existing = transferRecordRepository.findByVisitNo(visitNo);

        if (!existing.isEmpty()) {
            TransferRecord record = existing.get(0);
            EntityFieldMapper.mapFields(record, data);
            record.setSyncTime(LocalDateTime.now());
            transferRecordRepository.save(record);
            stats.updateCount++;
        } else {
            TransferRecord record = new TransferRecord();
            EntityFieldMapper.mapFields(record, data);
            if (record.getVisitNo() == null) {
                record.setVisitNo(visitNo);
            }
            record.setSyncTime(LocalDateTime.now());
            transferRecordRepository.save(record);
            stats.insertCount++;
        }
    }

    private void processDiagnosisMain(JsonNode sourceRecord, Map<String, Object> data, 
                                       SyncStats stats, List<InterfaceMappingTable> allMappings) {
        String hisId = (String) data.get("his_id");
        if (hisId == null || hisId.isEmpty()) {
            stats.skipCount++;
            return;
        }

        Optional<DiagnosisMain> existing = diagnosisMainRepository.findByHisId(hisId);
        
        if (existing.isPresent()) {
            stats.skipCount++;
        } else {
            DiagnosisMain main = new DiagnosisMain();
            EntityFieldMapper.mapFields(main, data);
            if (main.getHisId() == null) {
                main.setHisId(hisId);
            }
            if (main.getStatus() == null) {
                main.setStatus("ACTIVE");
            }

            // 处理 patient_id 关联
            if (main.getPatientId() == null) {
                String patientNo = (String) data.get("patient_no");
                String patientHisId = (String) data.get("patient_his_id");
                
                if (patientNo != null && !patientNo.isEmpty()) {
                    Optional<Patient> patientOpt = patientRepository.findByPatientNo(patientNo);
                    if (patientOpt.isPresent()) {
                        main.setPatientId(patientOpt.get().getId());
                    } else {
                        Patient newPatient = new Patient();
                        newPatient.setPatientNo(patientNo);
                        newPatient.setName("未知");
                        newPatient.setGender("未知");
                        newPatient.setSyncTime(LocalDateTime.now());
                        Patient savedPatient = patientRepository.save(newPatient);
                        main.setPatientId(savedPatient.getId());
                        logger.debug("Created new patient for diagnosis: {}", savedPatient.getId());
                    }
                } else if (patientHisId != null && !patientHisId.isEmpty()) {
                    Optional<Patient> patientOpt = patientRepository.findByPatientNo(patientHisId);
                    if (patientOpt.isPresent()) {
                        main.setPatientId(patientOpt.get().getId());
                    } else {
                        Patient newPatient = new Patient();
                        newPatient.setPatientNo(patientHisId);
                        newPatient.setName("未知");
                        newPatient.setGender("未知");
                        newPatient.setSyncTime(LocalDateTime.now());
                        Patient savedPatient = patientRepository.save(newPatient);
                        main.setPatientId(savedPatient.getId());
                        logger.debug("Created new patient for diagnosis: {}", savedPatient.getId());
                    }
                }
            }

            // 处理 visit_id 关联
            if (main.getVisitId() == null) {
                String visitNo = (String) data.get("visit_no");
                if (visitNo != null && !visitNo.isEmpty()) {
                    Optional<Visit> visitOpt = visitRepository.findByVisitNo(visitNo);
                    if (visitOpt.isPresent()) {
                        main.setVisitId(visitOpt.get().getId());
                    }
                }
            }

            // 如果 patient_id 或 visit_id 为空，跳过
            if (main.getPatientId() == null) {
                stats.skipCount++;
                logger.warn("Diagnosis skipped: patient_id is null for his_id={}", hisId);
                return;
            }
            
            if (main.getVisitId() == null) {
                stats.skipCount++;
                logger.warn("Diagnosis skipped: visit_id is null for his_id={}", hisId);
                return;
            }

            main.setSyncTime(LocalDateTime.now());
            
            DiagnosisMain savedMain = diagnosisMainRepository.save(main);
            
            processDiagnosisItems(sourceRecord, savedMain.getId(), stats, allMappings);
            stats.insertCount++;
        }
    }

    private void processDiagnosisItems(JsonNode sourceRecord, Long mainId, 
                                        SyncStats stats, List<InterfaceMappingTable> allMappings) {
        Optional<InterfaceMappingTable> itemMappingOpt = allMappings.stream()
            .filter(t -> "diagnosis_item".equals(t.getTargetTable()))
            .findFirst();
        
        if (!itemMappingOpt.isPresent()) {
            return;
        }

        InterfaceMappingTable itemMapping = itemMappingOpt.get();
        String dataPath = itemMapping.getDataPath();
        
        if (dataPath == null || dataPath.isEmpty()) {
            return;
        }

        JsonNode diitmNode = sourceRecord.get(dataPath);
        if (diitmNode == null || !diitmNode.isTextual()) {
            return;
        }

        try {
            String diitmStr = diitmNode.asText();
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            JsonNode itemsArray = mapper.readTree(diitmStr);
            
            List<InterfaceFieldMapping> itemFieldMappings = fieldMappingRepository.findByMappingTableId(itemMapping.getId());
            
            if (itemsArray.isArray()) {
                for (JsonNode item : itemsArray) {
                    Map<String, Object> itemData = FieldMappingProcessor.processRecord(item, itemFieldMappings);
                    
                    String diagnosisCode = (String) itemData.get("diagnosis_code");
                    if (diagnosisCode == null || diagnosisCode.isEmpty()) {
                        continue;
                    }

                    Optional<DiagnosisItem> existingItem = diagnosisItemRepository.findByMainIdAndDiagnosisCode(mainId, diagnosisCode);
                    
                    if (!existingItem.isPresent()) {
                        DiagnosisItem diagnosisItem = new DiagnosisItem();
                        EntityFieldMapper.mapFields(diagnosisItem, itemData);
                        if (diagnosisItem.getMainId() == null) {
                            diagnosisItem.setMainId(mainId);
                        }
                        if (diagnosisItem.getDiagnosisCode() == null) {
                            diagnosisItem.setDiagnosisCode(diagnosisCode);
                        }
                        diagnosisItemRepository.save(diagnosisItem);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error processing diagnosis items: {}", e.getMessage());
        }
    }

    private void processOrderMain(JsonNode sourceRecord, Map<String, Object> data, 
                                   SyncStats stats, List<InterfaceMappingTable> allMappings) {
        String orderNo = (String) data.get("order_no");
        if (orderNo == null || orderNo.isEmpty()) {
            stats.skipCount++;
            return;
        }

        Optional<OrderMain> existing = orderMainRepository.findByOrderNo(orderNo);
        OrderMain savedMain;
        
        if (existing.isPresent()) {
            OrderMain main = existing.get();
            EntityFieldMapper.mapFields(main, data);
            main.setSyncTime(LocalDateTime.now());
            savedMain = orderMainRepository.save(main);
            stats.updateCount++;
        } else {
            OrderMain main = new OrderMain();
            EntityFieldMapper.mapFields(main, data);
            if (main.getOrderNo() == null) {
                main.setOrderNo(orderNo);
            }

            // 处理 visit_id 关联
            if (main.getVisitId() == null) {
                String visitNo = (String) data.get("visit_no");
                if (visitNo != null && !visitNo.isEmpty()) {
                    Optional<Visit> visitOpt = visitRepository.findByVisitNo(visitNo);
                    if (visitOpt.isPresent()) {
                        main.setVisitId(visitOpt.get().getId());
                    }
                }
            }

            // 处理 patient_id 关联
            if (main.getPatientId() == null) {
                String patientNo = (String) data.get("patient_no");
                if (patientNo != null && !patientNo.isEmpty()) {
                    Optional<Patient> patientOpt = patientRepository.findByPatientNo(patientNo);
                    if (patientOpt.isPresent()) {
                        main.setPatientId(patientOpt.get().getId());
                    } else {
                        Patient newPatient = new Patient();
                        newPatient.setPatientNo(patientNo);
                        newPatient.setName("未知");
                        newPatient.setGender("未知");
                        newPatient.setSyncTime(LocalDateTime.now());
                        Patient savedPatient = patientRepository.save(newPatient);
                        main.setPatientId(savedPatient.getId());
                        logger.debug("Created new patient for order: {}", savedPatient.getId());
                    }
                }
            }

            // 处理 doctor_id 关联
            if (main.getDoctorId() == null) {
                String doctorCode = (String) data.get("doctor_code");
                if (doctorCode != null && !doctorCode.isEmpty()) {
                    Optional<HisStaff> staffOpt = hisStaffRepository.findByStaffCode(doctorCode);
                    if (staffOpt.isPresent()) {
                        main.setDoctorId(staffOpt.get().getId());
                    }
                }
            }

            // 设置默认值
            if (main.getOrderType() == null || main.getOrderType().isEmpty()) {
                main.setOrderType("L");
            }
            if (main.getOrderCategory() == null || main.getOrderCategory().isEmpty()) {
                main.setOrderCategory("常规");
            }
            if (main.getStatus() == null || main.getStatus().isEmpty()) {
                main.setStatus("ACTIVE");
            }
            if (main.getStartTime() == null) {
                main.setStartTime(LocalDateTime.now());
            }

            // 如果 visit_id 或 patient_id 为空，跳过
            if (main.getVisitId() == null) {
                stats.skipCount++;
                logger.warn("Order skipped: visit_id is null for order_no={}", orderNo);
                return;
            }
            if (main.getPatientId() == null) {
                stats.skipCount++;
                logger.warn("Order skipped: patient_id is null for order_no={}", orderNo);
                return;
            }

            main.setSyncTime(LocalDateTime.now());
            savedMain = orderMainRepository.save(main);
            stats.insertCount++;
        }
        
        processOrderItems(sourceRecord, savedMain.getId(), stats, allMappings);
    }

    private void processOrderItems(JsonNode sourceRecord, Long mainId, 
                                    SyncStats stats, List<InterfaceMappingTable> allMappings) {
        Optional<InterfaceMappingTable> itemMappingOpt = allMappings.stream()
            .filter(t -> "order_item".equals(t.getTargetTable()))
            .findFirst();
        
        if (!itemMappingOpt.isPresent()) {
            return;
        }

        InterfaceMappingTable itemMapping = itemMappingOpt.get();
        String dataPath = itemMapping.getDataPath();
        
        if (dataPath == null || dataPath.isEmpty()) {
            return;
        }

        JsonNode ciitmNode = sourceRecord.get(dataPath);
        if (ciitmNode == null || !ciitmNode.isTextual()) {
            return;
        }

        try {
            String ciitmStr = ciitmNode.asText();
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            JsonNode itemsArray = mapper.readTree(ciitmStr);
            
            List<InterfaceFieldMapping> itemFieldMappings = fieldMappingRepository.findByMappingTableId(itemMapping.getId());
            
            if (itemsArray.isArray()) {
                for (JsonNode item : itemsArray) {
                    Map<String, Object> itemData = FieldMappingProcessor.processRecord(item, itemFieldMappings);
                    
                    String itemId = (String) itemData.get("item_id");
                    if (itemId == null || itemId.isEmpty()) {
                        continue;
                    }

                    Optional<OrderItem> existingItem = orderItemRepository.findByItemId(itemId);
                    
                    if (existingItem.isPresent()) {
                        OrderItem orderItem = existingItem.get();
                        EntityFieldMapper.mapFields(orderItem, itemData);
                        orderItemRepository.save(orderItem);
                    } else {
                        OrderItem orderItem = new OrderItem();
                        EntityFieldMapper.mapFields(orderItem, itemData);
                        if (orderItem.getMainId() == null) {
                            orderItem.setMainId(mainId);
                        }
                        if (orderItem.getItemId() == null) {
                            orderItem.setItemId(itemId);
                        }
                        orderItemRepository.save(orderItem);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error processing order items: {}", e.getMessage());
        }
    }
    
    private void disableInactiveStaffUsers() {
        try {
            LocalDateTime threshold = LocalDateTime.now().minusHours(25);
            
            List<HisStaff> inactiveStaff = hisStaffRepository.findAll().stream()
                .filter(staff -> staff.getSyncTime() == null || staff.getSyncTime().isBefore(threshold))
                .toList();
            
            for (HisStaff staff : inactiveStaff) {
                User user = userRepository.findByHisStaffId(staff.getId());
                if (user != null && user.getEnabled()) {
                    user.setEnabled(false);
                    userRepository.save(user);
                    logger.info("Disabled user {} for inactive his_staff {}", user.getUsername(), staff.getId());
                }
            }
        } catch (Exception e) {
            logger.error("Error disabling inactive staff users: {}", e.getMessage());
        }
    }

    private Map<Long, Set<Long>> incomingDoctorDepartments = new HashMap<>();
    
    private void processDoctorDepartment(Map<String, Object> data, SyncStats stats) {
        String codeUser = (String) data.get("code_user");
        String codeDept = (String) data.get("code_dept");
        
        if (codeUser == null || codeUser.isEmpty() || codeDept == null || codeDept.isEmpty()) {
            stats.skipCount++;
            return;
        }
        
        Optional<HisStaff> hisStaffOpt = hisStaffRepository.findByCodeUser(codeUser);
        if (!hisStaffOpt.isPresent()) {
            stats.skipCount++;
            return;
        }
        
        User user = userRepository.findByHisStaffId(hisStaffOpt.get().getId());
        if (user == null) {
            stats.skipCount++;
            return;
        }
        
        Long doctorId = user.getId();
        
        Optional<Department> deptOpt = departmentRepository.findByCode(codeDept);
        if (!deptOpt.isPresent()) {
            stats.skipCount++;
            return;
        }
        
        Long departmentId = deptOpt.get().getId();
        
        Optional<DoctorDepartment> existing = doctorDepartmentRepository.findByDoctorIdAndDepartmentId(doctorId, departmentId);
        
        if (existing.isPresent()) {
            stats.skipCount++;
        } else {
            DoctorDepartment dd = new DoctorDepartment();
            dd.setDoctorId(doctorId);
            dd.setDepartmentId(departmentId);
            dd.setIsPrimary(false);
            doctorDepartmentRepository.save(dd);
            stats.insertCount++;
        }
    }
    
    public void cleanupDoctorDepartments() {
        logger.info("Recalculating is_primary for all doctor_department records");
        int updated = doctorDepartmentRepository.recalculateIsPrimary();
        logger.info("Completed is_primary recalculation, {} records updated", updated);
    }
    
    public BatchSyncResultDto executeBatchSync(String deptCode) {
        BatchSyncResultDto result = new BatchSyncResultDto();
        long startMillis = System.currentTimeMillis();
        
        List<InterfaceConfig> configs = configRepository.findByDeptParamNotNullAndEnabledTrue();
        
        if (configs.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("未找到需要科室参数的接口配置");
            result.setTotalCount(0);
            result.setSuccessCount(0);
            result.setFailedCount(0);
            result.setDurationMs(System.currentTimeMillis() - startMillis);
            return result;
        }
        
        List<SyncItemResultDto> items = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;
        
        for (InterfaceConfig config : configs) {
            SyncItemResultDto itemResult = new SyncItemResultDto();
            itemResult.setConfigName(config.getConfigName());
            
            try {
                SyncResultDto syncResult = executeSync(config.getId(), deptCode);
                
                itemResult.setSuccess(syncResult.getSuccess());
                itemResult.setMessage(syncResult.getMessage());
                itemResult.setTotalCount(syncResult.getTotalCount());
                itemResult.setInsertCount(syncResult.getInsertCount());
                itemResult.setUpdateCount(syncResult.getUpdateCount());
                itemResult.setSkipCount(syncResult.getSkipCount());
                
                if (syncResult.getSuccess()) {
                    successCount++;
                } else {
                    failedCount++;
                }
            } catch (Exception e) {
                logger.error("批量同步失败: {}", config.getConfigName(), e);
                itemResult.setSuccess(false);
                itemResult.setMessage("同步失败：" + e.getMessage());
                itemResult.setTotalCount(0);
                itemResult.setInsertCount(0);
                itemResult.setUpdateCount(0);
                itemResult.setSkipCount(0);
                failedCount++;
            }
            
            items.add(itemResult);
        }
        
        result.setItems(items);
        result.setTotalCount(configs.size());
        result.setSuccessCount(successCount);
        result.setFailedCount(failedCount);
        result.setDurationMs(System.currentTimeMillis() - startMillis);
        
        if (failedCount == 0) {
            result.setSuccess(true);
            result.setMessage("批量同步成功，共" + configs.size() + "个接口");
        } else if (successCount == 0) {
            result.setSuccess(false);
            result.setMessage("批量同步失败，共" + configs.size() + "个接口");
        } else {
            result.setSuccess(true);
            result.setMessage("批量同步完成，成功" + successCount + "个，失败" + failedCount + "个");
        }
        
        return result;
    }

    private static class SyncStats {
        int totalCount = 0;
        int insertCount = 0;
        int updateCount = 0;
        int skipCount = 0;
        int failCount = 0;
    }
}