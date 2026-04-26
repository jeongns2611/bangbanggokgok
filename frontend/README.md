This is a [Next.js](https://nextjs.org) project bootstrapped with [`create-next-app`](https://nextjs.org/docs/app/api-reference/cli/create-next-app).

## Getting Started

First, run the development server:

```bash
npm run dev
# or
yarn dev
# or
pnpm dev
# or
bun dev
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

You can start editing the page by modifying `app/page.tsx`. The page auto-updates as you edit the file.

This project uses [`next/font`](https://nextjs.org/docs/app/building-your-application/optimizing/fonts) to automatically optimize and load [Geist](https://vercel.com/font), a new font family for Vercel.

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy on Vercel

The easiest way to deploy your Next.js app is to use the [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme) from the creators of Next.js.

Check out our [Next.js deployment documentation](https://nextjs.org/docs/app/building-your-application/deploying) for more details.

## AI Chat API

- Frontend proxy route: `POST /api/ai/chat`
- Backend target route: `POST /api/v1/ai/chat`

Example request body:

```json
{
	"query": "강남구에서 사회초년생 출퇴근이 편한 곳 찾아줘",
	"sidoName": "서울특별시",
	"sigunguName": "강남구",
	"houseType": ["연립/다세대(빌라)", "오피스텔"],
	"rentType": ["월세", "전세"],
	"minMonthlyCost": 50,
	"maxMonthlyCost": 200,
	"minDeposit": 1000,
	"maxDeposit": 50000,
	"managementCost": 15,
	"minFloorSize": 33.0,
	"maxFloorSize": 84.5,
	"floor": ["지상층"]
}
```

Client-side helper:

```ts
import { requestAiChat } from '@/lib/aiChat';

const result = await requestAiChat({
	query: '강남구에서 사회초년생 출퇴근이 편한 곳 찾아줘',
	sidoName: '서울특별시',
	sigunguName: '강남구',
	houseType: ['연립/다세대(빌라)', '오피스텔'],
	rentType: ['월세', '전세'],
	minMonthlyCost: 50,
	maxMonthlyCost: 200,
	minDeposit: 1000,
	maxDeposit: 50000,
	managementCost: 15,
	minFloorSize: 33,
	maxFloorSize: 84.5,
	floor: ['지상층'],
});

console.log(result);
```
