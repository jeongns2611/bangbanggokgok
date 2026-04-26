'use client';

import { useCallback, useEffect, useState } from 'react';
import StatisticsHero from '@/components/features/statistics/StatisticsHero';
import MarketPricePanel from '@/components/features/statistics/MarketPricePanel';
import TransactionTrendPanel from '@/components/features/statistics/TransactionTrendPanel';
import LivingCostPanel from '@/components/features/statistics/LivingCostPanel';
import LivingInfraPanel from '@/components/features/statistics/LivingInfraPanel';
import SafetyPanel from '@/components/features/statistics/SafetyPanel';

export type RegionMonthlyTrend = {
  baseYearMonth: number;
  avgDeposit: number;
  avgRentDeposit?: number;
  avgRent: number;
  avgJeonSae: number;
  avgJeonse?: number;
  transactionCount: number;
};

type RegionMonthlyResponse = {
  trends?: RegionMonthlyTrend[];
  data?: {
    trends?: RegionMonthlyTrend[];
  };
  message?: string;
};

type InfraMonthlyPriceItem = {
  baseYearMonth: number;
  averagePriceThousandWon: number;
};

type RegionInfraPayload = {
  trends?: Array<{
    baseYearMonth: number;
    porkBellyPrice?: number;
    kimbapPrice?: number;
    porkBellyAvgLast12Months?: number;
    kimbapAvgLast12Months?: number;
  }>;
  livingCostIndex?: {
    porkBellyIndex?: {
      avgLast12MonthsThousandWon?: number;
      monthlyAveragePrices?: InfraMonthlyPriceItem[];
    };
    kimbapIndex?: {
      avgLast12MonthsThousandWon?: number;
      monthlyAveragePrices?: InfraMonthlyPriceItem[];
    };
  };
  livingInfrastructure?: {
    convenienceStoreCount?: number;
    cafeCount?: number;
    hospitalCount?: number;
    laundryCount?: number;
  };
  regionSafety?: {
    cctvCount?: number;
    streetLightCount?: number;
    securityFacilityCount?: number;
    compositeSafetyScore?: number;
  };
};

type RegionInfraResponse = {
  code?: number;
  message?: string;
  data?: RegionInfraPayload;
  
  trends?: RegionInfraPayload['trends'];
  livingCostIndex?: RegionInfraPayload['livingCostIndex'];
  livingInfrastructure?: RegionInfraPayload['livingInfrastructure'];
  regionSafety?: RegionInfraPayload['regionSafety'];
};

export type LivingCostTrendItem = {
  baseYearMonth: number;
  porkBellyPrice: number;
  kimbapPrice: number;
};

export type LivingCostStats = {
  porkBellyAvgLast12Months: number;
  kimbapAvgLast12Months: number;
  trends: LivingCostTrendItem[];
};

export type LivingInfrastructureStats = {
  convenienceStoreCount: number;
  cafeCount: number;
  hospitalCount: number;
  laundryCount: number;
};

export type RegionSafetyStats = {
  cctvCount: number;
  streetLightCount: number;
  securityFacilityCount: number;
  compositeSafetyScore: number;
};

export default function StatisticsDashboardClient() {
  const [sido, setSido] = useState('서울특별시');
  const [sigungu, setSigungu] = useState('강남구');
  const [monthlyTrends, setMonthlyTrends] = useState<RegionMonthlyTrend[]>([]);
  const [monthlyLoading, setMonthlyLoading] = useState(false);
  const [monthlyError, setMonthlyError] = useState<string | null>(null);
  const [infraLoading, setInfraLoading] = useState(false);
  const [infraError, setInfraError] = useState<string | null>(null);
  const [livingCostStats, setLivingCostStats] = useState<LivingCostStats | null>(null);
  const [livingInfrastructureStats, setLivingInfrastructureStats] = useState<LivingInfrastructureStats | null>(null);
  const [regionSafetyStats, setRegionSafetyStats] = useState<RegionSafetyStats | null>(null);
  const [aiInsights, setAiInsights] = useState<string[]>([]);
  const [aiLoading, setAiLoading] = useState(false);

  const fetchMonthlyTrends = useCallback(async (signal?: AbortSignal) => {
    setMonthlyLoading(true);
    setMonthlyError(null);

    try {
      const params = new URLSearchParams({ sidoName: sido, sigunguName: sigungu });
      const response = await fetch(`/api/statistics/regions/monthly?${params.toString()}`, {
        method: 'GET',
        cache: 'no-store',
        signal,
      });

      const body: RegionMonthlyResponse | null = await response.json().catch(() => null);

      

      if (!response.ok) {
        throw new Error(body?.message || '과거 매물 시세 조회에 실패했습니다.');
      }

      const fetchedTrends = Array.isArray(body?.trends)
        ? body.trends
        : Array.isArray(body?.data?.trends)
          ? body.data.trends
          : [];

      const normalized = fetchedTrends
        .map((item) => ({
          baseYearMonth: Number(item.baseYearMonth),
          avgDeposit: Number(item.avgRentDeposit ?? item.avgDeposit) || 0,
          avgRentDeposit: Number(item.avgRentDeposit ?? item.avgDeposit) || 0,
          avgRent: Number(item.avgRent) || 0,
          avgJeonSae: Number(item.avgJeonSae ?? item.avgJeonse) || 0,
          avgJeonse: Number(item.avgJeonSae ?? item.avgJeonse) || 0,
          transactionCount: Number(item.transactionCount) || 0,
        }))
        .filter((item) => Number.isFinite(item.baseYearMonth) && item.baseYearMonth > 0)
        .sort((a, b) => a.baseYearMonth - b.baseYearMonth);

      
      setMonthlyTrends(normalized);
    } catch (fetchError) {
      if (fetchError instanceof DOMException && fetchError.name === 'AbortError') {
        return;
      }

      setMonthlyError(fetchError instanceof Error ? fetchError.message : '과거 매물 시세 조회에 실패했습니다.');
      setMonthlyTrends([]);
    } finally {
      setMonthlyLoading(false);
    }
  }, [sido, sigungu]);

  const fetchInfraStats = useCallback(async (signal?: AbortSignal) => {
    setInfraLoading(true);
    setInfraError(null);

    try {
      const params = new URLSearchParams({ sidoName: sido, sigunguName: sigungu });
      const response = await fetch(`/api/statistics/regions/infra?${params.toString()}`, {
        method: 'GET',
        cache: 'no-store',
        signal,
      });

      const body: RegionInfraResponse | null = await response.json().catch(() => null);

      

      if (!response.ok) {
        throw new Error(body?.message || '생활비/인프라/안전도 조회에 실패했습니다.');
      }

      const payload = body?.data ?? body;

      let normalizedCostTrends: LivingCostTrendItem[] = [];
      let porkBellyAvgLast12Months = 0;
      let kimbapAvgLast12Months = 0;

      if (Array.isArray(payload?.trends)) {
        normalizedCostTrends = payload.trends
          .map((item) => ({
            baseYearMonth: Number(item.baseYearMonth),
            porkBellyPrice: Number(item.porkBellyPrice) || 0,
            kimbapPrice: Number(item.kimbapPrice) || 0,
          }))
          .filter((item) => Number.isFinite(item.baseYearMonth) && item.baseYearMonth > 0)
          .sort((a, b) => a.baseYearMonth - b.baseYearMonth);

        porkBellyAvgLast12Months = Number(payload.trends[0]?.porkBellyAvgLast12Months) || 0;
        kimbapAvgLast12Months = Number(payload.trends[0]?.kimbapAvgLast12Months) || 0;
      } else {
        const porkMonthly = payload?.livingCostIndex?.porkBellyIndex?.monthlyAveragePrices ?? [];
        const kimbapMonthly = payload?.livingCostIndex?.kimbapIndex?.monthlyAveragePrices ?? [];

        const map = new Map<number, LivingCostTrendItem>();

        porkMonthly.forEach((item) => {
          const key = Number(item.baseYearMonth);
          if (!Number.isFinite(key) || key <= 0) return;
          map.set(key, {
            baseYearMonth: key,
            porkBellyPrice: Number(item.averagePriceThousandWon) || 0,
            kimbapPrice: map.get(key)?.kimbapPrice || 0,
          });
        });

        kimbapMonthly.forEach((item) => {
          const key = Number(item.baseYearMonth);
          if (!Number.isFinite(key) || key <= 0) return;
          map.set(key, {
            baseYearMonth: key,
            porkBellyPrice: map.get(key)?.porkBellyPrice || 0,
            kimbapPrice: Number(item.averagePriceThousandWon) || 0,
          });
        });

        normalizedCostTrends = Array.from(map.values()).sort((a, b) => a.baseYearMonth - b.baseYearMonth);
        porkBellyAvgLast12Months = Number(payload?.livingCostIndex?.porkBellyIndex?.avgLast12MonthsThousandWon) || 0;
        kimbapAvgLast12Months = Number(payload?.livingCostIndex?.kimbapIndex?.avgLast12MonthsThousandWon) || 0;
      }

      

      setLivingCostStats({
        porkBellyAvgLast12Months,
        kimbapAvgLast12Months,
        trends: normalizedCostTrends,
      });

      setLivingInfrastructureStats({
        convenienceStoreCount: Number(payload?.livingInfrastructure?.convenienceStoreCount) || 0,
        cafeCount: Number(payload?.livingInfrastructure?.cafeCount) || 0,
        hospitalCount: Number(payload?.livingInfrastructure?.hospitalCount) || 0,
        laundryCount: Number(payload?.livingInfrastructure?.laundryCount) || 0,
      });

      setRegionSafetyStats({
        cctvCount: Number(payload?.regionSafety?.cctvCount) || 0,
        streetLightCount: Number(payload?.regionSafety?.streetLightCount) || 0,
        securityFacilityCount: Number(payload?.regionSafety?.securityFacilityCount) || 0,
        compositeSafetyScore: Number(payload?.regionSafety?.compositeSafetyScore) || 0,
      });
    } catch (fetchError) {
      if (fetchError instanceof DOMException && fetchError.name === 'AbortError') {
        return;
      }

      setInfraError(fetchError instanceof Error ? fetchError.message : '생활비/인프라/안전도 조회에 실패했습니다.');
      setLivingCostStats(null);
      setLivingInfrastructureStats(null);
      setRegionSafetyStats(null);
    } finally {
      setInfraLoading(false);
    }
  }, [sido, sigungu]);

  const fetchAiInsights = useCallback(async (signal?: AbortSignal) => {
    setAiLoading(true);
    setAiInsights([]);
    try {
      const params = new URLSearchParams({ sigunguName: sigungu });
      const response = await fetch(`/api/ai/regions/monthly-trends?${params.toString()}`, {
        method: 'GET',
        cache: 'no-store',
        signal,
      });
      const body = await response.json().catch(() => null);
      if (response.ok && Array.isArray(body?.data?.insights)) {
        setAiInsights(body.data.insights);
      }
    } catch (err) {
      if (err instanceof DOMException && err.name === 'AbortError') return;
    } finally {
      setAiLoading(false);
    }
  }, [sigungu]);

  const fetchSequentialRegionStats = useCallback(async (signal?: AbortSignal) => {
    await fetchMonthlyTrends(signal);
    await fetchInfraStats(signal);
  }, [fetchInfraStats, fetchMonthlyTrends]);

  useEffect(() => {
    const controller = new AbortController();
    fetchSequentialRegionStats(controller.signal)
      .then(() => fetchAiInsights(controller.signal))
      .catch(() => {});

    return () => {
      controller.abort();
    };
  }, [fetchSequentialRegionStats, fetchAiInsights]);

  return (
    <>
      <StatisticsHero
        sido={sido}
        sigungu={sigungu}
        onSidoChange={setSido}
        onSigunguChange={setSigungu}
      />
      <MarketPricePanel
        sidoName={sido}
        sigunguName={sigungu}
        trends={monthlyTrends}
        loading={monthlyLoading}
        error={monthlyError}
        onRetry={() => fetchSequentialRegionStats()}
      />
      <TransactionTrendPanel
        trends={monthlyTrends}
        loading={monthlyLoading}
        error={monthlyError}
        onRetry={() => fetchSequentialRegionStats()}
        aiInsights={aiInsights}
        aiLoading={aiLoading}
      />
      <LivingCostPanel data={livingCostStats} loading={infraLoading} error={infraError} onRetry={() => fetchSequentialRegionStats()} />
      <LivingInfraPanel data={livingInfrastructureStats} loading={infraLoading} error={infraError} onRetry={() => fetchSequentialRegionStats()} />
      <SafetyPanel data={regionSafetyStats} loading={infraLoading} error={infraError} onRetry={() => fetchSequentialRegionStats()} />
    </>
  );
}
