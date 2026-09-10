import { Badge, type BadgeProps } from "@/components/ui/badge";

const VARIANT_MAP: Record<string, BadgeProps["variant"]> = {
  // program / generic
  DRAFT: "muted",
  ACTIVE: "success",
  INACTIVE: "warning",
  ARCHIVED: "muted",
  // accreditation
  PREPARATION: "info",
  SUBMITTED: "info",
  UNDER_REVIEW: "info",
  ACCREDITED: "success",
  EXPIRED: "muted",
  REJECTED: "destructive",
  CANCELLED: "destructive",
  // academic
  PLANNED: "muted",
  COMPLETED: "info",
};

/** Renders a status string as a colour-coded badge. */
export function StatusBadge({ status }: { status?: string | null }) {
  if (!status) return <span className="text-muted-foreground">—</span>;
  return <Badge variant={VARIANT_MAP[status] ?? "secondary"}>{status.replace(/_/g, " ")}</Badge>;
}
