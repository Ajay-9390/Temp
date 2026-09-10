"use client";

import { useParams } from "next/navigation";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ProgramForm } from "@/features/programs/ProgramForm";
import { useProgram, useUpdateProgram } from "@/features/programs/hooks";

export default function EditProgramPage() {
  const { id } = useParams<{ id: string }>();
  const { data: program, isLoading } = useProgram(id);
  const update = useUpdateProgram(id);

  if (isLoading || !program) {
    return <p className="text-muted-foreground">Loading...</p>;
  }

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Edit Program</h1>
        <p className="text-sm text-muted-foreground">
          Department cannot be changed after creation
        </p>
      </div>
      <Card>
        <CardHeader><CardTitle className="text-base">Program Details</CardTitle></CardHeader>
        <CardContent>
          <ProgramForm
            submitLabel="Save Changes"
            lockedDepartmentId={program.departmentId}
            defaultValues={{
              departmentId: program.departmentId,
              name: program.name,
              code: program.code,
              degree: program.degree ?? "",
              branch: program.branch ?? "",
              description: program.description ?? "",
              durationYears: program.durationYears,
              totalSemesters: program.totalSemesters,
              intake: program.intake,
              establishedYear: program.establishedYear,
            }}
            onSubmit={(values) => update.mutateAsync(values)}
            isSubmitting={update.isPending}
            error={update.error}
          />
        </CardContent>
      </Card>
    </div>
  );
}
