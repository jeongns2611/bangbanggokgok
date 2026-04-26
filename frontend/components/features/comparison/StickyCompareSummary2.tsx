'use client';

type HouseImage = {
  image_url?: string | null;
  is_thumbnail?: boolean | null;
};

type CommuteData = {
  commuteTime: number;
  commuteDistance: number;
};

type InfraCount = {
  convenienceStoreCount: number;
  laundryCount: number;
  cafeCount: number;
  hospitalCount: number;
  pharmacyCount: number;
  busStopCount: number;
};

type MinDist = {
  convenienceDist: number;
  laundryDist: number;
  cafeDist: number;
  hospitalDist: number;
  pharmacyDist: number;
  subwayDist: number;
};

type DongStats = {
  cctvCount: number;
  streetLightCount: number;
  safetyFacilityCount: number;
  safetyScore: number;
  avgMeatPrice: number;
  avgMealPrice: number;
};

type ComparisonHouse = {
  images: HouseImage[];
  houseId: number;
  dong: string;
  houseType: string;
  rentType: string;
  houseStatus: string;
  deposit: number;
  monthlyCost: number;
  managementCost?: number | null;
  managementItems?: string | null;
  floorSize: number;
  buildYear?: number | null;
  description: string;
  viewCount: number;
  floor: string;
  commuteData: CommuteData;
  infraCount: InfraCount;
  minDist: MinDist;
  dongStats: DongStats;
};

type StickyCompareSummaryProps = {
  houses: ComparisonHouse[];
};

function formatPrice(value: number) {
  return value.toLocaleString('ko-KR');
}

function getThumbnail(images: HouseImage[]) {
  return (
    images.find((image) => image.is_thumbnail)?.image_url ||
    images[0]?.image_url ||
    ''
  );
}

function getShortDong(fullDong: string) {
  const parts = fullDong.split(' ');
  return parts[parts.length - 1] || fullDong;
}

export default function StickyCompareSummary({
  houses,
}: StickyCompareSummaryProps) {
  return (
    <div className="rounded-b-2xl border border-gray-200 bg-white/95 shadow-sm backdrop-blur">
      <div className="flex min-h-[72px] items-center">
        <div className="hidden shrink-0 border-r border-gray-100 px-5 text-sm font-semibold text-gray-500 md:flex md:w-[140px]">
          비교 중인 매물
        </div>

        <div className="flex flex-1 flex-col divide-y divide-gray-100 md:flex-row md:divide-x md:divide-y-0">
          {houses.map((house, index) => (
            <div
              key={house.houseId}
              className="flex min-w-0 flex-1 items-center gap-3 px-4 py-3"
            >
              <div className="flex h-12 w-12 shrink-0 overflow-hidden rounded-xl bg-gray-100">
                {getThumbnail(house.images) ? (
                  <img
                    src={getThumbnail(house.images)}
                    alt={`${getShortDong(house.dong)} 매물 이미지`}
                    className="h-full w-full object-cover"
                  />
                ) : (
                  <div className="h-full w-full bg-gray-200" />
                )}
              </div>

              <div className="min-w-0 flex-1">
                <div className="flex items-center gap-2">
                  <span className="shrink-0 rounded-full bg-background-100 px-2 py-0.5 text-xs font-semibold text-primary-600">
                    매물 {index + 1}
                  </span>
                  <p className="truncate text-sm font-semibold text-gray-900">
                    {getShortDong(house.dong)} · {house.houseType}
                  </p>
                </div>

                <p className="mt-1 truncate text-sm text-gray-700">
                  {house.rentType === '전세'
                    ? `전세 ${formatPrice(house.deposit)}만원`
                    : `보증금 ${formatPrice(house.deposit)} / 월세 ${formatPrice(house.monthlyCost)}만원`}
                </p>

                <p className="mt-0.5 truncate text-xs text-gray-500">
                  {house.floorSize}㎡ · {house.floor}
                </p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}