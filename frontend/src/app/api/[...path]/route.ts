import { NextRequest } from "next/server";

/**
 * Runtime reverse proxy for the Evidence Management backend.
 *
 * The browser calls relative `/api/v1/evidences/...` URLs; this handler forwards them to the
 * backend resolved from `BACKEND_INTERNAL_URL` AT REQUEST TIME (so it works in Docker where
 * the backend is reachable at http://backend:8080, and locally at http://localhost:8080).
 * Streaming is preserved for uploads and file downloads/previews; redirects (e.g. Supabase
 * signed URLs) are passed through to the browser.
 */
export const dynamic = "force-dynamic";

function backendBaseUrl(): string {
  return process.env.BACKEND_INTERNAL_URL || "http://localhost:8080";
}

async function proxy(req: NextRequest, ctx: { params: { path?: string[] } }): Promise<Response> {
  const segments = ctx.params.path ?? [];
  const target = `${backendBaseUrl()}/api/${segments.join("/")}${req.nextUrl.search}`;

  const headers = new Headers(req.headers);
  headers.delete("host");
  headers.delete("connection");
  headers.delete("content-length");

  const method = req.method.toUpperCase();
  const hasBody = method !== "GET" && method !== "HEAD";

  const init: RequestInit & { duplex?: "half" } = {
    method,
    headers,
    redirect: "manual",
  };
  if (hasBody) {
    init.body = req.body;
    init.duplex = "half";
  }

  let backendResponse: Response;
  try {
    backendResponse = await fetch(target, init);
  } catch (error) {
    return Response.json(
      {
        status: 502,
        error: "Bad Gateway",
        code: "BACKEND_UNREACHABLE",
        message: `Cannot reach backend at ${backendBaseUrl()}: ${(error as Error).message}`,
        path: req.nextUrl.pathname,
        fieldErrors: [],
      },
      { status: 502 },
    );
  }

  const responseHeaders = new Headers(backendResponse.headers);
  responseHeaders.delete("content-encoding");
  responseHeaders.delete("transfer-encoding");
  responseHeaders.delete("connection");

  return new Response(backendResponse.body, {
    status: backendResponse.status,
    statusText: backendResponse.statusText,
    headers: responseHeaders,
  });
}

export {
  proxy as GET,
  proxy as POST,
  proxy as PUT,
  proxy as DELETE,
  proxy as PATCH,
  proxy as HEAD,
};
