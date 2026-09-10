"use client";

import * as React from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Skeleton } from "@/components/ui/skeleton";
import { PageHeader } from "@/components/evidence/page-header";
import { useEvidence, useUpdateEvidence } from "@/hooks/use-evidence";
import { CATEGORY_LABELS } from "@/lib/constants";
import { cleanMetadata, evidenceMetadataSchema, type EvidenceMetadataForm } from "@/lib/schemas";
import { EVIDENCE_CATEGORIES } from "@/lib/types";

export default function EditEvidencePage({ params }: { params: { id: string } }) {
  const { id } = params;
  const router = useRouter();
  const { data: evidence, isLoading } = useEvidence(id);
  const update = useUpdateEvidence(id);

  const form = useForm<EvidenceMetadataForm>({
    resolver: zodResolver(evidenceMetadataSchema),
    defaultValues: {
      title: "",
      description: "",
      category: "OTHER",
      programId: "",
      departmentId: "",
      academicYearId: "",
      criterionId: "",
      requirementId: "",
      tags: "",
    },
  });

  const { reset } = form;
  React.useEffect(() => {
    if (evidence) {
      reset({
        title: evidence.title,
        description: evidence.description ?? "",
        category: evidence.category,
        programId: evidence.programId ?? "",
        departmentId: evidence.departmentId ?? "",
        academicYearId: evidence.academicYearId ?? "",
        criterionId: evidence.criterionId ?? "",
        requirementId: evidence.requirementId ?? "",
        tags: evidence.tags.join(", "),
      });
    }
  }, [evidence, reset]);

  const onSubmit = form.handleSubmit(async (values) => {
    await update.mutateAsync(cleanMetadata(values));
    router.push(`/evidence/${id}`);
  });

  if (isLoading) {
    return <Skeleton className="h-96 w-full" />;
  }
  if (!evidence) {
    return <p className="text-muted-foreground">Evidence not found.</p>;
  }

  const err = (field: keyof EvidenceMetadataForm) => form.formState.errors[field]?.message;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Edit Evidence"
        description={evidence.title}
        actions={
          <Button asChild variant="ghost">
            <Link href={`/evidence/${id}`}>
              <ArrowLeft className="h-4 w-4" /> Back
            </Link>
          </Button>
        }
      />

      <form onSubmit={onSubmit} className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Details</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-1">
              <Label htmlFor="title">Title *</Label>
              <Input id="title" {...form.register("title")} />
              {err("title") ? <p className="text-xs text-destructive">{err("title")}</p> : null}
            </div>
            <div className="space-y-1">
              <Label htmlFor="description">Description</Label>
              <Textarea id="description" rows={5} {...form.register("description")} />
            </div>
            <div className="space-y-1">
              <Label htmlFor="tags">Tags (comma-separated)</Label>
              <Input id="tags" {...form.register("tags")} />
            </div>
          </CardContent>
        </Card>

        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Classification</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-1">
                <Label>Category *</Label>
                <Select value={form.watch("category")} onValueChange={(v) => form.setValue("category", v)}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {EVIDENCE_CATEGORIES.map((c) => (
                      <SelectItem key={c} value={c}>
                        {CATEGORY_LABELS[c]}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              {(["programId", "departmentId", "academicYearId", "criterionId", "requirementId"] as const).map((field) => (
                <div key={field} className="space-y-1">
                  <Label htmlFor={field}>{fieldLabel(field)}</Label>
                  <Input id={field} placeholder="UUID (optional)" {...form.register(field)} />
                  {err(field) ? <p className="text-xs text-destructive">{err(field)}</p> : null}
                </div>
              ))}
            </CardContent>
          </Card>

          <Button type="submit" className="w-full" disabled={update.isPending}>
            {update.isPending ? "Saving…" : "Save Changes"}
          </Button>
        </div>
      </form>
    </div>
  );
}

function fieldLabel(field: string): string {
  switch (field) {
    case "programId":
      return "Program ID";
    case "departmentId":
      return "Department ID";
    case "academicYearId":
      return "Academic Year ID";
    case "criterionId":
      return "NBA Criterion ID";
    case "requirementId":
      return "NBA Requirement ID";
    default:
      return field;
  }
}
