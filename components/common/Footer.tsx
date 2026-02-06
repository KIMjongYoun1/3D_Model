import React from 'react';

const Footer = () => {
  return (
    <footer className="w-full border-t border-slate-100 bg-white py-8 px-8">
      <div className="max-w-7xl mx-auto flex flex-col sm:flex-row justify-between items-center gap-4">
        <div className="flex items-center gap-2 opacity-50 grayscale">
          <div className="w-6 h-6 bg-slate-900 rounded flex items-center justify-center">
            <span className="font-black text-xs italic text-white">Q</span>
          </div>
          <span className="text-xs font-bold tracking-tighter text-slate-900">QUANTUM STUDIO</span>
        </div>
        
        <p className="text-[10px] font-medium text-slate-400 uppercase tracking-[0.2em]">
          © 2026 Quantum Studio. All rights reserved.
        </p>
        
        <div className="flex gap-6">
          <a href="#" className="text-[10px] font-bold text-slate-400 hover:text-slate-900 transition-colors uppercase tracking-widest">Privacy</a>
          <a href="#" className="text-[10px] font-bold text-slate-400 hover:text-slate-900 transition-colors uppercase tracking-widest">Terms</a>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
