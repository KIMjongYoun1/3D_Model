"use client";

import React, { useEffect, useState, useCallback } from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { Button } from '@/components/ui/Button';

const Header = () => {
  const pathname = usePathname();
  const router = useRouter();
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [adminName, setAdminName] = useState<string | null>(null);

  const navItems = [
    { name: '지식 관리', href: '/knowledge' },
    { name: 'AI 어시스턴트', href: '/ai' },
  ];

  const authItems = [
    { name: '로그인', href: '/login' },
    { name: '가입', href: '/register' },
  ];

  const checkLoginStatus = useCallback(() => {
    const token = typeof window !== 'undefined' ? localStorage.getItem('adminToken') : null;
    const name = typeof window !== 'undefined' ? localStorage.getItem('adminName') : null;
    
    if (token) {
      setIsLoggedIn(true);
      setAdminName(name);
    } else {
      setIsLoggedIn(false);
      setAdminName(null);
    }
  }, []);

  useEffect(() => {
    checkLoginStatus();
    
    const timer = setTimeout(checkLoginStatus, 100);
    
    window.addEventListener('storage', checkLoginStatus);
    return () => {
      window.removeEventListener('storage', checkLoginStatus);
      clearTimeout(timer);
    };
  }, [checkLoginStatus, pathname]);

  const handleLogout = () => {
    localStorage.removeItem('adminToken');
    localStorage.removeItem('adminName');
    localStorage.removeItem('adminEmail');
    setIsLoggedIn(false);
    setAdminName(null);
    
    window.dispatchEvent(new Event('storage'));
    router.push('/');
  };

  return (
    <header className="sticky top-0 z-[100] w-full border-b border-slate-200 bg-white/80 backdrop-blur-xl px-8 h-16 flex items-center">
      {/* Left: Logo */}
      <div className="flex-1 flex justify-start">
        <Link href="/" className="flex items-center gap-2 no-underline">
          <div className="w-8 h-8 bg-indigo-600 rounded-lg flex items-center justify-center shadow-[0_0_20px_rgba(99,102,241,0.2)]">
            <span className="font-black text-lg italic text-white text-[14px]">Q</span>
          </div>
          <h1 className="text-xl font-black italic tracking-tighter text-slate-900 hidden sm:block">
            QUANTUM<span className="text-indigo-600 font-black">ADMIN</span>
          </h1>
        </Link>
      </div>

      {/* Center: Navigation */}
      <nav className="absolute left-1/2 -translate-x-1/2 flex items-center gap-1 sm:gap-6">
        {navItems.map((item) => (
          <Link
            key={item.href}
            href={item.href}
            className={`px-3 py-2 text-[12px] font-bold uppercase tracking-widest transition-all ${
              pathname === item.href
                ? 'text-indigo-600'
                : 'text-slate-500 hover:text-slate-900'
            }`}
          >
            {item.name}
          </Link>
        ))}
      </nav>

      {/* Right: Admin Auth */}
      <div className="flex-1 flex justify-end items-center gap-4">
        {isLoggedIn ? (
          <div className="flex items-center gap-4">
            <div className="flex flex-col items-end">
              <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest leading-none">Admin</span>
              <span className="text-[12px] font-bold text-slate-900">{adminName}</span>
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
          <div className="flex items-center gap-3">
            {authItems.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className={`px-3 py-2 text-[12px] font-bold uppercase tracking-widest transition-all ${
                  pathname === item.href ? 'text-indigo-600' : 'text-slate-500 hover:text-slate-900'
                }`}
              >
                {item.name}
              </Link>
            ))}
          </div>
        )}
      </div>
    </header>
  );
};

export default Header;
