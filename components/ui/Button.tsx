import React from 'react';

interface ButtonProps {
  variant?: 'primary' | 'secondary' | 'outline' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
  children: React.ReactNode;
  onClick?: () => void;
  disabled?: boolean;
  type?: 'button' | 'submit' | 'reset';
  className?: string;
}

export const Button = ({
  variant = 'primary',
  size = 'md',
  children,
  onClick,
  disabled = false,
  type = 'button',
  className = '',
}: ButtonProps) => {
  const baseStyles = 'rounded-full font-black tracking-widest transition-all duration-300 focus:outline-none disabled:cursor-not-allowed uppercase';
  
  const variantStyles = {
    primary: 'bg-blue-600 text-white hover:bg-blue-700 shadow-lg shadow-blue-600/20 disabled:bg-slate-200 disabled:text-slate-400',
    secondary: 'bg-slate-900 text-white hover:bg-black shadow-lg shadow-black/10 disabled:bg-slate-200 disabled:text-slate-400',
    outline: 'border-2 border-slate-200 text-slate-600 hover:bg-slate-50 hover:border-slate-300 disabled:border-slate-100 disabled:text-slate-300',
    ghost: 'bg-transparent text-slate-500 hover:bg-slate-100 hover:text-slate-900'
  };

  const sizeStyles = {
    sm: 'px-4 py-1.5 text-[10px]',
    md: 'px-6 py-2.5 text-[11px]',
    lg: 'px-8 py-3.5 text-[13px]',
  };

  return (
    <button
      type={type}
      className={`${baseStyles} ${variantStyles[variant]} ${sizeStyles[size]} ${className}`}
      onClick={onClick}
      disabled={disabled}
    >
      {children}
    </button>
  );
};
