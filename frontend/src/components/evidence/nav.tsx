"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { BarChart3, FileStack, LayoutDashboard, TriangleAlert, Upload } from "lucide-react";
import { cn } from "@/lib/utils";

const items = [
  { href: "/evidence", label: "Dashboard", icon: LayoutDashboard, exact: true },
  { href: "/evidence/list", label: "All Evidence", icon: FileStack, exact: false },
  { href: "/evidence/upload", label: "Upload", icon: Upload, exact: false },
  { href: "/evidence/gaps", label: "Gaps", icon: TriangleAlert, exact: false },
  { href: "/evidence/reports", label: "Reports", icon: BarChart3, exact: false },
];

export function EvidenceNav() {
  const pathname = usePathname();
  return (
    <nav className="flex flex-col gap-1">
      {items.map((item) => {
        const active = item.exact ? pathname === item.href : pathname.startsWith(item.href);
        const Icon = item.icon;
        return (
          <Link
            key={item.href}
            href={item.href}
            className={cn(
              "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors",
              active ? "bg-primary text-primary-foreground" : "text-muted-foreground hover:bg-accent hover:text-foreground",
            )}
          >
            <Icon className="h-4 w-4" />
            {item.label}
          </Link>
        );
      })}
    </nav>
  );
}
