"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const navItems = [
  { name: "지식 관리", href: "/knowledge", icon: "📚" },
  { name: "AI 어시스턴트", href: "/ai", icon: "🤖" },
];

export default function Sidebar() {
  const pathname = usePathname();

  return (
    <aside className="w-60 flex-shrink-0 border-r border-slate-200 bg-white/90 backdrop-blur-sm flex flex-col">
      <nav className="p-4 space-y-1">
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
