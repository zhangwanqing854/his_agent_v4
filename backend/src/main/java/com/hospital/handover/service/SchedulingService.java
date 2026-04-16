package com.hospital.handover.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.handover.dto.*;
import com.hospital.handover.entity.Department;
import com.hospital.handover.entity.DepartmentDutyStaff;
import com.hospital.handover.entity.DepartmentSchedulingConfig;
import com.hospital.handover.entity.DoctorDepartment;
import com.hospital.handover.entity.HisStaff;
import com.hospital.handover.entity.Scheduling;
import com.hospital.handover.entity.SchedulingDetail;
import com.hospital.handover.entity.User;
import com.hospital.handover.repository.DepartmentDutyStaffRepository;
import com.hospital.handover.repository.DepartmentRepository;
import com.hospital.handover.repository.DepartmentSchedulingConfigRepository;
import com.hospital.handover.repository.DoctorDepartmentRepository;
import com.hospital.handover.repository.HisStaffRepository;
import com.hospital.handover.repository.SchedulingDetailRepository;
import com.hospital.handover.repository.SchedulingRepository;
import com.hospital.handover.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SchedulingService {

    private final SchedulingRepository schedulingRepository;
    private final SchedulingDetailRepository schedulingDetailRepository;
    private final DepartmentSchedulingConfigRepository configRepository;
    private final DepartmentRepository departmentRepository;
    private final HisStaffRepository hisStaffRepository;
    private final UserRepository userRepository;
    private final DoctorDepartmentRepository doctorDepartmentRepository;
    private final DepartmentDutyStaffRepository dutyStaffRepository;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SchedulingService(SchedulingRepository schedulingRepository,
                            SchedulingDetailRepository schedulingDetailRepository,
                            DepartmentSchedulingConfigRepository configRepository,
                            DepartmentRepository departmentRepository,
                            HisStaffRepository hisStaffRepository,
                            UserRepository userRepository,
                            DoctorDepartmentRepository doctorDepartmentRepository,
                            DepartmentDutyStaffRepository dutyStaffRepository,
                            ObjectMapper objectMapper) {
        this.schedulingRepository = schedulingRepository;
        this.schedulingDetailRepository = schedulingDetailRepository;
        this.configRepository = configRepository;
        this.departmentRepository = departmentRepository;
        this.hisStaffRepository = hisStaffRepository;
        this.userRepository = userRepository;
        this.doctorDepartmentRepository = doctorDepartmentRepository;
        this.dutyStaffRepository = dutyStaffRepository;
        this.objectMapper = objectMapper;
    }

    public List<SchedulingListItemDto> getSchedulingList(Long departmentId, String yearMonth) {
        if (departmentId == null) {
            return new ArrayList<>();
        }
        
        List<Scheduling> schedulings;
        
        if (yearMonth != null && !yearMonth.isEmpty()) {
            schedulings = schedulingRepository.findByDepartmentIdAndYearMonth(departmentId, yearMonth);
        } else {
            schedulings = schedulingRepository.findByDepartmentIdOrderByYearMonthDesc(departmentId);
        }
        
        return schedulings.stream()
            .map(this::toListItemDto)
            .collect(Collectors.toList());
    }

    public SchedulingDto getSchedulingById(Long id) {
        Optional<Scheduling> optional = schedulingRepository.findById(id);
        if (optional.isEmpty()) {
            return null;
        }
        
        Scheduling scheduling = optional.get();
        SchedulingDto dto = toDto(scheduling);
        
        List<SchedulingDetail> details = schedulingDetailRepository.findBySchedulingIdOrderByDutyDateAsc(id);
        dto.setDetails(details.stream()
            .map(this::toDetailDto)
            .collect(Collectors.toList()));
        
        return dto;
    }

    @Transactional
    public SchedulingListItemDto createScheduling(Long departmentId, Long userId, CreateSchedulingRequest request) {
        if (schedulingRepository.existsByDepartmentIdAndYearMonth(departmentId, request.getYearMonth())) {
            throw new RuntimeException("该月份排班已存在");
        }
        
        Scheduling scheduling = new Scheduling();
        scheduling.setDepartmentId(departmentId);
        scheduling.setYearMonth(request.getYearMonth());
        scheduling.setStatus("draft");
        scheduling.setCreatedBy(userId);
        
        Scheduling saved = schedulingRepository.save(scheduling);
        
        createEmptyDetails(saved.getId(), request.getYearMonth());
        
        return toListItemDto(saved);
    }

    private void createEmptyDetails(Long schedulingId, String yearMonth) {
        String[] parts = yearMonth.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        
        int daysInMonth = java.time.YearMonth.of(year, month).lengthOfMonth();
        
        List<SchedulingDetail> details = new ArrayList<>();
        for (int day = 1; day <= daysInMonth; day++) {
            SchedulingDetail detail = new SchedulingDetail();
            detail.setSchedulingId(schedulingId);
            detail.setDutyDate(LocalDate.of(year, month, day));
            detail.setStaffId(null);
            detail.setRemark(null);
            details.add(detail);
        }
        
        schedulingDetailRepository.saveAll(details);
    }

    @Transactional
    public SchedulingListItemDto updateStatus(Long id, String status) {
        Optional<Scheduling> optional = schedulingRepository.findById(id);
        if (optional.isEmpty()) {
            throw new RuntimeException("排班不存在");
        }
        
        Scheduling scheduling = optional.get();
        scheduling.setStatus(status);
        
        Scheduling saved = schedulingRepository.save(scheduling);
        return toListItemDto(saved);
    }

    @Transactional
    public void deleteScheduling(Long id) {
        if (!schedulingRepository.existsById(id)) {
            throw new RuntimeException("排班不存在");
        }
        
        schedulingDetailRepository.deleteBySchedulingId(id);
        schedulingRepository.deleteById(id);
    }

    @Transactional
    public List<SchedulingDetailDto> updateDetails(Long schedulingId, UpdateSchedulingDetailsRequest request) {
        if (!schedulingRepository.existsById(schedulingId)) {
            throw new RuntimeException("排班不存在");
        }
        
        for (UpdateSchedulingDetailsRequest.SchedulingDetailItem item : request.getDetails()) {
            LocalDate dutyDate = LocalDate.parse(item.getDutyDate(), DATE_FORMATTER);
            Optional<SchedulingDetail> optional = schedulingDetailRepository.findBySchedulingIdAndDutyDate(schedulingId, dutyDate);
            
            if (optional.isPresent()) {
                SchedulingDetail detail = optional.get();
                detail.setStaffId(item.getStaffId());
                detail.setRemark(item.getRemark());
                schedulingDetailRepository.save(detail);
            }
        }
        
        List<SchedulingDetail> details = schedulingDetailRepository.findBySchedulingIdOrderByDutyDateAsc(schedulingId);
        return details.stream()
            .map(this::toDetailDto)
            .collect(Collectors.toList());
    }

    public List<HisStaffDto> getSchedulableStaff(Long departmentId) {
        if (departmentId == null) {
            return new ArrayList<>();
        }
        
        List<DepartmentDutyStaff> dutyStaffList = dutyStaffRepository.findByDepartmentIdOrderByDisplayOrderAsc(departmentId);
        
        if (!dutyStaffList.isEmpty()) {
            List<Long> hisStaffIds = dutyStaffList.stream()
                .map(DepartmentDutyStaff::getStaffId)
                .collect(Collectors.toList());
            
            List<HisStaff> staffList = hisStaffRepository.findAllById(hisStaffIds);
            return staffList.stream()
                .map(this::toHisStaffDto)
                .collect(Collectors.toList());
        }
        
        List<DoctorDepartment> doctorDepts = doctorDepartmentRepository.findByDepartmentId(departmentId);
        List<Long> userIds = doctorDepts.stream()
            .map(DoctorDepartment::getDoctorId)
            .collect(Collectors.toList());
        
        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<User> users = userRepository.findAllById(userIds);
        List<Long> fallbackStaffIds = users.stream()
            .map(User::getHisStaffId)
            .filter(id -> id != null)
            .collect(Collectors.toList());
        
        if (fallbackStaffIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<HisStaff> staffList = hisStaffRepository.findAllById(fallbackStaffIds);
        return staffList.stream()
            .map(this::toHisStaffDto)
            .collect(Collectors.toList());
    }

    public SchedulingConfigDto getConfig(Long departmentId) {
        if (departmentId == null) {
            SchedulingConfigDto dto = new SchedulingConfigDto();
            dto.setDepartmentId(null);
            dto.setStaffOrder(new ArrayList<>());
            dto.setLastPosition(null);
            return dto;
        }
        
        Optional<DepartmentSchedulingConfig> optional = configRepository.findByDepartmentId(departmentId);
        
        if (optional.isEmpty()) {
            List<DepartmentDutyStaff> dutyStaffList = dutyStaffRepository.findByDepartmentIdOrderByDisplayOrderAsc(departmentId);
            
            List<Long> defaultOrder;
            if (!dutyStaffList.isEmpty()) {
                defaultOrder = dutyStaffList.stream()
                    .map(DepartmentDutyStaff::getStaffId)
                    .collect(Collectors.toList());
            } else {
                List<DoctorDepartment> doctorDepts = doctorDepartmentRepository.findByDepartmentId(departmentId);
                List<Long> userIds = doctorDepts.stream()
                    .map(DoctorDepartment::getDoctorId)
                    .collect(Collectors.toList());
                
                List<User> users = userRepository.findAllById(userIds);
                List<Long> hisStaffIds = users.stream()
                    .map(User::getHisStaffId)
                    .filter(id -> id != null)
                    .collect(Collectors.toList());
                
                defaultOrder = hisStaffIds;
            }
            
            DepartmentSchedulingConfig config = new DepartmentSchedulingConfig();
            config.setDepartmentId(departmentId);
            config.setStaffOrder(toJson(defaultOrder));
            config.setLastPosition(null);
            configRepository.save(config);
            
            SchedulingConfigDto dto = new SchedulingConfigDto();
            dto.setDepartmentId(departmentId);
            dto.setStaffOrder(defaultOrder);
            dto.setLastPosition(null);
            return dto;
        }
        
        return toConfigDto(optional.get());
    }

    @Transactional
    public SchedulingConfigDto updateConfig(Long departmentId, UpdateSchedulingConfigRequest request) {
        Optional<DepartmentSchedulingConfig> optional = configRepository.findByDepartmentId(departmentId);
        
        DepartmentSchedulingConfig config;
        if (optional.isEmpty()) {
            config = new DepartmentSchedulingConfig();
            config.setDepartmentId(departmentId);
        } else {
            config = optional.get();
        }
        
        config.setStaffOrder(toJson(request.getStaffOrder()));
        configRepository.save(config);
        
        return toConfigDto(config);
    }

    @Transactional
    public List<SchedulingDetailDto> autoGenerate(Long schedulingId, Long departmentId, AutoGenerateRequest request) {
        if (!schedulingRepository.existsById(schedulingId)) {
            throw new RuntimeException("排班不存在");
        }
        
        LocalDate startDate = LocalDate.parse(request.getStartDate(), DATE_FORMATTER);
        LocalDate endDate = LocalDate.parse(request.getEndDate(), DATE_FORMATTER);
        List<Long> staffOrder = request.getStaffOrder();
        int startPosition = request.getStartPosition();
        
        int days = (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
        
        List<SchedulingDetail> generatedDetails = new ArrayList<>();
        
        for (int i = 0; i < days; i++) {
            LocalDate dutyDate = startDate.plusDays(i);
            int staffIndex = (startPosition + i) % staffOrder.size();
            Long staffId = staffOrder.get(staffIndex);
            
            Optional<SchedulingDetail> optional = schedulingDetailRepository.findBySchedulingIdAndDutyDate(schedulingId, dutyDate);
            
            if (optional.isPresent()) {
                SchedulingDetail detail = optional.get();
                detail.setStaffId(staffId);
                schedulingDetailRepository.save(detail);
                generatedDetails.add(detail);
            }
        }
        
        Optional<DepartmentSchedulingConfig> configOptional = configRepository.findByDepartmentId(departmentId);
        if (configOptional.isPresent()) {
            DepartmentSchedulingConfig config = configOptional.get();
            int newLastPosition = (startPosition + days - 1) % staffOrder.size();
            config.setLastPosition(newLastPosition);
            config.setStaffOrder(toJson(staffOrder));
            configRepository.save(config);
        }
        
        return generatedDetails.stream()
            .map(this::toDetailDto)
            .collect(Collectors.toList());
    }

    private SchedulingListItemDto toListItemDto(Scheduling scheduling) {
        SchedulingListItemDto dto = new SchedulingListItemDto();
        dto.setId(scheduling.getId());
        dto.setDepartmentId(scheduling.getDepartmentId());
        dto.setYearMonth(scheduling.getYearMonth());
        dto.setStatus(scheduling.getStatus());
        dto.setCreatedBy(scheduling.getCreatedBy());
        dto.setCreatedAt(scheduling.getCreatedAt().format(DATETIME_FORMATTER));
        
        Optional<Department> dept = departmentRepository.findById(scheduling.getDepartmentId());
        dept.ifPresent(d -> dto.setDepartmentName(d.getName()));
        
        Optional<User> user = userRepository.findById(scheduling.getCreatedBy());
        user.ifPresent(u -> dto.setCreatedByName(u.getUsername()));
        
        return dto;
    }

    private SchedulingDto toDto(Scheduling scheduling) {
        SchedulingDto dto = new SchedulingDto();
        dto.setId(scheduling.getId());
        dto.setDepartmentId(scheduling.getDepartmentId());
        dto.setYearMonth(scheduling.getYearMonth());
        dto.setStatus(scheduling.getStatus());
        dto.setCreatedBy(scheduling.getCreatedBy());
        dto.setCreatedAt(scheduling.getCreatedAt().format(DATETIME_FORMATTER));
        dto.setUpdatedAt(scheduling.getUpdatedAt().format(DATETIME_FORMATTER));
        
        Optional<Department> dept = departmentRepository.findById(scheduling.getDepartmentId());
        dept.ifPresent(d -> dto.setDepartmentName(d.getName()));
        
        Optional<User> user = userRepository.findById(scheduling.getCreatedBy());
        user.ifPresent(u -> dto.setCreatedByName(u.getUsername()));
        
        return dto;
    }

    private SchedulingDetailDto toDetailDto(SchedulingDetail detail) {
        SchedulingDetailDto dto = new SchedulingDetailDto();
        dto.setId(detail.getId());
        dto.setDutyDate(detail.getDutyDate().format(DATE_FORMATTER));
        dto.setStaffId(detail.getStaffId());
        dto.setRemark(detail.getRemark());
        
        if (detail.getStaffId() != null) {
            Optional<HisStaff> staff = hisStaffRepository.findById(detail.getStaffId());
            staff.ifPresent(s -> dto.setStaffName(s.getName()));
        }
        
        return dto;
    }

    private SchedulingConfigDto toConfigDto(DepartmentSchedulingConfig config) {
        SchedulingConfigDto dto = new SchedulingConfigDto();
        dto.setDepartmentId(config.getDepartmentId());
        dto.setStaffOrder(fromJson(config.getStaffOrder()));
        dto.setLastPosition(config.getLastPosition());
        dto.setUpdatedAt(config.getUpdatedAt().format(DATETIME_FORMATTER));
        return dto;
    }

    private HisStaffDto toHisStaffDto(HisStaff staff) {
        HisStaffDto dto = new HisStaffDto();
        dto.setId(staff.getId());
        dto.setStaffCode(staff.getStaffCode());
        dto.setName(staff.getName());
        dto.setDepartmentId(staff.getDepartmentId());
        dto.setTitle(staff.getTitle());
        dto.setPhone(staff.getPhone());
        
        if (staff.getDepartmentId() != null) {
            Optional<Department> dept = departmentRepository.findById(staff.getDepartmentId());
            dept.ifPresent(d -> dto.setDepartmentName(d.getName()));
        }
        
        return dto;
    }

    private String toJson(List<Long> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private List<Long> fromJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }
}