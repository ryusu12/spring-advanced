package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

/**
 * Interceptor를 활용하여 API 로깅을 진행하는 클래스입니다.
 * Admin API에 접근할 때마다 인증 여부를 확인한 후, 로깅합니다.
 * 로깅 내용으로, 요청 시각과 URL이 포함됩니다.
 */
@RequiredArgsConstructor
public class AdminUserInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * HttpServletRequest를 사전 처리하는 메서드입니다.
     * 어드민 권한 여부를 확인하고 로깅합니다.
     * 권한이 없으면 403 Forbidden 응답을 반환합니다.
     * 권한이 있으면 요청이 컨트롤러로 전달됩니다.
     *
     * @param request  현재 HTTP 요청 객체
     * @param response 현재 HTTP 응답 객체
     * @param handler  실행할 핸들러 정보
     * @return boolean (인증 성공 시 true, 실패시 false)
     * @throws Exception Forbidden 발생시의 예외
     */
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) throws Exception {
        // JwtFilter 에서 set 한 userId 값을 가져옵니다.
        Long userId = (Long) request.getAttribute("userId");

        // DB의 user를 검색하여 userRole이 admin인지 확인합니다.
        User user = userRepository.findById(userId).orElseThrow(() -> new InvalidRequestException("User not found"));

        if (!UserRole.ADMIN.equals(user.getUserRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자 권한이 없습니다.");
            logger.warn("[FORBIDDEN] url: {}, requestTime: {}", request.getRequestURI(), LocalDateTime.now());
            return false;
        }

        logger.info("url: {}, requestTime: {}", request.getRequestURI(), LocalDateTime.now());
        return true;
    }

}