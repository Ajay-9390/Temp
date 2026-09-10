"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { Pencil } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { StatusBadge } from "@/components/status-badge";
import { useProgram, useProgramOverview, useProgramStatusUpdate } from "@/features/programs/hooks";
import { useAccreditationCycles } from "@/features/accreditation/hooks";
import { useAcademicYears } from "@/features/academic-years/hooks";
import { PROGRAM_TRANSITIONS } from "@/lib/lifecycle";
import { OverviewTab } from "@/features/programs/OverviewTab";
import { SemestersByYear } from "@/features/semesters/SemestersByYear";

export default function ProgramDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { data: program, isLoading } = useProgram(id);
  const overview = useProgramOverview(id);
  const cycles = useAccreditationCycles(id);
  const years = useAcademicYears(id);
  const statusUpdate = useProgramStatusUpdate(id);

  if (isLoading || !program) return <p className="text-muted-foreground">Loading...</p>;

  const nextStatuses = PROGRAM_TRANSITIONS[program.status];

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-semibold">{program.name}</h1>
            <StatusBadge status={program.status} />
          </div>
          <p className="text-sm text-muted-foreground">
            {program.code} · {program.degree} · {program.departmentName}
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          {nextStatuses.map((s) => (
            <Button key={s} size="sm" variant="outline" disabled={statusUpdate.isPending}
              onClick={() => statusUpdate.mutate(s)}>
              Move to {s}
            </Button>
          ))}
          <Button asChild size="sm"><Link href={`/programs/${id}/edit`}><Pencil className="h-4 w-4" /> Edit</Link></Button>
        </div>
      </div>

      {statusUpdate.isError && (
        <p className="text-sm text-destructive">{(statusUpdate.error as Error).message}</p>
      )}

      <Tabs defaultValue="overview">
        <TabsList>
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="accreditation">Accreditation</TabsTrigger>
          <TabsTrigger value="years">Academic Years</TabsTrigger>
          <TabsTrigger value="semesters">Semesters</TabsTrigger>
        </TabsList>

        <TabsContent value="overview">
          <OverviewTab overview={overview.data} program={program} />
        </TabsContent>

        <TabsContent value="accreditation">
          <Card>
            <CardHeader className="flex-row items-center justify-between">
              <CardTitle className="text-base">Accreditation Cycles</CardTitle>
              <Button asChild size="sm"><Link href={`/programs/${id}/accreditation-cycles/new`}>Add Cycle</Link></Button>
            </CardHeader>
            <CardContent className="space-y-3">
              {cycles.data?.length === 0 && <p className="text-sm text-muted-foreground">No accreditation cycles yet.</p>}
              {cycles.data?.map((c) => (
                <div key={c.id} className="flex items-center justify-between rounded-md border p-3">
                  <div>
                    <p className="font-medium">{c.name} <span className="text-muted-foreground">· {c.tier}</span></p>
                    <p className="text-xs text-muted-foreground">{c.frameworkVersion} · Application year {c.applicationYear}</p>
                  </div>
                  <div className="flex items-center gap-3">
                    <StatusBadge status={c.status} />
                    <Button asChild size="sm" variant="outline">
                      <Link href={`/programs/${id}/accreditation-cycles`}>Manage</Link>
                    </Button>
                  </div>
                </div>
              ))}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="years">
          <Card>
            <CardHeader className="flex-row items-center justify-between">
              <CardTitle className="text-base">Academic Years</CardTitle>
              <Button asChild size="sm"><Link href={`/programs/${id}/academic-years`}>Manage Years</Link></Button>
            </CardHeader>
            <CardContent className="space-y-3">
              {years.data?.length === 0 && <p className="text-sm text-muted-foreground">No academic years yet.</p>}
              {years.data?.map((y) => (
                <div key={y.id} className="flex items-center justify-between rounded-md border p-3">
                  <div>
                    <p className="font-medium">{y.name} {y.status === "ACTIVE" && <span className="text-xs text-green-700">(current)</span>}</p>
                    <p className="text-xs text-muted-foreground">{y.startDate} → {y.endDate} · {y.semesterCount} semesters</p>
                  </div>
                  <div className="flex items-center gap-3">
                    <StatusBadge status={y.status} />
                    <Button asChild size="sm" variant="outline">
                      <Link href={`/academic-years/${y.id}/semesters`}>Semesters</Link>
                    </Button>
                  </div>
                </div>
              ))}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="semesters">
          <Card>
            <CardHeader><CardTitle className="text-base">Semester Structure</CardTitle></CardHeader>
            <CardContent className="space-y-4">
              {years.data?.length === 0 && <p className="text-sm text-muted-foreground">Create an academic year first.</p>}
              {years.data?.map((y) => <SemestersByYear key={y.id} year={y} />)}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
