import type { NextConfig } from "next";

// Java API'nin adresi. Bilgisayarda 8080, yayinda Render adresi olacak.
const API_URL = process.env.API_URL ?? "http://localhost:8080";

const nextConfig: NextConfig = {
  // Sitedeki /api/... istekleri Java sunucusuna aktarilir.
  async rewrites() {
    return [{ source: "/api/:path*", destination: `${API_URL}/api/:path*` }];
  },
};

export default nextConfig;