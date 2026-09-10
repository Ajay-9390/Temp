import * as React from "react";
import { cn } from "@/lib/utils";
import type { AttainmentStatus } from "@/lib/api/types";

interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  variant?: "default" | "success" | "info" | "warning" | "destructive";
}

export function Badge({ className, variant = "default", ...props }: BadgeProps) {
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium",
        variant === "default"     && "bg-gray-100 text-gray-700",
        variant === "success"     && "bg-green-100 text-green-700",
        variant === "info"        && "bg-blue-100 text-blue-700",
        variant === "warning"     && "bg-amber-100 text-amber-700",
        variant === "destructive" && "bg-red-100 text-red-700",
        className
      )}
      {...props}
    />
  );
}

export function StatusBadge({ status }: { status: AttainmentStatus | string }) {
  const variant =
    status === "Excellent" ? "success" :
    status === "Good" ? "info" :
    status === "Needs Improvement" ? "warning" : "default";
  return <Badge variant={variant}>{status}</Badge>;
}
