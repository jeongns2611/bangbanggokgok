'use client';

import Link from 'next/link';
import Image from 'next/image';
import { useState, useEffect, useRef } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { HiOutlineMenu, HiOutlineX } from 'react-icons/hi';
import { useAuthStore } from '@/store/useAuthStore';

export default function Header() {
  const pathname = usePathname();
  const router = useRouter();
  const [isOpen, setIsOpen] = useState(false);
  const [hydrated, setHydrated] = useState(false);
  const [myPageOpen, setMyPageOpen] = useState(false);
  const myPageRef = useRef<HTMLDivElement>(null);

  const isLoggedIn = useAuthStore((state) => state.isLoggedIn);
  const accessToken = useAuthStore((state) => state.accessToken);
  const logout = useAuthStore((state) => state.logout);

  useEffect(() => {
    setHydrated(true);
  }, []);

  
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (myPageRef.current && !myPageRef.current.contains(e.target as Node)) {
        setMyPageOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = async () => {
    try {
      if (accessToken) {
        const authorizationHeader = `Bearer ${accessToken}`;
        
        await fetch('/api/auth/logout', {
          method: 'POST',
          headers: { Authorization: authorizationHeader },
        });
      }
    } catch {
      
    }
    logout();
    setMyPageOpen(false);
    router.push('/');
  };

  const isLogin = hydrated && isLoggedIn;
  const isHome = pathname === '/';

  const navItems = [
    { href: '/recommend', label: '동네&매물 추천' },
    { href: '/search', label: 'AI 매물 검색' },
    { href: '/statistics', label: '지역분석통계' },
    { href: '/comparison', label: 'AI 매물 비교' },
  ];

  const myPageItems = [
    { href: '/mypage/wishlist', label: '찜한실매물' },
    { href: '/mypage/conditions', label: '매물추천조건' },
    { href: '/mypage/posts', label: '매물등록' },
  ];

  const myPageMenuItemClass =
    'block w-full px-4 py-2.5 text-left text-sm font-sans font-medium text-gray-700 hover:bg-gray-50';

  const isNavItemActive = (href: string) =>
    pathname === href || pathname.startsWith(`${href}/`);

  const desktopNavClass = (isActive: boolean) =>
    `text-base font-sans transition ${
      isHome
        ? isActive
          ? 'font-bold !text-white drop-shadow-[0_2px_8px_rgba(0,0,0,0.45)]'
          : 'font-medium !text-white/85 hover:text-white drop-shadow-[0_2px_8px_rgba(0,0,0,0.45)]'
        : isActive
          ? 'font-extrabold !text-primary-500'
          : 'font-medium !text-gray-700 hover:text-primary-100'
    }`;

  const mobileMenuItemClass = (isActive: boolean) =>
    `block w-full py-3 text-left text-sm font-sans transition ${
      isHome
        ? isActive
          ? 'font-bold !text-white'
          : 'font-medium text-white/85 hover:text-white'
        : isActive
          ? 'font-extrabold !text-primary-500'
          : 'font-medium text-gray-700 hover:text-primary-100'
    }`;
  const handleNavClick = (href: string) => {
    if (!isLogin) {
      alert('로그인이 필요합니다.');
      return;
    }
    router.push(href);
  };

  const handleMyPageItemClick = (href: string) => {
    setMyPageOpen(false);
    setIsOpen(false);
    router.push(href);
  };

  return (
    <header
      className={`z-50 w-full ${
        isHome
          ? 'absolute top-0 left-0 bg-transparent'
          : 'border-b border-gray-200 bg-white'
      }`}
    >
      <div className="mx-auto flex h-[72px] max-w-[1280px] items-center px-6">
        <Link
          href="/"
          className="flex items-center gap-3"
          onClick={() => setIsOpen(false)}
        >
          <Image src="/logo.png" alt="logo" width={40} height={40} />
          <span
            className={`text-[22px] font-bold tracking-[-0.02em] ${
              isHome
                ? 'text-white drop-shadow-[0_2px_8px_rgba(0,0,0,0.45)]'
                : 'text-primary-500'
            }`}
          >
            방방곡곡
          </span>
        </Link>

        <div className="ml-auto hidden items-center md:flex">
          <nav className="flex items-center gap-8">
            {navItems.map((item) => {
              const isActive = isNavItemActive(item.href);

              return (
                <button
                  key={item.href}
                  type="button"
                  onClick={() => handleNavClick(item.href)}
                  className={desktopNavClass(isActive)}
                >
                  {item.label}
                </button>
              );
            })}

            {isLogin ? (
              <div className="relative" ref={myPageRef}>
                <button
                  type="button"
                  onClick={() => setMyPageOpen((prev) => !prev)}
                  className={desktopNavClass(pathname.startsWith('/mypage'))}
                >
                  마이페이지
                </button>
                {myPageOpen && (
                  <div className="absolute right-0 z-50 mt-2 w-40 rounded-lg border border-gray-200 bg-white py-1 shadow-lg">
                    {myPageItems.map((item) => (
                      <button
                        key={item.href}
                        type="button"
                        className={myPageMenuItemClass}
                        onClick={() => handleMyPageItemClick(item.href)}
                      >
                        {item.label}
                      </button>
                    ))}
                    <button
                      type="button"
                      onClick={handleLogout}
                      className={myPageMenuItemClass}
                    >
                      로그아웃
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <a
                href="/api/auth/google"
                className={`ml-2 rounded-full border px-5 py-2.5 text-sm font-medium transition ${
                  isHome
                    ? 'border-white/40 !text-white hover:bg-white/10'
                    : 'border-gray-300 text-gray-700 hover:bg-gray-50'
                }`}
              >
                로그인
              </a>
            )}
          </nav>
        </div>

        <button
          type="button"
          className={`ml-auto md:hidden ${isHome ? 'text-white' : 'text-gray-800'}`}
          onClick={() => setIsOpen((prev) => !prev)}
          aria-label="메뉴 열기"
        >
          {isOpen ? <HiOutlineX size={28} /> : <HiOutlineMenu size={28} />}
        </button>
      </div>

      {isOpen && (
        <div
          className={`md:hidden ${
            isHome
              ? 'bg-black/40 backdrop-blur-sm'
              : 'border-t border-gray-200 bg-white'
          }`}
        >
          <nav className="flex flex-col px-6 py-4">
            {navItems.map((item) => {
              const isActive = isNavItemActive(item.href);

              return (
                <button
                  key={item.href}
                  type="button"
                  onClick={() => {
                    if (!isLogin) {
                      alert('로그인이 필요합니다.');
                      return;
                    }
                    setIsOpen(false);
                    router.push(item.href);
                  }}
                  className={mobileMenuItemClass(isActive)}
                >
                  {item.label}
                </button>
              );
            })}

            {isLogin ? (
              <>
                {myPageItems.map((item) => (
                  <button
                    key={item.href}
                    type="button"
                    className={mobileMenuItemClass(pathname.startsWith(item.href))}
                    onClick={() => handleMyPageItemClick(item.href)}
                  >
                    {item.label}
                  </button>
                ))}
                <button
                  type="button"
                  onClick={() => {
                    setIsOpen(false);
                    handleLogout();
                  }}
                  className={mobileMenuItemClass(false)}
                >
                  로그아웃
                </button>
              </>
            ) : (
              <a
                href="/api/auth/google"
                className={`${mobileMenuItemClass(false)} ${isHome ? '!text-white' : ''}`}
                onClick={() => setIsOpen(false)}
              >
                로그인
              </a>
            )}
          </nav>
        </div>
      )}
    </header>
  );
}