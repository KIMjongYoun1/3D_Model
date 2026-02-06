import React from 'react';

const Footer = () => {
  return (
    <footer className="w-full border-t border-slate-100 bg-white py-8 px-8 flex items-center relative min-h-[80px]">
      {/* Left: Logo */}
      <div className="flex-1 flex justify-start items-center gap-2 opacity-50 grayscale">
        <div className="w-6 h-6 bg-slate-900 rounded flex items-center justify-center">
          <span className="font-black text-xs italic text-white">Q</span>
        </div>
        <span className="text-xs font-bold tracking-tighter text-slate-900">QUANTUM STUDIO</span>
      </div>
      
      {/* Center: Copyright - Perfectly Centered */}
      <div className="absolute left-1/2 -translate-x-1/2">
        <p className="text-[10px] font-medium text-slate-400 uppercase tracking-[0.2em] whitespace-nowrap">
          © 2026 Quantum Studio. All rights reserved.
        </p>
      </div>
      
      {/* Right: Links */}
      <div className="flex-1 flex justify-end gap-6">
        <a href="#" className="text-[10px] font-bold text-slate-400 hover:text-slate-900 transition-colors uppercase tracking-widest">Privacy</a>
        <a href="#" className="text-[10px] font-bold text-slate-400 hover:text-slate-900 transition-colors uppercase tracking-widest">Terms</a>
      </div>
    </footer>
  );
};

export default Footer;
