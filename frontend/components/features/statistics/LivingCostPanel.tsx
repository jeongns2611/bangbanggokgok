'use client';

import { useMemo } from 'react';
import {
    Area,
    AreaChart,
    CartesianGrid,
    Line,
    ResponsiveContainer,
    Tooltip,
    XAxis,
    YAxis,
} from 'recharts';
import InfoTooltip from '@/components/common/InfoTooltip';
import type { LivingCostStats } from '@/components/features/statistics/StatisticsDashboardClient';

type LivingCostPanelProps = {
    data: LivingCostStats | null;
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

function formatCostUnit(amount: number) {
    return `${Math.round(amount).toLocaleString()}천원`;
}

function formatAverageCost(amount: number) {
    return `평균 ${formatCostUnit(amount)}`;
}

const costCards = [
    {
        label: '삼겹살지수',
        dataKey: 'porkBellyPrice' as const,
        description: '외식 물가를 대표하는 삼겹살 가격의 변화 추이를 보여줍니다.',
    },
    {
        label: '김밥지수',
        dataKey: 'kimbapPrice' as const,
        description: '대표 간편식 메뉴인 김밥 가격의 변화 추이를 보여줍니다.',
    },
];

export default function LivingCostPanel({ data, loading, error, onRetry }: LivingCostPanelProps) {
    const foodTrendData = useMemo(() => {
        return (data?.trends ?? []).map((item) => ({
            month: formatYearMonth(item.baseYearMonth),
            porkBellyPrice: item.porkBellyPrice,
            kimbapPrice: item.kimbapPrice,
        }));
    }, [data]);

    const averageMap = useMemo(() => {
        return {
            porkBellyPrice: data?.porkBellyAvgLast12Months ?? 0,
            kimbapPrice: data?.kimbapAvgLast12Months ?? 0,
        };
    }, [data]);

    return (
        <section className="rounded-[8px] border border-neutral-200 bg-white px-5 py-5">
            <div className="mb-4 flex items-start justify-between">
                <div className="flex flex-col gap-1">
                    <h2 className="text-[20px] font-bold text-black">생활비 지수</h2>
                </div>

                <InfoTooltip text="지역 내 주요 외식·생활 품목의 평균 가격 추이를 바탕으로 생활비 수준을 비교한 참고 지표입니다." />
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

            <div className="grid grid-cols-1 gap-10 xl:grid-cols-2">
                {costCards.map((card) => (
                    <div key={card.label} className="flex flex-col gap-3">
                        <div className="flex items-center gap-3">
                            <span className="rounded-full bg-[#DDE6DD] px-4 py-1 text-[14px] font-semibold text-[#4F6B4F]">
                                {card.label}
                            </span>
                            <span className="text-base font-semibold text-black">
                                {formatAverageCost(averageMap[card.dataKey])}
                            </span>
                        </div>

                        <p className="text-sm font-medium leading-6 text-black">{card.description}</p>

                        <div className="h-[136px] w-full rounded-[10px] border border-neutral-200 bg-white p-3 shadow-sm">
                            {loading ? (
                                <div className="flex h-full items-center justify-center text-sm text-neutral-500">
                                    생활비 지수를 불러오는 중...
                                </div>
                            ) : foodTrendData.length === 0 ? (
                                <div className="flex h-full items-center justify-center text-sm text-neutral-500">
                                    생활비 지수 데이터가 없습니다.
                                </div>
                            ) : (
                                <ResponsiveContainer width="100%" height="100%">
                                    <AreaChart data={foodTrendData} margin={{ top: 10, right: 10, left: -15, bottom: 0 }}>
                                        <defs>
                                            <linearGradient id={`foodFill-${card.label}`} x1="0" y1="0" x2="0" y2="1">
                                                <stop offset="0%" stopColor="#5F7C5F" stopOpacity={0.22} />
                                                <stop offset="100%" stopColor="#5F7C5F" stopOpacity={0.04} />
                                            </linearGradient>
                                        </defs>

                                        <CartesianGrid vertical={false} stroke="#E5E7EB" />
                                        <XAxis
                                            dataKey="month"
                                            tickLine={false}
                                            axisLine={false}
                                            tick={{ fontSize: 11, fill: '#444' }}
                                        />
                                        <YAxis
                                            tickLine={false}
                                            axisLine={false}
                                            tick={{ fontSize: 11, fill: '#444' }}
                                            domain={card.dataKey === 'porkBellyPrice' ? [0, 30] : undefined}
                                        />
                                        <Tooltip
                                            formatter={(value) => [
                                                formatCostUnit(Number(value)),
                                                card.label,
                                            ]}
                                        />
                                        <Line
                                            type="monotone"
                                            dataKey={card.dataKey}
                                            name={card.label}
                                            stroke="#587357"
                                            strokeWidth={2.5}
                                            dot={{ r: 3.5, fill: '#587357', strokeWidth: 0 }}
                                            activeDot={{ r: 4.5 }}
                                        />
                                    </AreaChart>
                                </ResponsiveContainer>
                            )}
                        </div>
                    </div>
                ))}
            </div>
        </section>
    );
}