package com.talentx.hrms.service.audit;

import com.talentx.hrms.entity.analytics.AuditLog;
import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.core.User;
import com.talentx.hrms.entity.employee.Employee;
import com.talentx.hrms.repository.AuditLogRepository;
import com.talentx.hrms.service.auth.AuthService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.NotBlank;
import net.jqwik.api.constraints.NotEmpty;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * **Property 21: Audit Log Completeness**
 * **Validates: Requirements 8.4**
 * 
 * Property-based test to verify that audit logs are complete and contain all required information
 * for data access and modification operations.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuditLogCompletenessPropertyTest {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private EntityManager entityManager;

    @MockBean
    private AuthService authService;

    private User testUser;
    private Organization testOrganization;

    @BeforeEach
    void setUp() {
        // Create test organization
        testOrganization = new Organization();
        testOrganization.setName("Test Organization");
        testOrganization.setLegalName("Test Organization Legal");
        entityManager.persist(testOrganization);

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setOrganization(testOrganization);
        entityManager.persist(testUser);

        entityManager.flush();

        // Mock auth service to return test user
        when(authService.getCurrentUser()).thenReturn(testUser);
    }

    @Property(tries = 100)
    @Label("Data change operations should create complete audit logs with all required fields")
    void dataChangeOperationsShouldCreateCompleteAuditLogs(
            @ForAll @From("validActions") String action,
            @ForAll @From("validEntityTypes") String entityType,
            @ForAll @NotBlank String entityId,
            @ForAll @NotBlank String entityName,
            @ForAll @From("validDataObjects") Map<String, Object> oldValues,
            @ForAll @From("validDataObjects") Map<String, Object> newValues) {

        // When: A data change operation is logged
        auditLogService.logDataChange(action, entityType, entityId, entityName, oldValues, newValues);
        entityManager.flush();

        // Then: An audit log should be created with all required fields
        List<AuditLog> auditLogs = auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
        
        assertThat(auditLogs).isNotEmpty();
        
        AuditLog auditLog = auditLogs.get(0);
        
        // Verify all required fields are present
        assertThat(auditLog.getTimestamp()).isNotNull();
        assertThat(auditLog.getUser()).isEqualTo(testUser);
        assertThat(auditLog.getUsername()).isEqualTo(testUser.getUsername());
        assertThat(auditLog.getAction()).isEqualTo(action);
        assertThat(auditLog.getEntityType()).isEqualTo(entityType);
        assertThat(auditLog.getEntityId()).isEqualTo(entityId);
        assertThat(auditLog.getEntityName()).isEqualTo(entityName);
        assertThat(auditLog.getStatus()).isEqualTo("SUCCESS");
        assertThat(auditLog.getSeverity()).isNotNull();
        assertThat(auditLog.getModule()).isNotNull();
        
        // Verify data values are captured (if provided)
        if (oldValues != null && !oldValues.isEmpty()) {
            assertThat(auditLog.getOldValues()).isNotNull();
        }
        if (newValues != null && !newValues.isEmpty()) {
            assertThat(auditLog.getNewValues()).isNotNull();
        }
        
        // Verify timestamp is recent (within last minute)
        assertThat(auditLog.getTimestamp()).isAfter(Instant.now().minusSeconds(60));
    }

    @Property(tries = 100)
    @Label("Authentication events should create complete audit logs")
    void authenticationEventsShouldCreateCompleteAuditLogs(
            @ForAll @NotBlank String username,
            @ForAll @From("validAuthActions") String action,
            @ForAll boolean success,
            @ForAll String errorMessage) {

        // When: An authentication event is logged
        auditLogService.logAuthentication(username, action, success, errorMessage);
        entityManager.flush();

        // Then: An audit log should be created with all required fields
        List<AuditLog> auditLogs = auditLogRepository.findByUsernameAndActionAndStatusAndTimestampAfterOrderByTimestampDesc(
            username, action, success ? "SUCCESS" : "FAILURE", Instant.now().minusSeconds(60));
        
        assertThat(auditLogs).isNotEmpty();
        
        AuditLog auditLog = auditLogs.get(0);
        
        // Verify all required fields are present
        assertThat(auditLog.getTimestamp()).isNotNull();
        assertThat(auditLog.getUsername()).isEqualTo(username);
        assertThat(auditLog.getAction()).isEqualTo(action);
        assertThat(auditLog.getEntityType()).isEqualTo("User");
        assertThat(auditLog.getStatus()).isEqualTo(success ? "SUCCESS" : "FAILURE");
        assertThat(auditLog.getSeverity()).isEqualTo(success ? "LOW" : "MEDIUM");
        assertThat(auditLog.getModule()).isEqualTo("AUTHENTICATION");
        
        if (!success && errorMessage != null) {
            assertThat(auditLog.getErrorMessage()).isEqualTo(errorMessage);
        }
        
        // Verify timestamp is recent
        assertThat(auditLog.getTimestamp()).isAfter(Instant.now().minusSeconds(60));
    }

    @Property(tries = 100)
    @Label("Security events should create complete audit logs")
    void securityEventsShouldCreateCompleteAuditLogs(
            @ForAll @From("validSecurityActions") String action,
            @ForAll @NotBlank String description,
            @ForAll @From("validSeverityLevels") String severity) {

        // When: A security event is logged
        auditLogService.logSecurityEvent(action, description, severity);
        entityManager.flush();

        // Then: An audit log should be created with all required fields
        List<AuditLog> auditLogs = auditLogRepository.findByModuleOrderByTimestampDesc("SECURITY");
        
        assertThat(auditLogs).isNotEmpty();
        
        AuditLog auditLog = auditLogs.get(0);
        
        // Verify all required fields are present
        assertThat(auditLog.getTimestamp()).isNotNull();
        assertThat(auditLog.getUser()).isEqualTo(testUser);
        assertThat(auditLog.getUsername()).isEqualTo(testUser.getUsername());
        assertThat(auditLog.getAction()).isEqualTo(action);
        assertThat(auditLog.getEntityType()).isEqualTo("Security");
        assertThat(auditLog.getDescription()).isEqualTo(description);
        assertThat(auditLog.getStatus()).isEqualTo("FAILURE");
        assertThat(auditLog.getSeverity()).isEqualTo(severity);
        assertThat(auditLog.getModule()).isEqualTo("SECURITY");
        
        // Verify timestamp is recent
        assertThat(auditLog.getTimestamp()).isAfter(Instant.now().minusSeconds(60));
    }

    @Property(tries = 100)
    @Label("Compliance events should create complete audit logs with rule information")
    void complianceEventsShouldCreateCompleteAuditLogs(
            @ForAll @From("validComplianceActions") String action,
            @ForAll @NotBlank String ruleCode,
            @ForAll @NotBlank String violationType,
            @ForAll @From("validEntityTypes") String entityType,
            @ForAll @NotBlank String entityId,
            @ForAll @NotBlank String description) {

        // When: A compliance event is logged
        auditLogService.logComplianceEvent(action, ruleCode, violationType, entityType, entityId, description);
        entityManager.flush();

        // Then: An audit log should be created with all required fields
        List<AuditLog> auditLogs = auditLogRepository.findByModuleOrderByTimestampDesc("COMPLIANCE");
        
        assertThat(auditLogs).isNotEmpty();
        
        AuditLog auditLog = auditLogs.get(0);
        
        // Verify all required fields are present
        assertThat(auditLog.getTimestamp()).isNotNull();
        assertThat(auditLog.getUser()).isEqualTo(testUser);
        assertThat(auditLog.getUsername()).isEqualTo(testUser.getUsername());
        assertThat(auditLog.getAction()).isEqualTo(action);
        assertThat(auditLog.getEntityType()).isEqualTo(entityType);
        assertThat(auditLog.getEntityId()).isEqualTo(entityId);
        assertThat(auditLog.getDescription()).isEqualTo(description);
        assertThat(auditLog.getStatus()).isEqualTo("SUCCESS");
        assertThat(auditLog.getSeverity()).isEqualTo("HIGH");
        assertThat(auditLog.getModule()).isEqualTo("COMPLIANCE");
        assertThat(auditLog.getSubModule()).isEqualTo(ruleCode);
        assertThat(auditLog.getAdditionalData()).isNotNull();
        
        // Verify timestamp is recent
        assertThat(auditLog.getTimestamp()).isAfter(Instant.now().minusSeconds(60));
    }

    @Property(tries = 50)
    @Label("System events should create complete audit logs")
    void systemEventsShouldCreateCompleteAuditLogs(
            @ForAll @From("validSystemActions") String action,
            @ForAll @NotBlank String description,
            @ForAll @From("validSeverityLevels") String severity,
            @ForAll @From("validDataObjects") Map<String, Object> additionalData) {

        // When: A system event is logged
        auditLogService.logSystemEvent(action, description, severity, additionalData);
        entityManager.flush();

        // Then: An audit log should be created with all required fields
        List<AuditLog> auditLogs = auditLogRepository.findByModuleOrderByTimestampDesc("SYSTEM");
        
        assertThat(auditLogs).isNotEmpty();
        
        AuditLog auditLog = auditLogs.get(0);
        
        // Verify all required fields are present
        assertThat(auditLog.getTimestamp()).isNotNull();
        assertThat(auditLog.getAction()).isEqualTo(action);
        assertThat(auditLog.getEntityType()).isEqualTo("System");
        assertThat(auditLog.getDescription()).isEqualTo(description);
        assertThat(auditLog.getStatus()).isEqualTo("SUCCESS");
        assertThat(auditLog.getSeverity()).isEqualTo(severity);
        assertThat(auditLog.getModule()).isEqualTo("SYSTEM");
        
        if (additionalData != null && !additionalData.isEmpty()) {
            assertThat(auditLog.getAdditionalData()).isNotNull();
        }
        
        // Verify timestamp is recent
        assertThat(auditLog.getTimestamp()).isAfter(Instant.now().minusSeconds(60));
    }

    // Data generators

    @Provide
    Arbitrary<String> validActions() {
        return Arbitraries.of("CREATE", "UPDATE", "DELETE", "VIEW", "EXPORT");
    }

    @Provide
    Arbitrary<String> validAuthActions() {
        return Arbitraries.of("LOGIN", "LOGOUT", "FAILED_LOGIN", "PASSWORD_CHANGE");
    }

    @Provide
    Arbitrary<String> validSecurityActions() {
        return Arbitraries.of("PERMISSION_DENIED", "UNAUTHORIZED_ACCESS", "SUSPICIOUS_ACTIVITY", "ACCOUNT_LOCKED");
    }

    @Provide
    Arbitrary<String> validComplianceActions() {
        return Arbitraries.of("RULE_VIOLATION", "COMPLIANCE_CHECK", "POLICY_ENFORCEMENT", "AUDIT_REVIEW");
    }

    @Provide
    Arbitrary<String> validSystemActions() {
        return Arbitraries.of("STARTUP", "SHUTDOWN", "CONFIG_CHANGE", "MAINTENANCE", "BACKUP");
    }

    @Provide
    Arbitrary<String> validEntityTypes() {
        return Arbitraries.of("Employee", "User", "Department", "Organization", "PayrollRun", "LeaveRequest", "JobPosting");
    }

    @Provide
    Arbitrary<String> validSeverityLevels() {
        return Arbitraries.of("LOW", "MEDIUM", "HIGH", "CRITICAL");
    }

    @Provide
    Arbitrary<Map<String, Object>> validDataObjects() {
        return Arbitraries.maps(
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20),
            Arbitraries.oneOf(
                Arbitraries.strings().ofMaxLength(100),
                Arbitraries.integers(),
                Arbitraries.longs(),
                Arbitraries.doubles()
            )
        ).ofMinSize(0).ofMaxSize(5);
    }
}

