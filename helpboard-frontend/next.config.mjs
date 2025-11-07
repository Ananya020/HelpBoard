/** @type {import('next').NextConfig} */
const nextConfig = {
  eslint: {
    ignoreDuringBuilds: true,
  },
  typescript: {
    ignoreBuildErrors: true,
  },
  images: {
    remotePatterns: [
      {
        protocol: "https",
        hostname: "**",
      },
    ],
    unoptimized: true,
  },

  // ✅ Add rewrites for backend proxy
  async rewrites() {
    return [
      {
        source: "/ws/:path*", // Proxy WebSocket/SockJS requests
        destination: "http://localhost:8080/ws/:path*",
      },
      {
        source: "/api/:path*", // Proxy REST API requests if needed
        destination: "http://localhost:8080/api/:path*",
      },
    ];
  },
};

export default nextConfig;
