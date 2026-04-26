'use client';

import HalfGauge from '@/components/common/HalfGauge';

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
  recommendReasons: string[];
  nonRecommendReasons: string[];
  scoreSummary?: {
    trafficScore: number;
    infraScore: number;
    safetyScore: number;
    needFitScore: number;
  };
};

type FinalEvaluationSectionProps = {
  houses: ComparisonHouse[];
  overallEvaluation?: string;
  aiLoading?: boolean;
};

export default function FinalEvaluationSection({
  houses,
  overallEvaluation,
  aiLoading = false,
}: FinalEvaluationSectionProps) {
  const evaluationSentences = splitSentences(overallEvaluation);

  return (
    <div className="space-y-5">
      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        {houses.map((house, index) => (
          <div
            key={house.houseId}
            className="rounded-2xl border border-gray-200 bg-white p-5"
          >
            <h4 className="mb-4 text-center text-sm font-semibold text-gray-700">
              매물 {index + 1}
            </h4>

            <div className="flex justify-center">
              <HalfGauge score={house.scoreSummary?.needFitScore ?? 0} />
            </div>

            <div className="mt-20 space-y-3">
              <ScoreBar
                label="교통"
                value={house.scoreSummary?.trafficScore ?? 0}
                colorClass="bg-[#2f7d32]"
              />
              <ScoreBar
                label="생활인프라"
                value={house.scoreSummary?.infraScore ?? 0}
                colorClass="bg-[#ef4444]"
              />
              <ScoreBar
                label="안전"
                value={house.scoreSummary?.safetyScore ?? 0}
                colorClass="bg-[#eab308]"
              />
            </div>
          </div>
        ))}
      </div>

      <div className="rounded-2xl bg-[#eef4ef] p-5">
        <p className="mb-3 text-sm font-semibold text-gray-800">💡 AI 평가</p>

        {aiLoading ? (
          <div className="flex min-h-[140px] items-center justify-center rounded-2xl bg-white px-6 text-center">
            <p className="text-xl font-semibold text-gray-700">
              AI 평가를 생성중이에요
            </p>
          </div>
        ) : (
          <ul className="list-disc space-y-1 pl-5 text-sm leading-6 text-gray-600">
            {(evaluationSentences.length > 0
              ? evaluationSentences
              : ['비교 데이터를 바탕으로 AI 평가를 불러오는 중입니다.']
            ).map((sentence) => (
              <li key={sentence}>{sentence}</li>
            ))}
          </ul>
        )}

        <div className="mt-4 grid grid-cols-1 gap-3 md:grid-cols-2">
          {houses.map((house, index) => (
            <div key={`recommend-${house.houseId}`} className="rounded-xl bg-white p-4">
              <p className="mb-2 text-sm font-semibold text-gray-800">
                매물 {index + 1} 추천 이유
              </p>
              <ul className="list-disc space-y-1 pl-5 text-sm text-gray-600">
                {(house.recommendReasons.length > 0 && !aiLoading
                  ? house.recommendReasons
                  : ['AI 추천 이유를 생성하는 중입니다.']
                )
                  .slice(0, 4)
                  .map((reason) => (
                    <li key={reason}>{reason}</li>
                  ))}
              </ul>
            </div>
          ))}
        </div>

        <div className="mt-3 grid grid-cols-1 gap-3 md:grid-cols-2">
          {houses.map((house, index) => (
            <div key={`non-recommend-${house.houseId}`} className="rounded-xl bg-white p-4">
              <p className="mb-2 text-sm font-semibold text-gray-800">
                매물 {index + 1} 비추천 이유
              </p>
              <ul className="list-disc space-y-1 pl-5 text-sm text-gray-600">
                {(house.nonRecommendReasons.length > 0 && !aiLoading
                  ? house.nonRecommendReasons
                  : ['AI 주의사항을 생성하는 중입니다.']
                )
                  .slice(0, 4)
                  .map((reason) => (
                    <li key={reason}>{reason}</li>
                  ))}
              </ul>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

function splitSentences(text?: string) {
  if (!text) {
    return [];
  }

  return text
    .split(/(?<=[.!?])\s+|\n+/)
    .map((sentence) => sentence.trim())
    .filter(Boolean);
}

type ScoreBarProps = {
  label: string;
  value: number;
  colorClass: string;
};

function ScoreBar({ label, value, colorClass }: ScoreBarProps) {
  return (
    <div className="flex items-center gap-3">
      <span className="w-20 shrink-0 text-sm text-gray-600">{label}</span>
      <div className="h-3 flex-1 rounded-full bg-gray-200">
        <div
          className={`h-3 rounded-full ${colorClass}`}
          style={{ width: `${value}%` }}
        />
      </div>
    </div>
  );
}