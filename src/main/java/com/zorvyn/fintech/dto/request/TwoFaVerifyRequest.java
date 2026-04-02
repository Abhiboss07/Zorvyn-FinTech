package com.zorvyn.fintech.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TwoFaVerifyRequest {

    @NotBlank(message = "2FA code is required")
    @Size(min = 6, max = 6, message = "2FA code must be 6 digits")
    private String code;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
