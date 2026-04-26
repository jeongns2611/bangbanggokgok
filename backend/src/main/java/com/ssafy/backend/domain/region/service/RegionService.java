package com.ssafy.backend.domain.region.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.ssafy.backend.domain.region.entity.Region;
import com.ssafy.backend.domain.region.entity.RegionSido;
import com.ssafy.backend.domain.region.entity.RegionSigungu;
import com.ssafy.backend.domain.region.repository.RegionRepository;
import com.ssafy.backend.domain.region.repository.RegionSidoRepository;
import com.ssafy.backend.domain.region.repository.RegionSigunguRepository;
import com.ssafy.backend.global.error.code.ErrorCode;
import com.ssafy.backend.global.error.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(readOnly = true)
public class RegionService {

    private final RegionSidoRepository regionSidoRepository;
    private final RegionSigunguRepository regionSigunguRepository;
    private final RegionRepository regionRepository;

    private final Cache<String, String> regionNameCache;
    private final Cache<String, String> regionCodeCache;

    private final String SIDO_KEY_PREFIX = "SIDO:";
    private final String SIGUNGU_KEY_PREFIX = "SIGUNGU:";
    private final String DONG_KEY_PREFIX = "DONG:";

    public RegionService(RegionSidoRepository regionSidoRepository, RegionSigunguRepository regionSigunguRepository, RegionRepository regionRepository) {
        this.regionSidoRepository = regionSidoRepository;
        this.regionSigunguRepository = regionSigunguRepository;
        this.regionRepository = regionRepository;

        Expiry<String, String> jitterExpiry = new Expiry<String, String>() {
            private long getJitterDuration() {
                // 5일부터 9일 사이의 시간을 나노초로 변환
                long minDays = 5;
                long maxDays = 9;
                long randomDays = ThreadLocalRandom.current().nextLong(minDays, maxDays + 1);
                return TimeUnit.DAYS.toNanos(randomDays);
            }

            @Override
            public long expireAfterCreate(String key, String value, long currentTime) {
                return getJitterDuration();
            }

            @Override
            public long expireAfterUpdate(String key, String value, long currentTime, long currentDuration) {
                return currentDuration; // 업데이트 시에는 기존 만료 시간 유지 (혹은 다시 계산 가능)
            }

            @Override
            public long expireAfterRead(String key, String value, long currentTime, long currentDuration) {
                return currentDuration; // 읽기 시에는 영향 없음
            }
        };

        // regionNameCache 설정
        this.regionNameCache = Caffeine.newBuilder()
                .expireAfter(jitterExpiry) // Jitter 적용
                .refreshAfterWrite(5, TimeUnit.DAYS) // 7일 후 백그라운드 갱신 시도
                .maximumSize(5000)
                .build(key -> loadNameFromDb(key)); // DB에서 다시 읽어오는 로직

        // regionCodeCache 설정
        this.regionCodeCache = Caffeine.newBuilder()
                .expireAfter(jitterExpiry)
                .refreshAfterWrite(5, TimeUnit.DAYS)
                .maximumSize(5000)
                .build(key -> loadCodeFromDb(key));
    }

    // CacheLoader를 위한 DB 조회 로직 분리
    private String loadNameFromDb(String key) {
        if (key.startsWith(SIDO_KEY_PREFIX)) {
            String code = key.replace(SIDO_KEY_PREFIX, "");
            return regionSidoRepository.findById(code).map(RegionSido::getSidoName).orElse("");
        }
        if (key.startsWith(SIGUNGU_KEY_PREFIX)) {
            String code = key.replace(SIGUNGU_KEY_PREFIX, "");
            return regionSigunguRepository.findById(code).map(RegionSigungu::getSigunguName).orElse("");
        }
        if (key.startsWith(DONG_KEY_PREFIX)) {
            String code = key.replace(DONG_KEY_PREFIX, "");
            return regionRepository.findById(code).map(Region::getDongName).orElse("");
        }
        return "";
    }

    private String loadCodeFromDb(String key) {
        if (key.startsWith(SIDO_KEY_PREFIX)) {
            String name = key.replace(SIDO_KEY_PREFIX, "");
            return regionSidoRepository.findBySidoName(name).map(RegionSido::getSidoCode).orElse("");
        }
        if (key.startsWith(SIGUNGU_KEY_PREFIX)) {
            String name = key.replace(SIGUNGU_KEY_PREFIX, "");
            String[] parts = name.split("/");
            if (parts.length == 2) {
                return regionSigunguRepository.findSigunguCodeByNames(parts[0], parts[1]).orElse("");
            }
        }
        if (key.startsWith(DONG_KEY_PREFIX)) {
            String name = key.replace(DONG_KEY_PREFIX, "");
            String[] parts = name.split("/");
            if (parts.length == 3) {
                return regionRepository.findRegionCodeByNames(parts[0], parts[1], parts[2]).orElse("");
            }
        }
        return "";
    }

    // Cahce Warming 적용
    @PostConstruct
    public void initCache() {
        // regionNameCache(Code -> Name)
        // regionCodeCache(Name -> Code)

        List<RegionSido> sidoList = regionSidoRepository.findAll();
        for (RegionSido sido : sidoList) {
            regionNameCache.put(makeSidoKey(sido.getSidoCode()), sido.getSidoName());
            regionCodeCache.put(makeSidoKey(sido.getSidoName()), sido.getSidoCode());
        }

        List<RegionSigungu> sigunguList = regionSigunguRepository.findAllWithSidoFetch();
        for (RegionSigungu sigungu : sigunguList) {
            regionNameCache.put(makeSigunguKey(sigungu.getSigunguCode()), sigungu.getSigunguName());
            regionCodeCache.put(makeSigunguKey(sigungu.getRegionSido().getSidoName(), sigungu.getSigunguName()), sigungu.getSigunguCode());
        }

        List<Region> regionList = regionRepository.findAllWithSidoAndSigunguFetch();
        for (Region region : regionList) {
            regionNameCache.put(makeDongKey(region.getRegionCode()), region.getDongName());
            regionCodeCache.put(makeDongKey(region.getRegionSido().getSidoName(), region.getRegionSigungu().getSigunguName(), region.getDongName()), region.getRegionCode());
        }
    }

    public String getSidoCode(String sidoName) {
        if (sidoName == null) return null;
        return regionCodeCache.get(makeSidoKey(sidoName), key ->
                regionSidoRepository.findBySidoName(sidoName.trim())
                        .map(RegionSido::getSidoCode)
                        .orElseThrow(() -> new BusinessException(ErrorCode.FAIL, "존재하지 않는 지역명입니다: " + sidoName))
        );
    }

    public String getSidoName(String sidoCode) {
        if (sidoCode == null) return null;
        return regionNameCache.get(makeSidoKey(sidoCode), key ->
                regionSidoRepository.findById(sidoCode)
                        .map(RegionSido::getSidoName)
                        .orElseThrow(() -> new BusinessException(ErrorCode.FAIL, "존재하지 않는 코드입니다: " + sidoCode))
        );
    }

    public String getSigunguCode(String sidoName, String sigunguName) {
        return regionCodeCache.get(makeSigunguKey(sidoName, sigunguName), key ->
                regionSigunguRepository.findSigunguCodeByNames(sidoName.trim(), sigunguName.trim())
                        .orElseThrow(() -> new BusinessException(ErrorCode.FAIL,
                                String.format("존재하지 않는 지역 정보입니다. (시도: %s, 시군구: %s)", sidoName, sigunguName)
                        ))
        );
    }

    public String getSigunguName(String sigunguCode) {
        if (sigunguCode == null) return null;
        return regionNameCache.get(makeSigunguKey(sigunguCode), key ->
                regionSigunguRepository.findById(sigunguCode)
                        .map(RegionSigungu::getSigunguName)
                        .orElseThrow(() -> new BusinessException(ErrorCode.FAIL, "존재하지 않는 코드입니다: " + sigunguCode))
        );
    }

    public String getRegionCode(String sidoName, String sigunguName, String dongName) {
        return regionCodeCache.get(makeDongKey(sidoName, sigunguName, dongName), key ->
                regionRepository.findRegionCodeByNames(
                                sidoName.trim(),
                                sigunguName.trim(),
                                dongName.trim()
                        )
                        .orElseThrow(() -> new BusinessException(ErrorCode.FAIL,
                                String.format("존재하지 않는 지역입니다: %s %s %s", sidoName, sigunguName, dongName)
                        ))
        );
    }

    public String getRegionName(String regionCode) {
        if (regionCode == null) return null;
        return regionNameCache.get(makeDongKey(regionCode), key ->
                regionRepository.findById(regionCode)
                        .map(Region::getDongName)
                        .orElseThrow(() -> new BusinessException(ErrorCode.FAIL, "존재하지 않는 코드입니다: " + regionCode))
        );
    }

    // Key Generation Methods
    private String makeSidoKey(String key) {
        return SIDO_KEY_PREFIX + key.trim();
    }

    // [Name -> Code] 검색용: "SIGUNGU:서울/강남구"
    private String makeSigunguKey(String sidoName, String sigunguName) {
        return SIGUNGU_KEY_PREFIX + sidoName.trim() + "/" + sigunguName.trim();
    }

    // [Code -> Name] 검색용: "SIGUNGU:11680"
    private String makeSigunguKey(String code) {
        return SIGUNGU_KEY_PREFIX + code.trim();
    }

    // [Name -> Code] 검색용: "DONG:서울/강남구/역삼동"
    private String makeDongKey(String sidoName, String sigunguName, String dongName) {
        return DONG_KEY_PREFIX + sidoName.trim() + "/" + sigunguName.trim() + "/" + dongName.trim();
    }

    // [Code -> Name] 검색용: "DONG:1168010100"
    private String makeDongKey(String code) {
        return DONG_KEY_PREFIX + code.trim();
    }
}
