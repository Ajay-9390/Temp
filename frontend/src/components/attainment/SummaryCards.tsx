"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { StatusBadge } from "@/components/ui/badge";
import { formatAttainment } from "@/lib/utils";
import type { AttainmentSummaryResponse } from "@/lib/api/types";
import { TrendingUp, TrendingDown, Award, AlertTriangle } from "lucide-react";

interface SummaryCardsProps {
  summary: AttainmentSummaryResponse;
}

export function SummaryCards({ summary }: SummaryCardsProps) {
  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4" data-testid="summary-cards">
      <MetricCard
        title="Avg PO Attainment"
        value={formatAttainment(summary.averagePOAttainment)}
        icon={<TrendingUp className="h-4 w-4 text-muted-foreground" aria-hidden />}
        description="Across all Program Outcomes"
      />
      <MetricCard
        title="Avg PSO Attainment"
        value={formatAttainment(summary.averagePSOAttainment)}
        icon={<TrendingUp className="h-4 w-4 text-muted-foreground" aria-hidden />}
        description="Across all Program Specific Outcomes"
      />
      <MetricCard
        title="Highest PO"
        value={summary.highestPO
          ? `${summary.highestPO.poCode} — ${formatAttainment(summary.highestPO.finalAttainment)}`
          : "—"}
        icon={<Award className="h-4 w-4 text-green-500" aria-hidden />}
        description={summary.highestPO?.poDescription ?? ""}
      />
      <MetricCard
        title="Lowest PO"
        value={summary.lowestPO
          ? `${summary.lowestPO.poCode} — ${formatAttainment(summary.lowestPO.finalAttainment)}`
          : "—"}
        icon={<AlertTriangle className="h-4 w-4 text-amber-500" aria-hidden />}
        description={summary.lowestPO?.poDescription ?? ""}
      />
    </div>
  );
}

function MetricCard({
  title, value, icon, description,
}: {
  title: string;
  value: string;
  icon: React.ReactNode;
  description: string;
}) {
  return (
    <Card>
      <CardHeader className="pb-2">
        <div className="flex items-center justify-between">
          <CardTitle className="text-sm font-medium text-muted-foreground">{title}</CardTitle>
          {icon}
        </div>
      </CardHeader>
      <CardContent>
        <p className="text-2xl font-bold">{value}</p>
        <p className="text-xs text-muted-foreground mt-1 truncate" title={description}>
          {description}
        </p>
      </CardContent>
    </Card>
  );
}
