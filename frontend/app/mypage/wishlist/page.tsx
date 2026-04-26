'use client';

import { useEffect, useState, useCallback } from 'react';
import { fetchWithAuth } from '@/lib/fetchWithAuth';
import PropertyCard from '@/components/features/mypage/PropertyCard';
import PostedHouseDetailModal from '@/components/features/mypage/PostedHouseDetailModal';
import {
  fetchPostedHouseDetail,
  type PostedHouseDetail,
} from '@/app/mypage/posts/api';

type WishlistItem = {
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
  isLiked: boolean;
};

type WishlistApiItem = Omit<WishlistItem, 'isLiked'>;

const mapApiItemToWishlistItem = (item: WishlistApiItem): WishlistItem => ({
  ...item,
  isLiked: true,
});

export default function WishlistPage() {
  const [items, setItems] = useState<WishlistItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [selectedHouse, setSelectedHouse] = useState<PostedHouseDetail | null>(null);

  const loadWishlist = useCallback(async () => {
    try {
      setLoading(true);
      const res = await fetchWithAuth('/api/houses/wish', { method: 'GET' });
      if (!res.ok) {
        throw new Error('찜한 목록 조회 실패');
      }

      const json = await res.json();
      const data: WishlistApiItem[] = Array.isArray(json?.data?.data)
        ? json.data.data
        : Array.isArray(json?.data)
          ? json.data
          : [];

      setItems(data.map(mapApiItemToWishlistItem));
    } catch (error) {
      
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadWishlist();
  }, [loadWishlist]);

  const handleToggleLike = useCallback(async (houseId: number, liked: boolean) => {
    try {
      const res = await fetchWithAuth(`/api/houses/wish/${houseId}`, {
        method: 'POST',
      });

      if (!res.ok) {
        throw new Error('찜하기 토글 실패');
      }

      setItems((prev) =>
        liked
          ? prev.map((item) =>
            item.houseId === houseId ? { ...item, isLiked: true } : item,
          )
          : prev.filter((item) => item.houseId !== houseId),
      );
    } catch (error) {
      
      throw error;
    }
  }, []);

  const handleCardClick = async (houseId: number) => {
    setModalOpen(true);
    setDetailLoading(true);
    try {
      const data = await fetchPostedHouseDetail(houseId);
      
      setSelectedHouse(data);
    } catch {
      alert('매물 정보를 불러오지 못했습니다.');
    } finally {
      setDetailLoading(false);
    }
  };

  return (
    <>
      {loading ? (
        <div className="flex min-h-80 items-center justify-center text-sm text-gray-500">
          불러오는 중...
        </div>
      ) : items.length === 0 ? (
        <div className="flex min-h-80 items-center justify-center text-sm text-gray-500">
          찜한 매물이 없습니다.
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {items.map((item) => (
            <PropertyCard
              key={item.houseId}
              houseId={item.houseId}
              thumbnailUrl={item.thumbnailUrl}
              sidoName={item.sidoName}
              sigunguName={item.sigunguName}
              dongName={item.dongName}
              houseType={item.houseType}
              rentType={item.rentType}
              houseStatus={item.houseStatus}
              floor={item.floor}
              deposit={item.deposit}
              monthlyCost={item.monthlyCost}
              isLiked={item.isLiked}
              badgeText="찜한 매물"
              onToggleLike={handleToggleLike}
              onCardClick={handleCardClick}
            />
          ))}
        </div>
      )}
      <PostedHouseDetailModal
        open={modalOpen}
        loading={detailLoading}
        onClose={() => {
          setModalOpen(false);
          setSelectedHouse(null);
        }}
        house={selectedHouse}
      />
    </>
  );
}