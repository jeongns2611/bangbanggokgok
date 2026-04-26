'use client';

import { useRef } from 'react';
import styles from './RangeSlider.module.css';

type DoubleRangeSliderProps = {
  label?: string;
  min: number;
  max: number;
  step?: number;
  minValue: number;
  maxValue: number;
  unit?: string;
  onChange: (next: { minValue: number; maxValue: number }) => void;
};

export default function DoubleRangeSlider({
  label,
  min,
  max,
  step = 1,
  minValue,
  maxValue,
  unit = '',
  onChange,
}: DoubleRangeSliderProps) {
  const sliderAreaRef = useRef<HTMLDivElement | null>(null);

  const clamp = (value: number, minVal: number, maxVal: number) =>
    Math.min(Math.max(value, minVal), maxVal);

  const minPercent = ((minValue - min) / (max - min)) * 100;
  const maxPercent = ((maxValue - min) / (max - min)) * 100;

  const applyRangeChange = (type: 'min' | 'max', rawValue: number) => {
    if (type === 'min') {
      const nextMin = clamp(rawValue, min, maxValue - step);

      onChange({
        minValue: nextMin,
        maxValue,
      });
      return;
    }

    const nextMax = clamp(rawValue, minValue + step, max);

    onChange({
      minValue,
      maxValue: nextMax,
    });
  };

  const isLargeManwonRange = unit === '만원' && max >= 10000;

  const formatManwonToEok = (value: number) => {
    if (value >= 10000) {
      const eok = Math.floor(value / 10000);
      const man = value % 10000;

      if (man === 0) {
        return `${eok}억`;
      }

      return `${eok}억 ${man.toLocaleString()}만원`;
    }

    return `${value.toLocaleString()}만원`;
  };

  const formatDisplayValue = (value: number, isMax: boolean) => {
    if (isMax && value === max) {
      return '무제한';
    }

    if (isLargeManwonRange) {
      return formatManwonToEok(value);
    }

    return `${value.toLocaleString()}${unit}`;
  };

  const formatEdgeValue = (value: number) => {
    if (isLargeManwonRange) {
      return formatManwonToEok(value);
    }

    return `${value.toLocaleString()}${unit}`;
  };

  const getValueFromPointer = (clientX: number) => {
    const rect = sliderAreaRef.current?.getBoundingClientRect();
    if (!rect) return minValue;

    const ratio = Math.min(Math.max((clientX - rect.left) / rect.width, 0), 1);
    const rawValue = min + ratio * (max - min);
    const steppedValue = Math.round(rawValue / step) * step;

    return clamp(steppedValue, min, max);
  };

  const handleTrackPointerDown = (e: React.PointerEvent<HTMLDivElement>) => {
    const clickedValue = getValueFromPointer(e.clientX);

    const distanceToMin = Math.abs(clickedValue - minValue);
    const distanceToMax = Math.abs(clickedValue - maxValue);

    if (distanceToMin <= distanceToMax) {
      const nextMin = clamp(clickedValue, min, maxValue - step);

      onChange({
        minValue: nextMin,
        maxValue,
      });
      return;
    }

    const nextMax = clamp(clickedValue, minValue + step, max);

    onChange({
      minValue,
      maxValue: nextMax,
    });
  };

  return (
    <div className={styles.wrapper}>
      <div className={styles.header}>
        {label && <span className={styles.label}>{label}</span>}

        <div className={styles.values}>
          <div className={styles.inputBox}>
            <span className={styles.valueText}>
              {formatDisplayValue(minValue, false)}
            </span>
          </div>

          <span className={styles.tilde}>~</span>

          <div className={styles.inputBox}>
            <span className={styles.valueText}>
              {formatDisplayValue(maxValue, true)}
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
              left: `${minPercent}%`,
              width: `${Math.max(maxPercent - minPercent, 0)}%`,
            }}
          />

          <input
            type="range"
            min={min}
            max={max}
            step={step}
            value={maxValue}
            onChange={(e) => applyRangeChange('max', Number(e.target.value))}
            className={styles.slider}
          />

          <input
            type="range"
            min={min}
            max={max}
            step={step}
            value={minValue}
            onChange={(e) => applyRangeChange('min', Number(e.target.value))}
            className={styles.slider}
          />
        </div>

        <span className={styles.edgeLabel}>무제한</span>
      </div>
    </div>
  );
}