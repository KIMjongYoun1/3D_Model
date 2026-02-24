import React from 'react';

interface InputProps {
  type?: 'text' | 'email' | 'password' | 'number' | 'textarea' | 'datetime-local';
  label?: string;
  placeholder?: string;
  value?: string;
  onChange?: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  error?: string;
  disabled?: boolean;
  required?: boolean;
  className?: string;
}

export const Input = ({
  type = 'text',
  label,
  placeholder,
  value,
  onChange,
  error,
  disabled = false,
  required = false,
  className = '',
}: InputProps) => {
  const baseStyles = 'w-full px-6 py-4 rounded-2xl bg-slate-50 border border-slate-200 text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-400 transition-all duration-300 shadow-inner';
  const errorStyles = error ? 'border-red-500 focus:ring-red-500/20 focus:border-red-500' : '';
  const disabledStyles = disabled ? 'opacity-50 cursor-not-allowed' : '';

  return (
    <div className={`w-full flex flex-col space-y-2 ${className}`}>
      {label && (
        <label className="block text-[10px] font-black text-slate-500 uppercase tracking-widest px-1">
          {label}
          {required && <span className="text-blue-600 ml-1">*</span>}
        </label>
      )}
      
      {type === 'textarea' ? (
        <textarea
          placeholder={placeholder}
          value={value}
          onChange={onChange as any}
          disabled={disabled}
          required={required}
          className={`${baseStyles} ${errorStyles} ${disabledStyles} flex-1 min-h-[120px] resize-none`}
        />
      ) : (
        <input
          type={type}
          placeholder={placeholder}
          value={value}
          onChange={onChange as any}
          disabled={disabled}
          required={required}
          className={`${baseStyles} ${errorStyles} ${disabledStyles}`}
        />
      )}
      
      {error && (
        <p className="px-1 text-[10px] font-bold text-red-500 uppercase tracking-tight">{error}</p>
      )}
    </div>
  );
};
