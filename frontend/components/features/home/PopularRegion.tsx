import Link from "next/link";

type Region = {
  name: string;
  description: string;
};

const regions: Region[] = [
  {
    name: "강남구",
    description: "업무지구와 생활 인프라가 밀집된 지역",
  },
  {
    name: "마포구",
    description: "대학가와 상권이 어우러진 지역",
  },
  {
    name: "성동구",
    description: "주거와 트렌디한 상권이 공존하는 지역",
  },
  {
    name: "동대문구",
    description: "교통과 생활 편의성이 좋은 지역",
  },
];

export default function PopularRegion() {
  return (
    <section className="w-full bg-white py-14 md:py-16">
      <div className="mx-auto max-w-[1200px] px-6">
        <div className="mb-8">
          <h2 className="text-2xl font-semibold text-gray-900">
            서울 인기 지역
          </h2>
        </div>

        <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-4">
          {regions.map((region) => (
            <Link
              key={region.name}
              href={`/search?region=${encodeURIComponent(region.name)}`}
              className="group relative overflow-hidden rounded-2xl border border-gray-200 bg-white p-6 transition-all duration-300 hover:-translate-y-1 hover:border-[#2b7f59]/30 hover:shadow-[0_16px_40px_rgba(16,24,40,0.08)]"
            >
              {}
              <div className="absolute inset-0 bg-gradient-to-br from-[#f7fbf8] via-white to-[#f3fcf8]" />

              {}
              <div className="relative z-10">
                <div className="mb-8 flex items-start justify-between gap-3">
                  <div>
                    <p className="text-xl font-semibold text-gray-900">
                      {region.name}
                    </p>
                    <p className="mt-2 text-sm leading-6 text-gray-600">
                      {region.description}
                    </p>
                  </div>

                  <span className="shrink-0 rounded-full bg-[#eef7f1] px-3 py-1 text-xs font-medium text-[#2b7f59]">
                    인기
                  </span>
                </div>

                <div className="relative z-10 flex items-end justify-end">
                  <span className="text-sm font-medium text-[#2b7f59] transition-transform duration-300 group-hover:translate-x-1">
                    둘러보기 →
                  </span>
                </div>
              </div>
            </Link>
          ))}
        </div>
      </div>
    </section>
  );
}