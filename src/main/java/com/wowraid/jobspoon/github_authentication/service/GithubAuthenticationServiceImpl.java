package com.wowraid.jobspoon.github_authentication.service;

import com.wowraid.jobspoon.account.entity.Account;
import com.wowraid.jobspoon.account.entity.LoginType;
import com.wowraid.jobspoon.accountProfile.entity.AccountProfile;
import com.wowraid.jobspoon.accountProfile.service.AccountProfileService;
import com.wowraid.jobspoon.authentication.service.AuthenticationService;
import com.wowraid.jobspoon.config.FrontendConfig;
import com.wowraid.jobspoon.github_authentication.repository.GithubAuthenticationRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
@Slf4j
@Service
@RequiredArgsConstructor
public class GithubAuthenticationServiceImpl implements GithubAuthenticationService {
    final private GithubAuthenticationRepository githubAuthenticationRepository;
    final private AccountProfileService accountProfileService;
    final private AuthenticationService authenticationService;
    final private FrontendConfig frontendConfig;
    @Override
    public String handleLogin(String code) {
//        log.info("[GithubAuthenticationService] handleLogin]");
        try {
            Map<String, Object> tokenResponse = requestAccessToken(code);
            String accessToken = (String) tokenResponse.get("access_token");
//            log.info("requestAccessToken(): access_token {}", accessToken);
            Map<String, Object> userInfo = requestUserInfo(accessToken);
//            log.info("userInfo: {}", userInfo);

            //email이 존재하지않는다면 user/email url get 형태로 재시도
            String email = (String) userInfo.get("email");
            if (email == null || email.isBlank()) {
                email =requestPrimaryEmail(accessToken);
                if (email == null) throw new IllegalArgumentException("이메일이 없습니다.");
            }
            //name이 null또는 공백일경우 name대신 user의 아이디(login)를 db에 저장시도
            String nickname = (String) userInfo.get("name");
            if (nickname == null || nickname.isBlank()) {
                nickname = (String) userInfo.get("login");
                if (nickname == null) nickname = "github_user";
            }
            log.info("email: {}, nickname: {}", email, nickname);

            Optional<AccountProfile> optionalProfile =
                    accountProfileService.loadProfileByEmailAndLoginType(email, LoginType.GITHUB);
            Account account = null;

            if (optionalProfile.isPresent()) {
                account = optionalProfile.get().getAccount();
                log.info("account (existing): {}", account);
            }
            String origin = frontendConfig.getOrigins().get(0);
//            boolean isNewAdmin=false;
            if (account == null) {
                log.info("현재 신규 관리자 등록을 제한하고 있습니다.");
                String htmlResponse ="""
                    <html>
                      <body>
                        <script>
                        window.opener.postMessage({
                          Message: '현재 신규 관리자 등록을 제한하고 있습니다.'
                         }, '%s');
                         window.close();
                        </script>
                      </body>
                    </html>
                    """.formatted(origin);
                return htmlResponse;

//                isNewAdmin=true;
//                log.info("New user detected. Creating account and profile...");
//                RegisterAccountRequest registerAccountRequest=new RegisterAccountRequest(GithubType);
//                RegisterAccountProfileRequest registerAccountProfileRequest= new RegisterAccountProfileRequest(nickname,email);
//                account = accountService.createAccount(registerAccountRequest).orElseThrow(()->new IllegalStateException("계정 생성 실패"));
//                AccountProfile accountProfile=accountProfileService.createAccountProfile(account,registerAccountProfileRequest).orElseThrow(()->new IllegalStateException("계정 프로필 생성 실패"));
            }
            log.info("account result: {}", account);
            String userToken = authenticationService.createUserTokenWithAccessToken(account.getId(), accessToken);
            log.info("userToken: {}", userToken);

            String htmlResponse =
                    """
                    <html>
                      <body>
                        <script>
                          window.opener.postMessage({
                            userToken: '%s',
                            user: { name: '%s', email: '%s' }
                          }, '%s');
                          window.close();
                        </script>
                      </body>
                    </html>
                    """.formatted(userToken, nickname, email, origin);
            // ──  htmlResponse 작성·응답 ──
            return htmlResponse;

        } catch (Exception e) {
            log.error("Github 로그인 에러", e);
        }
    }

    @Override
    public String getLoginLink() {
        return this.githubAuthenticationRepository.getLoginLink();
    }
//
    @Override
    public Map<String, Object> requestAccessToken(String code) {
        return this.githubAuthenticationRepository.getAccessToken(code);
    }

    @Override
    public Map<String, Object> requestUserInfo(String accessToken) {
        return this.githubAuthenticationRepository.getUserInfo(accessToken);
    }

    @Override
    public String requestPrimaryEmail(String accessToken) {
        List<Map<String, Object>> emails = githubAuthenticationRepository.getUserEmails(accessToken);

        // primary=true인 이메일 항목을 찾아 반환
        // - 이메일 목록 중 '기본 이메일(primary)'을 필터링 -이렇게하지않으면 다른값이 입력될수 있음
        // - 첫 번째 항목의 "email" 값을 반환
        return emails.stream()
                .filter(e -> Boolean.TRUE.equals(e.get("primary"))) // primary 이메일 필터
                .map(e -> (String) e.get("email"))                  // email 값 추출
                .findFirst()                                                         // 첫 번째 결과 선택
                .orElse(null);                                                  // 없으면 null 반환
    }

}
