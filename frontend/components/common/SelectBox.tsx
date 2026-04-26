'use client';

import { useEffect, useRef, useState } from 'react';
import clsx from 'clsx';
import { IoIosArrowUp } from "react-icons/io";

type Option = {
    label: string;
    value: string;
};

type SelectBoxProps = {
    placeholder: string;
    options: Option[];
    value: string;
    onChange: (value: string) => void;
    disabled?: boolean;
    size?: 'default' | 'compact';
    className?: string;
};

export default function SelectBox({
    placeholder,
    options,
    value,
    onChange,
    disabled = false,
    size = 'default',
    className = '',
}: SelectBoxProps) {
    const [isOpen, setIsOpen] = useState(false);
    const wrapperRef = useRef<HTMLDivElement>(null);

    const selectedOption = options.find((option) => option.value === value);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (!wrapperRef.current?.contains(event.target as Node)) {
                setIsOpen(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);

        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    const handleSelect = (optionValue: string) => {
        if (value === optionValue) {
            
            onChange('');
        } else {
            onChange(optionValue);
        }
        setIsOpen(false);
    };

    const isCompact = size === 'compact';

    const triggerClassName = isCompact
        ? 'h-12 rounded-xl px-4 text-[15px] font-medium'
        : 'h-16 rounded-2xl px-6 text-2xl font-semibold';

    const listClassName = isCompact
        ? 'top-[52px] max-h-60 rounded-xl'
        : 'top-[72px] max-h-72 rounded-2xl';

    const optionClassName = isCompact
        ? 'h-11 px-4 text-sm'
        : 'h-14 px-6 text-lg';

    return (
        <div ref={wrapperRef} className={clsx('relative w-full', className)}>
            <button
                type="button"
                disabled={disabled}
                onClick={() => setIsOpen((prev) => !prev)}
                className={clsx(
                    'flex w-full items-center justify-between border bg-white text-left outline-none transition-colors',
                    triggerClassName,
                    disabled
                        ? 'cursor-not-allowed border-gray-200 bg-gray-100 text-gray-400'
                        : 'cursor-pointer border-[#D9DEE3] text-black hover:border-primary-200',
                )}
            >
                <span className={clsx(!selectedOption && 'text-gray-400')}>
                    {selectedOption ? selectedOption.label : placeholder}
                </span>

                <IoIosArrowUp
                    className={clsx(
                        'shrink-0 text-lg text-primary-500 transition-transform duration-200',
                        isOpen && 'rotate-180',
                        disabled && 'text-gray-300',
                    )}
                />
            </button>

            {isOpen && !disabled && (
                <ul className={clsx(
                    'absolute left-0 z-20 w-full overflow-y-auto border border-[#D9DEE3] bg-white shadow-lg',
                    listClassName,
                )}>
                    {options.length > 0 ? (
                        options.map((option) => {
                            const isSelected = option.value === value;

                            return (
                                <li key={option.value}>
                                    <button
                                        type="button"
                                        onClick={() => handleSelect(option.value)}
                                        className={clsx(
                                            'cursor-pointer flex w-full items-center text-left font-medium transition-colors',
                                            optionClassName,
                                            isSelected
                                                ? 'bg-background-100 text-primary-600'
                                                : 'bg-white text-black hover:bg-gray-50',
                                        )}
                                    >
                                        {option.label}
                                    </button>
                                </li>
                            );
                        })
                    ) : (
                        <li className="px-6 py-4 text-lg text-gray-400">
                            선택 가능한 항목이 없습니다
                        </li>
                    )}
                </ul>
            )}
        </div>
    );
}