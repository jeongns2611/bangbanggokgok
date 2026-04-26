import StatisticsDashboardClient from '@/components/features/statistics/StatisticsDashboardClient';
import Footer from '@/components/features/home/Footer';

export default function StatisticsPage() {
  return (
    <div className="min-h-screen bg-white">
      <main className="mx-auto mb-10 flex w-[84%] max-w-[1080px] flex-col gap-4 px-4 py-6">
        <StatisticsDashboardClient />
      </main>
      <Footer />
    </div>
  );
}