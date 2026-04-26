'use client';

import { Suspense, useEffect, useMemo, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import RecommendLayout from '@/components/features/recommend/step/RecommendLayout';
import Step1Location from '@/components/features/recommend/step/Step1Location';
import Step2Housing from '@/components/features/recommend/step/Step2Housing';
import Step3Lifestyle from '@/components/features/recommend/step/Step3Lifestyle';
import Step4Result from '@/components/features/recommend/step/Step4Result';
import RegionStatsModal from '@/components/features/recommend/RegionStatsModal';
import { seoulDistrictNames } from '@/components/features/recommend/step/SeoulDistrictMap';
import { fetchWithAuth } from '@/lib/fetchWithAuth';
import type { RegionTopRecommendationItem } from '@/types/api';

type Step = 1 | 2 | 3 | 4;

type HousingType =
  | '단독/다가구(원룸)'
  | '연립/다세대(빌라)'
  | '오피스텔'
  | '아파트';

type ContractType = '전세' | '월세';

type LifestyleOption =
  | '카페'
  | '편의시설'
  | '공원 많은 곳'
  | '치안 우수'
  | '역세권'
  | '식당';

type RangeValue = {
  minValue: number;
  maxValue: number;
};

type Option = {
  label: string;
  value: string;
};

type MetricTab = '편의시설' | '통근시간' | '평균가격' | '매물수' | '식비지수';

const LIFESTYLE_TAG_TO_BACKEND: Record<LifestyleOption, string> = {
  '카페': '카페/디저트 많음',
  '공원 많은 곳': '편의시설 많음',
  '역세권': '역세권 (800m)',
  '편의시설': '편의시설 많음',
  '치안 우수': '치안 우수',
  '식당': '식당 많음',
};

const cityOptions: Option[] = [
  { label: '서울특별시', value: 'seoul' },
];

const districtOptionMap: Record<string, Option[]> = {
  seoul: seoulDistrictNames.map((name) => ({
    label: name,
    value: name,
  })),
};

function RecommendPageContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const isCreateMode = searchParams.get('create') === '1';
  const [isCheckingEntry, setIsCheckingEntry] = useState(true);
  const [isSubmittingCondition, setIsSubmittingCondition] = useState(false);
  const [topRegionRecommendations, setTopRegionRecommendations] = useState<RegionTopRecommendationItem[]>([]);
  const [isLoadingRegionTop, setIsLoadingRegionTop] = useState(false);
  const [userNeedIdForRegion, setUserNeedIdForRegion] = useState<number | null>(null);
  const [currentStep, setCurrentStep] = useState<Step>(1);
  const [loadingProgress, setLoadingProgress] = useState<'analyzing' | 'checking' | 'composing' | 'waiting' | null>(null);

  const [needName, setNeedName] = useState('');
  const [placeKeyword, setPlaceKeyword] = useState('');
  const [commuteHour, setCommuteHour] = useState('');
  const [commuteMinute, setCommuteMinute] = useState('');
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');

  const [housingTypes, setHousingTypes] = useState<HousingType[]>([]);
  const [contractType, setContractType] = useState<ContractType>('월세');
  const [selectedFloors, setSelectedFloors] = useState<string[]>(['전체']);
  const [minSize, setMinSize] = useState('');

  const [monthlyRentRange, setMonthlyRentRange] = useState<RangeValue>({
    minValue: 0,
    maxValue: 200,
  });

  const [depositRange, setDepositRange] = useState<RangeValue>({
    minValue: 0,
    maxValue: 50000,
  });

  const [city, setCity] = useState('seoul');
  const [district, setDistrict] = useState('');
  const [lifestylePreferences, setLifestylePreferences] = useState<
    LifestyleOption[]
  >([]);

  const [selectedMetric, setSelectedMetric] = useState<MetricTab | null>(null);

  const [isRegionStatsModalOpen, setIsRegionStatsModalOpen] = useState(false);

  const cityLabel =
    cityOptions.find((option) => option.value === city)?.label ?? city;

  useEffect(() => {
    const redirectBySavedConditions = async () => {
      if (isCreateMode) {
        setIsCheckingEntry(false);
        return;
      }

      try {
        const response = await fetchWithAuth('/api/recommend/conditions');
        const data = await response.json().catch(() => null);
        const conditions = data?.data?.conditions;

        if (Array.isArray(conditions) && conditions.length > 0) {
          router.replace('/recommend/map');
          return;
        }
      } catch (error) {
        
      }

      setIsCheckingEntry(false);
    };

    redirectBySavedConditions();
  }, [isCreateMode, router]);

  const metricValueSelector = (stat: RegionTopRecommendationItem, metric: MetricTab): number | string => {
    
    const hasNoMatches = !stat.matchingHouseCount || stat.matchingHouseCount === 0;
    
    switch (metric) {
      case '편의시설':
        if (hasNoMatches) return '-';
        return (stat.facilityCount?.totalCount ?? 0) === 0 ? '-' : stat.facilityCount?.totalCount ?? '-';
      case '통근시간':
        if (hasNoMatches) return '-';
        return (stat.commute?.avgCommuteMinutes ?? 0) === 0 ? '-' : stat.commute?.avgCommuteMinutes ?? '-';
      case '평균가격':
        if (hasNoMatches) return '-';
        const monthlyRent = stat.housingCost?.avgMonthlyRent ?? 0;
        const deposit = stat.housingCost?.avgDeposit ?? 0;
        
        const price = monthlyRent !== 0 && monthlyRent !== null && monthlyRent !== undefined ? monthlyRent : deposit;
        return price === 0 ? '-' : price;
      case '매물수':
        const count = stat.matchingHouseCount ?? 0;
        return count === 0 ? '-' : count;
      case '식비지수':
        
        return (stat.livingCost?.foodCostIndex ?? 0) === 0 ? '-' : stat.livingCost?.foodCostIndex ?? '-';
      default:
        return '-';
    }
  };

  const hasValidMatchingHouses = (stat: RegionTopRecommendationItem) => {
    return stat.matchingHouseCount && stat.matchingHouseCount > 0;
  };

  const top3RegionDefaults = [...topRegionRecommendations]
    .sort((left, right) => left.rank - right.rank)
    .filter((item) => item.rank >= 1 && item.rank <= 3)
    .map((item) => ({ rank: item.rank as 1 | 2 | 3, label: item.sigunguName }));

  const districtMetrics = useMemo(() => {
    return seoulDistrictNames.map((name) => {
      const stat = topRegionRecommendations.find((item) => item.sigunguName === name);
      const defaultRank = !selectedMetric
        ? (top3RegionDefaults.find((item) => item.label === name)?.rank as 1 | 2 | 3 | undefined)
        : undefined;

      return {
        district: name,
        value: stat && selectedMetric ? metricValueSelector(stat, selectedMetric) : (selectedMetric ? '-' : '-'),
        rank: defaultRank as 1 | 2 | 3 | undefined,
        hasMatchingHouses: stat ? hasValidMatchingHouses(stat) : false,
        stat,
      };
    });
  }, [topRegionRecommendations, selectedMetric, top3RegionDefaults]);

  const rankedMetrics = useMemo(() => {
    if (!selectedMetric) {
      
      return districtMetrics;
    }

    
    
    const validDistricts = districtMetrics.filter((item) => item.hasMatchingHouses);
    
    if (validDistricts.length === 0) {
      
      return districtMetrics.map((item) => ({
        ...item,
        rank: undefined,
      }));
    }

    const order =
      selectedMetric === '통근시간' ||
        selectedMetric === '평균가격' ||
        selectedMetric === '식비지수'
        ? 'asc'
        : 'desc';

    const calc = (v: number | string) => {
      if (v === '-') return Infinity; 
      const n = Number(v);
      if (Number.isNaN(n)) return Infinity;
      return n;
    };

    
    const sortedByMetric = [...validDistricts].sort((a, b) =>
      order === 'asc' ? calc(a.value) - calc(b.value) : calc(b.value) - calc(a.value),
    );

    
    const metricRanks = new Map<string, 1 | 2 | 3 | undefined>();
    sortedByMetric.forEach((item, index) => {
      metricRanks.set(item.district, index < 3 ? (index + 1) as 1 | 2 | 3 : undefined);
    });

    
    return districtMetrics.map((item) => ({
      ...item,
      rank: metricRanks.get(item.district),
    }));
  }, [districtMetrics, selectedMetric]);

  const [selectedRegionStats, setSelectedRegionStats] =
    useState<RegionTopRecommendationItem | null>(null);

  const districtOptions = city
    ? [...(districtOptionMap[city] ?? [])].sort((left, right) =>
      left.label.localeCompare(right.label, 'ko-KR'),
    )
    : [];

  const maxCommuteTime =
    Number(commuteHour || 0) * 60 + Number(commuteMinute || 0);
  const parsedLat = Number(latitude);
  const parsedLng = Number(longitude);
  const parsedMinSize = Number(minSize);

  const isStep3SubmitEnabled =
    placeKeyword.trim().length > 0 &&
    Number.isFinite(parsedLat) &&
    Number.isFinite(parsedLng) &&
    maxCommuteTime > 0 &&
    Number.isFinite(parsedMinSize) &&
    parsedMinSize > 0 &&
    selectedFloors.length > 0 &&
    lifestylePreferences.length > 0;

  
  useEffect(() => {
    if (!isLoadingRegionTop) {
      setLoadingProgress(null);
      return;
    }

    setLoadingProgress('analyzing');

    const timer1 = setTimeout(() => setLoadingProgress('checking'), 3000);
    const timer2 = setTimeout(() => setLoadingProgress('composing'), 8000);
    const timer3 = setTimeout(() => setLoadingProgress('waiting'), 15000);

    return () => {
      clearTimeout(timer1);
      clearTimeout(timer2);
      clearTimeout(timer3);
    };
  }, [isLoadingRegionTop]);

  
  const validateStep1 = (): boolean => {
    if (!placeKeyword.trim()) {
      alert('주요 활동 장소를 선택해주세요.');
      return false;
    }
    if (!Number.isFinite(parsedLat) || !Number.isFinite(parsedLng)) {
      alert('위치를 정확히 선택해주세요.');
      return false;
    }
    if (maxCommuteTime <= 0) {
      alert('통근시간을 입력해주세요.');
      return false;
    }
    return true;
  };

  const validateStep2 = (): boolean => {
    if (housingTypes.length === 0) {
      alert('주택 유형을 1개 이상 선택해주세요.');
      return false;
    }
    if (!contractType) {
      alert('계약 유형을 선택해주세요.');
      return false;
    }
    if (!minSize || parsedMinSize <= 0) {
      alert('최소 면적을 입력해주세요.');
      return false;
    }
    return true;
  };

  const validateStep3 = (): boolean => {
    if (selectedFloors.length === 0) {
      alert('층을 선택해주세요.');
      return false;
    }
    if (lifestylePreferences.length === 0) {
      alert('라이프스타일 선호도를 선택해주세요.');
      return false;
    }
    return true;
  };

  const buildConditionPayload = (sigunguNameOverride?: string) => {
    const resolvedSigunguName = sigunguNameOverride || district;
    const districtLabel = resolvedSigunguName
      ? districtOptions.find((option) => option.value === resolvedSigunguName)
        ?.label ?? resolvedSigunguName
      : '';

    const normalizedFloors =
      selectedFloors.includes('전체')
        ? ['반지하', '1층', '지상층']
        : selectedFloors;

    const normalizedNeedName =
      needName.trim() ||
      `${cityLabel} ${districtLabel}`.trim() ||
      placeKeyword.trim();

    const normalizedTargetAddress =
      placeKeyword.trim() || `${cityLabel} ${districtLabel}`.trim() || cityLabel;

    const mappedLifestyleTags = lifestylePreferences
      .map((tag) => LIFESTYLE_TAG_TO_BACKEND[tag])
      .filter((tag): tag is string => Boolean(tag));

    return {
      needName: normalizedNeedName,
      targetAddress: normalizedTargetAddress,
      lat: parsedLat,
      lng: parsedLng,
      ...(cityLabel ? { sidoName: cityLabel } : {}),
      ...(districtLabel ? { sigunguName: districtLabel } : {}),
      maxCommuteTime,
      rentType: contractType,
      maxDeposit: depositRange.maxValue === 50000 ? 210000000 : depositRange.maxValue,
      maxMonthlyRent: monthlyRentRange.maxValue === 200 ? 210000000 : monthlyRentRange.maxValue,
      minDeposit: depositRange.minValue,
      minMonthlyRent: monthlyRentRange.minValue,
      housingTypes,
      minSize: parsedMinSize,
      lifestyleTags: mappedLifestyleTags,
      floors: normalizedFloors,
    };
  };

  const createConditionAndGetId = async (sigunguNameOverride?: string) => {
    const payload = buildConditionPayload(sigunguNameOverride);

    
    

    const response = await fetchWithAuth('/api/recommend/conditions', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    const data = await response.json().catch(() => null);
    
    

    if (!response.ok) {
      throw new Error(data?.message || '조건 생성 실패');
    }

    const createdConditionId = Number(data?.data?.id ?? data?.id);
    
    return Number.isNaN(createdConditionId) || createdConditionId <= 0
      ? null
      : createdConditionId;
  };

  const handleNext = async () => {
    
    if (currentStep === 1) {
      if (!validateStep1()) {
        return;
      }
    } else if (currentStep === 2) {
      if (!validateStep2()) {
        return;
      }
    } else if (currentStep === 3) {
      if (!validateStep3()) {
        return;
      }
    }

    if (currentStep === 3 && city && district) {
      try {
        setIsSubmittingCondition(true);
        const createdConditionId = await createConditionAndGetId();

        if (createdConditionId !== null && !Number.isNaN(createdConditionId) && createdConditionId > 0) {
          
          router.push(`/recommend/map?conditionId=${createdConditionId}`);
          return;
        }

        alert('조건 생성에 실패했습니다. 잠시 후 다시 시도해주세요.');
        return;
      } catch (error) {
        
        alert('조건 생성에 실패했습니다. 잠시 후 다시 시도해주세요.');
      } finally {
        setIsSubmittingCondition(false);
      }
    } else if (currentStep === 3) {
      
      try {
        setIsLoadingRegionTop(true);
        const regionMinMonthlyRent = contractType === '전세' ? 0 : monthlyRentRange.minValue;
        const regionMaxMonthlyRent = contractType === '전세' ? 0 : monthlyRentRange.maxValue;
        const params = new URLSearchParams({
          sidoName: cityLabel,
          rentType: contractType,
          minDeposit: String(depositRange.minValue),
          maxDeposit: String(depositRange.maxValue === 50000 ? 210000000 : depositRange.maxValue),
          minMonthlyRent: String(regionMinMonthlyRent),
          maxMonthlyRent: String(regionMaxMonthlyRent === 200 ? 210000000 : regionMaxMonthlyRent),
          minFloorSize: String(parsedMinSize > 0 ? parsedMinSize : 10),
          destinationLatitude: String(parsedLat),
          destinationLongitude: String(parsedLng),
          ...(userNeedIdForRegion != null ? { userNeedId: String(userNeedIdForRegion) } : {}),
        });

        housingTypes.forEach((type) => params.append('houseType', type));

        const regionTopUrl = `/api/recommend/regions/top?${params.toString()}`;
        
        

        const response = await fetchWithAuth(regionTopUrl);
        const data = await response.json().catch(() => null);
        
        

        const recommendations = Array.isArray(data?.data?.recommendations)
          ? data.data.recommendations
          : Array.isArray(data?.recommendations)
            ? data.recommendations
            : [];

        const rawUserNeedId = Number(data?.data?.userNeedId ?? data?.userNeedId);
        if (!Number.isNaN(rawUserNeedId) && rawUserNeedId > 0) {
          setUserNeedIdForRegion(rawUserNeedId);
        }

        if (response.ok && recommendations.length > 0) {
          setTopRegionRecommendations(recommendations);
        } else {
          setTopRegionRecommendations([]);
        }
      } catch (error) {
        
        setTopRegionRecommendations([]);
      } finally {
        setIsLoadingRegionTop(false);
      }
      setCurrentStep(4);
    } else if (currentStep < 4) {
      setCurrentStep((prev) => (prev + 1) as Step);
    }
  };

  const handlePrev = () => {
    if (currentStep > 1) {
      setCurrentStep((prev) => (prev - 1) as Step);
    }
  };

  const handleChangeCity = (value: string) => {
    setCity(value);
    setDistrict('');
  };

  const handleToggleLifestylePreference = (value: LifestyleOption) => {
    setLifestylePreferences((prev) =>
      prev.includes(value)
        ? prev.filter((item) => item !== value)
        : [...prev, value],
    );
  };

  const handleToggleFloor = (value: string) => {
    const floorDetailOptions = ['반지하', '1층', '지상층'];

    setSelectedFloors((prev) =>
      value === '전체'
        ? ['전체']
        : (() => {
            const prevWithoutAll = prev.filter((item) => item !== '전체');
            const next = prevWithoutAll.includes(value)
              ? prevWithoutAll.filter((item) => item !== value)
              : [...prevWithoutAll, value];

            if (next.length === floorDetailOptions.length) {
              return ['전체'];
            }

            return next;
          })(),
    );
  };

  const handleToggleHousingType = (value: HousingType) => {
    setHousingTypes((prev) =>
      prev.includes(value)
        ? prev.filter((item) => item !== value)
        : [...prev, value],
    );
  };

  const handleClickRegion = (sigunguName: string) => {
    const foundRegion = topRegionRecommendations.find(
      (item) => item.sigunguName === sigunguName,
    );

    if (!foundRegion) return;

    setSelectedRegionStats(foundRegion);
    setIsRegionStatsModalOpen(true);
  };

  const handleCloseRegionStatsModal = () => {
    setIsRegionStatsModalOpen(false);
    setSelectedRegionStats(null);
  };

  const handleClickViewListings = (region: RegionTopRecommendationItem) => {
    const submitAndNavigate = async () => {
      try {
        setIsSubmittingCondition(true);
        

        const createdConditionId = await createConditionAndGetId(region.sigunguName);

        if (createdConditionId) {
          router.push(`/recommend/map?conditionId=${createdConditionId}`);
          return;
        }

        alert('조건 생성에 실패했습니다. 잠시 후 다시 시도해주세요.');
        return;
      } catch (error) {
        
        alert('조건 생성에 실패했습니다. 잠시 후 다시 시도해주세요.');
      } finally {
        setIsSubmittingCondition(false);
      }
    };

    void submitAndNavigate();
  };

  if (isCheckingEntry) {
    return null;
  }

  return (
    <>
      <RecommendLayout
        currentStep={currentStep}
        onNext={handleNext}
        onPrev={handlePrev}
        nextLabel={
          currentStep === 3
            ? city && district
              ? isSubmittingCondition
                ? '조건 저장 중...'
                : '매물 보러가기'
              : '추천받기'
            : '다음 단계로'
        }
        nextLoading={currentStep === 3 && !city && !district && isLoadingRegionTop}
        nextDisabled={
          isSubmittingCondition ||
          isLoadingRegionTop ||
          (currentStep === 1 &&
            (!placeKeyword.trim() ||
              !Number.isFinite(parsedLat) ||
              !Number.isFinite(parsedLng) ||
              maxCommuteTime <= 0)) ||
          (currentStep === 2 &&
            (housingTypes.length === 0 ||
              !minSize ||
              parsedMinSize <= 0)) ||
          (currentStep === 3 &&
            (selectedFloors.length === 0 ||
              lifestylePreferences.length === 0 ||
              !isStep3SubmitEnabled))
        }
        showPrevButton={currentStep > 1}
      >
        {currentStep === 1 && (
          <Step1Location
            needName={needName}
            placeKeyword={placeKeyword}
            commuteHour={commuteHour}
            commuteMinute={commuteMinute}
            onChangeNeedName={setNeedName}
            onChangePlaceKeyword={setPlaceKeyword}
            onChangeCommuteHour={setCommuteHour}
            onChangeCommuteMinute={setCommuteMinute}
            onSelectLocation={(lat, lng, _placeName, address) => {
              
              setLatitude(String(lat));
              setLongitude(String(lng));
              if (address) {
                setPlaceKeyword(address);
              }
            }}
          />
        )}

        {currentStep === 2 && (
          <Step2Housing
            housingTypes={housingTypes}
            contractType={contractType}
            selectedFloors={selectedFloors}
            minSize={minSize}
            monthlyRentRange={monthlyRentRange}
            depositRange={depositRange}
            onToggleHousingType={handleToggleHousingType}
            onChangeContractType={setContractType}
            onToggleFloor={handleToggleFloor}
            onChangeMinSize={setMinSize}
            onChangeMonthlyRentRange={setMonthlyRentRange}
            onChangeDepositRange={setDepositRange}
          />
        )}

        {currentStep === 3 && (
          <Step3Lifestyle
            city={city}
            district={district}
            lifestylePreferences={lifestylePreferences}
            cityOptions={cityOptions}
            districtOptions={districtOptions}
            onChangeCity={handleChangeCity}
            onChangeDistrict={setDistrict}
            onToggleLifestylePreference={handleToggleLifestylePreference}
          />
        )}

        {currentStep === 4 && (
          <Step4Result
            selectedMetric={selectedMetric}
            onChangeMetric={setSelectedMetric}
            recommendedRegions={
              top3RegionDefaults.length > 0
                ? top3RegionDefaults
                : [
                    { rank: 1, label: '데이터 로딩 중...' },
                  ]
            }
            districtMetrics={rankedMetrics}
            onClickRegion={handleClickRegion}
            isLoadingRegionTop={isLoadingRegionTop}
            loadingProgress={loadingProgress}
          />
        )}
      </RecommendLayout>

      <RegionStatsModal
        isOpen={isRegionStatsModalOpen}
        region={selectedRegionStats}
        onClose={handleCloseRegionStatsModal}
        onClickViewListings={handleClickViewListings}
      />
    </>
  );
}

export default function RecommendPage() {
  return (
    <Suspense fallback={null}>
      <RecommendPageContent />
    </Suspense>
  );
}
