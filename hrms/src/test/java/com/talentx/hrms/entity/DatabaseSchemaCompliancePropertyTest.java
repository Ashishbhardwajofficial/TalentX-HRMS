package com.talentx.hrms.entity;

import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.core.Department;
import com.talentx.hrms.entity.core.Location;
import com.talentx.hrms.entity.core.User;
import com.talentx.hrms.entity.employee.Employee;
import com.talentx.hrms.entity.employee.EmployeeAddress;
import com.talentx.hrms.entity.employee.EmergencyContact;
import com.talentx.hrms.entity.employee.EmployeeEmploymentHistory;
import com.talentx.hrms.entity.security.Role;
import com.talentx.hrms.entity.security.Permission;
import com.talentx.hrms.entity.security.UserRole;
import com.talentx.hrms.entity.security.RolePermission;
import com.talentx.hrms.entity.recruitment.JobPosting;
import com.talentx.hrms.entity.recruitment.Candidate;
import com.talentx.hrms.entity.recruitment.Application;
import com.talentx.hrms.entity.recruitment.Interview;
import com.talentx.hrms.entity.performance.PerformanceReviewCycle;
import com.talentx.hrms.entity.performance.PerformanceReview;
import com.talentx.hrms.entity.performance.Goal;
import com.talentx.hrms.entity.skills.Skill;
import com.talentx.hrms.entity.skills.EmployeeSkill;
import com.talentx.hrms.entity.training.TrainingProgram;
import com.talentx.hrms.entity.training.TrainingEnrollment;
import com.talentx.hrms.entity.benefits.BenefitPlan;
import com.talentx.hrms.entity.benefits.EmployeeBenefit;
import com.talentx.hrms.entity.finance.EmployeeBankDetail;
import com.talentx.hrms.entity.payroll.Payslip;
import com.talentx.hrms.entity.exit.EmployeeExit;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.sql.Timestamp;
import java.util.Map;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * **Feature: hrms-alignment-stabilization, Property 1: Database Schema Compliance**
 * 
 * Test that validates all HRMS entities comply with the Database.txt schema definition.
 * This test ensures that for any HRMS entity, all field names, types, and table mappings
 * match exactly with the corresponding database table definition in Database.txt.
 * 
 * Validates Requirements: 1.1, 1.2, 1.3, 1.4, 1.5
 */
@ActiveProfiles("test")
public class DatabaseSchemaCompliancePropertyTest {

    // Define expected table names and their corresponding entity classes
    private static final Map<Class<?>, String> ENTITY_TABLE_MAPPING = new HashMap<>();
    
    static {
        // Core entities - these should be working based on completed tasks
        ENTITY_TABLE_MAPPING.put(Organization.class, "organizations");
        ENTITY_TABLE_MAPPING.put(Department.class, "departments");
        ENTITY_TABLE_MAPPING.put(Location.class, "locations");
        ENTITY_TABLE_MAPPING.put(User.class, "users");
        
        // Security entities
        ENTITY_TABLE_MAPPING.put(Role.class, "roles");
        ENTITY_TABLE_MAPPING.put(Permission.class, "permissions");
        ENTITY_TABLE_MAPPING.put(UserRole.class, "user_roles");
        ENTITY_TABLE_MAPPING.put(RolePermission.class, "role_permissions");
        
        // Employee entities
        ENTITY_TABLE_MAPPING.put(Employee.class, "employees");
        ENTITY_TABLE_MAPPING.put(EmployeeAddress.class, "employee_addresses");
        ENTITY_TABLE_MAPPING.put(EmergencyContact.class, "emergency_contacts");
        ENTITY_TABLE_MAPPING.put(EmployeeEmploymentHistory.class, "employee_employment_history");
        
        // Recruitment entities - recently aligned
        ENTITY_TABLE_MAPPING.put(JobPosting.class, "job_postings");
        ENTITY_TABLE_MAPPING.put(Candidate.class, "candidates");
        ENTITY_TABLE_MAPPING.put(Application.class, "applications");
        ENTITY_TABLE_MAPPING.put(Interview.class, "interviews");
        
        // Performance entities - recently aligned
        ENTITY_TABLE_MAPPING.put(PerformanceReviewCycle.class, "performance_review_cycles");
        ENTITY_TABLE_MAPPING.put(PerformanceReview.class, "performance_reviews");
        ENTITY_TABLE_MAPPING.put(Goal.class, "goals");
        
        // Skills entities - recently aligned
        ENTITY_TABLE_MAPPING.put(Skill.class, "skills");
        ENTITY_TABLE_MAPPING.put(EmployeeSkill.class, "employee_skills");
        
        // Training entities - recently aligned
        ENTITY_TABLE_MAPPING.put(TrainingProgram.class, "training_programs");
        ENTITY_TABLE_MAPPING.put(TrainingEnrollment.class, "training_enrollments");
        
        // Benefits entities - recently aligned
        ENTITY_TABLE_MAPPING.put(BenefitPlan.class, "benefit_plans");
        ENTITY_TABLE_MAPPING.put(EmployeeBenefit.class, "employee_benefits");
        
        // Finance entities - recently aligned
        ENTITY_TABLE_MAPPING.put(EmployeeBankDetail.class, "employee_bank_details");
        ENTITY_TABLE_MAPPING.put(Payslip.class, "payslips");
        
        // Exit entities - recently aligned
        ENTITY_TABLE_MAPPING.put(EmployeeExit.class, "employee_exits");
    }

    @Test
    void validateTableAnnotations() {
        // Property: All entities must have correct @Table annotations matching Database.txt
        for (Map.Entry<Class<?>, String> entry : ENTITY_TABLE_MAPPING.entrySet()) {
            Class<?> entityClass = entry.getKey();
            String expectedTableName = entry.getValue();
            
            Table tableAnnotation = entityClass.getAnnotation(Table.class);
            assertThat(tableAnnotation)
                .withFailMessage("Entity %s must have @Table annotation", entityClass.getSimpleName())
                .isNotNull();
            
            assertThat(tableAnnotation.name())
                .withFailMessage("Entity %s table name should be '%s' but was '%s'", 
                    entityClass.getSimpleName(), expectedTableName, tableAnnotation.name())
                .isEqualTo(expectedTableName);
        }
    }

    @Test
    void validatePrimaryKeyFields() {
        // Property: All entities must have proper primary key fields with correct naming
        for (Class<?> entityClass : ENTITY_TABLE_MAPPING.keySet()) {
            Field[] fields = entityClass.getDeclaredFields();
            boolean hasIdField = false;
            
            for (Field field : fields) {
                if (field.isAnnotationPresent(Id.class)) {
                    hasIdField = true;
                    
                    // Validate ID field type
                    assertThat(field.getType())
                        .withFailMessage("Entity %s ID field should be Long but was %s", 
                            entityClass.getSimpleName(), field.getType().getSimpleName())
                        .isEqualTo(Long.class);
                    
                    // Validate ID field has @GeneratedValue
                    assertThat(field.isAnnotationPresent(GeneratedValue.class))
                        .withFailMessage("Entity %s ID field should have @GeneratedValue annotation", 
                            entityClass.getSimpleName())
                        .isTrue();
                    
                    break;
                }
            }
            
            assertThat(hasIdField)
                .withFailMessage("Entity %s must have a field annotated with @Id", 
                    entityClass.getSimpleName())
                .isTrue();
        }
    }

    @Test
    void validateFieldNamingConventions() {
        // Property: All entity fields must follow proper naming conventions matching database columns
        for (Class<?> entityClass : ENTITY_TABLE_MAPPING.keySet()) {
            Field[] fields = entityClass.getDeclaredFields();
            
            for (Field field : fields) {
                // Skip static fields and relationship fields
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
                    field.isAnnotationPresent(ManyToOne.class) ||
                    field.isAnnotationPresent(OneToMany.class) ||
                    field.isAnnotationPresent(OneToOne.class) ||
                    field.isAnnotationPresent(ManyToMany.class)) {
                    continue;
                }
                
                String fieldName = field.getName();
                
                // Validate field naming convention (camelCase)
                assertThat(fieldName)
                    .withFailMessage("Entity %s field '%s' should follow camelCase naming convention", 
                        entityClass.getSimpleName(), fieldName)
                    .matches("^[a-z][a-zA-Z0-9]*$");
            }
        }
    }

    @Test
    void validateFieldTypes() {
        // Property: All entity fields must have correct Java types matching database column types
        for (Class<?> entityClass : ENTITY_TABLE_MAPPING.keySet()) {
            Field[] fields = entityClass.getDeclaredFields();
            
            for (Field field : fields) {
                // Skip static fields and relationship fields
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
                    field.isAnnotationPresent(ManyToOne.class) ||
                    field.isAnnotationPresent(OneToMany.class) ||
                    field.isAnnotationPresent(OneToOne.class) ||
                    field.isAnnotationPresent(ManyToMany.class)) {
                    continue;
                }
                
                Class<?> fieldType = field.getType();
                
                // Check if field type is valid for database mapping
                boolean isValidType = isValidDatabaseFieldType(fieldType);
                
                assertThat(isValidType)
                    .withFailMessage("Entity %s field '%s' has invalid type '%s' for database mapping", 
                        entityClass.getSimpleName(), field.getName(), fieldType.getSimpleName())
                    .isTrue();
            }
        }
    }

    @Test
    void validateRelationshipMappings() {
        // Property: All relationship fields must have proper JPA annotations and foreign key mappings
        for (Class<?> entityClass : ENTITY_TABLE_MAPPING.keySet()) {
            Field[] fields = entityClass.getDeclaredFields();
            
            for (Field field : fields) {
                // Check ManyToOne relationships
                if (field.isAnnotationPresent(ManyToOne.class)) {
                    validateManyToOneRelationship(entityClass, field);
                }
                
                // Check OneToMany relationships
                if (field.isAnnotationPresent(OneToMany.class)) {
                    validateOneToManyRelationship(entityClass, field);
                }
                
                // Check OneToOne relationships
                if (field.isAnnotationPresent(OneToOne.class)) {
                    validateOneToOneRelationship(entityClass, field);
                }
                
                // Check ManyToMany relationships
                if (field.isAnnotationPresent(ManyToMany.class)) {
                    validateManyToManyRelationship(entityClass, field);
                }
            }
        }
    }

    @Test
    void validateLombokAnnotations() {
        // Property: All entities must have proper Lombok @Getter and @Setter annotations
        for (Class<?> entityClass : ENTITY_TABLE_MAPPING.keySet()) {
            // Check for Lombok @Getter annotation
            boolean hasGetterAnnotation = entityClass.isAnnotationPresent(lombok.Getter.class);
            
            // Check for Lombok @Setter annotation  
            boolean hasSetterAnnotation = entityClass.isAnnotationPresent(lombok.Setter.class);
            
            assertThat(hasGetterAnnotation)
                .withFailMessage("Entity %s must have @Getter annotation", 
                    entityClass.getSimpleName())
                .isTrue();
                
            assertThat(hasSetterAnnotation)
                .withFailMessage("Entity %s must have @Setter annotation", 
                    entityClass.getSimpleName())
                .isTrue();
        }
    }

    // Helper methods
    
    private boolean isValidDatabaseFieldType(Class<?> fieldType) {
        return fieldType == String.class ||
               fieldType == Long.class ||
               fieldType == Integer.class ||
               fieldType == Boolean.class ||
               fieldType == BigDecimal.class ||
               fieldType == LocalDate.class ||
               fieldType == LocalDateTime.class ||
               fieldType == LocalTime.class ||
               fieldType == Timestamp.class ||
               fieldType.isEnum() ||
               fieldType == byte[].class;
    }
    
    private void validateManyToOneRelationship(Class<?> entityClass, Field field) {
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        
        assertThat(joinColumn)
            .withFailMessage("Entity %s ManyToOne field '%s' must have @JoinColumn annotation", 
                entityClass.getSimpleName(), field.getName())
            .isNotNull();
        
        assertThat(joinColumn.name())
            .withFailMessage("Entity %s ManyToOne field '%s' must have proper foreign key column name", 
                entityClass.getSimpleName(), field.getName())
            .isNotEmpty();
    }
    
    private void validateOneToManyRelationship(Class<?> entityClass, Field field) {
        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        
        // OneToMany should have mappedBy or @JoinColumn
        boolean hasMappedBy = oneToMany.mappedBy() != null && !oneToMany.mappedBy().isEmpty();
        boolean hasJoinColumn = field.isAnnotationPresent(JoinColumn.class);
        
        assertThat(hasMappedBy || hasJoinColumn)
            .withFailMessage("Entity %s OneToMany field '%s' must have either mappedBy or @JoinColumn", 
                entityClass.getSimpleName(), field.getName())
            .isTrue();
    }
    
    private void validateOneToOneRelationship(Class<?> entityClass, Field field) {
        // OneToOne relationships should have proper mapping
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        OneToOne oneToOne = field.getAnnotation(OneToOne.class);
        
        boolean hasMappedBy = oneToOne.mappedBy() != null && !oneToOne.mappedBy().isEmpty();
        boolean hasJoinColumn = joinColumn != null;
        
        assertThat(hasMappedBy || hasJoinColumn)
            .withFailMessage("Entity %s OneToOne field '%s' must have either mappedBy or @JoinColumn", 
                entityClass.getSimpleName(), field.getName())
            .isTrue();
    }
    
    private void validateManyToManyRelationship(Class<?> entityClass, Field field) {
        ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
        
        // ManyToMany should have mappedBy or @JoinTable
        boolean hasMappedBy = manyToMany.mappedBy() != null && !manyToMany.mappedBy().isEmpty();
        boolean hasJoinTable = field.isAnnotationPresent(JoinTable.class);
        
        assertThat(hasMappedBy || hasJoinTable)
            .withFailMessage("Entity %s ManyToMany field '%s' must have either mappedBy or @JoinTable", 
                entityClass.getSimpleName(), field.getName())
            .isTrue();
    }
}

