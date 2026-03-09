package com.clinictraining.medbackend.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Method;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditRepositoryAspect {

    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;

    public AuditRepositoryAspect(AuditEventRepository auditEventRepository, ObjectMapper objectMapper) {
        this.auditEventRepository = auditEventRepository;
        this.objectMapper = objectMapper;
    }

    @AfterReturning(pointcut = "execution(* org.springframework.data.jpa.repository.JpaRepository+.save(..))", returning = "result")
    public void captureSave(JoinPoint joinPoint, Object result) {
        Object entity = result != null ? result : (joinPoint.getArgs().length > 0 ? joinPoint.getArgs()[0] : null);
        if (entity == null) {
            return;
        }
        if (entity instanceof AuditEvent || entity instanceof ApiCallLog) {
            return; // avoid infinite loops
        }
        if (!entity.getClass().getName().startsWith("com.clinictraining")) {
            return;
        }
        try {
            AuditEvent event = new AuditEvent();
            event.setEntityName(entity.getClass().getSimpleName());
            event.setEntityId(resolveId(entity));
            event.setAction("SAVE");
            event.setPayload(objectMapper.writeValueAsString(entity));
            auditEventRepository.save(event);
        } catch (Exception ex) {
            // swallow – audit failures should not break main flow
        }
    }

    private String resolveId(Object entity) {
        try {
            Method method = entity.getClass().getMethod("getId");
            Object value = method.invoke(entity);
            return value == null ? "unknown" : value.toString();
        } catch (Exception ex) {
            return "unknown";
        }
    }
}
