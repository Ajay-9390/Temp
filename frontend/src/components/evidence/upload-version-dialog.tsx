"use client";

import * as React from "react";
import { Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { UploadDropzone, validateFile } from "@/components/evidence/upload-dropzone";
import { useUploadVersion } from "@/hooks/use-evidence";

export function UploadVersionDialog({ evidenceId }: { evidenceId: string }) {
  const upload = useUploadVersion(evidenceId);
  const [open, setOpen] = React.useState(false);
  const [file, setFile] = React.useState<File | null>(null);
  const [changeReason, setChangeReason] = React.useState("");
  const [error, setError] = React.useState<string | null>(null);

  const submit = async () => {
    if (!file) {
      setError("Please choose a file");
      return;
    }
    const v = validateFile(file);
    if (v) {
      setError(v);
      return;
    }
    const fd = new FormData();
    fd.append("file", file);
    if (changeReason.trim()) fd.append("changeReason", changeReason.trim());
    await upload.mutateAsync(fd);
    setOpen(false);
    setFile(null);
    setChangeReason("");
    setError(null);
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="outline">
          <Plus className="h-4 w-4" /> New Version
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Upload New Version</DialogTitle>
          <DialogDescription>
            Uploading a new file creates a new version. Previous versions are retained for audit.
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-3">
          <UploadDropzone file={file} onFileChange={(f) => { setFile(f); setError(null); }} error={error} disabled={upload.isPending} />
          <div className="space-y-1">
            <Label htmlFor="changeReason">Change reason</Label>
            <Textarea
              id="changeReason"
              rows={3}
              placeholder="What changed in this version?"
              value={changeReason}
              onChange={(e) => setChangeReason(e.target.value)}
              disabled={upload.isPending}
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => setOpen(false)} disabled={upload.isPending}>
            Cancel
          </Button>
          <Button onClick={submit} disabled={upload.isPending}>
            {upload.isPending ? "Uploading…" : "Upload"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
