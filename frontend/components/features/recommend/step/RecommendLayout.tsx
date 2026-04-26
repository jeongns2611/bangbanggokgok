'use client';

import type { ReactNode } from 'react';
import clsx from 'clsx';
import RecommendHeader from '@/components/features/recommend/step/RecommendHeader';
import Button from '@/components/common/Button';

type Step = 1 | 2 | 3 | 4;

type RecommendLayoutProps = {
  currentStep: Step;
  children: ReactNode;
  onNext?: () => void;
  onPrev?: () => void;
  nextLabel?: string;
  prevLabel?: string;
  nextDisabled?: boolean;
  nextLoading?: boolean;
  showPrevButton?: boolean;
};

export default function RecommendLayout({
  currentStep,
  children,
  onNext,
  onPrev,
  nextLabel = '다음 단계로',
  prevLabel = '이전',
  nextDisabled = false,
  nextLoading = false,
  showPrevButton = false,
}: RecommendLayoutProps) {
  const actionButtonClass =
    'h-10 min-w-[124px] rounded-xl px-4 text-sm font-semibold';

  const showNextButton = currentStep !== 4;

  return (
    <main className="min-h-screen bg-[#F6FAF7]">
      <div className="mx-auto w-full max-w-6xl px-4 pb-10 sm:px-6">
        <RecommendHeader currentStep={currentStep} />

        <div className="mx-auto w-full max-w-5xl">
          <div className="rounded-[20px] border border-[#E5ECE7] bg-white px-5 py-5 shadow-[0_10px_24px_rgba(16,24,40,0.04)] sm:px-6 sm:py-6">
            {children}
          </div>

          <div className="mt-4 flex items-center justify-between">
            <div>
              {showPrevButton && (
                <Button
                  label={prevLabel}
                  onClick={onPrev}
                  size="md"
                  className={clsx(
                    actionButtonClass,
                    'border-[#D9E2DC] bg-white text-primary-500 hover:bg-[#F8FBF9]',
                  )}
                />
              )}
            </div>

            <div>
              {showNextButton && (
                <Button
                  label={nextLabel}
                  onClick={onNext}
                  size="md"
                  disabled={nextDisabled}
                  isActive={!nextDisabled}
                  leftIcon={nextLoading ? (
                    <span
                      className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent"
                      aria-hidden="true"
                    />
                  ) : undefined}
                  className={clsx(
                    actionButtonClass,
                    !nextDisabled && 'hover:opacity-95',
                  )}
                />
              )}
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}