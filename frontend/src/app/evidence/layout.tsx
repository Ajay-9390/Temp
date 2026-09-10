import Link from "next/link";
import { FileCheck2 } from "lucide-react";
import { EvidenceNav } from "@/components/evidence/nav";

export default function EvidenceLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="mx-auto flex min-h-screen w-full max-w-[1400px] gap-6 p-4 lg:p-6">
      <aside className="hidden w-60 shrink-0 flex-col lg:flex">
        <Link href="/evidence" className="mb-6 flex items-center gap-2 px-3">
          <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary text-primary-foreground">
            <FileCheck2 className="h-5 w-5" />
          </span>
          <div className="leading-tight">
            <div className="text-sm font-semibold">NBA Evidence</div>
            <div className="text-xs text-muted-foreground">Management</div>
          </div>
        </Link>
        <EvidenceNav />
        <div className="mt-auto px-3 pt-6 text-xs text-muted-foreground">
          No-auth dev build · mock user
        </div>
      </aside>
      <main className="min-w-0 flex-1 space-y-6">{children}</main>
    </div>
  );
}
