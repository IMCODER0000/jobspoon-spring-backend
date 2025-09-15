package com.wowraid.jobspoon.kakao_authentication.service.mobile_response;


import lombok.Getter;

@Getter
public class KakaoLoginMobileResponse {

    private boolean isNewUser;
    private String token;
    private String nickname;
    private String email;


    public KakaoLoginMobileResponse(boolean isNewUser, String token, String nickname, String email) {
        this.isNewUser = isNewUser;
        this.token = token;
        this.nickname = nickname;
        this.email = email;
    }

}
