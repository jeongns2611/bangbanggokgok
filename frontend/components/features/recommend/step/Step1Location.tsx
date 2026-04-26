'use client';

import type { ReactNode } from 'react';
import LocationSearchMap from '@/components/common/LocationSearchMap';

type Step1LocationProps = {
  needName: string;
  placeKeyword: string;
  commuteHour: string;
  commuteMinute: string;
  onChangeNeedName: (value: string) => void;
  onChangePlaceKeyword: (value: string) => void;
  onChangeCommuteHour: (value: string) => void;
  onChangeCommuteMinute: (value: string) => void;
  onSelectLocation: (
    lat: number,
    lng: number,
    placeName?: string,
    address?: string,
    jibunAddress?: string,
  ) => void;
  mapSlot?: ReactNode;
};

function TimeInput({
  value,
  onChange,
  placeholder,
}: {
  value: string;
  onChange: (value: string) => void;
  placeholder: string;
}) {
  return (
    <input
      type="number"
      min={0}
      value={value}
      onChange={(e) => onChange(e.target.value)}
      placeholder={placeholder}
      className="h-10 w-12 rounded-xl border border-[#D9DEE3] bg-[#F8FAF9] text-center text-sm font-semibold text-black outline-none placeholder:text-[#A0A7AE] focus:border-background-400 focus:bg-white"
    />
  );
}

export default function Step1Location({
  needName,
  placeKeyword,
  commuteHour,
  commuteMinute,
  onChangeNeedName,
  onChangePlaceKeyword,
  onChangeCommuteHour,
  onChangeCommuteMinute,
  onSelectLocation,
  mapSlot,
}: Step1LocationProps) {
  return (
    <section>
      <h2 className="text-[22px] font-extrabold tracking-[-0.02em] text-black sm:text-[25px]">
        STEP 1. 위치 및 통근 조건 설정
      </h2>

      <div className="mt-6">
        <h3 className="text-[16px] font-bold text-black">조건 이름</h3>
        <p className="mt-1 text-[14px] leading-6 text-[#4B5563]">
          저장할 조건 이름을 입력해주세요.
        </p>

        <input
          type="text"
          value={needName}
          onChange={(event) => onChangeNeedName(event.target.value)}
          placeholder="예: 출퇴근 45분 이내"
          className="mt-3 h-10 w-full max-w-96 rounded-xl border border-[#D9DEE3] bg-[#F8FAF9] px-4 text-sm font-medium text-black outline-none placeholder:text-[#9CA3AF] focus:border-background-400 focus:bg-white"
        />
      </div>

      <div className="mt-6">
        <h3 className="text-[16px] font-bold text-black">주요 활동 장소</h3>
        <p className="mt-1 text-[14px] leading-6 text-[#4B5563]">
          통근·통학 기준이 되는 위치를 선택하세요.
        </p>

        <div className="mt-3">
          <LocationSearchMap
            placeKeyword={placeKeyword}
            onChangePlaceKeyword={onChangePlaceKeyword}
            onSelectLocation={onSelectLocation}
            mapSlot={mapSlot}
          />
        </div>
      </div>

      <div className="mt-8">
        <h3 className="text-[16px] font-bold text-black">통근시간</h3>
        <p className="mt-1 text-[14px] leading-6 text-[#4B5563]">
          원하는 최대 통근 시간을 선택해주세요.
        </p>

        <div className="mt-3 flex items-center gap-3">
          <TimeInput
            value={commuteHour}
            onChange={onChangeCommuteHour}
            placeholder="0"
          />
          <span className="text-[14px] font-semibold text-black">시간</span>

          <TimeInput
            value={commuteMinute}
            onChange={onChangeCommuteMinute}
            placeholder="00"
          />
          <span className="text-[14px] font-semibold text-black">분</span>
        </div>
      </div>
    </section>
  );
}
