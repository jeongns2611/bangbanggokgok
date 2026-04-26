import HalfGauge from '@/components/common/HalfGauge';
import InfoTooltip from '@/components/common/InfoTooltip';
import type { RegionSafetyStats } from '@/components/features/statistics/StatisticsDashboardClient';

type SafetyPanelProps = {
    data: RegionSafetyStats | null;
    loading: boolean;
    error: string | null;
    onRetry: () => void;
};

function formatCount(value: number | undefined) {
    return `${Number(value || 0).toLocaleString()}개`;
}

export default function SafetyPanel({ data, loading, error, onRetry }: SafetyPanelProps) {
    const safetyItems = [
        { label: 'CCTV', value: formatCount(data?.cctvCount) },
        { label: '가로등', value: formatCount(data?.streetLightCount) },
        { label: '치안시설', value: formatCount(data?.securityFacilityCount) },
    ];
    const compositeScore = Number(data?.compositeSafetyScore || 0);

    return (
        <section className="rounded-[8px] border border-neutral-200 bg-white px-5 py-5">
            <div className="mb-4 flex items-start justify-between">
                <div className="flex flex-col gap-1">
                    <h2 className="text-[20px] font-bold text-black">지역 안전도</h2>
                </div>

                <InfoTooltip text="지역구별 면적 대비 CCTV, 가로등, 치안시설 수 비율을 기준으로 점수를 산출해 지역의 안전 수준을 분석한 지표입니다." />
            </div>

            {error ? (
                <div className="mb-4 rounded-[8px] border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                    <p>{error}</p>
                    <button
                        type="button"
                        onClick={onRetry}
                        className="mt-2 rounded-md border border-red-300 bg-white px-3 py-1 text-xs font-semibold text-red-700"
                    >
                        다시 조회
                    </button>
                </div>
            ) : null}

            {loading ? (
                <div className="mb-6 flex h-[64px] items-center justify-center rounded-[8px] bg-neutral-50 text-sm text-neutral-500">
                    지역 안전도 데이터를 불러오는 중...
                </div>
            ) : null}

            <div className="mb-6 flex flex-col items-center gap-3">
                <span className="rounded-full bg-[#DDE6DD] px-4 py-1 text-[14px] font-semibold text-[#4F6B4F]">
                    안전 인프라 지수
                </span>

                <div className="flex w-full justify-center">
                    <HalfGauge score={compositeScore} size={160} orangeThreshold={40} greenThreshold={70} />
                </div>
            </div>

            <div className="grid grid-cols-1 gap-y-3 md:grid-cols-3">
                {safetyItems.map((item) => (
                    <div key={item.label} className="flex items-center gap-3">
                        <span className="min-w-[90px] rounded-full bg-[#DDE6DD] px-4 py-1 text-center text-[14px] font-semibold text-[#4F6B4F]">
                            {item.label}
                        </span>
                        <span className="text-base font-semibold text-black">{item.value}</span>
                    </div>
                ))}
            </div>
        </section>
    );
}