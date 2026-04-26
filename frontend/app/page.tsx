import HeroSection from "@/components/features/home/HeroSection";
import AveragePriceSection from "@/components/features/home/AveragePriceSection";
import PopularListingSection from "@/components/features/home/PopularListingSection";
import Footer from "@/components/features/home/Footer";
import PopularRegion from "@/components/features/home/PopularRegion";


export default function Page() {
  return (
    <main className="bg-white">
      <HeroSection />
      <AveragePriceSection />
      <PopularListingSection />
      <PopularRegion />
      <Footer />
    </main>
  );
}