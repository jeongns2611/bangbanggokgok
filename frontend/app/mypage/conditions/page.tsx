'use client';

import { useMemo, useState } from 'react';
import ConditionCard from '@/components/features/mypage/ConditionCard';
import ConditionModal from '@/components/features/mypage/ConditionModal';
import { HiOutlinePlus } from 'react-icons/hi2';

export type ConditionFormValues = {
  conditionName: string;
  city: string;
  district: string;
  placeKeyword: string;
  lat: number;
  lng: number;
  commuteHours: string;
  commuteMinutes: string;
  selectedContract: string[];
  monthlyRentRange: {
    minValue: number;
    maxValue: number;
  };
  depositRange: {
    minValue: number;
    maxValue: number;
  };
  selectedHousingTypes: string[];
  minArea: string;
  selectedFloors: string[];
  selectedEnvironments: string[];
};

export type SavedCondition = {
  id: number | string;
  isDefault: boolean;
  form: ConditionFormValues;
};

const CITY_LABEL_MAP: Record<string, string> = {
  seoul: '서울특별시',
  gyeonggi: '경기도',
  incheon: '인천광역시',
};

const CITY_VALUE_MAP: Record<string, string> = {
  서울: 'seoul',
  서울시: 'seoul',
  서울특별시: 'seoul',
  경기도: 'gyeonggi',
  인천광역시: 'incheon',
};

const DISTRICT_LABEL_MAP: Record<string, string> = {
  gangnam: '강남구',
  gwanak: '관악구',
  dongjak: '동작구',
  mapo: '마포구',
  suwon: '수원시',
  seongnam: '성남시',
  yongin: '용인시',
  yeonsu: '연수구',
  namdong: '남동구',
  bupyeong: '부평구',
};

const DISTRICT_VALUE_MAP: Record<string, string> = {
  gangnam: '강남구',
  gangdong: '강동구',
  gangbuk: '강북구',
  gangseo: '강서구',
  gwanak: '관악구',
  gwangjin: '광진구',
  guro: '구로구',
  geumcheon: '금천구',
  nowon: '노원구',
  dobong: '도봉구',
  dongdaemun: '동대문구',
  dongjak: '동작구',
  mapo: '마포구',
  seodaemun: '서대문구',
  seocho: '서초구',
  seongdong: '성동구',
  seongbuk: '성북구',
  songpa: '송파구',
  yangcheon: '양천구',
  yeongdeungpo: '영등포구',
  yongsan: '용산구',
  eunpyeong: '은평구',
  jongno: '종로구',
  jung: '중구',
  jungnang: '중랑구',
  suwon: '수원시',
  seongnam: '성남시',
  yongin: '용인시',
  yeonsu: '연수구',
  namdong: '남동구',
  bupyeong: '부평구',
};


const LIFESTYLE_TAG_TO_BACKEND: Record<string, string> = {
  '카페': '카페/디저트 많음',
  '공원 많은 곳': '편의시설 많음',
  '역세권': '역세권 (800m)',
  '편의시설': '편의시설 많음',
  '치안 우수': '치안 우수',
  '식당': '식당 많음',
};

const BACKEND_TO_LIFESTYLE_TAG: Record<string, string> = {
  '카페/디저트 많음': '카페',
  '편의시설 많음': '편의시설',
  '역세권 (800m)': '역세권',
  '치안 우수': '치안 우수',
  '식당 많음': '식당',
};

function toCityValue(value?: string) {
  if (!value) return '';
  return CITY_VALUE_MAP[value] || value;
}

function toDistrictValue(value?: string) {
  if (!value) return '';
  return DISTRICT_VALUE_MAP[value] || value;
}

function toEnvironmentValues(tags: unknown) {
  if (!Array.isArray(tags)) return [];
  return tags
    .map((tag) => (typeof tag === 'string' ? BACKEND_TO_LIFESTYLE_TAG[tag] || tag : ''))
    .filter((tag): tag is string => Boolean(tag));
}

function toFiniteNumber(value: unknown, fallback = 0) {
  const num = Number(value);
  return Number.isFinite(num) ? num : fallback;
}

function toTargetAddress(value: unknown) {
  if (typeof value !== 'string') return '';
  return value.trim();
}

export const defaultConditionFormValues: ConditionFormValues = {
  conditionName: '',
  city: '',
  district: '',
  placeKeyword: '',
  lat: 0,
  lng: 0,
  commuteHours: '',
  commuteMinutes: '',
  selectedContract: ['전세'],
  monthlyRentRange: {
    minValue: 0,
    maxValue: 200,
  },
  depositRange: {
    minValue: 0,
    maxValue: 50000,
  },
  selectedHousingTypes: [],
  minArea: '',
  selectedFloors: [],
  selectedEnvironments: [],
};



function formatRegion(form: ConditionFormValues) {
  const districtLabel = DISTRICT_LABEL_MAP[form.district];
  const cityLabel = CITY_LABEL_MAP[form.city];

  if (districtLabel) return [districtLabel];
  if (form.district) return [form.district];
  if (cityLabel) return [cityLabel];
  if (form.city) return [form.city];
  return [];
}

function formatPropertyTypes(types: string[]) {
  if (!types.length) return ['주거 유형 미설정'];
  return types;
}

function formatPriceRange(form: ConditionFormValues) {
  const depositText = `보증금 ${form.depositRange.maxValue.toLocaleString()}만원 이하`;

  if (form.selectedContract.includes('월세')) {
    return `${depositText} · 월세 ${form.monthlyRentRange.maxValue.toLocaleString()}만원 이하`;
  }

  return depositText;
}

function formatExtraConditions(form: ConditionFormValues) {
  return form.selectedEnvironments ?? [];
}


import { useEffect } from 'react';
import { fetchWithAuth } from '@/lib/fetchWithAuth';

export default function ConditionsPage() {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [conditions, setConditions] = useState<SavedCondition[]>([]);
  const [editingCondition, setEditingCondition] = useState<SavedCondition | null>(null);

  
  useEffect(() => {
    const fetchConditions = async () => {
      try {
        const res = await fetchWithAuth('/api/recommend/conditions');
        const data = await res.json();
        
        
        
        const conditionsArr = data.data?.conditions || [];
        if (Array.isArray(conditionsArr)) {
          setConditions(
            conditionsArr.map((item: any) => {
              const userNeed = item.userNeed || item.uesrNeed || {};
              return {
                id: item.id,
                isDefault: Boolean(item.isDefaultNeed ?? item.isDefualtNeed ?? false),
                form: {
                  conditionName: item.name || '',
                  city: toCityValue(userNeed.sidoName),
                  district: toDistrictValue(userNeed.sigunguName),
                  placeKeyword: userNeed.targetAddress || '',
                  lat: userNeed.lat || 0,
                  lng: userNeed.lng || 0,
                  commuteHours: '',
                  commuteMinutes: '',
                  selectedContract: [userNeed.rentType || ''],
                  monthlyRentRange: {
                    minValue: 0,
                    maxValue: userNeed.monthlyRentMax || 0,
                  },
                  depositRange: {
                    minValue: 0,
                    maxValue: userNeed.depositMax || 0,
                  },
                  selectedHousingTypes: userNeed.housingTypes || [],
                  minArea: '',
                  selectedFloors: userNeed.floors || [],
                  selectedEnvironments: toEnvironmentValues(userNeed.lifestyleTags),
                },
              };
            })
          );
        }
      } catch (e) {
        
        
      }
    };
    fetchConditions();
  }, []);

  const modalMode = editingCondition ? 'edit' : 'create';

  const sortedConditions = useMemo(() => {
    return [...conditions].sort((a, b) => {
      if (a.isDefault === b.isDefault) return 0;
      return a.isDefault ? -1 : 1;
    });
  }, [conditions]);

  const handleOpenCreateModal = () => {
    setEditingCondition(null);
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingCondition(null);
  };

  const handleEdit = (id: number | string) => {
    
    (async () => {
      try {
        const res = await fetchWithAuth(`/api/recommend/conditions/${id}`);
        const data = await res.json();
        
        
        
        const detail = data.data;
        if (!detail) return;
        const d = detail.userNeed || detail;
        const sidoName = detail.sidoName || d.sidoName || '';
        const sigunguName = detail.sigunguName || d.sigunguName || '';
        const targetAddress = toTargetAddress(detail.targetAddress ?? d.targetAddress);

        
        
        
        const form = {
          conditionName: detail.name || detail.needName || d.name || d.needName || '',
          city: toCityValue(sidoName),
          district: toDistrictValue(sigunguName),
          placeKeyword: targetAddress,
          lat: toFiniteNumber(d.lat),
          lng: toFiniteNumber(d.lng),
          commuteHours: d.maxCommuteTime ? String(Math.floor(d.maxCommuteTime / 60)) : '',
          commuteMinutes: d.maxCommuteTime ? String(d.maxCommuteTime % 60) : '',
          selectedContract: [d.rentType || ''],
          monthlyRentRange: {
            minValue: toFiniteNumber(d.minMonthlyRent),
            maxValue: toFiniteNumber(d.maxMonthlyRent),
          },
          depositRange: {
            minValue: toFiniteNumber(d.minDeposit),
            maxValue: toFiniteNumber(d.maxDeposit),
          },
          selectedHousingTypes: d.housingTypes || [],
          minArea:
            d.minExclusiveSize != null
              ? String(d.minExclusiveSize)
              : d.minSize != null
                ? String(d.minSize)
                : '',
          selectedFloors: d.floors || [],
          selectedEnvironments: toEnvironmentValues(d.lifestyleTags),
        };
        
        setEditingCondition({ id, isDefault: false, form });
        setIsModalOpen(true);
      } catch (e) {
        
        
      }
    })();
  };

  const handleDelete = async (id: number | string) => {
    try {
      const res = await fetchWithAuth(`/api/recommend/conditions/${id}`, {
        method: 'DELETE',
      });
      if (!res.ok) {
        
        
        return;
      }
      setConditions((prev) => prev.filter((condition) => condition.id !== id));
    } catch (e) {
      
      
    }
  };

  const handleSetDefault = async (targetId: number | string) => {
    try {
      const res = await fetchWithAuth(`/api/recommend/conditions/set-default/${targetId}`, {
        method: 'PUT',
      });
      if (!res.ok) {
        
        
        return;
      }
      setConditions((prev) =>
        prev.map((condition) => ({
          ...condition,
          isDefault: condition.id === targetId,
        })),
      );
    } catch (e) {
      
      
    }
  };

  
  const handleSubmitCondition = async (formValues: ConditionFormValues) => {
    try {
      
      const sidoName = CITY_LABEL_MAP[formValues.city] || formValues.city;
      const sigunguName = DISTRICT_LABEL_MAP[formValues.district] || formValues.district;
      const conditionName = formValues.conditionName || `${sidoName} ${sigunguName}`;
      
      
      const lifestyleTags = formValues.selectedEnvironments.map(
        tag => LIFESTYLE_TAG_TO_BACKEND[tag] || tag
      );

      
      const payload = {
        needName: conditionName,
        lat: formValues.lat || 0,
        lng: formValues.lng || 0,
        targetAddress: formValues.placeKeyword || '',
        sidoName,
        sigunguName,
        maxCommuteTime: Number(formValues.commuteHours) * 60 + Number(formValues.commuteMinutes),
        rentType: formValues.selectedContract[0],
        maxDeposit: formValues.depositRange.maxValue >= 50000 ? 210000000 : formValues.depositRange.maxValue,
        maxMonthlyRent: formValues.monthlyRentRange.maxValue >= 200 ? 210000000 : formValues.monthlyRentRange.maxValue,
        minDeposit: formValues.depositRange.minValue,
        minMonthlyRent: formValues.monthlyRentRange.minValue,
        housingTypes: formValues.selectedHousingTypes,
        minSize: Number(formValues.minArea) || 0.1,
        lifestyleTags,
        floors: formValues.selectedFloors,
      };
      
      
      let result;
      if (modalMode === 'edit' && editingCondition) {
        
        const putRes = await fetchWithAuth(`/api/recommend/conditions/${editingCondition.id}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
        });
        result = await putRes.json();
        
        
      } else {
        
        const postRes = await fetchWithAuth('/api/recommend/conditions', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
        });
        result = await postRes.json();
        
        
      }
      
      const res = await fetchWithAuth('/api/recommend/conditions');
      const data = await res.json();
      const conditionsArr = data.data?.conditions || [];
      if (Array.isArray(conditionsArr)) {
        setConditions(
          conditionsArr.map((item: any) => {
            const userNeed = item.userNeed || item.uesrNeed || {};
            return {
              id: item.id,
              isDefault: Boolean(item.isDefaultNeed ?? item.isDefualtNeed ?? false),
              form: {
                conditionName: item.name || '',
                city: toCityValue(userNeed.sidoName),
                district: toDistrictValue(userNeed.sigunguName),
                placeKeyword: userNeed.targetAddress || '',
                lat: userNeed.lat || 0,
                lng: userNeed.lng || 0,
                commuteHours: '',
                commuteMinutes: '',
                selectedContract: [userNeed.rentType || ''],
                monthlyRentRange: {
                  minValue: 0,
                  maxValue: userNeed.monthlyRentMax || 0,
                },
                depositRange: {
                  minValue: 0,
                  maxValue: userNeed.depositMax || 0,
                },
                selectedHousingTypes: userNeed.housingTypes || [],
                minArea: '',
                selectedFloors: userNeed.floors || [],
                selectedEnvironments: toEnvironmentValues(userNeed.lifestyleTags),
              },
            };
          })
        );
      }
      handleCloseModal();
    } catch (e) {
      
      
    }
  };

  return (
    <section className="space-y-5">
      <div className="flex flex-col gap-4 rounded-2xl border border-gray-200 bg-white px-5 py-5 md:flex-row md:items-center md:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-[-0.03em] text-primary-black md:text-[28px]">
            매물 추천 조건
          </h1>
          <p className="mt-1 text-sm text-gray-500">
            원하는 지역과 예산 조건을 저장하고 맞춤 추천을 받아보세요.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <button
            type="button"
            onClick={handleOpenCreateModal}
            className="inline-flex cursor-pointer items-center gap-1.5 rounded-xl bg-background-400 px-4 py-2.5 text-sm font-semibold text-white transition hover:brightness-95"
          >
            <HiOutlinePlus className="text-base" />
            조건 추가
          </button>
        </div>
      </div>

      <div className=" border border-gray-200 bg-white p-4">
        <div className="space-y-3">
          {sortedConditions.map((condition) => (
            <ConditionCard
              key={condition.id}
              id={condition.id}
              name={condition.form.conditionName}
              regions={formatRegion(condition.form)}
              propertyTypes={formatPropertyTypes(condition.form.selectedHousingTypes)}
              priceRange={formatPriceRange(condition.form)}
              extraConditions={formatExtraConditions(condition.form)}
              isDefault={condition.isDefault}
              onEdit={handleEdit}
              onDelete={handleDelete}
              onSetDefault={handleSetDefault}
            />
          ))}
        </div>
      </div>

      <ConditionModal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        onSubmit={handleSubmitCondition}
        initialData={editingCondition?.form ?? null}
        mode={modalMode}
      />
    </section>
  );
}