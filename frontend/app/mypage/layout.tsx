'use client';

import type { ReactNode } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useEffect, useRef, useState } from 'react';

const tabs = [
    { label: '찜한 실매물', href: '/mypage/wishlist' },
    { label: '매물추천조건', href: '/mypage/conditions' },
    { label: '매물등록', href: '/mypage/posts' },
];

export default function MyPageLayout({
    children,
}: {
    children: ReactNode;
}) {
    const pathname = usePathname();
    const containerRef = useRef<HTMLDivElement | null>(null);
    const tabRefs = useRef<(HTMLAnchorElement | null)[]>([]);
    const [indicator, setIndicator] = useState({ left: 0, width: 0 });

    useEffect(() => {
        const updateIndicator = () => {
            const activeIndex = tabs.findIndex((tab) => pathname.startsWith(tab.href));
            const activeTab = tabRefs.current[activeIndex];

            if (!activeTab) return;

            setIndicator({
                left: activeTab.offsetLeft,
                width: activeTab.offsetWidth,
            });
        };

        updateIndicator();
        window.addEventListener('resize', updateIndicator);

        return () => {
            window.removeEventListener('resize', updateIndicator);
        };
    }, [pathname]);

    return (
        <section className="min-h-screen bg-[#f7f7f5] py-10">
            <div className="mx-auto max-w-[1200px] px-6">
                <div className="border-b border-gray-200">
                    <div ref={containerRef} className="relative flex gap-8">
                        {tabs.map((tab, index) => {
                            const isActive = pathname.startsWith(tab.href);

                            return (
                                <Link
                                    key={tab.href}
                                    href={tab.href}
                                    ref={(el) => {
                                        tabRefs.current[index] = el;
                                    }}
                                    className={`whitespace-nowrap py-4 text-[17px] font-semibold transition-colors ${isActive ? 'text-black' : 'text-black/60 hover:text-black'
                                        }`}
                                >
                                    {tab.label}
                                </Link>
                            );
                        })}

                        <span
                            className="absolute bottom-0 h-[3px] rounded-full bg-background-400 transition-all duration-300 ease-in-out"
                            style={{
                                left: `${indicator.left}px`,
                                width: `${indicator.width}px`,
                            }}
                        />
                    </div>
                </div>

                <div className="pt-8">{children}</div>
            </div>
        </section>
    );
}