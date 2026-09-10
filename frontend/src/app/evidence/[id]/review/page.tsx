"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { PageHeader } from "@/components/evidence/page-header";
import { StatusBadge } from "@/components/evidence/status-badge";
import { DocumentPreview } from "@/components/evidence/document-preview";
import { ReviewPanel } from "@/components/evidence/review-panel";
import { ReviewHistory } from "@/components/evidence/audit-timeline";
import { useEvidence, useReviews } from "@/hooks/use-evidence";
import { CATEGORY_LABELS } from "@/lib/constants";
import { formatDateTime } from "@/lib/utils";

export default function ReviewPage({ params }: { params: { id: string } }) {
  const { id } = params;
  const router = useRouter();
  const { data: evidence, isLoading } = useEvidence(id);
  const { data: reviews = [] } = useReviews(id);

  if (isLoading) {
    return <Skeleton className="h-[70vh] w-full" />;
  }
  if (!evidence) {
    return <p className="text-muted-foreground">Evidence not found.</p>;
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Review Evidence"
        description={evidence.title}
        actions={
          <div className="flex items-center gap-2">
            <StatusBadge status={evidence.status} />
            <Button asChild variant="ghost">
              <Link href={`/evidence/${id}`}>
                <ArrowLeft className="h-4 w-4" /> Back
              </Link>
            </Button>
          </div>
        }
      />

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-5">
        <div className="lg:col-span-3">
          <DocumentPreview evidenceId={id} version={evidence.latestVersion} />
        </div>

        <div className="space-y-6 lg:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle>Metadata</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2 text-sm">
              <Row label="Category" value={CATEGORY_LABELS[evidence.category]} />
              <Row label="Version" value={`v${evidence.currentVersion}`} />
              <Row label="Criterion" value={evidence.criterionId} mono />
              <Row label="Requirement" value={evidence.requirementId} mono />
              <Row label="Uploaded By" value={evidence.uploadedBy} />
              <Row label="Updated" value={formatDateTime(evidence.updatedAt)} />
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Decision</CardTitle>
            </CardHeader>
            <CardContent>
              <ReviewPanel evidenceId={id} status={evidence.status} onReviewed={() => router.push(`/evidence/${id}`)} />
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Previous Reviews</CardTitle>
            </CardHeader>
            <CardContent>
              <ReviewHistory items={reviews} />
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}

function Row({ label, value, mono }: { label: string; value?: string | null; mono?: boolean }) {
  return (
    <div className="flex items-center justify-between gap-4 border-b py-1.5 last:border-0">
      <span className="text-muted-foreground">{label}</span>
      <span className={mono ? "font-mono text-xs" : ""}>{value || "—"}</span>
    </div>
  );
}
