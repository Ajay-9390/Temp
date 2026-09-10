"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { StatusBadge } from "@/components/status-badge";
import type { Program, ProgramOverview } from "@/lib/api/types";

function Stat({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="rounded-md border p-3">
      <p className="text-xs uppercase tracking-wide text-muted-foreground">{label}</p>
      <p className="mt-1 font-medium">{value}</p>
    </div>
  );
}

/** Lightweight program dashboard. Deliberately shows NO attainment/readiness numbers —
 *  those come from future modules, surfaced here only as integration placeholders. */
export function OverviewTab({ overview, program }: { overview?: ProgramOverview; program: Program }) {
  return (
    <div className="grid gap-6 lg:grid-cols-3">
      <Card className="lg:col-span-2">
        <CardHeader><CardTitle className="text-base">Program Information</CardTitle></CardHeader>
        <CardContent className="grid gap-3 sm:grid-cols-2">
          <Stat label="Program" value={program.name} />
          <Stat label="Code" value={program.code} />
          <Stat label="Department" value={overview?.departmentName ?? program.departmentName ?? "—"} />
          <Stat label="Institution" value={overview?.institutionName ?? "—"} />
          <Stat label="Duration" value={`${program.durationYears} years`} />
          <Stat label="Total Semesters" value={program.totalSemesters} />
          <Stat label="Intake" value={program.intake} />
          <Stat label="Status" value={<StatusBadge status={program.status} />} />
        </CardContent>
      </Card>

      <div className="space-y-6">
        <Card>
          <CardHeader><CardTitle className="text-base">Accreditation</CardTitle></CardHeader>
          <CardContent className="space-y-2 text-sm">
            <div className="flex justify-between"><span className="text-muted-foreground">Current cycle</span>
              <span>{overview?.currentAccreditation?.name ?? "—"}</span></div>
            <div className="flex justify-between"><span className="text-muted-foreground">Tier</span>
              <span>{overview?.tier ?? "—"}</span></div>
            <div className="flex justify-between items-center"><span className="text-muted-foreground">Status</span>
              <StatusBadge status={overview?.currentAccreditation?.status} /></div>
            <div className="flex justify-between"><span className="text-muted-foreground">Active year</span>
              <span>{overview?.currentAcademicYear?.name ?? "—"}</span></div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">Integration Points</CardTitle>
          </CardHeader>
          <CardContent className="space-y-1.5 text-sm">
            {overview && Object.entries(overview.integrationPoints).map(([k, v]) => (
              <div key={k} className="flex justify-between">
                <span className="capitalize text-muted-foreground">{k}</span>
                <span className="text-xs">{v}</span>
              </div>
            ))}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
