import type { LivingInfrastructureStats } from '@/components/features/statistics/StatisticsDashboardClient';

type LivingInfraPanelProps = {
    data: LivingInfrastructureStats | null;
    loading: boolean;
    error: string | null;
    onRetry: () => void;
};

function formatCount(value: number | undefined) {
    return `${Number(value || 0).toLocaleString()}개`;
}

export default function LivingInfraPanel({ data, loading, error, onRetry }: LivingInfraPanelProps) {
    const infraItems = [
        { label: '편의점', value: formatCount(data?.convenienceStoreCount) },
        { label: '카페', value: formatCount(data?.cafeCount) },
        { label: '병원', value: formatCount(data?.hospitalCount) },
        { label: '세탁소', value: formatCount(data?.laundryCount) },
    ];

    return (
        <section className="rounded-[8px] border border-neutral-200 bg-white px-5 py-5">
            <div className="mb-4 flex flex-col gap-1">
                <h2 className="text-[20px] font-bold text-black">생활 인프라</h2>
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
                <div className="flex h-[64px] items-center justify-center rounded-[8px] bg-neutral-50 text-sm text-neutral-500">
                    생활 인프라 데이터를 불러오는 중...
                </div>
            ) : null}

            <div className="grid grid-cols-2 gap-y-3 md:grid-cols-4">
                {infraItems.map((item) => (
                    <div key={item.label} className="flex items-center gap-3">
                        <span className="min-w-[80px] rounded-full bg-[#DDE6DD] px-4 py-1 text-center text-[14px] font-semibold text-[#4F6B4F]">
                            {item.label}
                        </span>
                        <span className="text-base font-semibold text-black">{item.value}</span>
                    </div>
                ))}
            </div>
        </section>
    );
}