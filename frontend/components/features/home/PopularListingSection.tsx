'use client';

import Image from 'next/image';
import { useState } from 'react';
import { FiChevronLeft, FiChevronRight } from 'react-icons/fi';

type Listing = {
  id: number;
  image: string;
  dealType: '월세' | '전세';
  priceText: string;
  infoText: string;
  locationText: string;
};

const popularListings: Listing[] = [
  {
    id: 1,
    image: '/house.png',
    dealType: '월세',
    priceText: '월세 50만원 / 보증금 1000만원',
    infoText: '오피스텔 · 10평 · 7층',
    locationText: '서울시 성동구 성수동',
  },
  {
    id: 2,
    image: '/house.png',
    dealType: '전세',
    priceText: '전세 1억 8,000만원',
    infoText: '원룸 · 8평 · 4층',
    locationText: '서울시 마포구 합정동',
  },
  {
    id: 3,
    image: '/house.png',
    dealType: '월세',
    priceText: '월세 65만원 / 보증금 2000만원',
    infoText: '오피스텔 · 12평 · 11층',
    locationText: '서울시 동작구 노량진동',
  },
  {
    id: 4,
    image: '/house.png',
    dealType: '전세',
    priceText: '전세 2억 1,000만원',
    infoText: '아파트 · 14평 · 9층',
    locationText: '서울시 광진구 자양동',
  },
  {
    id: 5,
    image: '/house.png',
    dealType: '월세',
    priceText: '월세 58만원 / 보증금 1500만원',
    infoText: '원룸 · 9평 · 3층',
    locationText: '서울시 관악구 봉천동',
  },
  {
    id: 6,
    image: '/house.png',
    dealType: '전세',
    priceText: '전세 1억 6,500만원',
    infoText: '빌라 · 11평 · 2층',
    locationText: '서울시 마포구 연남동',
  },
  {
    id: 7,
    image: '/house.png',
    dealType: '월세',
    priceText: '월세 72만원 / 보증금 3000만원',
    infoText: '오피스텔 · 13평 · 15층',
    locationText: '서울시 영등포구 당산동',
  },
  {
    id: 8,
    image: '/house.png',
    dealType: '전세',
    priceText: '전세 1억 9,500만원',
    infoText: '오피스텔 · 10평 · 6층',
    locationText: '서울시 동대문구 회기동',
  },
  {
    id: 9,
    image: '/house.png',
    dealType: '월세',
    priceText: '월세 48만원 / 보증금 800만원',
    infoText: '원룸 · 7평 · 4층',
    locationText: '서울시 중랑구 면목동',
  },
  {
    id: 10,
    image: '/house.png',
    dealType: '전세',
    priceText: '전세 2억 3,000만원',
    infoText: '아파트 · 15평 · 11층',
    locationText: '서울시 성동구 행당동',
  },
];

const VISIBLE_COUNT = 3;
const GAP_PX = 20;

function getDealTypeStyle(dealType: Listing['dealType']) {
  if (dealType === '전세') {
    return 'bg-blue-600 text-white';
  }

  return 'bg-amber-700 text-white';
}

export default function PopularListingSection() {
  const [currentIndex, setCurrentIndex] = useState(0);

  const maxIndex = Math.max(popularListings.length - VISIBLE_COUNT, 0);

  const handlePrev = () => {
    setCurrentIndex((prev) => Math.max(prev - 1, 0));
  };

  const handleNext = () => {
    setCurrentIndex((prev) => Math.min(prev + 1, maxIndex));
  };

  const isPrevDisabled = currentIndex === 0;
  const isNextDisabled = currentIndex === maxIndex;

  return (
    <section className="w-full bg-white py-14 md:py-16">
      <div className="mx-auto max-w-[1200px] px-6">
        <div className="mb-8">
          <h2 className="text-2xl font-semibold text-gray-900">
            지금 인기 있는 매물
          </h2>
        </div>

        <div className="relative">
          <button
            type="button"
            onClick={handlePrev}
            disabled={isPrevDisabled}
            className="absolute -left-10 top-1/2 z-10 -translate-y-1/2 text-gray-600 transition disabled:cursor-not-allowed disabled:opacity-30"
            aria-label="이전 매물 보기"
          >
            <FiChevronLeft size={28} />
          </button>

          <button
            type="button"
            onClick={handleNext}
            disabled={isNextDisabled}
            className="absolute -right-10 top-1/2 z-10 -translate-y-1/2 text-gray-600 transition disabled:cursor-not-allowed disabled:opacity-30"
            aria-label="다음 매물 보기"
          >
            <FiChevronRight size={28} />
          </button>

          <div className="overflow-hidden">
            <div
              className="flex transition-transform duration-500 ease-in-out"
              style={{
                gap: `${GAP_PX}px`,
                transform: `translateX(calc(-${currentIndex} * ((100% - ${GAP_PX * 2}px) / ${VISIBLE_COUNT} + ${GAP_PX}px)))`,
              }}
            >
              {popularListings.map((listing) => (
                <article
                  key={listing.id}
                  className="shrink-0 overflow-hidden rounded-xl border border-gray-200 bg-white"
                  style={{
                    width: `calc((100% - ${GAP_PX * 2}px) / ${VISIBLE_COUNT})`,
                  }}
                >
                  <div className="relative aspect-[4/3] w-full">
                    <Image
                      src={listing.image}
                      alt="매물 이미지"
                      fill
                      className="object-cover"
                    />

                    <span
                      className={`absolute left-0 top-0 px-4 py-2 text-sm font-semibold ${getDealTypeStyle(
                        listing.dealType,
                      )}`}
                    >
                      {listing.dealType}
                    </span>
                  </div>

                  <div className="p-5">
                    <h3 className="text-base font-semibold text-gray-900 md:text-lg">
                      {listing.priceText}
                    </h3>
                    <p className="mt-2 text-sm text-gray-600">
                      {listing.infoText}
                    </p>
                    <p className="mt-1 text-sm text-gray-600">
                      {listing.locationText}
                    </p>
                  </div>
                </article>
              ))}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
