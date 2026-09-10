"use client";

import * as React from "react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Skeleton } from "@/components/ui/skeleton";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { PageHeader } from "@/components/evidence/page-header";
import { StatCard } from "@/components/evidence/stat-card";
import { GapStatusBadge } from "@/components/evidence/status-badge";
import { useGaps } from "@/hooks/use-evidence";
import { CATEGORY_LABELS } from "@/lib/constants";
import { EVIDENCE_STATUSES } from "@/lib/types";
import { shortId } from "@/lib/utils";

const ALL = "ALL";

export default function GapsPage() {
  const [programId, setProgramId] = React.useState("");
  const [academicYearId, setAcademicYearId] = React.useState("");
  const [criterionId, setCriterionId] = React.useState("");
  const [status, setStatus] = React.useState("");

  const { data, isLoading } = useGaps({
    programId: programId.trim() || undefined,
    academicYearId: academicYearId.trim() || undefined,
    criterionId: criterionId.trim() || undefined,
    status: status || undefined,
  });

  return (
    <div className="space-y-6">
      <PageHeader title="Evidence Gaps" description="Required evidence vs. what has been uploaded and approved" />

      <Card>
        <CardContent className="grid grid-cols-1 gap-3 p-4 sm:grid-cols-2 lg:grid-cols-4">
          <div className="space-y-1">
            <Label htmlFor="programId">Program ID</Label>
            <Input id="programId" placeholder="UUID" value={programId} onChange={(e) => setProgramId(e.target.value)} />
          </div>
          <div className="space-y-1">
            <Label htmlFor="academicYearId">Academic Year ID</Label>
            <Input id="academicYearId" placeholder="UUID" value={academicYearId} onChange={(e) => setAcademicYearId(e.target.value)} />
          </div>
          <div className="space-y-1">
            <Label htmlFor="criterionId">Criterion ID</Label>
            <Input id="criterionId" placeholder="UUID" value={criterionId} onChange={(e) => setCriterionId(e.target.value)} />
          </div>
          <div className="space-y-1">
            <Label>Status</Label>
            <Select value={status || ALL} onValueChange={(v) => setStatus(v === ALL ? "" : v)}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>All</SelectItem>
                <SelectItem value="MISSING">Missing</SelectItem>
                {EVIDENCE_STATUSES.map((s) => (
                  <SelectItem key={s} value={s}>
                    {s}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {isLoading || !data ? (
        <Skeleton className="h-64 w-full" />
      ) : (
        <>
          <div className="grid grid-cols-2 gap-4 lg:grid-cols-6">
            <StatCard label="Required" value={data.totalRequired} />
            <StatCard label="Uploaded" value={data.uploaded} />
            <StatCard label="Approved" value={data.approved} accent="text-emerald-600" />
            <StatCard label="Pending" value={data.pending} accent="text-amber-600" />
            <StatCard label="Rejected" value={data.rejected} accent="text-red-600" />
            <StatCard label="Missing" value={data.missing} accent="text-red-600" />
          </div>

          <div className="rounded-lg border bg-card">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Criterion</TableHead>
                  <TableHead>Requirement</TableHead>
                  <TableHead>Expected Evidence</TableHead>
                  <TableHead>Category</TableHead>
                  <TableHead>Mandatory</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Action</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data.items.map((item) => (
                  <TableRow key={`${item.criterionId}-${item.requirementId}`}>
                    <TableCell className="font-medium">{item.criterionCode}</TableCell>
                    <TableCell>{item.requirementCode}</TableCell>
                    <TableCell>{item.expectedEvidence}</TableCell>
                    <TableCell>{CATEGORY_LABELS[item.expectedCategory]}</TableCell>
                    <TableCell>{item.mandatory ? "Yes" : "No"}</TableCell>
                    <TableCell>
                      <GapStatusBadge status={item.currentStatus} />
                    </TableCell>
                    <TableCell>
                      {item.evidenceId ? (
                        <Button asChild variant="link" className="h-auto p-0">
                          <Link href={`/evidence/${item.evidenceId}`}>View</Link>
                        </Button>
                      ) : (
                        <span className="text-xs text-muted-foreground">{shortId(item.criterionId)}</span>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
                {data.items.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} className="h-24 text-center text-muted-foreground">
                      No gap items for the selected filters.
                    </TableCell>
                  </TableRow>
                ) : null}
              </TableBody>
            </Table>
          </div>
        </>
      )}
    </div>
  );
}
