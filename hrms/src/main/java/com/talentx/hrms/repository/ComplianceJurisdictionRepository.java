package com.talentx.hrms.repository;

import com.talentx.hrms.entity.compliance.ComplianceJurisdiction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplianceJurisdictionRepository extends JpaRepository<ComplianceJurisdiction, Long> {
    
    // Find by code
    Optional<ComplianceJurisdiction> findByCode(String code);
    
    // Find by name
    Optional<ComplianceJurisdiction> findByName(String name);
    
    // Find by country
    List<ComplianceJurisdiction> findByCountry(String country);
    Page<ComplianceJurisdiction> findByCountry(String country, Pageable pageable);
    
    // Find by state/province
    List<ComplianceJurisdiction> findByStateProvince(String stateProvince);
    
    // Find by country and state/province
    List<ComplianceJurisdiction> findByCountryAndStateProvince(String country, String stateProvince);
    
    // Find by jurisdiction type
    List<ComplianceJurisdiction> findByJurisdictionType(String jurisdictionType);
    
    // Find default jurisdiction
    Optional<ComplianceJurisdiction> findByIsDefaultTrue();
    
    // Find by regulatory body
    List<ComplianceJurisdiction> findByRegulatoryBody(String regulatoryBody);
    
    // Search by name or description
    @Query("SELECT j FROM ComplianceJurisdiction j WHERE " +
           "(LOWER(j.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<ComplianceJurisdiction> findByNameOrDescriptionContainingIgnoreCase(@Param("searchTerm") String searchTerm, 
                                                                            Pageable pageable);
    
    // Find all ordered by name
    @Query("SELECT j FROM ComplianceJurisdiction j ORDER BY j.name")
    List<ComplianceJurisdiction> findAllOrderByName();
    
    // Find all ordered by country then state/province
    @Query("SELECT j FROM ComplianceJurisdiction j ORDER BY j.country, j.stateProvince, j.name")
    List<ComplianceJurisdiction> findAllOrderByCountryAndState();
    
    // Check if code exists
    boolean existsByCode(String code);
    
    // Check if name exists
    boolean existsByName(String name);
    
    // Count by country
    long countByCountry(String country);
    
    // Count by jurisdiction type
    long countByJurisdictionType(String jurisdictionType);
}

