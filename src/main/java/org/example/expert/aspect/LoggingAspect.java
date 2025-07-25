package org.example.expert.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.expert.config.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;

    @Around("@annotation(org.example.expert.domain.LogAdminAccess)")
    public Object logAdminAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        /* 메서드 실행 전: 요청 데이터 로깅 */
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        // 요청한 사용자의 ID
        String token = jwtUtil.substringToken(request.getHeader("Authorization"));
        String requestId = jwtUtil.extractClaims(token).getSubject();
        log.info("요청한 사용자의 ID: {}", requestId);

        log.info("요청 시각: {}", LocalDateTime.now());                    // API 요청 시각

        log.info("요청 URL: {}", request.getRequestURL().toString());     // API 요청 URL

        Object[] args = joinPoint.getArgs();
        if(args.length > 1 && args[1] != null){     // 요청 본문이 있는 경우 로그 출력
            log.info("요청 본문: {}", objectMapper.writeValueAsString(args[1]));
        } else{
            log.info("요청 본문 없음");
        }

        /* 타겟 메서드 실행*/
        Object result = joinPoint.proceed();

        /* 메서드 실행 후: 응답 데이터 로깅*/
        if (result != null) {                       // 응답 본문이 있는 경우 로그 출력
            log.info("응답 본문: {}", objectMapper.writeValueAsString(result));
        } else {
            log.info("응답 본문 없음");
        }

        /* 원래 메서드 실행 결과 반환*/
        return result;
    }
}