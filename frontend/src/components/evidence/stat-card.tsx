import * as React from "react";
import { Card, CardContent } from "@/components/ui/card";
import { cn } from "@/lib/utils";

export function StatCard({
  label,
  value,
  hint,
  icon,
  accent,
}: {
  label: string;
  value: number | string;
  hint?: string;
  icon?: React.ReactNode;
  accent?: string;
}) {
  return (
    <Card>
      <CardContent className="flex items-center justify-between p-5">
        <div>
          <p className="text-sm font-medium text-muted-foreground">{label}</p>
          <p className={cn("mt-1 text-3xl font-semibold tabular-nums", accent)}>{value}</p>
          {hint ? <p className="mt-1 text-xs text-muted-foreground">{hint}</p> : null}
        </div>
        {icon ? <div className="text-muted-foreground/70">{icon}</div> : null}
      </CardContent>
    </Card>
  );
}
