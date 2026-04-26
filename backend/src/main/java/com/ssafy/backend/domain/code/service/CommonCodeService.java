package com.ssafy.backend.domain.code.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ssafy.backend.domain.code.entity.CommonCodeDetail;
import com.ssafy.backend.domain.code.repository.CommonCodeDetailRepository;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(readOnly = true)
public class CommonCodeService {
    private final CommonCodeDetailRepository commonCodeDetailRepository;

    // Caffeine Cache 정의
    private final Cache<String, Long> nameToCodeCache;
    private final Cache<Long, String> CodeToNameCache;

    // 생성자에서 캐시 설정 초기화
    public CommonCodeService(CommonCodeDetailRepository commonCodeDetailRepository) {
        this.commonCodeDetailRepository = commonCodeDetailRepository;

        this.nameToCodeCache = Caffeine.newBuilder()
                // Cache Stampede 발생가능성 => 해결필요
                .expireAfterWrite(7, TimeUnit.DAYS) // 7일 뒤 만료 (데이터 최신화)
                // 최대 1000개 제한. 초과 시 W-TinyLFU(빈도+최신성 고려) 정책으로 제거
                .maximumSize(1000)
                .build();

        this.CodeToNameCache = Caffeine.newBuilder()
                .expireAfterWrite(7, TimeUnit.DAYS) // 7일 뒤 만료 (데이터 최신화)
                // 최대 1000개 제한. 초과 시 W-TinyLFU(빈도+최신성 고려) 정책으로 제거
                .maximumSize(1000)
                .build();
    }

    public Long getCommonCode(String codeName) {
        if (codeName == null) return null;

        // 캐시 조회: 없으면 DB 조회 후 캐싱 (Atomic하게 동작)
        return nameToCodeCache.get(codeName.trim(), name ->
                commonCodeDetailRepository.findByCodeName(name)
                        .map(CommonCodeDetail::getId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.FAIL, "존재하지 않는 코드명입니다: " + name))
        );
    }

    public String getCommonCodeName(Long codeId) {
        if (codeId == null) return null;

        return CodeToNameCache.get(codeId, id ->
                commonCodeDetailRepository.findById(id)
                        .map(CommonCodeDetail::getCodeName)
                        .orElseThrow(() -> new BusinessException(ErrorCode.FAIL, "존재하지 않는 코드 아이디입니다: " + id))
        );
    }

    public List<Long> getCommonCodeList(List<String> codeNames) {
        if (codeNames == null || codeNames.isEmpty()) {
            return List.of();
        }
        return codeNames.stream()
                .map(this::getCommonCode)
                .toList();
    }

    // 그룹 조회 등은 캐싱 빈도가 낮다면 기존 로직 유지
    public Long getCommonCodeInGroup(Long groupId, String codeName) {
        return commonCodeDetailRepository.findByCodeNameAndGroup_Id(codeName.trim(), groupId)
                .map(CommonCodeDetail::getId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.FAIL, String.format("해당 그룹(ID: %d) 내에 존재하지 않는 코드명입니다: %s", groupId, codeName)
                ));
    }

    public List<CommonCodeDetail> getActiveCodesByGroup(Long groupId) {
        List<CommonCodeDetail> codes = commonCodeDetailRepository.findByGroup_IdAndIsActiveTrueOrderBySortOrderAsc(groupId);

        if (codes.isEmpty()) {
            throw new BusinessException(ErrorCode.FAIL, "해당 그룹에 활성화된 코드가 존재하지 않습니다. 그룹 ID: " + groupId);
        }

        return codes;
    }
}
