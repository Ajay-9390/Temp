"use client";

import { useState } from "react";
import { usePOAttainments } from "@/hooks/usePOAttainment";
import { POAttainmentTable } from "@/components/attainment/AttainmentTable";
import { AttainmentChart } from "@/components/attainment/AttainmentChart";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Select } from "@/components/ui/select";
import { DEFAULT_ACADEMIC_YEAR, MOCK_PROGRAM_ID } from "@/lib/api/types";
import { AlertTriangle } from "lucide-react";

export default function POListPage() {
  const [academicYear, setAcademicYear] = useState(DEFAULT_ACADEMIC_YEAR);
  const { data = [], isLoading, isError, error } = usePOAttainments(MOCK_PROGRAM_ID, academicYear);
  const apiError = error as { message?: string } | null;

  return (
    <div className="space-y-6" data-testid="po-list-page">
      <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">PO Attainment</h1>
          <p className="text-muted-foreground text-sm">All Program Outcome attainment records</p>
        </div>
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
      </div>

      {isError && (
        <Alert variant="destructive">
          <AlertTriangle className="h-4 w-4" aria-hidden />
          <AlertDescription>{apiError?.message ?? "Failed to load PO attainments."}</AlertDescription>
        </Alert>
      )}

      {data.length > 0 && (
        <Card>
          <CardHeader><CardTitle className="text-base">Attainment Overview</CardTitle></CardHeader>
          <CardContent>
            <AttainmentChart data={data} codeKey="poCode" title="PO Attainment (%)"/>
          </CardContent>
        </Card>
      )}

      <POAttainmentTable data={data} isLoading={isLoading} />
    </div>
  );
}
