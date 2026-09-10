"use client";

import * as React from "react";
import { FileUp, File as FileIcon, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { ACCEPTED_FILE_EXTENSIONS, FILE_TYPE_OPTIONS, MAX_FILE_SIZE_BYTES } from "@/lib/constants";
import { cn, formatBytes } from "@/lib/utils";

export function validateFile(file: File): string | null {
  const ext = file.name.split(".").pop()?.toLowerCase() ?? "";
  if (!FILE_TYPE_OPTIONS.includes(ext as (typeof FILE_TYPE_OPTIONS)[number])) {
    return `Unsupported file type ".${ext}". Allowed: ${FILE_TYPE_OPTIONS.join(", ")}`;
  }
  if (file.size === 0) {
    return "File is empty";
  }
  if (file.size > MAX_FILE_SIZE_BYTES) {
    return `File exceeds ${formatBytes(MAX_FILE_SIZE_BYTES)}`;
  }
  return null;
}

export function UploadDropzone({
  file,
  onFileChange,
  error,
  disabled,
}: {
  file: File | null;
  onFileChange: (file: File | null) => void;
  error?: string | null;
  disabled?: boolean;
}) {
  const inputRef = React.useRef<HTMLInputElement>(null);
  const [dragActive, setDragActive] = React.useState(false);

  const handleFiles = (files: FileList | null) => {
    if (!files || files.length === 0) return;
    onFileChange(files[0]);
  };

  return (
    <div className="space-y-2">
      {file ? (
        <div className="flex items-center justify-between rounded-lg border bg-card p-4">
          <div className="flex items-center gap-3">
            <span className="flex h-10 w-10 items-center justify-center rounded-md bg-muted">
              <FileIcon className="h-5 w-5 text-muted-foreground" />
            </span>
            <div className="min-w-0">
              <p className="truncate text-sm font-medium">{file.name}</p>
              <p className="text-xs text-muted-foreground">
                {formatBytes(file.size)} · {file.type || "unknown type"}
              </p>
            </div>
          </div>
          <Button
            type="button"
            variant="ghost"
            size="icon"
            onClick={() => onFileChange(null)}
            disabled={disabled}
            aria-label="Remove file"
          >
            <X className="h-4 w-4" />
          </Button>
        </div>
      ) : (
        <div
          role="button"
          tabIndex={0}
          onClick={() => inputRef.current?.click()}
          onKeyDown={(e) => (e.key === "Enter" || e.key === " ") && inputRef.current?.click()}
          onDragOver={(e) => {
            e.preventDefault();
            setDragActive(true);
          }}
          onDragLeave={() => setDragActive(false)}
          onDrop={(e) => {
            e.preventDefault();
            setDragActive(false);
            handleFiles(e.dataTransfer.files);
          }}
          className={cn(
            "flex cursor-pointer flex-col items-center justify-center gap-2 rounded-lg border-2 border-dashed p-10 text-center transition-colors",
            dragActive ? "border-primary bg-accent" : "border-input hover:border-primary/50",
          )}
        >
          <FileUp className="h-8 w-8 text-muted-foreground" />
          <p className="text-sm font-medium">Drag &amp; drop a file here, or click to browse</p>
          <p className="text-xs text-muted-foreground">
            {FILE_TYPE_OPTIONS.join(", ").toUpperCase()} · up to {formatBytes(MAX_FILE_SIZE_BYTES)}
          </p>
        </div>
      )}
      <input
        ref={inputRef}
        type="file"
        accept={ACCEPTED_FILE_EXTENSIONS}
        className="hidden"
        onChange={(e) => handleFiles(e.target.files)}
        disabled={disabled}
      />
      {error ? <p className="text-sm text-destructive">{error}</p> : null}
    </div>
  );
}
