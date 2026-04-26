'use client';

import {
    HiOutlinePencilSquare,
    HiOutlineTrash,
    HiOutlineMapPin,
} from 'react-icons/hi2';
import { BsCurrencyDollar } from 'react-icons/bs';
import { PiBuildingsLight } from 'react-icons/pi';

type ConditionCardProps = {
    id: number | string;
    name: string;
    regions: string[];
    propertyTypes: string[];
    priceRange: string;
    extraConditions?: string[];
    isDefault?: boolean;
    onEdit?: (id: number | string) => void;
    onDelete?: (id: number | string) => void;
    onSetDefault?: (id: number | string) => void;
};

export default function ConditionCard({
    id,
    name,
    regions,
    propertyTypes,
    priceRange,
    extraConditions = [],
    isDefault = false,
    onEdit,
    onDelete,
    onSetDefault,
}: ConditionCardProps) {
    return (
        <article className="border-b border-gray-200 bg-white px-5 py-4 transition hover:border-background-300 ">
            <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
                <div className="min-w-0 flex-1">
                    <div className="mb-3 flex flex-wrap items-center gap-2">
                        <h3 className="text-xl font-bold tracking-[-0.02em] text-primary-black">
                            {name}
                        </h3>

                        {isDefault ? (
                            <span className="inline-flex items-center rounded-full border border-background-300 bg-background-100 px-3 py-1 text-[12px] leading-none font-medium text-primary-200">
                                기본 조건
                            </span>
                        ) : (
                            <button
                                type="button"
                                onClick={() => onSetDefault?.(id)}
                                className="cursor-pointer inline-flex items-center gap-1 rounded-full border border-gray-300 bg-gray-50 px-3 py-1 text-[12px] leading-none font-medium text-gray-600 transition hover:bg-gray-100 hover:text-gray-700"
                            >
                                기본 조건으로 설정
                            </button>
                        )}
                    </div>

                    <div className="flex flex-wrap gap-2">
                        {regions.length > 0 && (
                            <span className="inline-flex items-center gap-1.5 rounded-full bg-background-100 px-2.5 py-1.5 text-sm font-medium text-primary-200">
                                <HiOutlineMapPin className="text-sm" />
                                {regions.join(', ')}
                            </span>
                        )}

                        <span className="inline-flex items-center gap-1.5 rounded-full bg-background-100 px-2.5 py-1.5 text-sm font-medium text-primary-200">
                            <PiBuildingsLight className="text-sm" />
                            {propertyTypes.join(', ')}
                        </span>

                        <span className="inline-flex items-center gap-1.5 rounded-full bg-background-100 px-3 py-1.5 text-sm font-medium text-primary-200">
                            <BsCurrencyDollar className="text-sm" />
                            {priceRange}
                        </span>

                        {extraConditions.map((condition, index) => (
                            <span
                                key={`${condition}-${index}`}
                                className="rounded-full bg-gray-100 px-3 py-1.5 text-sm font-medium text-gray-700"
                            >
                                {condition}
                            </span>
                        ))}
                    </div>
                </div>

                <div className="flex shrink-0 items-center gap-2 self-end lg:self-center">
                    <button
                        type="button"
                        onClick={() => onEdit?.(id)}
                        className="cursor-pointer inline-flex items-center gap-1.5 rounded-xl border border-gray-200 bg-white px-3 py-2 text-sm font-medium text-gray-700 transition hover:bg-gray-50"
                    >
                        <HiOutlinePencilSquare className="text-base" />
                        편집
                    </button>

                    <button
                        type="button"
                        onClick={() => onDelete?.(id)}
                        disabled={isDefault}
                        className={`inline-flex items-center gap-1.5 rounded-xl px-3 py-2 text-sm font-medium text-white transition ${
                            isDefault
                                ? 'cursor-not-allowed bg-gray-300'
                                : 'cursor-pointer bg-background-400 hover:brightness-95'
                        }`}
                    >
                        <HiOutlineTrash className="text-base" />
                        삭제
                    </button>
                </div>
            </div>
        </article>
    );
}