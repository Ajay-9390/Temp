// Keycloak-ready token accessor. Standalone dev needs no token (backend runs with
// app.security.enabled=false). When the central Auth module is integrated, replace
// getAccessToken() with the real Keycloak adapter (e.g. keycloak-js) — the API client and
// all hooks already send Authorization: Bearer <token> when a token is present.

const TOKEN_KEY = "nba_access_token";

export function getAccessToken(): string | null {
  if (typeof window === "undefined") return null;
  return window.localStorage.getItem(TOKEN_KEY) ?? process.env.NEXT_PUBLIC_DEV_TOKEN ?? null;
}

export function setAccessToken(token: string) {
  if (typeof window !== "undefined") window.localStorage.setItem(TOKEN_KEY, token);
}

export function clearAccessToken() {
  if (typeof window !== "undefined") window.localStorage.removeItem(TOKEN_KEY);
}
