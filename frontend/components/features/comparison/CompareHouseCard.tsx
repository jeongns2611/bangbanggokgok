'use client';

import Image from 'next/image';
import { IoClose } from 'react-icons/io5';

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
};

type CompareHouseCardProps = {
  house: ComparisonHouse;
  onRemove?: (houseId: number) => void;
};

function formatPrice(value: number) {
  return value.toLocaleString('ko-KR');
}

function extractDongName(fullAddress: string) {
  const parts = fullAddress.split(' ');
  return parts[parts.length - 1] || fullAddress;
}

export default function CompareHouseCard({
  house,
  onRemove,
}: CompareHouseCardProps) {
  const thumbnail =
    house.images.find((img) => img.is_thumbnail)?.image_url ||
    house.images[0]?.image_url ||
    '/placeholder-house.png';

  return (
    <article className="overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm">
      <div className="relative h-[220px] w-full">
        <Image
          src={thumbnail}
          alt={`${house.dong} 매물 이미지`}
          fill
          className="object-cover"
          unoptimized
        />

        <button
          type="button"
          onClick={() => onRemove?.(house.houseId)}
          className="absolute right-3 top-3 flex h-8 w-8 items-center justify-center rounded-full bg-black/35 text-white transition hover:bg-black/50"
          aria-label="비교 매물 제거"
        >
          <IoClose size={18} />
        </button>
      </div>

      <div className="space-y-2 p-5">
        <h2 className="text-lg font-semibold text-gray-900">
          {extractDongName(house.dong)} · {house.houseType}
        </h2>

        <p className="text-sm text-gray-500">{house.dong}</p>

        <p className="pt-1 text-sm font-medium text-gray-800">
          보증금 {formatPrice(house.deposit)}만원 / 월세 {formatPrice(house.monthlyCost)}만원
        </p>
      </div>
    </article>
  );
}