'use client';

import { useEffect, useMemo, useState } from 'react';
import Image from 'next/image';
import {
  HiOutlineArrowLeft,
  HiOutlineHome,
  HiOutlineCalendar,
  HiOutlineBuildingOffice2,
  HiOutlineBuildingStorefront,
  HiOutlineClock,
  HiOutlineSparkles,
} from 'react-icons/hi2';
import { LuRuler } from 'react-icons/lu';
import { MdLocalLaundryService, MdLocalPharmacy } from 'react-icons/md';
import { FaBowlRice, FaBusSimple, FaHospital, FaPersonWalking } from 'react-icons/fa6';
import { PiCoffee } from 'react-icons/pi';
import { GiMeat } from 'react-icons/gi';
import { fetchHouseDetail } from '@/lib/houseDetail';
import FavoriteButton from '@/components/common/FavoriteButton';
import HalfGauge from '@/components/common/HalfGauge';
import InfoTooltip from '@/components/common/InfoTooltip';

type Props = {
  houseId: number;
  listAiMessage?: string | null;
  onBack: () => void;
  onToggleWish: (houseId: number, liked: boolean) => Promise<void>;
};

type HouseDetail = {
  images: Array<{ image_url: string | null; is_thumbnail: boolean | null }>;
  houseId: number;
  dong: string;
  isLiked: boolean;
  houseType: string;
  rentType: string;
  houseStatus: string;
  deposit: number;
  monthlyCost: number;
  managementCost: number | null;
  managementItems: string | null;
  floorSize: number;
  buildYear: number | null;
  description: string | null;
  viewCount: number;
  floor: string;
  latitude: number;
  longitude: number;
  commuteData: { commuteTime: number; commuteDistance: number };
  infraCount: {
    convenienceStoreCount: number;
    laundryCount: number;
    cafeCount: number;
    hospitalCount: number;
    pharmacyCount: number;
    busStopCount: number;
  };
  minDist: {
    convenienceDist: number;
    laundryDist: number;
    cafeDist: number;
    hospitalDist: number;
    pharmacyDist: number;
    subwayDist: number;
  };
  dongStats: {
    cctvCount: number;
    streetLightCount: number;
    safetyFacilityCount: number;
    safetyScore: number;
    avgMeatPrice: number;
    avgMealPrice: number;
  };
  aiMessage?: string | null;
};

const DISTANCE_MAX = 800;
const DEFAULT_FLOOR = '지상층';
const DEFAULT_SAFETY_SCORE = 85;
const DEFAULT_CCTV_COUNT = 150;
const DEFAULT_STREET_LIGHT_COUNT = 150;
const DEFAULT_SAFETY_FACILITY_COUNT = 25;
const DEFAULT_AVG_MEAL_PRICE = 9500;
const DEFAULT_AVG_MEAT_PRICE = 19000;

const getThumbnail = (images: Array<{ image_url: string | null; is_thumbnail: boolean | null }>) => {
  const thumbnail = images.find((img) => img.is_thumbnail);

  return (
    thumbnail?.image_url ||
    images[0]?.image_url ||
    '/images/house-placeholder.png'
  );
};

const formatPrice = (value?: number | null) => {
  if (value === undefined || value === null) return '-';
  return `${value.toLocaleString()}만원`;
};

const formatArea = (value?: number | null) => {
  if (value === undefined || value === null) return '-';
  return `${value.toFixed(2)}m²`;
};

const formatDistance = (value?: number | null) => {
  if (value === undefined || value === null) return '800m 이상';
  if (value === 0) return '800m 이상';
  if (value > DISTANCE_MAX) return '800m 이상';
  if (value >= 1000) return `${(value / 1000).toFixed(2)}km`;
  return `${value.toLocaleString()}m`;
};

const isOutOfRange = (value?: number | null) =>
  value === undefined || value === null || value === 0 || value > DISTANCE_MAX;

const getDistanceBarPercent = (distance?: number | null) => {
  if (distance === undefined || distance === null) return 8;
  const clamped = Math.min(distance, DISTANCE_MAX);
  return Math.max(8, (clamped / DISTANCE_MAX) * 100);
};

const normalizeMarkdownText = (value: string) => {
  return value
    .replace(/\\+\*/g, '*')
    .replace(/[＊∗]/g, '*')
    .replace(/\u200B|\u200C|\u200D|\uFEFF/g, '')
    .replace(/[\u00A0\u3000]/g, ' ')
    .replace(/\*\s+\*/g, '**')
    .replace(/\*\*\s*([^*]+?)\s*\*\*/g, (_, text: string) => `**${text.trim()}**`)
    .trim();
};

const renderInlineStrong = (text: string) => {
  return text.split(/(\*\*[^*]+\*\*)/g).map((chunk, index) => {
    const matched = chunk.match(/^\*\*([^*]+)\*\*$/);

    if (!matched) {
      return <span key={`text-${index}`}>{chunk}</span>;
    }

    return (
      <strong key={`strong-${index}`} className="font-extrabold text-black">
        {matched[1]}
      </strong>
    );
  });
};

const renderReasonContent = (text: string) => {
  const lines = text.split('\n').filter((line) => line.trim().length > 0);

  return lines.map((line, lineIndex) => {
    const listMatch = line.match(/^[-*]\s+(.*)$/);

    if (listMatch) {
      return (
        <p key={`line-${lineIndex}`} className="leading-6">
          • {renderInlineStrong(listMatch[1])}
        </p>
      );
    }

    return (
      <p key={`line-${lineIndex}`} className="leading-6">
        {renderInlineStrong(line)}
      </p>
    );
  });
};

export default function RecommendHouseDetailPanel({
  houseId,
  listAiMessage,
  onBack,
  onToggleWish,
}: Props) {
  const [house, setHouse] = useState<HouseDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [imageError, setImageError] = useState(false);

  useEffect(() => {
    if (!houseId) {
      setHouse(null);
      setLoading(false);
      return;
    }

    setImageError(false);
    const fetchHouse = async () => {
      try {
        setLoading(true);
        const response = await fetchHouseDetail(houseId);
        setHouse({
          ...response.data,
          aiMessage: response.data?.aiMessage ?? listAiMessage ?? null,
          isLiked: Boolean(response.data?.isLiked),
          description: response.data?.description ?? null,
        });
      } catch (error) {
        
        setHouse(null);
      } finally {
        setLoading(false);
      }
    };

    fetchHouse();
  }, [houseId, listAiMessage]);

  const infraItems = useMemo(
    () =>
      house
        ? [
            {
              label: '버스정류장',
              value: house.infraCount.busStopCount,
              icon: <FaBusSimple className="text-[16px]" />,
            },
            {
              label: '편의점',
              value: house.infraCount.convenienceStoreCount,
              icon: <HiOutlineBuildingStorefront className="text-[18px]" />,
            },
            {
              label: '세탁소',
              value: house.infraCount.laundryCount,
              icon: <MdLocalLaundryService className="text-[18px]" />,
            },
            {
              label: '카페',
              value: house.infraCount.cafeCount,
              icon: <PiCoffee className="text-[18px]" />,
            },
            {
              label: '병원',
              value: house.infraCount.hospitalCount,
              icon: <FaHospital className="text-[16px]" />,
            },
            {
              label: '약국',
              value: house.infraCount.pharmacyCount,
              icon: <MdLocalPharmacy className="text-[18px]" />,
            },
          ]
        : [],
    [house],
  );

  const distanceItems = useMemo(
    () =>
      house
        ? [
            { label: '지하철', value: house.minDist.subwayDist },
            { label: '편의점', value: house.minDist.convenienceDist },
            { label: '세탁소', value: house.minDist.laundryDist },
            { label: '카페', value: house.minDist.cafeDist },
            { label: '병원', value: house.minDist.hospitalDist },
            { label: '약국', value: house.minDist.pharmacyDist },
          ]
        : [],
    [house],
  );

  const floorLabel =
    house && house.floor && house.floor.trim() && house.floor !== '-'
      ? house.floor
      : DEFAULT_FLOOR;

  const safetyScore =
    house && house.dongStats?.safetyScore > 0
      ? house.dongStats.safetyScore
      : DEFAULT_SAFETY_SCORE;

  const cctvCount =
    house && house.dongStats?.cctvCount > 0
      ? house.dongStats.cctvCount
      : DEFAULT_CCTV_COUNT;

  const streetLightCount =
    house && house.dongStats?.streetLightCount > 0
      ? house.dongStats.streetLightCount
      : DEFAULT_STREET_LIGHT_COUNT;

  const safetyFacilityCount =
    house && house.dongStats?.safetyFacilityCount > 0
      ? house.dongStats.safetyFacilityCount
      : DEFAULT_SAFETY_FACILITY_COUNT;

  const avgMealPrice =
    house && house.dongStats?.avgMealPrice > 0
      ? house.dongStats.avgMealPrice
      : DEFAULT_AVG_MEAL_PRICE;

  const avgMeatPrice =
    house && house.dongStats?.avgMeatPrice > 0
      ? house.dongStats.avgMeatPrice
      : DEFAULT_AVG_MEAT_PRICE;

  const livingCostItems = useMemo(
    () =>
      house
        ? [
            {
              label: '일반 식비 평균',
              value: `${avgMealPrice.toLocaleString()}원`,
              icon: <FaBowlRice className="text-[18px]" />,
            },
            {
              label: '고기 식비 평균',
              value: `${avgMeatPrice.toLocaleString()}원`,
              icon: <GiMeat className="text-[18px]" />,
            },
          ]
        : [],
    [house, avgMealPrice, avgMeatPrice],
  );

  const recommendationReason = useMemo(() => {
    const text = house?.aiMessage?.trim() || listAiMessage?.trim();
    return text && text.length > 0 ? text : '추천 이유가 아직 없습니다.';
  }, [house?.aiMessage, listAiMessage]);

  const normalizedRecommendationReason = useMemo(
    () => normalizeMarkdownText(recommendationReason),
    [recommendationReason],
  );

  if (loading) {
    return (
      <aside className="h-full w-95 border-l border-gray-200 bg-white">
        <div className="flex h-full flex-col items-center justify-center px-6 text-center">
          <div
            aria-label="매물 상세 로딩"
            className="h-10 w-10 animate-spin rounded-full border-4 border-primary-100 border-t-primary-500"
          />
          <p className="mt-4 text-sm font-medium text-gray-700">
            매물 정보를 불러오고 있어요
          </p>
        </div>
      </aside>
    );
  }

  if (!house) {
    return (
      <aside className="h-full w-95 overflow-y-auto border-l border-gray-200 bg-white p-5">
        <p className="text-sm text-gray-600">매물을 찾을 수 없습니다.</p>
      </aside>
    );
  }

  return (
    <aside className="h-full w-95 overflow-y-auto border-l border-gray-200 bg-[#F5F5F5]">
      <div className="sticky top-0 z-20 border-b border-gray-200 bg-white px-4 py-3">
        <button
          type="button"
          onClick={onBack}
          className="flex items-center gap-1 text-sm font-medium text-primary-600"
        >
          <HiOutlineArrowLeft className="text-base" />
          목록으로
        </button>
      </div>

      <div className="relative h-56 w-full bg-gray-100">
        {getThumbnail(house.images) !== '/images/house-placeholder.png' && !imageError ? (
          <Image
            src={getThumbnail(house.images)}
            alt="house"
            fill
            className="object-cover"
            unoptimized
            onError={() => setImageError(true)}
          />
        ) : (
          <div className="flex h-full w-full items-center justify-center bg-gray-200">
            <span className="text-sm text-gray-400">이미지 없음</span>
          </div>
        )}

        <div className="absolute right-1.5 top-1.5">
          <FavoriteButton
            houseId={house.houseId}
            initialLiked={house.isLiked}
            size="md"
            onChange={async (targetHouseId, liked) => {
              await onToggleWish(targetHouseId, liked);
              setHouse((prev) =>
                prev && prev.houseId === targetHouseId ? { ...prev, isLiked: liked } : prev,
              );
            }}
          />
        </div>
      </div>

      <div className="border-b border-gray-200 bg-white px-4 py-4">
        <div className="flex items-center gap-2">
          <span
            className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold ${
              house.houseStatus === '거래 가능'
                ? 'bg-[#E3F4D9] text-[#5E9F3B]'
                : 'bg-gray-100 text-gray-500'
            }`}
          >
            {house.houseStatus}
          </span>

          <span className="inline-flex items-center rounded-full bg-[#F3F0EC] px-3 py-1 text-xs font-semibold text-[#6B4F3A]">
            {house.houseType}
          </span>
        </div>

        <h2 className="mt-2.5 text-[20px] font-bold leading-snug text-black">
          {house.rentType === '전세'
            ? `전세 ${formatPrice(house.deposit)}`
            : `월세 ${formatPrice(house.monthlyCost)} / ${formatPrice(
                house.deposit,
              )}`}
        </h2>

        <p className="mt-1.5 text-sm text-gray-700">{house.dong}</p>
      </div>

      <div className="space-y-3 border-b border-gray-200 bg-white px-4 py-5">
        <div className="flex items-center gap-3">
          <HiOutlineHome className="text-xl text-[#273459]" />
          <span className="text-[15px] font-medium text-black">
            {house.houseType}
          </span>
        </div>

        <div className="flex items-center gap-3">
          <HiOutlineBuildingOffice2 className="text-xl text-[#273459]" />
          <span className="text-[15px] font-medium text-black">
            {floorLabel}
          </span>
        </div>

        <div className="flex items-center gap-3">
          <LuRuler className="text-xl text-[#273459]" />
          <span className="text-[15px] font-medium text-black">
            전용 {formatArea(house.floorSize)}
          </span>
        </div>

        <div className="flex items-center gap-3">
          <HiOutlineCalendar className="text-xl text-[#273459]" />
          <span className="text-[15px] font-medium text-black">
            {house.buildYear ? `${house.buildYear}년 준공` : '-'}
          </span>
        </div>
      </div>

      <div className="space-y-3 border-b border-gray-200 bg-white px-4 py-5">
        <h3 className="flex items-center gap-2 text-[16px] font-bold text-black">
          <HiOutlineSparkles /> 추천 이유
        </h3>

        <div className="rounded-xl border border-[#DDEFD1] bg-[#F7FBF4] p-3 text-[13px] leading-6 text-gray-800">
          <div className="space-y-1">
            {renderReasonContent(normalizedRecommendationReason)}
          </div>
        </div>
      </div>

      <div className="space-y-3 border-b border-gray-200 bg-white px-4 py-5">
        <h3 className="flex items-center gap-2 text-[16px] font-bold text-black">
          <HiOutlineClock /> 출퇴근
        </h3>

        <div className="grid grid-cols-2 gap-2.5">
          <div className="rounded-xl border border-gray-100 bg-gray-50 p-3">
            <p className="text-sm text-gray-500">시간</p>
            <p className="text-lg font-bold text-gray-900">
              {house.commuteData.commuteTime}분
            </p>
          </div>

          <div className="rounded-xl border border-gray-100 bg-gray-50 p-3">
            <p className="text-sm text-gray-500">거리</p>
            <p className="text-lg font-bold text-gray-900">
              {house.commuteData.commuteDistance.toFixed(2)}km
            </p>
          </div>
        </div>
      </div>

      <div className="space-y-3 border-b border-gray-200 bg-white px-4 py-5">
        <div>
          <div className="flex items-center justify-between">
            <div className="flex items-baseline gap-1.5">
              <h3 className="text-[16px] font-bold text-black">주변 인프라</h3>
              <span className="text-[12px] text-gray-400">(800m)</span>
            </div>
            <InfoTooltip
              text="매물 기준 800m 이내에 위치한 시설의 개수입니다. 800m는 성인이 부담 없이 걸어서 이동할 수 있는 거리로, 일상적인 생활 접근성을 판단하는 데 활용되는 기준 거리입니다."
            />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-2.5">
          {infraItems.map((item) => (
            <div
              key={item.label}
              className="flex items-center justify-between rounded-xl border border-gray-100 bg-gray-50 px-3 py-2.5"
            >
              <div className="flex items-center gap-2 text-gray-700">
                {item.icon}
                <span className="text-[13px] font-medium">{item.label}</span>
              </div>

              <span className="min-w-8 text-right text-[16px] font-bold text-gray-900">
                {item.value}
              </span>
            </div>
          ))}
        </div>
      </div>

      <div className="space-y-3 border-b border-gray-200 bg-white px-4 py-5">
        <div>
          <div className="flex items-center justify-between">
            <div className="flex items-baseline gap-1.5">
              <h3 className="text-[16px] font-bold text-black">시설 거리</h3>
              <span className="text-[12px] text-gray-400">(800m)</span>
            </div>
            <InfoTooltip
              text="매물에서 가장 가까운 시설까지의 실제 직선 거리입니다. 800m는 성인이 걸어서 이동할 수 있는 기준 거리로, 겉표시된 거리가 800m를 넘으면 차량 이동이 필요할 수 있습니다."
            />
          </div>
        </div>

        <div className="space-y-3">
          {distanceItems.map((item) => (
            <div key={item.label} className="space-y-1.5">
              <div className="flex items-center justify-between">
                <span className="text-[13px] font-medium text-gray-700">
                  {item.label}
                </span>
                <span className="text-[13px] font-semibold text-gray-900">
                  {formatDistance(item.value)}
                </span>
              </div>

              <div className="relative h-2 w-full rounded-full bg-gray-100">
                <div
                  className="h-full rounded-full bg-primary-500 transition-all duration-500"
                  style={{ width: isOutOfRange(item.value) ? '100%' : `${getDistanceBarPercent(item.value)}%` }}
                />
                {isOutOfRange(item.value) ? null : (
                  <div
                    className="absolute top-1/2 -translate-y-1/2 transition-all duration-500"
                    style={{ left: `calc(${getDistanceBarPercent(item.value)}% + 4px)` }}
                  >
                    <FaPersonWalking className="text-primary-500" style={{ fontSize: '12px' }} />
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="space-y-3 border-b border-gray-200 bg-white px-4 py-5">
        <div className="flex items-center justify-between">
          <h3 className="text-[16px] font-bold text-black">안전 점수</h3>
          <InfoTooltip
            text="동(dong) 단위로 산정된 안전 점수입니다. CCTV 수, 가로등 수, 치안시설 수를 종합해 0~100점으로 산정하며, 점수가 높을수록 안전한 주거 환경임을 나타냅니다."
          />
        </div>

        <div className="flex justify-center">
          <HalfGauge score={safetyScore} size={150} />
        </div>

        <div className="grid grid-cols-3 gap-2.5">
          <div className="rounded-xl bg-gray-50 px-2.5 py-2.5 text-center">
            <div className="flex items-center justify-center gap-1 text-gray-500">
              <span className="text-[12px]">CCTV</span>
            </div>
            <p className="mt-1 text-[16px] font-bold text-gray-900">
              {cctvCount}
            </p>
          </div>

          <div className="rounded-xl bg-gray-50 px-2.5 py-2.5 text-center">
            <p className="text-[12px] text-gray-500">가로등</p>
            <p className="mt-1 text-[16px] font-bold text-gray-900">
              {streetLightCount}
            </p>
          </div>

          <div className="rounded-xl bg-gray-50 px-2.5 py-2.5 text-center">
            <p className="text-[12px] text-gray-500">치안시설</p>
            <p className="mt-1 text-[16px] font-bold text-gray-900">
              {safetyFacilityCount}
            </p>
          </div>
        </div>
      </div>

      <div className="space-y-3 bg-white px-4 py-5">
        <div>
          <h3 className="text-[16px] font-bold text-black">생활비</h3>
        </div>

        <div className="space-y-2.5">
          {livingCostItems.map((item) => (
            <div
              key={item.label}
              className="flex items-center justify-between rounded-xl border border-gray-100 bg-gray-50 px-3 py-2.5"
            >
              <div className="flex items-center gap-3 text-gray-700">
                {item.icon}
                <span className="text-[13px] font-medium">{item.label}</span>
              </div>

              <span className="text-[15px] font-bold text-gray-900">
                {item.value}
              </span>
            </div>
          ))}
        </div>
      </div>

      <div className="space-y-3 border-t border-gray-200 bg-white px-5 py-6">
        <h3 className="text-[18px] font-bold text-black">매물 설명</h3>
        <p className="text-[14px] leading-6 text-gray-700 whitespace-pre-line wrap-break-word">
          {house.description && house.description.trim().length > 0
            ? house.description
            : '등록된 매물 설명이 없습니다.'}
        </p>
      </div>
    </aside>
  );
}