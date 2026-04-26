'use client';

import { useEffect, useRef, useState } from 'react';
import type { ReactNode } from 'react';
import { HiOutlineMagnifyingGlass } from 'react-icons/hi2';

declare global {
  interface Window {
    kakao?: any;
  }
}

type LocationSearchMapProps = {
  placeKeyword: string;
  onChangePlaceKeyword: (value: string) => void;
  onSelectLocation?: (lat: number, lng: number, placeName?: string, address?: string, jibunAddress?: string) => void;
  selectedLat?: number;
  selectedLng?: number;
  mapSlot?: ReactNode;
  placeholder?: string;
};

export default function LocationSearchMap({
  placeKeyword,
  onChangePlaceKeyword,
  onSelectLocation,
  selectedLat,
  selectedLng,
  mapSlot,
  placeholder = '통근·통학 기준이 되는 위치를 검색하세요.',
}: LocationSearchMapProps) {
  const mapRef = useRef<HTMLDivElement | null>(null);
  const mapInstanceRef = useRef<any>(null);
  const placesServiceRef = useRef<any>(null);
  const geocoderRef = useRef<any>(null);
  const markerRef = useRef<any>(null);
  const wrapperRef = useRef<HTMLDivElement | null>(null);

  const [suggestions, setSuggestions] = useState<any[]>([]);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isNoResult, setIsNoResult] = useState(false);

  const searchDebounceRef = useRef<number | null>(null);
  const latestSearchRequestIdRef = useRef(0);

  const moveToLocation = (lat: number, lng: number) => {
    if (!mapInstanceRef.current || !window.kakao) return;

    const position = new window.kakao.maps.LatLng(lat, lng);
    mapInstanceRef.current.setCenter(position);

    if (!markerRef.current) {
      markerRef.current = new window.kakao.maps.Marker({
        map: mapInstanceRef.current,
        position,
      });
    } else {
      markerRef.current.setPosition(position);
      markerRef.current.setMap(mapInstanceRef.current);
    }
  };

  const selectSuggestion = (place: any) => {
    const lat = Number(place.y);
    const lng = Number(place.x);
    const name = place.place_name;
    const address = place.road_address_name || place.address_name || name;
    const jibunAddress = place.address_name || '';

    onChangePlaceKeyword(address);
    moveToLocation(lat, lng);
    setSuggestions([]);
    setIsDropdownOpen(false);
    setIsNoResult(false);

    if (onSelectLocation) {
      onSelectLocation(lat, lng, name, address, jibunAddress);
    }
  };

  const normalizeAddressResults = (data: any[]) =>
    data.map((item, index) => ({
      id: `address-${index}-${item.address_name}`,
      x: item.x,
      y: item.y,
      place_name: item.road_address?.address_name || item.address_name,
      address_name: item.address_name,
      road_address_name: item.road_address?.address_name || '',
      source: 'address',
    }));

  const mergeSearchResults = (addressResults: any[], placeResults: any[]) => {
    const merged = [...addressResults, ...placeResults];

    return merged.filter(
      (item, index, array) =>
        index ===
        array.findIndex(
          (target) =>
            target.x === item.x &&
            target.y === item.y &&
            target.place_name === item.place_name,
        ),
    );
  };

  const runKeywordSearch = (
    rawKeyword: string,
    options?: { openDropdown?: boolean; moveToFirstResult?: boolean },
  ) => {
    const trimmedKeyword = rawKeyword.trim();

    if (
      !trimmedKeyword ||
      !placesServiceRef.current ||
      !geocoderRef.current ||
      !window.kakao
    ) {
      setSuggestions([]);
      setIsDropdownOpen(false);
      setIsNoResult(false);
      return;
    }

    const requestId = ++latestSearchRequestIdRef.current;
    const kakaoStatus = window.kakao.maps.services.Status;
    let addressResults: any[] = [];
    let placeResults: any[] = [];
    let completedCount = 0;

    const finalizeSearch = () => {
      completedCount += 1;

      if (completedCount < 2) {
        return;
      }

      if (requestId !== latestSearchRequestIdRef.current) {
        return;
      }

      const mergedResults = mergeSearchResults(addressResults, placeResults);

      setSuggestions(mergedResults);
      setIsNoResult(mergedResults.length === 0);
      setIsDropdownOpen(Boolean(options?.openDropdown));

      if (options?.moveToFirstResult && mergedResults[0]) {
        selectSuggestion(mergedResults[0]);
      }
    };

    geocoderRef.current.addressSearch(trimmedKeyword, (data: any[], status: string) => {
      if (requestId !== latestSearchRequestIdRef.current) return;

      if (status === kakaoStatus.OK) {
        addressResults = normalizeAddressResults(data);
      }

      finalizeSearch();
    });

    placesServiceRef.current.keywordSearch(trimmedKeyword, (data: any[], status: string) => {
      if (requestId !== latestSearchRequestIdRef.current) return;

      if (status === kakaoStatus.OK) {
        placeResults = data.map((item) => ({
          ...item,
          source: 'place',
        }));
      }

      finalizeSearch();
    });
  };

  useEffect(() => {
    if (!mapRef.current || !window.kakao) return;

    window.kakao.maps.load(() => {
      if (!mapRef.current) return;

      const map = new window.kakao.maps.Map(mapRef.current, {
        center: new window.kakao.maps.LatLng(37.5665, 126.978),
        level: 7,
      });

      mapInstanceRef.current = map;
      placesServiceRef.current = new window.kakao.maps.services.Places();
      geocoderRef.current = new window.kakao.maps.services.Geocoder();
    });
  }, []);

  useEffect(() => {
    if (
      typeof selectedLat !== 'number' ||
      typeof selectedLng !== 'number' ||
      !Number.isFinite(selectedLat) ||
      !Number.isFinite(selectedLng) ||
      selectedLat === 0 ||
      selectedLng === 0
    ) {
      return;
    }

    moveToLocation(selectedLat, selectedLng);
  }, [selectedLat, selectedLng]);

  useEffect(() => {
    if (!placeKeyword) {
      if (searchDebounceRef.current) {
        window.clearTimeout(searchDebounceRef.current);
      }
      return;
    }

    if (searchDebounceRef.current) {
      window.clearTimeout(searchDebounceRef.current);
    }

    searchDebounceRef.current = window.setTimeout(() => {
      runKeywordSearch(placeKeyword, { openDropdown: true });
    }, 250);

    return () => {
      if (searchDebounceRef.current) {
        window.clearTimeout(searchDebounceRef.current);
      }
    };
  }, [placeKeyword]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        wrapperRef.current &&
        !wrapperRef.current.contains(event.target as Node)
      ) {
        setIsDropdownOpen(false);
      }
    };

    document.addEventListener('click', handleClickOutside);
    return () => {
      document.removeEventListener('click', handleClickOutside);
    };
  }, []);

  const visibleSuggestions = placeKeyword ? suggestions : [];
  const visibleNoResult = placeKeyword ? isNoResult : false;
  const visibleDropdownOpen = placeKeyword ? isDropdownOpen : false;

  return (
    <div
      className="overflow-hidden rounded-2xl border border-[#D9DEE3] bg-white"
      ref={wrapperRef}
    >
      <div className="border-b border-[#E5E7EB] px-4 py-3">
        <div className="relative flex h-11 items-center rounded-xl border border-[#D9DEE3] bg-[#F8FAF9] px-4">
          <input
            type="text"
            value={placeKeyword}
            onChange={(e) => onChangePlaceKeyword(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                runKeywordSearch(placeKeyword, {
                  openDropdown: true,
                  moveToFirstResult: true,
                });
              }
            }}
            placeholder={placeholder}
            className="flex-1 bg-transparent text-sm text-black outline-none placeholder:text-[#9CA3AF]"
          />
          <HiOutlineMagnifyingGlass className="text-[20px] text-[#9CA3AF]" />

          {visibleDropdownOpen &&
            (visibleSuggestions.length > 0 || visibleNoResult) && (
              <div className="absolute left-0 top-full z-20 mt-2 w-full overflow-hidden rounded-2xl border border-[#E5E7EB] bg-white shadow-lg">
                {visibleNoResult ? (
                  <div className="px-4 py-3 text-sm text-[#6B7280]">
                    검색 결과가 없습니다.
                  </div>
                ) : (
                  <ul className="max-h-60 overflow-y-auto">
                    {visibleSuggestions.map((item) => (
                      <li
                        key={item.id}
                        role="button"
                        tabIndex={0}
                        className="cursor-pointer px-4 py-3 text-sm text-black hover:bg-[#F3F4F6]"
                        onClick={() => selectSuggestion(item)}
                        onKeyDown={(event) => {
                          if (event.key === 'Enter' || event.key === ' ') {
                            event.preventDefault();
                            selectSuggestion(item);
                          }
                        }}
                      >
                        <div className="font-medium">{item.place_name}</div>
                        <div className="text-xs text-[#6B7280]">
                          {item.road_address_name || item.address_name}
                        </div>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            )}
        </div>
      </div>

      <div className="h-70 w-full bg-[#F3F4F6]">
        {mapSlot ? mapSlot : <div ref={mapRef} className="h-full w-full" />}
      </div>
    </div>
  );
}
