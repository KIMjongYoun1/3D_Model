import React from 'react';

interface CardProps {
  id?: string;
  children: React.ReactNode;
  title?: string;
  subtitle?: string;
  className?: string;
  onClick?: () => void;
  variant?: 'default' | 'bento' | 'glass';
}

export const Card = ({
  id,
  children,
  title,
  subtitle,
  className = '',
  onClick,
  variant = 'default',
}: CardProps) => {
  const baseStyles = 'transition-all duration-500';
  
  const variantStyles = {
    default: 'bg-white border border-slate-200 rounded-[2.5rem] shadow-xl shadow-slate-200/50',
    bento: 'bg-white/80 backdrop-blur-2xl border border-slate-200 rounded-[3rem] shadow-xl hover:shadow-2xl hover:border-blue-200',
    glass: 'bg-white/40 backdrop-blur-3xl border border-white/20 rounded-[3.5rem] shadow-2xl'
  };

  const interactiveStyles = onClick ? 'cursor-pointer hover:scale-[1.02] active:scale-[0.98]' : '';

  return (
    <div
      id={id}
      className={`${baseStyles} ${variantStyles[variant]} ${interactiveStyles} ${className}`}
      onClick={onClick}
    >
      {(title || subtitle) && (
        <div className="px-8 pt-8 pb-4">
          {title && <h3 className="text-lg font-black text-slate-900 tracking-tighter italic uppercase">{title}</h3>}
          {subtitle && <p className="text-[10px] font-bold text-slate-400 uppercase tracking-[0.2em] mt-1">{subtitle}</p>}
        </div>
      )}
      <div className={title || subtitle ? 'px-8 pb-8' : ''}>
        {children}
      </div>
    </div>
  );
};
