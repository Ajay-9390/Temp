"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from "@/components/ui/select";
import { FieldError, FormError } from "@/components/form-error";
import { useDepartments } from "@/features/departments/hooks";
import { programSchema, type ProgramFormValues } from "./schema";
import type { Program } from "@/lib/api/types";

interface Props {
  defaultValues?: Partial<ProgramFormValues>;
  lockedDepartmentId?: string;
  submitLabel: string;
  onSubmit: (values: ProgramFormValues) => Promise<Program>;
  isSubmitting: boolean;
  error: unknown;
}

export function ProgramForm({ defaultValues, lockedDepartmentId, submitLabel, onSubmit, isSubmitting, error }: Props) {
  const router = useRouter();
  const departments = useDepartments();
  const {
    register, handleSubmit, setValue, watch, formState: { errors },
  } = useForm<ProgramFormValues>({
    resolver: zodResolver(programSchema),
    defaultValues: {
      departmentId: lockedDepartmentId ?? "",
      name: "", code: "", degree: "", branch: "", description: "",
      durationYears: 4, totalSemesters: 8, intake: 60, establishedYear: undefined,
      ...defaultValues,
    },
  });

  const departmentId = watch("departmentId");

  const submit = handleSubmit(async (values) => {
    const program = await onSubmit(values);
    router.push(`/programs/${program.id}`);
  });

  return (
    <form onSubmit={submit} className="space-y-5" noValidate>
      <FormError error={error} />

      <div className="grid gap-4 sm:grid-cols-2">
        <div className="sm:col-span-2">
          <Label htmlFor="name">Program Name *</Label>
          <Input id="name" {...register("name")} placeholder="B.Tech Computer Science and Engineering" />
          <FieldError message={errors.name?.message} />
        </div>

        <div>
          <Label htmlFor="code">Program Code *</Label>
          <Input id="code" {...register("code")} placeholder="BTECH-CSE" />
          <FieldError message={errors.code?.message} />
        </div>

        <div>
          <Label>Department *</Label>
          <Select
            value={departmentId}
            onValueChange={(v) => setValue("departmentId", v, { shouldValidate: true })}
            disabled={!!lockedDepartmentId}
          >
            <SelectTrigger><SelectValue placeholder="Select department" /></SelectTrigger>
            <SelectContent>
              {departments.data?.content.map((d) => (
                <SelectItem key={d.id} value={d.id}>{d.name} ({d.code})</SelectItem>
              ))}
            </SelectContent>
          </Select>
          <FieldError message={errors.departmentId?.message} />
        </div>

        <div>
          <Label htmlFor="degree">Degree</Label>
          <Input id="degree" {...register("degree")} placeholder="B.Tech" />
        </div>

        <div>
          <Label htmlFor="branch">Branch</Label>
          <Input id="branch" {...register("branch")} placeholder="Computer Science and Engineering" />
        </div>

        <div>
          <Label htmlFor="durationYears">Duration (years) *</Label>
          <Input id="durationYears" type="number" {...register("durationYears")} />
          <FieldError message={errors.durationYears?.message} />
        </div>

        <div>
          <Label htmlFor="totalSemesters">Total Semesters *</Label>
          <Input id="totalSemesters" type="number" {...register("totalSemesters")} />
          <FieldError message={errors.totalSemesters?.message} />
        </div>

        <div>
          <Label htmlFor="intake">Intake *</Label>
          <Input id="intake" type="number" {...register("intake")} />
          <FieldError message={errors.intake?.message} />
        </div>

        <div>
          <Label htmlFor="establishedYear">Established Year</Label>
          <Input id="establishedYear" type="number" {...register("establishedYear")} placeholder="2010" />
          <FieldError message={errors.establishedYear?.message} />
        </div>

        <div className="sm:col-span-2">
          <Label htmlFor="description">Description</Label>
          <Textarea id="description" {...register("description")} rows={3} />
        </div>
      </div>

      <div className="flex gap-3">
        <Button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Saving..." : submitLabel}
        </Button>
        <Button type="button" variant="outline" onClick={() => router.back()}>Cancel</Button>
      </div>
    </form>
  );
}
