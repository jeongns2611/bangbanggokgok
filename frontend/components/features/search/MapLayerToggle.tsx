'use client';

import { PiPersonSimpleWalkBold, PiWarningCircleBold } from 'react-icons/pi';
import clsx from 'clsx';

type Props = {
  isVisible: boolean;
  showSafetyLayer: boolean;
  showCrimeLayer: boolean;
  onToggleSafety: () => void;
  onToggleCrime: () => void;
  className?: string;
};

export default function MapLayerToggle({
  isVisible,
  showSafetyLayer,
  showCrimeLayer,
  onToggleSafety,
  onToggleCrime,
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
      <div className="hidden divide-y divide-gray-200 overflow-hidden rounded-[10px] border border-gray-200 bg-white shadow-md">
        <button
          type="button"
          onClick={onToggleSafety}
          className={clsx(
            'flex w-[56px] flex-col items-center justify-center gap-1.5 px-1.5 py-2.5 text-center transition-colors',
            showSafetyLayer ? 'bg-emerald-50' : 'bg-white',
            'hover:bg-emerald-50',
          )}
        >
          <PiPersonSimpleWalkBold
            className={clsx(
              'text-[18px]',
              showSafetyLayer ? 'text-emerald-600' : 'text-gray-700',
            )}
          />
          <span
            className={clsx(
              'break-keep text-[10px] font-semibold leading-[1.2]',
              showSafetyLayer ? 'text-emerald-700' : 'text-gray-900',
            )}
          >
            여성안전
          </span>
        </button>

        <button
          type="button"
          onClick={onToggleCrime}
          className={clsx(
            'flex w-[56px] flex-col items-center justify-center gap-1.5 px-1.5 py-2.5 text-center transition-colors',
            showCrimeLayer ? 'bg-red-50' : 'bg-white',
            'hover:bg-red-50',
          )}
        >
          <PiWarningCircleBold
            className={clsx(
              'text-[18px]',
              showCrimeLayer ? 'text-red-600' : 'text-gray-700',
            )}
          />
          <span
            className={clsx(
              'break-keep text-[10px] font-semibold leading-[1.2]',
              showCrimeLayer ? 'text-red-700' : 'text-gray-900',
            )}
          >
            범죄주의
          </span>
        </button>
      </div>
    </aside>
  );
}