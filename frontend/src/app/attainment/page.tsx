"use client";

import { useState } from "react";
import { useAttainmentSummary } from "@/hooks/useAttainmentSummary";
import { SummaryCards } from "@/components/attainment/SummaryCards";
import { POAttainmentTable } from "@/components/attainment/AttainmentTable";
import { PSOAttainmentTable } from "@/components/attainment/AttainmentTable";
import { AttainmentChart } from "@/components/attainment/AttainmentChart";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { DEFAULT_ACADEMIC_YEAR, MOCK_PROGRAM_ID } from "@/lib/api/types";
import { AlertTriangle, RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";

export default function AttainmentDashboardPage() {
  const [programId]    = useState(MOCK_PROGRAM_ID);
  const [academicYear, setAcademicYear] = useState(DEFAULT_ACADEMIC_YEAR);

  const { data, isLoading, isError, error, refetch, isFetching } =
    useAttainmentSummary(programId, academicYear);

  const apiError = error as { message?: string } | null;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Attainment Dashboard</h1>
          <p className="text-muted-foreground text-sm">
            PO and PSO attainment overview — B.Tech CSE Programme
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Select
            value={academicYear}
            onChange={(e) => setAcademicYear(e.target.value)}
            className="w-32"
            aria-label="Select academic year"
          >
            <option value="2023-24">2023-24</option>
            <option value="2022-23">2022-23</option>
            <option value="2024-25">2024-25</option>
          </Select>
          <Button
            variant="outline"
            size="icon"
            onClick={() => refetch()}
            isLoading={isFetching}
            aria-label="Refresh data"
          >
            <RefreshCw className="h-4 w-4" aria-hidden />
          </Button>
        </div>
      </div>

      {/* Error state */}
      {isError && (
        <Alert variant="destructive" role="alert">
          <AlertTriangle className="h-4 w-4" aria-hidden />
          <AlertDescription>
            {apiError?.message ?? "Failed to load attainment data. Please try again."}
          </AlertDescription>
        </Alert>
      )}

      {/* Empty / not calculated state */}
      {!isLoading && !isError && data && !data.poAttainments.length && (
        <Alert variant="warning">
          <AlertDescription>
            No attainment records found for {academicYear}.{" "}
            <a href="/attainment/calculate" className="underline font-medium">
              Run a calculation
            </a>{" "}
            to get started.
          </AlertDescription>
        </Alert>
      )}

      {/* Summary cards */}
      {data && data.poAttainments.length > 0 && (
        <>
          <SummaryCards summary={data} />

          {/* Charts */}
          <div className="grid gap-6 lg:grid-cols-2">
            <Card>
              <CardHeader><CardTitle className="text-base">PO Attainment</CardTitle></CardHeader>
              <CardContent>
                <AttainmentChart
                  data={data.poAttainments}
                  codeKey="poCode"
                  title="Program Outcomes"
                />
              </CardContent>
            </Card>
            <Card>
              <CardHeader><CardTitle className="text-base">PSO Attainment</CardTitle></CardHeader>
              <CardContent>
                <AttainmentChart
                  data={data.psoAttainments}
                  codeKey="psoCode"
                  title="Program Specific Outcomes"
                />
              </CardContent>
            </Card>
          </div>

          {/* Tables */}
          <Card>
            <CardHeader><CardTitle className="text-base">Program Outcomes</CardTitle></CardHeader>
            <CardContent className="p-0">
              <POAttainmentTable data={data.poAttainments} isLoading={isLoading} />
            </CardContent>
          </Card>

          <Card>
            <CardHeader><CardTitle className="text-base">Program Specific Outcomes</CardTitle></CardHeader>
            <CardContent className="p-0">
              <PSOAttainmentTable data={data.psoAttainments} isLoading={isLoading} />
            </CardContent>
          </Card>
        </>
      )}

      {/* Loading skeletons */}
      {isLoading && (
        <div className="space-y-4 animate-pulse" aria-busy="true" aria-label="Loading dashboard">
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            {[...Array(4)].map((_, i) => (
              <div key={i} className="h-28 rounded-lg bg-muted" />
            ))}
          </div>
          <div className="h-64 rounded-lg bg-muted" />
          <div className="h-64 rounded-lg bg-muted" />
        </div>
      )}
    </div>
  );
}
