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

type FacilityDistanceChartProps = {
  houses: ComparisonHouse[];
};

const DISTANCE_MAX = 800;

const facilityDistanceRows = [
  { label: '편의점', key: 'convenienceDist' },
  { label: '세탁소', key: 'laundryDist' },
  { label: '병원', key: 'hospitalDist' },
  { label: '카페', key: 'cafeDist' },
  { label: '약국', key: 'pharmacyDist' },
  { label: '지하철 역', key: 'subwayDist' },
] as const;

export default function FacilityDistanceChart({
  houses,
}: FacilityDistanceChartProps) {
  return (
    <div className="space-y-6">
      {facilityDistanceRows.map((row) => {
        const values = houses.map((house) => house.minDist[row.key]);
        const best = Math.min(...values);

        return (
          <div key={row.key} className="border-b border-gray-200 pb-4 last:border-b-0">
            <div className="mb-3 text-sm font-semibold text-gray-900">
              {row.label}
            </div>

            <div className="space-y-3">
              {houses.map((house, index) => {
                const distance = house.minDist[row.key];
                const width = Math.min((distance / DISTANCE_MAX) * 100, 100);

                const isBest = distance === best;

                return (
                  <div
                    key={`${row.key}-${index}`}
                    className="grid grid-cols-[60px_80px_1fr] items-center gap-4"
                  >
                    <span className="text-sm text-gray-600">
                      매물 {index + 1}
                    </span>

                    <span
                      className={`text-sm font-semibold ${
                        isBest
                          ? 'text-[var(--color-primary-100)]'
                          : 'text-gray-800'
                      }`}
                    >
                      {distance}m
                    </span>

                    <div className="h-3 rounded-full bg-gray-200">
                      <div
                        className={`h-3 rounded-full ${
                          isBest
                            ? 'bg-[var(--color-primary-100)]'
                            : 'bg-[var(--color-primary-100)]/40'
                        }`}
                        style={{ width: `${width}%` }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        );
      })}
    </div>
  );
}