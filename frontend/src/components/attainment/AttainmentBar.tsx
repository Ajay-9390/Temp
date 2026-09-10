"use client";

import { attainmentBarColor, formatAttainment } from "@/lib/utils";

interface AttainmentBarProps {
  value: number | null;
  label?: string;
  showValue?: boolean;
}

export function AttainmentBar({ value, label, showValue = true }: AttainmentBarProps) {
  const pct = value ?? 0;
  const barColor = attainmentBarColor(value);

  return (
    <div className="space-y-1">
      {label && <span className="text-xs text-muted-foreground">{label}</span>}
      <div className="flex items-center gap-2">
        <div className="flex-1 h-2 rounded-full bg-gray-100 overflow-hidden">
          <div
            className={`h-full rounded-full transition-all duration-500 ${barColor}`}
            style={{ width: `${Math.min(pct, 100)}%` }}
            role="progressbar"
            aria-valuenow={pct}
            aria-valuemin={0}
            aria-valuemax={100}
          />
        </div>
        {showValue && (
          <span className="text-sm font-medium tabular-nums w-14 text-right">
            {formatAttainment(value)}
          </span>
        )}
      </div>
    </div>
  );
}
