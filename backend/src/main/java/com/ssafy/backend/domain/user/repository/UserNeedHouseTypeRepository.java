package com.ssafy.backend.domain.user.repository;

import com.ssafy.backend.domain.user.entity.UserNeedHouseType;
import com.ssafy.backend.domain.user.entity.UserNeedHouseTypeId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNeedHouseTypeRepository extends JpaRepository<UserNeedHouseType, UserNeedHouseTypeId> {

    List<UserNeedHouseType> findByIdUserNeedIdInOrderByCommonCodeDetailSortOrderAsc(Collection<Long> userNeedIds);

    List<UserNeedHouseType> findByIdUserNeedIdOrderByCommonCodeDetailSortOrderAsc(Long userNeedId);

    void deleteByIdUserNeedId(Long userNeedId);
}
