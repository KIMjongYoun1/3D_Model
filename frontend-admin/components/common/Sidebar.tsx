"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const navItems = [
  { name: "회원 관리", href: "/members", icon: "👥" },
  { name: "거래 관리", href: "/transactions", icon: "💳" },
  { name: "구독 관리", href: "/subscriptions", icon: "📦" },
  { name: "매출 대시보드", href: "/dashboard", icon: "📊" },
  { name: "플랜 관리", href: "/plans", icon: "📋" },
  { name: "지식 관리", href: "/knowledge", icon: "📚" },
  { name: "약관 관리", href: "/terms", icon: "📜" },
  { name: "AI 어시스턴트", href: "/ai", icon: "🤖" },
];

export default function Sidebar() {
  const pathname = usePathname();

  return (
    <aside className="w-60 flex-shrink-0 border-r border-slate-200 bg-white/90 backdrop-blur-sm flex flex-col overflow-y-auto">
      <div className="px-4 pt-4 pb-2">
        <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest">관리 메뉴</span>
      </div>
      <nav className="p-4 pt-0 space-y-1 flex-1">
        {navItems.map((item) => (
          <Link
            key={item.href}
            href={item.href}
            className={`flex items-center gap-3 px-4 py-3 rounded-xl text-[13px] font-bold transition-all ${
              pathname === item.href
                ? "bg-indigo-50 text-indigo-600"
                : "text-slate-600 hover:bg-slate-50 hover:text-slate-900"
            }`}
          >
            <span className="text-lg">{item.icon}</span>
            {item.name}
          </Link>
        ))}
      </nav>
    </aside>
  );
}
