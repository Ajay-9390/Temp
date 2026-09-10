"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { FieldError, FormError } from "@/components/form-error";
import { accreditationCycleSchema, type AccreditationCycleFormValues } from "./schema";

// Tiers are configurable on the backend; the current allowed set is TIER_I / TIER_II.
const TIERS = ["TIER_I", "TIER_II"];

interface Props {
  defaultValues?: Partial<AccreditationCycleFormValues>;
  submitLabel: string;
  onSubmit: (values: AccreditationCycleFormValues) => Promise<unknown>;
  isSubmitting: boolean;
  error: unknown;
  onDone: () => void;
}

export function AccreditationCycleForm({ defaultValues, submitLabel, onSubmit, isSubmitting, error, onDone }: Props) {
  const {
    register, handleSubmit, setValue, watch, formState: { errors },
  } = useForm<AccreditationCycleFormValues>({
    resolver: zodResolver(accreditationCycleSchema),
    defaultValues: {
      name: "", tier: "TIER_I", frameworkVersion: "GAPC v4.0",
      applicationYear: new Date().getFullYear(), startDate: "", endDate: "", remarks: "",
      ...defaultValues,
    },
  });

  const submit = handleSubmit(async (values) => { await onSubmit(values); onDone(); });

  return (
    <form onSubmit={submit} className="space-y-4" noValidate>
      <FormError error={error} />
      <div className="grid gap-4 sm:grid-cols-2">
        <div className="sm:col-span-2">
          <Label htmlFor="name">Cycle Name *</Label>
          <Input id="name" {...register("name")} placeholder="NBA 2026" />
          <FieldError message={errors.name?.message} />
        </div>
        <div>
          <Label>Tier *</Label>
          <Select value={watch("tier")} onValueChange={(v) => setValue("tier", v, { shouldValidate: true })}>
            <SelectTrigger><SelectValue /></SelectTrigger>
            <SelectContent>{TIERS.map((t) => <SelectItem key={t} value={t}>{t}</SelectItem>)}</SelectContent>
          </Select>
          <FieldError message={errors.tier?.message} />
        </div>
        <div>
          <Label htmlFor="frameworkVersion">Framework Version *</Label>
          <Input id="frameworkVersion" {...register("frameworkVersion")} placeholder="GAPC v4.0" />
          <FieldError message={errors.frameworkVersion?.message} />
        </div>
        <div>
          <Label htmlFor="applicationYear">Application Year *</Label>
          <Input id="applicationYear" type="number" {...register("applicationYear")} />
          <FieldError message={errors.applicationYear?.message} />
        </div>
        <div />
        <div>
          <Label htmlFor="startDate">Start Date</Label>
          <Input id="startDate" type="date" {...register("startDate")} />
        </div>
        <div>
          <Label htmlFor="endDate">End Date</Label>
          <Input id="endDate" type="date" {...register("endDate")} />
          <FieldError message={errors.endDate?.message} />
        </div>
        <div className="sm:col-span-2">
          <Label htmlFor="remarks">Remarks</Label>
          <Textarea id="remarks" {...register("remarks")} rows={2} />
        </div>
      </div>
      <div className="flex gap-3">
        <Button type="submit" disabled={isSubmitting}>{isSubmitting ? "Saving..." : submitLabel}</Button>
        <Button type="button" variant="outline" onClick={onDone}>Cancel</Button>
      </div>
    </form>
  );
}
