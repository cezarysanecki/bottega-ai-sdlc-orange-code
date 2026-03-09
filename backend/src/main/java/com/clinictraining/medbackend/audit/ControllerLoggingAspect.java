package com.clinictraining.medbackend.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class ControllerLoggingAspect {

    private final ApiCallLogRepository apiCallLogRepository;
    private final ObjectMapper objectMapper;

    public ControllerLoggingAspect(ApiCallLogRepository apiCallLogRepository, ObjectMapper objectMapper) {
        this.apiCallLogRepository = apiCallLogRepository;
        this.objectMapper = objectMapper;
    }

    @Around("execution(* com.clinictraining.medbackend.web..*(..))")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        ApiCallLog logEntry = new ApiCallLog();
        HttpServletRequest request = currentRequest();
        if (request != null) {
            logEntry.setPath(request.getRequestURI());
            logEntry.setMethod(request.getMethod());
        } else {
            logEntry.setPath(joinPoint.getSignature().toShortString());
            logEntry.setMethod("N/A");
        }
        logEntry.setPayload(maskArguments(joinPoint.getArgs()));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logEntry.setUsername(auth != null ? auth.getName() : "anonymous");

        try {
            Object result = joinPoint.proceed();
            logEntry.setStatus(200);
            return result;
        } catch (Throwable ex) {
            logEntry.setStatus(500);
            logEntry.setPayload(logEntry.getPayload() + " | error: " + ex.getClass().getSimpleName());
            throw ex;
        } finally {
            apiCallLogRepository.save(logEntry);
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    private String maskArguments(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        try {
            return Arrays.stream(args)
                    .map(this::maskObject)
                    .collect(Collectors.joining(", ", "[", "]"));
        } catch (Exception ex) {
            return "[unlogged]";
        }
    }

    private String maskObject(Object arg) {
        if (arg == null) {
            return "null";
        }
        try {
            String json = objectMapper.writeValueAsString(arg);
            return json.replaceAll("(?i)\"(password|secret)\":\"[^\"]*\"", "\"$1\":\"***\"");
        } catch (Exception ex) {
            return arg.toString();
        }
    }
}
