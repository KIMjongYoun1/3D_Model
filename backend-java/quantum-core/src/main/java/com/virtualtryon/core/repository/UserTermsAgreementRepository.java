package com.virtualtryon.core.repository;

import com.virtualtryon.core.entity.Terms;
import com.virtualtryon.core.entity.User;
import com.virtualtryon.core.entity.UserTermsAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserTermsAgreementRepository extends JpaRepository<UserTermsAgreement, UUID> {

    List<UserTermsAgreement> findByUserOrderByAgreedAtDesc(User user);

    boolean existsByUserAndTerms(User user, Terms terms);

    @Query("SELECT uta.terms.id FROM UserTermsAgreement uta WHERE uta.user.id = :userId")
    List<UUID> findAgreedTermsIdsByUserId(@Param("userId") UUID userId);
}
