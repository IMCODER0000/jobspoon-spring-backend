package com.wowraid.jobspoon.authentication.controller.response_form;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@NoArgsConstructor(force = true)
public class TokenAuthenticationExpiredResponseForm {

    private boolean status;
    private String nickname;

    public TokenAuthenticationExpiredResponseForm(boolean status) {
        this.status = status;
    }

    public TokenAuthenticationExpiredResponseForm(boolean status, String nickname) {
        this.status = status;
        this.nickname = nickname;
    }
}
