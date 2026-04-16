package com.hospital.handover.service;

import com.hospital.handover.entity.HisStaff;
import com.hospital.handover.entity.ShiftHandover;
import com.hospital.handover.repository.HisStaffRepository;
import com.hospital.handover.repository.ShiftHandoverRepository;
import com.hospital.handover.dto.SmsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SmsNotificationService {
    
    private static final Logger log = LoggerFactory.getLogger(SmsNotificationService.class);
    
    private final SmsService smsService;
    private final SmsConfigService smsConfigService;
    private final SmsLogService smsLogService;
    private final HisStaffRepository hisStaffRepository;
    private final ShiftHandoverRepository shiftHandoverRepository;
    
    public SmsNotificationService(SmsService smsService,
                                   SmsConfigService smsConfigService,
                                   SmsLogService smsLogService,
                                   HisStaffRepository hisStaffRepository,
                                   ShiftHandoverRepository shiftHandoverRepository) {
        this.smsService = smsService;
        this.smsConfigService = smsConfigService;
        this.smsLogService = smsLogService;
        this.hisStaffRepository = hisStaffRepository;
        this.shiftHandoverRepository = shiftHandoverRepository;
    }
    
    @Async
    public void sendHandoverNotification(Long handoverId) {
        if (!smsConfigService.isEnabled()) {
            log.info("短信功能已禁用，跳过发送");
            return;
        }
        
        ShiftHandover handover = shiftHandoverRepository.findById(handoverId).orElse(null);
        if (handover == null || handover.getToDoctorId() == null) {
            log.warn("交班记录不存在或接班医生未设置");
            return;
        }
        
        HisStaff toDoctor = hisStaffRepository.findById(handover.getToDoctorId()).orElse(null);
        if (toDoctor == null) {
            log.warn("接班医生信息不存在");
            return;
        }
        
        String phone = toDoctor.getPhone();
        if (phone == null || phone.isEmpty()) {
            log.warn("接班医生 {} 电话号码为空，跳过短信发送", toDoctor.getName());
            smsLogService.logFailure(handoverId, "", "", "电话号码为空");
            return;
        }
        
        String content = formatContent(handover);
        
        try {
            SmsResult result = smsService.send(phone, content);
            
            if (result.isSuccess()) {
                smsLogService.logSuccess(handoverId, phone, content);
                log.info("短信发送成功: handoverId={}, phone={}", handoverId, phone);
            } else {
                smsLogService.logFailure(handoverId, phone, content, result.getErrorMessage());
                log.error("短信发送失败: handoverId={}, error={}", handoverId, result.getErrorMessage());
            }
        } catch (Exception e) {
            smsLogService.logFailure(handoverId, phone, content, e.getMessage());
            log.error("短信发送异常: handoverId={}", handoverId, e);
        }
    }
    
    private String formatContent(ShiftHandover handover) {
        return String.format(
            "【北京大学国际医院】您有新的交接班任务待处理。科室：%s，交班医生：%s，请及时登录系统处理。",
            handover.getDeptName() != null ? handover.getDeptName() : "",
            handover.getFromDoctorName() != null ? handover.getFromDoctorName() : ""
        );
    }
}