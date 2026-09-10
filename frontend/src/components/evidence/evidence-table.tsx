"use client";

import Link from "next/link";
import { ArrowDown, ArrowUp, ChevronsUpDown } from "lucide-react";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Skeleton } from "@/components/ui/skeleton";
import { StatusBadge } from "@/components/evidence/status-badge";
import { CATEGORY_LABELS } from "@/lib/constants";
import { formatDate, shortId } from "@/lib/utils";
import type { EvidenceSummaryResponse } from "@/lib/types";

interface Props {
  rows: EvidenceSummaryResponse[];
  isLoading?: boolean;
  sort: string;
  onSortChange: (field: string) => void;
}

const SORTABLE = new Set(["title", "category", "status", "createdAt", "updatedAt"]);

export function EvidenceTable({ rows, isLoading, sort, onSortChange }: Props) {
  const [sortField, sortDir] = sort.split(",");

  const header = (label: string, field?: string, className?: string) => {
    const sortable = field && SORTABLE.has(field);
    return (
      <TableHead className={className}>
        {sortable ? (
          <button
            type="button"
            onClick={() => onSortChange(field!)}
            className="inline-flex items-center gap-1 hover:text-foreground"
          >
            {label}
            {sortField === field ? (
              sortDir === "asc" ? <ArrowUp className="h-3.5 w-3.5" /> : <ArrowDown className="h-3.5 w-3.5" />
            ) : (
              <ChevronsUpDown className="h-3.5 w-3.5 opacity-50" />
            )}
          </button>
        ) : (
          label
        )}
      </TableHead>
    );
  };

  return (
    <div className="rounded-lg border bg-card">
      <Table>
        <TableHeader>
          <TableRow>
            {header("ID")}
            {header("Title", "title")}
            {header("Category", "category")}
            {header("Program")}
            {header("Academic Year")}
            {header("Criterion")}
            {header("Requirement")}
            {header("Type")}
            {header("Status", "status")}
            {header("Ver.")}
            {header("Uploaded", "createdAt")}
            {header("Updated", "updatedAt")}
          </TableRow>
        </TableHeader>
        <TableBody>
          {isLoading
            ? Array.from({ length: 6 }).map((_, i) => (
                <TableRow key={i}>
                  {Array.from({ length: 12 }).map((__, j) => (
                    <TableCell key={j}>
                      <Skeleton className="h-4 w-16" />
                    </TableCell>
                  ))}
                </TableRow>
              ))
            : rows.map((row) => (
                <TableRow key={row.id}>
                  <TableCell className="font-mono text-xs text-muted-foreground">{shortId(row.id)}</TableCell>
                  <TableCell className="max-w-[240px]">
                    <Link href={`/evidence/${row.id}`} className="font-medium text-primary hover:underline">
                      {row.title}
                    </Link>
                  </TableCell>
                  <TableCell>{CATEGORY_LABELS[row.category]}</TableCell>
                  <TableCell className="font-mono text-xs">{shortId(row.programId)}</TableCell>
                  <TableCell className="font-mono text-xs">{shortId(row.academicYearId)}</TableCell>
                  <TableCell className="font-mono text-xs">{shortId(row.criterionId)}</TableCell>
                  <TableCell className="font-mono text-xs">{shortId(row.requirementId)}</TableCell>
                  <TableCell className="uppercase">{row.latestFileType ?? "—"}</TableCell>
                  <TableCell>
                    <StatusBadge status={row.status} />
                  </TableCell>
                  <TableCell className="tabular-nums">{row.currentVersion}</TableCell>
                  <TableCell className="whitespace-nowrap text-sm text-muted-foreground">{formatDate(row.createdAt)}</TableCell>
                  <TableCell className="whitespace-nowrap text-sm text-muted-foreground">{formatDate(row.updatedAt)}</TableCell>
                </TableRow>
              ))}
          {!isLoading && rows.length === 0 ? (
            <TableRow>
              <TableCell colSpan={12} className="h-24 text-center text-muted-foreground">
                No evidence found. Adjust filters or upload new evidence.
              </TableCell>
            </TableRow>
          ) : null}
        </TableBody>
      </Table>
    </div>
  );
}
