'use client';

import { useEffect, useMemo, useRef, useState } from 'react';
import clsx from 'clsx';
import Button from '@/components/common/Button';
import { uploadHouseImages } from '@/lib/houseImages';

import LocationSearchMap from '@/components/common/LocationSearchMap';

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

const BUILDING_TYPE_OPTIONS = [
    '단독/다가구(원룸)',
    '연립/다세대(빌라)',
    '오피스텔',
    '아파트',
];

const RENT_TYPE_OPTIONS = ['전세', '월세'];

const FLOOR_OPTIONS = ['반지하', '1층', '지상층'];

export type UploadedImage = {
    id: number;
    file?: File;
    preview: string;
    
    objectKey?: string;
    isThumbnail?: boolean;
};

export type UploadedImagePayload = {
    objectKey: string;
    isThumbnail: boolean;
};

export type PostFormValues = {
    address: string;
    legalDongName: string;
    buildingType: string;
    rentType: '전세' | '월세' | '';
    area: string;
    builtYear: string;
    floor: string;
    extraNote: string;
    images: UploadedImage[];
    
    uploadedImagePayloads: UploadedImagePayload[];
    deposit: number;
    monthlyCost: number;
    managementCost: number;
    contractStart: string;
    contractEnd: string;
    managementItems: string;
};

type PostFormProps = {
    mode: 'create' | 'edit';
    initialValues?: PostFormValues;
    onSubmit: (values: PostFormValues) => void;
};

const defaultValues: PostFormValues = {
    address: '',
    legalDongName: '',
    buildingType: '',
    rentType: '',
    area: '',
    builtYear: '',
    floor: '',
    extraNote: '',
    images: [],
    uploadedImagePayloads: [],
    deposit: 0,
    monthlyCost: 0,
    managementCost: 0,
    contractStart: '',
    contractEnd: '',
    managementItems: '',
};

function SectionTitle({
    title,
    description,
    required,
}: {
    title: string;
    description?: string;
    required?: boolean;
}) {
    return (
        <div className="mb-4">
            <h3 className="text-[18px] font-bold text-primary-black">{title}{required && <span className="text-red-500"> *</span>}</h3>
            {description && (
                <p className="mt-1 text-sm leading-6 text-black/55">{description}</p>
            )}
        </div>
    );
}

export default function PostForm({
    mode,
    initialValues,
    onSubmit,
}: PostFormProps) {
    const fileInputRef = useRef<HTMLInputElement | null>(null);

    const [address, setAddress] = useState(defaultValues.address);
    const [legalDongName, setLegalDongName] = useState(defaultValues.legalDongName);
    const [buildingType, setBuildingType] = useState(defaultValues.buildingType);
    const [rentType, setRentType] = useState<PostFormValues['rentType']>(
        defaultValues.rentType,
    );
    const [area, setArea] = useState(defaultValues.area);
    const [builtYear, setBuiltYear] = useState(defaultValues.builtYear);
    const [floor, setFloor] = useState(defaultValues.floor);
    const [extraNote, setExtraNote] = useState(defaultValues.extraNote);
    const [images, setImages] = useState<UploadedImage[]>(defaultValues.images);
    const [uploadedPayloads, setUploadedPayloads] = useState<UploadedImagePayload[]>([]);
    const [isUploadingImages, setIsUploadingImages] = useState(false);

    const [deposit, setDeposit] = useState(defaultValues.deposit);
    const [monthlyCost, setMonthlyCost] = useState(defaultValues.monthlyCost);
    const [managementCost, setManagementCost] = useState(defaultValues.managementCost);
    const [contractStart, setContractStart] = useState(defaultValues.contractStart);
    const [contractEnd, setContractEnd] = useState(defaultValues.contractEnd);
    const [managementItems, setManagementItems] = useState(defaultValues.managementItems);
    const [detailedAddress, setDetailedAddress] = useState('');
    const [showDetailedAddressInput, setShowDetailedAddressInput] = useState(false);
    const [verifiedAddress, setVerifiedAddress] = useState('');
    const [isAddressVerified, setIsAddressVerified] = useState(mode === 'edit');
    const [isVerifyingAddress, setIsVerifyingAddress] = useState(false);

    useEffect(() => {
        const source = initialValues ?? defaultValues;

        setAddress(source.address);
        setLegalDongName(source.legalDongName);
        setBuildingType(source.buildingType);
        setRentType(source.rentType);
        setArea(source.area);
        setBuiltYear(source.builtYear);
        setFloor(source.floor);
        setExtraNote(source.extraNote);
        setImages(source.images);
        setUploadedPayloads(source.uploadedImagePayloads ?? []);
        setDeposit(source.deposit);
        setMonthlyCost(source.monthlyCost);
        setManagementCost(source.managementCost);
        setContractStart(source.contractStart);
        setContractEnd(source.contractEnd);
        setManagementItems(source.managementItems);
        setDetailedAddress('');
        setShowDetailedAddressInput(mode === 'edit');
        setVerifiedAddress(source.address);
        setIsAddressVerified(mode === 'edit');
    }, [initialValues]);

    const isMonthlyRent = rentType === '월세';

    const checklist = useMemo(() => {
        const basicInfoDone =
            address.trim() !== '' &&
            buildingType.trim() !== '' &&
            area.trim() !== '' &&
            floor.trim() !== '';

        const priceInfoDone =
            rentType !== '' &&
            deposit > 0 &&
            (isMonthlyRent ? monthlyCost > 0 : true);

        const contractDone = contractStart !== '' && contractEnd !== '';
        
        const photoDone = images.length > 0 && uploadedPayloads.length === images.length;
        const extraDone = extraNote.trim().length > 0;

        return {
            basicInfoDone,
            addressDone: isAddressVerified,
            buildingTypeDone: buildingType.trim() !== '',
            areaDone: area.trim() !== '',
            builtYearDone: builtYear.trim() !== '',
            floorDone: floor.trim() !== '',
            priceInfoDone,
            rentTypeDone: rentType.trim() !== '',
            depositDone: deposit > 0,
            monthlyRentDone: isMonthlyRent ? monthlyCost > 0 : false,
            maintenanceDone: managementCost > 0,
            contractDone,
            photoDone,
            extraDone,
        };
    }, [
        address,
        area,
        builtYear,
        buildingType,
        contractStart,
        contractEnd,
        deposit,
        extraNote,
        floor,
        images.length,
        uploadedPayloads.length,
        isMonthlyRent,
        managementCost,
        monthlyCost,
        rentType,
        isAddressVerified,
    ]);

    const checklistItems = useMemo(() => {
        const items = [
            { label: '매물 기본 정보', checked: checklist.basicInfoDone },
            { label: '매물 주소', checked: checklist.addressDone },
            { label: '건물 유형', checked: checklist.buildingTypeDone },
            { label: '전용 면적', checked: checklist.areaDone },
            { label: '건축년도', checked: checklist.builtYearDone },
            { label: '층수', checked: checklist.floorDone },
            { label: '가격 정보', checked: checklist.priceInfoDone },
            { label: '거래 유형', checked: checklist.rentTypeDone },
            { label: '보증금', checked: checklist.depositDone },
            ...(isMonthlyRent
                ? [{ label: '월세', checked: checklist.monthlyRentDone }]
                : []),
            { label: '관리비', checked: checklist.maintenanceDone },
            { label: '계약 기간', checked: checklist.contractDone },
            { label: '매물 사진', checked: checklist.photoDone },
            { label: '추가 내용', checked: checklist.extraDone },
        ];

        return items;
    }, [checklist, isMonthlyRent]);

    const isSubmitAvailable =
        checklist.basicInfoDone &&
        checklist.priceInfoDone &&
        checklist.contractDone &&
        (mode === 'edit' || isAddressVerified);

    const handleVerifyAddress = async () => {
        if (!address.trim() || mode === 'edit') return;

        const combinedAddress = [address.trim(), detailedAddress.trim()]
            .filter(Boolean)
            .join(' ');

        try {
            setIsVerifyingAddress(true);
            const res = await fetch('/api/houses/address/validate', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ address: combinedAddress }),
            });
            const data = await res.json();
            

            const result = data?.data ?? data;
            if (!result?.valid) {
                setIsAddressVerified(false);
                setVerifiedAddress('');
                alert(result?.userMessage || '유효하지 않은 주소입니다. 다시 확인해 주세요.');
                return;
            }

            setIsAddressVerified(true);
            setVerifiedAddress(combinedAddress);
        } catch (e) {
            
            setIsAddressVerified(false);
            setVerifiedAddress('');
            alert('주소 검증 중 오류가 발생했습니다.');
        } finally {
            setIsVerifyingAddress(false);
        }
    };

    const handleOpenFilePicker = () => {
        fileInputRef.current?.click();
    };

    const handleChangeImages = (e: React.ChangeEvent<HTMLInputElement>) => {
        const files = Array.from(e.target.files || []);
        if (!files.length) return;

        const remainCount = 10 - images.length;
        const slicedFiles = files.slice(0, remainCount);

        const nextImages = slicedFiles.map((file) => ({
            id: Date.now() + Math.random(),
            file,
            preview: URL.createObjectURL(file),
        }));

        setImages((prev) => [...prev, ...nextImages]);
        setUploadedPayloads([]); 
        e.target.value = '';
    };

    const handleDeleteImage = (targetId: number) => {
        setImages((prev) => {
            const target = prev.find((image) => image.id === targetId);
            if (target?.file) {
                URL.revokeObjectURL(target.preview);
            }
            return prev.filter((image) => image.id !== targetId);
        });
        setUploadedPayloads([]); 
    };

    const handleUploadImages = async () => {
        if (isUploadingImages || images.length === 0) return;
        setIsUploadingImages(true);
        try {
            const filesToUpload = images.map((img, index) => ({
                file: img.file!,
                isThumbnail: img.isThumbnail ?? index === 0,
            })).filter((item) => !!item.file);

            if (filesToUpload.length === 0) {
                alert('업로드할 파일이 없습니다.');
                return;
            }

            const payloads = await uploadHouseImages(filesToUpload);
            setUploadedPayloads(payloads);
            alert(`${payloads.length}장의 이미지가 업로드되었습니다.`);
        } catch (err) {
            const message = err instanceof Error ? err.message : '이미지 업로드에 실패했습니다.';
            alert(message);
            setUploadedPayloads([]);
        } finally {
            setIsUploadingImages(false);
        }
    };

    const handleSubmit = () => {
        if (!isSubmitAvailable) return;
        const values: PostFormValues = {
            address: mode === 'create' ? (verifiedAddress || address) : address,
            legalDongName,
            buildingType,
            rentType,
            area,
            builtYear,
            floor,
            extraNote,
            images,
            uploadedImagePayloads: uploadedPayloads,
            deposit,
            monthlyCost: isMonthlyRent ? monthlyCost : 0,
            managementCost,
            contractStart,
            contractEnd,
            managementItems,
        };

        onSubmit(values);
    };

    return (
        <main className="space-y-6">
            <div>
                <h1 className="text-[34px] font-bold tracking-[-0.03em] text-primary-black">
                    {mode === 'edit' ? '실매물 수정하기' : '실매물 등록하기'}
                </h1>
                <p className="mt-2 text-sm leading-6 text-black/55">
                    {mode === 'edit'
                        ? '기존에 등록한 매물 정보를 수정할 수 있습니다.'
                        : '실제 거주 경험이 있거나 확인된 매물을 등록해 주세요.'}
                    <br />
                    등록된 정보는 다른 사용자들의 주거 선택에 도움을 줍니다.
                </p>
            </div>

            <div className="grid grid-cols-1 gap-6 xl:grid-cols-[minmax(0,1fr)_280px]">
                <section className="border border-gray-200 bg-white">
                    <div className="border-b border-gray-100 px-6 py-5">
                        <h2 className="text-lg font-bold text-primary-black">매물 기본 정보</h2>
                    </div>

                    <div className="space-y-10 px-6 py-6">
                        <div className="grid grid-cols-1 gap-8 lg:grid-cols-[140px_minmax(0,1fr)]">
                            <div className="pt-3 text-[15px] font-semibold text-primary-black">
                                매물 기본 정보
                            </div>

                            <div className="space-y-8">
                                <div>
                                    <label className="mb-2 block text-sm font-semibold text-primary-black">
                                        매물 주소<span className="text-red-500"> *</span>
                                    </label>

                                    {mode === 'edit' ? (
                                        <input
                                            type="text"
                                            value={address}
                                            readOnly
                                            className="h-11 w-full cursor-not-allowed border border-gray-200 bg-gray-100 px-3 text-sm text-gray-500 outline-none"
                                        />
                                    ) : (
                                        <LocationSearchMap
                                            placeKeyword={address}
                                            onChangePlaceKeyword={(value) => {
                                                setAddress(value);
                                                setLegalDongName('');
                                                setShowDetailedAddressInput(false);
                                                setDetailedAddress('');
                                                setVerifiedAddress('');
                                                setIsAddressVerified(false);
                                            }}
                                            onSelectLocation={(_, __, ___, addr, jibunAddr) => {
                                                if (!addr) return;
                                                setAddress(addr);

                                                const parts = (jibunAddr || addr).trim().split(/\s+/);
                                                setLegalDongName(parts[2] || '');
                                                setShowDetailedAddressInput(true);
                                                setDetailedAddress('');
                                                setVerifiedAddress('');
                                                setIsAddressVerified(false);
                                            }}
                                            placeholder="예: 서울시 강남구 역삼동 123-4"
                                        />
                                    )}

                                    {showDetailedAddressInput && mode === 'create' && (
                                        <div className="mt-3 rounded-xl border border-gray-200 bg-[#fafafa] p-3">
                                            <label className="mb-2 block text-sm font-semibold text-primary-black">
                                                상세 주소
                                            </label>
                                            <div className="flex flex-col gap-2 md:flex-row">
                                                <input
                                                    type="text"
                                                    value={detailedAddress}
                                                    onChange={(e) => {
                                                        setDetailedAddress(e.target.value);
                                                        setVerifiedAddress('');
                                                        setIsAddressVerified(false);
                                                    }}
                                                    placeholder="예: 101동 1201호"
                                                    className="h-11 w-full border border-gray-200 bg-white px-3 text-sm outline-none focus:border-background-300"
                                                />
                                                <Button
                                                    label={isVerifyingAddress ? '확인 중...' : '주소 확인'}
                                                    size="sm"
                                                    onClick={handleVerifyAddress}
                                                    disabled={isVerifyingAddress || !address.trim()}
                                                    className="shrink-0 rounded-md px-4"
                                                />
                                            </div>
                                            {isAddressVerified && (
                                                <p className="mt-2 text-sm font-medium text-green-600">
                                                    ✓ 주소 확인이 완료되었습니다.
                                                </p>
                                            )}
                                        </div>
                                    )}
                                </div>

                                <div>
                                    <label className="mb-3 block text-sm font-semibold text-primary-black">
                                        건물 유형<span className="text-red-500"> *</span>
                                    </label>
                                    <div className="flex flex-wrap gap-3">
                                        {BUILDING_TYPE_OPTIONS.map((option) => (
                                            <Button
                                                key={option}
                                                label={option}
                                                size="sm"
                                                isActive={buildingType === option}
                                                onClick={mode === 'edit' ? undefined : () => setBuildingType(option)}
                                                disabled={mode === 'edit'}
                                                className="rounded-md px-4"
                                            />
                                        ))}
                                    </div>
                                </div>

                                <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
                                    <div>
                                        <label className="mb-2 block text-sm font-semibold text-primary-black">
                                            전용 면적<span className="text-red-500"> *</span>
                                        </label>
                                        <div className="flex items-center gap-2">
                                            <input
                                                type="number"
                                                value={area}
                                                onChange={(e) => setArea(e.target.value)}
                                                readOnly={mode === 'edit'}
                                                placeholder="예: 18"
                                                className={clsx(
                                                    'h-11 w-full border border-gray-200 px-3 text-sm outline-none',
                                                    mode === 'edit'
                                                        ? 'cursor-not-allowed bg-gray-100 text-gray-500'
                                                        : 'bg-[#fafafa] focus:border-background-300 focus:bg-white',
                                                )}
                                            />
                                            <span className="shrink-0 text-sm text-black/55">㎡</span>
                                        </div>
                                    </div>

                                    <div>
                                        <label className="mb-2 block text-sm font-semibold text-primary-black">
                                            건축년도
                                        </label>
                                        <input
                                            type="number"
                                            value={builtYear}
                                            onChange={(e) => setBuiltYear(e.target.value)}
                                            readOnly={mode === 'edit'}
                                            placeholder="예: 2018"
                                            className={clsx(
                                                'h-11 w-full border border-gray-200 px-3 text-sm outline-none',
                                                mode === 'edit'
                                                    ? 'cursor-not-allowed bg-gray-100 text-gray-500'
                                                    : 'bg-[#fafafa] focus:border-background-300 focus:bg-white',
                                            )}
                                        />
                                    </div>
                                </div>

                                <div className="max-w-[320px]">
                                    <label className="mb-3 block text-sm font-semibold text-primary-black">
                                        층수<span className="text-red-500"> *</span>
                                    </label>
                                    <div className="flex flex-wrap gap-3">
                                        {FLOOR_OPTIONS.map((option) => (
                                            <Button
                                                key={option}
                                                label={option}
                                                size="sm"
                                                isActive={floor === option}
                                                onClick={mode === 'edit' ? undefined : () => setFloor(option)}
                                                disabled={mode === 'edit'}
                                                className="rounded-md px-4"
                                            />
                                        ))}
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="grid grid-cols-1 gap-8 lg:grid-cols-[140px_minmax(0,1fr)]">
                            <div className="pt-1 text-[15px] font-semibold text-primary-black">
                                가격 정보
                            </div>

                            <div className="space-y-8">
                                <div>
                                    <label className="mb-3 block text-sm font-semibold text-primary-black">
                                        거래 유형<span className="text-red-500"> *</span>
                                    </label>
                                    <div className="flex flex-wrap gap-3">
                                        {RENT_TYPE_OPTIONS.map((option) => (
                                            <Button
                                                key={option}
                                                label={option}
                                                size="sm"
                                                isActive={rentType === option}
                                                onClick={() => setRentType(option as '전세' | '월세')}
                                                className="rounded-md px-6"
                                            />
                                        ))}
                                    </div>
                                </div>

                                {rentType === '' ? (
                                    <div className="rounded-lg border border-dashed border-gray-200 bg-gray-50 px-4 py-6 text-sm text-gray-500">
                                        거래 유형을 먼저 선택해 주세요.
                                    </div>
                                ) : (
                                    <>
                                        <div
                                            className={clsx(
                                                'grid grid-cols-1 gap-8',
                                                isMonthlyRent && 'md:grid-cols-2',
                                            )}
                                        >
                                            <div>
                                                <label className="mb-2 block text-sm font-semibold text-primary-black">
                                                    보증금<span className="text-red-500"> *</span>
                                                </label>
                                                <div className="flex items-center gap-2">
                                                    <input
                                                        type="number"
                                                        value={deposit || ''}
                                                        onChange={(e) => setDeposit(Number(e.target.value) || 0)}
                                                        placeholder="예: 3000"
                                                        className="h-11 w-full border border-gray-200 bg-[#fafafa] px-3 text-sm outline-none focus:border-background-300 focus:bg-white"
                                                    />
                                                    <span className="shrink-0 text-sm text-black/55">만원</span>
                                                </div>
                                            </div>

                                            {isMonthlyRent && (
                                                <div>
                                                    <label className="mb-2 block text-sm font-semibold text-primary-black">
                                                        월세<span className="text-red-500"> *</span>
                                                    </label>
                                                    <div className="flex items-center gap-2">
                                                        <input
                                                            type="number"
                                                            value={monthlyCost || ''}
                                                            onChange={(e) => setMonthlyCost(Number(e.target.value) || 0)}
                                                            placeholder="예: 70"
                                                            className="h-11 w-full border border-gray-200 bg-[#fafafa] px-3 text-sm outline-none focus:border-background-300 focus:bg-white"
                                                        />
                                                        <span className="shrink-0 text-sm text-black/55">만원</span>
                                                    </div>
                                                </div>
                                            )}
                                        </div>

                                        <div className="max-w-[360px]">
                                            <label className="mb-2 block text-sm font-semibold text-primary-black">
                                                관리비
                                            </label>
                                            <div className="flex items-center gap-2">
                                                <input
                                                    type="number"
                                                    value={managementCost || ''}
                                                    onChange={(e) => setManagementCost(Number(e.target.value) || 0)}
                                                    placeholder="예: 5"
                                                    className="h-11 w-full border border-gray-200 bg-[#fafafa] px-3 text-sm outline-none focus:border-background-300 focus:bg-white"
                                                />
                                                <span className="shrink-0 text-sm text-black/55">만원</span>
                                            </div>
                                        </div>

                                        <div>
                                            <label className="mb-2 block text-sm font-semibold text-primary-black">
                                                관리비 항목
                                            </label>
                                            <input
                                                type="text"
                                                value={managementItems}
                                                onChange={(e) => setManagementItems(e.target.value)}
                                                placeholder="예: 수도, 전기, 인터넷"
                                                className="h-11 w-full border border-gray-200 bg-[#fafafa] px-3 text-sm outline-none focus:border-background-300 focus:bg-white"
                                            />
                                        </div>
                                    </>
                                )}
                            </div>
                        </div>

                        <div className="grid grid-cols-1 gap-8 lg:grid-cols-[140px_minmax(0,1fr)]">
                            <div className="pt-1 text-[15px] font-semibold text-primary-black">
                                계약 기간
                            </div>

                            <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
                                <div>
                                    <label className="mb-2 block text-sm font-semibold text-primary-black">
                                        계약 시작<span className="text-red-500"> *</span>
                                    </label>
                                    <input
                                        type="month"
                                        value={contractStart}
                                        onChange={(e) => setContractStart(e.target.value)}
                                        className="h-11 w-full border border-gray-200 bg-[#fafafa] px-3 text-sm outline-none focus:border-background-300 focus:bg-white"
                                    />
                                </div>
                                <div>
                                    <label className="mb-2 block text-sm font-semibold text-primary-black">
                                        계약 종료<span className="text-red-500"> *</span>
                                    </label>
                                    <input
                                        type="month"
                                        value={contractEnd}
                                        onChange={(e) => setContractEnd(e.target.value)}
                                        className="h-11 w-full border border-gray-200 bg-[#fafafa] px-3 text-sm outline-none focus:border-background-300 focus:bg-white"
                                    />
                                </div>
                            </div>
                        </div>

                        <div className="grid grid-cols-1 gap-8 lg:grid-cols-[140px_minmax(0,1fr)]">
                            <div className="pt-1 text-[15px] font-semibold text-primary-black">
                                매물 사진
                            </div>

                            <div>
                                <SectionTitle
                                    title="매물 사진"
                                    description="매물의 실제 모습을 확인할 수 있는 사진을 등록해 주세요. 최대 10장까지 등록 가능합니다."
                                />

                                <input
                                    ref={fileInputRef}
                                    type="file"
                                    accept="image/*"
                                    multiple
                                    onChange={handleChangeImages}
                                    className="hidden"
                                />

                                <div className="rounded-xl bg-[#f7f8f7] p-4">
                                    <div className="flex flex-wrap gap-4">
                                        <button
                                            type="button"
                                            onClick={handleOpenFilePicker}
                                            disabled={isUploadingImages}
                                            className="flex h-[108px] w-[124px] flex-col items-center justify-center rounded-lg border border-dashed border-gray-300 bg-white text-sm font-medium text-gray-500 transition hover:border-background-300 hover:text-primary-200 disabled:opacity-50"
                                        >
                                            <span className="text-2xl leading-none">＋</span>
                                            <span className="mt-1">사진 추가</span>
                                        </button>

                                        {images.map((image, index) => (
                                            <div
                                                key={image.id}
                                                className="relative h-[108px] w-[124px] overflow-hidden rounded-lg bg-[#e9edf0]"
                                            >
                                                {index === 0 && (
                                                    <span className="absolute left-2 top-2 z-10 rounded-full bg-background-200 px-2 py-0.5 text-[10px] font-semibold text-primary-300">
                                                        썸네일
                                                    </span>
                                                )}

                                                {uploadedPayloads.length > 0 && (
                                                    <span className="absolute bottom-1 right-1 z-10 rounded-full bg-green-500 px-1.5 py-0.5 text-[9px] font-bold text-white">
                                                        ✓
                                                    </span>
                                                )}

                                                <img
                                                    src={image.preview}
                                                    alt={`업로드 이미지 ${index + 1}`}
                                                    className="h-full w-full object-cover"
                                                />

                                                <button
                                                    type="button"
                                                    onClick={() => handleDeleteImage(image.id)}
                                                    disabled={isUploadingImages}
                                                    className="absolute right-2 top-2 z-10 flex h-6 w-6 items-center justify-center rounded-full bg-black/55 text-xs text-white disabled:opacity-50"
                                                >
                                                    ✕
                                                </button>
                                            </div>
                                        ))}
                                    </div>

                                    <div className="mt-4 flex items-center justify-between">
                                        <p className="text-xs text-black/45">
                                            {images.length}/10장 선택됨
                                            {uploadedPayloads.length > 0 && (
                                                <span className="ml-2 font-semibold text-green-600">
                                                    ({uploadedPayloads.length}장 업로드 완료)
                                                </span>
                                            )}
                                        </p>

                                        {images.length > 0 && uploadedPayloads.length !== images.length && (
                                            <Button
                                                label={isUploadingImages ? '업로드 중...' : '이미지 업로드'}
                                                size="sm"
                                                onClick={handleUploadImages}
                                                disabled={isUploadingImages}
                                                className="rounded-md px-4"
                                            />
                                        )}
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="grid grid-cols-1 gap-8 lg:grid-cols-[140px_minmax(0,1fr)]">
                            <div className="pt-1 text-[15px] font-semibold text-primary-black">
                                추가 내용
                            </div>

                            <div>
                                <SectionTitle
                                    title="추가 내용"
                                    description="실제 거주 기간이나 집 장단점 등을 작성해 주세요."
                                />

                                <textarea
                                    value={extraNote}
                                    onChange={(e) => setExtraNote(e.target.value)}
                                    placeholder="예: 채광이 좋고 역과 가까워서 출퇴근이 편했습니다."
                                    className="min-h-[140px] w-full resize-none border border-gray-200 bg-[#fafafa] px-4 py-3 text-sm outline-none focus:border-background-300 focus:bg-white"
                                />
                            </div>
                        </div>
                    </div>
                </section>

                <aside className="h-fit border border-gray-200 bg-white xl:sticky xl:top-24">
                    <div className="px-5 py-5">
                        <h2 className="text-[28px] font-bold tracking-[-0.03em] text-primary-black">
                            등록 항목
                        </h2>

                        <div className="mt-5 space-y-3 border-t border-gray-100 pt-4">
                            {checklistItems.map((item) => (
                                <div
                                    key={item.label}
                                    className="flex items-center justify-between gap-3 text-sm"
                                >
                                    <span
                                        className={clsx(
                                            'font-medium',
                                            item.checked ? 'text-primary-300' : 'text-black/70',
                                        )}
                                    >
                                        {item.label}
                                    </span>

                                    {item.checked && (
                                        <span className="flex h-5 w-5 items-center justify-center rounded-full bg-background-100 text-[11px] font-bold text-primary-200">
                                            ✓
                                        </span>
                                    )}
                                </div>
                            ))}
                        </div>

                        <Button
                            label={mode === 'edit' ? '수정하기' : '등록하기'}
                            type="button"
                            size="md"
                            fullWidth
                            isActive={isSubmitAvailable}
                            disabled={!isSubmitAvailable}
                            onClick={handleSubmit}
                            className="mt-8 rounded-lg text-base font-bold"
                        />
                    </div>
                </aside>
            </div>
        </main>
    );
}