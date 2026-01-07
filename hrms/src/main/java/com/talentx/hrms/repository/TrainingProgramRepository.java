package com.talentx.hrms.repository;

import com.talentx.hrms.entity.training.TrainingProgram;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingProgramRepository extends JpaRepository<TrainingProgram, Long> {
    
    // Find by organization
    List<TrainingProgram> findByOrganizationId(Long organizationId);
    Page<TrainingProgram> findByOrganizationId(Long organizationId, Pageable pageable);
    
    // Find active programs by organization
    List<TrainingProgram> findByOrganizationIdAndActiveTrue(Long organizationId);
    Page<TrainingProgram> findByOrganizationIdAndActiveTrue(Long organizationId, Pageable pageable);
    
    // Find by training type
    List<TrainingProgram> findByOrganizationIdAndTrainingType(Long organizationId, TrainingProgram.TrainingType trainingType);
    Page<TrainingProgram> findByOrganizationIdAndTrainingType(Long organizationId, TrainingProgram.TrainingType trainingType, Pageable pageable);
    
    // Find by delivery method
    List<TrainingProgram> findByOrganizationIdAndDeliveryMethod(Long organizationId, TrainingProgram.DeliveryMethod deliveryMethod);
    
    // Find mandatory programs
    List<TrainingProgram> findByOrganizationIdAndIsMandatoryTrue(Long organizationId);
    Page<TrainingProgram> findByOrganizationIdAndIsMandatoryTrue(Long organizationId, Pageable pageable);
    
    // Find by title containing (case-insensitive)
    @Query("SELECT tp FROM TrainingProgram tp WHERE tp.organizationId = :organizationId AND LOWER(tp.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<TrainingProgram> findByOrganizationIdAndTitleContainingIgnoreCase(@Param("organizationId") Long organizationId, @Param("title") String title);
    
    @Query("SELECT tp FROM TrainingProgram tp WHERE tp.organizationId = :organizationId AND LOWER(tp.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<TrainingProgram> findByOrganizationIdAndTitleContainingIgnoreCase(@Param("organizationId") Long organizationId, @Param("title") String title, Pageable pageable);
    
    // Find by provider
    List<TrainingProgram> findByOrganizationIdAndProvider(Long organizationId, String provider);
    
    // Search with comprehensive criteria
    @Query("SELECT tp FROM TrainingProgram tp WHERE tp.organizationId = :organizationId AND " +
           "(:title IS NULL OR LOWER(tp.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:trainingType IS NULL OR tp.trainingType = :trainingType) AND " +
           "(:deliveryMethod IS NULL OR tp.deliveryMethod = :deliveryMethod) AND " +
           "(:isMandatory IS NULL OR tp.isMandatory = :isMandatory) AND " +
           "(:provider IS NULL OR LOWER(tp.provider) LIKE LOWER(CONCAT('%', :provider, '%'))) AND " +
           "(:active IS NULL OR tp.active = :active)")
    Page<TrainingProgram> findBySearchCriteria(@Param("organizationId") Long organizationId,
                                              @Param("title") String title,
                                              @Param("trainingType") TrainingProgram.TrainingType trainingType,
                                              @Param("deliveryMethod") TrainingProgram.DeliveryMethod deliveryMethod,
                                              @Param("isMandatory") Boolean isMandatory,
                                              @Param("provider") String provider,
                                              @Param("active") Boolean active,
                                              Pageable pageable);
    
    // Get distinct training types for organization
    @Query("SELECT DISTINCT tp.trainingType FROM TrainingProgram tp WHERE tp.organizationId = :organizationId AND tp.trainingType IS NOT NULL ORDER BY tp.trainingType")
    List<TrainingProgram.TrainingType> findDistinctTrainingTypesByOrganizationId(@Param("organizationId") Long organizationId);
    
    // Get distinct providers for organization
    @Query("SELECT DISTINCT tp.provider FROM TrainingProgram tp WHERE tp.organizationId = :organizationId AND tp.provider IS NOT NULL ORDER BY tp.provider")
    List<String> findDistinctProvidersByOrganizationId(@Param("organizationId") Long organizationId);
    
    // Count programs by type
    long countByOrganizationIdAndTrainingType(Long organizationId, TrainingProgram.TrainingType trainingType);
    
    // Count active programs
    long countByOrganizationIdAndActiveTrue(Long organizationId);
    
    // Count mandatory programs
    long countByOrganizationIdAndIsMandatoryTrue(Long organizationId);
    
    // Check if title exists for organization (case-insensitive)
    boolean existsByOrganizationIdAndTitleIgnoreCase(Long organizationId, String title);
    
    // Find programs with enrollments
    @Query("SELECT DISTINCT tp FROM TrainingProgram tp " +
           "INNER JOIN TrainingEnrollment te ON tp.id = te.trainingProgramId " +
           "WHERE tp.organizationId = :organizationId")
    List<TrainingProgram> findProgramsWithEnrollments(@Param("organizationId") Long organizationId);
    
    // Find programs without enrollments
    @Query("SELECT tp FROM TrainingProgram tp WHERE tp.organizationId = :organizationId AND tp.id NOT IN " +
           "(SELECT DISTINCT te.trainingProgramId FROM TrainingEnrollment te)")
    List<TrainingProgram> findProgramsWithoutEnrollments(@Param("organizationId") Long organizationId);
    
    // Find most popular programs (by enrollment count)
    @Query("SELECT tp FROM TrainingProgram tp " +
           "LEFT JOIN TrainingEnrollment te ON tp.id = te.trainingProgramId " +
           "WHERE tp.organizationId = :organizationId " +
           "GROUP BY tp.id " +
           "ORDER BY COUNT(te.id) DESC")
    List<TrainingProgram> findMostPopularPrograms(@Param("organizationId") Long organizationId, Pageable pageable);
    
    // Find programs by duration range
    @Query("SELECT tp FROM TrainingProgram tp WHERE tp.organizationId = :organizationId AND " +
           "(:minHours IS NULL OR tp.durationHours >= :minHours) AND " +
           "(:maxHours IS NULL OR tp.durationHours <= :maxHours)")
    List<TrainingProgram> findByDurationRange(@Param("organizationId") Long organizationId,
                                             @Param("minHours") Integer minHours,
                                             @Param("maxHours") Integer maxHours);
}

