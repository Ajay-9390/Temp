"use client";

import { useState } from "react";
import Link from "next/link";
import { Search } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { StatusBadge } from "@/components/status-badge";
import { DepartmentForm } from "@/features/departments/DepartmentForm";
import { useCreateDepartment, useDepartments } from "@/features/departments/hooks";

export default function DepartmentsPage() {
  const [search, setSearch] = useState("");
  const { data, isLoading } = useDepartments({ search: search || undefined });
  const create = useCreateDepartment();
  const [open, setOpen] = useState(false);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Departments</h1>
          <p className="text-sm text-muted-foreground">Manage academic departments</p>
        </div>
        <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger asChild><Button>New Department</Button></DialogTrigger>
          <DialogContent>
            <DialogHeader><DialogTitle>New Department</DialogTitle></DialogHeader>
            <DepartmentForm submitLabel="Create" isSubmitting={create.isPending} error={create.error}
              onSubmit={(v) => create.mutateAsync(v)} onDone={() => setOpen(false)} />
          </DialogContent>
        </Dialog>
      </div>
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Department Directory</CardTitle>
          <div className="relative pt-2">
            <Search className="absolute left-2.5 top-4.5 h-4 w-4 text-muted-foreground" />
            <Input placeholder="Search name or code..." className="pl-8" value={search}
              onChange={(e) => setSearch(e.target.value)} />
          </div>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Department</TableHead><TableHead>Code</TableHead>
                <TableHead>Programs</TableHead><TableHead>Status</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading && <TableRow><TableCell colSpan={5} className="text-center text-muted-foreground">Loading...</TableCell></TableRow>}
              {data?.content.length === 0 && <TableRow><TableCell colSpan={5} className="text-center text-muted-foreground">No departments found</TableCell></TableRow>}
              {data?.content.map((d) => (
                <TableRow key={d.id}>
                  <TableCell className="font-medium">{d.name}</TableCell>
                  <TableCell>{d.code}</TableCell>
                  <TableCell>{d.programCount}</TableCell>
                  <TableCell><StatusBadge status={d.status} /></TableCell>
                  <TableCell className="text-right">
                    <Button asChild size="sm" variant="outline"><Link href={`/departments/${d.id}`}>View</Link></Button>
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
