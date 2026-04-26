'use client';

import { useEffect, useMemo, useRef, useState } from 'react';
import clsx from 'clsx';
import Button from '@/components/common/Button';
import CompareSection from '@/components/features/comparison/CompareSection';
import CompareSelectCard from '@/components/features/comparison/CompareSelectedCard';
import CompareTable from '@/components/features/comparison/CompareTable';
import FacilityDistanceChart from '@/components/features/comparison/FacilityDistanceChart';
import FinalEvaluationSection from '@/components/features/comparison/FinalEvaluationSection';
import StickyCompareSummary2 from '@/components/features/comparison/StickyCompareSummary2';
import Footer from '@/components/features/home/Footer';
import { fetchWithAuth } from '@/lib/fetchWithAuth';

type HouseImage = {
  image_url?: string | null;
  is_thumbnail?: boolean | null;
};

type CommuteData = {
  commuteTime: number;
  commuteDistance: number;
};

type InfraCount = {
  convenienceStoreCount: number;
  laundryCount: number;
  cafeCount: number;
  hospitalCount: number;
  pharmacyCount: number;
  busStopCount: number;
};

type MinDist = {
  convenienceDist: number;
  laundryDist: number;
  cafeDist: number;
  hospitalDist: number;
  pharmacyDist: number;
  subwayDist: number;
};

type DongStats = {
  cctvCount: number;
  streetLightCount: number;
  safetyFacilityCount: number;
  safetyScore: number;
  avgMeatPrice: number;
  avgMealPrice: number;
};

type ComparisonHouse = {
  images: HouseImage[];
  houseId: number;
  dong: string;
  houseType: string;
  rentType: string;
  houseStatus: string;
  deposit: number;
  monthlyCost: number;
  managementCost?: number | null;
  managementItems?: string | null;
  floorSize: number;
  buildYear?: number | null;
  description: string;
  viewCount: number;
  floor: string;
  commuteData: CommuteData;
  infraCount: InfraCount;
  minDist: MinDist;
  dongStats: DongStats;
  recommendReasons: string[];
  nonRecommendReasons: string[];
  scoreSummary: {
    trafficScore: number;
    infraScore: number;
    safetyScore: number;
    needFitScore: number;
  };
};

type CompareApiHouse = {
  images?: HouseImage[] | null;
  houseId?: number | null;
  dong?: string | null;
  houseType?: string | null;
  rentType?: string | null;
  houseStatus?: string | null;
  deposit?: number | null;
  monthlyCost?: number | null;
  managementCost?: number | null;
  managementItems?: string | null;
  floorSize?: number | null;
  buildYear?: number | null;
  description?: string | null;
  viewCount?: number | null;
  floor?: string | null;
  commuteData?: CommuteData | CommuteData[] | null;
  infraCount?: Partial<InfraCount> | null;
  minDist?: Partial<MinDist> | null;
  dongStats?: Partial<DongStats> | null;
  scoreSummary?: {
    trafficScore?: number;
    infraScore?: number;
    safetyScore?: number;
    saferyScore?: number;
    needFitScore?: number;
  } | null;
};

type CompareApiResponse = {
  comparisonData?: CompareApiHouse[];
  data?: {
    comparisonData?: CompareApiHouse[];
    data?: {
      comparisonData?: CompareApiHouse[];
    };
  };
};

type AiCompareResponse = {
  data?: {
    overallEvaluation?: string;
    houses?: Array<{
      houseId: number | null;
      houseLabel?: string;
      recommendationReasons?: string[];
      cautionReasons?: string[];
    }>;
  };
};

type WishlistApiItem = {
  houseId: number;
  thumbnailUrl?: string | null;
  sidoName: string;
  sigunguName: string;
  dongName: string;
  houseType: string;
  rentType: string;
  houseStatus: string;
  floor: string;
  deposit: number;
  monthlyCost: number;
};

function formatPrice(value: number) {
  return value.toLocaleString('ko-KR');
}

const mapWishlistItemToComparisonHouse = (
  item: WishlistApiItem,
): ComparisonHouse => {
  const dong = [item.sidoName, item.sigunguName, item.dongName]
    .filter(Boolean)
    .join(' ');

  return {
    houseId: item.houseId,
    images: item.thumbnailUrl
      ? [{ image_url: item.thumbnailUrl, is_thumbnail: true }]
      : [],
    dong,
    houseType: item.houseType,
    rentType: item.rentType,
    houseStatus: item.houseStatus,
    deposit: Number(item.deposit) || 0,
    monthlyCost: Number(item.monthlyCost) || 0,
    managementCost: null,
    managementItems: null,
    floorSize: 0,
    buildYear: null,
    description: '',
    viewCount: 0,
    floor: item.floor,
    commuteData: {
      commuteTime: 0,
      commuteDistance: 0,
    },
    infraCount: {
      convenienceStoreCount: 0,
      laundryCount: 0,
      cafeCount: 0,
      hospitalCount: 0,
      pharmacyCount: 0,
      busStopCount: 0,
    },
    minDist: {
      convenienceDist: 0,
      laundryDist: 0,
      cafeDist: 0,
      hospitalDist: 0,
      pharmacyDist: 0,
      subwayDist: 0,
    },
    dongStats: {
      cctvCount: 0,
      streetLightCount: 0,
      safetyFacilityCount: 0,
      safetyScore: 0,
      avgMeatPrice: 0,
      avgMealPrice: 0,
    },
    recommendReasons: ['비교 평가 데이터 준비 중입니다.'],
    nonRecommendReasons: ['비교 평가 데이터 준비 중입니다.'],
    scoreSummary: {
      trafficScore: 0,
      infraScore: 0,
      safetyScore: 0,
      needFitScore: 0,
    },
  };
};

const pickWishlistArray = (json: unknown): WishlistApiItem[] => {
  if (typeof json !== 'object' || json === null) {
    return [];
  }

  const payload = json as {
    data?: WishlistApiItem[] | { data?: WishlistApiItem[] };
  };

  if (Array.isArray(payload.data)) {
    return payload.data;
  }

  if (payload.data && Array.isArray(payload.data.data)) {
    return payload.data.data;
  }

  return [];
};

const toCommuteData = (
  commuteData: CompareApiHouse['commuteData'],
): CommuteData => {
  if (Array.isArray(commuteData)) {
    const first = commuteData[0];
    return {
      commuteTime: Number(first?.commuteTime) || 0,
      commuteDistance: Number(first?.commuteDistance) || 0,
    };
  }

  return {
    commuteTime: Number(commuteData?.commuteTime) || 0,
    commuteDistance: Number(commuteData?.commuteDistance) || 0,
  };
};

const toInfraCount = (infraCount: CompareApiHouse['infraCount']): InfraCount => ({
  convenienceStoreCount: Number(infraCount?.convenienceStoreCount) || 0,
  laundryCount: Number(infraCount?.laundryCount) || 0,
  cafeCount: Number(infraCount?.cafeCount) || 0,
  hospitalCount: Number(infraCount?.hospitalCount) || 0,
  pharmacyCount: Number(infraCount?.pharmacyCount) || 0,
  busStopCount: Number(infraCount?.busStopCount) || 0,
});

const toMinDist = (minDist: CompareApiHouse['minDist']): MinDist => ({
  convenienceDist: Number(minDist?.convenienceDist) || 0,
  laundryDist: Number(minDist?.laundryDist) || 0,
  cafeDist: Number(minDist?.cafeDist) || 0,
  hospitalDist: Number(minDist?.hospitalDist) || 0,
  pharmacyDist: Number(minDist?.pharmacyDist) || 0,
  subwayDist: Number(minDist?.subwayDist) || 0,
});

const toDongStats = (dongStats: CompareApiHouse['dongStats']): DongStats => ({
  cctvCount: Number(dongStats?.cctvCount) || 0,
  streetLightCount: Number(dongStats?.streetLightCount) || 0,
  safetyFacilityCount: Number(dongStats?.safetyFacilityCount) || 0,
  safetyScore: Number(dongStats?.safetyScore) || 0,
  avgMeatPrice: Number(dongStats?.avgMeatPrice) || 0,
  avgMealPrice: Number(dongStats?.avgMealPrice) || 0,
});

const normalizeCompareHouse = (house: CompareApiHouse): ComparisonHouse => {
  const scoreSummary = house.scoreSummary ?? undefined;

  return {
    images: Array.isArray(house.images)
      ? house.images
          .filter((image) => image && typeof image === 'object')
          .map((image) => ({
            image_url: image.image_url ?? null,
            is_thumbnail: image.is_thumbnail ?? null,
          }))
      : [],
    houseId: Number(house.houseId) || 0,
    dong: house.dong ?? '',
    houseType: house.houseType ?? '-',
    rentType: house.rentType ?? '-',
    houseStatus: house.houseStatus ?? '-',
    deposit: Number(house.deposit) || 0,
    monthlyCost: Number(house.monthlyCost) || 0,
    managementCost: house.managementCost ?? null,
    managementItems: house.managementItems ?? null,
    floorSize: Number(house.floorSize) || 0,
    buildYear: house.buildYear ?? null,
    description: house.description ?? '',
    viewCount: Number(house.viewCount) || 0,
    floor: house.floor ?? '-',
    commuteData: toCommuteData(house.commuteData),
    infraCount: toInfraCount(house.infraCount),
    minDist: toMinDist(house.minDist),
    dongStats: toDongStats(house.dongStats),
    recommendReasons: ['AI 추천 이유를 생성하는 중입니다.'],
    nonRecommendReasons: ['AI 주의사항을 생성하는 중입니다.'],
    scoreSummary: {
      trafficScore: Number(scoreSummary?.trafficScore) || 0,
      infraScore: Number(scoreSummary?.infraScore) || 0,
      safetyScore:
        Number(scoreSummary?.safetyScore ?? scoreSummary?.saferyScore) || 0,
      needFitScore: Number(scoreSummary?.needFitScore) || 0,
    },
  };
};

const pickComparisonArray = (json: unknown): CompareApiHouse[] => {
  if (typeof json !== 'object' || json === null) {
    return [];
  }

  const payload = json as CompareApiResponse;

  if (Array.isArray(payload.comparisonData)) {
    return payload.comparisonData;
  }

  if (Array.isArray(payload?.data?.comparisonData)) {
    return payload.data.comparisonData;
  }

  if (Array.isArray(payload?.data?.data?.comparisonData)) {
    return payload.data.data.comparisonData;
  }

  return [];
};

export default function ComparePage() {
  const SECTION_HEADER_BG = 'bg-background-200';
  const HEADER_HEIGHT = 0;

  const [selectedHouseId1, setSelectedHouseId1] = useState<number | ''>('');
  const [selectedHouseId2, setSelectedHouseId2] = useState<number | ''>('');
  const [isCompared, setIsCompared] = useState(false);
  const [showStickySummary, setShowStickySummary] = useState(false);
  const [houseOptions, setHouseOptions] = useState<ComparisonHouse[]>([]);
  const [loadingHouseOptions, setLoadingHouseOptions] = useState(true);
  const [isCompareLoading, setIsCompareLoading] = useState(false);
  const [isAiLoading, setIsAiLoading] = useState(false);
  const [compareError, setCompareError] = useState<string | null>(null);
  const [overallEvaluation, setOverallEvaluation] = useState('');

  const compareTopRef = useRef<HTMLDivElement | null>(null);

  const selectedHouse1 = useMemo(
    () =>
      houseOptions.find((house) => house.houseId === selectedHouseId1) ??
      null,
    [houseOptions, selectedHouseId1],
  );

  const selectedHouse2 = useMemo(
    () =>
      houseOptions.find((house) => house.houseId === selectedHouseId2) ??
      null,
    [houseOptions, selectedHouseId2],
  );

  const compareHouses = useMemo(() => {
    if (!selectedHouse1 || !selectedHouse2) return [];
    return [selectedHouse1, selectedHouse2];
  }, [selectedHouse1, selectedHouse2]);

  const canCompare =
    selectedHouse1 &&
    selectedHouse2 &&
    selectedHouse1.houseId !== selectedHouse2.houseId;

  useEffect(() => {
    const loadWishHouses = async () => {
      try {
        setLoadingHouseOptions(true);
        const res = await fetchWithAuth('/api/houses/wish', { method: 'GET' });

        if (!res.ok) {
          throw new Error('찜매물 목록 조회 실패');
        }

        const json = await res.json();
        const list = pickWishlistArray(json);
        setHouseOptions(list.map(mapWishlistItemToComparisonHouse));
      } catch (error) {
        
        setHouseOptions([]);
      } finally {
        setLoadingHouseOptions(false);
      }
    };

    loadWishHouses();
  }, []);

  useEffect(() => {
    const hasHouseId = (houseId: number | '') =>
      houseId === '' || houseOptions.some((house) => house.houseId === houseId);

    if (!hasHouseId(selectedHouseId1)) {
      setSelectedHouseId1('');
      setIsCompared(false);
    }

    if (!hasHouseId(selectedHouseId2)) {
      setSelectedHouseId2('');
      setIsCompared(false);
    }
  }, [houseOptions, selectedHouseId1, selectedHouseId2]);

  const mergeComparedHouses = (comparedHouses: ComparisonHouse[]) => {
    const comparedMap = new Map(
      comparedHouses.map((house) => [house.houseId, house]),
    );

    setHouseOptions((prev) =>
      prev.map((house) => comparedMap.get(house.houseId) ?? house),
    );
  };

  const requestAiEvaluation = async (comparedHouses: ComparisonHouse[]) => {
    try {
      setIsAiLoading(true);

      const comparisonData = comparedHouses.map((house) => ({
        houseId: house.houseId,
        dong: house.dong,
        houseType: house.houseType,
        rentType: house.rentType,
        deposit: house.deposit,
        monthlyCost: house.monthlyCost,
        floorSize: house.floorSize,
        floor: house.floor,
        description: house.description,
        commuteData: house.commuteData,
        infraCount: house.infraCount,
        minDist: house.minDist,
        dongStats: house.dongStats,
        scoreSummary: house.scoreSummary,
      }));

      const res = await fetchWithAuth('/api/houses/compare/ai', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ comparisonData }),
      });

      if (!res.ok) {
        throw new Error('AI 비교 평가 조회 실패');
      }

      const json = (await res.json()) as AiCompareResponse;
      const aiData = json.data;
      const aiHouses = Array.isArray(aiData?.houses) ? aiData.houses : [];
      const aiByHouseId = new Map(
        aiHouses
          .filter(
            (house): house is typeof house & { houseId: number } =>
              typeof house.houseId === 'number',
          )
          .map((house) => [house.houseId, house]),
      );
      const aiByLabel = new Map(
        aiHouses
          .filter(
            (house): house is typeof house & { houseLabel: string } =>
              typeof house.houseLabel === 'string' && house.houseLabel.length > 0,
          )
          .map((house) => [house.houseLabel, house]),
      );
      const comparedIndexByHouseId = new Map(
        comparedHouses.map((house, index) => [house.houseId, index]),
      );

      setOverallEvaluation(aiData?.overallEvaluation ?? '');

      setHouseOptions((prev) =>
        prev.map((house) => {
          const compareIndex = comparedIndexByHouseId.get(house.houseId);
          const aiHouse =
            aiByHouseId.get(house.houseId) ??
            (compareIndex != null
              ? aiByLabel.get(`매물 ${compareIndex + 1}`) ?? aiHouses[compareIndex]
              : undefined);

          if (!aiHouse) {
            return house;
          }

          return {
            ...house,
            recommendReasons: Array.isArray(aiHouse.recommendationReasons)
              ? aiHouse.recommendationReasons
              : [],
            nonRecommendReasons: Array.isArray(aiHouse.cautionReasons)
              ? aiHouse.cautionReasons
              : [],
          };
        }),
      );
    } catch (error) {
      
    } finally {
      setIsAiLoading(false);
    }
  };

  const handleCompare = async () => {
    if (!canCompare || isCompareLoading) return;

    try {
      setIsCompareLoading(true);
      setCompareError(null);
      setOverallEvaluation('');

      const ids = [selectedHouse1.houseId, selectedHouse2.houseId].join(',');
      const res = await fetchWithAuth(`/api/houses/compare?ids=${ids}`, {
        method: 'GET',
      });

      if (!res.ok) {
        const errorJson = await res.json().catch(() => null);
        const message =
          errorJson?.message ??
          (res.status === 404
            ? '존재하지 않는 매물이 포함되어 있습니다.'
            : '매물 비교 데이터 조회에 실패했습니다.');
        throw new Error(message);
      }

      const json = await res.json();
      

      const rawComparisonData = pickComparisonArray(json);
      

      const comparedHouses = rawComparisonData.map(normalizeCompareHouse);
      

      if (comparedHouses.length < 2) {
        throw new Error('비교 가능한 매물 데이터가 충분하지 않습니다.');
      }

      mergeComparedHouses(comparedHouses);
      setIsCompared(true);
      void requestAiEvaluation(comparedHouses);
    } catch (error) {
      setIsCompared(false);
      setCompareError(
        error instanceof Error
          ? error.message
          : '매물 비교 데이터 조회에 실패했습니다.',
      );
    } finally {
      setIsCompareLoading(false);
    }
  };

  const handleRemoveFirst = () => {
    setSelectedHouseId1('');
    setIsCompared(false);
    setCompareError(null);
    setOverallEvaluation('');
  };

  const handleRemoveSecond = () => {
    setSelectedHouseId2('');
    setIsCompared(false);
    setCompareError(null);
    setOverallEvaluation('');
  };

  useEffect(() => {
    const target = compareTopRef.current;

    if (!target || !isCompared) {
      setShowStickySummary(false);
      return;
    }

    const observer = new IntersectionObserver(
      ([entry]) => {
        setShowStickySummary(!entry.isIntersecting);
      },
      {
        root: null,
        threshold: 0,
        rootMargin: `-${HEADER_HEIGHT}px 0px 0px 0px`,
      },
    );

    observer.observe(target);

    return () => observer.disconnect();
  }, [isCompared]);

  return (
    <div className="min-h-screen bg-white">
      <main className="bg-white">
      {isCompared && compareHouses.length === 2 && (
        <div
          className={clsx(
            'fixed left-0 right-0 z-40 transition-all duration-300',
            showStickySummary
              ? 'pointer-events-auto translate-y-0 opacity-100'
              : 'pointer-events-none -translate-y-2 opacity-0',
          )}
          style={{ top: `${HEADER_HEIGHT}px` }}
        >
          <div className="mx-auto max-w-275 px-6">
            <StickyCompareSummary2 houses={compareHouses} />
          </div>
        </div>
      )}

      <div className="mx-auto mb-10 max-w-275 px-6 py-10">
        <div ref={compareTopRef}>
          <section className="grid grid-cols-1 gap-8 md:grid-cols-2">
            <CompareSelectCard
              slotTitle="매물 1"
              selectedHouse={selectedHouse1}
              houseOptions={houseOptions}
              selectedHouseId={selectedHouseId1}
              onSelect={(houseId) => {
                setSelectedHouseId1(houseId);
                setIsCompared(false);
                setCompareError(null);
                setOverallEvaluation('');
              }}
              onRemove={handleRemoveFirst}
            />

            <CompareSelectCard
              slotTitle="매물 2"
              selectedHouse={selectedHouse2}
              houseOptions={houseOptions}
              selectedHouseId={selectedHouseId2}
              onSelect={(houseId) => {
                setSelectedHouseId2(houseId);
                setIsCompared(false);
                setCompareError(null);
                setOverallEvaluation('');
              }}
              onRemove={handleRemoveSecond}
            />
          </section>

          <div className="mt-8 flex justify-center">
            <Button
              label={isCompareLoading ? '비교 데이터 불러오는 중...' : '비교하기'}
              onClick={handleCompare}
              disabled={!canCompare || isCompareLoading}
              isActive={!!canCompare}
              size="sm"
              leftIcon={
                isCompareLoading ? (
                  <span className="h-4 w-4 animate-spin rounded-full border-2 border-gray-300 border-t-white" />
                ) : undefined
              }
              className="px-8"
            />
          </div>

          {compareError && (
            <p className="mt-3 text-center text-sm text-red-500">{compareError}</p>
          )}
        </div>

        {!isCompared && (
          <div className="mt-10 rounded-2xl border border-dashed border-gray-300 bg-white py-14 text-center text-gray-500">
            {loadingHouseOptions
              ? '찜매물을 불러오는 중입니다.'
              : houseOptions.length === 0
                ? '찜한 매물이 없습니다. 찜한 매물을 추가한 뒤 비교해 주세요.'
                : '비교할 매물 2개를 선택한 뒤 비교하기 버튼을 눌러주세요.'}
          </div>
        )}

        {isCompared && compareHouses.length === 2 && (
          <div className={clsx('mt-10 space-y-6', showStickySummary && 'pt-28')}>
            <CompareSection
              title="최종 평가"
              headerBgColor={SECTION_HEADER_BG}
            >
              <FinalEvaluationSection
                houses={compareHouses}
                overallEvaluation={overallEvaluation}
                aiLoading={isAiLoading}
              />
            </CompareSection>

            <CompareSection
              title="기본 정보"
              headerBgColor={SECTION_HEADER_BG}
            >
              <CompareTable
                houses={compareHouses}
                rows={[
                  { label: '주소', values: compareHouses.map((h) => h.dong) },
                  {
                    label: '주거유형',
                    values: compareHouses.map((h) => h.houseType),
                  },
                  {
                    label: '계약형태',
                    values: compareHouses.map((h) => h.rentType),
                  },
                  {
                    label: '전용 면적',
                    values: compareHouses.map((h) => `${h.floorSize}㎡`),
                  },
                  {
                    label: '층수',
                    values: compareHouses.map((h) => h.floor),
                  },
                  {
                    label: '건축년도',
                    values: compareHouses.map((h) =>
                      h.buildYear ? `${h.buildYear}년` : '-',
                    ),
                  },
                  {
                    label: '매물 상태',
                    values: compareHouses.map((h) => h.houseStatus),
                  },
                  {
                    label: '조회수',
                    values: compareHouses.map((h) => `${h.viewCount}`),
                  },
                ]}
              />
            </CompareSection>

            <CompareSection
              title="가격 정보"
              headerBgColor={SECTION_HEADER_BG}
            >
              <CompareTable
                houses={compareHouses}
                rows={[
                  {
                    label: '보증금',
                    values: compareHouses.map(
                      (h) => `${formatPrice(h.deposit)}만원`,
                    ),
                  },
                  {
                    label: '월세',
                    values: compareHouses.map(
                      (h) => `${formatPrice(h.monthlyCost)}만원`,
                    ),
                  },
                  {
                    label: '관리비',
                    values: compareHouses.map((h) =>
                      h.managementCost != null ? `${h.managementCost}만원` : '-',
                    ),
                  },
                  {
                    label: '관리비 포함 항목',
                    values: compareHouses.map((h) => h.managementItems || '-'),
                  },
                ]}
              />
            </CompareSection>

            <CompareSection
              title="출퇴근 정보"
              headerBgColor={SECTION_HEADER_BG}
            >
              <CompareTable
                houses={compareHouses}
                rows={[
                  {
                    label: '예상 출퇴근 시간',
                    values: compareHouses.map(
                      (h) => `${h.commuteData.commuteTime}분`,
                    ),
                  },
                  {
                    label: '이동 거리',
                    values: compareHouses.map(
                      (h) => `${h.commuteData.commuteDistance}km`,
                    ),
                  },
                ]}
              />
            </CompareSection>

            <CompareSection
              title="생활 인프라(800m)"
              headerBgColor={SECTION_HEADER_BG}
            >
              <CompareTable
                houses={compareHouses}
                rows={[
                  {
                    label: '편의점',
                    values: compareHouses.map(
                      (h) => `${h.infraCount.convenienceStoreCount}개`,
                    ),
                  },
                  {
                    label: '세탁소',
                    values: compareHouses.map(
                      (h) => `${h.infraCount.laundryCount}개`,
                    ),
                  },
                  {
                    label: '병원',
                    values: compareHouses.map(
                      (h) => `${h.infraCount.hospitalCount}개`,
                    ),
                  },
                  {
                    label: '카페',
                    values: compareHouses.map(
                      (h) => `${h.infraCount.cafeCount}개`,
                    ),
                  },
                  {
                    label: '약국',
                    values: compareHouses.map(
                      (h) => `${h.infraCount.pharmacyCount}개`,
                    ),
                  },
                  {
                    label: '버스 정류장',
                    values: compareHouses.map(
                      (h) => `${h.infraCount.busStopCount}개`,
                    ),
                  },
                ]}
              />
            </CompareSection>

            <CompareSection
              title="주요 시설 최단 거리"
              headerBgColor={SECTION_HEADER_BG}
            >
              <FacilityDistanceChart houses={compareHouses} />
            </CompareSection>

            <CompareSection title="안전도" headerBgColor={SECTION_HEADER_BG}>
              <CompareTable
                houses={compareHouses}
                rows={[
                  {
                    label: '치안시설',
                    values: compareHouses.map(
                      (h) => `${h.dongStats.safetyFacilityCount}개`,
                    ),
                  },
                  {
                    label: '가로등',
                    values: compareHouses.map(
                      (h) => `${h.dongStats.streetLightCount}개`,
                    ),
                  },
                  {
                    label: 'CCTV',
                    values: compareHouses.map(
                      (h) => `${h.dongStats.cctvCount}개`,
                    ),
                  },
                  {
                    label: '안전종합점수',
                    values: compareHouses.map(
                      (h) => `${h.dongStats.safetyScore}점`,
                    ),
                  },
                ]}
              />
            </CompareSection>

            <CompareSection
              title="지역 생활"
              headerBgColor={SECTION_HEADER_BG}
            >
              <CompareTable
                houses={compareHouses}
                rows={[
                  {
                    label: '평균 고기 식비',
                    values: compareHouses.map(
                      (h) => `${formatPrice(h.dongStats.avgMeatPrice)}원`,
                    ),
                  },
                  {
                    label: '평균 일반 식비',
                    values: compareHouses.map(
                      (h) => `${formatPrice(h.dongStats.avgMealPrice)}원`,
                    ),
                  },
                ]}
              />
            </CompareSection>

          </div>
        )}
      </div>
      </main>
      <Footer />
    </div>
  );
}