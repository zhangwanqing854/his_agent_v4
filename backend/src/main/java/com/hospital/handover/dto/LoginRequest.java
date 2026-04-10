package com.hospital.handover.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    
    @NotBlank(message = "用户编码不能为空")
    private String usercode;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    private String captcha;
    private String captchaId;
    
    public LoginRequest() {}
    
    public LoginRequest(String usercode, String password) {
        this.usercode = usercode;
        this.password = password;
    }
    
    public String getUsercode() { return usercode; }
    public void setUsercode(String usercode) { this.usercode = usercode; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getCaptcha() { return captcha; }
    public void setCaptcha(String captcha) { this.captcha = captcha; }
    
    public String getCaptchaId() { return captchaId; }
    public void setCaptchaId(String captchaId) { this.captchaId = captchaId; }
}