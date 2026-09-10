"use client";

import * as React from "react";
import Link from "next/link";
import {
  AlertTriangle,
  CheckCircle2,
  Clock,
  FileStack,
  ListChecks,
  Upload,
  XCircle,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import { PageHeader } from "@/components/evidence/page-header";
import { StatCard } from "@/components/evidence/stat-card";
import { StatusBarChart } from "@/components/evidence/status-bar-chart";
import { useStatistics } from "@/hooks/use-evidence";
import { CATEGORY_LABELS, STATUS_META } from "@/lib/constants";
import { EVIDENCE_CATEGORIES, EVIDENCE_STATUSES } from "@/lib/types";

export default function DashboardPage() {
  const [programId, setProgramId] = React.useState("");
  const [academicYearId, setAcademicYearId] = React.useState("");
  const scope = {
    programId: programId.trim() || undefined,
    academicYearId: academicYearId.trim() || undefined,
  };
  const { data, isLoading } = useStatistics(scope);

  const statusData = data
    ? EVIDENCE_STATUSES.map((s) => ({
        label: STATUS_META[s].label,
        value: data.byStatus[s] ?? 0,
        className: statusBarColor(s),
      }))
    : [];

  const categoryData = data
    ? EVIDENCE_CATEGORIES.map((c) => ({ label: CATEGORY_LABELS[c], value: data.byCategory[c] ?? 0 })).filter(
        (d) => d.value > 0,
      )
    : [];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Evidence Dashboard"
        description="Overview of NBA accreditation evidence lifecycle"
        actions={
          <Button asChild>
            <Link href="/evidence/upload">
              <Upload className="h-4 w-4" /> Upload Evidence
            </Link>
          </Button>
        }
      />

      <Card>
        <CardContent className="flex flex-col gap-3 p-4 sm:flex-row sm:items-end">
          <div className="flex-1 space-y-1">
            <Label htmlFor="programId">Program ID (optional)</Label>
            <Input id="programId" placeholder="UUID to scope stats" value={programId} onChange={(e) => setProgramId(e.target.value)} />
          </div>
          <div className="flex-1 space-y-1">
            <Label htmlFor="academicYearId">Academic Year ID (optional)</Label>
            <Input id="academicYearId" placeholder="UUID to scope gaps" value={academicYearId} onChange={(e) => setAcademicYearId(e.target.value)} />
          </div>
        </CardContent>
      </Card>

      {isLoading || !data ? (
        <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
          {Array.from({ length: 8 }).map((_, i) => (
            <Skeleton key={i} className="h-24" />
          ))}
        </div>
      ) : (
        <>
          <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
            <StatCard label="Total Evidence" value={data.totalEvidence} icon={<FileStack className="h-6 w-6" />} />
            <StatCard label="Required" value={data.required} hint="From NBA Criteria" icon={<ListChecks className="h-6 w-6" />} />
            <StatCard label="Approved" value={data.approved} accent="text-emerald-600" icon={<CheckCircle2 className="h-6 w-6" />} />
            <StatCard label="Under Review" value={data.underReview} accent="text-amber-600" icon={<Clock className="h-6 w-6" />} />
            <StatCard label="Changes Required" value={data.changesRequired} accent="text-orange-600" icon={<AlertTriangle className="h-6 w-6" />} />
            <StatCard label="Rejected" value={data.rejected} accent="text-red-600" icon={<XCircle className="h-6 w-6" />} />
            <StatCard label="Draft" value={data.draft} />
            <StatCard label="Missing" value={data.missing} accent="text-red-600" hint="Required but not present" />
          </div>

          <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>By Status</CardTitle>
              </CardHeader>
              <CardContent>
                <StatusBarChart data={statusData} />
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle>By Category</CardTitle>
              </CardHeader>
              <CardContent>
                {categoryData.length ? (
                  <StatusBarChart data={categoryData} />
                ) : (
                  <p className="text-sm text-muted-foreground">No evidence yet.</p>
                )}
              </CardContent>
            </Card>
          </div>
        </>
      )}
    </div>
  );
}

function statusBarColor(status: string): string {
  switch (status) {
    case "APPROVED":
      return "bg-emerald-500";
    case "UNDER_REVIEW":
      return "bg-amber-500";
    case "CHANGES_REQUIRED":
      return "bg-orange-500";
    case "REJECTED":
      return "bg-red-500";
    case "SUBMITTED":
      return "bg-blue-500";
    case "ARCHIVED":
      return "bg-zinc-400";
    default:
      return "bg-slate-400";
  }
}
