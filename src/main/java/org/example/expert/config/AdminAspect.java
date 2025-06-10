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

@Aspect
@RequiredArgsConstructor
@Component
public class AdminAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    ObjectMapper objectMapper = new ObjectMapper();

    @Pointcut("execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..))")
    public void commentPointcut() {
    }

    @Pointcut("execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
    public void userPointcut() {
    }

    @Around("commentPointcut() || userPointcut()")
    public Object getLog(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        Object result = joinPoint.proceed();

        Long userId = (Long) request.getAttribute("userId");
        String url = request.getRequestURI();
        String reqBody = getRequestBody(joinPoint);
        String resBody = getResponseBody(result);

        createLog(userId, url, reqBody, resBody);

        return result;
    }

    private void createLog(Long userId, String url, String reqBody, String resBody) {
        log.info("요청한 사용자의 ID: {}", userId);
        log.info("API 요청 시각: {}", LocalDateTime.now());
        log.info("API 요청 URL: {}", url);
        log.info("RequestBody: {}", reqBody);
        log.info("ResponseBody: {}", resBody);
    }

    private String getRequestBody(ProceedingJoinPoint joinPoint) throws JsonProcessingException {
        Object[] args = joinPoint.getArgs();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Annotation[][] parameterAnnotations = signature.getMethod().getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation.annotationType().equals(RequestBody.class)) {
                    return objectMapper.writeValueAsString(args[i]);
                }
            }
        }
        return null;
    }

    private String getResponseBody(Object result) throws JsonProcessingException {
        String resBody = null;
        if (result != null) {
            Map<String, Object> responseMap = objectMapper.convertValue(result, Map.class);
            resBody = objectMapper.writeValueAsString(responseMap.get("body"));
        }
        return resBody;
    }

}