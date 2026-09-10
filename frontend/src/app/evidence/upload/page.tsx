"use client";

import * as React from "react";
import { useRouter } from "next/navigation";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm, type UseFormReturn } from "react-hook-form";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { PageHeader } from "@/components/evidence/page-header";
import { UploadDropzone, validateFile } from "@/components/evidence/upload-dropzone";
import { useCreateEvidence } from "@/hooks/use-evidence";
import { CATEGORY_LABELS } from "@/lib/constants";
import { cleanMetadata, evidenceMetadataSchema, type EvidenceMetadataForm } from "@/lib/schemas";
import { EVIDENCE_CATEGORIES } from "@/lib/types";

export default function UploadPage() {
  const router = useRouter();
  const create = useCreateEvidence();
  const [file, setFile] = React.useState<File | null>(null);
  const [fileError, setFileError] = React.useState<string | null>(null);

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

  const onFileChange = (f: File | null) => {
    setFile(f);
    setFileError(f ? validateFile(f) : null);
  };

  const onSubmit = form.handleSubmit(async (values) => {
    if (!file) {
      setFileError("Please choose a file to upload");
      return;
    }
    const v = validateFile(file);
    if (v) {
      setFileError(v);
      return;
    }
    const payload = cleanMetadata(values);
    const fd = new FormData();
    fd.append("data", new Blob([JSON.stringify(payload)], { type: "application/json" }));
    fd.append("file", file);
    const created = await create.mutateAsync(fd);
    router.push(`/evidence/${created.id}`);
  });

  const err = (field: keyof EvidenceMetadataForm) => form.formState.errors[field]?.message;

  return (
    <div className="space-y-6">
      <PageHeader title="Upload Evidence" description="Create a new evidence item with an initial file version" />

      <form onSubmit={onSubmit} className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="space-y-6 lg:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle>Document</CardTitle>
            </CardHeader>
            <CardContent>
              <UploadDropzone file={file} onFileChange={onFileChange} error={fileError} disabled={create.isPending} />
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Details</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-1">
                <Label htmlFor="title">Title *</Label>
                <Input id="title" placeholder="e.g. FDP Certificate - AI/ML" {...form.register("title")} />
                {err("title") ? <p className="text-xs text-destructive">{err("title")}</p> : null}
              </div>
              <div className="space-y-1">
                <Label htmlFor="description">Description</Label>
                <Textarea id="description" rows={4} placeholder="Describe this evidence" {...form.register("description")} />
                {err("description") ? <p className="text-xs text-destructive">{err("description")}</p> : null}
              </div>
              <div className="space-y-1">
                <Label htmlFor="tags">Tags (comma-separated)</Label>
                <Input id="tags" placeholder="fdp, training, 2025" {...form.register("tags")} />
              </div>
            </CardContent>
          </Card>
        </div>

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
              <IdField label="Program ID" field="programId" form={form} />
              <IdField label="Department ID" field="departmentId" form={form} />
              <IdField label="Academic Year ID" field="academicYearId" form={form} />
              <IdField label="NBA Criterion ID" field="criterionId" form={form} />
              <IdField label="NBA Requirement ID" field="requirementId" form={form} />
            </CardContent>
          </Card>

          <Button type="submit" className="w-full" disabled={create.isPending}>
            {create.isPending ? "Uploading…" : "Create Evidence"}
          </Button>
        </div>
      </form>
    </div>
  );
}

function IdField({
  label,
  field,
  form,
}: {
  label: string;
  field: keyof EvidenceMetadataForm;
  form: UseFormReturn<EvidenceMetadataForm>;
}) {
  const message = form.formState.errors[field]?.message;
  return (
    <div className="space-y-1">
      <Label htmlFor={field}>{label}</Label>
      <Input id={field} placeholder="UUID (optional)" {...form.register(field)} />
      {message ? <p className="text-xs text-destructive">{message}</p> : null}
    </div>
  );
}
