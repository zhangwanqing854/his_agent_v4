package com.hospital.handover.service;

import com.hospital.handover.dto.*;
import com.hospital.handover.entity.*;
import com.hospital.handover.repository.*;
import com.hospital.handover.util.AgeCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HandoverService {

    private final ShiftHandoverRepository shiftHandoverRepository;
    private final HandoverPatientRepository handoverPatientRepository;
    private final VisitRepository visitRepository;
    private final PatientRepository patientRepository;
    private final OrderMainRepository orderMainRepository;
    private final OrderItemRepository orderItemRepository;
    private final DepartmentRepository departmentRepository;
    private final TransferRecordRepository transferRecordRepository;
    private final SchedulingDetailRepository schedulingDetailRepository;
    private final DiagnosisMainRepository diagnosisMainRepository;
    private final DiagnosisItemRepository diagnosisItemRepository;
    private final HisStaffRepository hisStaffRepository;
    private final HandoverTodoRepository handoverTodoRepository;
    private final SmsNotificationService smsNotificationService;
    private final HandoverNoGenerator handoverNoGenerator;

    public HandoverService(ShiftHandoverRepository shiftHandoverRepository,
                          HandoverPatientRepository handoverPatientRepository,
                          VisitRepository visitRepository,
                          PatientRepository patientRepository,
                          OrderMainRepository orderMainRepository,
                          OrderItemRepository orderItemRepository,
                          DepartmentRepository departmentRepository,
                          TransferRecordRepository transferRecordRepository,
                          SchedulingDetailRepository schedulingDetailRepository,
                          DiagnosisMainRepository diagnosisMainRepository,
                          DiagnosisItemRepository diagnosisItemRepository,
                          HisStaffRepository hisStaffRepository,
                          HandoverTodoRepository handoverTodoRepository,
                          SmsNotificationService smsNotificationService,
                          HandoverNoGenerator handoverNoGenerator) {
        this.shiftHandoverRepository = shiftHandoverRepository;
        this.handoverPatientRepository = handoverPatientRepository;
        this.visitRepository = visitRepository;
        this.patientRepository = patientRepository;
        this.orderMainRepository = orderMainRepository;
        this.orderItemRepository = orderItemRepository;
        this.departmentRepository = departmentRepository;
        this.transferRecordRepository = transferRecordRepository;
        this.schedulingDetailRepository = schedulingDetailRepository;
        this.diagnosisMainRepository = diagnosisMainRepository;
        this.diagnosisItemRepository = diagnosisItemRepository;
        this.hisStaffRepository = hisStaffRepository;
        this.handoverTodoRepository = handoverTodoRepository;
        this.smsNotificationService = smsNotificationService;
        this.handoverNoGenerator = handoverNoGenerator;
    }

    public List<HandoverDto> getHandoverList(Long deptId) {
        List<ShiftHandover> handovers = shiftHandoverRepository.findByDeptIdOrderByHandoverDateDesc(deptId);
        return handovers.stream()
            .map(this::toHandoverDto)
            .collect(Collectors.toList());
    }

    public HandoverDto getHandoverById(Long id) {
        Optional<ShiftHandover> handover = shiftHandoverRepository.findById(id);
        return handover.map(this::toHandoverDto).orElse(null);
    }

    public List<HandoverPatientDto> getHandoverPatients(Long handoverId) {
        List<HandoverPatient> patients = handoverPatientRepository.findByHandoverIdOrderByBedNo(handoverId);
        return patients.stream()
            .map(this::toHandoverPatientDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public HandoverDto createHandover(HandoverCreateRequest request) {
        Optional<Department> dept = departmentRepository.findById(request.getDeptId());
        if (!dept.isPresent()) {
            return null;
        }

        ShiftHandover handover = new ShiftHandover();
        handover.setDeptId(request.getDeptId());
        handover.setDeptName(dept.get().getName());
        handover.setHandoverDate(request.getHandoverDate());
        handover.setShift(request.getShift());
        handover.setFromDoctorId(request.getFromDoctorId());
        handover.setFromDoctorName(getDoctorName(request.getFromDoctorId()));
        handover.setToDoctorId(request.getToDoctorId());
        handover.setToDoctorName(getDoctorName(request.getToDoctorId()));
        handover.setStatus("DRAFT");
        handover.setHandoverNo(handoverNoGenerator.generateHandoverNo());

        ShiftHandover saved = shiftHandoverRepository.save(handover);

        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        LocalDateTime now = LocalDateTime.now();
        List<Visit> visits = visitRepository.findHandoverPatients(request.getDeptId(), cutoffTime, now);

        for (Visit visit : visits) {
            HandoverPatient hp = createHandoverPatient(saved.getId(), visit, cutoffTime);
            handoverPatientRepository.save(hp);
        }

        return toHandoverDto(saved);
    }

    private String getDoctorName(Long doctorId) {
        if (doctorId == null) {
            return null;
        }
        Optional<HisStaff> staff = hisStaffRepository.findById(doctorId);
        return staff.map(HisStaff::getName).orElse("医生" + doctorId);
    }

    private HandoverPatient createHandoverPatient(Long handoverId, Visit visit, LocalDateTime cutoffTime) {
        HandoverPatient hp = new HandoverPatient();
        hp.setHandoverId(handoverId);
        hp.setVisitId(visit.getId());
        hp.setPatientId(visit.getPatientId());
        hp.setBedNo(visit.getBedNo() != null ? visit.getBedNo() : "");
        hp.setDiagnosis("");

        String filterReason = determineFilterReason(visit, cutoffTime);
        hp.setFilterReason(filterReason);

        Optional<Patient> patient = patientRepository.findById(visit.getPatientId());
        if (patient.isPresent()) {
            hp.setPatientName(patient.get().getName() != null ? patient.get().getName() : "");
            hp.setGender(patient.get().getGender() != null ? patient.get().getGender() : "");
            hp.setAge(AgeCalculator.calculateAgeOrDefault(patient.get().getBirthDate(), patient.get().getAge() != null ? patient.get().getAge() : 0));
        } else {
            hp.setPatientName("");
            hp.setGender("");
            hp.setAge(0);
        }

        String currentCondition = buildCurrentCondition(visit.getId(), cutoffTime);
        hp.setCurrentCondition(currentCondition);

        return hp;
    }

    private String determineFilterReason(Visit visit, LocalDateTime cutoffTime) {
        boolean isNewAdmission = visit.getAdmissionDatetime() != null 
            && visit.getAdmissionDatetime().isAfter(cutoffTime);
        boolean isLevel1Nursing = "01".equals(visit.getNurseLevelCode());

        if (isNewAdmission && isLevel1Nursing) {
            return "24小时内新入院+一级护理";
        } else if (isNewAdmission) {
            return "24小时内新入院";
        } else if (isLevel1Nursing) {
            return "一级护理";
        }
        return "";
    }

    private String buildCurrentCondition(Long visitId, LocalDateTime cutoffTime) {
        List<OrderMain> orders = orderMainRepository.findTemporaryOrdersWithin24h(visitId, cutoffTime);
        if (orders.isEmpty()) {
            return "";
        }
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        return orders.stream()
            .map(o -> {
                List<OrderItem> items = orderItemRepository.findByMainId(o.getId());
                
                StringBuilder sb = new StringBuilder();
                sb.append(o.getStartTime().format(timeFormatter));
                sb.append(" ").append(o.getOrderName());
                
                if (!items.isEmpty()) {
                    OrderItem item = items.get(0);
                    if (item.getRoute() != null && !item.getRoute().isEmpty()) {
                        sb.append(" ").append(item.getRoute());
                    }
                    if (o.getServiceType() != null && o.getServiceType().startsWith("01")) {
                        if (item.getDosage() != null && !item.getDosage().isEmpty()) {
                            sb.append(" ").append(item.getDosage());
                            if (item.getDosageUnit() != null && !item.getDosageUnit().isEmpty()) {
                                sb.append(item.getDosageUnit());
                            }
                        }
                    }
                }
                
                String content = sb.toString();
                if (content.length() > 100) {
                    content = content.substring(0, 100) + "...";
                }
                return content;
            })
            .collect(Collectors.joining("\n"));
    }

    private HandoverDto toHandoverDto(ShiftHandover handover) {
        HandoverDto dto = new HandoverDto();
        dto.setId(handover.getId());
        dto.setDeptId(handover.getDeptId());
        dto.setDeptName(handover.getDeptName());
        dto.setHandoverNo(handover.getHandoverNo());
        dto.setHandoverDate(handover.getHandoverDate());
        dto.setShift(handover.getShift());
        dto.setFromDoctorId(handover.getFromDoctorId());
        dto.setFromDoctorName(handover.getFromDoctorName() != null ? handover.getFromDoctorName() : getDoctorName(handover.getFromDoctorId()));
        dto.setToDoctorId(handover.getToDoctorId());
        dto.setToDoctorName(handover.getToDoctorName() != null ? handover.getToDoctorName() : getDoctorName(handover.getToDoctorId()));
        dto.setStatus(handover.getStatus());
        dto.setCreatedAt(handover.getCreatedAt());
        dto.setUpdatedAt(handover.getUpdatedAt());

        List<HandoverPatient> patients = handoverPatientRepository.findByHandoverIdOrderByBedNo(handover.getId());
        dto.setPatientCount(patients.size());

        return dto;
    }

    private HandoverPatientDto toHandoverPatientDto(HandoverPatient hp) {
        HandoverPatientDto dto = new HandoverPatientDto();
        dto.setId(hp.getId());
        dto.setVisitId(hp.getVisitId());
        dto.setPatientId(hp.getPatientId());
        dto.setPatientName(hp.getPatientName());
        dto.setGender(hp.getGender());
        
        if (hp.getPatientId() != null) {
            Optional<Patient> patient = patientRepository.findById(hp.getPatientId());
            if (patient.isPresent() && patient.get().getBirthDate() != null) {
                dto.setAge(AgeCalculator.calculateAge(patient.get().getBirthDate()));
            } else {
                dto.setAge(hp.getAge());
            }
        } else {
            dto.setAge(hp.getAge());
        }
        
        dto.setBedNo(hp.getBedNo());
        dto.setDiagnosis(hp.getDiagnosis());
        dto.setFilterReason(hp.getFilterReason());
        dto.setCurrentCondition(hp.getCurrentCondition());
        dto.setVitals(hp.getVitals());
        dto.setObservationItems(hp.getObservationItems());
        dto.setMewsScore(hp.getMewsScore());
        dto.setBradenScore(hp.getBradenScore());
        dto.setFallRisk(hp.getFallRisk());
        return dto;
    }

    public HandoverStatsDto calculateStats(Long deptId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoffTime = now.minusHours(24);

        HandoverStatsDto dto = new HandoverStatsDto();
        dto.setTotalPatients(visitRepository.countTotalPatients(deptId, now));
        dto.setAdmission(visitRepository.countAdmission24h(deptId, cutoffTime, now));
        dto.setDischarge(visitRepository.countDischarge24h(deptId, cutoffTime));
        dto.setTransferOut(transferRecordRepository.countTransferOut24h(deptId, cutoffTime));
        dto.setTransferIn(transferRecordRepository.countTransferIn24h(deptId, cutoffTime));

        return dto;
    }

    public DutyStaffDto getDutyStaff(Long deptId) {
        LocalDate today = LocalDate.now();
        String yearMonth = String.format("%d-%02d", today.getYear(), today.getMonthValue());
        
        List<Object[]> results = schedulingDetailRepository.findTodayDutyStaff(deptId, yearMonth, today);
        if (results.isEmpty()) {
            return null;
        }

        DutyStaffDto dto = new DutyStaffDto();
        dto.setStaffId((Long) results.get(0)[0]);
        dto.setStaffName((String) results.get(0)[1]);
        return dto;
    }

    public List<HandoverPatientDto> getHandoverPatientsForCreate(Long deptId) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        LocalDateTime now = LocalDateTime.now();
        
        List<Visit> visits = visitRepository.findHandoverPatients(deptId, cutoffTime, now);
        
        return visits.stream()
            .map(v -> {
                HandoverPatientDto dto = new HandoverPatientDto();
                dto.setVisitId(v.getId());
                dto.setPatientId(v.getPatientId());
                dto.setPatientName(v.getPatientName());
                dto.setBedNo(v.getBedNo() != null ? v.getBedNo() : "");
                dto.setDiagnosis(getDiagnosis(v.getId()));
                dto.setFilterReason(determineFilterReason(v, cutoffTime));
                dto.setCurrentCondition(buildCurrentCondition(v.getId(), cutoffTime));
                
                Optional<Patient> patient = patientRepository.findById(v.getPatientId());
                if (patient.isPresent()) {
                    dto.setGender(patient.get().getGender());
                    dto.setAge(AgeCalculator.calculateAgeOrDefault(patient.get().getBirthDate(), patient.get().getAge() != null ? patient.get().getAge() : 0));
                } else {
                    dto.setGender("");
                    dto.setAge(0);
                }
                
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    private String getDiagnosis(Long visitId) {
        List<DiagnosisMain> diagnosisMains = diagnosisMainRepository.findByVisitIdOrderByDiagnosisTimeDesc(visitId);
        if (diagnosisMains.isEmpty()) {
            return "";
        }
        
        DiagnosisMain latestDiagnosis = diagnosisMains.get(0);
        List<DiagnosisItem> mainDiagnosis = diagnosisItemRepository.findByMainIdAndIsMainOrderBySortOrder(latestDiagnosis.getId(), true);
        
        if (mainDiagnosis.isEmpty()) {
            return "";
        }
        
        return mainDiagnosis.stream()
            .map(DiagnosisItem::getDiagnosisName)
            .collect(Collectors.joining("; "));
    }

    @Transactional
    public boolean deleteHandover(Long id) {
        Optional<ShiftHandover> handover = shiftHandoverRepository.findById(id);
        if (!handover.isPresent()) {
            return false;
        }
        
        ShiftHandover sh = handover.get();
        if (!"DRAFT".equals(sh.getStatus())) {
            return false;
        }
        
        handoverPatientRepository.deleteByHandoverId(id);
        shiftHandoverRepository.deleteById(id);
        return true;
    }

    @Transactional
    public HandoverDto updateHandover(Long id, HandoverCreateRequest request) {
        Optional<ShiftHandover> existing = shiftHandoverRepository.findById(id);
        if (!existing.isPresent()) {
            return null;
        }
        
        ShiftHandover handover = existing.get();
        if (!"DRAFT".equals(handover.getStatus())) {
            return null;
        }
        
        Optional<Department> dept = departmentRepository.findById(request.getDeptId());
        if (!dept.isPresent()) {
            return null;
        }
        
        handover.setDeptId(request.getDeptId());
        handover.setDeptName(dept.get().getName());
        handover.setHandoverDate(request.getHandoverDate());
        handover.setShift(request.getShift());
        handover.setToDoctorId(request.getToDoctorId());
        handover.setToDoctorName(getDoctorName(request.getToDoctorId()));
        
        ShiftHandover saved = shiftHandoverRepository.save(handover);
        return toHandoverDto(saved);
    }

    @Transactional
    public HandoverDto submitHandover(Long id, Long currentDoctorId) {
        Optional<ShiftHandover> existing = shiftHandoverRepository.findById(id);
        if (!existing.isPresent()) {
            return null;
        }
        
        ShiftHandover handover = existing.get();
        if (!"DRAFT".equals(handover.getStatus())) {
            return null;
        }
        
        if (handover.getFromDoctorId() == null || !handover.getFromDoctorId().equals(currentDoctorId)) {
            return null;
        }
        
        if (handover.getToDoctorId() == null) {
            return null;
        }
        
        handover.setStatus("PENDING");
        ShiftHandover saved = shiftHandoverRepository.save(handover);
        
        smsNotificationService.sendHandoverNotification(saved.getId());
        
        return toHandoverDto(saved);
    }

    @Transactional
    public HandoverDto acceptHandover(Long id, Long currentDoctorId) {
        Optional<ShiftHandover> existing = shiftHandoverRepository.findById(id);
        if (!existing.isPresent()) {
            return null;
        }
        
        ShiftHandover handover = existing.get();
        String status = handover.getStatus();
        if (!"DRAFT".equals(status) && !"PENDING".equals(status)) {
            return null;
        }
        
        if (handover.getToDoctorId() == null || !handover.getToDoctorId().equals(currentDoctorId)) {
            return null;
        }
        
        handover.setStatus("COMPLETED");
        ShiftHandover saved = shiftHandoverRepository.save(handover);
        return toHandoverDto(saved);
    }

    @Transactional
    public HandoverDto rejectHandover(Long id, String reason, Long currentDoctorId) {
        Optional<ShiftHandover> existing = shiftHandoverRepository.findById(id);
        if (!existing.isPresent()) {
            return null;
        }
        
        ShiftHandover handover = existing.get();
        if (!"PENDING".equals(handover.getStatus())) {
            return null;
        }
        
        if (handover.getToDoctorId() == null || !handover.getToDoctorId().equals(currentDoctorId)) {
            return null;
        }
        
        handover.setStatus("DRAFT");
        handover.setRejectReason(reason);
        ShiftHandover saved = shiftHandoverRepository.save(handover);
        return toHandoverDto(saved);
    }

    public List<TodoItemDto> getHandoverTodos(Long handoverId) {
        List<HandoverTodo> todos = handoverTodoRepository.findByHandoverIdOrderByCreatedAtAsc(handoverId);
        return todos.stream()
            .map(this::toTodoItemDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public TodoItemDto createHandoverTodo(Long handoverId, String content, LocalDateTime dueTime) {
        Optional<ShiftHandover> handover = shiftHandoverRepository.findById(handoverId);
        if (!handover.isPresent()) {
            return null;
        }
        
        HandoverTodo todo = new HandoverTodo();
        todo.setHandoverId(handoverId);
        todo.setContent(content);
        todo.setDueTime(dueTime);
        todo.setStatus("PENDING");
        
        HandoverTodo saved = handoverTodoRepository.save(todo);
        return toTodoItemDto(saved);
    }

    @Transactional
    public TodoItemDto completeTodo(Long todoId) {
        Optional<HandoverTodo> existing = handoverTodoRepository.findById(todoId);
        if (!existing.isPresent()) {
            return null;
        }
        
        HandoverTodo todo = existing.get();
        todo.setStatus("COMPLETED");
        
        HandoverTodo saved = handoverTodoRepository.save(todo);
        return toTodoItemDto(saved);
    }

    private TodoItemDto toTodoItemDto(HandoverTodo todo) {
        TodoItemDto dto = new TodoItemDto();
        dto.setId(todo.getId());
        dto.setHandoverId(todo.getHandoverId());
        dto.setContent(todo.getContent());
        dto.setDueTime(todo.getDueTime());
        dto.setStatus(todo.getStatus());
        dto.setCreatedAt(todo.getCreatedAt());
        return dto;
    }

    @Transactional
    public HandoverPatientDto updateHandoverPatient(Long handoverId, Long patientId, HandoverPatientUpdateRequest request) {
        Optional<HandoverPatient> existing = handoverPatientRepository.findById(patientId);
        if (!existing.isPresent() || !existing.get().getHandoverId().equals(handoverId)) {
            return null;
        }
        
        HandoverPatient patient = existing.get();
        patient.setVitals(request.getVitals());
        patient.setCurrentCondition(request.getCurrentCondition());
        patient.setObservationItems(request.getObservationItems());
        
        HandoverPatient saved = handoverPatientRepository.save(patient);
        return toHandoverPatientDto(saved);
    }
}