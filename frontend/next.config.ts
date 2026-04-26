import type { NextConfig } from 'next';

const nextConfig: NextConfig = {
  output: 'standalone',
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'dbeeghg6i9hk3.cloudfront.net',
      },
      {
        protocol: 'https',
        hostname: 'ssafy-banggok.s3.ap-northeast-2.amazonaws.com',
      },
    ],
  },
};

export default nextConfig;
