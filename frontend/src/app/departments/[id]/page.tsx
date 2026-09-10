"use client";

import { useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { StatusBadge } from "@/components/status-badge";
import { DepartmentForm } from "@/features/departments/DepartmentForm";
import {
  useDepartment, useDepartmentPrograms, useDepartmentStatusUpdate, useUpdateDepartment,
} from "@/features/departments/hooks";

export default function DepartmentDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { data: dept, isLoading } = useDepartment(id);
  const programs = useDepartmentPrograms(id);
  const statusUpdate = useDepartmentStatusUpdate(id);
  const update = useUpdateDepartment(id);
  const [editOpen, setEditOpen] = useState(false);

  if (isLoading || !dept) return <p className="text-muted-foreground">Loading...</p>;

  const toggleTo = dept.status === "ACTIVE" ? "INACTIVE" : "ACTIVE";

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-semibold">{dept.name}</h1>
            <StatusBadge status={dept.status} />
          </div>
          <p className="text-sm text-muted-foreground">Code: {dept.code}</p>
          <Link href="/departments" className="text-sm text-muted-foreground hover:underline">← All departments</Link>
        </div>
        <div className="flex gap-2">
          <Dialog open={editOpen} onOpenChange={setEditOpen}>
            <DialogTrigger asChild><Button size="sm">Edit</Button></DialogTrigger>
            <DialogContent>
              <DialogHeader><DialogTitle>Edit Department</DialogTitle></DialogHeader>
              <DepartmentForm
                submitLabel="Save Changes"
                lockInstitution
                defaultValues={{
                  institutionId: dept.institutionId,
                  name: dept.name,
                  code: dept.code,
                  description: dept.description ?? "",
                  hodUserId: dept.hodUserId ?? "",
                }}
                isSubmitting={update.isPending}
                error={update.error}
                onSubmit={(v) => update.mutateAsync(v)}
                onDone={() => setEditOpen(false)}
              />
            </DialogContent>
          </Dialog>
          <Button size="sm" variant="outline" disabled={statusUpdate.isPending}
            onClick={() => statusUpdate.mutate(toggleTo)}>
            {toggleTo === "ACTIVE" ? "Activate" : "Deactivate"}
          </Button>
        </div>
      </div>

      {dept.description && (
        <Card><CardContent className="pt-6 text-sm">{dept.description}</CardContent></Card>
      )}

      <Card>
        <CardHeader className="flex-row items-center justify-between">
          <CardTitle className="text-base">Programs ({programs.data?.content.length ?? 0})</CardTitle>
          <Button asChild size="sm"><Link href="/programs/new">Add Program</Link></Button>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Program</TableHead><TableHead>Code</TableHead>
                <TableHead>Status</TableHead><TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {programs.data?.content.length === 0 && (
                <TableRow><TableCell colSpan={4} className="text-center text-muted-foreground">No programs in this department</TableCell></TableRow>
              )}
              {programs.data?.content.map((p) => (
                <TableRow key={p.id}>
                  <TableCell className="font-medium">{p.name}</TableCell>
                  <TableCell>{p.code}</TableCell>
                  <TableCell><StatusBadge status={p.status} /></TableCell>
                  <TableCell className="text-right">
                    <Button asChild size="sm" variant="outline"><Link href={`/programs/${p.id}`}>View</Link></Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
}
