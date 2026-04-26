'use client';

import {
  RiStore2Line,
  RiHospitalLine,
  RiBusLine,
} from 'react-icons/ri';
import clsx from 'clsx';

export type InfraCategory = '편의시설' | '의료시설' | '교통시설';

type Props = {
  isVisible: boolean;
  selectedCategory: InfraCategory | null;
  onSelect: (category: InfraCategory) => void;
  className?: string;
};

const menuItems: {
  key: InfraCategory;
  label: string;
  icon: React.ReactNode;
}[] = [
  {
    key: '편의시설',
    label: '편의시설',
    icon: <RiStore2Line className="h-[18px] w-[18px]" />,
  },
  {
    key: '의료시설',
    label: '의료시설',
    icon: <RiHospitalLine className="h-[18px] w-[18px]" />,
  },
  {
    key: '교통시설',
    label: '교통시설',
    icon: <RiBusLine className="h-[18px] w-[18px]" />,
  },
];

export default function HouseInfraFloatingMenu({
  isVisible,
  selectedCategory,
  onSelect,
  className,
}: Props) {
  return (
    <aside
      className={clsx(
        'absolute bottom-6 z-30 transition-all duration-300 ease-in-out',
        isVisible
          ? 'pointer-events-auto translate-x-0 opacity-100'
          : 'pointer-events-none translate-x-3 opacity-0',
        className,
      )}
    >
      <div className="divide-y divide-gray-200 overflow-hidden rounded-[10px] border border-gray-200 bg-white shadow-md">
        {menuItems.map((item) => {
          const isActive = selectedCategory === item.key;

          return (
            <button
              key={item.key}
              type="button"
              onClick={() => onSelect(item.key)}
              className={clsx(
                'flex w-[56px] flex-col items-center justify-center gap-1.5 px-1.5 py-2.5 text-center transition-colors',
                isActive ? 'bg-emerald-50' : 'bg-white',
                'hover:bg-emerald-50',
              )}
            >
              <div
                className={clsx(
                  'transition-colors',
                  isActive ? 'text-emerald-600' : 'text-gray-700',
                )}
              >
                {item.icon}
              </div>

              <span
                className={clsx(
                  'break-keep text-[10px] font-semibold leading-[1.2]',
                  isActive ? 'text-emerald-700' : 'text-gray-900',
                )}
              >
                {item.label}
              </span>
            </button>
          );
        })}
      </div>
    </aside>
  );
}