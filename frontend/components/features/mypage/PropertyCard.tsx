'use client';

import { useMemo, useState } from 'react';
import Image from 'next/image';
import { FaEdit } from 'react-icons/fa';
import FavoriteButton from '@/components/common/FavoriteButton';

type PropertyCardProps = {
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

    badgeText?: string;
    isLiked?: boolean;
    showLikeButton?: boolean;

    isEditMode?: boolean;
    isSelected?: boolean;

    onCardClick?: (houseId: number) => void;
    onToggleLike?: (houseId: number, liked: boolean) => Promise<void> | void;
    onSelect?: (houseId: number) => void;
    onEdit?: (houseId: number) => void;
};

function formatPrice(value: number) {
    return `${value.toLocaleString()}만원`;
}

function getTitle(dongName: string, houseType: string) {
    return `${dongName} · ${houseType}`;
}

function getAddress(sidoName: string, sigunguName: string, dongName: string) {
    return `${sidoName} ${sigunguName} ${dongName}`;
}

function getPriceText(rentType: string, deposit: number, monthlyCost: number) {
    if (rentType === '전세') {
        return `전세 ${formatPrice(deposit)}`;
    }

    if (rentType === '월세') {
        return `보증금 ${formatPrice(deposit)} / 월세 ${formatPrice(monthlyCost)}`;
    }

    return `보증금 ${formatPrice(deposit)} / 월세 ${formatPrice(monthlyCost)}`;
}

function normalizeHouseStatus(status: string) {
    if (status === '거래가능') return '거래 가능';
    if (status === '거래완료') return '거래 완료';
    if (status === '매물삭제') return '매물 삭제';
    return status;
}

function getStatusBadgeClassName(status: string) {
    if (status === '거래 가능') {
        return 'bg-[#E3F4D9] text-[#2f7a2d]';
    }

    if (status === '거래 완료') {
        return 'bg-[#E7ECF8] text-[#36538F]';
    }

    if (status === '매물 삭제') {
        return 'bg-[#F4F4F5] text-[#52525B]';
    }

    return 'bg-background-100 text-primary-200';
}

export default function PropertyCard({
    houseId,
    thumbnailUrl,
    sidoName,
    sigunguName,
    dongName,
    houseType,
    rentType,
    houseStatus,
    floor,
    deposit,
    monthlyCost,
    badgeText,
    isLiked = false,
    showLikeButton = true,
    isEditMode = false,
    isSelected = false,
    onCardClick,
    onToggleLike,
    onSelect,
    onEdit,
}: PropertyCardProps) {
    const [hasImageError, setHasImageError] = useState(false);

    const handleCardAction = () => {
        if (isEditMode) {
            onSelect?.(houseId);
            return;
        }

        onCardClick?.(houseId);
    };

    const title = getTitle(dongName, houseType);
    const address = getAddress(sidoName, sigunguName, dongName);
    const priceText = getPriceText(rentType, deposit, monthlyCost);
    const normalizedStatus = normalizeHouseStatus(houseStatus);
    const displayBadge = badgeText || normalizedStatus;
    const displayBadgeClassName = badgeText
        ? 'bg-background-100 text-primary-200'
        : getStatusBadgeClassName(normalizedStatus);
    const safeThumbnailUrl = useMemo(() => {
        if (!thumbnailUrl) return null;
        return thumbnailUrl;
    }, [thumbnailUrl]);
    const shouldShowImage = Boolean(safeThumbnailUrl) && !hasImageError;

    return (
        <article
            onClick={(e) => {
                e.preventDefault();
                e.stopPropagation();
                handleCardAction();
            }}
            onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    e.stopPropagation();
                    handleCardAction();
                }
            }}
            role="button"
            tabIndex={0}
            className={`group cursor-pointer overflow-hidden rounded-2xl border-2 transition-all duration-300 hover:-translate-y-1 hover:shadow-[0_14px_32px_rgba(23,45,33,0.12)] ${isSelected
                ? 'border-[#27694b] bg-[#f3fcf8] shadow-[0_6px_24px_rgba(39,105,75,0.2)] ring-2 ring-[#27694b]/20'
                : 'border-black/5 bg-white shadow-[0_6px_24px_rgba(23,45,33,0.06)]'
                }`}
        >
            <div className="relative aspect-[4/3] overflow-hidden bg-gray-100">
                {shouldShowImage ? (
                    <Image
                        src={safeThumbnailUrl as string}
                        alt={title}
                        fill
                        onError={() => setHasImageError(true)}
                        className="object-cover transition-transform duration-500 group-hover:scale-105"
                    />
                ) : (
                    <div className="flex h-full w-full items-center justify-center text-sm text-gray-400">
                        이미지 없음
                    </div>
                )}

                <div className="absolute inset-0 bg-gradient-to-b from-black/5 via-transparent to-black/10" />

                {!isEditMode && (
                    <span className="absolute left-3 top-3 rounded-full bg-white/88 px-3 py-1 text-sm font-semibold text-[#27694b] backdrop-blur-sm">
                        {houseType}
                    </span>
                )}

                {isEditMode && (
                    <>
                        <div className="absolute left-3 top-3">
                            <button
                                type="button"
                                onClick={(e) => {
                                    e.stopPropagation();
                                    onSelect?.(houseId);
                                }}
                                className={`flex h-6 w-6 items-center justify-center rounded-full border-2 transition ${isSelected
                                    ? 'border-[#27694b] bg-[#27694b]'
                                    : 'border-white bg-white/20 backdrop-blur-sm'
                                    }`}
                            >
                                {isSelected && (
                                    <svg className="h-4 w-4 text-white" fill="currentColor" viewBox="0 0 20 20">
                                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                                    </svg>
                                )}
                            </button>
                        </div>
                        <button
                            type="button"
                            onClick={(e) => {
                                e.stopPropagation();
                                onEdit?.(houseId);
                            }}
                            className="cursor-pointer absolute right-3 top-3 flex h-9 w-9 items-center justify-center rounded-full bg-white/90 backdrop-blur-sm transition hover:scale-105 hover:bg-primary-200"
                        >
                            <FaEdit className="text-[16px] text-primary-200 transition-colors hover:text-white" />
                        </button>
                    </>
                )}

                {showLikeButton && !isEditMode && (
                    <div
                        onClick={(e) => {
                            e.stopPropagation();
                        }}
                        className="absolute right-3 top-3 flex h-10 w-10 items-center justify-center transition hover:scale-105"
                    >
                        <FavoriteButton
                            houseId={houseId}
                            size="md"
                            initialLiked={isLiked}
                            onChange={onToggleLike}
                        />
                    </div>
                )}
            </div>

            <div className="space-y-3 p-4">
                <div className="space-y-1">
                    <h3 className="line-clamp-1 text-lg font-bold text-[#0e1a13]">
                        {title}
                    </h3>
                    <p className="line-clamp-1 text-sm leading-5 text-black/55">
                        {address}
                    </p>
                    <p className="text-sm text-black/50">
                        {rentType} · {floor}
                    </p>
                </div>

                <div className="flex items-end justify-between gap-3">
                    <div className="min-w-0">
                        <span className="mb-1 block text-xs font-medium text-black/45">
                            거래 정보
                        </span>
                        <p className="line-clamp-1 text-base font-bold text-[#27694b]">
                            {priceText}
                        </p>
                    </div>

                    <span className={`shrink-0 rounded-full px-3 py-1 text-xs font-semibold ${displayBadgeClassName}`}>
                        {displayBadge}
                    </span>
                </div>
            </div>
        </article>
    );
}