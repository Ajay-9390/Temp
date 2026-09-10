import { Download, Eye, FileClock } from "lucide-react";
import { Button } from "@/components/ui/button";
import { OcrBadge } from "@/components/evidence/status-badge";
import { api } from "@/lib/api";
import { formatBytes, formatDateTime } from "@/lib/utils";
import type { VersionResponse } from "@/lib/types";

export function VersionTimeline({ evidenceId, versions }: { evidenceId: string; versions: VersionResponse[] }) {
  if (versions.length === 0) {
    return <p className="text-sm text-muted-foreground">No versions uploaded yet.</p>;
  }
  return (
    <ol className="relative space-y-6 border-l pl-6">
      {versions.map((v) => (
        <li key={v.id} className="relative">
          <span className="absolute -left-[31px] flex h-6 w-6 items-center justify-center rounded-full border bg-background">
            <FileClock className="h-3.5 w-3.5 text-muted-foreground" />
          </span>
          <div className="flex flex-wrap items-center gap-2">
            <span className="font-semibold">Version {v.versionNumber}</span>
            <OcrBadge status={v.ocrStatus} />
          </div>
          <p className="mt-0.5 text-sm text-muted-foreground">
            {v.fileName} · {formatBytes(v.fileSize)} · {v.fileType?.toUpperCase() ?? ""}
            {v.pageCount ? ` · ${v.pageCount} pages` : ""}
          </p>
          <p className="text-xs text-muted-foreground">
            Uploaded by {v.uploadedBy} on {formatDateTime(v.createdAt)}
          </p>
          {v.changeReason ? <p className="mt-1 text-sm">Reason: {v.changeReason}</p> : null}
          {v.textPreview ? (
            <p className="mt-2 line-clamp-2 rounded bg-muted p-2 text-xs text-muted-foreground">{v.textPreview}</p>
          ) : null}
          <div className="mt-2 flex gap-2">
            <Button asChild variant="outline" size="sm">
              <a href={api.versions.previewUrl(evidenceId, v.versionNumber)} target="_blank" rel="noreferrer">
                <Eye className="h-4 w-4" /> Preview
              </a>
            </Button>
            <Button asChild variant="outline" size="sm">
              <a href={api.versions.downloadUrl(evidenceId, v.versionNumber)}>
                <Download className="h-4 w-4" /> Download
              </a>
            </Button>
          </div>
        </li>
      ))}
    </ol>
  );
}
