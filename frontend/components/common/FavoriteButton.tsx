'use client';

import { useState, useCallback, useEffect } from 'react';
import { IoHeart, IoHeartOutline } from 'react-icons/io5';

type FavoriteButtonProps = {
  houseId: number;
  initialLiked?: boolean;
  size?: 'sm' | 'md' | 'lg';
  disabled?: boolean;
  className?: string;
  onChange?: (houseId: number, liked: boolean) => Promise<void> | void;
};

const sizeMap = {
  sm: { button: 'h-8 w-8', icon: 18 },
  md: { button: 'h-10 w-10', icon: 22 },
  lg: { button: 'h-12 w-12', icon: 26 },
};

export default function FavoriteButton({
  houseId,
  initialLiked = false,
  size = 'md',
  disabled = false,
  className = '',
  onChange,
}: FavoriteButtonProps) {
  const [liked, setLiked] = useState(initialLiked);
  const [isLoading, setIsLoading] = useState(false);
  const currentSize = sizeMap[size];

  useEffect(() => {
    setLiked(initialLiked);
  }, [initialLiked]);

  const handleClick = useCallback(async () => {
    if (disabled || isLoading) return;

    const next = !liked;
    
    setIsLoading(true);
    try {
      await onChange?.(houseId, next);
      
      setLiked(next);
      
    } catch (error) {
      
      
    } finally {
      setIsLoading(false);
    }
  }, [disabled, isLoading, liked, houseId, onChange]);

  return (
    <button
      type="button"
      onClick={handleClick}
      disabled={disabled || isLoading}
      aria-pressed={liked}
      aria-label={liked ? '찜 해제' : '찜하기'}
      className={[
        'flex items-center justify-center',
        'transition-all duration-200',
        disabled || isLoading ? 'cursor-not-allowed opacity-60' : 'hover:scale-110 active:scale-95',
        'focus:outline-none',
        currentSize.button,
        className,
      ].join(' ')}
    >
      {liked ? (
        <IoHeart
          size={currentSize.icon}
          className="text-primary-100 drop-shadow-[0_2px_6px_rgba(14,26,19,0.4)]"
        />
      ) : (
        <IoHeartOutline
          size={currentSize.icon}
          className="text-background-100 drop-shadow-[0_2px_6px_rgba(14,26,19,0.55)]"
        />
      )}
    </button>
  );
}