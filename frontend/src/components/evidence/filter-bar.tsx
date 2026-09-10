"use client";

import { Search, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { CATEGORY_LABELS, FILE_TYPE_OPTIONS, STATUS_META } from "@/lib/constants";
import { EVIDENCE_CATEGORIES, EVIDENCE_STATUSES } from "@/lib/types";

export interface EvidenceFilterValues {
  keyword: string;
  category: string;
  status: string;
  fileType: string;
  programId: string;
  academicYearId: string;
  criterionId: string;
  createdFrom: string;
  createdTo: string;
}

export const EMPTY_FILTERS: EvidenceFilterValues = {
  keyword: "",
  category: "",
  status: "",
  fileType: "",
  programId: "",
  academicYearId: "",
  criterionId: "",
  createdFrom: "",
  createdTo: "",
};

const ALL = "ALL";

export function EvidenceFilters({
  value,
  onChange,
  onClear,
}: {
  value: EvidenceFilterValues;
  onChange: (next: EvidenceFilterValues) => void;
  onClear: () => void;
}) {
  const set = (patch: Partial<EvidenceFilterValues>) => onChange({ ...value, ...patch });
  const fromSelect = (v: string) => (v === ALL ? "" : v);

  return (
    <div className="space-y-4 rounded-lg border bg-card p-4">
      <div className="flex flex-col gap-3 md:flex-row md:items-end">
        <div className="flex-1 space-y-1">
          <Label htmlFor="keyword">Search</Label>
          <div className="relative">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input
              id="keyword"
              value={value.keyword}
              onChange={(e) => set({ keyword: e.target.value })}
              placeholder="Title, description, filename, tags, extracted text"
              className="pl-9"
            />
          </div>
        </div>
        <Button variant="ghost" onClick={onClear} className="gap-1">
          <X className="h-4 w-4" /> Clear
        </Button>
      </div>

      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <FilterSelect
          label="Category"
          value={value.category || ALL}
          onValueChange={(v) => set({ category: fromSelect(v) })}
          options={[{ value: ALL, label: "All categories" }, ...EVIDENCE_CATEGORIES.map((c) => ({ value: c, label: CATEGORY_LABELS[c] }))]}
        />
        <FilterSelect
          label="Status"
          value={value.status || ALL}
          onValueChange={(v) => set({ status: fromSelect(v) })}
          options={[{ value: ALL, label: "All statuses" }, ...EVIDENCE_STATUSES.map((s) => ({ value: s, label: STATUS_META[s].label }))]}
        />
        <FilterSelect
          label="File type"
          value={value.fileType || ALL}
          onValueChange={(v) => set({ fileType: fromSelect(v) })}
          options={[{ value: ALL, label: "All types" }, ...FILE_TYPE_OPTIONS.map((f) => ({ value: f, label: f.toUpperCase() }))]}
        />
        <div className="space-y-1">
          <Label htmlFor="programId">Program ID</Label>
          <Input id="programId" value={value.programId} onChange={(e) => set({ programId: e.target.value })} placeholder="UUID" />
        </div>
        <div className="space-y-1">
          <Label htmlFor="academicYearId">Academic Year ID</Label>
          <Input id="academicYearId" value={value.academicYearId} onChange={(e) => set({ academicYearId: e.target.value })} placeholder="UUID" />
        </div>
        <div className="space-y-1">
          <Label htmlFor="criterionId">Criterion ID</Label>
          <Input id="criterionId" value={value.criterionId} onChange={(e) => set({ criterionId: e.target.value })} placeholder="UUID" />
        </div>
        <div className="space-y-1">
          <Label htmlFor="createdFrom">Created from</Label>
          <Input id="createdFrom" type="date" value={value.createdFrom} onChange={(e) => set({ createdFrom: e.target.value })} />
        </div>
        <div className="space-y-1">
          <Label htmlFor="createdTo">Created to</Label>
          <Input id="createdTo" type="date" value={value.createdTo} onChange={(e) => set({ createdTo: e.target.value })} />
        </div>
      </div>
    </div>
  );
}

function FilterSelect({
  label,
  value,
  onValueChange,
  options,
}: {
  label: string;
  value: string;
  onValueChange: (v: string) => void;
  options: { value: string; label: string }[];
}) {
  return (
    <div className="space-y-1">
      <Label>{label}</Label>
      <Select value={value} onValueChange={onValueChange}>
        <SelectTrigger>
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          {options.map((o) => (
            <SelectItem key={o.value} value={o.value}>
              {o.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );
}
