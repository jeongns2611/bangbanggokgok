'use client';

import { useEffect, useState } from 'react';

import {
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';

type ChartPoint = {
  label: string;
  value: number;
};

const monthlyRentData: ChartPoint[] = [
  { label: "1월", value: 51 },
  { label: "2월", value: 52 },
  { label: "3월", value: 52 },
  { label: "4월", value: 53 },
  { label: "5월", value: 54 },
  { label: "6월", value: 55 },
];

const jeonseData: ChartPoint[] = [
  { label: "1월", value: 23100 },
  { label: "2월", value: 23200 },
  { label: "3월", value: 23400 },
  { label: "4월", value: 23500 },
  { label: "5월", value: 23600 },
  { label: "6월", value: 23800 },
];

type LineChartCardProps = {
  title: string;
  valueText: string;
  data: ChartPoint[];
  chartType: 'rent' | 'jeonse';
};

function formatYAxisValue(value: number, type: "rent" | "jeonse") {
  if (type === "rent") {
    return `${value}만`;
  }

  const eok = Math.floor(value / 10000);
  const man = value % 10000;

  if (eok > 0) {
    return man > 0 ? `${eok}억 ${man.toLocaleString()}만` : `${eok}억`;
  }

  return `${value.toLocaleString()}만`;
}

function LineChartCard({ title, valueText, data, chartType }: LineChartCardProps) {
  const [isMounted, setIsMounted] = useState(false);

  useEffect(() => {
    setIsMounted(true);
  }, []);

  return (
    <div className="rounded-2xl border border-gray-200 p-5 md:p-6">
      <div className="mb-5 flex items-end justify-between gap-4">
        <div>
          <p className="text-sm font-medium text-gray-500">{title}</p>
          <h3 className="mt-1 text-2xl font-semibold text-gray-900 md:text-[28px]">
            {valueText}
          </h3>
        </div>
        <span className="text-sm text-gray-400">최근 6개월</span>
      </div>

      <div className="h-[200px] w-full min-w-0">
        {!isMounted ? (
          <div className="h-full w-full rounded-md bg-gray-50" />
        ) : (
          <ResponsiveContainer width="100%" height="100%" minWidth={280} minHeight={200}>
            <LineChart data={data} margin={{ top: 10, right: 16, left: 0, bottom: 0 }}>
              <CartesianGrid vertical={false} stroke="#e5e7eb" strokeDasharray="4 4" />
              <XAxis
                dataKey="label"
                tickLine={false}
                axisLine={false}
                tick={{ fontSize: 12, fill: '#6b7280' }}
              />
              <YAxis
                tickLine={false}
                axisLine={false}
                tick={{ fontSize: 11, fill: '#9ca3af' }}
                tickFormatter={(v) => formatYAxisValue(v, chartType)}
                width={chartType === 'jeonse' ? 72 : 40}
              />
              <Tooltip
                formatter={(value) => {
                  const numericValue =
                    typeof value === 'number' ? value : Number(value ?? 0);
                  return [formatYAxisValue(numericValue, chartType), title];
                }}
                contentStyle={{ borderRadius: '8px', border: '1px solid #e5e7eb', fontSize: '13px' }}
              />
              <Line
                type="monotone"
                dataKey="value"
                stroke="#4d7c57"
                strokeWidth={2.5}
                dot={{ r: 4, fill: '#4d7c57', strokeWidth: 0 }}
                activeDot={{ r: 5 }}
              />
            </LineChart>
          </ResponsiveContainer>
        )}
      </div>
    </div>
  );
}

export default function AveragePriceSection() {
  return (
    <section className="w-full bg-white py-14 md:py-16">
      <div className="mx-auto max-w-[1200px] px-6">
        <div className="mb-8">
          <h2 className="text-2xl font-semibold text-gray-900">
            서울 평균 월세 · 전세 추이
          </h2>
        </div>

        <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
          <LineChartCard
            title="서울 평균 월세"
            valueText="55만원"
            data={monthlyRentData}
            chartType="rent"
          />
          <LineChartCard
            title="서울 평균 전세"
            valueText="2억 3,800만원"
            data={jeonseData}
            chartType="jeonse"
          />
        </div>
      </div>
    </section>
  );
}