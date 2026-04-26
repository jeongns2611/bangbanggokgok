'use client';

import { useEffect, useMemo, useState } from 'react';
import { IoClose } from 'react-icons/io5';
import SelectBox from '@/components/common/SelectBox';
import DoubleRangeSlider from '@/components/common/RangeSlider/DoubleRangeSlider';
import Button from '@/components/common/Button';
import LocationSearchMap from '@/components/common/LocationSearchMap';
import type { ConditionFormValues } from '@/app/mypage/conditions/page';
import { defaultConditionFormValues } from '@/app/mypage/conditions/page';

type ConditionModalProps = {
    isOpen: boolean;
    onClose: () => void;
    onSubmit: (values: ConditionFormValues) => void;
    initialData?: ConditionFormValues | null;
    mode?: 'create' | 'edit';
};

const CITY_OPTIONS = [
    { label: '서울특별시', value: 'seoul' },
];

const SEOUL_DISTRICT_OPTIONS = [
    '강남구',
    '강동구',
    '강북구',
    '강서구',
    '관악구',
    '광진구',
    '구로구',
    '금천구',
    '노원구',
    '도봉구',
    '동대문구',
    '동작구',
    '마포구',
    '서대문구',
    '서초구',
    '성동구',
    '성북구',
    '송파구',
    '양천구',
    '영등포구',
    '용산구',
    '은평구',
    '종로구',
    '중구',
    '중랑구',
];

const DISTRICT_MAP: Record<string, { label: string; value: string }[]> = {
    seoul: SEOUL_DISTRICT_OPTIONS.map((district) => ({
        label: district,
        value: district,
    })),
};

const CONTRACT_OPTIONS = ['전세', '월세'];
const HOUSING_TYPE_OPTIONS = [
    '단독/다가구(원룸)',
    '연립/다세대(빌라)',
    '오피스텔',
    '아파트',
];
const FLOOR_OPTIONS = ['반지하', '1층', '지상층'];
const ENVIRONMENT_OPTIONS = [
    '카페',
    '공원 많은 곳',
    '역세권',
    '편의시설',
    '치안 우수',
    '식당',
];

function FieldLabel({
    title,
    description,
}: {
    title: string;
    description?: string;
}) {
    return (
        <div className="mb-3">
            <h3 className="text-[15px] font-bold text-primary-black">{title}</h3>
            {description && <p className="mt-1 text-sm text-black/55">{description}</p>}
        </div>
    );
}

export default function ConditionModal({
    isOpen,
    onClose,
    onSubmit,
    initialData = null,
    mode = 'create',
}: ConditionModalProps) {
    const [conditionName, setConditionName] = useState(defaultConditionFormValues.conditionName);
    const [city, setCity] = useState(defaultConditionFormValues.city);
    const [district, setDistrict] = useState(defaultConditionFormValues.district);

    const [commuteHours, setCommuteHours] = useState(defaultConditionFormValues.commuteHours);
    const [commuteMinutes, setCommuteMinutes] = useState(defaultConditionFormValues.commuteMinutes);
    const [placeKeyword, setPlaceKeyword] = useState(
        defaultConditionFormValues.placeKeyword,
    );
    const [lat, setLat] = useState(defaultConditionFormValues.lat);
    const [lng, setLng] = useState(defaultConditionFormValues.lng);

    const [selectedContract, setSelectedContract] = useState<string[]>(
        defaultConditionFormValues.selectedContract,
    );
    const [selectedHousingTypes, setSelectedHousingTypes] = useState<string[]>(
        defaultConditionFormValues.selectedHousingTypes,
    );
    const [selectedFloors, setSelectedFloors] = useState<string[]>(
        defaultConditionFormValues.selectedFloors,
    );
    const [selectedEnvironments, setSelectedEnvironments] = useState<string[]>(
        defaultConditionFormValues.selectedEnvironments,
    );

    const [monthlyRentRange, setMonthlyRentRange] = useState(
        defaultConditionFormValues.monthlyRentRange,
    );

    const [depositRange, setDepositRange] = useState(defaultConditionFormValues.depositRange);

    const [minArea, setMinArea] = useState(defaultConditionFormValues.minArea);

    const districtOptions = useMemo(() => {
        return city
            ? [...(DISTRICT_MAP[city] ?? [])].sort((left, right) =>
                left.label.localeCompare(right.label, 'ko-KR'),
            )
            : [];
    }, [city]);

    const isMonthlyRentSelected = selectedContract.includes('월세');

    useEffect(() => {
        if (!isOpen) return;

        const handleEsc = (e: KeyboardEvent) => {
            if (e.key === 'Escape') onClose();
        };

        document.addEventListener('keydown', handleEsc);
        document.body.style.overflow = 'hidden';

        return () => {
            document.removeEventListener('keydown', handleEsc);
            document.body.style.overflow = 'auto';
        };
    }, [isOpen, onClose]);

    useEffect(() => {
        if (!isOpen) return;

        const source = initialData ?? defaultConditionFormValues;

        setConditionName(source.conditionName);
        setCity(source.city);
        setDistrict(source.district);
        setPlaceKeyword(source.placeKeyword);
        setLat(source.lat);
        setLng(source.lng);
        setCommuteHours(source.commuteHours);
        setCommuteMinutes(source.commuteMinutes);
        setSelectedContract(source.selectedContract);
        setMonthlyRentRange(source.monthlyRentRange);
        setDepositRange(source.depositRange);
        setSelectedHousingTypes(source.selectedHousingTypes);
        setMinArea(source.minArea);
        setSelectedFloors(source.selectedFloors);
        setSelectedEnvironments(source.selectedEnvironments);
    }, [isOpen, initialData]);

    useEffect(() => {
        if (!city) {
            setDistrict('');
            return;
        }

        const hasCurrentDistrict = districtOptions.some((option) => option.value === district);

        if (!hasCurrentDistrict) {
            setDistrict('');
        }
    }, [city, district, districtOptions]);

    const toggleSingle = (
        value: string,
        currentList: string[],
        setter: React.Dispatch<React.SetStateAction<string[]>>,
    ) => {
        setter(currentList.includes(value) ? [] : [value]);
    };

    const toggleMulti = (
        value: string,
        currentList: string[],
        setter: React.Dispatch<React.SetStateAction<string[]>>,
    ) => {
        setter(
            currentList.includes(value)
                ? currentList.filter((item) => item !== value)
                : [...currentList, value],
        );
    };

    const isFormComplete = useMemo(() => {
        const totalCommuteMinutes =
            (Number(commuteHours) || 0) * 60 + (Number(commuteMinutes) || 0);
        return (
            conditionName.trim() !== '' &&
            city !== '' &&
            district !== '' &&
            lat !== 0 &&
            lng !== 0 &&
            totalCommuteMinutes > 0 &&
            selectedContract.length > 0 &&
            selectedHousingTypes.length > 0 &&
            minArea.trim() !== '' &&
            selectedFloors.length > 0
        );
    }, [
        conditionName,
        city,
        district,
        lat,
        lng,
        commuteHours,
        commuteMinutes,
        selectedContract,
        selectedHousingTypes,
        minArea,
        selectedFloors,
    ]);

    const handleSubmit = () => {
        const payload: ConditionFormValues = {
            conditionName,
            city,
            district,
            placeKeyword,
            lat,
            lng,
            commuteHours,
            commuteMinutes,
            selectedContract,
            monthlyRentRange,
            depositRange,
            selectedHousingTypes,
            minArea,
            selectedFloors,
            selectedEnvironments,
        };

        onSubmit(payload);
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/35 px-4 py-8">
            <div className="relative max-h-[90vh] w-full max-w-[700px] overflow-y-auto border border-gray-200 bg-white shadow-[0_20px_60px_rgba(0,0,0,0.18)]">
                <div className="flex items-center justify-between border-b border-gray-200 px-6 py-4">
                    <h2 className="text-lg font-bold">
                        {mode === 'edit' ? '조건 편집' : '나의 조건 설정'}
                    </h2>
                    <button
                        type="button"
                        onClick={onClose}
                        aria-label="모달 닫기"
                        className="flex h-9 w-9 items-center justify-center rounded-md border border-gray-200 bg-white text-gray-500 transition hover:bg-gray-50 hover:text-gray-700"
                    >
                        <IoClose className="text-[20px]" />
                    </button>
                </div>

                <div className="px-7 pb-10 pt-6">
                    <input
                        type="text"
                        placeholder="조건 이름을 입력해주세요."
                        value={conditionName}
                        onChange={(e) => setConditionName(e.target.value)}
                        className="h-14 w-full border border-gray-200 bg-[#f7f7f7] px-4 text-[18px] font-bold text-primary-black outline-none placeholder:text-black/65 focus:border-background-300 focus:bg-white"
                    />

                    <div className="mt-5 border-t border-gray-200 pt-5">
                        <FieldLabel title="어느 지역에서 거주하고 싶으신가요?" />

                        <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
                            <SelectBox
                                placeholder="시 / 도 선택"
                                options={CITY_OPTIONS}
                                value={city}
                                onChange={setCity}
                                className="!w-full"
                            />

                            <SelectBox
                                placeholder="구 / 군 선택"
                                options={districtOptions}
                                value={district}
                                onChange={setDistrict}
                                disabled={!city}
                                className="!w-full"
                            />
                        </div>
                    </div>

                    <div className="mt-6">
                        <FieldLabel
                            title="통학 또는 통근 기준 위치를 입력해주세요."
                            description="동,로,길 기준이 되는 위치를 검색하세요."
                        />

                        <LocationSearchMap
                            placeKeyword={placeKeyword}
                            onChangePlaceKeyword={setPlaceKeyword}
                            selectedLat={lat}
                            selectedLng={lng}
                            onSelectLocation={(selectedLat, selectedLng, placeName, address) => {
                                const selectedAddress = address || placeName;
                                if (selectedAddress) {
                                    setPlaceKeyword(selectedAddress);
                                }
                                if (typeof selectedLat === 'number' && typeof selectedLng === 'number') {
                                    setLat(selectedLat);
                                    setLng(selectedLng);
                                }
                            }}
                            placeholder="예: 강남역, 서울역, 판교역"
                        />
                    </div>

                    <div className="mt-6">
                        <FieldLabel title="하루 통학·통근은 최대 몇 분까지 괜찮으신가요?" />

                        <div className="flex items-center gap-3">
                            <input
                                type="number"
                                placeholder="시간"
                                value={commuteHours}
                                onChange={(e) => setCommuteHours(e.target.value)}
                                className="h-11 w-[92px] border border-gray-200 bg-[#f7f7f7] px-3 text-center text-sm outline-none focus:border-background-300 focus:bg-white"
                            />
                            <input
                                type="number"
                                placeholder="분"
                                value={commuteMinutes}
                                onChange={(e) => setCommuteMinutes(e.target.value)}
                                className="h-11 w-[92px] border border-gray-200 bg-[#f7f7f7] px-3 text-center text-sm outline-none focus:border-background-300 focus:bg-white"
                            />
                        </div>
                    </div>

                    <div className="mt-6">
                        <FieldLabel
                            title="계약 형태"
                            description="원하는 계약 형태를 선택해주세요."
                        />

                        <div className="flex flex-wrap gap-3">
                            {CONTRACT_OPTIONS.map((option) => (
                                <Button
                                    key={option}
                                    label={option}
                                    size="sm"
                                    isActive={selectedContract.includes(option)}
                                    onClick={() => toggleSingle(option, selectedContract, setSelectedContract)}
                                    className="rounded-md px-4 text-sm font-medium"
                                />
                            ))}
                        </div>
                    </div>

                    <div
                        className={`mt-6 grid grid-cols-1 gap-6 ${isMonthlyRentSelected ? 'md:grid-cols-2' : ''
                            }`}
                    >
                        {isMonthlyRentSelected && (
                            <div>
                                <FieldLabel title="월세" description="월세 범위를 설정해주세요." />
                                <DoubleRangeSlider
                                    min={0}
                                    max={200}
                                    step={5}
                                    minValue={monthlyRentRange.minValue}
                                    maxValue={monthlyRentRange.maxValue}
                                    unit="만원"
                                    onChange={setMonthlyRentRange}
                                />
                            </div>
                        )}

                        <div>
                            <FieldLabel title={selectedContract.includes('전세') ? '전세' : '보증금'} description={selectedContract.includes('전세') ? '전세 범위를 설정해주세요.' : '보증금 범위를 설정해주세요.'} />
                            <DoubleRangeSlider
                                min={0}
                                    max={50000}
                                step={50}
                                minValue={depositRange.minValue}
                                maxValue={depositRange.maxValue}
                                unit="만원"
                                onChange={setDepositRange}
                            />
                        </div>
                    </div>

                    <div className="mt-6">
                        <FieldLabel title="선호하는 주거 유형이 있으신가요?" />

                        <div className="flex flex-wrap gap-3">
                            {HOUSING_TYPE_OPTIONS.map((option) => (
                                <Button
                                    key={option}
                                    label={option}
                                    size="sm"
                                    isActive={selectedHousingTypes.includes(option)}
                                    onClick={() =>
                                        toggleMulti(option, selectedHousingTypes, setSelectedHousingTypes)
                                    }
                                    className="rounded-md px-4 text-sm font-medium"
                                />
                            ))}
                        </div>
                    </div>

                    <div className="mt-6 grid grid-cols-1 gap-6 md:grid-cols-[1fr_1.2fr]">
                        <div>
                            <FieldLabel title="최소 연면적" />
                            <input
                                type="number"
                                value={minArea}
                                onChange={(e) => setMinArea(e.target.value)}
                                placeholder="예: 18(㎡)"
                                className="h-11 w-full border border-gray-200 bg-[#f7f7f7] px-3 text-sm outline-none focus:border-background-300 focus:bg-white"
                            />
                        </div>

                        <div>
                            <FieldLabel title="층 수" />
                            <div className="flex flex-wrap gap-3">
                                {FLOOR_OPTIONS.map((option) => (
                                    <Button
                                        key={option}
                                        label={option}
                                        size="sm"
                                        isActive={selectedFloors.includes(option)}
                                        onClick={() => toggleMulti(option, selectedFloors, setSelectedFloors)}
                                        className="rounded-md px-4 text-sm font-medium"
                                    />
                                ))}
                            </div>
                        </div>
                    </div>

                    <div className="mt-6">
                        <FieldLabel title="어떤 생활 환경을 선호하시나요?" />

                        <div className="flex flex-wrap gap-3">
                            {ENVIRONMENT_OPTIONS.map((option) => (
                                <Button
                                    key={option}
                                    label={option}
                                    size="sm"
                                    isActive={selectedEnvironments.includes(option)}
                                    onClick={() =>
                                        toggleMulti(option, selectedEnvironments, setSelectedEnvironments)
                                    }
                                    className="rounded-md px-4 text-sm font-medium"
                                />
                            ))}
                        </div>
                    </div>

                    <Button
                        label={mode === 'edit' ? '수정 완료' : '완료'}
                        type="button"
                        fullWidth
                        isActive={isFormComplete}
                        disabled={!isFormComplete}
                        onClick={handleSubmit}
                        className="mt-8 h-12 rounded-md text-base font-bold hover:brightness-95"
                    />
                </div>
            </div>
        </div>
    );
}