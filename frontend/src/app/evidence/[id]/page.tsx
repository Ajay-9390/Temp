"use client";

import * as React from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Archive, Pencil, Send, ShieldCheck, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Badge } from "@/components/ui/badge";
import { PageHeader } from "@/components/evidence/page-header";
import { StatusBadge } from "@/components/evidence/status-badge";
import { DocumentPreview } from "@/components/evidence/document-preview";
import { VersionTimeline } from "@/components/evidence/version-timeline";
import { MappingPanel } from "@/components/evidence/mapping-panel";
import { AuditTimeline, ReviewHistory } from "@/components/evidence/audit-timeline";
import { UploadVersionDialog } from "@/components/evidence/upload-version-dialog";
import {
  useAudit,
  useDeleteEvidence,
  useEvidence,
  useLifecycleAction,
  useMappings,
  useReviews,
  useVersions,
} from "@/hooks/use-evidence";
import { CATEGORY_LABELS } from "@/lib/constants";
import { formatDateTime } from "@/lib/utils";

export default function EvidenceDetailPage({ params }: { params: { id: string } }) {
  const { id } = params;
  const router = useRouter();
  const { data: evidence, isLoading } = useEvidence(id);
  const { data: versions = [] } = useVersions(id);
  const { data: reviews = [] } = useReviews(id);
  const { data: audit = [] } = useAudit(id);
  const { submit, startReview, archive } = useLifecycleAction(id);
  const del = useDeleteEvidence();

  if (isLoading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-10 w-1/2" />
        <Skeleton className="h-64 w-full" />
      </div>
    );
  }
  if (!evidence) {
    return <p className="text-muted-foreground">Evidence not found.</p>;
  }

  const status = evidence.status;
  const editable = status === "DRAFT" || status === "CHANGES_REQUIRED";
  const canSubmit = editable && evidence.currentVersion >= 1;
  const canArchive = status !== "ARCHIVED";

  const handleDelete = () => {
    if (window.confirm("Permanently delete this DRAFT evidence and its files? This cannot be undone.")) {
      del.mutate(id, { onSuccess: () => router.push("/evidence/list") });
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title={evidence.title}
        description={`${CATEGORY_LABELS[evidence.category]} · v${evidence.currentVersion} · created ${formatDateTime(evidence.createdAt)}`}
        actions={
          <div className="flex flex-wrap items-center gap-2">
            <StatusBadge status={status} />
            {editable ? (
              <Button asChild variant="outline">
                <Link href={`/evidence/${id}/edit`}>
                  <Pencil className="h-4 w-4" /> Edit
                </Link>
              </Button>
            ) : null}
            {canSubmit ? (
              <Button onClick={() => submit.mutate(undefined)} disabled={submit.isPending}>
                <Send className="h-4 w-4" /> Submit
              </Button>
            ) : null}
            {status === "SUBMITTED" ? (
              <Button onClick={() => startReview.mutate()} disabled={startReview.isPending}>
                <ShieldCheck className="h-4 w-4" /> Start Review
              </Button>
            ) : null}
            {status === "UNDER_REVIEW" ? (
              <Button asChild>
                <Link href={`/evidence/${id}/review`}>
                  <ShieldCheck className="h-4 w-4" /> Review
                </Link>
              </Button>
            ) : null}
            {canArchive ? <UploadVersionDialog evidenceId={id} /> : null}
            {canArchive ? (
              <Button variant="outline" onClick={() => archive.mutate(undefined)} disabled={archive.isPending}>
                <Archive className="h-4 w-4" /> Archive
              </Button>
            ) : null}
            {status === "DRAFT" ? (
              <Button variant="destructive" onClick={handleDelete} disabled={del.isPending}>
                <Trash2 className="h-4 w-4" /> Delete
              </Button>
            ) : null}
          </div>
        }
      />

      <Tabs defaultValue="overview">
        <TabsList>
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="mappings">Mappings ({evidence.mappingCount})</TabsTrigger>
          <TabsTrigger value="versions">Versions ({evidence.versionCount})</TabsTrigger>
          <TabsTrigger value="reviews">Reviews ({evidence.reviewCount})</TabsTrigger>
          <TabsTrigger value="audit">Audit</TabsTrigger>
        </TabsList>

        <TabsContent value="overview">
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>Evidence Information</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3 text-sm">
                {evidence.description ? <p>{evidence.description}</p> : <p className="text-muted-foreground">No description.</p>}
                <MetaRow label="Category" value={CATEGORY_LABELS[evidence.category]} />
                <MetaRow label="Program ID" value={evidence.programId} mono />
                <MetaRow label="Department ID" value={evidence.departmentId} mono />
                <MetaRow label="Academic Year ID" value={evidence.academicYearId} mono />
                <MetaRow label="Criterion ID" value={evidence.criterionId} mono />
                <MetaRow label="Requirement ID" value={evidence.requirementId} mono />
                <MetaRow label="Uploaded By" value={evidence.uploadedBy} />
                <MetaRow label="Updated" value={formatDateTime(evidence.updatedAt)} />
                {evidence.tags.length ? (
                  <div className="flex flex-wrap gap-1 pt-1">
                    {evidence.tags.map((t) => (
                      <Badge key={t} variant="secondary">
                        {t}
                      </Badge>
                    ))}
                  </div>
                ) : null}
              </CardContent>
            </Card>

            <DocumentPreview evidenceId={id} version={evidence.latestVersion} />
          </div>
        </TabsContent>

        <TabsContent value="mappings">
          <Card>
            <CardHeader>
              <CardTitle>NBA Mappings</CardTitle>
            </CardHeader>
            <CardContent>
              <MappingPanel evidenceId={id} />
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="versions">
          <Card>
            <CardHeader className="flex-row items-center justify-between space-y-0">
              <CardTitle>Version History</CardTitle>
              {canArchive ? <UploadVersionDialog evidenceId={id} /> : null}
            </CardHeader>
            <CardContent>
              <VersionTimeline evidenceId={id} versions={versions} />
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="reviews">
          <Card>
            <CardHeader>
              <CardTitle>Review History</CardTitle>
            </CardHeader>
            <CardContent>
              <ReviewHistory items={reviews} />
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="audit">
          <Card>
            <CardHeader>
              <CardTitle>Audit Trail</CardTitle>
            </CardHeader>
            <CardContent>
              <AuditTimeline items={audit} />
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}

function MetaRow({ label, value, mono }: { label: string; value?: string | null; mono?: boolean }) {
  return (
    <div className="flex items-center justify-between gap-4 border-b py-1.5 last:border-0">
      <span className="text-muted-foreground">{label}</span>
      <span className={mono ? "font-mono text-xs" : "text-right"}>{value || "—"}</span>
    </div>
  );
}
