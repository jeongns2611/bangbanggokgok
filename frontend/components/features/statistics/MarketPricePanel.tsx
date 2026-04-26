'use client';

import { useMemo } from 'react';
import {
    Line,
    LineChart,
    ResponsiveContainer,
    Tooltip,
    XAxis,
    YAxis,
} from 'recharts';
import type { RegionMonthlyTrend } from '@/components/features/statistics/StatisticsDashboardClient';

type MarketPricePanelProps = {
    sidoName: string;
    sigunguName: string;
    trends: RegionMonthlyTrend[];
    loading: boolean;
    error: string | null;
    onRetry: () => void;
};

function formatYearMonth(value: number) {
    const year = Math.floor(value / 100);
    const month = value % 100;

    if (!year || !month) {
        return '-';
    }

    return `${String(year).slice(-2)}.${String(month).padStart(2, '0')}`;
}

function formatAverageLabel(amount: number) {
    return `평균 ${Math.round(amount).toLocaleString()}만원`;
}

const cards = [
    { label: '월세시세', dataKey: 'avgRent' as const },
    { label: '보증금시세', dataKey: 'avgDeposit' as const },
    { label: '전세시세', dataKey: 'avgJeonSae' as const },
];

export default function MarketPricePanel({
    trends,
    loading,
    error,
    onRetry,
}: MarketPricePanelProps) {

    const marketChartData = useMemo(() => {
        return trends.map((item) => ({
            month: formatYearMonth(item.baseYearMonth),
            avgRent: item.avgRent,
            avgDeposit: item.avgDeposit,
            avgJeonSae: item.avgJeonSae,
        }));
    }, [trends]);

    const averageByKey = useMemo(() => {
        if (!trends.length) {
            return {
                avgRent: 0,
                avgDeposit: 0,
                avgJeonSae: 0,
            };
        }

        const sum = trends.reduce(
            (acc, item) => {
                acc.avgRent += item.avgRent;
                acc.avgDeposit += item.avgDeposit;
                acc.avgJeonSae += item.avgJeonSae;
                return acc;
            },
            {
                avgRent: 0,
                avgDeposit: 0,
                avgJeonSae: 0,
            },
        );

        return {
            avgRent: sum.avgRent / trends.length,
            avgDeposit: sum.avgDeposit / trends.length,
            avgJeonSae: sum.avgJeonSae / trends.length,
        };
    }, [trends]);

    return (
        <section className="rounded-[8px] border border-neutral-200 bg-white px-5 py-5">
            <div className="mb-4 flex flex-col gap-1">
                <h2 className="text-[20px] font-bold text-black">지역 시세</h2>
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

            <div className="grid grid-cols-1 gap-8 lg:grid-cols-3">
                {cards.map((card) => (
                    <div key={card.label} className="flex flex-col gap-3">
                        <div className="flex items-center gap-2">
                            <span className="rounded-full bg-[#DDE6DD] px-4 py-1 text-[14px] font-semibold text-[#4F6B4F]">
                                {card.label}
                            </span>
                            <span className="text-base font-semibold text-black">
                                {formatAverageLabel(averageByKey[card.dataKey])}
                            </span>
                        </div>

                        <div className="h-[160px] w-full">
                            {loading ? (
                                <div className="flex h-full items-center justify-center rounded-[8px] bg-neutral-50 text-sm text-neutral-500">
                                    시세 데이터를 불러오는 중...
                                </div>
                            ) : marketChartData.length === 0 ? (
                                <div className="flex h-full items-center justify-center rounded-[8px] bg-neutral-50 text-sm text-neutral-500">
                                    조회된 시세 데이터가 없습니다.
                                </div>
                            ) : (
                                <ResponsiveContainer width="100%" height="100%">
                                    <LineChart data={marketChartData} margin={{ top: 20, right: 10, left: -20, bottom: 0 }}>
                                        <XAxis
                                            dataKey="month"
                                            tickLine={false}
                                            axisLine={false}
                                            tick={{ fontSize: 12, fill: '#BDBDBD' }}
                                        />
                                        <YAxis hide />
                                        <Tooltip />
                                        <Line
                                            type="linear"
                                            dataKey={card.dataKey}
                                            name={card.label}
                                            stroke="#6F58D9"
                                            strokeWidth={4}
                                            dot={{ r: 0 }}
                                            activeDot={{ r: 5 }}
                                        />
                                    </LineChart>
                                </ResponsiveContainer>
                            )}
                        </div>
                    </div>
                ))}
            </div>
        </section>
    );
}