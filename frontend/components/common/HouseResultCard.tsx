'use client';

import Image from 'next/image';
import { useCallback, useState } from 'react';
import FavoriteButton from '@/components/common/FavoriteButton';
import type { HouseListItem } from '@/mock/houses';

type HouseResultCardProps = {
  house: HouseListItem;
  isSelected: boolean;
  onSelectHouse: (houseId: number) => void;
  onToggleWish?: (houseId: number, liked: boolean) => Promise<void>;
  recommendationOrder?: number;
};

const getThumbnail = (images: HouseListItem['images']) => {
  const thumbnail = images.find((image) => image.is_thumbnail);

  return (
    thumbnail?.image_url ||
    images[0]?.image_url ||
    '/images/house-placeholder.png'
  );
};

const formatPrice = (
  deposit: number,
  monthlyCost: number,
  rentType: string,
) => {
  if (rentType === '전세') {
    return `전세 ${deposit.toLocaleString()}만원`;
  }

  return `월세 ${monthlyCost.toLocaleString()}만원 / 보증금 ${deposit.toLocaleString()}만원`;
};

export default function HouseResultCard({
  house,
  isSelected,
  onSelectHouse,
  onToggleWish,
  recommendationOrder,
}: HouseResultCardProps) {
  const [imageError, setImageError] = useState(false);

  

  const handleImageError = useCallback(() => {
    setImageError(true);
  }, []);
  return (
    <div
      role="button"
      tabIndex={0}
      onClick={() => onSelectHouse(house.houseId)}
      onKeyDown={(event) => {
        if (event.key === 'Enter' || event.key === ' ') {
          event.preventDefault();
          onSelectHouse(house.houseId);
        }
      }}
      aria-label={`${house.houseType} 매물 선택`}
      className={`w-full cursor-pointer overflow-hidden rounded-2xl border text-left transition ${
        isSelected
          ? 'border-primary-500 ring-2 ring-primary-100'
          : 'border-gray-200 hover:border-gray-300'
      }`}
    >
      <div className="relative h-45 w-full bg-gray-100">
        {imageError ? (
          <div className="flex h-full w-full items-center justify-center bg-gray-200">
            <p className="text-sm font-medium text-gray-500">이미지 없음</p>
          </div>
        ) : (
          <Image
            src={getThumbnail(house.images)}
            alt={`${house.houseType} 이미지`}
            fill
            className="object-cover"
            unoptimized
            onError={handleImageError}
          />
        )}

        {recommendationOrder && (
          <div className="absolute left-3 top-3 z-20 flex h-10 w-10 items-center justify-center rounded-full border-2 border-white bg-primary-100 text-2xl font-extrabold text-white shadow-lg">
            {recommendationOrder === 1 ? '①' : recommendationOrder === 2 ? '②' : '③'}
          </div>
        )}

        <div
          className="absolute right-1.5 top-1.5 z-10"
          onClick={(event) => event.stopPropagation()}
          onKeyDown={(event) => event.stopPropagation()}
        >
          <FavoriteButton
            houseId={house.houseId}
            initialLiked={house.isLiked}
            size="md"
            onChange={onToggleWish}
          />
        </div>
      </div>

      <div className="space-y-3 px-4 py-4">
        <div className="flex items-center gap-2">
          <span
            className={`rounded-full px-2.5 py-1 text-xs font-semibold ${
              house.houseStatus === '거래 가능'
                ? 'bg-[#E3F4D9] text-[#5E9F3B]'
                : house.houseStatus === '거래 완료'
                  ? 'bg-gray-200 text-gray-500'
                  : 'bg-gray-100 text-gray-600'
            }`}
          >
            {house.houseStatus}
          </span>

          <span className="rounded-full bg-[#F3F0EC] px-2.5 py-1 text-xs font-semibold text-[#6B4F3A]">
            {house.houseType}
          </span>
        </div>

        <div>
          <h3 className="text-[17px] font-bold leading-snug text-black">
            {formatPrice(house.deposit, house.monthlyCost, house.rentType)}
          </h3>
          <p className="mt-1 text-[14px] text-gray-600">{house.dong}</p>
        </div>

        <div className="text-[14px] text-gray-700">
          <span>{house.floor}</span>
        </div>
      </div>
    </div>
  );
}