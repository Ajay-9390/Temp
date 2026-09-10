"use client";

import { useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { StatusBadge } from "@/components/status-badge";
import { AccreditationCycleForm } from "@/features/accreditation/AccreditationCycleForm";
import { useAccreditationCycles, useCycleStatusUpdate, useUpdateAccreditationCycle } from "@/features/accreditation/hooks";
import { CYCLE_TRANSITIONS } from "@/lib/lifecycle";
import type { AccreditationCycle, AccreditationCycleStatus } from "@/lib/api/types";

function CycleRow({ cycle }: { cycle: AccreditationCycle }) {
  const statusUpdate = useCycleStatusUpdate(cycle.id);
  const update = useUpdateAccreditationCycle(cycle.id);
  const [editOpen, setEditOpen] = useState(false);
  const next = CYCLE_TRANSITIONS[cycle.status];
  return (
    <div className="rounded-md border p-4">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <p className="font-medium">{cycle.name} <span className="text-muted-foreground">· {cycle.tier}</span></p>
          <p className="text-xs text-muted-foreground">
            {cycle.frameworkVersion} · Application year {cycle.applicationYear}
            {cycle.startDate && ` · ${cycle.startDate} → ${cycle.endDate}`}
          </p>
          {cycle.remarks && <p className="mt-1 text-xs">{cycle.remarks}</p>}
        </div>
        <div className="flex items-center gap-2">
          <StatusBadge status={cycle.status} />
          <Dialog open={editOpen} onOpenChange={setEditOpen}>
            <DialogTrigger asChild><Button size="sm" variant="ghost">Edit</Button></DialogTrigger>
            <DialogContent>
              <DialogHeader><DialogTitle>Edit Accreditation Cycle</DialogTitle></DialogHeader>
              <AccreditationCycleForm
                submitLabel="Save Changes"
                defaultValues={{
                  name: cycle.name,
                  tier: cycle.tier,
                  frameworkVersion: cycle.frameworkVersion,
                  applicationYear: cycle.applicationYear,
                  startDate: cycle.startDate ?? "",
                  endDate: cycle.endDate ?? "",
                  remarks: cycle.remarks ?? "",
                }}
                isSubmitting={update.isPending}
                error={update.error}
                onSubmit={(v) => update.mutateAsync(v)}
                onDone={() => setEditOpen(false)}
              />
            </DialogContent>
          </Dialog>
        </div>
      </div>
      {next.length > 0 && (
        <div className="mt-3 flex flex-wrap gap-2">
          {next.map((s) => (
            <Button key={s} size="sm" variant="outline" disabled={statusUpdate.isPending}
              onClick={() => statusUpdate.mutate({ status: s as AccreditationCycleStatus })}>
              → {s}
            </Button>
          ))}
        </div>
      )}
      {statusUpdate.isError && <p className="mt-2 text-xs text-destructive">{(statusUpdate.error as Error).message}</p>}
    </div>
  );
}

export default function AccreditationCyclesPage() {
  const { id } = useParams<{ id: string }>();
  const { data, isLoading } = useAccreditationCycles(id);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Accreditation Cycles</h1>
          <Link href={`/programs/${id}`} className="text-sm text-muted-foreground hover:underline">← Back to program</Link>
        </div>
        <Button asChild><Link href={`/programs/${id}/accreditation-cycles/new`}>Add Cycle</Link></Button>
      </div>
      <Card>
        <CardHeader><CardTitle className="text-base">History</CardTitle></CardHeader>
        <CardContent className="space-y-3">
          {isLoading && <p className="text-muted-foreground">Loading...</p>}
          {data?.length === 0 && <p className="text-sm text-muted-foreground">No accreditation cycles yet.</p>}
          {data?.map((c) => <CycleRow key={c.id} cycle={c} />)}
        </CardContent>
      </Card>
    </div>
  );
}
