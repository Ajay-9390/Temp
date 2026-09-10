import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { Providers } from "./providers";
import { NavBar } from "@/components/layout/NavBar";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "PO/PSO Attainment | NBA Module 09",
  description: "Program Outcome and Program Specific Outcome Attainment Dashboard",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className={inter.className}>
        <Providers>
          <div className="min-h-screen bg-background">
            <NavBar />
            <main className="container mx-auto px-4 py-6 max-w-7xl">
              {children}
            </main>
          </div>
        </Providers>
      </body>
    </html>
  );
}
