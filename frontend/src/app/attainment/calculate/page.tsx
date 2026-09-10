"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useCalculatePOAttainment, useCalculatePSOAttainment, useCalculateAllAttainment } from "@/hooks/useCalculateAttainment";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { StatusBadge } from "@/components/ui/badge";
import { formatAttainment } from "@/lib/utils";
import { MOCK_PROGRAM_ID, DEFAULT_ACADEMIC_YEAR } from "@/lib/api/types";
import type { POAttainmentResponse, PSOAttainmentResponse } from "@/lib/api/types";
import { CheckCircle2, AlertTriangle, Calculator, BarChart3 } from "lucide-react";

const schema = z.object({
  programId:          z.string().uuid("Must be a valid UUID"),
  academicYear:       z.string().regex(/^\d{4}-\d{2,4}$/, "Format: YYYY-YY (e.g. 2023-24)"),
  calculationMethod:  z.string().min(1),
  calculationVersion: z.string().min(1),
});

type FormValues = z.infer<typeof schema>;
type CalculationMode = "PO" | "PSO" | "ALL";

export default function CalculatePage() {
  const [mode, setMode]           = useState<CalculationMode>("ALL");
  const [poResults, setPOResults] = useState<POAttainmentResponse[]>([]);
  const [psoResults, setPSOResults] = useState<PSOAttainmentResponse[]>([]);

  const calcPO  = useCalculatePOAttainment();
  const calcPSO = useCalculatePSOAttainment();
  const calcAll = useCalculateAllAttainment();

  const { register, handleSubmit, formState: { errors } } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      programId:          MOCK_PROGRAM_ID,
      academicYear:       DEFAULT_ACADEMIC_YEAR,
      calculationMethod:  "WEIGHTED",
      calculationVersion: "v1",
    },
  });

  const isLoading = calcPO.isPending || calcPSO.isPending || calcAll.isPending;
  const mutError  = (calcPO.error || calcPSO.error || calcAll.error) as { message?: string } | null;
  const isSuccess = calcPO.isSuccess || calcPSO.isSuccess || calcAll.isSuccess;
  const hasResults = poResults.length > 0 || psoResults.length > 0;

  const onSubmit = async (values: FormValues) => {
    setPOResults([]);
    setPSOResults([]);
    calcPO.reset();
    calcPSO.reset();
    calcAll.reset();

    if (mode === "PO") {
      const res = await calcPO.mutateAsync(values);
      setPOResults(res);
    } else if (mode === "PSO") {
      const res = await calcPSO.mutateAsync(values);
      setPSOResults(res);
    } else {
      const res = await calcAll.mutateAsync(values);
      setPOResults(res.poResults);
      setPSOResults(res.psoResults);
    }
  };

  return (
    <div className="space-y-4" data-testid="calculate-page">
      {/* Page heading */}
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Calculate Attainment</h1>
        <p className="text-muted-foreground text-sm">
          Select a program, academic year, and method, then trigger the calculation.
        </p>
      </div>

      {/* Two-column layout: form left, results right */}
      <div className="flex gap-6 items-start">

        {/* ── Left: form ─────────────────────────────────────────────────── */}
        <div className="w-full max-w-sm shrink-0">
          <Card>
            <CardHeader>
              <CardTitle className="text-base flex items-center gap-2">
                <Calculator className="h-4 w-4" aria-hidden />
                Calculation Parameters
              </CardTitle>
              <CardDescription>Calculations are idempotent — safe to re-run.</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>

                <Field label="Program ID" error={errors.programId?.message}>
                  <Input
                    {...register("programId")}
                    placeholder="UUID of the program"
                    aria-invalid={!!errors.programId}
                    data-testid="input-program-id"
                  />
                </Field>

                <Field label="Academic Year" error={errors.academicYear?.message}>
                  <Input
                    {...register("academicYear")}
                    placeholder="e.g. 2023-24"
                    aria-invalid={!!errors.academicYear}
                    data-testid="input-academic-year"
                  />
                </Field>

                <Field label="Calculation Method" error={errors.calculationMethod?.message}>
                  <Select
                    {...register("calculationMethod")}
                    aria-invalid={!!errors.calculationMethod}
                    data-testid="select-method"
                  >
                    <option value="WEIGHTED">Weighted Average</option>
                  </Select>
                </Field>

                <Field label="Calculation Version" error={errors.calculationVersion?.message}>
                  <Input
                    {...register("calculationVersion")}
                    placeholder="e.g. v1"
                    aria-invalid={!!errors.calculationVersion}
                    data-testid="input-version"
                  />
                </Field>

                {/* Mode selector */}
                <div className="space-y-1.5">
                  <p className="text-sm font-medium">What to calculate</p>
                  <div className="flex flex-wrap gap-2">
                    {(["PO", "PSO", "ALL"] as CalculationMode[]).map((m) => (
                      <button
                        key={m}
                        type="button"
                        onClick={() => setMode(m)}
                        className={`rounded-md border px-4 py-2 text-sm font-medium transition-colors
                          ${mode === m
                            ? "bg-primary text-primary-foreground border-primary"
                            : "bg-background hover:bg-accent border-input"}`}
                        data-testid={`mode-${m.toLowerCase()}`}
                        aria-pressed={mode === m}
                      >
                        {m === "ALL" ? "PO + PSO" : `${m} Attainment`}
                      </button>
                    ))}
                  </div>
                </div>

                <div className="pt-2">
                  <Button
                    type="submit"
                    isLoading={isLoading}
                    disabled={isLoading}
                    className="w-full gap-2"
                    data-testid="btn-calculate"
                  >
                    <BarChart3 className="h-4 w-4" aria-hidden />
                    {mode === "ALL" ? "Calculate PO + PSO" :
                     mode === "PO"  ? "Calculate PO Attainment" :
                                      "Calculate PSO Attainment"}
                  </Button>
                </div>

              </form>
            </CardContent>
          </Card>
        </div>

        {/* ── Right: results panel ────────────────────────────────────────── */}
        <div className="flex-1 min-w-0 space-y-4">

          {/* Error */}
          {mutError && (
            <Alert variant="destructive" role="alert">
              <AlertTriangle className="h-4 w-4" aria-hidden />
              <AlertTitle>Calculation Failed</AlertTitle>
              <AlertDescription>
                {mutError.message ?? "An unexpected error occurred. Check that the backend is running."}
              </AlertDescription>
            </Alert>
          )}

          {/* Loading placeholder */}
          {isLoading && (
            <div className="space-y-3 animate-pulse" aria-busy="true" aria-label="Calculating">
              <div className="h-10 rounded-lg bg-muted" />
              <div className="h-48 rounded-lg bg-muted" />
            </div>
          )}

          {/* Success */}
          {isSuccess && hasResults && (
            <div className="space-y-4">
              <Alert variant="default" className="border-green-300 bg-green-50 text-green-800">
                <CheckCircle2 className="h-4 w-4" aria-hidden />
                <AlertTitle>Calculation Complete</AlertTitle>
                <AlertDescription>
                  {poResults.length > 0 && `${poResults.length} PO records saved. `}
                  {psoResults.length > 0 && `${psoResults.length} PSO records saved.`}
                </AlertDescription>
              </Alert>

              {poResults.length > 0 && (
                <ResultTable
                  title="PO Results"
                  rows={poResults.map((r) => ({
                    code: r.poCode,
                    description: r.poDescription,
                    attainment: r.finalAttainment,
                    status: r.status,
                  }))}
                />
              )}

              {psoResults.length > 0 && (
                <ResultTable
                  title="PSO Results"
                  rows={psoResults.map((r) => ({
                    code: r.psoCode,
                    description: r.psoDescription,
                    attainment: r.finalAttainment,
                    status: r.status,
                  }))}
                />
              )}
            </div>
          )}

          {/* Empty state — nothing calculated yet */}
          {!isLoading && !isSuccess && !mutError && (
            <div className="flex h-full min-h-[200px] items-center justify-center rounded-lg border border-dashed text-muted-foreground text-sm">
              Results will appear here after calculation
            </div>
          )}

        </div>
      </div>
    </div>
  );
}

// ── Shared helpers ────────────────────────────────────────────────────────────

function Field({
  label, error, children,
}: { label: string; error?: string; children: React.ReactNode }) {
  return (
    <div className="space-y-1.5">
      <label className="text-sm font-medium">{label}</label>
      {children}
      {error && <p className="text-xs text-destructive" role="alert">{error}</p>}
    </div>
  );
}

function ResultTable({
  title,
  rows,
}: {
  title: string;
  rows: { code: string; description: string; attainment: number | null; status: string }[];
}) {
  return (
    <Card>
      <CardHeader className="pb-2">
        <CardTitle className="text-base">{title}</CardTitle>
      </CardHeader>
      <CardContent className="p-0">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-muted/50">
              <tr>
                <th className="px-4 py-2 text-left font-medium">Code</th>
                <th className="px-4 py-2 text-left font-medium">Description</th>
                <th className="px-4 py-2 text-right font-medium">Attainment</th>
                <th className="px-4 py-2 text-right font-medium">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {rows.map((r) => (
                <tr key={r.code} className="hover:bg-muted/20 transition-colors">
                  <td className="px-4 py-2.5 font-semibold">{r.code}</td>
                  <td className="px-4 py-2.5 text-muted-foreground truncate max-w-xs" title={r.description}>
                    {r.description}
                  </td>
                  <td className="px-4 py-2.5 text-right tabular-nums font-medium">
                    {formatAttainment(r.attainment)}
                  </td>
                  <td className="px-4 py-2.5 text-right">
                    <StatusBadge status={r.status} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </CardContent>
    </Card>
  );
}
