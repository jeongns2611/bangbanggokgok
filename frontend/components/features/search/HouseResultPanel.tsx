'use client';

import { useEffect, useRef } from 'react';
import HouseResultCard from '@/components/common/HouseResultCard';
import type { HouseListItem } from '@/mock/houses';

export type ComparisonHouse = HouseListItem;

type HouseResultPanelProps = {
  houses: HouseListItem[];
  selectedHouseId: number | null;
  onSelectHouse: (houseId: number) => void;
  onToggleWish: (houseId: number, liked: boolean) => Promise<void>;
  hasMore?: boolean;
  isLoadingMore?: boolean;
  onLoadMore?: () => void;
  recommendationOrders?: Map<number, number>;
};

export default function HouseResultPanel({
  houses,
  selectedHouseId,
  onSelectHouse,
  onToggleWish,
  hasMore = false,
  isLoadingMore = false,
  onLoadMore,
  recommendationOrders,
}: HouseResultPanelProps) {
  const loadMoreRef = useRef<HTMLDivElement | null>(null);
  const hasMoreRef = useRef(hasMore);
  hasMoreRef.current = hasMore;
  const isLoadingMoreRef = useRef(isLoadingMore);
  isLoadingMoreRef.current = isLoadingMore;

  useEffect(() => {
    if (!onLoadMore || !loadMoreRef.current) return;

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting && hasMoreRef.current && !isLoadingMoreRef.current) {
            onLoadMore();
          }
        });
      },
      { threshold: 0.2 },
    );

    observer.observe(loadMoreRef.current);

    return () => {
      observer.disconnect();
    };
  }, [onLoadMore, houses.length]);

  return (
    <aside className="h-full w-[420px] overflow-y-auto border-l border-gray-200 bg-white">
      <div className="border-b border-gray-200 px-5 py-3.5">
        <h2 className="text-[17px] font-bold text-black">검색 결과</h2>
      </div>

      <div className="space-y-3 p-4">
        {houses.length === 0 && (
          <div className="rounded-xl border border-dashed border-gray-300 bg-gray-50 px-4 py-8 text-center text-sm text-gray-600">
            조건에 맞는 매물이 없습니다.
          </div>
        )}

        {houses.map((house) => {
          const isSelected = selectedHouseId === house.houseId;

          return (
            <HouseResultCard
              key={house.houseId}
              house={house}
              isSelected={isSelected}
              onSelectHouse={onSelectHouse}
              onToggleWish={onToggleWish}
              recommendationOrder={recommendationOrders?.get(house.houseId)}
            />
          );
        })}

        {hasMore && (
          <div ref={loadMoreRef} className="py-3 text-center text-sm text-gray-500">
            {isLoadingMore ? '불러오는 중...' : '스크롤해서 더 보기'}
          </div>
        )}
      </div>
    </aside>
  );
}
