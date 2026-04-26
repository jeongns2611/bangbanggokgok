package com.ssafy.backend.global.auth.service;

import com.ssafy.backend.global.auth.dto.response.LoginAuthResponse;

public interface GoogleLoginService {

    LoginAuthResponse login(String googleToken);
}
