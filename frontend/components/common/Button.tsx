import type { ReactNode } from 'react';
import clsx from 'clsx';

type ButtonProps = {
  label: string;
  isActive?: boolean;
  onClick?: () => void;
  size?: 'sm' | 'md' | 'lg';
  type?: 'button' | 'submit' | 'reset';
  disabled?: boolean;
  fullWidth?: boolean;
  leftIcon?: ReactNode;
  className?: string;
};

const sizeClasses: Record<'sm' | 'md' | 'lg', string> = {
  sm: 'h-10 px-4 text-sm rounded-xl',
  md: 'h-14 px-6 text-lg rounded-2xl',
  lg: 'h-16 px-8 text-xl rounded-2xl',
};

export default function Button({
  label,
  isActive = false,
  onClick,
  size = 'md',
  type = 'button',
  disabled = false,
  fullWidth = false,
  leftIcon,
  className = '',
}: ButtonProps) {
  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      aria-pressed={isActive}
      className={clsx(
        'inline-flex cursor-pointer items-center justify-center gap-2 border font-semibold whitespace-nowrap transition-colors duration-200',
        sizeClasses[size],
        fullWidth && 'w-full',
        disabled
          ? 'cursor-not-allowed border-gray-200 bg-gray-100 text-gray-400'
          : isActive
            ? 'border-background-400 bg-background-400 text-white'
            : 'border-[#D9DEE3] bg-white text-primary-600',
        className,
      )}
    >
      {leftIcon}
      <span>{label}</span>
    </button>
  );
}