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

type CompareTableRow = {
  label: string;
  values: string[];
};

type CompareTableProps = {
  houses: ComparisonHouse[];
  rows: CompareTableRow[];
};

export default function CompareTable({
  houses,
  rows,
}: CompareTableProps) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-180 border-collapse">
        <thead>
          <tr className="border-b border-gray-300">
            <th className="w-45 px-4 py-3 text-left text-[15px] font-semibold text-gray-700">
              항목
            </th>
            {houses.map((house, index) => (
              <th
                key={house.houseId}
                className="px-4 py-3 text-center text-[15px] font-semibold text-gray-700"
              >
                매물 {index + 1}
              </th>
            ))}
          </tr>
        </thead>

        <tbody>
          {rows.map((row) => (
            <tr
              key={row.label}
              className="border-b border-gray-200 last:border-b-0"
            >
              <td className="px-4 py-4 text-[15px] font-medium text-gray-800">
                {row.label}
              </td>

              {row.values.map((value, index) => (
                <td
                  key={`${row.label}-${index}`}
                  className="px-4 py-4 text-center text-[15px] text-gray-600"
                >
                  {value}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}