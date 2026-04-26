'use client';

import { useEffect, useRef } from 'react';
import Image from 'next/image';
import {
  HiOutlineBuildingOffice2,
  HiOutlineCalendar,
  HiOutlineHome,
  HiOutlineMapPin,
  HiOutlinePhoto,
  HiOutlineXMark,
} from 'react-icons/hi2';
import { LuRuler } from 'react-icons/lu';
import type { PostedHouseDetail } from '@/app/mypage/posts/api';

type KakaoLatLng = unknown;

type KakaoMapInstance = {
  setCenter: (center: KakaoLatLng) => void;
  relayout: () => void;
};

type KakaoMarkerInstance = {
  setPosition: (position: KakaoLatLng) => void;
  setMap: (map: KakaoMapInstance) => void;
};

type KakaoMapsApi = {
  load: (callback: () => void) => void;
  LatLng: new (lat: number, lng: number) => KakaoLatLng;
  Map: new (
    container: HTMLElement,
    options: { center: KakaoLatLng; level: number },
  ) => KakaoMapInstance;
  Marker: new (options: {
    position: KakaoLatLng;
    map: KakaoMapInstance;
  }) => KakaoMarkerInstance;
  event: {
    trigger: (target: KakaoMapInstance, eventName: string) => void;
  };
};

declare global {
  interface Window {
    kakao?: any;
  }
}

type Props = {
  open: boolean;
  loading: boolean;
  house: PostedHouseDetail | null;
  onClose: () => void;
};

const formatPrice = (value?: number) => {
  if (value === undefined || value === null) return '-';
  return `${value.toLocaleString()}만원`;
};

const formatArea = (value?: number) => {
  if (value === undefined || value === null) return '-';
  return `${value.toFixed(2)}m²`;
};

const getImageUrl = (image?: { image_url?: string | null; imageUrl?: string | null }) => {
  return image?.image_url || image?.imageUrl || null;
};

const getIsThumbnail = (image?: { is_thumbnail?: boolean | null; isThumbnail?: boolean | null }) => {
  return Boolean(image?.is_thumbnail ?? image?.isThumbnail);
};

const getThumbnail = (images?: PostedHouseDetail['images']) => {
  if (!images || images.length === 0) return null;
  const thumbnail = images.find((img) => getIsThumbnail(img) && getImageUrl(img));
  return getImageUrl(thumbnail) || getImageUrl(images[0]) || null;
};

export default function PostedHouseDetailModal({ open, loading, house, onClose }: Props) {
  const mapRef = useRef<HTMLDivElement | null>(null);
  const mapInstanceRef = useRef<KakaoMapInstance | null>(null);
  const markerRef = useRef<KakaoMarkerInstance | null>(null);

  useEffect(() => {
    if (!open) {
      mapInstanceRef.current = null;
      markerRef.current = null;
      return;
    }

    if (
      house?.lat === undefined ||
      house?.lat === null ||
      house?.lng === undefined ||
      house?.lng === null ||
      !mapRef.current ||
      !window.kakao
    ) {
      return;
    }

    window.kakao.maps.load(() => {
      if (
        !mapRef.current ||
        house?.lat === undefined ||
        house?.lat === null ||
        house?.lng === undefined ||
        house?.lng === null
      ) {
        return;
      }

      const center = new window.kakao.maps.LatLng(house.lat, house.lng);

      mapInstanceRef.current = new window.kakao.maps.Map(mapRef.current, {
        center,
        level: 4,
      });

      markerRef.current = new window.kakao.maps.Marker({
        position: center,
        map: mapInstanceRef.current,
      });

      const mapInstance = mapInstanceRef.current;
      if (!mapInstance) {
        return;
      }

      window.kakao.maps.event.trigger(mapInstance, 'resize');
      mapInstance.relayout();
      mapInstance.setCenter(center);
    });
  }, [house?.houseId, house?.lat, house?.lng, open]);

  if (!open) return null;

  const thumbnail = getThumbnail(house?.images);
  const images = house?.images?.filter((image) => Boolean(getImageUrl(image))) ?? [];
  const addressText =
    house?.address ||
    `${house?.sidoName ?? ''} ${house?.sigunguName ?? ''} ${house?.dongName ?? ''}`.trim() ||
    '-';
  const title =
    house?.rentType === '전세'
      ? `전세 ${formatPrice(house?.deposit)}`
      : `월세 ${formatPrice(house?.monthlyCost)} / 보증금 ${formatPrice(house?.deposit)}`;

  const getStatusClassName = (status?: string) => {
    if (status === '거래가능' || status === '거래 가능') {
      return 'bg-[#E3F4D9] text-[#5E9F3B]';
    }

    if (status === '거래완료' || status === '거래 완료') {
      return 'bg-[#E8ECF9] text-[#36538F]';
    }

    return 'bg-gray-100 text-gray-600';
  };

  return (
    <div
      className="fixed inset-0 z-100 flex items-center justify-center bg-black/45 p-4"
      onClick={onClose}
      role="dialog"
      aria-modal="true"
      aria-label="등록 매물 상세 정보"
    >
      <div
        className="relative h-[90vh] w-full max-w-215 overflow-hidden rounded-2xl bg-[#F5F5F5] shadow-2xl"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="sticky top-0 z-20 flex items-center justify-between border-b border-gray-200 bg-white px-5 py-4">
          <h2 className="text-[18px] font-bold text-black">등록 매물 상세</h2>
          <button
            type="button"
            onClick={onClose}
            className="rounded-full p-1 text-gray-600 transition hover:bg-gray-100 hover:text-black"
            aria-label="닫기"
          >
            <HiOutlineXMark className="text-[24px]" />
          </button>
        </div>

        {loading ? (
          <div className="flex h-[calc(90vh-65px)] items-center justify-center bg-white text-sm text-gray-600">
            불러오는 중...
          </div>
        ) : !house ? (
          <div className="flex h-[calc(90vh-65px)] items-center justify-center bg-white text-sm text-gray-600">
            매물을 찾을 수 없습니다.
          </div>
        ) : (
          <div className="h-[calc(90vh-65px)] overflow-y-auto bg-[#F5F5F5]">
            <div className="relative h-80 w-full bg-gray-100">
              {thumbnail ? (
                <Image
                  src={thumbnail}
                  alt="매물 대표 이미지"
                  fill
                  className="object-cover"
                  unoptimized
                />
              ) : (
                <div className="flex h-full w-full items-center justify-center bg-gray-200 text-sm text-gray-500">
                  이미지 없음
                </div>
              )}

              <div className="absolute inset-0 bg-linear-to-t from-black/35 via-black/10 to-transparent" />

              <div className="absolute bottom-4 left-4 right-4 flex flex-wrap items-center gap-2">
                <span
                  className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold ${getStatusClassName(
                    house.houseStatus,
                  )}`}
                >
                  {house.houseStatus ?? '-'}
                </span>

                <span className="inline-flex items-center rounded-full bg-white/90 px-3 py-1 text-xs font-semibold text-[#6B4F3A]">
                  {house.houseType ?? '-'}
                </span>

                <span className="inline-flex items-center rounded-full bg-white/90 px-3 py-1 text-xs font-semibold text-gray-700">
                  {house.rentType ?? '-'}
                </span>
              </div>
            </div>

            <div className="space-y-5 border-b border-gray-200 bg-white px-5 py-5">
              <h3 className="mt-3 text-[24px] font-bold leading-snug text-black">{title}</h3>

              <div className="rounded-xl border border-gray-100 bg-gray-50 px-4 py-3">
                <div className="flex items-start gap-2">
                  <HiOutlineMapPin className="mt-0.5 shrink-0 text-[18px] text-[#273459]" />
                  <div className="space-y-1">
                    <p className="text-xs font-semibold text-gray-500">주소</p>
                    <p className="text-[15px] font-medium text-gray-800">{addressText}</p>
                    <p className="text-[13px] text-gray-600">
                      {house.sidoName ?? '-'} / {house.sigunguName ?? '-'} / {house.dongName ?? '-'}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <div className="space-y-3 border-b border-gray-200 bg-white px-5 py-6">
              <h4 className="text-[18px] font-bold text-black">거래 정보</h4>
              <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                <div className="rounded-xl border border-gray-100 bg-gray-50 px-4 py-3">
                  <p className="text-xs text-gray-500">
                    {house.rentType === '전세' ? '전세' : '보증금'}
                  </p>
                  <p className="mt-1 text-[18px] font-bold text-gray-900">{formatPrice(house.deposit)}</p>
                </div>
                {house.rentType !== '전세' && (
                  <div className="rounded-xl border border-gray-100 bg-gray-50 px-4 py-3">
                    <p className="text-xs text-gray-500">월세</p>
                    <p className="mt-1 text-[18px] font-bold text-gray-900">{formatPrice(house.monthlyCost)}</p>
                  </div>
                )}
                <div className="rounded-xl border border-gray-100 bg-gray-50 px-4 py-3">
                  <p className="text-xs text-gray-500">관리비</p>
                  <p className="mt-1 text-[18px] font-bold text-gray-900">
                    {formatPrice(house.managementCost ?? undefined)}
                  </p>
                </div>
                <div className="rounded-xl border border-gray-100 bg-gray-50 px-4 py-3">
                  <p className="text-xs text-gray-500">관리비 항목</p>
                  <p className="mt-1 text-[15px] font-medium text-gray-900">{house.managementItems || '-'}</p>
                </div>
              </div>
            </div>

            <div className="space-y-3 border-b border-gray-200 bg-white px-5 py-6">
              <h4 className="text-[18px] font-bold text-black">매물 기본 정보</h4>
              <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                <div className="flex items-center gap-3 rounded-xl border border-gray-100 bg-gray-50 px-4 py-3">
                  <HiOutlineHome className="text-[20px] text-[#273459]" />
                  <div>
                    <p className="text-xs text-gray-500">주택유형</p>
                    <p className="text-[15px] font-semibold text-gray-900">{house.houseType ?? '-'}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3 rounded-xl border border-gray-100 bg-gray-50 px-4 py-3">
                  <HiOutlineBuildingOffice2 className="text-[20px] text-[#273459]" />
                  <div>
                    <p className="text-xs text-gray-500">층 정보</p>
                    <p className="text-[15px] font-semibold text-gray-900">{house.floor ?? '-'}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3 rounded-xl border border-gray-100 bg-gray-50 px-4 py-3">
                  <LuRuler className="text-[20px] text-[#273459]" />
                  <div>
                    <p className="text-xs text-gray-500">연면적</p>
                    <p className="text-[15px] font-semibold text-gray-900">{formatArea(house.floorSize)}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3 rounded-xl border border-gray-100 bg-gray-50 px-4 py-3">
                  <HiOutlineCalendar className="text-[20px] text-[#273459]" />
                  <div>
                    <p className="text-xs text-gray-500">건축년도</p>
                    <p className="text-[15px] font-semibold text-gray-900">
                      {house.buildYear ? `${house.buildYear}년` : '-'}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <div className="space-y-3 border-b border-gray-200 bg-white px-5 py-6">
              <h4 className="text-[18px] font-bold text-black">위치 정보</h4>
              {house.lat !== undefined && house.lat !== null && house.lng !== undefined && house.lng !== null ? (
                <div className="overflow-hidden rounded-xl border border-gray-200">
                  <div ref={mapRef} className="h-56 w-full bg-gray-100" />
                </div>
              ) : (
                <div className="rounded-xl border border-dashed border-gray-300 bg-gray-50 px-4 py-6 text-center text-sm text-gray-500">
                  좌표 정보가 없어 지도를 표시할 수 없습니다.
                </div>
              )}
            </div>

            <div className="space-y-3 border-b border-gray-200 bg-white px-5 py-6">
              <div className="flex items-center justify-between">
                <h4 className="text-[18px] font-bold text-black">매물 이미지</h4>
                <span className="inline-flex items-center gap-1 rounded-full bg-gray-100 px-3 py-1 text-xs font-semibold text-gray-600">
                  <HiOutlinePhoto className="text-[14px]" />
                  {images.length}장
                </span>
              </div>

              {images.length > 0 ? (
                <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
                  {images.map((image, index) => (
                    <div key={`${getImageUrl(image)}-${index}`} className="relative aspect-4/3 overflow-hidden rounded-xl bg-gray-100">
                      <Image
                        src={getImageUrl(image)!}
                        alt={`매물 이미지 ${index + 1}`}
                        fill
                        className="object-cover"
                        unoptimized
                      />
                      {getIsThumbnail(image) && (
                        <span className="absolute left-2 top-2 rounded-full bg-black/60 px-2 py-0.5 text-[11px] font-semibold text-white">
                          대표
                        </span>
                      )}
                    </div>
                  ))}
                </div>
              ) : (
                <div className="rounded-xl border border-dashed border-gray-300 bg-gray-50 px-4 py-6 text-center text-sm text-gray-500">
                  등록된 이미지가 없습니다.
                </div>
              )}
            </div>

            <div className="space-y-3 bg-white px-5 py-6">
              <h4 className="text-[18px] font-bold text-black">설명</h4>
              <p className="whitespace-pre-line text-[15px] leading-relaxed text-gray-700">
                {house.description || '매물 설명이 없습니다.'}
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}