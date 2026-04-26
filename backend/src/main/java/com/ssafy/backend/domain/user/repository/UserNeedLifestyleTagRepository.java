package com.ssafy.backend.domain.user.repository;

import com.ssafy.backend.domain.user.entity.UserNeedLifestyleTag;
import com.ssafy.backend.domain.user.entity.UserNeedLifestyleTagId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNeedLifestyleTagRepository extends JpaRepository<UserNeedLifestyleTag, UserNeedLifestyleTagId> {

    List<UserNeedLifestyleTag> findByIdUserNeedIdInOrderByCommonCodeDetailSortOrderAsc(Collection<Long> userNeedIds);

    List<UserNeedLifestyleTag> findByIdUserNeedIdOrderByCommonCodeDetailSortOrderAsc(Long userNeedId);

    void deleteByIdUserNeedId(Long userNeedId);
}
