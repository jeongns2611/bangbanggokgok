'use client';

import { HiOutlineTrophy } from 'react-icons/hi2';
import HouseResultCard from '@/components/common/HouseResultCard';
import type { HouseListItem } from '@/mock/houses';

type RankedHouseCardProps = {
  house: HouseListItem;
  rank: number;
  isSelected: boolean;
  onSelectHouse: (houseId: number) => void;
  onToggleWish?: (houseId: number, liked: boolean) => Promise<void>;
};

const getRankWrapperClassName = (rank: number) => {
  if (rank === 1) {
    return 'bg-[#F4C542] text-white';
  }

  if (rank === 2) {
    return 'bg-[#B8C2CC] text-white';
  }

  if (rank === 3) {
    return 'bg-[#B8793D] text-white';
  }

  return 'border border-gray-300 bg-white text-gray-700';
};

const getRankLabel = (rank: number) => {
  return `${rank}위`;
};

export default function RankedHouseCard({
  house,
  rank,
  isSelected,
  onSelectHouse,
  onToggleWish,
}: RankedHouseCardProps) {
  const isTopThree = rank <= 3;

  return (
    <div className="relative">
      <div
        className={`absolute left-3 top-3 z-20 flex min-h-10 min-w-10 items-center justify-center gap-1 rounded-full px-3 text-sm font-bold shadow-md ${getRankWrapperClassName(
          rank,
        )}`}
      >
        {isTopThree && <HiOutlineTrophy className="h-4 w-4" />}
        <span>{getRankLabel(rank)}</span>
      </div>

      <HouseResultCard
        house={house}
        isSelected={false}
        onSelectHouse={onSelectHouse}
        onToggleWish={onToggleWish}
      />
    </div>
  );
}