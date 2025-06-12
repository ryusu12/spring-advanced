package org.example.expert.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * AOP를 활용하여 API 로깅을 진행하는 클래스입니다.
 * Admin API 메서드 실행 전후에 요청/응답 데이터를 로깅합니다.
 * 로깅 내용으로 요청한 사용자의 ID, API 요청 시각, API 요청 URL, RequestBody, ResponseBody이 포함됩니다.
 */
@Aspect
@RequiredArgsConstructor
@Component
public class AdminAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Pointcut("execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..))")
    private void commentPointcut() {
    }

    @Pointcut("execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
    private void userPointcut() {
    }

    /**
     * 지정된 Pointcut에 해당하는 Admin API 요청/응답 데이터를 로깅하는 AOP Around Advice 메서드입니다.
     * 요청 정보와 응답 정보를 추출하여 로깅한 후, 원래의 메서드 실행 결과를 반환합니다.
     *
     * @param joinPoint Aspect 조인 포인트
     * @return result
     * @throws Throwable 원래 메서드 실행 중 발생할 수 있는 예외
     */
    @Around("commentPointcut() || userPointcut()")
    public Object logAdminRequestAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        // 요청 정보를 로깅합니다
        Long userId = (Long) request.getAttribute("userId");
        String url = request.getRequestURI();
        String requestBody = getRequestBody(joinPoint);
        logAdminApiRequest(userId, url, requestBody);

        // 원래의 메서드를 실행하고 결과를 받습니다.
        Object result = joinPoint.proceed();

        // 응답 정보를 로깅합니다
        String responseBody = getResponseBody(result);
        logger.info("ResponseBody: {}", responseBody);

        // 원래 메서드의 실행 결과를 반환합니다.
        return result;
    }

    /*
     * 요청 정보를 바탕으로 로깅하는 메서드입니다.
     * Logger 클래스를 활용하여 INFO 레벨로 기록합니다.
     */
    private void logAdminApiRequest(Long userId, String url, String requestBody) {
        logger.info("요청한 사용자의 ID: {}", userId);
        logger.info("API 요청 시각: {}", LocalDateTime.now());
        logger.info("API 요청 URL: {}", url);
        logger.info("RequestBody: {}", requestBody);
    }

    /*
     * ProceedingJoinPoint에서 RequestBody를 추출하는 메서드입니다.
     * @RequestBody가 있으면 파라미터 값을 JSON으로 변환하여 반환합니다.
     * @RequestBody가 없으면 null을 반환합니다.
     */
    private String getRequestBody(ProceedingJoinPoint joinPoint) throws JsonProcessingException {
        Object[] args = joinPoint.getArgs();

        // MethodSignature를 통해 파라미터 어노테이션들을 확인합니다.
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Annotation[][] parameterAnnotations = signature.getMethod().getParameterAnnotations();

        // 모든 파라미터를 순회하며 @RequestBody 어노테이션이 있는지 확인합니다.
        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation.annotationType().equals(RequestBody.class)) {
                    // @RequestBody의 파라미터 값을 JSON으로 변환하여 반환합니다.
                    return objectMapper.writeValueAsString(args[i]);
                }
            }
        }
        return null;
    }

    /*
     * 메서드 실행 결과(result)에서 ResponseBody를 추출하는 메서드입니다.
     * 결과 객체를 Map으로 변환하여 "body" 키의 값을 찾아 JSON 문자열로 반환합니다.
     * ResponseBody가 없으면 null을 반환합니다.
     */
    private String getResponseBody(Object result) throws JsonProcessingException {
        if (result == null) return null;

        Map<String, Object> responseMap = objectMapper.convertValue(result, Map.class);
        return objectMapper.writeValueAsString(responseMap.get("body"));
    }

}