"use client";

import React, { useEffect, useState, useCallback } from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { Button } from '@/components/ui/Button';
import { checkAuth, logout } from '@/lib/authApi';

const Header = () => {
  const pathname = usePathname();
  const router = useRouter();
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userName, setUserName] = useState<string | null>(null);

  const navItems = [
    { name: 'Studio', href: '/studio' },
    { name: '결제', href: '/payment' },
    { name: '마이페이지', href: '/mypage', protected: true },
  ];

  const checkLoginStatus = useCallback(async () => {
    const user = await checkAuth();
    if (user) {
      setIsLoggedIn(true);
      setUserName(user.name);
    } else {
      setIsLoggedIn(false);
      setUserName(null);
    }
  }, []);

  useEffect(() => {
    checkLoginStatus();
    window.addEventListener('storage', checkLoginStatus);
    return () => window.removeEventListener('storage', checkLoginStatus);
  }, [checkLoginStatus, pathname]);

  const handleLogout = async () => {
    await logout();
    setIsLoggedIn(false);
    setUserName(null);
    window.dispatchEvent(new Event('storage'));
    router.push('/studio');
  };

  const handleNaverLogin = () => {
    const clientId = process.env.NEXT_PUBLIC_NAVER_CLIENT_ID;
    const redirectUri = encodeURIComponent('http://localhost:3000/api/auth/callback/naver');
    const state = Math.random().toString(36).substring(7);
    const naverAuthUrl = `https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=${clientId}&redirect_uri=${redirectUri}&state=${state}`;
    
    // 상태값 저장 (CSRF 방지)
    localStorage.setItem('naver_auth_state', state);
    
    window.location.href = naverAuthUrl;
  };

  return (
    <header className="sticky top-0 z-[100] w-full border-b border-slate-200 bg-white/80 backdrop-blur-xl px-8 h-16 flex items-center">
      {/* Left: Logo */}
      <div className="flex-1 flex justify-start">
        <Link href="/" className="flex items-center gap-2">
          <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center shadow-[0_0_20px_rgba(37,99,235,0.2)]">
            <span className="font-black text-lg italic text-white text-[14px]">Q</span>
          </div>
          <h1 className="text-xl font-black italic tracking-tighter text-slate-900 hidden sm:block">
            QUANTUM<span className="text-blue-600 font-black">STUDIO</span>
          </h1>
        </Link>
      </div>

      {/* Center: Navigation - Perfectly Centered */}
      <nav className="absolute left-1/2 -translate-x-1/2 flex items-center gap-1 sm:gap-6">
        {navItems
          .filter(item => !item.protected || isLoggedIn)
          .map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className={`px-3 py-2 text-[12px] font-bold uppercase tracking-widest transition-all ${
                pathname === item.href
                  ? 'text-blue-600'
                  : 'text-slate-500 hover:text-slate-900'
              }`}
            >
              {item.name}
            </Link>
          ))}
      </nav>

      {/* Right: Auth Buttons */}
      <div className="flex-1 flex justify-end items-center gap-4">
        {isLoggedIn ? (
          <div className="flex items-center gap-4">
            <div className="flex flex-col items-end">
              <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest leading-none">Logged in as</span>
              <span className="text-[12px] font-bold text-slate-900">{userName}</span>
            </div>
            <Button 
              variant="outline"
              onClick={handleLogout}
              className="text-[10px] px-5 py-2"
            >
              LOGOUT
            </Button>
          </div>
        ) : (
          <div className="flex items-center gap-2">
            <Button 
              variant="naver"
              onClick={handleNaverLogin}
              className="text-[10px] px-4 py-2 flex items-center gap-2"
            >
              <span className="w-4 h-4 bg-white text-[#03C75A] rounded-sm flex items-center justify-center text-[10px] font-black">N</span>
              NAVER LOGIN
            </Button>
            <Button 
              variant="kakao" 
              onClick={() => alert('카카오 로그인은 준비 중입니다.')}
              className="text-[10px] px-4 py-2 flex items-center gap-2"
            >
              <span className="w-4 h-4 bg-[#3C1E1E] text-[#FEE500] rounded-sm flex items-center justify-center text-[10px] font-black">K</span>
              KAKAO LOGIN
            </Button>
          </div>
        )}
      </div>
    </header>
  );
};

export default Header;
