"use client";

import Link from "next/link";
import { formatAttainment } from "@/lib/utils";
import { StatusBadge } from "@/components/ui/badge";
import { AttainmentBar } from "./AttainmentBar";
import type { POAttainmentResponse, PSOAttainmentResponse } from "@/lib/api/types";

// ─── PO Table ─────────────────────────────────────────────────────────────────

interface POTableProps {
  data: POAttainmentResponse[];
  isLoading?: boolean;
}

export function POAttainmentTable({ data, isLoading }: POTableProps) {
  if (isLoading) return <TableSkeleton rows={6} />;
  if (!data.length) return <EmptyState label="No PO attainment records found." />;

  return (
    <div className="overflow-x-auto rounded-lg border" data-testid="po-table">
      <table className="w-full text-sm">
        <thead className="bg-muted/50">
          <tr>
            <th className="px-4 py-3 text-left font-medium">PO</th>
            <th className="px-4 py-3 text-left font-medium">Description</th>
            <th className="px-4 py-3 text-left font-medium">Attainment</th>
            <th className="px-4 py-3 text-left font-medium">Status</th>
            <th className="px-4 py-3 text-left font-medium">Method</th>
            <th className="px-4 py-3 text-left font-medium">Version</th>
            <th className="px-4 py-3 text-right font-medium">Actions</th>
          </tr>
        </thead>
        <tbody className="divide-y">
          {data.map((row) => (
            <tr key={row.id} className="hover:bg-muted/30 transition-colors">
              <td className="px-4 py-3 font-semibold">{row.poCode}</td>
              <td className="px-4 py-3 text-muted-foreground max-w-xs truncate" title={row.poDescription}>
                {row.poDescription}
              </td>
              <td className="px-4 py-3 min-w-[160px]">
                <AttainmentBar value={row.finalAttainment} />
              </td>
              <td className="px-4 py-3">
                <StatusBadge status={row.status} />
              </td>
              <td className="px-4 py-3 text-muted-foreground">{row.calculationMethod}</td>
              <td className="px-4 py-3 text-muted-foreground">{row.calculationVersion}</td>
              <td className="px-4 py-3 text-right">
                <Link
                  href={`/attainment/po/${row.id}`}
                  className="text-primary hover:underline text-xs font-medium"
                  data-testid={`po-detail-link-${row.poCode}`}
                >
                  Details →
                </Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

// ─── PSO Table ────────────────────────────────────────────────────────────────

interface PSOTableProps {
  data: PSOAttainmentResponse[];
  isLoading?: boolean;
}

export function PSOAttainmentTable({ data, isLoading }: PSOTableProps) {
  if (isLoading) return <TableSkeleton rows={3} />;
  if (!data.length) return <EmptyState label="No PSO attainment records found." />;

  return (
    <div className="overflow-x-auto rounded-lg border" data-testid="pso-table">
      <table className="w-full text-sm">
        <thead className="bg-muted/50">
          <tr>
            <th className="px-4 py-3 text-left font-medium">PSO</th>
            <th className="px-4 py-3 text-left font-medium">Description</th>
            <th className="px-4 py-3 text-left font-medium">Attainment</th>
            <th className="px-4 py-3 text-left font-medium">Status</th>
            <th className="px-4 py-3 text-left font-medium">Method</th>
            <th className="px-4 py-3 text-left font-medium">Version</th>
            <th className="px-4 py-3 text-right font-medium">Actions</th>
          </tr>
        </thead>
        <tbody className="divide-y">
          {data.map((row) => (
            <tr key={row.id} className="hover:bg-muted/30 transition-colors">
              <td className="px-4 py-3 font-semibold">{row.psoCode}</td>
              <td className="px-4 py-3 text-muted-foreground max-w-xs truncate" title={row.psoDescription}>
                {row.psoDescription}
              </td>
              <td className="px-4 py-3 min-w-[160px]">
                <AttainmentBar value={row.finalAttainment} />
              </td>
              <td className="px-4 py-3">
                <StatusBadge status={row.status} />
              </td>
              <td className="px-4 py-3 text-muted-foreground">{row.calculationMethod}</td>
              <td className="px-4 py-3 text-muted-foreground">{row.calculationVersion}</td>
              <td className="px-4 py-3 text-right">
                <Link
                  href={`/attainment/pso/${row.id}`}
                  className="text-primary hover:underline text-xs font-medium"
                  data-testid={`pso-detail-link-${row.psoCode}`}
                >
                  Details →
                </Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

// ─── Shared helpers ───────────────────────────────────────────────────────────

function TableSkeleton({ rows }: { rows: number }) {
  return (
    <div className="rounded-lg border overflow-hidden animate-pulse" aria-busy="true" aria-label="Loading">
      <div className="h-10 bg-muted/50" />
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="h-12 border-t bg-muted/20" />
      ))}
    </div>
  );
}

function EmptyState({ label }: { label: string }) {
  return (
    <div className="rounded-lg border p-8 text-center text-muted-foreground text-sm">
      {label}
    </div>
  );
}
