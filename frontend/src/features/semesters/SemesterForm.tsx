"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { FieldError, FormError } from "@/components/form-error";
import { semesterSchema, type SemesterFormValues } from "./schema";

interface Props {
  defaultValues?: Partial<SemesterFormValues>;
  submitLabel: string;
  onSubmit: (values: SemesterFormValues) => Promise<unknown>;
  isSubmitting: boolean;
  error: unknown;
  onDone: () => void;
}

export function SemesterForm({ defaultValues, submitLabel, onSubmit, isSubmitting, error, onDone }: Props) {
  const { register, handleSubmit, formState: { errors } } = useForm<SemesterFormValues>({
    resolver: zodResolver(semesterSchema),
    defaultValues: { semesterNumber: 1, name: "Semester 1", startDate: "", endDate: "", ...defaultValues },
  });

  const submit = handleSubmit(async (values) => { await onSubmit(values); onDone(); });

  return (
    <form onSubmit={submit} className="space-y-4" noValidate>
      <FormError error={error} />
      <div className="grid gap-4 sm:grid-cols-2">
        <div>
          <Label htmlFor="semesterNumber">Semester Number *</Label>
          <Input id="semesterNumber" type="number" {...register("semesterNumber")} />
          <FieldError message={errors.semesterNumber?.message} />
        </div>
        <div>
          <Label htmlFor="name">Name *</Label>
          <Input id="name" {...register("name")} placeholder="Semester 1" />
          <FieldError message={errors.name?.message} />
        </div>
        <div>
          <Label htmlFor="startDate">Start Date</Label>
          <Input id="startDate" type="date" {...register("startDate")} />
        </div>
        <div>
          <Label htmlFor="endDate">End Date</Label>
          <Input id="endDate" type="date" {...register("endDate")} />
          <FieldError message={errors.endDate?.message} />
        </div>
      </div>
      <div className="flex gap-3">
        <Button type="submit" disabled={isSubmitting}>{isSubmitting ? "Saving..." : submitLabel}</Button>
        <Button type="button" variant="outline" onClick={onDone}>Cancel</Button>
      </div>
    </form>
  );
}
