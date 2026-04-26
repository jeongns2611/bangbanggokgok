'use client';

import Button from '@/components/common/Button';
import DoubleRangeSlider from '@/components/common/RangeSlider/DoubleRangeSlider';

type HousingType =
    | '단독/다가구(원룸)'
    | '연립/다세대(빌라)'
    | '오피스텔'
    | '아파트';

type ContractType = '전세' | '월세';

type RangeValue = {
    minValue: number;
    maxValue: number;
};

type Step2HousingProps = {
    housingTypes: HousingType[];
    contractType: ContractType;
    selectedFloors: string[];
    minSize: string;
    monthlyRentRange: RangeValue;
    depositRange: RangeValue;
    onToggleHousingType: (value: HousingType) => void;
    onChangeContractType: (value: ContractType) => void;
    onToggleFloor: (value: string) => void;
    onChangeMinSize: (value: string) => void;
    onChangeMonthlyRentRange: (value: RangeValue) => void;
    onChangeDepositRange: (value: RangeValue) => void;
};

const housingOptions: HousingType[] = [
    '단독/다가구(원룸)',
    '연립/다세대(빌라)',
    '오피스텔',
    '아파트',
];

const contractOptions: ContractType[] = ['전세', '월세'];
const floorOptions = ['전체', '반지하', '1층', '지상층'];

export default function Step2Housing({
    housingTypes,
    contractType,
    selectedFloors,
    minSize,
    monthlyRentRange,
    depositRange,
    onToggleHousingType,
    onChangeContractType,
    onToggleFloor,
    onChangeMinSize,
    onChangeMonthlyRentRange,
    onChangeDepositRange,
}: Step2HousingProps) {
    const isAllFloorsSelected = selectedFloors.includes('전체');

    return (
        <section>
            <h2 className="text-[22px] font-extrabold tracking-[-0.02em] text-black sm:text-[25px]">
                STEP 2. 주거 유형 및 예산 설정
            </h2>

            <div className="mt-6 space-y-7">
                <div>
                    <div className="flex items-center gap-2">
                        <h3 className="text-[16px] font-bold text-black">주거 유형</h3>
                        <span className="text-[12px] font-medium text-[#6B7280]">(중복 선택)</span>
                    </div>
                    <p className="mt-1 text-[14px] leading-6 text-[#4B5563]">
                        원하는 주거 유형을 선택해주세요.
                    </p>

                    <div className="mt-3 flex flex-wrap gap-2.5">
                        {housingOptions.map((option) => (
                            <Button
                                key={option}
                                label={option}
                                size="sm"
                                isActive={housingTypes.includes(option)}
                                onClick={() => onToggleHousingType(option)}
                                className="h-9 rounded-xl px-3.5 text-sm font-semibold"
                            />
                        ))}
                    </div>
                </div>

                <div>
                    <h3 className="text-[16px] font-bold text-black">계약 형태</h3>
                    <p className="mt-1 text-[14px] leading-6 text-[#4B5563]">
                        원하는 계약 형태를 선택해주세요.
                    </p>

                    <div className="mt-3 flex flex-wrap gap-2.5">
                        {contractOptions.map((option) => (
                            <Button
                                key={option}
                                label={option}
                                size="sm"
                                isActive={contractType === option}
                                onClick={() => onChangeContractType(option)}
                                className="h-9 min-w-20 rounded-xl px-3.5 text-sm font-semibold"
                            />
                        ))}
                    </div>
                </div>

                <div className="grid gap-7 md:grid-cols-2">
                    {contractType === '월세' && (
                        <div>
                            <h3 className="text-[16px] font-bold text-black">월세</h3>
                            <p className="mb-3 mt-1 text-[14px] leading-6 text-[#4B5563]">
                                월세 범위를 설정해주세요.
                            </p>

                            <DoubleRangeSlider
                                min={0}
                                max={200}
                                step={1}
                                minValue={monthlyRentRange.minValue}
                                maxValue={monthlyRentRange.maxValue}
                                unit="만원"
                                onChange={onChangeMonthlyRentRange}
                            />
                        </div>
                    )}

                    <div>
                        <h3 className="text-[16px] font-bold text-black">{contractType === '전세' ? '전세' : '보증금'}</h3>
                        <p className="mb-3 mt-1 text-[14px] leading-6 text-[#4B5563]">
                            {contractType === '전세' ? '전세 범위를 설정해주세요.' : '최대 보증금을 입력해주세요.'}
                        </p>

                        <DoubleRangeSlider
                            min={0}
                            max={50000}
                            step={10}
                            minValue={depositRange.minValue}
                            maxValue={depositRange.maxValue}
                            unit="만원"
                            onChange={onChangeDepositRange}
                        />
                    </div>
                </div>

                <div className="grid gap-7 md:grid-cols-2">
                    <div>
                        <h3 className="text-[16px] font-bold text-black">최소 연면적</h3>
                        <p className="mt-1 text-[14px] leading-6 text-[#4B5563]">
                            최소 연면적(㎡)을 입력해주세요.
                        </p>

                        <div className="mt-3 w-full max-w-56">
                            <input
                                type="number"
                                min={0}
                                step={0.1}
                                value={minSize}
                                onChange={(event) => onChangeMinSize(event.target.value)}
                                placeholder="예: 20.5"
                                className="h-10 w-full rounded-xl border border-[#D9DEE3] bg-[#F8FAF9] px-4 text-sm font-medium text-black outline-none placeholder:text-[#9CA3AF] focus:border-background-400 focus:bg-white"
                            />
                        </div>
                    </div>

                    <div>
                        <h3 className="text-[16px] font-bold text-black">층수</h3>
                        <p className="mt-1 text-[14px] leading-6 text-[#4B5563]">
                            선호하는 층수를 선택해주세요.
                        </p>

                        <div className="mt-3 flex flex-wrap gap-2.5">
                            {floorOptions.map((option) => (
                                <Button
                                    key={option}
                                    label={option}
                                    size="sm"
                                    isActive={
                                        option === '전체'
                                            ? isAllFloorsSelected
                                            : !isAllFloorsSelected && selectedFloors.includes(option)
                                    }
                                    onClick={() => onToggleFloor(option)}
                                    className="h-9 min-w-20 rounded-xl px-3.5 text-sm font-semibold"
                                />
                            ))}
                        </div>
                    </div>
                </div>
            </div>
        </section>
    );
}