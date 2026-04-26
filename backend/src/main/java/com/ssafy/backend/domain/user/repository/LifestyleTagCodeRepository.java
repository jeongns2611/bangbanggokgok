package com.ssafy.backend.domain.user.repository;

import com.ssafy.backend.domain.user.entity.LifestyleTagCode;
import com.ssafy.backend.domain.user.entity.LifestyleTagCodeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LifestyleTagCodeRepository extends JpaRepository<LifestyleTagCode, LifestyleTagCodeId> {
}
