import { ApiClientError } from "@/lib/api/client";

/** Renders a friendly, non-technical error box from an ApiClientError (or any Error). */
export function FormError({ error }: { error: unknown }) {
  if (!error) return null;
  let message = "Something went wrong. Please try again.";
  let details: { field: string; message: string }[] | undefined;

  if (error instanceof ApiClientError) {
    message = error.message;
    details = error.details;
  } else if (error instanceof Error) {
    message = error.message;
  }

  return (
    <div className="rounded-md border border-destructive/40 bg-destructive/10 p-3 text-sm text-destructive">
      <p className="font-medium">{message}</p>
      {details && details.length > 0 && (
        <ul className="mt-1 list-inside list-disc">
          {details.map((d) => (
            <li key={d.field}>
              {d.field}: {d.message}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

/** Inline field-level validation message. */
export function FieldError({ message }: { message?: string }) {
  if (!message) return null;
  return <p className="mt-1 text-xs text-destructive">{message}</p>;
}
