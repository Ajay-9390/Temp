"use client";

import { Download, FileQuestion } from "lucide-react";
import { Button } from "@/components/ui/button";
import { api } from "@/lib/api";
import type { VersionResponse } from "@/lib/types";

export function DocumentPreview({
  evidenceId,
  version,
}: {
  evidenceId: string;
  version: VersionResponse | null;
}) {
  if (!version) {
    return (
      <div className="flex h-full min-h-[400px] flex-col items-center justify-center gap-2 rounded-lg border bg-muted/30 text-muted-foreground">
        <FileQuestion className="h-10 w-10" />
        <p className="text-sm">No file uploaded for this evidence.</p>
      </div>
    );
  }

  const previewUrl = api.versions.previewUrl(evidenceId, version.versionNumber);
  const downloadUrl = api.versions.downloadUrl(evidenceId, version.versionNumber);
  const mime = version.mimeType ?? "";
  const isImage = mime.startsWith("image/");
  const isPdf = mime === "application/pdf" || version.fileType === "pdf";

  return (
    <div className="flex h-full flex-col overflow-hidden rounded-lg border bg-card">
      <div className="flex items-center justify-between border-b px-4 py-2">
        <div className="min-w-0">
          <p className="truncate text-sm font-medium">{version.fileName}</p>
          <p className="text-xs text-muted-foreground">Version {version.versionNumber}</p>
        </div>
        <Button asChild variant="outline" size="sm">
          <a href={downloadUrl}>
            <Download className="h-4 w-4" /> Download
          </a>
        </Button>
      </div>
      <div className="flex-1 bg-muted/20">
        {isImage ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img src={previewUrl} alt={version.fileName} className="mx-auto max-h-[70vh] object-contain" />
        ) : isPdf ? (
          <iframe src={previewUrl} title={version.fileName} className="h-[70vh] w-full" />
        ) : (
          <div className="flex h-full min-h-[300px] flex-col items-center justify-center gap-3 p-6 text-center text-muted-foreground">
            <FileQuestion className="h-10 w-10" />
            <p className="text-sm">Inline preview is not available for this file type ({version.fileType?.toUpperCase()}).</p>
            <Button asChild variant="outline" size="sm">
              <a href={downloadUrl}>
                <Download className="h-4 w-4" /> Download to view
              </a>
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}
