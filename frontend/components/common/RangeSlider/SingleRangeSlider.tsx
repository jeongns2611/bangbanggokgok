'use client';

import { useRef } from 'react';
import styles from './RangeSlider.module.css';

type SingleRangeSliderProps = {
  label?: string;
  min: number;
  max: number;
  step?: number;
  value: number;
  unit?: string;
  onChange: (value: number) => void;
};

export default function SingleRangeSlider({
  label,
  min,
  max,
  step = 1,
  value,
  unit = '',
  onChange,
}: SingleRangeSliderProps) {
  const sliderAreaRef = useRef<HTMLDivElement | null>(null);

  const percent = ((value - min) / (max - min)) * 100;

  const clamp = (target: number, minVal: number, maxVal: number) =>
    Math.min(Math.max(target, minVal), maxVal);

  const handleSliderChange = (nextValue: number) => {
    const clampedValue = clamp(nextValue, min, max);
    onChange(clampedValue);
  };

  const isLargeManwonRange = unit === '만원' && max >= 10000;

  const formatManwonToEok = (targetValue: number) => {
    if (targetValue >= 10000) {
      const eok = Math.floor(targetValue / 10000);
      const man = targetValue % 10000;

      if (man === 0) {
        return `${eok}억`;
      }

      return `${eok}억 ${man.toLocaleString()}만원`;
    }

    return `${targetValue.toLocaleString()}만원`;
  };

  const formatDisplayValue = (targetValue: number) => {
    if (targetValue === max) {
      return '무제한';
    }

    if (isLargeManwonRange) {
      return formatManwonToEok(targetValue);
    }

    return `${targetValue.toLocaleString()}${unit}`;
  };

  const formatEdgeValue = (targetValue: number) => {
    if (isLargeManwonRange) {
      return formatManwonToEok(targetValue);
    }

    return `${targetValue.toLocaleString()}${unit}`;
  };

  const getValueFromPointer = (clientX: number) => {
    const rect = sliderAreaRef.current?.getBoundingClientRect();
    if (!rect) return value;

    const ratio = Math.min(Math.max((clientX - rect.left) / rect.width, 0), 1);
    const rawValue = min + ratio * (max - min);
    const steppedValue = Math.round(rawValue / step) * step;

    return clamp(steppedValue, min, max);
  };

  const handleTrackPointerDown = (e: React.PointerEvent<HTMLDivElement>) => {
    const nextValue = getValueFromPointer(e.clientX);
    onChange(nextValue);
  };

  return (
    <div className={styles.wrapper}>
      <div className={styles.header}>
        {label && <span className={styles.label}>{label}</span>}

        <div className={styles.values}>
          <div className={styles.inputBox}>
            <span className={styles.valueText}>
              {formatDisplayValue(value)}
            </span>
          </div>
        </div>
      </div>

      <div className={styles.sliderRow}>
        <span className={styles.edgeLabel}>{formatEdgeValue(min)}</span>

        <div
          ref={sliderAreaRef}
          className={styles.sliderArea}
          onPointerDown={handleTrackPointerDown}
        >
          <div className={styles.track} />

          <div
            className={styles.range}
            style={{
              left: 0,
              width: `${percent}%`,
            }}
          />

          <input
            type="range"
            min={min}
            max={max}
            step={step}
            value={value}
            onChange={(e) => handleSliderChange(Number(e.target.value))}
            className={styles.slider}
          />
        </div>

        <span className={styles.edgeLabel}>무제한</span>
      </div>
    </div>
  );
}