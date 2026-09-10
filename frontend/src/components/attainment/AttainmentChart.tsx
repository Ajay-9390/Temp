"use client";

import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, ReferenceLine, Cell,
} from "recharts";
import type { POAttainmentResponse, PSOAttainmentResponse } from "@/lib/api/types";

interface Props {
  data: POAttainmentResponse[] | PSOAttainmentResponse[];
  codeKey: "poCode" | "psoCode";
  title: string;
}

export function AttainmentChart({ data, codeKey, title }: Props) {
  const chartData = data
    .filter((r) => (r as POAttainmentResponse).finalAttainment != null)
    .map((r) => ({
      code: (r as POAttainmentResponse)[codeKey as "poCode"] ?? (r as PSOAttainmentResponse)[codeKey as "psoCode"],
      value: ((r as POAttainmentResponse).finalAttainment ?? 0),
    }));

  if (!chartData.length) return null;

  return (
    <div className="space-y-2">
      <h3 className="text-sm font-medium text-muted-foreground">{title}</h3>
      <ResponsiveContainer width="100%" height={220}>
        <BarChart data={chartData} margin={{ top: 4, right: 8, left: 0, bottom: 4 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
          <XAxis dataKey="code" tick={{ fontSize: 11 }} />
          <YAxis domain={[0, 100]} tick={{ fontSize: 11 }} unit="%" />
          <Tooltip
            formatter={(v: number) => [`${v.toFixed(2)}%`, "Attainment"]}
            contentStyle={{ fontSize: 12 }}
          />
          <ReferenceLine y={75} stroke="#22c55e" strokeDasharray="4 2" label={{ value: "75%", fontSize: 10 }} />
          <ReferenceLine y={60} stroke="#3b82f6" strokeDasharray="4 2" label={{ value: "60%", fontSize: 10 }} />
          <Bar dataKey="value" radius={[3, 3, 0, 0]} maxBarSize={40}>
            {chartData.map((entry) => (
              <Cell
                key={entry.code}
                fill={entry.value >= 75 ? "#22c55e" : entry.value >= 60 ? "#3b82f6" : "#f59e0b"}
              />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
