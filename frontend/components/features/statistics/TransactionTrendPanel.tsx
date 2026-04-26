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
import { HiSparkles } from 'react-icons/hi2';
import type { RegionMonthlyTrend } from '@/components/features/statistics/StatisticsDashboardClient';

type TransactionTrendPanelProps = {
    trends: RegionMonthlyTrend[];
    loading: boolean;
    error: string | null;
    onRetry: () => void;
    aiInsights: string[];
    aiLoading: boolean;
};

function formatYearMonth(value: number) {
    const year = Math.floor(value / 100);
    const month = value % 100;

    if (!year || !month) {
        return '-';
    }

    return `${String(year).slice(-2)}.${String(month).padStart(2, '0')}`;
}

type CustomTooltipProps = {
    active?: boolean;
    payload?: Array<{
        value: number;
        payload: {
            month: string;
            transactionCount: number;
        };
    }>;
    label?: string;
};

function CustomTooltip({ active, payload, label }: CustomTooltipProps) {
    if (!active || !payload || !payload.length) return null;

    return (
        <div className="rounded-[12px] border border-neutral-200 bg-white px-4 py-3 shadow-md">
            <p className="mb-2 text-[15px] font-bold text-black">{label}</p>
            <p className="text-[14px] text-neutral-700">거래 건수 : {payload[0].value.toLocaleString()}건</p>
        </div>
    );
}

export default function TransactionTrendPanel({ trends, loading, error, onRetry, aiInsights, aiLoading }: TransactionTrendPanelProps) {
    const trendData = useMemo(() => {
        return trends.map((item) => ({
            month: formatYearMonth(item.baseYearMonth),
            transactionCount: item.transactionCount,
        }));
    }, [trends]);

    const yMax = useMemo(() => {
        if (!trendData.length) {
            return 10;
        }

        const max = Math.max(...trendData.map((item) => item.transactionCount));
        return Math.max(Math.ceil(max * 1.2), 10);
    }, [trendData]);

    return (
        <section className="rounded-[8px] border border-neutral-200 bg-white px-5 py-5">
            <div className="mb-4 flex flex-col gap-1">
                <h2 className="text-[20px] font-bold text-black">매물 거래 동향</h2>
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

            <div className="grid grid-cols-1 gap-6 lg:grid-cols-[1.15fr_0.85fr]">
                <div className="flex min-h-[320px] items-center rounded-[16px] border border-neutral-200 bg-white px-4 py-4 shadow-sm">
                    <div className="h-[320px] w-full">
                        {loading ? (
                            <div className="flex h-full items-center justify-center rounded-[8px] bg-neutral-50 text-sm text-neutral-500">
                                거래 동향 데이터를 불러오는 중...
                            </div>
                        ) : trendData.length === 0 ? (
                            <div className="flex h-full items-center justify-center rounded-[8px] bg-neutral-50 text-sm text-neutral-500">
                                조회된 거래 동향 데이터가 없습니다.
                            </div>
                        ) : (
                            <ResponsiveContainer width="100%" height="100%">
                                <AreaChart data={trendData} margin={{ top: 16, right: 12, left: -12, bottom: 4 }}>
                                    <defs>
                                        <linearGradient id="transactionFill" x1="0" y1="0" x2="0" y2="1">
                                            <stop offset="0%" stopColor="#5F7C5F" stopOpacity={0.22} />
                                            <stop offset="100%" stopColor="#5F7C5F" stopOpacity={0.04} />
                                        </linearGradient>
                                    </defs>

                                    <CartesianGrid vertical={false} stroke="#E5E7EB" />
                                    <XAxis
                                        dataKey="month"
                                        tickLine={false}
                                        axisLine={false}
                                        tick={{ fontSize: 13, fill: '#444' }}
                                    />
                                    <YAxis
                                        tickLine={false}
                                        axisLine={false}
                                        tick={{ fontSize: 13, fill: '#444' }}
                                        domain={[0, yMax]}
                                    />
                                    <Tooltip
                                        cursor={{ stroke: '#D1D5DB', strokeWidth: 1 }}
                                        content={<CustomTooltip />}
                                    />
                                    <Area
                                        type="monotone"
                                        dataKey="transactionCount"
                                        stroke="none"
                                        fill="url(#transactionFill)"
                                    />
                                    <Line
                                        type="monotone"
                                        dataKey="transactionCount"
                                        stroke="#63785F"
                                        strokeWidth={3.5}
                                        dot={{ r: 4.5, fill: '#63785F', strokeWidth: 0 }}
                                        activeDot={{ r: 6, fill: '#63785F', stroke: '#fff', strokeWidth: 2 }}
                                    />
                                </AreaChart>
                            </ResponsiveContainer>
                        )}
                    </div>
                </div>

                <div className="flex min-h-[320px] flex-col rounded-[16px] border border-[#E7ECE7] bg-[#F8FAF8] px-5 py-5 shadow-sm">
                    <div className="mb-4 flex items-center gap-2">
                        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-[#E4EFE4]">
                            <HiSparkles className="text-[15px] text-[#4F6B4F]" />
                        </div>

                        <div>
                            <p className="text-[11px] font-semibold uppercase tracking-[0.08em] text-[#6F816F]">
                                AI Insight
                            </p>
                            <h3 className="text-[18px] font-bold text-black">AI 요약</h3>
                        </div>
                    </div>

                    <div className="flex flex-1 flex-col gap-2.5">
                        {aiLoading ? (
                            <div className="flex flex-1 items-center justify-center text-sm text-neutral-400">
                                AI 요약을 생성하는 중...
                            </div>
                        ) : aiInsights.length > 0 ? (
                            aiInsights.map((item, index) => (
                                <div
                                    key={index}
                                    className="rounded-[12px] border border-[#E4EAE4] bg-white px-4 py-3"
                                >
                                    <div className="flex items-start gap-2">
                                        <span className="mt-[7px] h-2 w-2 shrink-0 rounded-full bg-[#6C8467]" />
                                        <p className="text-[14px] font-medium leading-6 text-[#222]">{item}</p>
                                    </div>
                                </div>
                            ))
                        ) : (
                            <div className="flex flex-1 items-center justify-center text-sm text-neutral-400">
                                AI 요약 데이터가 없습니다.
                            </div>
                        )}
                    </div>

                    <div className="mt-3 rounded-[10px] bg-[#EEF4EE] px-3 py-2">
                        <p className="text-[12px] leading-5 text-[#5B6B5B]">
                            최근 1년 거래 데이터를 바탕으로 계절성과 가격 흐름을 요약한 참고 분석입니다.
                        </p>
                    </div>
                </div>
            </div>
        </section>
    );
}