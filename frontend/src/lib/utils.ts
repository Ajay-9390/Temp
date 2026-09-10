import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";
import type { AttainmentStatus } from "./api/types";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function formatAttainment(value: number | null | undefined): string {
  if (value == null) return "—";
  return `${value.toFixed(2)}%`;
}

export function statusColor(status: AttainmentStatus | string): string {
  switch (status) {
    case "Excellent":       return "text-green-700 bg-green-100";
    case "Good":            return "text-blue-700 bg-blue-100";
    case "Needs Improvement": return "text-amber-700 bg-amber-100";
    default:                return "text-gray-700 bg-gray-100";
  }
}

export function attainmentBarColor(value: number | null): string {
  if (value == null) return "bg-gray-300";
  if (value >= 75) return "bg-green-500";
  if (value >= 60) return "bg-blue-500";
  return "bg-amber-500";
}

export function mappingLevelLabel(level: number): string {
  switch (level) {
    case 3:  return "High (3)";
    case 2:  return "Medium (2)";
    case 1:  return "Low (1)";
    default: return "None (0)";
  }
}

export function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString("en-IN", {
    day: "2-digit", month: "short", year: "numeric",
    hour: "2-digit", minute: "2-digit",
  });
}
