import type { Metadata } from "next";
import Link from "next/link";
import { GraduationCap } from "lucide-react";
import "./globals.css";
import { Providers } from "./providers";

export const metadata: Metadata = {
  title: "NBA Program Management",
  description: "Manage institutions, departments, programs and accreditation context",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>
        <Providers>
          <div className="min-h-screen bg-muted/30">
            <header className="border-b bg-background">
              <div className="container flex h-16 items-center justify-between">
                <Link href="/programs" className="flex items-center gap-2 font-semibold">
                  <GraduationCap className="h-6 w-6" />
                  <span>NBA Program Management</span>
                </Link>
                <nav className="flex items-center gap-6 text-sm font-medium text-muted-foreground">
                  <Link href="/programs" className="hover:text-foreground">Programs</Link>
                  <Link href="/departments" className="hover:text-foreground">Departments</Link>
                </nav>
              </div>
            </header>
            <main className="container py-8">{children}</main>
          </div>
        </Providers>
      </body>
    </html>
  );
}
