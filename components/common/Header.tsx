"use client";

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';

const Header = () => {
  const pathname = usePathname();

  const navItems = [
    { name: 'Studio', href: '/studio' },
    { name: '회원가입', href: '/register' },
    { name: '결제', href: '/payment' },
    { name: '마이페이지', href: '/mypage' },
  ];

  const handleNaverLogin = () => {
    const clientId = 'EL1L7BQu1OMnGnxYyzls';
    const redirectUri = encodeURIComponent('http://localhost:3000/api/auth/callback/naver');
    const state = Math.random().toString(36).substring(7);
    const naverAuthUrl = `https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=${clientId}&redirect_uri=${redirectUri}&state=${state}`;
    
    // 상태값 저장 (CSRF 방지)
    localStorage.setItem('naver_auth_state', state);
    
    window.location.href = naverAuthUrl;
  };

  return (
    <header className="sticky top-0 z-[100] w-full border-b border-slate-200 bg-white/80 backdrop-blur-xl px-8 h-16 flex items-center justify-between">
      <div className="flex items-center gap-4">
        <Link href="/" className="flex items-center gap-2">
          <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center shadow-[0_0_20px_rgba(37,99,235,0.2)]">
            <span className="font-black text-lg italic text-white text-[14px]">Q</span>
          </div>
          <h1 className="text-xl font-black italic tracking-tighter text-slate-900 hidden sm:block">
            QUANTUM<span className="text-blue-600 font-black">STUDIO</span>
          </h1>
        </Link>
      </div>

      <nav className="flex items-center gap-1 sm:gap-6">
        {navItems.map((item) => (
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

      <div className="flex items-center gap-4">
        <button 
          onClick={handleNaverLogin}
          className="flex items-center gap-2 px-4 py-2 bg-[#03C75A] hover:bg-[#02b351] text-white text-[10px] font-black rounded-full transition-all shadow-lg shadow-green-900/10"
        >
          <span className="w-4 h-4 bg-white text-[#03C75A] rounded-sm flex items-center justify-center text-[10px] font-black">N</span>
          NAVER LOGIN
        </button>
        <button className="px-5 py-2 bg-slate-900 hover:bg-black text-white text-[10px] font-black rounded-full transition-all shadow-lg shadow-black/10">
          LOGIN
        </button>
      </div>
    </header>
  );
};

export default Header;
