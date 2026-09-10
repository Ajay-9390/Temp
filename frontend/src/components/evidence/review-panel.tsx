"use client";

import * as React from "react";
import { Check, MessageSquareWarning, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { useReviewActions } from "@/hooks/use-evidence";
import type { EvidenceStatus } from "@/lib/types";

export function ReviewPanel({
  evidenceId,
  status,
  onReviewed,
}: {
  evidenceId: string;
  status: EvidenceStatus;
  onReviewed?: () => void;
}) {
  const { approve, reject, requestChanges } = useReviewActions(evidenceId);
  const [reviewer, setReviewer] = React.useState("");
  const [comments, setComments] = React.useState("");
  const [error, setError] = React.useState<string | null>(null);

  const canReview = status === "UNDER_REVIEW";
  const busy = approve.isPending || reject.isPending || requestChanges.isPending;

  const run = async (kind: "approve" | "reject" | "requestChanges") => {
    setError(null);
    if ((kind === "reject" || kind === "requestChanges") && !comments.trim()) {
      setError("Comments are required to reject or request changes.");
      return;
    }
    const body = { reviewer: reviewer.trim() || undefined, comments: comments.trim() || undefined };
    const mutation = kind === "approve" ? approve : kind === "reject" ? reject : requestChanges;
    await mutation.mutateAsync(body);
    setComments("");
    onReviewed?.();
  };

  return (
    <div className="space-y-4">
      {!canReview ? (
        <div className="rounded-md border border-amber-200 bg-amber-50 p-3 text-sm text-amber-800">
          Review actions are available only when the evidence is <strong>Under Review</strong>. Current status:{" "}
          {status}.
        </div>
      ) : null}

      <div className="space-y-1">
        <Label htmlFor="reviewer">Reviewer (optional)</Label>
        <Input
          id="reviewer"
          placeholder="e.g. dr-sharma"
          value={reviewer}
          onChange={(e) => setReviewer(e.target.value)}
          disabled={!canReview || busy}
        />
      </div>

      <div className="space-y-1">
        <Label htmlFor="comments">Comments</Label>
        <Textarea
          id="comments"
          rows={5}
          placeholder="Required when rejecting or requesting changes"
          value={comments}
          onChange={(e) => setComments(e.target.value)}
          disabled={!canReview || busy}
        />
        {error ? <p className="text-sm text-destructive">{error}</p> : null}
      </div>

      <div className="flex flex-wrap gap-2">
        <Button onClick={() => run("approve")} disabled={!canReview || busy} className="bg-emerald-600 hover:bg-emerald-700">
          <Check className="h-4 w-4" /> Approve
        </Button>
        <Button
          variant="outline"
          onClick={() => run("requestChanges")}
          disabled={!canReview || busy}
          className="border-orange-300 text-orange-700 hover:bg-orange-50"
        >
          <MessageSquareWarning className="h-4 w-4" /> Request Changes
        </Button>
        <Button variant="destructive" onClick={() => run("reject")} disabled={!canReview || busy}>
          <X className="h-4 w-4" /> Reject
        </Button>
      </div>
    </div>
  );
}
