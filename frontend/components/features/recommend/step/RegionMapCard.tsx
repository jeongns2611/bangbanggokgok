'use client';

import SeoulDistrictMap from './SeoulDistrictMap';

type MetricTab = '편의시설' | '통근시간' | '평균가격' | '매물수' | '식비지수';

type RegionMapCardProps = {
    selectedMetric: MetricTab | null;
    onClickRegion: (sigunguName: string) => void;
    districtMetrics?: {
        district: string;
        value: number | string;
        rank?: 1 | 2 | 3;
    }[];
};

export default function RegionMapCard({
    selectedMetric,
    onClickRegion,
    districtMetrics = [],
}: RegionMapCardProps) {
    return (
        <SeoulDistrictMap
            selectedMetric={selectedMetric}
            districtMetrics={districtMetrics}
            onSelectDistrict={(district) => {
                onClickRegion(district);
            }}
        />
    );
}