package com.wowraid.jobspoon.kakao_authentication.service.response;

import com.wowraid.jobspoon.kakao_authentication.service.mobile_response.KakaoLoginMobileResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public abstract class KakaoLoginResponse {



    public static KakaoLoginResponse of(boolean isNewUser, String token, String nickname, String email, String origin) {
        return isNewUser
                ? new NewUserKakaoLoginResponse(isNewUser, token, nickname, email, origin)
                : new ExistingUserKakaoLoginResponse(isNewUser, token, nickname, email, origin);
    }

    public static KakaoLoginMobileResponse ofMobile(boolean isNewUser, String token, String nickname, String email, String origin) {
        return new KakaoLoginMobileResponse(isNewUser, token, nickname, email);
    }



    public abstract String getHtmlResponse();
    public abstract String getUserToken();
    public abstract boolean getIsNewUser();
    protected static String escape(String str) {
        return str.replace("'", "\\'");
    }

}

