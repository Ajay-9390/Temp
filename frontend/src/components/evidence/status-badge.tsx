import { Badge } from "@/components/ui/badge";
import { STATUS_META, OCR_STATUS_META } from "@/lib/constants";
import { cn } from "@/lib/utils";
import type { EvidenceStatus, OcrStatus } from "@/lib/types";

export function StatusBadge({ status, className }: { status: EvidenceStatus; className?: string }) {
  const meta = STATUS_META[status];
  return (
    <Badge variant="outline" className={cn("font-medium", meta.className, className)}>
      {meta.label}
    </Badge>
  );
}

export function OcrBadge({ status }: { status: OcrStatus | null }) {
  if (!status) return null;
  const meta = OCR_STATUS_META[status];
  return <Badge variant="outline" className={cn("border-transparent", meta.className)}>{meta.label}</Badge>;
}

export function GapStatusBadge({ status }: { status: string }) {
  if (status === "MISSING") {
    return <Badge variant="outline" className="border-red-200 bg-red-100 text-red-700">Missing</Badge>;
  }
  const known = STATUS_META[status as EvidenceStatus];
  if (known) {
    return <Badge variant="outline" className={cn("font-medium", known.className)}>{known.label}</Badge>;
  }
  return <Badge variant="outline">{status}</Badge>;
}
