import React from 'react';

interface CardProps {
  children: React.ReactNode;
  title?: string;
  subtitle?: string;
  className?: string;
  onClick?: () => void;
}

export const Card = ({
  children,
  title,
  subtitle,
  className = '',
  onClick,
}: CardProps) => {
  const baseStyles = 'bg-white rounded-lg shadow-md p-6 transition-shadow';
  const interactiveStyles = onClick ? 'cursor-pointer hover:shadow-lg' : '';

  return (
    <div
      className={`${baseStyles} ${interactiveStyles} ${className}`}
      onClick={onClick}
    >
      {title && (
        <div className="mb-4">
          <h3 className="text-xl font-semibold text-gray-900">{title}</h3>
          {subtitle && (
            <p className="text-sm text-gray-500 mt-1">{subtitle}</p>
          )}
        </div>
      )}
      {children}
    </div>
  );
};


