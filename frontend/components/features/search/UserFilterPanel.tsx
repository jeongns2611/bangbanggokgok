'use client';

import { useEffect, useMemo, useState } from 'react';
import SelectBox from '@/components/common/SelectBox';
import Button from '@/components/common/Button';
import DoubleRangeSlider from '@/components/common/RangeSlider/DoubleRangeSlider';
import SingleRangeSlider from '@/components/common/RangeSlider/SingleRangeSlider';

const HOUSE_TYPE_OPTIONS = [
  '전체',
  '오피스텔',
  '아파트',
  '단독/다가구(원룸)',
  '연립/다세대(빌라)',
] as const;

const RENT_TYPE_OPTIONS = ['전체', '전세', '월세'] as const;
const FLOOR_OPTIONS = ['전체', '반지하', '1층', '지상층'] as const;

const SIDO_OPTIONS = [
  { label: '서울특별시', value: '서울특별시' },
];

const SIGUNGU_MAP: Record<string, { label: string; value: string }[]> = {
  서울특별시: [
    { label: '강남구', value: '강남구' },
    { label: '강동구', value: '강동구' },
    { label: '강북구', value: '강북구' },
    { label: '강서구', value: '강서구' },
    { label: '관악구', value: '관악구' },
    { label: '광진구', value: '광진구' },
    { label: '구로구', value: '구로구' },
    { label: '금천구', value: '금천구' },
    { label: '노원구', value: '노원구' },
    { label: '도봉구', value: '도봉구' },
    { label: '동대문구', value: '동대문구' },
    { label: '동작구', value: '동작구' },
    { label: '마포구', value: '마포구' },
    { label: '서대문구', value: '서대문구' },
    { label: '서초구', value: '서초구' },
    { label: '성동구', value: '성동구' },
    { label: '성북구', value: '성북구' },
    { label: '송파구', value: '송파구' },
    { label: '양천구', value: '양천구' },
    { label: '영등포구', value: '영등포구' },
    { label: '용산구', value: '용산구' },
    { label: '은평구', value: '은평구' },
    { label: '종로구', value: '종로구' },
    { label: '중구', value: '중구' },
    { label: '중랑구', value: '중랑구' },
  ],
};

type RangeValue = {
  minValue: number;
  maxValue: number;
};

export type FilterState = {
  sidoName: string;
  sigunguName: string;
  houseType: string[];
  rentType: string[];
  monthlyCost: RangeValue;
  deposit: RangeValue;
  managementCost: number;
  floorSize: RangeValue;
  floor: string[];
};

export const INITIAL_FILTER_STATE: FilterState = {
  sidoName: '서울특별시',
  sigunguName: '',
  houseType: ['전체'],
  rentType: ['전체'],
  monthlyCost: {
    minValue: 0,
    maxValue: 205,
  },
  deposit: {
    minValue: 0,
    maxValue: 50100,
  },
  managementCost: 50,
  floorSize: {
    minValue: 5,
    maxValue: 101,
  },
  floor: ['전체'],
};

type UserFilterPanelProps = {
  initialFilters?: FilterState;
  onSubmit?: (filters: FilterState) => void;
  onChange?: (filters: FilterState) => void;
};

export default function UserFilterPanel({
  initialFilters = INITIAL_FILTER_STATE,
  onSubmit,
  onChange,
}: UserFilterPanelProps) {
  const [filters, setFilters] = useState<FilterState>(initialFilters);

  useEffect(() => {
    setFilters(initialFilters);
  }, [initialFilters]);

  useEffect(() => {
    onChange?.(filters);
  }, [filters, onChange]);

  const sigunguOptions = useMemo(() => {
    if (!filters.sidoName) return [];
    return [...(SIGUNGU_MAP[filters.sidoName] || [])].sort((left, right) =>
      left.label.localeCompare(right.label, 'ko-KR'),
    );
  }, [filters.sidoName]);

  const isRegionValid = Boolean(filters.sidoName && filters.sigunguName);
  const isMonthlyRentSelected =
    filters.rentType.includes('월세') || filters.rentType.includes('전체');

  const toggleMultiSelectWithAll = (
    key: 'houseType' | 'rentType' | 'floor',
    value: string,
  ) => {
    setFilters((prev) => {
      const current = prev[key];

      if (value === '전체') {
        return { ...prev, [key]: ['전체'] };
      }

      if (current.includes('전체')) {
        return { ...prev, [key]: [value] };
      }

      let next: string[];
      if (current.includes(value)) {
        next = current.filter((v) => v !== value);
      } else {
        next = [...current, value];
      }

      const allOptions =
        key === 'houseType'
          ? HOUSE_TYPE_OPTIONS.filter((opt) => opt !== '전체')
          : key === 'rentType'
            ? RENT_TYPE_OPTIONS.filter((opt) => opt !== '전체')
            : FLOOR_OPTIONS.filter((opt) => opt !== '전체');

      const isAllSelected = allOptions.every((opt) => next.includes(opt));

      if (isAllSelected || next.length === 0) {
        return { ...prev, [key]: ['전체'] };
      }

      return { ...prev, [key]: next };
    });
  };

  const handleReset = () => {
    setFilters(INITIAL_FILTER_STATE);
  };

  const handleSubmit = () => {
    if (!isRegionValid) return;
    onSubmit?.(filters);
  };

  const isSelected = (
    key: 'houseType' | 'rentType' | 'floor',
    value: string,
  ) => filters[key].includes(value);

  const toggleButtonClass = (active: boolean) =>
    active
      ? '!border-primary-100 !bg-primary-100 !text-white'
      : '!border-[#D9DEE3] !bg-white !text-primary-600 hover:!bg-background-100';

  return (
    <div className="flex min-h-0 w-full flex-1 flex-col overflow-hidden rounded-b-[20px] border-x border-b border-primary-100 bg-white shadow-[0_8px_24px_rgba(0,0,0,0.08)]">
      <div className="min-h-0 flex-1 overflow-y-auto px-6 pb-6 pt-8">
        <section>
          <div className="mb-5 flex items-center gap-2">
            <h3 className="text-[18px] font-semibold text-black">
              지역 <span className="text-red-500">*</span>
            </h3>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <SelectBox
              placeholder="시 / 도 선택"
              options={SIDO_OPTIONS}
              value={filters.sidoName}
              onChange={(value) =>
                setFilters((prev) => ({
                  ...prev,
                  sidoName: value,
                  sigunguName: '',
                }))
              }
            />

            <SelectBox
              placeholder="구 / 군 선택"
              options={sigunguOptions}
              value={filters.sigunguName}
              onChange={(value) =>
                setFilters((prev) => ({
                  ...prev,
                  sigunguName: value,
                }))
              }
              disabled={!filters.sidoName}
            />
          </div>
        </section>

        <section className="mt-10">
          <h3 className="mb-5 text-[18px] font-semibold text-black">
            주거유형
          </h3>

          <div className="grid grid-cols-2 gap-3">
            {HOUSE_TYPE_OPTIONS.map((option) => (
              <Button
                key={option}
                label={option}
                size="md"
                fullWidth
                isActive={isSelected('houseType', option)}
                onClick={() => toggleMultiSelectWithAll('houseType', option)}
                className={`${option === '전체' ? 'col-span-2' : ''} ${toggleButtonClass(
                  isSelected('houseType', option),
                )}`}
              />
            ))}
          </div>
        </section>

        <section className="mt-10">
          <div className="mb-5 flex items-center gap-3">
            <h3 className="text-[18px] font-semibold text-black">계약 형태</h3>
            <span className="text-[14px] font-medium text-gray-400">
              중복 선택 가능
            </span>
          </div>

          <div className="grid grid-cols-3 gap-3">
            {RENT_TYPE_OPTIONS.map((option) => (
              <Button
                key={option}
                label={option}
                size="md"
                fullWidth
                isActive={isSelected('rentType', option)}
                onClick={() => toggleMultiSelectWithAll('rentType', option)}
                className={toggleButtonClass(isSelected('rentType', option))}
              />
            ))}
          </div>
        </section>

        {isMonthlyRentSelected && (
          <section className="mt-10">
            <h3 className="mb-5 text-[18px] font-semibold text-black">월세</h3>
            <DoubleRangeSlider
              min={0}
              max={205}
              step={5}
              minValue={filters.monthlyCost.minValue}
              maxValue={filters.monthlyCost.maxValue}
              unit="만원"
              onChange={(next) =>
                setFilters((prev) => ({
                  ...prev,
                  monthlyCost: next,
                }))
              }
            />
          </section>
        )}

        <section className="mt-10">
          <h3 className="mb-5 text-[18px] font-semibold text-black">
            보증금(전세금)
          </h3>
          <DoubleRangeSlider
            min={0}
            max={50100}
            step={100}
            minValue={filters.deposit.minValue}
            maxValue={filters.deposit.maxValue}
            unit="만원"
            onChange={(next) =>
              setFilters((prev) => ({
                ...prev,
                deposit: next,
              }))
            }
          />
        </section>

        <section className="mt-10">
          <h3 className="mb-5 text-[18px] font-semibold text-black">관리비</h3>
          <SingleRangeSlider
            min={0}
            max={50}
            step={1}
            value={filters.managementCost}
            unit="만원"
            onChange={(value) =>
              setFilters((prev) => ({
                ...prev,
                managementCost: value,
              }))
            }
          />
        </section>

        <section className="mt-10">
          <h3 className="mb-5 text-[18px] font-semibold text-black">전용면적 (㎡)</h3>
          <DoubleRangeSlider
            min={5}
            max={101}
            step={1}
            minValue={filters.floorSize.minValue}
            maxValue={filters.floorSize.maxValue}
            unit="㎡"
            onChange={(next) =>
              setFilters((prev) => ({
                ...prev,
                floorSize: next,
              }))
            }
          />
        </section>

        <section className="mt-10">
          <div className="mb-5 flex items-center gap-3">
            <h3 className="text-[18px] font-semibold text-black">층수</h3>
            <span className="text-[14px] font-medium text-gray-400">
              중복 선택 가능
            </span>
          </div>

          <div className="grid grid-cols-3 gap-3">
            {FLOOR_OPTIONS.map((option) => (
              <Button
                key={option}
                label={option}
                size="md"
                fullWidth
                isActive={isSelected('floor', option)}
                onClick={() => toggleMultiSelectWithAll('floor', option)}
                className={toggleButtonClass(isSelected('floor', option))}
              />
            ))}
          </div>
        </section>
      </div>

      <div className="flex shrink-0 gap-4 border-t border-gray-200 bg-white px-6 py-5">
        <Button
          label="초기화"
          size="lg"
          fullWidth
          onClick={handleReset}
          className="!border-[#D9DEE3] !bg-white !text-primary-600 hover:!bg-background-100"
        />

        <Button
          label="검색하기"
          size="lg"
          fullWidth
          onClick={handleSubmit}
          disabled={!isRegionValid}
          className="!border-primary-100 !bg-primary-100 !text-white disabled:!border-gray-200 disabled:!bg-gray-100 disabled:!text-gray-400"
        />
      </div>
    </div>
  );
}