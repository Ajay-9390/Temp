"use client";

import Link from "next/link";
import { Button } from "@/components/ui/button";
import { StatusBadge } from "@/components/status-badge";
import { useSemesters } from "./hooks";
import type { AcademicYear } from "@/lib/api/types";

/** Read-only semester list for a single academic year, used inside the program detail tab. */
export function SemestersByYear({ year }: { year: AcademicYear }) {
  const { data, isLoading } = useSemesters(year.id);

  return (
    <div className="rounded-md border">
      <div className="flex items-center justify-between border-b bg-muted/40 px-3 py-2">
        <p className="font-medium">{year.name}</p>
        <Button asChild size="sm" variant="ghost">
          <Link href={`/academic-years/${year.id}/semesters`}>Manage</Link>
        </Button>
      </div>
      <div className="divide-y">
        {isLoading && <p className="px-3 py-2 text-sm text-muted-foreground">Loading...</p>}
        {data?.length === 0 && <p className="px-3 py-2 text-sm text-muted-foreground">No semesters.</p>}
        {data?.map((s) => (
          <div key={s.id} className="flex items-center justify-between px-3 py-2 text-sm">
            <span>{s.name} <span className="text-muted-foreground">(#{s.semesterNumber})</span></span>
            <StatusBadge status={s.status} />
          </div>
        ))}
      </div>
    </div>
  );
}
