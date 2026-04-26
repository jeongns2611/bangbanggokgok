'use client';

import { useEffect, useRef, useState } from 'react';
import { IoInformationCircle } from 'react-icons/io5';

interface InfoTooltipProps {
    text: string;
}

export default function InfoTooltip({ text }: InfoTooltipProps) {
    const [open, setOpen] = useState(false);
    const wrapperRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (!wrapperRef.current?.contains(event.target as Node)) {
                setOpen(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    return (
        <div ref={wrapperRef} className="relative">
            <button
                type="button"
                onClick={() => setOpen((prev) => !prev)}
                className="cursor-pointer text-neutral-300 transition hover:text-neutral-500"
                aria-label="설명 보기"
            >
                <IoInformationCircle className="text-[26px]" />
            </button>

            {open && (
                <div className="absolute right-0 top-9 z-20 w-[280px] rounded-[14px] border border-neutral-200 bg-white p-4 shadow-lg">
                    <p className="text-[14px] leading-6 text-neutral-700">{text}</p>
                </div>
            )}
        </div>
    );
}