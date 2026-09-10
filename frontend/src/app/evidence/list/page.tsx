"use client";

import * as React from "react";
import Link from "next/link";
import { ChevronLeft, ChevronRight, Upload } from "lucide-react";
import { Button } from "@/components/ui/button";
import { PageHeader } from "@/components/evidence/page-header";
import { EvidenceFilters, EMPTY_FILTERS, type EvidenceFilterValues } from "@/components/evidence/filter-bar";
import { EvidenceTable } from "@/components/evidence/evidence-table";
import { useEvidenceList } from "@/hooks/use-evidence";
import type { EvidenceListParams } from "@/lib/api";

function useDebounced<T>(value: T, delay = 300): T {
  const [debounced, setDebounced] = React.useState(value);
  React.useEffect(() => {
    const t = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(t);
  }, [value, delay]);
  return debounced;
}

const PAGE_SIZE = 20;

export default function EvidenceListPage() {
  const [filters, setFilters] = React.useState<EvidenceFilterValues>(EMPTY_FILTERS);
  const [page, setPage] = React.useState(0);
  const [sort, setSort] = React.useState("createdAt,desc");
  const debouncedFilters = useDebounced(filters, 350);

  React.useEffect(() => {
    setPage(0);
  }, [debouncedFilters]);

  const params: EvidenceListParams = {
    keyword: debouncedFilters.keyword || undefined,
    category: debouncedFilters.category || undefined,
    status: debouncedFilters.status || undefined,
    fileType: debouncedFilters.fileType || undefined,
    programId: debouncedFilters.programId || undefined,
    academicYearId: debouncedFilters.academicYearId || undefined,
    criterionId: debouncedFilters.criterionId || undefined,
    createdFrom: debouncedFilters.createdFrom ? `${debouncedFilters.createdFrom}T00:00:00Z` : undefined,
    createdTo: debouncedFilters.createdTo ? `${debouncedFilters.createdTo}T23:59:59Z` : undefined,
    page,
    size: PAGE_SIZE,
    sort,
  };

  const { data, isLoading, isFetching } = useEvidenceList(params);

  const onSortChange = (field: string) => {
    setSort((prev) => {
      const [prevField, prevDir] = prev.split(",");
      if (prevField === field) {
        return `${field},${prevDir === "asc" ? "desc" : "asc"}`;
      }
      return `${field},asc`;
    });
  };

  const totalElements = data?.totalElements ?? 0;
  const totalPages = data?.totalPages ?? 0;

  return (
    <div className="space-y-6">
      <PageHeader
        title="All Evidence"
        description={`${totalElements} item${totalElements === 1 ? "" : "s"}`}
        actions={
          <Button asChild>
            <Link href="/evidence/upload">
              <Upload className="h-4 w-4" /> Upload
            </Link>
          </Button>
        }
      />

      <EvidenceFilters value={filters} onChange={setFilters} onClear={() => setFilters(EMPTY_FILTERS)} />

      <EvidenceTable rows={data?.content ?? []} isLoading={isLoading} sort={sort} onSortChange={onSortChange} />

      <div className="flex items-center justify-between">
        <p className="text-sm text-muted-foreground">
          Page {totalPages === 0 ? 0 : page + 1} of {totalPages}
          {isFetching ? " · updating…" : ""}
        </p>
        <div className="flex gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0}
          >
            <ChevronLeft className="h-4 w-4" /> Previous
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={() => setPage((p) => p + 1)}
            disabled={data?.last ?? true}
          >
            Next <ChevronRight className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </div>
  );
}
