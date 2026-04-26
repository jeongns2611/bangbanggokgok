'use client';

import Button from '@/components/common/Button';
import SelectBox from '@/components/common/SelectBox';

type LifestyleOption =
    | '카페'
    | '편의시설'
    | '공원 많은 곳'
    | '치안 우수'
    | '역세권'
    | '식당';

type Option = {
    label: string;
    value: string;
};

type Step3LifestyleProps = {
    city: string;
    district: string;
    lifestylePreferences: LifestyleOption[];
    cityOptions: Option[];
    districtOptions: Option[];
    onChangeCity: (value: string) => void;
    onChangeDistrict: (value: string) => void;
    onToggleLifestylePreference: (value: LifestyleOption) => void;
};

const lifestyleOptions: LifestyleOption[] = [
    '카페',
    '편의시설',
    '공원 많은 곳',
    '치안 우수',
    '역세권',
    '식당',
];

export default function Step3Lifestyle({
    city,
    district,
    lifestylePreferences,
    cityOptions,
    districtOptions,
    onChangeCity,
    onChangeDistrict,
    onToggleLifestylePreference,
}: Step3LifestyleProps) {
    return (
        <section>
            <h2 className="text-[22px] font-extrabold tracking-[-0.02em] text-black sm:text-[25px]">
                STEP 3. 생활 스타일 및 지역 선호
            </h2>

            <div className="mt-6 space-y-7">
                <div>
                    <h3 className="text-[16px] font-bold text-black">
                        지역 선택 (선택 사항)
                    </h3>
                    <p className="mt-1 text-[14px] leading-6 text-[#4B5563]">
                        선택하지 않으면 조건에 맞는 지역을 추천합니다.
                    </p>

                    <div className="mt-3 grid gap-3 md:grid-cols-2">
                        <SelectBox
                            placeholder="시 / 도 선택"
                            options={cityOptions}
                            value={city}
                            onChange={onChangeCity}
                            className="w-full"
                        />

                        <SelectBox
                            placeholder="구 / 군 선택"
                            options={districtOptions}
                            value={district}
                            onChange={onChangeDistrict}
                            className="w-full"
                            disabled={!city}
                        />
                    </div>
                </div>

                <div>
                    <h3 className="text-[16px] font-bold text-black">
                        생활 환경 선호 (필수)
                    </h3>
                    <p className="mt-1 text-[14px] leading-6 text-[#4B5563]">
                        최소 1개 이상 선택해야 매물 보러가기가 활성화됩니다.
                    </p>

                    <div className="mt-3 flex flex-wrap gap-3">
                        {lifestyleOptions.map((option) => (
                            <Button
                                key={option}
                                label={option}
                                size="sm"
                                isActive={lifestylePreferences.includes(option)}
                                onClick={() => onToggleLifestylePreference(option)}
                                className="h-10 min-w-28 rounded-xl px-4 text-sm font-semibold"
                            />
                        ))}
                    </div>
                </div>
            </div>
        </section>
    );
}