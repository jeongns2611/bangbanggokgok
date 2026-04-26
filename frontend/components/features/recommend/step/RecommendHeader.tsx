'use client';

import clsx from 'clsx';
import {
    HiOutlineMapPin,
    HiOutlineBuildingOffice2,
    HiOutlineBars3BottomLeft,
    HiOutlineChartBarSquare,
} from 'react-icons/hi2';

type Step = 1 | 2 | 3 | 4;

type RecommendHeaderProps = {
    currentStep: Step;
};

const steps = [
    { step: 1, icon: HiOutlineMapPin },
    { step: 2, icon: HiOutlineBuildingOffice2 },
    { step: 3, icon: HiOutlineBars3BottomLeft },
    { step: 4, icon: HiOutlineChartBarSquare },
] as const;

export default function RecommendHeader({ currentStep }: RecommendHeaderProps) {
    return (
        <section className="flex flex-col items-center px-4 pb-4 pt-7">
            <span className="mb-2 inline-flex items-center rounded-full border border-[#DDE8E1] bg-white px-3 py-1 text-xs font-semibold text-primary-100">
                맞춤 추천
            </span>

            <h1 className="text-[28px] font-extrabold tracking-[-0.02em] text-primary-black md:text-[32px]">
                동네&매물 추천
            </h1>

            <p className="mt-2 text-center text-sm font-medium leading-relaxed text-slate-600 md:text-[15px]">
                아래 단계에 따라 조건을 입력하면 맞춤 매물을 추천합니다.
            </p>

            <div className="mt-5 w-full max-w-md">
                <div className="relative">
                    <div className="absolute left-9 right-9 top-8.5 h-0.5 rounded-full bg-[#E3E8E5]" />

                    <div className="relative flex items-start justify-between">
                        {steps.map(({ step, icon: Icon }) => {
                            const isActive = currentStep === step;
                            const isPassed = currentStep > step;

                            return (
                                <div
                                    key={step}
                                    className="flex w-9 flex-col items-center md:w-10"
                                >
                                    <div
                                        className={clsx(
                                            'mb-1.5 flex h-6 w-6 items-center justify-center transition-colors',
                                            isActive || isPassed
                                                ? 'text-primary-200'
                                                : 'text-slate-400',
                                        )}
                                    >
                                        <Icon className="text-[18px] md:text-[20px]" />
                                    </div>

                                    <div
                                        className={clsx(
                                            'flex h-9 w-9 items-center justify-center rounded-full text-[17px] font-extrabold transition-all md:h-10 md:w-10 md:text-[20px]',
                                            isActive || isPassed
                                                ? 'bg-primary-100 text-white'
                                                : 'bg-[#E5E5E5] text-white',
                                        )}
                                    >
                                        {step}
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                </div>
            </div>
        </section>
    );
}
