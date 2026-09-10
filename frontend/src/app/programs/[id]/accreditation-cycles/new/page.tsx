"use client";

import { useParams, useRouter } from "next/navigation";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { AccreditationCycleForm } from "@/features/accreditation/AccreditationCycleForm";
import { useCreateAccreditationCycle } from "@/features/accreditation/hooks";

export default function NewAccreditationCyclePage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const create = useCreateAccreditationCycle(id);

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <h1 className="text-2xl font-semibold">New Accreditation Cycle</h1>
      <Card>
        <CardHeader><CardTitle className="text-base">Cycle Details</CardTitle></CardHeader>
        <CardContent>
          <AccreditationCycleForm
            submitLabel="Create Cycle"
            onSubmit={(values) => create.mutateAsync(values)}
            isSubmitting={create.isPending}
            error={create.error}
            onDone={() => router.push(`/programs/${id}/accreditation-cycles`)}
          />
        </CardContent>
      </Card>
    </div>
  );
}
