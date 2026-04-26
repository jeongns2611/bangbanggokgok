package com.ssafy.backend.domain.code.repository;

import com.ssafy.backend.domain.code.entity.CommonCodeDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommonCodeDetailRepository extends JpaRepository<CommonCodeDetail, Long> {
    /**
     * 1. code_name 단독 검색 (기존)
     */
    Optional<CommonCodeDetail> findByCodeName(String codeName);

    /**
     * 2. code_name과 group_id 복합 검색
     */
    Optional<CommonCodeDetail> findByCodeNameAndGroup_Id(String codeName, Long groupId);

    Optional<CommonCodeDetail> findByCodeNameAndGroup_IdAndIsActiveTrue(String codeName, Long groupId);

    /**
     * 3. (참고) 특정 그룹 내의 모든 코드 조회
     */
    List<CommonCodeDetail> findByGroup_IdAndIsActiveTrueOrderBySortOrderAsc(Long groupId);

    List<CommonCodeDetail> findByGroup_IdAndCodeNameInAndIsActiveTrue(Long groupId, Collection<String> codeNames);

}
