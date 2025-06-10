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

@RequiredArgsConstructor
public class AdminUserInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) throws Exception {
        // JwtFilter 에서 set 한 userId 값을 가져옴
        Long userId = (Long) request.getAttribute("userId");

        // 본인의 권한을 admin 에서 user로 변경하면 여전히 토큰의 userRole이 admin이라서 접근이 가능한 문제 발생
        // DB의 user를 검색하여 userRole이 admin인지 확인
        User user = userRepository.findById(userId).orElseThrow(() -> new InvalidRequestException("User not found"));

        if (!UserRole.ADMIN.equals(user.getUserRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자 권한이 없습니다.");
            log.warn("[FORBIDDEN] url: {}, requestTime: {}", request.getRequestURI(), LocalDateTime.now());
            return false;
        }

        log.info("url: {}, requestTime: {}", request.getRequestURI(), LocalDateTime.now());
        return true;
    }

}