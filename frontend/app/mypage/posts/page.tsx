'use client';

import { useEffect, useState, useCallback, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import PropertyCard from '@/components/features/mypage/PropertyCard';
import Button from '@/components/common/Button';
import SelectBox from '@/components/common/SelectBox';
import PostedHouseDetailModal from '@/components/features/mypage/PostedHouseDetailModal';
import { useAuthStore } from '@/store/useAuthStore';
import { fetchWithAuth } from '@/lib/fetchWithAuth';
import {
  fetchPostedHouseDetail,
  type PostedHouseDetail,
} from '@/app/mypage/posts/api';

type HouseItem = {
  houseId: number;
  thumbnailUrl: string | null;
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

type StatusFilter = 'all' | 'deleted' | 'completed' | 'available';

const statusFilterOptions = [
  { label: '전체', value: 'all' },
  { label: '매물 삭제', value: 'deleted' },
  { label: '거래완료', value: 'completed' },
  { label: '거래 가능', value: 'available' },
] as const;

const normalizeHouseStatus = (status?: string) => {
  if (status === '거래가능') return '거래 가능';
  if (status === '거래완료') return '거래 완료';
  if (status === '매물삭제') return '매물 삭제';
  return status || '상태 미정';
};

export default function PostPage() {
  const [items, setItems] = useState<HouseItem[]>([]);
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('all');
  const [loading, setLoading] = useState(true);
  const [isEditMode, setIsEditMode] = useState(false);
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());
  const [modalOpen, setModalOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [selectedHouse, setSelectedHouse] = useState<PostedHouseDetail | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [hasNextPage, setHasNextPage] = useState(false);
  const pageSize = 8;
  const router = useRouter();
  const accessToken = useAuthStore((state) => state.accessToken);

  const fetchPosts = useCallback(async (page: number = 0) => {
    if (!accessToken) {
      setLoading(false);
      return;
    }
    setLoading(true);
    try {
      const res = await fetchWithAuth(`/api/houses?page=${page}&size=${pageSize}`);
      if (res.ok) {
        const json = await res.json();
        
        const rawItems: HouseItem[] = json.data?.data ?? json.data ?? [];
        setItems(
          rawItems.map((item) => ({
            ...item,
            houseStatus: normalizeHouseStatus(item.houseStatus),
          })),
        );
        setCurrentPage(page);
        
        setHasNextPage(rawItems.length === pageSize);
      } else {
        const errText = await res.text();
        
      }
    } catch {
      
    } finally {
      setLoading(false);
    }
  }, [accessToken, pageSize]);

  useEffect(() => {
    fetchPosts(0);
  }, [fetchPosts]);

  const handleCardClick = async (houseId: number) => {
    if (isEditMode) {
      handleSelect(houseId);
    } else {
      try {
        setModalOpen(true);
        setDetailLoading(true);
        const data = await fetchPostedHouseDetail(houseId);
        
        setSelectedHouse(data);
        setModalOpen(true);
      } catch {
        alert('매물 정보를 불러오지 못했습니다.');
        setModalOpen(false);
      } finally {
        setDetailLoading(false);
      }
    }
  };

  const handleSelect = (houseId: number) => {
    setSelectedIds((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(houseId)) {
        newSet.delete(houseId);
      } else {
        newSet.add(houseId);
      }
      return newSet;
    });
  };

  const handleEditClick = () => {
    setIsEditMode((prev) => !prev);
    setSelectedIds(new Set());
  };

  const handleDelete = async () => {
    if (selectedIds.size === 0) return;
    try {
      await Promise.all(
        Array.from(selectedIds).map(async (houseId) => {
          const res = await fetchWithAuth(`/api/houses/${houseId}`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
          });
          const text = await res.text();
          let json = null;
          try { json = JSON.parse(text); } catch {}
          
          if (!res.ok) {
            
          }
        })
      );
      await fetchPosts();
    } catch (e) {
      
    } finally {
      setSelectedIds(new Set());
    }
  };

  const handleMarkAsCompleted = async () => {
    if (selectedIds.size === 0) return;
    try {
      await Promise.all(
        Array.from(selectedIds).map(async (houseId) => {
          const res = await fetchWithAuth(`/api/houses/${houseId}/sold`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
          });
          const json = await res.json().catch(() => null);
          if (!res.ok) {
            
          }
        })
      );
      await fetchPosts();
    } catch (e) {
      
    } finally {
      setSelectedIds(new Set());
    }
  };

  const handleUploadClick = () => {
    router.push('/mypage/posts/create');
  };

  const handleIndividualEdit = (houseId: number) => {
    router.push(`/mypage/posts/${houseId}/edit`);
  };

  const filteredItems = useMemo(() => {
    if (statusFilter === 'all') return items;

    if (statusFilter === 'deleted') {
      return items.filter((item) => item.houseStatus === '매물 삭제');
    }

    if (statusFilter === 'completed') {
      return items.filter((item) => item.houseStatus === '거래 완료');
    }

    return items.filter((item) => item.houseStatus === '거래 가능');
  }, [items, statusFilter]);

  return (
    <>
      <main className="space-y-6">
        <div className="flex flex-col gap-4 rounded-2xl border border-gray-200 bg-white px-5 py-5 md:flex-row md:items-center md:justify-between">
          <div>
            <h1 className="text-2xl font-bold tracking-[-0.03em] text-primary-black md:text-[28px]">
              등록 매물
            </h1>
            <p className="mt-1 text-sm text-gray-500">
              내가 등록한 매물을 확인하고 관리해보세요.          </p>
          </div>

          <div className="flex flex-wrap items-center gap-2">
            {isEditMode ? (
              <>
                <span className="text-sm text-gray-600">
                  {selectedIds.size}개 선택됨
                </span>
                <Button
                  label="취소"
                  size="sm"
                  onClick={handleEditClick}
                  className="bg-gray-400  hover:bg-gray-500"
                />
                <Button
                  label="삭제"
                  size="sm"
                  onClick={handleDelete}
                  disabled={selectedIds.size === 0}
                  className={`${selectedIds.size === 0
                    ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                    : 'bg-red-500  hover:bg-red-600'
                    }`}
                />
                <Button
                  label="거래완료"
                  size="sm"
                  onClick={handleMarkAsCompleted}
                  disabled={selectedIds.size === 0}
                  className={`${selectedIds.size === 0
                    ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                    : 'bg-green-500  hover:bg-green-600'
                    }`}
                />
              </>
            ) : (
              <>
                <Button
                  label="편집"
                  size="sm"
                  onClick={handleEditClick}
                  className="bg-primary-200 hover:bg-primary-300 hover:text-white"
                />
                <Button
                  label="매물 올리기"
                  size="sm"
                  onClick={handleUploadClick}
                  className="bg-primary-200 hover:bg-primary-300 hover:text-white"
                />
              </>
            )}
          </div>
        </div>

        <section className="relative border border-gray-200 bg-white p-5 md:p-6">
          <div className="absolute right-5 top-5 z-10 w-[130px] md:right-6 md:top-6">
            <SelectBox
              placeholder="상태"
              options={statusFilterOptions.map((option) => ({
                label: option.label,
                value: option.value,
              }))}
              value={statusFilter}
              onChange={(value) => setStatusFilter(value as StatusFilter)}
              size="compact"
            />
          </div>
          <div className="pt-14">
            {loading ? (
            <div className="flex min-h-[420px] items-center justify-center text-sm text-gray-500">
              불러오는 중...
            </div>
            ) : filteredItems.length > 0 ? (
            <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {filteredItems.map((property) => (
                <PropertyCard
                  key={property.houseId}
                  houseId={property.houseId}
                  thumbnailUrl={property.thumbnailUrl || null}
                  sidoName={property.sidoName}
                  sigunguName={property.sigunguName}
                  dongName={property.dongName}
                  houseType={property.houseType}
                  rentType={property.rentType}
                  houseStatus={property.houseStatus}
                  floor={property.floor}
                  deposit={property.deposit}
                  monthlyCost={property.monthlyCost}
                  isLiked={false}
                  showLikeButton={false}
                  isEditMode={isEditMode}
                  isSelected={selectedIds.has(property.houseId)}
                  onCardClick={handleCardClick}
                  onSelect={handleSelect}
                  onEdit={handleIndividualEdit}
                />
              ))}
            </div>
            ) : (
            <div className="flex min-h-[420px] items-center justify-center text-sm text-gray-500">
              조건에 맞는 매물이 없습니다.
            </div>
            )}

          {}
          <div className="mt-8 flex justify-center gap-4">
            <button
              onClick={() => fetchPosts(Math.max(0, currentPage - 1))}
              disabled={currentPage === 0}
              className="rounded-lg border border-gray-300 bg-white px-6 py-2 text-lg font-medium text-gray-700 disabled:cursor-not-allowed disabled:text-gray-400 hover:bg-gray-50"
            >
              &lt;
            </button>
            <button
              onClick={() => fetchPosts(currentPage + 1)}
              disabled={!hasNextPage}
              className="rounded-lg border border-gray-300 bg-white px-6 py-2 text-lg font-medium text-gray-700 disabled:cursor-not-allowed disabled:text-gray-400 hover:bg-gray-50"
            >
              &gt;
            </button>
          </div>
          </div>
        </section>
      </main>
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