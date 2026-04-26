'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import PostForm, { PostFormValues } from '@/components/features/mypage/Post';
import { fetchWithAuth } from '@/lib/fetchWithAuth';

function yearMonthToMonth(ym: number | null | undefined): string {
    if (!ym) return '';
    const s = String(ym);
    return `${s.slice(0, 4)}-${s.slice(4, 6)}`;
}

function monthToYearMonth(monthStr: string): number {
    return Number(monthStr.replace('-', ''));
}

export default function PostEditPage() {
    const router = useRouter();
    const params = useParams();
    const [initialValues, setInitialValues] = useState<PostFormValues | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        const houseId = params.id;
        if (!houseId) return;

        (async () => {
            try {
                const res = await fetch(`/api/houses/${houseId}`);
                if (!res.ok) {
                    alert('매물 정보를 불러오지 못했습니다.');
                    return;
                }
                const json = await res.json();
                
                const d = json.data ?? json;

                setInitialValues({
                    address: d.address || '',
                    legalDongName: d.dongName || '',
                    buildingType: d.houseType || '',
                    rentType: d.rentType || '',
                    area: d.floorSize != null ? String(d.floorSize) : '',
                    builtYear: d.buildYear != null ? String(d.buildYear) : '',
                    floor: d.floor || '',
                    extraNote: d.description || '',
                    images: (d.images || []).map((img: { image_url?: string; imageUrl?: string; is_thumbnail?: boolean; isThumbnail?: boolean }, idx: number) => ({
                        id: idx + 1,
                        preview: img.image_url || img.imageUrl || '',
                    })),
                    uploadedImagePayloads: (d.images || []).map((img: { object_key?: string; objectKey?: string; is_thumbnail?: boolean; isThumbnail?: boolean }) => ({
                        objectKey: img.object_key || img.objectKey || '',
                        isThumbnail: Boolean(img.is_thumbnail ?? img.isThumbnail),
                    })).filter((img: { objectKey: string; isThumbnail: boolean }) => img.objectKey !== ''),
                    deposit: d.deposit ?? 0,
                    monthlyCost: d.monthlyCost ?? 0,
                    managementCost: d.managementCost ?? 0,
                    contractStart: yearMonthToMonth(d.contractStartYearMonth ?? d.contractStart),
                    contractEnd: yearMonthToMonth(d.contractEndYearMonth ?? d.contractEnd),
                    managementItems: d.managementItems || '',
                });
            } catch {
                alert('네트워크 오류가 발생했습니다.');
            }
        })();
    }, [params.id]);

    const handleUpdate = async (values: PostFormValues) => {
        if (isSubmitting) return;
        setIsSubmitting(true);

        const houseId = params.id;
        const isMonthlyRent = values.rentType === '월세';

        const body = {
            rentType: values.rentType,
            deposit: values.deposit,
            monthlyCost: isMonthlyRent ? values.monthlyCost : 0,
            managementCost: values.managementCost || null,
            managementItems: values.managementItems || null,
            contractStartYearMonth: monthToYearMonth(values.contractStart),
            contractEndYearMonth: monthToYearMonth(values.contractEnd),
            description: values.extraNote || null,
            sidoName: values.address.trim().split(/\s+/)[0] || '',
            sigunguName: values.address.trim().split(/\s+/)[1] || '',
            dongName: values.legalDongName || values.address.trim().split(/\s+/)[2] || '',
            houseType: values.buildingType,
            floor: values.floor,
            address: values.address,
            floorSize: parseFloat(values.area) || 0,
            buildYear: values.builtYear ? parseInt(values.builtYear, 10) : null,
        };

        try {
            
            const res = await fetchWithAuth(`/api/houses/${houseId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(body),
            });

            const resData = await res.json().catch(() => null);
            

            if (!res.ok) {
                alert(resData?.message || '매물 수정에 실패했습니다.');
                return;
            }

            alert('매물이 수정되었습니다.');
            router.push('/mypage/posts');
        } catch {
            alert('네트워크 오류가 발생했습니다.');
        } finally {
            setIsSubmitting(false);
        }
    };

    if (!initialValues) {
        return (
            <div className="py-20 text-center text-sm text-gray-500">
                데이터를 불러오는 중입니다.
            </div>
        );
    }

    return (
        <PostForm
            mode="edit"
            initialValues={initialValues}
            onSubmit={handleUpdate}
        />
    );
}