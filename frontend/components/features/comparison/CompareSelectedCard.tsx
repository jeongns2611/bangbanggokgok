'use client';

import Image from 'next/image';
import { IoClose } from 'react-icons/io5';
import { HiPlus } from 'react-icons/hi';
import SelectBox from '@/components/common/SelectBox';

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

type CompareSelectCardProps = {
  slotTitle: string;
  selectedHouse: ComparisonHouse | null;
  houseOptions: ComparisonHouse[];
  selectedHouseId: number | '';
  onSelect: (houseId: number | '') => void;
  onRemove: () => void;
};

function formatPrice(value: number) {
  return value.toLocaleString('ko-KR');
}

function extractDongName(fullAddress: string) {
  const parts = fullAddress.split(' ');
  return parts[parts.length - 1] || fullAddress;
}

export default function CompareSelectCard({
  slotTitle,
  selectedHouse,
  houseOptions,
  selectedHouseId,
  onSelect,
  onRemove,
}: CompareSelectCardProps) {
  const thumbnail =
    selectedHouse?.images.find((img) => img.is_thumbnail)?.image_url ||
    selectedHouse?.images[0]?.image_url;

  const selectOptions = houseOptions.map((house) => ({
    label: `${extractDongName(house.dong)} · ${house.houseType} · ${house.deposit}/${house.monthlyCost}`,
    value: String(house.houseId),
  }));

  return (
    <article className="rounded-2xl border border-gray-200 bg-white shadow-sm">
      {selectedHouse ? (
        <>
          <div className="relative h-62.5 w-full overflow-hidden rounded-t-2xl">
            {thumbnail ? (
              <Image
                src={thumbnail}
                alt={`${selectedHouse.dong} 매물 이미지`}
                fill
                className="object-cover"
                unoptimized
              />
            ) : (
              <div className="flex h-full w-full items-center justify-center bg-gray-100 text-sm text-gray-400">
                이미지 없음
              </div>
            )}

            <button
              type="button"
              onClick={onRemove}
              className="absolute right-3 top-3 flex h-9 w-9 items-center justify-center rounded-full bg-black/35 text-white transition hover:bg-black/50"
              aria-label={`${slotTitle} 제거`}
            >
              <IoClose size={18} />
            </button>
          </div>

          <div className="space-y-4 p-6">
            <div>
              <p className="text-2xl font-bold text-gray-900">
                {extractDongName(selectedHouse.dong)} · {selectedHouse.houseType}
              </p>
              <p className="mt-2 text-lg text-gray-600">{selectedHouse.dong}</p>
            </div>

            <p className="text-lg font-medium text-gray-900">
              보증금 {formatPrice(selectedHouse.deposit)}만원 / 월세{' '}
              {formatPrice(selectedHouse.monthlyCost)}만원
            </p>

            <div className="pt-2">
              <label className="mb-2 block text-lg font-semibold text-gray-900">
                매물 선택
              </label>

              <SelectBox
                placeholder="매물 선택"
                options={selectOptions}
                value={selectedHouseId === '' ? '' : String(selectedHouseId)}
                onChange={(value) => onSelect(value ? Number(value) : '')}
                className="relative z-20"
              />
            </div>
          </div>
        </>
      ) : (
        <div className="p-6">
          <div className="flex min-h-62.5 items-center justify-center rounded-2xl border border-dashed border-[#c8d8ce] bg-[#fbfcfb]">
            <div className="text-center">
              <div className="mx-auto mb-5 flex h-14 w-14 items-center justify-center rounded-full border border-primary-100/40 text-primary-100">
                <HiPlus size={26} />
              </div>
              <p className="text-2xl font-bold text-gray-900">{slotTitle}</p>
              <p className="mt-3 text-lg text-gray-500">매물을 선택하세요</p>
            </div>
          </div>

          <div className="mt-6">
            <label className="mb-2 block text-lg font-semibold text-gray-900">
              매물 선택
            </label>

            <SelectBox
              placeholder="매물 선택"
              options={selectOptions}
              value={selectedHouseId === '' ? '' : String(selectedHouseId)}
              onChange={(value) => onSelect(value ? Number(value) : '')}
              className="relative z-20"
            />
          </div>
        </div>
      )}
    </article>
  );
}