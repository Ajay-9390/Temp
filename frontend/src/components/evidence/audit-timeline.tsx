import { Badge } from "@/components/ui/badge";
import { STATUS_META } from "@/lib/constants";
import { formatDateTime } from "@/lib/utils";
import type { AuditResponse, EvidenceStatus, ReviewResponse } from "@/lib/types";

export function AuditTimeline({ items }: { items: AuditResponse[] }) {
  if (items.length === 0) {
    return <p className="text-sm text-muted-foreground">No audit events recorded yet.</p>;
  }
  return (
    <ol className="relative space-y-4 border-l pl-6">
      {items.map((a) => (
        <li key={a.id} className="relative">
          <span className="absolute -left-[29px] top-1 h-3 w-3 rounded-full bg-primary" />
          <div className="flex flex-wrap items-center gap-2">
            <Badge variant="secondary">{a.action}</Badge>
            <span className="text-xs text-muted-foreground">{formatDateTime(a.createdAt)}</span>
          </div>
          <p className="mt-1 text-sm">
            by <span className="font-medium">{a.performedBy}</span>
            {a.entityType ? <span className="text-muted-foreground"> · {a.entityType}</span> : null}
          </p>
          {a.oldValue || a.newValue ? (
            <p className="mt-0.5 text-xs text-muted-foreground">
              {a.oldValue ? <span>{a.oldValue}</span> : null}
              {a.oldValue && a.newValue ? " → " : null}
              {a.newValue ? <span className="text-foreground">{a.newValue}</span> : null}
            </p>
          ) : null}
          {a.reason ? <p className="mt-0.5 text-sm italic text-muted-foreground">“{a.reason}”</p> : null}
        </li>
      ))}
    </ol>
  );
}

export function ReviewHistory({ items }: { items: ReviewResponse[] }) {
  if (items.length === 0) {
    return <p className="text-sm text-muted-foreground">No reviews yet.</p>;
  }
  return (
    <div className="space-y-3">
      {items.map((r) => {
        const meta = STATUS_META[r.resultingStatus as EvidenceStatus];
        return (
          <div key={r.id} className="rounded-md border p-3">
            <div className="flex flex-wrap items-center gap-2">
              <Badge variant="outline" className={meta?.className}>
                {r.decision.replace("_", " ")}
              </Badge>
              <span className="text-sm font-medium">{r.reviewer}</span>
              {r.versionNumber ? <span className="text-xs text-muted-foreground">v{r.versionNumber}</span> : null}
              <span className="ml-auto text-xs text-muted-foreground">{formatDateTime(r.reviewedAt)}</span>
            </div>
            {r.comments ? <p className="mt-2 text-sm">{r.comments}</p> : null}
          </div>
        );
      })}
    </div>
  );
}
