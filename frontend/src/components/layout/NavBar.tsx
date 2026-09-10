"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";
import { BarChart3, Calculator, BookOpen } from "lucide-react";

const navItems = [
  { href: "/attainment",           label: "Dashboard",     icon: BarChart3,  exact: true },
  { href: "/attainment/po",        label: "PO Attainment", icon: BookOpen,   exact: false },
  { href: "/attainment/pso",       label: "PSO Attainment",icon: BookOpen,   exact: false },
  { href: "/attainment/calculate", label: "Calculate",     icon: Calculator, exact: true },
];

export function NavBar() {
  const pathname = usePathname();

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur">
      <div className="container mx-auto flex h-14 items-center gap-6 px-4 max-w-7xl">
        <Link href="/attainment" className="flex items-center gap-2 font-semibold text-sm">
          <BarChart3 className="h-5 w-5 text-primary" aria-hidden />
          <span>NBA Attainment</span>
          <span className="text-xs text-muted-foreground hidden sm:inline">Module 09</span>
        </Link>

        <nav className="flex items-center gap-1" aria-label="Main navigation">
          {navItems.map(({ href, label, icon: Icon, exact }) => {
            const active = exact
              ? pathname === href
              : pathname === href || pathname.startsWith(href + "/");
            return (
              <Link
                key={href}
                href={href}
                className={cn(
                  "flex items-center gap-1.5 rounded-md px-3 py-1.5 text-sm transition-colors",
                  active
                    ? "bg-primary/10 text-primary font-medium"
                    : "text-muted-foreground hover:text-foreground hover:bg-accent"
                )}
                aria-current={active ? "page" : undefined}
              >
                <Icon className="h-3.5 w-3.5" aria-hidden />
                {label}
              </Link>
            );
          })}
        </nav>
      </div>
    </header>
  );
}
