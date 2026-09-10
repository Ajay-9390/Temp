"use client";

import * as React from "react";
import { Download, FileSpreadsheet, FileText } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { PageHeader } from "@/components/evidence/page-header";
import { api } from "@/lib/api";
import { CATEGORY_LABELS, STATUS_META } from "@/lib/constants";
import { EVIDENCE_CATEGORIES, EVIDENCE_STATUSES } from "@/lib/types";

const ALL = "ALL";
const FORMATS: { key: string; label: string; icon: React.ReactNode }[] = [
  { key: "xlsx", label: "Excel", icon: <FileSpreadsheet className="h-4 w-4" /> },
  { key: "pdf", label: "PDF", icon: <FileText className="h-4 w-4" /> },
  { key: "csv", label: "CSV", icon: <Download className="h-4 w-4" /> },
];

export default function ReportsPage() {
  const [programId, setProgramId] = React.useState("");
  const [academicYearId, setAcademicYearId] = React.useState("");
  const [category, setCategory] = React.useState("");
  const [status, setStatus] = React.useState("");

  const completionParams = (format: string) => ({
    programId: programId.trim() || undefined,
    academicYearId: academicYearId.trim() || undefined,
    format,
  });
  const statusParams = (format: string) => ({
    programId: programId.trim() || undefined,
    academicYearId: academicYearId.trim() || undefined,
    category: category || undefined,
    status: status || undefined,
    format,
  });

  return (
    <div className="space-y-6">
      <PageHeader title="Evidence Reports" description="Generate completion and status reports (Excel / PDF / CSV)" />

      <Card>
        <CardHeader>
          <CardTitle>Scope</CardTitle>
          <CardDescription>Optional filters applied to the reports below.</CardDescription>
        </CardHeader>
        <CardContent className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
          <div className="space-y-1">
            <Label htmlFor="programId">Program ID</Label>
            <Input id="programId" placeholder="UUID" value={programId} onChange={(e) => setProgramId(e.target.value)} />
          </div>
          <div className="space-y-1">
            <Label htmlFor="academicYearId">Academic Year ID</Label>
            <Input id="academicYearId" placeholder="UUID" value={academicYearId} onChange={(e) => setAcademicYearId(e.target.value)} />
          </div>
          <div className="space-y-1">
            <Label>Category (status report)</Label>
            <Select value={category || ALL} onValueChange={(v) => setCategory(v === ALL ? "" : v)}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>All</SelectItem>
                {EVIDENCE_CATEGORIES.map((c) => (
                  <SelectItem key={c} value={c}>
                    {CATEGORY_LABELS[c]}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-1">
            <Label>Status (status report)</Label>
            <Select value={status || ALL} onValueChange={(v) => setStatus(v === ALL ? "" : v)}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>All</SelectItem>
                {EVIDENCE_STATUSES.map((s) => (
                  <SelectItem key={s} value={s}>
                    {STATUS_META[s].label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Completion Report</CardTitle>
            <CardDescription>Required / uploaded / approved / missing and completion % by criterion.</CardDescription>
          </CardHeader>
          <CardContent className="flex flex-wrap gap-2">
            {FORMATS.map((f) => (
              <Button key={f.key} asChild variant="outline">
                <a href={api.reports.completionUrl(completionParams(f.key))}>
                  {f.icon} {f.label}
                </a>
              </Button>
            ))}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Status Report</CardTitle>
            <CardDescription>Matching evidence with program, criterion, status and academic year.</CardDescription>
          </CardHeader>
          <CardContent className="flex flex-wrap gap-2">
            {FORMATS.map((f) => (
              <Button key={f.key} asChild variant="outline">
                <a href={api.reports.statusUrl(statusParams(f.key))}>
                  {f.icon} {f.label}
                </a>
              </Button>
            ))}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
