/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  // Produces a self-contained server bundle for a small Docker runtime image.
  output: "standalone",
  // Lint is optional in this module; type-checking still runs during build.
  eslint: {
    ignoreDuringBuilds: true,
  },
  // NOTE: /api/* is proxied to the backend by a runtime Route Handler
  // (src/app/api/[...path]/route.ts) rather than a build-time rewrite, so
  // BACKEND_INTERNAL_URL is resolved at request time (works in Docker).
};

export default nextConfig;
