'use client';

import { useEffect, useMemo, useState } from 'react';

interface HalfGaugeProps {
  score: number;
  orangeThreshold?: number;
  greenThreshold?: number;
  size?: number;
}

const DEFAULT_SIZE = 220;
const START_ANGLE = 180;
const END_ANGLE = 0;

const clamp = (value: number, min: number, max: number) =>
  Math.max(min, Math.min(max, value));

const polarToCartesian = (cx: number, cy: number, r: number, angle: number) => {
  const rad = (angle * Math.PI) / 180;
  return {
    x: cx + r * Math.cos(rad),
    y: cy + r * Math.sin(rad),
  };
};

const describeArc = (
  cx: number,
  cy: number,
  r: number,
  startAngle: number,
  endAngle: number
) => {
  const start = polarToCartesian(cx, cy, r, startAngle);
  const end = polarToCartesian(cx, cy, r, endAngle);

  return [
    'M',
    start.x,
    start.y,
    'A',
    r,
    r,
    0,
    0,
    1,
    end.x,
    end.y,
  ].join(' ');
};

export default function HalfGauge({
  score,
  orangeThreshold = 30,
  greenThreshold = 60,
  size = DEFAULT_SIZE,
}: HalfGaugeProps) {
  const [animatedScore, setAnimatedScore] = useState(0);
  const safeScore = clamp(score, 0, 100);

  const strokeWidth = size * 0.08;
  const center = size / 2;
  const radius = (size - strokeWidth) / 2;

  const gaugeColor = useMemo(() => {
    if (safeScore < orangeThreshold) return '#EF4444';
    if (safeScore < greenThreshold) return '#F97316';
    return '#2F7D4A';
  }, [safeScore, orangeThreshold, greenThreshold]);

  useEffect(() => {
    let frameId: number;
    const duration = 900;
    const startValue = animatedScore;
    const diff = safeScore - startValue;
    const startTime = performance.now();

    const easeOutCubic = (t: number) => 1 - Math.pow(1 - t, 3);

    const animate = (now: number) => {
      const elapsed = now - startTime;
      const progress = clamp(elapsed / duration, 0, 1);
      const eased = easeOutCubic(progress);

      setAnimatedScore(startValue + diff * eased);

      if (progress < 1) frameId = requestAnimationFrame(animate);
    };

    frameId = requestAnimationFrame(animate);
    return () => cancelAnimationFrame(frameId);
  }, [safeScore]);

  const progressAngle = START_ANGLE + (animatedScore / 100) * 180;

  const backgroundPath = describeArc(center, center, radius, START_ANGLE, END_ANGLE);
  const progressPath = describeArc(center, center, radius, START_ANGLE, progressAngle);

  return (
    <div className="flex w-fit flex-col items-center">
      <div
        className="relative"
        style={{ width: size, height: center + strokeWidth }}
      >
        <svg
          width={size}
          height={center + strokeWidth}
          viewBox={`0 0 ${size} ${center + strokeWidth}`}
          className="overflow-visible"
        >
          <path
            d={backgroundPath}
            fill="none"
            stroke="#D9DDE3"
            strokeWidth={strokeWidth}
            strokeLinecap="round"
          />

          {animatedScore > 0 && (
            <path
              d={progressPath}
              fill="none"
              stroke={gaugeColor}
              strokeWidth={strokeWidth}
              strokeLinecap="round"
              className="transition-colors duration-300"
              style={{ filter: 'drop-shadow(0 2px 4px rgba(0,0,0,0.1))' }}
            />
          )}
        </svg>

        <div className="absolute inset-0 flex items-end justify-center pb-1">
          <div className="flex items-baseline gap-0.5">
            <span
              className="font-extrabold leading-none"
              style={{
                color: gaugeColor,
                fontSize: size * 0.27,
              }}
            >
              {Math.round(animatedScore)}
            </span>
            <span
              className="font-bold leading-none"
              style={{
                color: gaugeColor,
                fontSize: size * 0.09,
              }}
            >
              점
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}