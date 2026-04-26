package com.ssafy.backend.domain.user.repository;

import com.ssafy.backend.domain.user.entity.UserNeed;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface UserNeedRepository extends JpaRepository<UserNeed, Long> {

    boolean existsByUser_IdAndDeletedAtIsNull(Long userId);

    List<UserNeed> findByUser_IdAndDeletedAtIsNullOrderByIdDesc(Long userId);

    Optional<UserNeed> findByIdAndUser_IdAndDeletedAtIsNull(Long id, Long userId);
    Optional<UserNeed> findByUser_IdAndIsDefaultNeedTrueAndDeletedAtIsNull(Long userId);
}
