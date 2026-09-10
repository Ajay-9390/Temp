"use client";

import * as React from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { Link2, Trash2 } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useMappingMutations, useMappings } from "@/hooks/use-evidence";
import { MAPPING_TYPE_LABELS } from "@/lib/constants";
import { mappingSchema, type MappingForm } from "@/lib/schemas";
import { MAPPING_TYPES } from "@/lib/types";
import { formatDate, shortId } from "@/lib/utils";

export function MappingPanel({ evidenceId }: { evidenceId: string }) {
  const { data: mappings = [], isLoading } = useMappings(evidenceId);
  const { add, remove } = useMappingMutations(evidenceId);

  const form = useForm<MappingForm>({
    resolver: zodResolver(mappingSchema),
    defaultValues: { criterionId: "", requirementId: "", mappingType: "SUPPORTING" },
  });

  const onSubmit = form.handleSubmit(async (values) => {
    await add.mutateAsync(values);
    form.reset({ criterionId: "", requirementId: "", mappingType: "SUPPORTING" });
  });

  return (
    <div className="space-y-4">
      <div className="space-y-2">
        {isLoading ? (
          <p className="text-sm text-muted-foreground">Loading mappings…</p>
        ) : mappings.length === 0 ? (
          <p className="text-sm text-muted-foreground">
            Not mapped to any criterion yet. One evidence item can support multiple requirements.
          </p>
        ) : (
          mappings.map((m) => (
            <div key={m.id} className="flex items-center justify-between rounded-md border p-3">
              <div className="flex items-center gap-3">
                <Link2 className="h-4 w-4 text-muted-foreground" />
                <div className="text-sm">
                  <div className="font-mono text-xs text-muted-foreground">
                    criterion {shortId(m.criterionId)} · requirement {shortId(m.requirementId)}
                  </div>
                  <Badge variant="secondary" className="mt-1">
                    {MAPPING_TYPE_LABELS[m.mappingType]}
                  </Badge>
                  <span className="ml-2 text-xs text-muted-foreground">{formatDate(m.createdAt)}</span>
                </div>
              </div>
              <Button
                variant="ghost"
                size="icon"
                onClick={() => remove.mutate(m.id)}
                disabled={remove.isPending}
                aria-label="Remove mapping"
              >
                <Trash2 className="h-4 w-4 text-destructive" />
              </Button>
            </div>
          ))
        )}
      </div>

      <form onSubmit={onSubmit} className="grid grid-cols-1 gap-3 rounded-md border bg-muted/30 p-3 sm:grid-cols-4">
        <div className="space-y-1">
          <Label htmlFor="criterionId">Criterion ID</Label>
          <Input id="criterionId" placeholder="UUID" {...form.register("criterionId")} />
          {form.formState.errors.criterionId ? (
            <p className="text-xs text-destructive">{form.formState.errors.criterionId.message}</p>
          ) : null}
        </div>
        <div className="space-y-1">
          <Label htmlFor="requirementId">Requirement ID</Label>
          <Input id="requirementId" placeholder="UUID" {...form.register("requirementId")} />
          {form.formState.errors.requirementId ? (
            <p className="text-xs text-destructive">{form.formState.errors.requirementId.message}</p>
          ) : null}
        </div>
        <div className="space-y-1">
          <Label>Type</Label>
          <Select
            value={form.watch("mappingType")}
            onValueChange={(v) => form.setValue("mappingType", v as MappingForm["mappingType"])}
          >
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {MAPPING_TYPES.map((t) => (
                <SelectItem key={t} value={t}>
                  {MAPPING_TYPE_LABELS[t]}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="flex items-end">
          <Button type="submit" disabled={add.isPending} className="w-full">
            Add mapping
          </Button>
        </div>
      </form>
    </div>
  );
}
