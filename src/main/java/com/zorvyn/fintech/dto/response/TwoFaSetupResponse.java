package com.zorvyn.fintech.dto.response;

public class TwoFaSetupResponse {

    private String secret;
    private String qrCodeUri;

    public TwoFaSetupResponse(String secret, String qrCodeUri) {
        this.secret = secret;
        this.qrCodeUri = qrCodeUri;
    }

    public String getSecret() { return secret; }
    public String getQrCodeUri() { return qrCodeUri; }
}
