package com.cts.service;

import com.cts.client.AuditServiceClient;
import com.cts.dtos.AuditLogDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditServiceClient auditServiceClient;    // Inject the Feign Client

    public void logEvent(String operation, Long recordId, String details) {
        try {
            AuditLogDto auditLog = new AuditLogDto();
            auditLog.setServiceName("inventory-service");
            auditLog.setOperation(operation);
            auditLog.setRecordId(recordId);
            auditLog.setDetails(details);
            auditServiceClient.logEvent(auditLog);
        } catch (Exception e) {
            log.error("Failed to log event to Audit Service. Error: {}", e.getMessage());
        }
    }
}
