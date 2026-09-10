import type { Metadata } from "next";
import "./globals.css";
import { Providers } from "./providers";

export const metadata: Metadata = {
  title: "NBA Evidence Management",
  description: "Manage the lifecycle of NBA accreditation evidence",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className="min-h-screen bg-muted/30 antialiased">
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
