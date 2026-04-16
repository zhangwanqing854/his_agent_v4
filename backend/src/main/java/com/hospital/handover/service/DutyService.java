package com.hospital.handover.service;

import com.hospital.handover.dto.*;
import com.hospital.handover.entity.Duty;
import com.hospital.handover.repository.DutyRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DutyService {

    private final DutyRepository dutyRepository;

    public DutyService(DutyRepository dutyRepository) {
        this.dutyRepository = dutyRepository;
    }

    public List<DutyDto> getAllDuties() {
        List<Duty> duties = dutyRepository.findAll();
        List<DutyDto> result = new ArrayList<>();
        
        for (Duty duty : duties) {
            result.add(new DutyDto(duty.getId(), duty.getCode(), duty.getName()));
        }
        
        return result;
    }
}