"use client";

import { useState } from "react";
import Link from "next/link";
import { Plus, Search } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from "@/components/ui/table";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { StatusBadge } from "@/components/status-badge";
import { usePrograms, useProgramStatusUpdate } from "@/features/programs/hooks";
import { PROGRAM_TRANSITIONS } from "@/lib/lifecycle";
import type { Program, ProgramStatus } from "@/lib/api/types";

const STATUSES: ProgramStatus[] = ["DRAFT", "ACTIVE", "INACTIVE", "ARCHIVED"];
const PAGE_SIZE = 10;

// Friendly labels for the lifecycle actions available in the row.
const ACTION_LABEL: Record<ProgramStatus, string> = {
  ACTIVE: "Activate",
  INACTIVE: "Deactivate",
  ARCHIVED: "Archive",
  DRAFT: "Draft",
};

function ProgramRowActions({ program }: { program: Program }) {
  const statusUpdate = useProgramStatusUpdate(program.id);
  const next = PROGRAM_TRANSITIONS[program.status];
  return (
    <div className="flex flex-wrap items-center justify-end gap-1">
      <Button asChild size="sm" variant="outline"><Link href={`/programs/${program.id}`}>View</Link></Button>
      <Button asChild size="sm" variant="ghost"><Link href={`/programs/${program.id}/edit`}>Edit</Link></Button>
      {next.map((s) => (
        <Button key={s} size="sm" variant="ghost" disabled={statusUpdate.isPending}
          title={`Move to ${s}`}
          onClick={() => statusUpdate.mutate(s)}>
          {ACTION_LABEL[s]}
        </Button>
      ))}
    </div>
  );
}

export default function ProgramsPage() {
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState<ProgramStatus | "ALL">("ALL");
  const [page, setPage] = useState(0);

  const { data, isLoading, isError, error } = usePrograms({
    search: search || undefined,
    status: status === "ALL" ? undefined : status,
    page,
    size: PAGE_SIZE,
  });

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Programs</h1>
          <p className="text-sm text-muted-foreground">Manage accredited academic programs</p>
        </div>
        <Button asChild>
          <Link href="/programs/new"><Plus className="h-4 w-4" /> New Program</Link>
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Program Directory</CardTitle>
          <div className="flex flex-col gap-3 pt-2 sm:flex-row">
            <div className="relative flex-1">
              <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Search name, code, degree, branch..."
                className="pl-8"
                value={search}
                onChange={(e) => { setSearch(e.target.value); setPage(0); }}
              />
            </div>
            <Select value={status} onValueChange={(v) => { setStatus(v as ProgramStatus | "ALL"); setPage(0); }}>
              <SelectTrigger className="sm:w-48"><SelectValue placeholder="Status" /></SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">All statuses</SelectItem>
                {STATUSES.map((s) => <SelectItem key={s} value={s}>{s}</SelectItem>)}
              </SelectContent>
            </Select>
          </div>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Program</TableHead>
                <TableHead>Code</TableHead>
                <TableHead>Degree</TableHead>
                <TableHead>Department</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Current Accreditation</TableHead>
                <TableHead>Academic Year</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading && (
                <TableRow><TableCell colSpan={8} className="text-center text-muted-foreground">Loading...</TableCell></TableRow>
              )}
              {isError && (
                <TableRow><TableCell colSpan={8} className="text-center text-destructive">
                  {(error as Error)?.message ?? "Failed to load programs"}
                </TableCell></TableRow>
              )}
              {data && data.content.length === 0 && (
                <TableRow><TableCell colSpan={8} className="text-center text-muted-foreground">No programs found</TableCell></TableRow>
              )}
              {data?.content.map((p) => (
                <TableRow key={p.id}>
                  <TableCell className="font-medium">{p.name}</TableCell>
                  <TableCell>{p.code}</TableCell>
                  <TableCell>{p.degree ?? "—"}</TableCell>
                  <TableCell>{p.departmentName ?? "—"}</TableCell>
                  <TableCell><StatusBadge status={p.status} /></TableCell>
                  <TableCell>
                    {p.currentAccreditation
                      ? <span className="text-sm">{p.currentAccreditation.name} · <StatusBadge status={p.currentAccreditation.status} /></span>
                      : <span className="text-muted-foreground">—</span>}
                  </TableCell>
                  <TableCell>{p.currentAcademicYear?.name ?? "—"}</TableCell>
                  <TableCell className="text-right">
                    <ProgramRowActions program={p} />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>

          {data && data.totalPages > 1 && (
            <div className="flex items-center justify-between pt-4 text-sm">
              <span className="text-muted-foreground">
                Page {data.page + 1} of {data.totalPages} · {data.totalElements} total
              </span>
              <div className="space-x-2">
                <Button size="sm" variant="outline" disabled={data.first} onClick={() => setPage((p) => p - 1)}>Previous</Button>
                <Button size="sm" variant="outline" disabled={data.last} onClick={() => setPage((p) => p + 1)}>Next</Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
