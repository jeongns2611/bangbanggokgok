package com.ssafy.backend.domain.user.repository;

import com.ssafy.backend.domain.user.entity.UserNeedFloor;
import com.ssafy.backend.domain.user.entity.UserNeedFloorId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNeedFloorRepository extends JpaRepository<UserNeedFloor, UserNeedFloorId> {

    List<UserNeedFloor> findByIdUserNeedIdInOrderByCommonCodeDetailSortOrderAsc(Collection<Long> userNeedIds);

    List<UserNeedFloor> findByIdUserNeedIdOrderByCommonCodeDetailSortOrderAsc(Long userNeedId);

    void deleteByIdUserNeedId(Long userNeedId);
}
