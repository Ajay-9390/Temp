import { cn } from "@/lib/utils";

export interface BarDatum {
  label: string;
  value: number;
  className?: string;
}

export function StatusBarChart({ data }: { data: BarDatum[] }) {
  const max = Math.max(1, ...data.map((d) => d.value));
  return (
    <div className="space-y-3">
      {data.map((d) => (
        <div key={d.label} className="flex items-center gap-3">
          <div className="w-36 shrink-0 truncate text-sm text-muted-foreground">{d.label}</div>
          <div className="flex h-6 flex-1 items-center overflow-hidden rounded-md bg-muted">
            <div
              className={cn("h-full rounded-md transition-all", d.className ?? "bg-primary")}
              style={{ width: `${(d.value / max) * 100}%` }}
            />
          </div>
          <div className="w-10 shrink-0 text-right text-sm font-medium tabular-nums">{d.value}</div>
        </div>
      ))}
    </div>
  );
}
