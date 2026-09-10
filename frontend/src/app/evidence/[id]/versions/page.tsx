"use client";

import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { PageHeader } from "@/components/evidence/page-header";
import { StatusBadge } from "@/components/evidence/status-badge";
import { VersionTimeline } from "@/components/evidence/version-timeline";
import { UploadVersionDialog } from "@/components/evidence/upload-version-dialog";
import { useEvidence, useVersions } from "@/hooks/use-evidence";

export default function VersionsPage({ params }: { params: { id: string } }) {
  const { id } = params;
  const { data: evidence, isLoading } = useEvidence(id);
  const { data: versions = [], isLoading: loadingVersions } = useVersions(id);

  if (isLoading) {
    return <Skeleton className="h-96 w-full" />;
  }
  if (!evidence) {
    return <p className="text-muted-foreground">Evidence not found.</p>;
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Version History"
        description={evidence.title}
        actions={
          <div className="flex items-center gap-2">
            <StatusBadge status={evidence.status} />
            {evidence.status !== "ARCHIVED" ? <UploadVersionDialog evidenceId={id} /> : null}
            <Button asChild variant="ghost">
              <Link href={`/evidence/${id}`}>
                <ArrowLeft className="h-4 w-4" /> Back
              </Link>
            </Button>
          </div>
        }
      />

      <Card>
        <CardHeader>
          <CardTitle>Timeline</CardTitle>
        </CardHeader>
        <CardContent>
          {loadingVersions ? <Skeleton className="h-40 w-full" /> : <VersionTimeline evidenceId={id} versions={versions} />}
        </CardContent>
      </Card>
    </div>
  );
}
