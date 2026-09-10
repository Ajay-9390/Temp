"use client";

import { useState } from "react";
import { useParams } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { StatusBadge } from "@/components/status-badge";
import { SemesterForm } from "@/features/semesters/SemesterForm";
import { useCreateSemester, useSemesters, useSemesterStatusUpdate, useUpdateSemester } from "@/features/semesters/hooks";
import { useAcademicYear } from "@/features/academic-years/hooks";
import { ACADEMIC_TRANSITIONS } from "@/lib/lifecycle";
import type { AcademicLifecycleStatus, Semester } from "@/lib/api/types";

function SemesterActions({ semester }: { semester: Semester }) {
  const statusUpdate = useSemesterStatusUpdate(semester.id);
  const update = useUpdateSemester(semester.id);
  const [editOpen, setEditOpen] = useState(false);
  const next = ACADEMIC_TRANSITIONS[semester.status];
  return (
    <div className="flex flex-wrap items-center justify-end gap-1">
      <Dialog open={editOpen} onOpenChange={setEditOpen}>
        <DialogTrigger asChild><Button size="sm" variant="ghost">Edit</Button></DialogTrigger>
        <DialogContent>
          <DialogHeader><DialogTitle>Edit Semester</DialogTitle></DialogHeader>
          <SemesterForm
            submitLabel="Save Changes"
            defaultValues={{
              semesterNumber: semester.semesterNumber,
              name: semester.name,
              startDate: semester.startDate ?? "",
              endDate: semester.endDate ?? "",
            }}
            isSubmitting={update.isPending}
            error={update.error}
            onSubmit={(v) => update.mutateAsync(v)}
            onDone={() => setEditOpen(false)}
          />
        </DialogContent>
      </Dialog>
      {next.map((s) => (
        <Button key={s} size="sm" variant="ghost" disabled={statusUpdate.isPending}
          onClick={() => statusUpdate.mutate(s as AcademicLifecycleStatus)}>→ {s}</Button>
      ))}
    </div>
  );
}

export default function SemestersPage() {
  const { id } = useParams<{ id: string }>();
  const year = useAcademicYear(id);
  const { data, isLoading } = useSemesters(id);
  const create = useCreateSemester(id);
  const [open, setOpen] = useState(false);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Semesters</h1>
          <p className="text-sm text-muted-foreground">{year.data?.name}</p>
        </div>
        <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger asChild><Button>Add Semester</Button></DialogTrigger>
          <DialogContent>
            <DialogHeader><DialogTitle>New Semester</DialogTitle></DialogHeader>
            <SemesterForm submitLabel="Create" isSubmitting={create.isPending} error={create.error}
              onSubmit={(v) => create.mutateAsync(v)} onDone={() => setOpen(false)} />
          </DialogContent>
        </Dialog>
      </div>
      <Card>
        <CardHeader><CardTitle className="text-base">Semester Structure</CardTitle></CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>#</TableHead><TableHead>Name</TableHead><TableHead>Dates</TableHead>
                <TableHead>Status</TableHead><TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading && <TableRow><TableCell colSpan={5} className="text-center text-muted-foreground">Loading...</TableCell></TableRow>}
              {data?.length === 0 && <TableRow><TableCell colSpan={5} className="text-center text-muted-foreground">No semesters yet.</TableCell></TableRow>}
              {data?.map((s) => (
                <TableRow key={s.id}>
                  <TableCell>{s.semesterNumber}</TableCell>
                  <TableCell className="font-medium">{s.name}</TableCell>
                  <TableCell>{s.startDate ? `${s.startDate} → ${s.endDate}` : "—"}</TableCell>
                  <TableCell><StatusBadge status={s.status} /></TableCell>
                  <TableCell><SemesterActions semester={s} /></TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
}
