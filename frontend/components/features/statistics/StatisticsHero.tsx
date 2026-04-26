'use client';

import SelectBox from '@/components/common/SelectBox';
import { seoulDistrictNames } from '@/components/features/recommend/step/SeoulDistrictMap';

const sidoOptions = [
    { label: '서울특별시', value: '서울특별시' },
];

const sigunguOptions = seoulDistrictNames.map((districtName) => ({
    label: districtName,
    value: districtName,
})).sort((left, right) => left.label.localeCompare(right.label, 'ko-KR'));

type StatisticsHeroProps = {
    sido: string;
    sigungu: string;
    onSidoChange: (value: string) => void;
    onSigunguChange: (value: string) => void;
};

export default function StatisticsHero({
    sido,
    sigungu,
    onSidoChange,
    onSigunguChange,
}: StatisticsHeroProps) {

    return (
        <section className="flex flex-col gap-1 md:flex-row md:items-start md:justify-between">
            <div className="flex flex-col gap-2">
                <h1 className="text-[28px] font-bold leading-tight text-black">지역 분석 통계</h1>
                <p className="text-sm text-neutral-700">
                    주거 비용, 생활비, 인프라, 안전 정보를 한눈에 확인해보세요.
                </p>
            </div>

            <div className="flex flex-col gap-2 sm:flex-row lg:min-w-[400px] lg:justify-end">
                <SelectBox
                    placeholder="시 / 도 선택"
                    options={sidoOptions}
                    value={sido}
                    onChange={onSidoChange}
                    className="w-[360px]"
                />
                <SelectBox
                    placeholder="구 / 군 선택"
                    options={sigunguOptions}
                    value={sigungu}
                    onChange={onSigunguChange}
                    className="w-[360px]"
                />
            </div>
        </section>
    );
}