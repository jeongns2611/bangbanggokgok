'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import PostForm, { PostFormValues } from '@/components/features/mypage/Post';
import { fetchWithAuth } from '@/lib/fetchWithAuth';

const SIDO_FULL_NAME: Record<string, string> = {
    '서울': '서울특별시',
    '부산': '부산광역시',
    '대구': '대구광역시',
    '인천': '인천광역시',
    '광주': '광주광역시',
    '대전': '대전광역시',
    '울산': '울산광역시',
    '세종': '세종특별자치시',
    '경기': '경기도',
    '강원': '강원도', '강원특별자치도': '강원특별자치도',
    '충북': '충청북도',
    '충남': '충청남도',
    '전북': '전라북도', '전북특별자치도': '전북특별자치도',
    '전남': '전라남도',
    '경북': '경상북도',
    '경남': '경상남도',
    '제주': '제주특별자치도',
};

function toFullSidoName(short: string): string {
    return SIDO_FULL_NAME[short] || short;
}

function parseAddress(address: string) {
    const parts = address.trim().split(/\s+/);
    return {
        sidoName: toFullSidoName(parts[0] || ''),
        sigunguName: parts[1] || '',
        dongName: parts[2] || '',
    };
}

function monthToYearMonth(monthStr: string): number {
    
    return Number(monthStr.replace('-', ''));
}

export default function PostCreatePage() {
    const router = useRouter();
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleCreate = async (values: PostFormValues) => {
        if (isSubmitting) return;
        setIsSubmitting(true);

        const { sidoName, sigunguName, dongName: parsedDongName } = parseAddress(values.address);
        const dongName = values.legalDongName || parsedDongName;

        try {
            
            const imagePayloads = values.uploadedImagePayloads;

            
            const body = {
                images: imagePayloads,
                sidoName,
                sigunguName,
                dongName,
                houseType: values.buildingType,
                rentType: values.rentType,
                floor: values.floor,
                address: values.address,
                deposit: values.deposit,
                monthlyCost: values.monthlyCost,
                managementCost: values.managementCost || null,
                managementItems: values.managementItems || null,
                floorSize: parseFloat(values.area) || 0,
                contractStartYearMonth: monthToYearMonth(values.contractStart),
                contractEndYearMonth: monthToYearMonth(values.contractEnd),
                buildYear: values.builtYear ? parseInt(values.builtYear, 10) : null,
                description: values.extraNote || null,
            };

            
            
            const res = await fetchWithAuth('/api/houses', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(body),
            });

            const resData = await res.json().catch(() => null);
            

            if (!res.ok) {
                alert(resData?.message || '매물 등록에 실패했습니다.');
                return;
            }

            alert('매물이 등록되었습니다.');
            router.push('/mypage/posts');
        } catch (err) {
            const message = err instanceof Error ? err.message : '오류가 발생했습니다.';
            alert(message);
        } finally {
            setIsSubmitting(false);
        }
    };

    return <PostForm mode="create" onSubmit={handleCreate} />;
}