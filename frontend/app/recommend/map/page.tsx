'use client';

import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { HiChevronLeft, HiChevronRight } from 'react-icons/hi';
import HouseInfraFloatingMenu, {
  type InfraCategory,
} from '@/components/features/search/HouseInfraFloatingMenu';
import MapLayerToggle from '@/components/features/search/MapLayerToggle';
import RankedHouseCard from '@/components/features/recommend/RankedHouseCard';
import RecommendConditionPanel from '@/components/features/recommend/RecommendConditionPanel';
import RecommendHouseDetailPanel from '@/components/features/recommend/RecommendHouseDetailPanel';
import type { RecommendConditionItem } from '@/mock/recommendConditions';
import type { HouseListItem } from '@/mock/houses';
import { fetchWithAuth } from '@/lib/fetchWithAuth';
import { fetchHouseInfraMarkers } from '@/lib/houseInfrastructure';
import { getInfraMarkerHtml } from '@/lib/infraMarkerIcon';

declare global {
  interface Window {
    kakao?: any;
    __handlePinClick: (houseId: number) => void;
  }
}

const PANEL_HEIGHT_CLASS = 'h-[calc(100vh-73px)]';
const DEFAULT_FALLBACK_LAT = 37.4979;
const DEFAULT_FALLBACK_LNG = 127.0276;

type InitialLoadingPhase =
  | 'analyzing'
  | 'checking'
  | 'composing'
  | 'waiting';

type RecommendTop10ApiResponse = {
  recommendations?: unknown;
  nextHouseId?: unknown;
  hasNext?: unknown;
};

const toNumberOr = (value: unknown, fallback: number) => {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : fallback;
};

const toBooleanOr = (value: unknown, fallback: boolean) => {
  if (typeof value === 'boolean') {
    return value;
  }

  if (typeof value === 'string') {
    const normalized = value.trim().toLowerCase();
    if (normalized === 'true') {
      return true;
    }
    if (normalized === 'false') {
      return false;
    }
  }

  return fallback;
};

const normalizeHouseStatus = (status: unknown) => {
  if (typeof status !== 'string') {
    return '거래 가능';
  }

  if (status.includes('가능')) {
    return '거래 가능';
  }

  if (status.includes('완료')) {
    return '거래 완료';
  }

  return status;
};

const mapRecommendationToHouse = (
  item: unknown,
  fallbackIndex: number,
): HouseListItem | null => {
  if (typeof item !== 'object' || item === null) {
    return null;
  }

  const raw = item as Record<string, unknown>;
  const houseId = Number(raw.houseId);
  if (Number.isNaN(houseId)) {
    return null;
  }

  const images = Array.isArray(raw.images)
    ? raw.images
      .filter(
        (image): image is Record<string, unknown> =>
          typeof image === 'object' && image !== null,
      )
      .map((image) => ({
        image_url:
          typeof image.image_url === 'string' ? image.image_url : undefined,
        is_thumbnail:
          typeof image.is_thumbnail === 'boolean'
            ? image.is_thumbnail
            : undefined,
      }))
    : [];

  const latitude =
    toNumberOr(raw.lat, NaN) ||
    toNumberOr(raw.latitude, NaN) ||
    toNumberOr(raw.y, NaN);
  const longitude =
    toNumberOr(raw.lng, NaN) ||
    toNumberOr(raw.longitude, NaN) ||
    toNumberOr(raw.x, NaN);

  const fallbackLat = DEFAULT_FALLBACK_LAT + fallbackIndex * 0.00018;
  const fallbackLng = DEFAULT_FALLBACK_LNG + fallbackIndex * 0.00018;
  const aiMessage =
    typeof raw.aiMessage === 'string'
      ? raw.aiMessage
      : typeof raw.ai_message === 'string'
        ? raw.ai_message
        : null;

  return {
    houseId,
    aiMessage,
    houseType: typeof raw.houseType === 'string' ? raw.houseType : '-',
    rentType: typeof raw.rentType === 'string' ? raw.rentType : '-',
    deposit: toNumberOr(raw.deposit, 0),
    monthlyCost: toNumberOr(raw.monthlyCost, 0),
    floor: typeof raw.floor === 'string' ? raw.floor : '-',
    floorSize: toNumberOr(raw.floorSize, 0),
    dong: typeof raw.dong === 'string' ? raw.dong : '-',
    houseStatus: normalizeHouseStatus(raw.houseStatus),
    isLiked: toBooleanOr(raw.isLiked, false),
    images,
    lat: Number.isFinite(latitude) ? latitude : fallbackLat,
    lng: Number.isFinite(longitude) ? longitude : fallbackLng,
  };
};

export default function RecommendMapPage() {
  const [preferredConditionId, setPreferredConditionId] = useState<number | null>(
    null,
  );
  const mapRef = useRef<HTMLDivElement | null>(null);
  const resultListRef = useRef<HTMLDivElement | null>(null);
  const mapInstanceRef = useRef<any>(null);
  const markerListRef = useRef<any[]>([]);
  const hasAppliedInitialBoundsRef = useRef(false);
  const infraOverlaysRef = useRef<any[]>([]);
  const infraCircleRef = useRef<any>(null);
  const safetyPolygonsRef = useRef<any[]>([]);
  const crimePolygonsRef = useRef<any[]>([]);

  const [conditionOptions, setConditionOptions] = useState<
    RecommendConditionItem[]
  >([]);
  const [selectedConditionId, setSelectedConditionId] = useState<number | null>(
    null,
  );

  const [houses, setHouses] = useState<HouseListItem[]>([]);
  const [selectedHouseId, setSelectedHouseId] = useState<number | null>(null);
  const [detailHouseId, setDetailHouseId] = useState<number | null>(null);
  const [selectedInfraCategory, setSelectedInfraCategory] =
    useState<InfraCategory | null>(null);
  const [showSafetyLayer, setShowSafetyLayer] = useState(false);
  const [showCrimeLayer, setShowCrimeLayer] = useState(false);

  const [hasNext, setHasNext] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [nextHouseId, setNextHouseId] = useState<number>(0);
  const [isRightPanelOpen, setIsRightPanelOpen] = useState(true);
  const [mapReady, setMapReady] = useState(false);
  const [initialLoadingPhase, setInitialLoadingPhase] =
    useState<InitialLoadingPhase>('analyzing');
  const [initialLoadingProgress, setInitialLoadingProgress] = useState(12);

  const selectedHouse = useMemo(() => {
    return houses.find((house) => house.houseId === selectedHouseId) ?? null;
  }, [houses, selectedHouseId]);

  const detailHouse = useMemo(() => {
    if (detailHouseId === null) {
      return null;
    }

    return houses.find((house) => house.houseId === detailHouseId) ?? null;
  }, [houses, detailHouseId]);

  const initialLoadingMessage = useMemo(() => {
    switch (initialLoadingPhase) {
      case 'analyzing':
        return '조건을 분석하고 있어요';
      case 'checking':
        return '매물 데이터를 확인하고 있어요';
      case 'composing':
        return '사용자에게 맞는 추천 이유를 정리하고 있어요';
      case 'waiting':
        return '조금 더 걸리고 있어요. 결과가 준비되는 대로 보여드릴게요';
      default:
        return '추천 매물을 불러오는 중입니다.';
    }
  }, [initialLoadingPhase]);

  useEffect(() => {
    if (!isLoading) {
      return;
    }

    setInitialLoadingPhase('analyzing');
    setInitialLoadingProgress(12);

    const timer1 = window.setTimeout(() => {
      setInitialLoadingPhase('checking');
      setInitialLoadingProgress(40);
    }, 3000);

    const timer2 = window.setTimeout(() => {
      setInitialLoadingPhase('composing');
      setInitialLoadingProgress(72);
    }, 8000);

    const timer3 = window.setTimeout(() => {
      setInitialLoadingPhase('waiting');
      setInitialLoadingProgress(90);
    }, 15000);

    return () => {
      window.clearTimeout(timer1);
      window.clearTimeout(timer2);
      window.clearTimeout(timer3);
    };
  }, [isLoading]);

  useEffect(() => {
    if (typeof window === 'undefined') {
      return;
    }

    const raw = new URLSearchParams(window.location.search).get('conditionId');
    if (!raw) {
      return;
    }

    const parsed = Number(raw);
    if (!Number.isNaN(parsed) && parsed > 0) {
      setPreferredConditionId(parsed);
    }
  }, []);

  const fetchRecommendedHouses = async (
    conditionId: number,
    houseId: number,
  ): Promise<{
    items: HouseListItem[];
    hasNext: boolean;
    nextHouseId: number;
  }> => {
    const requestBody = { id: conditionId, houseId };
    

    const response = await fetchWithAuth('/api/recommend/houses/top10', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(requestBody),
    });

    

    const data = await response.json().catch(() => null);
    

    if (!response.ok) {
      
      throw new Error(data?.message || '추천 매물 조회 실패');
    }

    const payload: RecommendTop10ApiResponse = data?.data ?? data ?? {};
    

    const rawRecommendations = Array.isArray(payload.recommendations)
      ? payload.recommendations
      : [];
    

    const items = rawRecommendations
      .map((item, index) => mapRecommendationToHouse(item, index))
      .filter((item): item is HouseListItem => item !== null);
    

    const parsedNextHouseId = Number(payload.nextHouseId);
    const fallbackNextHouseId = items[items.length - 1]?.houseId ?? houseId;

    const result = {
      items,
      hasNext: toBooleanOr(payload.hasNext, false),
      nextHouseId: Number.isNaN(parsedNextHouseId)
        ? fallbackNextHouseId
        : parsedNextHouseId,
    };
    
    return result;
  };

  const clearInfraMarkers = () => {
    infraOverlaysRef.current.forEach((overlay) => overlay.setMap(null));
    infraOverlaysRef.current = [];

    if (infraCircleRef.current) {
      infraCircleRef.current.setMap(null);
      infraCircleRef.current = null;
    }
  };

  const clearSafetyLayer = () => {
    safetyPolygonsRef.current.forEach((polygon) => polygon.setMap(null));
    safetyPolygonsRef.current = [];
  };

  const clearCrimeLayer = () => {
    crimePolygonsRef.current.forEach((polygon) => polygon.setMap(null));
    crimePolygonsRef.current = [];
  };

  const getSafetyLayerStyle = (level: number) => {
    if (level >= 9) {
      return {
        strokeColor: '#166534',
        fillColor: '#22C55E',
        fillOpacity: 0.32,
      };
    }

    if (level >= 7) {
      return {
        strokeColor: '#15803D',
        fillColor: '#4ADE80',
        fillOpacity: 0.26,
      };
    }

    if (level >= 5) {
      return {
        strokeColor: '#16A34A',
        fillColor: '#86EFAC',
        fillOpacity: 0.22,
      };
    }

    if (level >= 3) {
      return {
        strokeColor: '#65A30D',
        fillColor: '#BEF264',
        fillOpacity: 0.18,
      };
    }

    return {
      strokeColor: '#A3A3A3',
      fillColor: '#E5E7EB',
      fillOpacity: 0.14,
    };
  };

  const getCrimeLayerStyle = (level: number) => {
    if (level >= 6) {
      return {
        strokeColor: '#991B1B',
        fillColor: '#DC2626',
        fillOpacity: 0.34,
      };
    }

    if (level >= 5) {
      return {
        strokeColor: '#B91C1C',
        fillColor: '#EF4444',
        fillOpacity: 0.28,
      };
    }

    if (level >= 4) {
      return {
        strokeColor: '#C2410C',
        fillColor: '#F97316',
        fillOpacity: 0.24,
      };
    }

    if (level >= 3) {
      return {
        strokeColor: '#D97706',
        fillColor: '#F59E0B',
        fillOpacity: 0.2,
      };
    }

    if (level >= 2) {
      return {
        strokeColor: '#CA8A04',
        fillColor: '#FACC15',
        fillOpacity: 0.16,
      };
    }

    return {
      strokeColor: '#65A30D',
      fillColor: '#A3E635',
      fillOpacity: 0.12,
    };
  };

  const moveToHouseWithRadius = (lat: number, lng: number) => {
    if (!mapInstanceRef.current || !window.kakao) return;

    const map = mapInstanceRef.current;
    const center = new window.kakao.maps.LatLng(lat, lng);

    const latOffset = 800 / 111320;
    const lngOffset = 800 / (111320 * Math.cos((lat * Math.PI) / 180));

    const sw = new window.kakao.maps.LatLng(lat - latOffset, lng - lngOffset);
    const ne = new window.kakao.maps.LatLng(lat + latOffset, lng + lngOffset);
    const bounds = new window.kakao.maps.LatLngBounds(sw, ne);

    map.setBounds(bounds);
    map.panTo(center);
  };

  const drawInfraRadiusCircle = (lat: number, lng: number) => {
    if (!mapInstanceRef.current || !window.kakao) return;

    const center = new window.kakao.maps.LatLng(lat, lng);

    const circle = new window.kakao.maps.Circle({
      center,
      radius: 800,
      strokeWeight: 2,
      strokeColor: '#10B981',
      strokeOpacity: 0.9,
      strokeStyle: 'solid',
      fillColor: '#10B981',
      fillOpacity: 0.08,
    });

    circle.setMap(mapInstanceRef.current);
    infraCircleRef.current = circle;
  };

  const drawInfraMarkers = (
    infraList: { type: string; lat: number | string; lng: number | string }[],
  ) => {
    if (!mapInstanceRef.current || !window.kakao) return;

    const map = mapInstanceRef.current;

    infraList.forEach((infra) => {
      const position = new window.kakao.maps.LatLng(
        Number(infra.lat),
        Number(infra.lng),
      );

      const overlay = new window.kakao.maps.CustomOverlay({
        position,
        yAnchor: 0.5,
        content: getInfraMarkerHtml(infra.type),
      });

      overlay.setMap(map);
      infraOverlaysRef.current.push(overlay);
    });
  };

  const drawGeoJsonPolygons = (geojson: any, layerType: 'safety' | 'crime') => {
    if (!mapInstanceRef.current || !window.kakao) return;

    const map = mapInstanceRef.current;
    const isSafety = layerType === 'safety';

    geojson.features.forEach((feature: any) => {
      const geometryType = feature.geometry?.type;
      const coordinates = feature.geometry?.coordinates;
      const properties = feature.properties ?? {};

      if (!coordinates) return;

      const polygonSets =
        geometryType === 'MultiPolygon'
          ? coordinates
          : geometryType === 'Polygon'
            ? [coordinates]
            : [];

      const level = isSafety
        ? Number(properties.safety_level ?? 0)
        : Number(properties.crime_level ?? 0);

      const style = isSafety
        ? getSafetyLayerStyle(level)
        : getCrimeLayerStyle(level);

      polygonSets.forEach((polygonCoords: any) => {
        const outerRing = polygonCoords[0];
        if (!outerRing) return;

        const path = outerRing.map(
          (coord: number[]) => new window.kakao.maps.LatLng(coord[1], coord[0]),
        );

        const polygon = new window.kakao.maps.Polygon({
          path,
          strokeWeight: 1,
          strokeColor: style.strokeColor,
          strokeOpacity: 0.75,
          fillColor: style.fillColor,
          fillOpacity: style.fillOpacity,
        });

        polygon.setMap(map);

        if (isSafety) {
          safetyPolygonsRef.current.push(polygon);
        } else {
          crimePolygonsRef.current.push(polygon);
        }
      });
    });
  };

  const handleToggleSafetyLayer = async () => {
    if (showSafetyLayer) {
      clearSafetyLayer();
      setShowSafetyLayer(false);
      return;
    }

    try {
      clearCrimeLayer();
      setShowCrimeLayer(false);
      const response = await fetch('/geo/women_safe_walk_seoul_merged.geojson');
      const data = await response.json();

      drawGeoJsonPolygons(data, 'safety');
      setShowSafetyLayer(true);
    } catch (error) {
      
    }
  };

  const handleToggleCrimeLayer = async () => {
    if (showCrimeLayer) {
      clearCrimeLayer();
      setShowCrimeLayer(false);
      return;
    }

    try {
      clearSafetyLayer();
      setShowSafetyLayer(false);
      const response = await fetch('/geo/crime_zone_seoul_merged.geojson');
      const data = await response.json();

      drawGeoJsonPolygons(data, 'crime');
      setShowCrimeLayer(true);
    } catch (error) {
      
    }
  };

  useEffect(() => {
    let isMounted = true;

    const loadConditions = async () => {
      try {
        const response = await fetchWithAuth('/api/recommend/conditions');
        const data = await response.json().catch(() => null);
        const sourceConditions = data?.data?.conditions;

        if (!Array.isArray(sourceConditions) || sourceConditions.length === 0) {
          if (!isMounted) return;
          setConditionOptions([]);
          setSelectedConditionId(null);
          setIsLoading(false);
          return;
        }

        const savedDefaultId = Number(
          window.localStorage.getItem('recommendDefaultConditionId'),
        );

        const mappedConditions: RecommendConditionItem[] = sourceConditions
          .map((item: unknown) => {
            if (typeof item !== 'object' || item === null) {
              return null;
            }

            const rawCondition = item as Record<string, unknown>;
            const conditionId = Number(rawCondition.id);

            if (Number.isNaN(conditionId)) {
              return null;
            }

            const name =
              typeof rawCondition.name === 'string' && rawCondition.name.trim()
                ? rawCondition.name
                : `조건 ${conditionId}`;

            return {
              conditionId,
              name,
              isDefault:
                !Number.isNaN(savedDefaultId) && conditionId === savedDefaultId,
            };
          })
          .filter(
            (condition): condition is RecommendConditionItem => condition !== null,
          );

        if (mappedConditions.length === 0) {
          if (!isMounted) return;
          setConditionOptions([]);
          setSelectedConditionId(null);
          setIsLoading(false);
          return;
        }

        if (!isMounted) return;
        setConditionOptions(mappedConditions);

        const forcedCondition =
          preferredConditionId == null
            ? null
            : mappedConditions.find(
                (condition) => condition.conditionId === preferredConditionId,
              );

        const defaultCondition =
          forcedCondition ??
          mappedConditions.find((condition) => condition.isDefault) ??
          mappedConditions[0];

        setSelectedConditionId(defaultCondition?.conditionId ?? null);
      } catch (error) {
        
        if (!isMounted) return;
        setConditionOptions([]);
        setSelectedConditionId(null);
        setIsLoading(false);
      }
    };

    loadConditions();

    return () => {
      isMounted = false;
    };
  }, [preferredConditionId]);

  useEffect(() => {
    if (selectedConditionId == null) {
      return;
    }

    hasAppliedInitialBoundsRef.current = false;

    let isMounted = true;

    const loadInitialHouses = async () => {
      setIsLoading(true);

      try {
        window.localStorage.setItem(
          'recommendDefaultConditionId',
          String(selectedConditionId),
        );

        const response = await fetchRecommendedHouses(selectedConditionId, 0);
        if (!isMounted) return;

        setHouses(response.items);
        setHasNext(response.hasNext);
        setNextHouseId(response.nextHouseId);
        setSelectedHouseId(null);
        setDetailHouseId(null);
        setIsRightPanelOpen(true);
      } catch (error) {
        
        if (!isMounted) return;
        setHouses([]);
        setHasNext(false);
        setNextHouseId(0);
        setSelectedHouseId(null);
      } finally {
        if (!isMounted) return;
        setInitialLoadingProgress(100);
        await new Promise((resolve) => window.setTimeout(resolve, 500));
        if (!isMounted) return;
        setIsLoading(false);
      }
    };

    loadInitialHouses();

    return () => {
      isMounted = false;
    };
  }, [selectedConditionId]);

  useEffect(() => {
    if (!mapRef.current || !window.kakao) {
      return;
    }

    window.kakao.maps.load(() => {
      if (!mapRef.current) {
        return;
      }

      const defaultCenterHouse = houses[0];
      const center = new window.kakao.maps.LatLng(
        defaultCenterHouse?.lat ?? 37.4979,
        defaultCenterHouse?.lng ?? 127.0276,
      );

      const map = new window.kakao.maps.Map(mapRef.current, {
        center,
        level: 5,
      });

      mapInstanceRef.current = map;
      setMapReady(true);
    });
  }, [houses]);

  useEffect(() => {
    if (!window.kakao || !mapInstanceRef.current) {
      return;
    }

    markerListRef.current.forEach((overlayOrMarker) =>
      overlayOrMarker.setMap(null),
    );
    markerListRef.current = [];

    const map = mapInstanceRef.current;

    window.__handlePinClick = (houseId: number) => {
      setSelectedHouseId(houseId);
      setDetailHouseId(houseId);
      setIsRightPanelOpen(true);
    };

    houses.forEach((house, index) => {
      const rank = index + 1;
      const isSelected = detailHouseId === house.houseId;
      const markerZIndex = Math.max(1, houses.length - index);

      const markerPosition = new window.kakao.maps.LatLng(house.lat, house.lng);

      const marker = new window.kakao.maps.Marker({
        map,
        position: markerPosition,
        title: `${rank}위 추천 매물`,
        opacity: 0,
        zIndex: markerZIndex,
      });

      const badgeBackground =
        rank === 1
          ? '#F4C542'
          : rank === 2
            ? '#B8C2CC'
            : rank === 3
              ? '#B8793D'
              : '#5E9F3B';

      const markerContent = `
        <div onclick="window.__handlePinClick(${house.houseId})" style="
          position: relative;
          transform: translateY(-8px);
          display: flex;
          flex-direction: column;
          align-items: center;
          cursor: pointer;
        ">
          <div style="
            min-width: 28px;
            height: 28px;
            border-radius: 9999px;
            background: ${isSelected ? '#EF4444' : badgeBackground};
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 11px;
            font-weight: 700;
            padding: 0 7px;
            box-shadow: 0 4px 10px rgba(0,0,0,0.18);
            border: ${isSelected ? '2px solid #b91c1c' : 'none'};
          ">
            ${rank}
          </div>
        </div>
      `;

      const customOverlay = new window.kakao.maps.CustomOverlay({
        position: markerPosition,
        content: markerContent,
        yAnchor: 1.2,
        clickable: true,
        zIndex: markerZIndex,
      });

      customOverlay.setMap(map);

      window.kakao.maps.event.addListener(marker, 'click', () =>
        window.__handlePinClick(house.houseId),
      );

      markerListRef.current.push(marker);
      markerListRef.current.push(customOverlay);
    });
  }, [houses, detailHouseId, mapReady]);

  useEffect(() => {
    if (!window.kakao || !mapInstanceRef.current || !mapReady) {
      return;
    }

    if (hasAppliedInitialBoundsRef.current || houses.length === 0) {
      return;
    }

    const map = mapInstanceRef.current;
    const bounds = new window.kakao.maps.LatLngBounds();

    houses.forEach((house) => {
      bounds.extend(new window.kakao.maps.LatLng(house.lat, house.lng));
    });

    if (houses.length === 1) {
      map.setCenter(new window.kakao.maps.LatLng(houses[0].lat, houses[0].lng));
      map.setLevel(4);
    } else {
      map.setBounds(bounds);
    }

    hasAppliedInitialBoundsRef.current = true;
  }, [houses, mapReady]);

  useEffect(() => {
    if (!selectedHouse || !mapInstanceRef.current || !window.kakao) {
      return;
    }

    const nextCenter = new window.kakao.maps.LatLng(
      selectedHouse.lat,
      selectedHouse.lng,
    );

    mapInstanceRef.current.panTo(nextCenter);
  }, [selectedHouse]);

  useEffect(() => {
    setSelectedInfraCategory(null);
    clearInfraMarkers();
  }, [selectedHouseId]);

  useEffect(() => {
    if (detailHouseId === null) {
      setSelectedInfraCategory(null);
      clearInfraMarkers();
    }
  }, [detailHouseId]);

  const handleSelectInfraCategory = async (category: InfraCategory) => {
    if (!selectedHouse) return;

    const next = selectedInfraCategory === category ? null : category;

    setSelectedInfraCategory(next);
    clearInfraMarkers();

    if (!next) {
      return;
    }

    moveToHouseWithRadius(selectedHouse.lat, selectedHouse.lng);
    drawInfraRadiusCircle(selectedHouse.lat, selectedHouse.lng);

    try {
      const infraList = await fetchHouseInfraMarkers(selectedHouse.houseId, next);
      drawInfraMarkers(infraList);
    } catch (error) {
      
    }
  };

  const handleLoadMore = async () => {
    if (
      selectedConditionId == null ||
      !hasNext ||
      isLoading ||
      isLoadingMore
    ) {
      return;
    }

    setIsLoadingMore(true);

    try {
      const response = await fetchRecommendedHouses(
        selectedConditionId,
        nextHouseId,
      );

      setHouses((prev) => {
        const existingIds = new Set(prev.map((house) => house.houseId));
        const appended = response.items.filter(
          (house) => !existingIds.has(house.houseId),
        );
        return [...prev, ...appended];
      });
      setHasNext(response.hasNext);
      setNextHouseId(response.nextHouseId);
    } catch (error) {
      
    } finally {
      setIsLoadingMore(false);
    }
  };

  const handleResultScroll = () => {
    if (!resultListRef.current || !hasNext || isLoading || isLoadingMore) {
      return;
    }

    const { scrollTop, clientHeight, scrollHeight } = resultListRef.current;
    const threshold = 48;

    if (scrollTop + clientHeight >= scrollHeight - threshold) {
      void handleLoadMore();
    }
  };

  const handleSelectCondition = (conditionId: number) => {
    setSelectedConditionId(conditionId);
    window.localStorage.setItem('recommendDefaultConditionId', String(conditionId));
  };

  const handleOpenHouseDetail = (houseId: number) => {
    setSelectedHouseId(houseId);
    setDetailHouseId(houseId);
    setIsRightPanelOpen(true);
  };

  const handleBackToList = () => {
    setDetailHouseId(null);
    setIsRightPanelOpen(true);
  };

  const handleToggleWish = useCallback(async (houseId: number, liked: boolean) => {
    const response = await fetchWithAuth(`/api/houses/wish/${houseId}`, {
      method: 'POST',
    });
    const data = await response.json().catch(() => null);
    if (!response.ok) {
      throw new Error(`찜하기 실패: ${data?.message || response.status}`);
    }
    setHouses((prev) =>
      prev.map((house) => (house.houseId === houseId ? { ...house, isLiked: liked } : house)),
    );
  }, []);

  const showInfraMenu = detailHouseId !== null && selectedHouseId !== null;
  const showGeoLayerButtons = detailHouseId === null;
    const floatingMenuRightClass = isRightPanelOpen ? 'right-[396px]' : 'right-4';
  const geoLayerMenuClassName = `${floatingMenuRightClass} bottom-6`;
  const infraMenuClassName = floatingMenuRightClass;

  return (
    <main
      className={`relative flex w-full overflow-hidden bg-white ${PANEL_HEIGHT_CLASS}`}
    >
      <section className="relative flex-1 overflow-hidden">
        <div
          ref={mapRef}
          className="h-full w-full bg-gray-100"
          aria-label="추천 매물 지도"
        />

        <RecommendConditionPanel
          conditionOptions={conditionOptions}
          selectedConditionId={selectedConditionId}
          onChangeCondition={handleSelectCondition}
        />

        <MapLayerToggle
          isVisible={showGeoLayerButtons}
          showSafetyLayer={showSafetyLayer}
          showCrimeLayer={showCrimeLayer}
          onToggleSafety={handleToggleSafetyLayer}
          onToggleCrime={handleToggleCrimeLayer}
          className={geoLayerMenuClassName}
        />

        {showInfraMenu && (
          <HouseInfraFloatingMenu
            isVisible={true}
            selectedCategory={selectedInfraCategory}
            onSelect={handleSelectInfraCategory}
            className={infraMenuClassName}
          />
        )}
      </section>

      <button
        type="button"
        onClick={() => setIsRightPanelOpen((prev) => !prev)}
        aria-label={
          isRightPanelOpen ? '오른쪽 패널 숨기기' : '오른쪽 패널 펼치기'
        }
        className={`absolute top-1/2 z-30 flex h-10 w-6 -translate-y-1/2 items-center justify-center rounded-l-full border border-r-0 border-gray-200 bg-white shadow-md transition-all ${
          isRightPanelOpen ? 'right-95' : 'right-0'
        }`}
      >
        {isRightPanelOpen ? (
          <HiChevronRight className="text-base text-gray-700" />
        ) : (
          <HiChevronLeft className="text-base text-gray-700" />
        )}
      </button>

      <div
        className={`absolute right-0 top-0 z-10 h-full w-95 transition-transform duration-300 ease-in-out ${
          isRightPanelOpen ? 'translate-x-0' : 'translate-x-full'
        }`}
      >
        {detailHouseId !== null ? (
          <RecommendHouseDetailPanel
            houseId={detailHouseId}
            listAiMessage={detailHouse?.aiMessage ?? null}
            onBack={handleBackToList}
            onToggleWish={handleToggleWish}
          />
        ) : (
          <aside className="flex h-full w-95 shrink-0 flex-col border-l border-gray-200 bg-white">
            <div className="border-b border-gray-200 px-4 py-3">
              <h2 className="text-base font-bold text-black">추천 결과</h2>
            </div>

            <div
              ref={resultListRef}
              onScroll={handleResultScroll}
              className="flex-1 overflow-y-auto px-4 py-4"
            >
              {isLoading ? (
                <div className="flex min-h-52 flex-col items-center justify-center gap-4 rounded-2xl border border-gray-200 bg-white px-4 py-6">
                  <p className="text-center text-sm font-medium text-gray-700">
                    {initialLoadingMessage}
                  </p>
                  <div className="w-full max-w-70">
                    <div className="h-2 w-full overflow-hidden rounded-full bg-gray-200">
                      <div
                        className="h-full bg-primary-300 transition-all duration-500 ease-out"
                        style={{ width: `${initialLoadingProgress}%` }}
                      />
                    </div>
                    <p className="mt-1 text-center text-xs text-gray-500">
                      {initialLoadingProgress}%
                    </p>
                  </div>
                </div>
              ) : houses.length === 0 ? (
                <div className="flex min-h-52 items-center justify-center text-sm text-gray-500">
                  추천 결과가 없습니다.
                </div>
              ) : (
                <div className="space-y-3">
                  {houses.map((house, index) => (
                    <RankedHouseCard
                      key={house.houseId}
                      house={house}
                      rank={index + 1}
                      isSelected={selectedHouseId === house.houseId}
                      onSelectHouse={handleOpenHouseDetail}
                      onToggleWish={handleToggleWish}
                    />
                  ))}

                  {isLoadingMore && (
                    <div className="flex h-10 w-full items-center justify-center rounded-xl border border-gray-200 bg-white text-sm font-semibold text-gray-600">
                      불러오는 중...
                    </div>
                  )}
                </div>
              )}
            </div>
          </aside>
        )}
      </div>
    </main>
  );
}