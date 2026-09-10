"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { FieldError, FormError } from "@/components/form-error";
import { useInstitutions } from "@/features/institutions/hooks";
import { departmentSchema, type DepartmentFormValues } from "./schema";

interface Props {
  defaultValues?: Partial<DepartmentFormValues>;
  lockInstitution?: boolean;
  submitLabel: string;
  onSubmit: (values: DepartmentFormValues) => Promise<unknown>;
  isSubmitting: boolean;
  error: unknown;
  onDone: () => void;
}

export function DepartmentForm({ defaultValues, lockInstitution, submitLabel, onSubmit, isSubmitting, error, onDone }: Props) {
  const institutions = useInstitutions();
  const { register, handleSubmit, setValue, watch, formState: { errors } } = useForm<DepartmentFormValues>({
    resolver: zodResolver(departmentSchema),
    defaultValues: { institutionId: "", name: "", code: "", description: "", hodUserId: "", ...defaultValues },
  });

  const submit = handleSubmit(async (values) => { await onSubmit(values); onDone(); });

  return (
    <form onSubmit={submit} className="space-y-4" noValidate>
      <FormError error={error} />
      <div>
        <Label>Institution *</Label>
        <Select value={watch("institutionId")} disabled={lockInstitution}
          onValueChange={(v) => setValue("institutionId", v, { shouldValidate: true })}>
          <SelectTrigger><SelectValue placeholder="Select institution" /></SelectTrigger>
          <SelectContent>
            {institutions.data?.content.map((i) => <SelectItem key={i.id} value={i.id}>{i.name}</SelectItem>)}
          </SelectContent>
        </Select>
        <FieldError message={errors.institutionId?.message} />
      </div>
      <div className="grid gap-4 sm:grid-cols-2">
        <div>
          <Label htmlFor="name">Name *</Label>
          <Input id="name" {...register("name")} placeholder="Computer Science & Engineering" />
          <FieldError message={errors.name?.message} />
        </div>
        <div>
          <Label htmlFor="code">Code *</Label>
          <Input id="code" {...register("code")} placeholder="CSE" />
          <FieldError message={errors.code?.message} />
        </div>
        <div>
          <Label htmlFor="hodUserId">HOD User ID</Label>
          <Input id="hodUserId" {...register("hodUserId")} placeholder="keycloak-user-id" />
        </div>
      </div>
      <div>
        <Label htmlFor="description">Description</Label>
        <Textarea id="description" {...register("description")} rows={2} />
      </div>
      <div className="flex gap-3">
        <Button type="submit" disabled={isSubmitting}>{isSubmitting ? "Saving..." : submitLabel}</Button>
        <Button type="button" variant="outline" onClick={onDone}>Cancel</Button>
      </div>
    </form>
  );
}
