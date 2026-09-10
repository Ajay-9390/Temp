"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ProgramForm } from "@/features/programs/ProgramForm";
import { useCreateProgram } from "@/features/programs/hooks";

export default function NewProgramPage() {
  const create = useCreateProgram();

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Create Program</h1>
        <p className="text-sm text-muted-foreground">New programs start in DRAFT status</p>
      </div>
      <Card>
        <CardHeader><CardTitle className="text-base">Program Details</CardTitle></CardHeader>
        <CardContent>
          <ProgramForm
            submitLabel="Create Program"
            onSubmit={(values) => create.mutateAsync(values)}
            isSubmitting={create.isPending}
            error={create.error}
          />
        </CardContent>
      </Card>
    </div>
  );
}
