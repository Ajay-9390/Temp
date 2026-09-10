"use client";

import { use } from "react";
import Link from "next/link";
import { usePSOTrace } from "@/hooks/useAttainmentTrace";
import { StatusBadge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { AttainmentBar } from "@/components/attainment/AttainmentBar";
import { formatAttainment, formatDate, mappingLevelLabel } from "@/lib/utils";
import { ArrowLeft, AlertTriangle, ChevronDown, ChevronRight } from "lucide-react";
import { useState } from "react";
import type { CourseBreakdown } from "@/lib/api/types";

interface Props {
  params: Promise<{ id: string }>;
}

export default function PSODetailPage({ params }: Props) {
  const { id } = use(params);
  const { data: trace, isLoading, isError, error } = usePSOTrace(id);
  const apiError = error as { message?: string } | null;

  if (isLoading) return <DetailSkeleton />;

  if (isError) {
    return (
      <Alert variant="destructive" role="alert">
        <AlertTriangle className="h-4 w-4" aria-hidden />
        <AlertDescription>
          {apiError?.message ?? "Failed to load PSO attainment details."}
        </AlertDescription>
      </Alert>
    );
  }

  if (!trace) return null;

  return (
    <div className="space-y-6" data-testid="pso-detail-page">
      <Link href="/attainment/pso" className="flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground">
        <ArrowLeft className="h-4 w-4" aria-hidden /> Back to PSO List
      </Link>

      <Card>
        <CardHeader>
          <div className="flex items-start justify-between gap-4">
            <div>
              <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">Program Specific Outcome</p>
              <CardTitle className="text-xl mt-1">{trace.psoCode}</CardTitle>
              <p className="text-sm text-muted-foreground mt-1 max-w-2xl">{trace.psoDescription}</p>
            </div>
            <StatusBadge status={trace.finalAttainment != null
              ? (trace.finalAttainment >= 75 ? "Excellent" : trace.finalAttainment >= 60 ? "Good" : "Needs Improvement")
              : "Needs Improvement"
            } />
          </div>
        </CardHeader>
        <CardContent>
          <div className="grid gap-6 sm:grid-cols-3">
            <div>
              <p className="text-xs text-muted-foreground mb-1">Direct Attainment</p>
              <AttainmentBar value={trace.directAttainment} />
            </div>
            <div>
              <p className="text-xs text-muted-foreground mb-1">Indirect Attainment</p>
              <AttainmentBar value={trace.indirectAttainment ?? null} />
            </div>
            <div>
              <p className="text-xs text-muted-foreground mb-1">Final Attainment</p>
              <AttainmentBar value={trace.finalAttainment} />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader><CardTitle className="text-base">Calculation Details</CardTitle></CardHeader>
        <CardContent>
          <dl className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-sm">
            <MetaItem label="Academic Year"  value={trace.academicYear} />
            <MetaItem label="Method"         value={trace.calculationMethod} />
            <MetaItem label="Version"        value={trace.calculationVersion} />
            <MetaItem label="Calculated At"  value={formatDate(trace.createdAt)} />
            <MetaItem label="Formula"        value={trace.formula} className="col-span-2 sm:col-span-4" />
            <MetaItem label="Total Weight"   value={trace.totalWeight.toString()} />
            <MetaItem label="Weighted Sum"   value={trace.weightedSum.toFixed(2)} />
          </dl>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Calculation Trace</CardTitle>
          <p className="text-xs text-muted-foreground">CO contributions per course</p>
        </CardHeader>
        <CardContent className="space-y-3">
          {trace.courseBreakdown.length === 0 ? (
            <p className="text-sm text-muted-foreground">No course contributions recorded.</p>
          ) : (
            trace.courseBreakdown.map((cb) => <CourseSection key={cb.courseId} course={cb} />)
          )}
        </CardContent>
      </Card>
    </div>
  );
}

function CourseSection({ course }: { course: CourseBreakdown }) {
  const [open, setOpen] = useState(true);
  return (
    <div className="rounded-lg border overflow-hidden">
      <button
        className="w-full flex items-center justify-between px-4 py-3 bg-muted/30 hover:bg-muted/50 transition-colors text-left"
        onClick={() => setOpen((o) => !o)}
        aria-expanded={open}
      >
        <span className="font-medium text-sm">{course.courseName}</span>
        {open ? <ChevronDown className="h-4 w-4" aria-hidden /> : <ChevronRight className="h-4 w-4" aria-hidden />}
      </button>
      {open && (
        <table className="w-full text-sm">
          <thead className="bg-muted/20">
            <tr>
              <th className="px-4 py-2 text-left font-medium">CO</th>
              <th className="px-4 py-2 text-right font-medium">Attainment</th>
              <th className="px-4 py-2 text-right font-medium">Mapping Level</th>
              <th className="px-4 py-2 text-right font-medium">Weighted Value</th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {course.cos.map((co) => (
              <tr key={co.coId} className="hover:bg-muted/10">
                <td className="px-4 py-2 font-medium">{co.coCode}</td>
                <td className="px-4 py-2 text-right tabular-nums">{formatAttainment(co.attainment)}</td>
                <td className="px-4 py-2 text-right">
                  <span className={`inline-flex items-center rounded px-1.5 py-0.5 text-xs font-medium
                    ${co.mappingLevel === 3 ? "bg-green-100 text-green-700" :
                      co.mappingLevel === 2 ? "bg-blue-100 text-blue-700" :
                      co.mappingLevel === 1 ? "bg-amber-100 text-amber-700" :
                      "bg-gray-100 text-gray-500"}`}>
                    {mappingLevelLabel(co.mappingLevel)}
                  </span>
                </td>
                <td className="px-4 py-2 text-right tabular-nums font-medium">
                  {co.weightedValue.toFixed(2)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

function MetaItem({ label, value, className }: { label: string; value: string; className?: string }) {
  return (
    <div className={className}>
      <dt className="text-xs text-muted-foreground">{label}</dt>
      <dd className="font-medium mt-0.5 text-sm">{value || "—"}</dd>
    </div>
  );
}

function DetailSkeleton() {
  return (
    <div className="space-y-6 animate-pulse" aria-busy="true" aria-label="Loading details">
      <div className="h-8 w-32 rounded bg-muted" />
      <div className="h-48 rounded-lg bg-muted" />
      <div className="h-32 rounded-lg bg-muted" />
      <div className="h-64 rounded-lg bg-muted" />
    </div>
  );
}
