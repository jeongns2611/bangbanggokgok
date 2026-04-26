'use client';

import Button from '@/components/common/Button';
import type { RegionTopRecommendationItem } from '@/types/api';

type RegionStatsModalProps = {
  isOpen: boolean;
  region: RegionTopRecommendationItem | null;
  onClose: () => void;
  onClickViewListings?: (region: RegionTopRecommendationItem) => void;
};

const formatManwon = (value?: number) => {
  if (value == null) return '-';
  return `${value.toLocaleString()}만원`;
};

const formatMinutesToHourMinute = (value?: number) => {
  if (value == null) return '-';

  const hour = Math.floor(value / 60);
  const minute = Math.round(value % 60);

  if (hour === 0) return `${minute}분`;
  if (minute === 0) return `${hour}시간`;

  return `${hour}시간 ${minute}분`;
};

const formatCount = (value?: number) => {
  if (value == null) return '-';
  return `${value.toLocaleString()}개`;
};

export default function RegionStatsModal({
  isOpen,
  region,
  onClose,
  onClickViewListings,
}: RegionStatsModalProps) {
  if (!isOpen || !region) return null;

  const rentTypeName = region.rentType?.codeName ?? '';
  const isJeonse = rentTypeName === '전세' || region.housingCost?.avgMonthlyRent === 0;
  const facility = region.facilityCount;
  const commute = region.commute;
  const housingCost = region.housingCost;
  const livingCost = region.livingCost;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/40 px-3 py-4 backdrop-blur-sm md:px-4 md:py-6">
      <div className="flex min-h-full items-start justify-center">
        <div className="my-auto flex w-full max-w-4xl flex-col overflow-hidden rounded-3xl bg-white shadow-xl max-h-[calc(100vh-32px)] md:max-h-[calc(100vh-48px)]">
          <div className="relative flex items-start justify-between gap-4 border-b border-[#E5E7EB] bg-white px-5 py-4 md:px-6 md:py-5">
            <div>
              <h2 className="text-2xl font-extrabold text-black md:text-[26px]">
                {region.sigunguName} 통계
              </h2>
              <p className="mt-2 text-sm text-[#4B5563] md:text-[15px]">
                선택한 지역의 주요 생활 통계를 확인할 수 있습니다.
              </p>
            </div>

            <button
              type="button"
              onClick={onClose}
              className="cursor-pointer rounded-full border border-[#E5E7EB] bg-white px-3 py-2 text-xl font-bold text-black transition hover:bg-gray-50"
            >
              ×
            </button>
          </div>

          <div className="min-h-0 flex-1 overflow-y-auto px-5 py-4 md:px-6 md:py-5">
            <div className="grid gap-4 lg:grid-cols-[1.1fr_1.4fr]">
              <div className="rounded-2xl border border-[#D9DEE3] bg-white p-5 shadow-sm md:p-6">
                <div className="space-y-6">
                  {!isJeonse && (
                    <div>
                      <div className="flex items-center justify-between gap-3">
                        <span className="text-base font-bold text-black md:text-[18px]">
                          평균 월세
                        </span>
                        <span className="rounded-xl bg-[#DDE8DF] px-3 py-1.5 text-base font-bold text-primary-300 md:px-4 md:py-2 md:text-[18px]">
                          {formatManwon(housingCost?.avgMonthlyRent)}
                        </span>
                      </div>
                      <p className="mt-3 text-sm text-[#4B5563] md:text-[15px]">
                        해당 지역 매물의 평균 월세 가격
                      </p>
                    </div>
                  )}

                  <div>
                    <div className="flex items-center justify-between gap-3">
                      <span className="text-base font-bold text-black md:text-[18px]">
                        평균 보증금
                      </span>
                      <span className="rounded-xl bg-[#DDE8DF] px-3 py-1.5 text-base font-bold text-primary-300 md:px-4 md:py-2 md:text-[18px]">
                        {formatManwon(housingCost?.avgDeposit)}
                      </span>
                    </div>
                    <p className="mt-3 text-sm text-[#4B5563] md:text-[15px]">
                      최근 등록된 매물 기준 평균 보증금
                    </p>
                  </div>

                  <div>
                    <div className="flex items-center justify-between gap-3">
                      <span className="text-base font-bold text-black md:text-[18px]">
                        거래 유형
                      </span>
                      <span className="rounded-xl bg-[#DDE8DF] px-3 py-1.5 text-base font-bold text-primary-300 md:px-4 md:py-2 md:text-[18px]">
                        {rentTypeName || '-'}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <div className="rounded-2xl border border-[#D9DEE3] bg-white p-5 shadow-sm md:p-6">
                <h3 className="text-base font-bold text-black md:text-[18px]">
                  생활 편의시설
                </h3>
                <p className="mt-3 text-sm text-[#4B5563] md:text-[15px]">
                  주변에서 이용할 수 있는 주요 생활시설
                </p>

                <div className="mt-6 space-y-3">
                  <div className="flex items-center justify-between gap-3">
                    <span className="text-base text-black md:text-[18px]">
                      편의점
                    </span>
                    <span className="rounded-xl bg-[#DDE8DF] px-3 py-1.5 text-base font-bold text-primary-300 md:px-4 md:py-2 md:text-[18px]">
                      {formatCount(facility?.convenienceStoreCount)}
                    </span>
                  </div>

                  <div className="flex items-center justify-between gap-3">
                    <span className="text-base text-black md:text-[18px]">
                      카페
                    </span>
                    <span className="rounded-xl bg-[#DDE8DF] px-3 py-1.5 text-base font-bold text-primary-300 md:px-4 md:py-2 md:text-[18px]">
                      {formatCount(facility?.cafeCount)}
                    </span>
                  </div>

                  <div className="flex items-center justify-between gap-3">
                    <span className="text-base text-black md:text-[18px]">
                      병원
                    </span>
                    <span className="rounded-xl bg-[#DDE8DF] px-3 py-1.5 text-base font-bold text-primary-300 md:px-4 md:py-2 md:text-[18px]">
                      {formatCount(facility?.hospitalCount)}
                    </span>
                  </div>

                  <div className="flex items-center justify-between gap-3">
                    <span className="text-base font-semibold text-black md:text-[18px]">
                      합계
                    </span>
                    <span className="rounded-xl bg-[#DDE8DF] px-3 py-1.5 text-base font-bold text-primary-300 md:px-4 md:py-2 md:text-[18px]">
                      {formatCount(facility?.totalCount)}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div className="mt-6 grid gap-4 lg:grid-cols-3">
              <div className="rounded-2xl border border-[#D9DEE3] bg-white p-5 shadow-sm md:p-6">
                <h3 className="text-base font-bold text-black md:text-[18px]">
                  식비 지수
                </h3>
                <p className="mt-2 text-sm text-[#4B5563] md:text-[15px]">
                  서울 평균 대비 식비 수준
                </p>

                <div className="mt-6 space-y-3">
                  <div className="flex items-center justify-between gap-3">
                    <span className="text-base text-black md:text-[18px]">
                      서울 평균 식비
                    </span>
                    <span className="rounded-xl bg-[#DDE8DF] px-3 py-1.5 text-base font-bold text-primary-300 md:px-4 md:py-2 md:text-[18px]">
                      {livingCost?.seoulAvgFoodCost != null
                        ? `${livingCost.seoulAvgFoodCost.toLocaleString()}원`
                        : '-'}
                    </span>
                  </div>

                  <div className="flex items-center justify-between gap-3">
                    <span className="text-base text-black md:text-[18px]">
                      {region.sigunguName} 평균 식비
                    </span>
                    <span className="rounded-xl bg-[#DDE8DF] px-3 py-1.5 text-base font-bold text-primary-300 md:px-4 md:py-2 md:text-[18px]">
                      {livingCost?.selectedRegionFoodCost != null
                        ? `${(livingCost.selectedRegionFoodCost === 0 ? 19000 : livingCost.selectedRegionFoodCost).toLocaleString()}원`
                        : '-'}
                    </span>
                  </div>

                </div>
              </div>

              <div className="rounded-2xl border border-[#D9DEE3] bg-white p-5 shadow-sm md:p-6">
                <h3 className="text-base font-bold text-black md:text-[18px]">
                  평균 통근 시간
                </h3>
                <p className="mt-2 text-sm text-[#4B5563] md:text-[15px]">
                  주요 업무지 기준 평균 이동 시간
                </p>

                <div className="mt-6 space-y-3">
                  <div className="flex items-center justify-between gap-3">
                    <span className="text-base text-black md:text-[18px]">통근 시간</span>
                    <span className="rounded-xl bg-[#DDE8DF] px-3 py-1.5 text-base font-bold text-primary-300 md:px-4 md:py-2 md:text-[18px]">
                      {formatMinutesToHourMinute(commute?.avgCommuteMinutes)}
                    </span>
                  </div>
                </div>
              </div>

              <div className="rounded-2xl border border-[#D9DEE3] bg-white p-5 shadow-sm md:p-6">
                <h3 className="text-base font-bold text-black md:text-[18px]">
                  매물 수
                </h3>
                <p className="mt-2 text-sm text-[#4B5563] md:text-[15px]">
                  조건에 맞는 현재 확인 가능한 매물 수
                </p>

                <div className="mt-6">
                  <span className="rounded-xl bg-[#DDE8DF] px-3 py-1.5 text-base font-bold text-primary-300 md:px-4 md:py-2 md:text-[18px]">
                    {region.hasMatchingHouses ? formatCount(region.matchingHouseCount) : '없음'}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div className="border-t border-[#E5E7EB] bg-white px-5 py-4 md:px-6 md:py-5">
            <Button
              label="이 지역 매물보기"
              size="md"
              isActive
              fullWidth
              onClick={() => onClickViewListings?.(region)}
              className="h-14 rounded-2xl text-base font-bold md:h-16 md:text-[18px]"
            />
          </div>
        </div>
      </div>
    </div>
  );
}