package com.talentx.hrms.repository;

import com.talentx.hrms.entity.core.Organization;
import com.talentx.hrms.entity.enums.LeaveTypeCategory;
import com.talentx.hrms.entity.leave.LeaveType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {
    
    // Find by name and organization
    Optional<LeaveType> findByNameAndOrganization(String name, Organization organization);
    
    // Find by code and organization
    Optional<LeaveType> findByCodeAndOrganization(String code, Organization organization);
    
    // Find all leave types by organization
    List<LeaveType> findByOrganization(Organization organization);
    
    // Find all leave types by organization with pagination
    Page<LeaveType> findByOrganization(Organization organization, Pageable pageable);
    
    // Find leave types by category
    List<LeaveType> findByOrganizationAndCategory(Organization organization, LeaveTypeCategory category);
    
    // Find paid leave types
    List<LeaveType> findByOrganizationAndIsPaidTrue(Organization organization);
    
    // Find unpaid leave types
    List<LeaveType> findByOrganizationAndIsPaidFalse(Organization organization);
    
    // Find leave types that require approval
    List<LeaveType> findByOrganizationAndRequiresApprovalTrue(Organization organization);
    
    // Find leave types that don't require approval
    List<LeaveType> findByOrganizationAndRequiresApprovalFalse(Organization organization);
    
    // Find leave types that require documentation
    List<LeaveType> findByOrganizationAndRequiresDocumentationTrue(Organization organization);
    
    // Find leave types eligible for probation employees
    List<LeaveType> findByOrganizationAndProbationEligibleTrue(Organization organization);
    
    // Find gender-specific leave types
    List<LeaveType> findByOrganizationAndIsGenderSpecificTrue(Organization organization);
    
    // Find leave types by applicable gender
    List<LeaveType> findByOrganizationAndApplicableGender(Organization organization, String gender);
    
    // Find leave types that allow carry forward
    List<LeaveType> findByOrganizationAndIsCarryForwardTrue(Organization organization);
    
    // Search leave types by name
    @Query("SELECT lt FROM LeaveType lt WHERE lt.organization = :organization AND " +
           "LOWER(lt.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<LeaveType> findByOrganizationAndNameContainingIgnoreCase(@Param("organization") Organization organization, 
                                                                 @Param("name") String name, 
                                                                 Pageable pageable);
    
    // Find leave types applicable to employee
    @Query("SELECT lt FROM LeaveType lt WHERE lt.organization = :organization AND " +
           "(lt.isGenderSpecific = false OR lt.applicableGender = :gender) AND " +
           "(lt.probationEligible = true OR :isOnProbation = false)")
    List<LeaveType> findApplicableToEmployee(@Param("organization") Organization organization,
                                           @Param("gender") String gender,
                                           @Param("isOnProbation") boolean isOnProbation);
    
    // Count leave types by organization
    long countByOrganization(Organization organization);
    
    // Count leave types by category
    long countByOrganizationAndCategory(Organization organization, LeaveTypeCategory category);
    
    // Check if leave type name exists in organization
    boolean existsByNameAndOrganization(String name, Organization organization);
    
    // Check if leave type code exists in organization
    boolean existsByCodeAndOrganization(String code, Organization organization);
    
    // Find active leave types
    @Query("SELECT lt FROM LeaveType lt WHERE lt.organization = :organization AND lt.active = true")
    List<LeaveType> findActiveByOrganization(@Param("organization") Organization organization);
    
    // Find leave types with usage statistics
    @Query("SELECT lt, COUNT(lr) FROM LeaveType lt LEFT JOIN lt.leaveRequests lr " +
           "WHERE lt.organization = :organization GROUP BY lt")
    List<Object[]> findLeaveTypesWithUsageCount(@Param("organization") Organization organization);
}

