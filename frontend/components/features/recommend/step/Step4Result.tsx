'use client';

import Button from '@/components/common/Button';
import type { ReactNode } from 'react';
import { BiLoaderCircle } from 'react-icons/bi';
import RegionMapCard from './RegionMapCard';

type MetricTab = '편의시설' | '통근시간' | '평균가격' | '매물수' | '식비지수';

type RecommendRegion = {
  rank: 1 | 2 | 3;
  label: string;
};

type Step4ResultProps = {
  selectedMetric: MetricTab | null;
  recommendedRegions: RecommendRegion[];
  onChangeMetric: (value: MetricTab | null) => void;
  onClickRegion: (sigunguName: string) => void;
  districtMetrics?: {
    district: string;
    value: number | string;
    rank?: 1 | 2 | 3;
  }[];
  mapSlot?: ReactNode;
  isLoadingRegionTop?: boolean;
  loadingProgress?: 'analyzing' | 'checking' | 'composing' | 'waiting' | null;
};

const metricTabs: MetricTab[] = [
  '편의시설',
  '통근시간',
  '평균가격',
  '매물수',
  '식비지수',
];

export default function Step4Result({
  selectedMetric,
  recommendedRegions,
  onChangeMetric,
  onClickRegion,
  districtMetrics = [],
  mapSlot,
  isLoadingRegionTop = false,
  loadingProgress = null,
}: Step4ResultProps) {
  const getLoadingMessage = () => {
    switch (loadingProgress) {
      case 'analyzing':
        return '조건을 분석하고 있어요';
      case 'checking':
        return '매물 데이터를 확인하고 있어요';
      case 'composing':
        return '사용자에게 맞는 추천 이유를 정리하고 있어요';
      case 'waiting':
        return '조금 더 걸리고 있어요. 결과가 준비되는 대로 보여드릴게요';
      default:
        return null;
    }
  };

  const getLoadingProgress = () => {
    switch (loadingProgress) {
      case 'analyzing':
        return 25;
      case 'checking':
        return 60;
      case 'composing':
        return 85;
      case 'waiting':
        return 95;
      default:
        return 0;
    }
  };
  return (
    <section>
      <div className="grid gap-6 lg:grid-cols-[360px_minmax(0,1fr)]">
        <div>
          <h2 className="text-[22px] font-extrabold tracking-[-0.02em] text-black sm:text-[25px]">
            STEP 4. 추천 지역 결과
          </h2>

          <p className="mt-2 text-[14px] leading-6 text-[#4B5563]">
            지도를 클릭하면 해당 지역의 상세 통계를 확인할 수 있습니다.
          </p>

          <div className="mt-5">
            <h3 className="text-[16px] font-bold text-black">추천 지역 TOP3</h3>
            <p className="mt-2 text-[14px] leading-6 text-[#6B7280]">
              추천 지역을 클릭하면 해당 지역구의 통계를 확인할 수 있어요.
              <br />
              상세 통계에서 <span className="font-semibold text-primary-black">매물 보러가기</span> 버튼을 눌러 바로 매물도 확인할 수 있습니다.
            </p>

            <div className="mt-4 space-y-4">
              {recommendedRegions.map((region) => (
                <div
                  key={region.rank}
                  className="grid grid-cols-[60px_minmax(0,1fr)] items-center gap-3"
                >
                  <span className="text-[18px] font-extrabold text-primary-300">
                    TOP{region.rank}.
                  </span>

                  <button
                    type="button"
                    onClick={() => onClickRegion(region.label)}
                    className="flex h-13 w-full cursor-pointer items-center justify-center rounded-xl border border-[#D9DEE3] bg-white px-5 text-[16px] font-semibold text-primary-black transition hover:border-background-400 hover:bg-background-100"
                  >
                    {region.label}
                  </button>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="overflow-hidden rounded-xl border border-[#D9DEE3] bg-white">
          <div className="relative z-10 flex flex-wrap gap-2 border-b border-[#E5E7EB] bg-white px-3 py-3">
            {metricTabs.map((tab) => (
              <div
                key={tab}
                className={`relative ${tab === '식비지수' ? 'hidden' : ''}`}
              >
                <Button
                  label={tab}
                  size="sm"
                  isActive={selectedMetric === tab}
                  onClick={() => onChangeMetric(selectedMetric === tab ? null : tab)}
                  className="h-9 rounded-xl px-3.5 text-sm font-semibold"
                  disabled={isLoadingRegionTop}
                />
                {isLoadingRegionTop && selectedMetric === tab && (
                  <BiLoaderCircle className="absolute right-2 top-1/2 -translate-y-1/2 animate-spin text-lg text-primary-500" />
                )}
              </div>
            ))}
          </div>

          <div className="h-105 w-full bg-[#F3F4F6]">
            {isLoadingRegionTop && loadingProgress ? (
              <div className="flex h-full flex-col items-center justify-center gap-6 px-6 py-8">
                <div className="flex flex-col items-center gap-4">
                  <BiLoaderCircle className="animate-spin text-4xl text-primary-500" />
                  <p className="text-center text-[16px] font-semibold text-primary-black">
                    {getLoadingMessage()}
                  </p>
                </div>

                <div className="w-full max-w-xs">
                  <div className="h-2 w-full overflow-hidden rounded-full bg-gray-200">
                    <div
                      className="h-full bg-primary-300 transition-all duration-500 ease-out"
                      style={{ width: `${getLoadingProgress()}%` }}
                    />
                  </div>
                  <p className="mt-2 text-center text-[12px] text-gray-500">
                    {getLoadingProgress()}%
                  </p>
                </div>
              </div>
            ) : mapSlot ? (
              mapSlot
            ) : (
              <div className="flex h-full items-center justify-center text-sm text-[#9CA3AF]">
                <div className="h-full w-full p-1 md:p-1.5">
                  <RegionMapCard
                    selectedMetric={selectedMetric}
                    onClickRegion={onClickRegion}
                    districtMetrics={districtMetrics}
                  />
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </section>
  );
}
