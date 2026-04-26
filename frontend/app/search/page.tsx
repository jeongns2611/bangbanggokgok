'use client';

import { Suspense, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import { HiChevronLeft, HiChevronRight } from 'react-icons/hi';
import SearchOverlay from '@/components/features/search/SearchOverlay';
import HouseResultPanel from '@/components/features/search/HouseResultPanel';
import HouseDetailPanel from '@/components/features/search/HouseDetailPanel';
import HouseInfraFloatingMenu, {
  type InfraCategory,
} from '@/components/features/search/HouseInfraFloatingMenu';
import MapLayerToggle from '@/components/features/search/MapLayerToggle';
import {
  INITIAL_FILTER_STATE,
  type FilterState,
} from '@/components/features/search/UserFilterPanel';
import type { AiChatRecommendationHouse } from '@/types/api';
import { type HouseListItem } from '@/mock/houses';
import { fetchWithAuth } from '@/lib/fetchWithAuth';
import { fetchHouseInfraMarkers } from '@/lib/houseInfrastructure';
import { getInfraMarkerHtml } from '@/lib/infraMarkerIcon';

declare global {
  interface Window {
    kakao?: any;
  }
}

const REGION_COORDINATES: Record<string, { lat: number; lng: number; level: number }> = {
  강남구: { lat: 37.4979, lng: 127.0276, level: 6 },
  강동구: { lat: 37.5303, lng: 127.1238, level: 6 },
  강북구: { lat: 37.6396, lng: 127.0253, level: 6 },
  강서구: { lat: 37.5505, lng: 126.834, level: 6 },
  관악구: { lat: 37.4819, lng: 126.9548, level: 6 },
  광진구: { lat: 37.5388, lng: 127.0823, level: 6 },
  구로구: { lat: 37.495, lng: 126.8874, level: 6 },
  금천구: { lat: 37.4546, lng: 126.9037, level: 6 },
  노원구: { lat: 37.6548, lng: 127.0565, level: 6 },
  도봉구: { lat: 37.6691, lng: 127.0474, level: 6 },
  동대문구: { lat: 37.5744, lng: 127.0396, level: 6 },
  동작구: { lat: 37.4959, lng: 126.9686, level: 6 },
  마포구: { lat: 37.5663, lng: 126.9014, level: 6 },
  서대문구: { lat: 37.5797, lng: 126.9368, level: 6 },
  서초구: { lat: 37.4837, lng: 127.0324, level: 6 },
  성동구: { lat: 37.5633, lng: 127.0371, level: 6 },
  성북구: { lat: 37.5894, lng: 127.0173, level: 6 },
  송파구: { lat: 37.5145, lng: 127.1059, level: 6 },
  양천구: { lat: 37.5168, lng: 126.8671, level: 6 },
  영등포구: { lat: 37.5263, lng: 126.8974, level: 6 },
  용산구: { lat: 37.5326, lng: 126.9905, level: 6 },
  은평구: { lat: 37.6024, lng: 126.9212, level: 6 },
  종로구: { lat: 37.5735, lng: 126.9787, level: 6 },
  중구: { lat: 37.5641, lng: 126.998, level: 6 },
  중랑구: { lat: 37.6063, lng: 127.0928, level: 6 },
};

const SEARCH_PAGE_SIZE = 10;
const INFRA_FOCUS_LEVEL = 3;
const SEARCH_FILTER_STORAGE_KEY = 'search:filters:v1';
const SEARCH_RELOAD_MARKER_KEY = 'search:reload-marker:v1';
const FILTER_MAX_LIMITS = {
  monthlyCost: 205,
  deposit: 50100,
  managementCost: 50,
  floorSize: 101,
} as const;

const HOUSE_TYPE_ALL = [
  '오피스텔',
  '아파트',
  '단독/다가구(원룸)',
  '연립/다세대(빌라)',
];

const RENT_TYPE_ALL = ['전세', '월세'];
const FLOOR_ALL = ['반지하', '1층', '지상층'];

const DEFAULT_MARKER_COLOR = '#2563EB';
const SELECTED_MARKER_COLOR = '#EF4444';
const MARKER_ICON_WIDTH = 36;
const MARKER_ICON_HEIGHT = 40;
const MARKER_ICON_ANCHOR_X = 16;
const MARKER_ICON_ANCHOR_Y = 38;

const createMarkerIconDataUrl = (color: string, order?: number) => {
  const orderText = order ? ['①', '②', '③'][order - 1] : '';
  const svg = `
    <svg width="32" height="38" viewBox="0 0 36 42" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M18 41.1C17.2 41.1 16.5 40.7 16.1 40L6.8 25.3C4.8 22.2 3.7 18.7 3.7 15.1C3.7 7.3 10.1 0.9 18 0.9C25.9 0.9 32.3 7.3 32.3 15.1C32.3 18.7 31.2 22.2 29.2 25.3L19.9 40C19.5 40.7 18.8 41.1 18 41.1Z" fill="${color}"/>
      <circle cx="18" cy="15" r="8.2" fill="white"/>
      ${orderText ? `<text x="18" y="15" text-anchor="middle" dominant-baseline="middle" font-size="14" font-weight="900" fill="${color}">${orderText}</text>` : ''}
    </svg>
  `;

  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`;
};

const normalizeHouseTypes = (houseTypes: string[]) => {
  if (houseTypes.includes('전체')) return HOUSE_TYPE_ALL;

  return houseTypes.map((type) => {
    if (type === '단독/다가구(원룸)') return '단독/다가구(원룸)';
    if (type === '연립/다세대(빌라)') return '연립/다세대(빌라)';
    return type;
  });
};

const normalizeRentTypes = (rentTypes: string[]) => {
  if (rentTypes.includes('전체')) return RENT_TYPE_ALL;
  return rentTypes;
};

const normalizeFloors = (floors: string[]) => {
  if (floors.includes('전체')) return FLOOR_ALL;
  return floors;
};

const normalizeMaxFilterValue = (value: number, maxLimit: number) =>
  value >= maxLimit ? -1 : value;

const normalizeHouseStatusLabel = (status: string) => {
  if (status.includes('거래가능')) {
    return status.replace('거래가능', '거래 가능');
  }

  return status;
};

const isValidMapCoordinate = (lat: number, lng: number) =>
  Number.isFinite(lat) &&
  Number.isFinite(lng) &&
  lat >= 33 &&
  lat <= 43 &&
  lng >= 124 &&
  lng <= 131;

const isNumber = (value: unknown): value is number =>
  typeof value === 'number' && Number.isFinite(value);

const isStringArray = (value: unknown): value is string[] =>
  Array.isArray(value) && value.every((item) => typeof item === 'string');

const isValidRange = (value: unknown): value is { minValue: number; maxValue: number } => {
  if (!value || typeof value !== 'object') return false;

  const range = value as { minValue?: unknown; maxValue?: unknown };
  return isNumber(range.minValue) && isNumber(range.maxValue);
};

const isValidFilterState = (value: unknown): value is FilterState => {
  if (!value || typeof value !== 'object') return false;

  const candidate = value as Partial<FilterState>;

  return (
    typeof candidate.sidoName === 'string' &&
    typeof candidate.sigunguName === 'string' &&
    isStringArray(candidate.houseType) &&
    isStringArray(candidate.rentType) &&
    isValidRange(candidate.monthlyCost) &&
    isValidRange(candidate.deposit) &&
    isNumber(candidate.managementCost) &&
    isValidRange(candidate.floorSize) &&
    isStringArray(candidate.floor)
  );
};

function MapPageContent() {
  const mapRef = useRef<HTMLDivElement | null>(null);
  const mapInstance = useRef<any>(null);
  const houseMarkersRef = useRef<any[]>([]);
  const infraOverlaysRef = useRef<any[]>([]);
  const infraCircleRef = useRef<any>(null);
  const safetyPolygonsRef = useRef<any[]>([]);
  const crimePolygonsRef = useRef<any[]>([]);
  const placesServiceRef = useRef<any>(null);

  const searchDebounceRef = useRef<number | null>(null);
  const latestSearchRequestIdRef = useRef(0);

  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();

  const selectedHouseId = Number(searchParams.get('houseId')) || null;
  const regionParam = searchParams.get('region');

  const [keyword, setKeyword] = useState('');
  const [suggestions, setSuggestions] = useState<any[]>([]);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isNoResult, setIsNoResult] = useState(false);
  const [houses, setHouses] = useState<HouseListItem[]>([]);
  const [isFiltering, setIsFiltering] = useState(false);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(false);
  const [searchLastId, setSearchLastId] = useState<number | null>(null);
  const [activeSearchFilters, setActiveSearchFilters] = useState<FilterState | null>(null);
  const [hasSearchedByFilter, setHasSearchedByFilter] = useState(false);
  const [isRightPanelOpen, setIsRightPanelOpen] = useState(true);
  const [selectedInfraCategory, setSelectedInfraCategory] =
    useState<InfraCategory | null>(null);
  const [showSafetyLayer, setShowSafetyLayer] = useState(false);
  const [showCrimeLayer, setShowCrimeLayer] = useState(false);
  const [restoredFilters, setRestoredFilters] = useState<FilterState | null>(null);
  const [latestAiRecommendedHouses, setLatestAiRecommendedHouses] = useState<
    (AiChatRecommendationHouse & { recommendationOrder: number })[]
  >([]);
  const [recommendationOrders, setRecommendationOrders] = useState<Map<number, number>>(
    new Map(),
  );
  const [temporarySelectedHouse, setTemporarySelectedHouse] = useState<{
    houseId: number;
    lat: number;
    lng: number;
  } | null>(null);

  const mockHousePositions = useMemo(
    () => [
      { id: 501, lat: 37.4979, lng: 127.0276 },
      { id: 502, lat: 37.4995, lng: 127.0305 },
      { id: 503, lat: 37.4958, lng: 127.0322 },
    ],
    [],
  );

  const initialFilters = useMemo<FilterState>(() => {
    if (restoredFilters) {
      return restoredFilters;
    }

    if (regionParam) {
      return {
        ...INITIAL_FILTER_STATE,
        sidoName: '서울특별시',
        sigunguName: regionParam,
      };
    }
    return INITIAL_FILTER_STATE;
  }, [regionParam, restoredFilters]);

  const [latestAiFilters, setLatestAiFilters] = useState<FilterState>(initialFilters);

  const moveToLocation = (lat: number, lng: number, level?: number) => {
    if (!mapInstance.current || !window.kakao) return;

    const position = new window.kakao.maps.LatLng(lat, lng);
    mapInstance.current.setCenter(position);
    if (level) {
      mapInstance.current.setLevel(level);
    }
  };

  const moveToHousesBounds = (
    targetHouses: HouseListItem[],
    fallbackSigunguName?: string,
  ) => {
    if (!mapInstance.current || !window.kakao) return;

    const validHouses = targetHouses.filter((house) =>
      isValidMapCoordinate(house.lat, house.lng),
    );

    if (validHouses.length === 0) {
      if (fallbackSigunguName && REGION_COORDINATES[fallbackSigunguName]) {
        const coords = REGION_COORDINATES[fallbackSigunguName];
        moveToLocation(coords.lat, coords.lng, coords.level);
      }
      return;
    }

    if (validHouses.length === 1) {
      moveToLocation(validHouses[0].lat, validHouses[0].lng, 4);
      return;
    }

    const bounds = new window.kakao.maps.LatLngBounds();
    validHouses.forEach((house) => {
      bounds.extend(new window.kakao.maps.LatLng(house.lat, house.lng));
    });

    mapInstance.current.setBounds(bounds);
  };

  const closeSearchDropdown = () => {
    setSuggestions([]);
    setIsDropdownOpen(false);
    setIsNoResult(false);
  };

  const runKeywordSearch = (
    rawKeyword: string,
    options?: {
      openDropdown?: boolean;
      moveToFirstResult?: boolean;
    },
  ) => {
    const trimmedKeyword = rawKeyword.trim();

    if (!trimmedKeyword || !placesServiceRef.current || !window.kakao) {
      setSuggestions([]);
      setIsDropdownOpen(false);
      setIsNoResult(false);
      return;
    }

    const requestId = ++latestSearchRequestIdRef.current;

    placesServiceRef.current.keywordSearch(
      trimmedKeyword,
      (data: any[], status: string) => {
        if (requestId !== latestSearchRequestIdRef.current) return;

        if (status === window.kakao.maps.services.Status.OK) {
          setSuggestions(data);
          setIsNoResult(false);
          setIsDropdownOpen(Boolean(options?.openDropdown));

          if (options?.moveToFirstResult && data[0]) {
            moveToLocation(Number(data[0].y), Number(data[0].x));
          }
          return;
        }

        if (status === window.kakao.maps.services.Status.ZERO_RESULT) {
          setSuggestions([]);
          setIsNoResult(true);
          setIsDropdownOpen(Boolean(options?.openDropdown));
          return;
        }

        setSuggestions([]);
        setIsDropdownOpen(false);
        setIsNoResult(false);
      },
    );
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

  const moveToHouseWithRadius = (lat: number, lng: number, level = INFRA_FOCUS_LEVEL) => {
    if (!mapInstance.current || !window.kakao) return;

    const map = mapInstance.current;
    const center = new window.kakao.maps.LatLng(lat, lng);

    const latOffset = 800 / 111320;
    const lngOffset = 800 / (111320 * Math.cos((lat * Math.PI) / 180));

    const sw = new window.kakao.maps.LatLng(lat - latOffset, lng - lngOffset);
    const ne = new window.kakao.maps.LatLng(lat + latOffset, lng + lngOffset);
    const bounds = new window.kakao.maps.LatLngBounds(sw, ne);

    map.setBounds(bounds);
    map.panTo(center);
    map.setLevel(level);
  };

  const drawInfraRadiusCircle = (lat: number, lng: number) => {
    if (!mapInstance.current || !window.kakao) return;

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

    circle.setMap(mapInstance.current);
    infraCircleRef.current = circle;
  };

  const drawInfraMarkers = (
    infraList: { type: string; lat: number | string; lng: number | string }[],
  ) => {
    if (!mapInstance.current || !window.kakao) return;

    const map = mapInstance.current;

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
    if (!mapInstance.current || !window.kakao) return;

    const map = mapInstance.current;
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
    if (!window.kakao || !mapRef.current || mapInstance.current) return;

    const regionCoord = regionParam ? REGION_COORDINATES[regionParam] : null;

    window.kakao.maps.load(() => {
      const center = regionCoord
        ? new window.kakao.maps.LatLng(regionCoord.lat, regionCoord.lng)
        : new window.kakao.maps.LatLng(37.4979, 127.0276);

      const map = new window.kakao.maps.Map(mapRef.current, {
        center,
        level: regionCoord ? regionCoord.level : 4,
      });

      mapInstance.current = map;
      placesServiceRef.current = new window.kakao.maps.services.Places();
    });
  }, [regionParam]);

  useEffect(() => {
    if (typeof window === 'undefined') return;

    const markForReload = () => {
      window.sessionStorage.setItem(SEARCH_RELOAD_MARKER_KEY, '1');
    };

    window.addEventListener('beforeunload', markForReload);

    return () => {
      window.removeEventListener('beforeunload', markForReload);
    };
  }, []);

  useEffect(() => {
    if (typeof window === 'undefined') return;

    const shouldRestore =
      window.sessionStorage.getItem(SEARCH_RELOAD_MARKER_KEY) === '1';
    window.sessionStorage.removeItem(SEARCH_RELOAD_MARKER_KEY);

    if (!shouldRestore) {
      window.sessionStorage.removeItem(SEARCH_FILTER_STORAGE_KEY);
      setRestoredFilters(null);
      return;
    }

    const raw = window.sessionStorage.getItem(SEARCH_FILTER_STORAGE_KEY);
    if (!raw) return;

    try {
      const parsed = JSON.parse(raw);
      if (isValidFilterState(parsed)) {
        setRestoredFilters(parsed);
      } else {
        window.sessionStorage.removeItem(SEARCH_FILTER_STORAGE_KEY);
      }
    } catch {
      window.sessionStorage.removeItem(SEARCH_FILTER_STORAGE_KEY);
    }
  }, []);

  useEffect(() => {
    const trimmedKeyword = keyword.trim();

    if (searchDebounceRef.current) {
      window.clearTimeout(searchDebounceRef.current);
      searchDebounceRef.current = null;
    }

    if (!trimmedKeyword) {
      closeSearchDropdown();
      return;
    }

    searchDebounceRef.current = window.setTimeout(() => {
      runKeywordSearch(trimmedKeyword, {
        openDropdown: true,
        moveToFirstResult: false,
      });
    }, 300);

    return () => {
      if (searchDebounceRef.current) {
        window.clearTimeout(searchDebounceRef.current);
      }
    };
  }, [keyword]);

  useEffect(() => {
    if (!mapInstance.current || !window.kakao) return;

    houseMarkersRef.current.forEach((marker) => marker.setMap(null));
    houseMarkersRef.current = [];

    const markerTargets = houses.map((house) => ({
      houseId: house.houseId,
      lat: house.lat,
      lng: house.lng,
    }));

    if (
      temporarySelectedHouse &&
      selectedHouseId === temporarySelectedHouse.houseId &&
      !markerTargets.some((target) => target.houseId === temporarySelectedHouse.houseId)
    ) {
      markerTargets.push(temporarySelectedHouse);
    }

    if (markerTargets.length === 0) return;

    markerTargets.forEach((house, index) => {
      const fallbackPosition = mockHousePositions[index % mockHousePositions.length];

      const lat = house.lat || fallbackPosition.lat;
      const lng = house.lng || fallbackPosition.lng;

      const latLng = new window.kakao.maps.LatLng(lat, lng);
      const isSelected = selectedHouseId === house.houseId;
      const recommendationOrder = recommendationOrders.get(house.houseId);
      const markerImage = new window.kakao.maps.MarkerImage(
        createMarkerIconDataUrl(
          isSelected ? SELECTED_MARKER_COLOR : DEFAULT_MARKER_COLOR,
          recommendationOrder,
        ),
        new window.kakao.maps.Size(MARKER_ICON_WIDTH, MARKER_ICON_HEIGHT),
        {
          offset: new window.kakao.maps.Point(MARKER_ICON_ANCHOR_X, MARKER_ICON_ANCHOR_Y),
        },
      );

      const marker = new window.kakao.maps.Marker({
        position: latLng,
        image: markerImage,
        zIndex: isSelected ? 10 : 1,
      });

      marker.setMap(mapInstance.current);

      window.kakao.maps.event.addListener(marker, 'click', () => {
        router.push(`${pathname}?houseId=${house.houseId}`);
        setIsRightPanelOpen(true);
      });

      houseMarkersRef.current.push(marker);
    });
  }, [
    houses,
    mockHousePositions,
    pathname,
    recommendationOrders,
    router,
    selectedHouseId,
    temporarySelectedHouse,
  ]);

  useEffect(() => {
    if (!mapInstance.current || !selectedHouseId || !window.kakao) return;

    const selectedHouse = houses.find((house) => house.houseId === selectedHouseId);
    const target =
      selectedHouse && selectedHouse.lat && selectedHouse.lng
        ? { lat: selectedHouse.lat, lng: selectedHouse.lng }
        : temporarySelectedHouse && temporarySelectedHouse.houseId === selectedHouseId
          ? { lat: temporarySelectedHouse.lat, lng: temporarySelectedHouse.lng }
        : mockHousePositions.find((item) => item.id === selectedHouseId);
    if (!target) return;

    const moveLatLng = new window.kakao.maps.LatLng(target.lat, target.lng);
    mapInstance.current.setCenter(moveLatLng);
  }, [houses, mockHousePositions, selectedHouseId, temporarySelectedHouse]);

  useEffect(() => {
    setSelectedInfraCategory(null);
    clearInfraMarkers();
  }, [selectedHouseId]);

  useEffect(() => {
    if (!mapInstance.current) return;

    const timer = window.setTimeout(() => {
      mapInstance.current.relayout();
    }, 320);

    return () => window.clearTimeout(timer);
  }, [isRightPanelOpen, selectedHouseId]);

  const handleSearch = () => {
    const trimmedKeyword = keyword.trim();

    if (!trimmedKeyword) {
      closeSearchDropdown();
      return;
    }

    
    setLatestAiRecommendedHouses([]);
    setRecommendationOrders(new Map());
    setTemporarySelectedHouse(null);

    runKeywordSearch(trimmedKeyword, {
      openDropdown: false,
      moveToFirstResult: true,
    });
  };

  const handleSelectSuggestion = (place: any) => {
    setKeyword(place.place_name);
    closeSearchDropdown();
    moveToLocation(Number(place.y), Number(place.x));
  };

  const handleFilterSubmit = async (
    filters: FilterState,
    options?: { preserveHouseId?: boolean },
  ) => {
    setIsFiltering(true);
    setLatestAiFilters(filters);
    
    setLatestAiRecommendedHouses([]);
    setRecommendationOrders(new Map());
    setTemporarySelectedHouse(null);

    try {
      if (typeof window !== 'undefined') {
        window.sessionStorage.setItem(SEARCH_FILTER_STORAGE_KEY, JSON.stringify(filters));
      }

      const payload = {
        sidoName: filters.sidoName,
        sigunguName: filters.sigunguName,
        houseType: normalizeHouseTypes(filters.houseType),
        rentType: normalizeRentTypes(filters.rentType),
        minMonthlyCost: filters.monthlyCost.minValue,
        maxMonthlyCost: normalizeMaxFilterValue(
          filters.monthlyCost.maxValue,
          FILTER_MAX_LIMITS.monthlyCost,
        ),
        minDeposit: filters.deposit.minValue,
        maxDeposit: normalizeMaxFilterValue(
          filters.deposit.maxValue,
          FILTER_MAX_LIMITS.deposit,
        ),
        managementCost: normalizeMaxFilterValue(
          filters.managementCost,
          FILTER_MAX_LIMITS.managementCost,
        ),
        minFloorSize: filters.floorSize.minValue,
        maxFloorSize: normalizeMaxFilterValue(
          filters.floorSize.maxValue,
          FILTER_MAX_LIMITS.floorSize,
        ),
        floor: normalizeFloors(filters.floor),
        lastId: 0,
        pageSize: SEARCH_PAGE_SIZE,
      };

      

      const response = await fetchWithAuth('/api/houses/search', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });

      const responseText = await response.text();
      
      

      if (!response.ok) {
        throw new Error(`필터 조회 실패: ${response.status} ${responseText}`);
      }

      const body = responseText ? JSON.parse(responseText) : {};
      const responsePayload = body?.data ?? {};
      const rawData =
        responsePayload?.data && Array.isArray(responsePayload.data)
          ? responsePayload.data
          : Array.isArray(body?.data)
            ? body.data
            : [];
      const nextHasMore = Boolean(responsePayload?.hasNext);
      const nextLastId =
        typeof responsePayload?.lastId === 'number' ? responsePayload.lastId : null;

      const mappedHouses: HouseListItem[] = rawData.map((item: any) => {
        let lat = Number(item.lat ?? 0);
        let lng = Number(item.lng ?? 0);

        
        
        if (lat < 33 || lat > 43 || lng < 124 || lng > 131) {
          [lat, lng] = [lng, lat];
        }

        return {
          houseId: Number(item.houseId),
          houseType: item.houseType ?? '-',
          rentType: item.rentType ?? '-',
          deposit: Number(item.deposit ?? 0),
          monthlyCost: Number(item.monthlyCost ?? 0),
          floor: item.floor ?? '-',
          floorSize: 0,
          dong: [item.sidoName, item.sigunguName, item.dongName]
            .filter(Boolean)
            .join(' '),
          houseStatus: normalizeHouseStatusLabel(item.houseStatus ?? '거래가능'),
          isLiked: Boolean(item.isLiked),
          lat,
          lng,
          images: [
            {
              image_url: item.thumbnailUrl ?? null,
              is_thumbnail: true,
            },
          ],
        };
      });

      setHouses(mappedHouses);
      setHasMore(nextHasMore);
      setSearchLastId(nextLastId);
      setActiveSearchFilters(filters);
      setHasSearchedByFilter(true);

      moveToHousesBounds(mappedHouses, filters.sigunguName);

      setIsRightPanelOpen(true);
      setSelectedInfraCategory(null);
      clearInfraMarkers();
      closeSearchDropdown();

      const preserveHouseId = options?.preserveHouseId ?? false;
      if (preserveHouseId && selectedHouseId) {
        router.push(`${pathname}?houseId=${selectedHouseId}`);
      } else {
        router.push(pathname);
      }
    } catch (error) {
      
      alert('매물 조회에 실패했습니다. 잠시 후 다시 시도해주세요.');
      setHouses([]);
      setHasMore(false);
      setSearchLastId(null);
      setActiveSearchFilters(null);
      setHasSearchedByFilter(true);
    } finally {
      setIsFiltering(false);
    }
  };

  const handleAiHouseResult = (aiHouses: AiChatRecommendationHouse[]) => {
    
    const recommendedWithOrder = aiHouses.map((house, index) => ({
      ...house,
      recommendationOrder: index + 1,
    }));
    setLatestAiRecommendedHouses(recommendedWithOrder);

    
    const orderMap = new Map<number, number>();
    recommendedWithOrder.forEach((house) => {
      orderMap.set(Number(house.houseId), house.recommendationOrder);
    });
    setRecommendationOrders(orderMap);
    setTemporarySelectedHouse(null);

    const mappedHouses: HouseListItem[] = aiHouses.map((item, index) => {
      const fallbackPosition = mockHousePositions[index % mockHousePositions.length];
      let lat = Number(item.lat ?? fallbackPosition.lat);
      let lng = Number(item.lng ?? fallbackPosition.lng);

      if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
        lat = fallbackPosition.lat;
        lng = fallbackPosition.lng;
      }

      if (lat < 33 || lat > 43 || lng < 124 || lng > 131) {
        [lat, lng] = [lng, lat];
      }

      const dong = [item.sidoName, item.sigunguName, item.dongName]
        .filter((value): value is string => typeof value === 'string' && value.length > 0)
        .join(' ');

      return {
        houseId: Number(item.houseId),
        houseType: item.houseType ?? '-',
        rentType: item.rentType ?? '-',
        deposit: Number(item.deposit ?? 0),
        monthlyCost: Number(item.monthlyCost ?? 0),
        floor: item.floor ?? '-',
        floorSize: Number(item.floorSize ?? 0),
        dong,
        houseStatus: normalizeHouseStatusLabel(item.houseStatus ?? '거래가능'),
        isLiked: Boolean(item.isLiked),
        lat,
        lng,
        images: [
          {
            image_url: item.thumbnailUrl ?? null,
            is_thumbnail: true,
          },
        ],
      };
    });

    
    setHouses(mappedHouses);
    setHasMore(false);
    setSearchLastId(null);
    setActiveSearchFilters(null);
    setHasSearchedByFilter(false); 
    setIsRightPanelOpen(true);
    setSelectedInfraCategory(null);
    clearInfraMarkers();

    moveToHousesBounds(mappedHouses, latestAiFilters.sigunguName);

    router.push(pathname);
  };

  useEffect(() => {
    if (!restoredFilters) return;

    void handleFilterSubmit(restoredFilters, {
      preserveHouseId: Boolean(selectedHouseId),
    });
  }, [restoredFilters]);

  const handleLoadMore = () => {
    if (!hasMore || isFiltering || isLoadingMore || !activeSearchFilters) return;

    void (async () => {
      setIsLoadingMore(true);

      try {
        const payload = {
          sidoName: activeSearchFilters.sidoName,
          sigunguName: activeSearchFilters.sigunguName,
          houseType: normalizeHouseTypes(activeSearchFilters.houseType),
          rentType: normalizeRentTypes(activeSearchFilters.rentType),
          minMonthlyCost: activeSearchFilters.monthlyCost.minValue,
          maxMonthlyCost: normalizeMaxFilterValue(
            activeSearchFilters.monthlyCost.maxValue,
            FILTER_MAX_LIMITS.monthlyCost,
          ),
          minDeposit: activeSearchFilters.deposit.minValue,
          maxDeposit: normalizeMaxFilterValue(
            activeSearchFilters.deposit.maxValue,
            FILTER_MAX_LIMITS.deposit,
          ),
          managementCost: normalizeMaxFilterValue(
            activeSearchFilters.managementCost,
            FILTER_MAX_LIMITS.managementCost,
          ),
          minFloorSize: activeSearchFilters.floorSize.minValue,
          maxFloorSize: normalizeMaxFilterValue(
            activeSearchFilters.floorSize.maxValue,
            FILTER_MAX_LIMITS.floorSize,
          ),
          floor: normalizeFloors(activeSearchFilters.floor),
          lastId: searchLastId ?? 0,
          pageSize: SEARCH_PAGE_SIZE,
        };

        

        const response = await fetchWithAuth('/api/houses/search', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(payload),
        });

        const responseText = await response.text();
        
        

        if (!response.ok) {
          throw new Error(`추가 매물 조회 실패: ${response.status} ${responseText}`);
        }

        const body = responseText ? JSON.parse(responseText) : {};
        const responsePayload = body?.data ?? {};
        const rawData =
          responsePayload?.data && Array.isArray(responsePayload.data)
            ? responsePayload.data
            : Array.isArray(body?.data)
              ? body.data
              : [];

        const mappedHouses: HouseListItem[] = rawData.map((item: any) => {
          let lat = Number(item.lat ?? 0);
          let lng = Number(item.lng ?? 0);

          if (lat < 33 || lat > 43 || lng < 124 || lng > 131) {
            [lat, lng] = [lng, lat];
          }

          return {
            houseId: Number(item.houseId),
            houseType: item.houseType ?? '-',
            rentType: item.rentType ?? '-',
            deposit: Number(item.deposit ?? 0),
            monthlyCost: Number(item.monthlyCost ?? 0),
            floor: item.floor ?? '-',
            floorSize: 0,
            dong: [item.sidoName, item.sigunguName, item.dongName]
              .filter(Boolean)
              .join(' '),
            houseStatus: normalizeHouseStatusLabel(item.houseStatus ?? '거래가능'),
            isLiked: Boolean(item.isLiked),
            lat,
            lng,
            images: [
              {
                image_url: item.thumbnailUrl ?? null,
                is_thumbnail: true,
              },
            ],
          };
        });

        setHouses((prev) => {
          const existingIds = new Set(prev.map((house) => house.houseId));
          const deduped = mappedHouses.filter((house) => !existingIds.has(house.houseId));
          return [...prev, ...deduped];
        });

        setHasMore(Boolean(responsePayload?.hasNext));
        setSearchLastId(
          typeof responsePayload?.lastId === 'number' ? responsePayload.lastId : null,
        );
      } catch (error) {
        
      } finally {
        setIsLoadingMore(false);
      }
    })();
  };

  const handleSelectHouse = (houseId: number) => {
    router.push(`${pathname}?houseId=${houseId}`);
    setIsRightPanelOpen(true);
  };

  const handleSelectAiCardHouse = (houseId: number) => {
    setIsRightPanelOpen(true);
    
    
    void (async () => {
      try {
        const response = await fetchWithAuth(`/api/houses/search/${houseId}`, {
          method: 'GET',
        });

        const body = await response.json().catch(() => null);

        if (!response.ok || !body) {
          router.push(`${pathname}?houseId=${houseId}`);
          return;
        }

        const data = body?.data ?? body;
        if (!data || typeof data !== 'object') {
          router.push(`${pathname}?houseId=${houseId}`);
          return;
        }

        const raw = data as Record<string, unknown>;

        let lat = Number(raw.lat ?? raw.latitude ?? 0);
        let lng = Number(raw.lng ?? raw.longitude ?? 0);

        if (lat < 33 || lat > 43 || lng < 124 || lng > 131) {
          [lat, lng] = [lng, lat];
        }

        const resolvedLat = Number.isFinite(lat) && lat !== 0 ? lat : mockHousePositions[0].lat;
        const resolvedLng = Number.isFinite(lng) && lng !== 0 ? lng : mockHousePositions[0].lng;

        setTemporarySelectedHouse({
          houseId,
          lat: resolvedLat,
          lng: resolvedLng,
        });

        
        moveToLocation(resolvedLat, resolvedLng, 4);

        router.push(`${pathname}?houseId=${houseId}`);
      } catch (error) {
        
        router.push(`${pathname}?houseId=${houseId}`);
      }
    })();
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

  const handleBackToList = () => {
    router.push(pathname);
  };

  const handleSelectInfraCategory = async (category: InfraCategory) => {
    if (!selectedHouseId) {
      
      return;
    }

    const selectedHouse = houses.find((house) => house.houseId === selectedHouseId);
    const targetHouse =
      selectedHouse && selectedHouse.lat && selectedHouse.lng
        ? { lat: selectedHouse.lat, lng: selectedHouse.lng }
        : mockHousePositions.find((item) => item.id === selectedHouseId);

    if (!targetHouse) {
      
      return;
    }

    const next = selectedInfraCategory === category ? null : category;

    setSelectedInfraCategory(next);
    clearInfraMarkers();

    if (!next) {
      return;
    }

    moveToHouseWithRadius(targetHouse.lat, targetHouse.lng, INFRA_FOCUS_LEVEL);
    drawInfraRadiusCircle(targetHouse.lat, targetHouse.lng);

    try {
      const infraList = await fetchHouseInfraMarkers(selectedHouseId, next);
      drawInfraMarkers(infraList);
    } catch (error) {
      
    }
  };

  const showRightPanel = houses.length > 0 || !!selectedHouseId || hasSearchedByFilter;
  const showInfraMenu = showRightPanel && !!selectedHouseId;
  const showGeoLayerButtons = !selectedHouseId;
  const floatingMenuRightClass =
    showRightPanel && isRightPanelOpen ? 'right-[436px]' : 'right-4';

  return (
    <div className="relative h-[calc(100vh-73px)] w-full overflow-hidden bg-gray-100">
      <div ref={mapRef} className="h-full w-full" />

      <div className="absolute left-0 top-0 z-20 h-full">
        <div className="flex h-full">
          <div className="w-[420px] p-4">
            <SearchOverlay
              keyword={keyword}
              suggestions={suggestions}
              isDropdownOpen={isDropdownOpen}
              isNoResult={isNoResult}
              aiFilters={latestAiFilters}
              onKeywordChange={setKeyword}
              onSearch={handleSearch}
              onSelectSuggestion={handleSelectSuggestion}
              onFilterSubmit={handleFilterSubmit}
              onAiHouseResult={handleAiHouseResult}
              onAiCardHouseSelect={handleSelectAiCardHouse}
              onLatestRecommendedHousesChange={setLatestAiRecommendedHouses}
            />
          </div>
        </div>
      </div>

      <MapLayerToggle
        isVisible={showGeoLayerButtons}
        showSafetyLayer={showSafetyLayer}
        showCrimeLayer={showCrimeLayer}
        onToggleSafety={handleToggleSafetyLayer}
        onToggleCrime={handleToggleCrimeLayer}
        className={floatingMenuRightClass}
      />

      {showRightPanel && (
        <>
          {showInfraMenu && (
            <HouseInfraFloatingMenu
              isVisible={true}
              selectedCategory={selectedInfraCategory}
              onSelect={handleSelectInfraCategory}
              className={floatingMenuRightClass}
            />
          )}

          <button
            type="button"
            onClick={() => setIsRightPanelOpen((prev) => !prev)}
            aria-label={
              isRightPanelOpen ? '오른쪽 패널 숨기기' : '오른쪽 패널 펼치기'
            }
            className={`absolute top-1/2 z-30 flex h-12 w-7 -translate-y-1/2 items-center justify-center rounded-l-full border border-r-0 border-gray-200 bg-white shadow-md transition-all duration-300 ease-in-out ${
              isRightPanelOpen ? 'right-[420px]' : 'right-0'
            }`}
          >
            {isRightPanelOpen ? (
              <HiChevronRight className="text-[18px] text-gray-700" />
            ) : (
              <HiChevronLeft className="text-[18px] text-gray-700" />
            )}
          </button>

          <div
            className={`absolute right-0 top-0 z-10 h-full w-[420px] transition-transform duration-300 ease-in-out ${
              isRightPanelOpen ? 'translate-x-0' : 'translate-x-full'
            }`}
          >
            {selectedHouseId ? (
              <HouseDetailPanel
                houseId={selectedHouseId}
                onBack={handleBackToList}
                onToggleWish={handleToggleWish}
              />
            ) : (
              <HouseResultPanel
                houses={
                  latestAiRecommendedHouses.length > 0
                    ? latestAiRecommendedHouses.map((rec) => 
                        houses.find((house) => house.houseId === rec.houseId)
                      ).filter((house): house is HouseListItem => house !== undefined)
                    : houses
                }
                selectedHouseId={selectedHouseId}
                onSelectHouse={handleSelectHouse}
                onToggleWish={handleToggleWish}
                hasMore={
                  latestAiRecommendedHouses.length === 0 &&
                  hasMore
                }
                isLoadingMore={isLoadingMore}
                onLoadMore={handleLoadMore}
                recommendationOrders={recommendationOrders}
              />
            )}
          </div>
        </>
      )}
    </div>
  );
}

function SearchPageFallback() {
  return (
    <div className="flex h-[calc(100vh-73px)] w-full items-center justify-center bg-gray-100 text-sm text-gray-600">
      페이지를 불러오는 중입니다.
    </div>
  );
}

export default function MapPage() {
  return (
    <Suspense fallback={<SearchPageFallback />}>
      <MapPageContent />
    </Suspense>
  );
}