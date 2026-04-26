import Image from 'next/image';

export default function HeroSection() {
  return (
    <section className="relative h-[550px] w-full overflow-hidden">
      <Image
        src="/mainpagehero.jpg"
        alt="도시와 집이 보이는 실내 풍경"
        fill
        priority
        className="object-cover"
      />

      <div className="absolute inset-0 bg-gradient-to-b from-black/55 via-black/25 to-black/35" />

      <div className="absolute inset-0 flex items-center justify-center px-6 pt-16 text-center">
        <h1 className="text-4xl font-bold leading-snug text-white drop-shadow-[0_4px_16px_rgba(0,0,0,0.35)] md:text-6xl">
          나에게 맞는 지역과 집을
          <br />
          데이터로 쉽게 찾다
        </h1>
      </div>
    </section>
  );
}
