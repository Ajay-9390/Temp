"use client";

import { useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { StatusBadge } from "@/components/status-badge";
import { AcademicYearForm } from "@/features/academic-years/AcademicYearForm";
import {
  useAcademicYears, useAcademicYearStatusUpdate, useCreateAcademicYear, useUpdateAcademicYear,
} from "@/features/academic-years/hooks";
import { ACADEMIC_TRANSITIONS } from "@/lib/lifecycle";
import type { AcademicLifecycleStatus, AcademicYear } from "@/lib/api/types";

function YearRow({ year }: { year: AcademicYear }) {
  const statusUpdate = useAcademicYearStatusUpdate(year.id);
  const update = useUpdateAcademicYear(year.id);
  const [editOpen, setEditOpen] = useState(false);
  const next = ACADEMIC_TRANSITIONS[year.status];
  return (
    <div className="rounded-md border p-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <p className="font-medium">{year.name}{year.status === "ACTIVE" && <span className="ml-2 text-xs text-green-700">(current)</span>}</p>
          <p className="text-xs text-muted-foreground">{year.startDate} → {year.endDate} · {year.semesterCount} semesters</p>
        </div>
        <div className="flex items-center gap-2">
          <StatusBadge status={year.status} />
          <Dialog open={editOpen} onOpenChange={setEditOpen}>
            <DialogTrigger asChild><Button size="sm" variant="ghost">Edit</Button></DialogTrigger>
            <DialogContent>
              <DialogHeader><DialogTitle>Edit Academic Year</DialogTitle></DialogHeader>
              <AcademicYearForm
                submitLabel="Save Changes"
                defaultValues={{ name: year.name, startDate: year.startDate, endDate: year.endDate }}
                isSubmitting={update.isPending}
                error={update.error}
                onSubmit={(v) => update.mutateAsync(v)}
                onDone={() => setEditOpen(false)}
              />
            </DialogContent>
          </Dialog>
          <Button asChild size="sm" variant="outline"><Link href={`/academic-years/${year.id}/semesters`}>Semesters</Link></Button>
        </div>
      </div>
      {next.length > 0 && (
        <div className="mt-3 flex flex-wrap gap-2">
          {next.map((s) => (
            <Button key={s} size="sm" variant="ghost" disabled={statusUpdate.isPending}
              onClick={() => statusUpdate.mutate(s as AcademicLifecycleStatus)}>→ {s}</Button>
          ))}
        </div>
      )}
      {statusUpdate.isError && <p className="mt-2 text-xs text-destructive">{(statusUpdate.error as Error).message}</p>}
    </div>
  );
}

export default function AcademicYearsPage() {
  const { id } = useParams<{ id: string }>();
  const { data, isLoading } = useAcademicYears(id);
  const create = useCreateAcademicYear(id);
  const [open, setOpen] = useState(false);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Academic Years</h1>
          <Link href={`/programs/${id}`} className="text-sm text-muted-foreground hover:underline">← Back to program</Link>
        </div>
        <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger asChild><Button>Add Academic Year</Button></DialogTrigger>
          <DialogContent>
            <DialogHeader><DialogTitle>New Academic Year</DialogTitle></DialogHeader>
            <AcademicYearForm submitLabel="Create" isSubmitting={create.isPending} error={create.error}
              onSubmit={(v) => create.mutateAsync(v)} onDone={() => setOpen(false)} />
          </DialogContent>
        </Dialog>
      </div>
      <Card>
        <CardHeader><CardTitle className="text-base">Years</CardTitle></CardHeader>
        <CardContent className="space-y-3">
          {isLoading && <p className="text-muted-foreground">Loading...</p>}
          {data?.length === 0 && <p className="text-sm text-muted-foreground">No academic years yet.</p>}
          {data?.map((y) => <YearRow key={y.id} year={y} />)}
        </CardContent>
      </Card>
    </div>
  );
}
