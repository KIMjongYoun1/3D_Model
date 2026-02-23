import Sidebar from "@/components/common/Sidebar";

export default function MainLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex-1 flex min-h-0 overflow-hidden">
      <Sidebar />
      <div className="flex-1 flex flex-col min-w-0 min-h-0 overflow-auto bg-[#f8f9fa]">
        {children}
      </div>
    </div>
  );
}
