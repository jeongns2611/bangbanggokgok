package com.ssafy.backend.domain.user.repository;

import com.ssafy.backend.domain.user.entity.HouseTypeCode;
import com.ssafy.backend.domain.user.entity.HouseTypeCodeId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HouseTypeCodeRepository extends JpaRepository<HouseTypeCode, HouseTypeCodeId> {

    List<HouseTypeCode> findByIdUserNeedIdInOrderByIdCodeIdAsc(Collection<Long> userNeedIds);

    List<HouseTypeCode> findByIdUserNeedIdOrderByIdCodeIdAsc(Long userNeedId);
}
