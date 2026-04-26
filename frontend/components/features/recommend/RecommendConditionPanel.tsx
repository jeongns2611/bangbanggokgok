'use client';

import { useRouter } from 'next/navigation';
import Button from '@/components/common/Button';
import SelectBox from '@/components/common/SelectBox';
import type { RecommendConditionItem } from '@/mock/recommendConditions';

type Props = {
  conditionOptions: RecommendConditionItem[];
  selectedConditionId: number | null;
  onChangeCondition: (conditionId: number) => void;
};

export default function RecommendConditionPanel({
  conditionOptions,
  selectedConditionId,
  onChangeCondition,
}: Props) {
  const router = useRouter();

  const selectOptions = conditionOptions.map((condition) => ({
    label: condition.name,
    value: String(condition.conditionId),
  }));

  return (
    <div className="absolute left-5 top-5 z-10 w-88 rounded-2xl bg-white/95 p-5 shadow-[0_16px_40px_rgba(0,0,0,0.12)] backdrop-blur-sm">
      <div className="flex items-center justify-between gap-3">
        <h1 className="text-lg font-bold text-black">내 조건 기반 추천</h1>
        <Button
          label="조건 추가"
          size="sm"
          onClick={() => router.push('/recommend?create=1')}
          className="ml-auto"
        />
      </div>

      <SelectBox
        placeholder="조건을 선택하세요"
        options={selectOptions}
        value={selectedConditionId == null ? '' : String(selectedConditionId)}
        onChange={(value) => {
          const parsed = Number(value);
          if (!Number.isNaN(parsed)) {
            onChangeCondition(parsed);
          }
        }}
        size="compact"
        className="mt-3"
      />
    </div>
  );
}
