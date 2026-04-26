'use client';

import { IoSearch } from 'react-icons/io5';
import { useEffect, useRef, useState } from 'react';
import { createPortal } from 'react-dom';
import { IoInformationCircle } from 'react-icons/io5';
import { getAiChatHistory, requestAiChat } from '@/lib/aiChat';
import { useAuthStore } from '@/store/useAuthStore';
import type { AiChatRecommendationHouse } from '@/types/api';
import UserFilterPanel, { type FilterState } from './UserFilterPanel';

const HOUSE_TYPE_ALL = [
  '오피스텔',
  '아파트',
  '단독/다가구(원룸)',
  '연립/다세대(빌라)',
];
const RENT_TYPE_ALL = ['전세', '월세'];
const FLOOR_ALL = ['반지하', '1층', '지상층'];
const FILTER_MAX_LIMITS = {
  monthlyCost: 205,
  deposit: 50100,
  floorSize: 101,
} as const;

type PlaceSuggestion = {
  id: string;
  place_name: string;
  road_address_name?: string;
  address_name?: string;
  y: string;
  x: string;
};

type SearchOverlayProps = {
  keyword: string;
  suggestions: PlaceSuggestion[];
  isDropdownOpen: boolean;
  isNoResult: boolean;
  aiFilters: FilterState;
  onKeywordChange: (value: string) => void;
  onSearch: () => void;
  onSelectSuggestion: (place: PlaceSuggestion) => void;
  onFilterSubmit: (filters: FilterState) => void;
  onAiHouseResult?: (houses: AiChatRecommendationHouse[]) => void;
  onAiCardHouseSelect?: (houseId: number) => void;
  onLatestRecommendedHousesChange?: (houses: (AiChatRecommendationHouse & { recommendationOrder: number })[]) => void;
};

type ChatTurn = {
  id: number;
  userMessage: string;
  assistantFirstParagraph: string;
  assistantCardParagraphs: string[];
  visibleFirstParagraph: boolean;
  visibleCardCount: number;
  isStreaming: boolean;
};

const normalizeSelection = (values: string[], allValues: string[]) => {
  if (values.includes('전체')) {
    return allValues;
  }

  return values;
};

const normalizeMaxFilterValue = (value: number, maxLimit: number) =>
  value >= maxLimit ? -1 : value;

const extractAiText = (value: unknown): string => {
  if (typeof value === 'string' && value.trim()) {
    return value;
  }

  if (Array.isArray(value)) {
    return value
      .map((item) => extractAiText(item))
      .filter(Boolean)
      .join('\n\n')
      .trim();
  }

  if (value && typeof value === 'object') {
    const candidate = value as Record<string, unknown>;
    const priorityKeys = [
      'answer',
      'message',
      'content',
      'response',
      'reply',
      'result',
      'text',
      'recommendation',
      'summary',
    ];

    for (const key of priorityKeys) {
      if (key in candidate) {
        const extracted = extractAiText(candidate[key]);
        if (extracted) {
          return extracted;
        }
      }
    }

    return JSON.stringify(candidate, null, 2);
  }

  return '';
};

const splitAiParagraphs = (rawText: string) => {
  const parsed = rawText
    .split(/\n\s*\n/)
    .map((paragraph) => paragraph.trim())
    .filter(Boolean);

  return {
    firstParagraph: parsed[0] ?? '',
    cardParagraphs: parsed.slice(1),
  };
};

const extractHouseIdFromParagraph = (paragraph: string) => {
  const match = paragraph.match(/\(\s*id\s*:\s*(\d+)\s*\)/i);
  if (!match) {
    return null;
  }

  const houseId = Number(match[1]);
  return Number.isFinite(houseId) && houseId > 0 ? houseId : null;
};

const extractAiHouses = (value: unknown): AiChatRecommendationHouse[] => {
  const toNumber = (input: unknown) => {
    const parsed = Number(input);
    return Number.isFinite(parsed) ? parsed : undefined;
  };

  const toString = (input: unknown) =>
    typeof input === 'string' && input.trim().length > 0 ? input : undefined;

  const normalizeHouse = (item: unknown): AiChatRecommendationHouse | null => {
    if (!item || typeof item !== 'object') {
      return null;
    }

    const raw = item as Record<string, unknown>;
    const houseId = toNumber(raw.houseId);

    if (!houseId || houseId <= 0) {
      return null;
    }

    return {
      houseId,
      thumbnailUrl: raw.thumbnailUrl == null ? null : toString(raw.thumbnailUrl),
      lat: toNumber(raw.lat),
      lng: toNumber(raw.lng),
      sidoName: toString(raw.sidoName),
      sigunguName: toString(raw.sigunguName),
      dongName: toString(raw.dongName),
      houseType: toString(raw.houseType),
      rentType: toString(raw.rentType),
      houseStatus: toString(raw.houseStatus),
      floor: toString(raw.floor),
      deposit: toNumber(raw.deposit),
      monthlyCost: toNumber(raw.monthlyCost),
      floorSize: toNumber(raw.floorSize),
    };
  };

  const targetArray = (() => {
    if (Array.isArray(value)) {
      return value;
    }

    if (value && typeof value === 'object') {
      const candidate = value as Record<string, unknown>;

      if (Array.isArray(candidate.data)) {
        return candidate.data;
      }
    }

    return [];
  })();

  return targetArray
    .map((item) => normalizeHouse(item))
    .filter((item): item is AiChatRecommendationHouse => item !== null);
};

export default function SearchOverlay({
  keyword,
  suggestions,
  isDropdownOpen,
  isNoResult,
  aiFilters,
  onKeywordChange,
  onSearch,
  onSelectSuggestion,
  onFilterSubmit,
  onAiHouseResult,
  onAiCardHouseSelect,
  onLatestRecommendedHousesChange,
}: SearchOverlayProps) {
  const isLoggedIn = useAuthStore((state) => state.isLoggedIn);
  const [activePanel, setActivePanel] = useState<'ai' | 'filter' | null>(null);
  const [chatInput, setChatInput] = useState('');
  const [chatTurns, setChatTurns] = useState<ChatTurn[]>([]);
  const [isSubmittingAi, setIsSubmittingAi] = useState(false);
  const [isLoadingHistory, setIsLoadingHistory] = useState(false);
  const [isGuideModalOpen, setIsGuideModalOpen] = useState(false);
  const [isChatInputFocused, setIsChatInputFocused] = useState(false);
  const [isClient, setIsClient] = useState(false);
  const [currentFilters, setCurrentFilters] = useState<FilterState>(aiFilters);
  const aiScrollRef = useRef<HTMLDivElement | null>(null);
  const nextTurnIdRef = useRef(1);

  useEffect(() => {
    setIsClient(true);
  }, []);

  useEffect(() => {
    setCurrentFilters(aiFilters);
  }, [aiFilters]);

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    onSearch();
  };

  const handleAiSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (isSubmittingAi) return;

    const trimmed = chatInput.trim();
    if (!trimmed) return;

    const turnId = nextTurnIdRef.current;
    nextTurnIdRef.current += 1;

    setChatTurns((prev) => [
      ...prev,
      {
        id: turnId,
        userMessage: trimmed,
        assistantFirstParagraph: '',
        assistantCardParagraphs: [],
        visibleFirstParagraph: false,
        visibleCardCount: 0,
        isStreaming: true,
      },
    ]);

    setChatInput('');
    setIsSubmittingAi(true);

    const payload = {
      query: trimmed,
      sidoName: currentFilters.sidoName,
      sigunguName: currentFilters.sigunguName,
      houseType: normalizeSelection(currentFilters.houseType, HOUSE_TYPE_ALL),
      rentType: normalizeSelection(currentFilters.rentType, RENT_TYPE_ALL),
      minMonthlyCost: currentFilters.monthlyCost.minValue,
      maxMonthlyCost: normalizeMaxFilterValue(
        currentFilters.monthlyCost.maxValue,
        FILTER_MAX_LIMITS.monthlyCost,
      ),
      minDeposit: currentFilters.deposit.minValue,
      maxDeposit: normalizeMaxFilterValue(
        currentFilters.deposit.maxValue,
        FILTER_MAX_LIMITS.deposit,
      ),
      managementCost: currentFilters.managementCost,
      minFloorSize: currentFilters.floorSize.minValue,
      maxFloorSize: normalizeMaxFilterValue(
        currentFilters.floorSize.maxValue,
        FILTER_MAX_LIMITS.floorSize,
      ),
      floor: normalizeSelection(currentFilters.floor, FLOOR_ALL),
    };

    try {
      const response = await requestAiChat(payload);
      const aiHouses = extractAiHouses(response.data);
      
      
      onAiHouseResult?.(aiHouses);

      const aiText = extractAiText(response.data).trim();
      const fallbackText = '요청은 성공했지만 AI 응답 본문을 찾지 못했습니다.';
      const { firstParagraph, cardParagraphs } = splitAiParagraphs(aiText || fallbackText);

      setChatTurns((prev) =>
        prev.map((turn) =>
          turn.id === turnId
            ? {
                ...turn,
                assistantFirstParagraph: firstParagraph,
                assistantCardParagraphs: cardParagraphs,
                visibleFirstParagraph: true,
                visibleCardCount: cardParagraphs.length,
                isStreaming: false,
              }
            : turn,
        ),
      );
    } catch (error) {
      const errorMessage =
        error instanceof Error
          ? error.message
          : 'AI 응답 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';

      setChatTurns((prev) =>
        prev.map((turn) =>
          turn.id === turnId
            ? {
                ...turn,
                assistantFirstParagraph: errorMessage,
                assistantCardParagraphs: [],
                visibleFirstParagraph: true,
                visibleCardCount: 0,
                isStreaming: false,
              }
            : turn,
        ),
      );
    } finally {
      setIsSubmittingAi(false);
    }
  };

  
  const getLatestRecommendedHouses = (): (AiChatRecommendationHouse & { recommendationOrder: number })[] => {
    if (chatTurns.length === 0) return [];
    
    const latestTurn = chatTurns[chatTurns.length - 1];
    const aiHouses = extractAiHouses({ data: latestTurn.assistantCardParagraphs });
    
    return aiHouses.map((house, index) => ({
      ...house,
      recommendationOrder: index + 1,
    }));
  };

  useEffect(() => {
    if (activePanel !== 'ai') {
      return;
    }

    if (!isLoggedIn) {
      setChatTurns([]);
      setIsLoadingHistory(false);
      nextTurnIdRef.current = 1;
      return;
    }

    let isMounted = true;

    const loadHistory = async () => {
      setIsLoadingHistory(true);

      try {
        const history = await getAiChatHistory();
        if (!isMounted) {
          return;
        }

        const mappedTurns: ChatTurn[] = history.map((item, index) => {
          const { firstParagraph, cardParagraphs } = splitAiParagraphs(item.aiMessage);
          const turnId = Number.isFinite(item.id) ? item.id : index + 1;

          return {
            id: turnId,
            userMessage: item.userMessage,
            assistantFirstParagraph: firstParagraph,
            assistantCardParagraphs: cardParagraphs,
            visibleFirstParagraph: Boolean(firstParagraph),
            visibleCardCount: cardParagraphs.length,
            isStreaming: false,
          };
        });

        setChatTurns(mappedTurns);

        const maxId = mappedTurns.reduce((acc, turn) => Math.max(acc, turn.id), 0);
        nextTurnIdRef.current = maxId + 1;
      } catch {
        if (!isMounted) {
          return;
        }

        setChatTurns([]);
      } finally {
        if (isMounted) {
          setIsLoadingHistory(false);
        }
      }
    };

    void loadHistory();

    return () => {
      isMounted = false;
    };
  }, [activePanel, isLoggedIn]);

  useEffect(() => {
    if (activePanel !== 'ai' || !aiScrollRef.current) return;

    const target = aiScrollRef.current;
    window.requestAnimationFrame(() => {
      target.scrollTo({
        top: target.scrollHeight,
        behavior: 'smooth',
      });
    });
  }, [activePanel, chatTurns]);

  useEffect(() => {
    const latestRecommendedHouses = getLatestRecommendedHouses();
    onLatestRecommendedHousesChange?.(latestRecommendedHouses);
  }, [chatTurns, onLatestRecommendedHousesChange]);

  const handleTogglePanel = (panel: 'ai' | 'filter') => {
    setActivePanel((prev) => (prev === panel ? null : panel));
  };

  const showGuideButtonInInput = !isChatInputFocused && chatInput.trim().length === 0;

  const closeGuideModal = () => {
    setIsGuideModalOpen(false);
  };

  useEffect(() => {
    if (!isGuideModalOpen) {
      return;
    }

    const handleEscKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        closeGuideModal();
      }
    };

    window.addEventListener('keydown', handleEscKey);
    return () => {
      window.removeEventListener('keydown', handleEscKey);
    };
  }, [isGuideModalOpen]);

  return (
    <div className="flex h-full w-[400px] max-w-[calc(100vw-24px)] flex-col">
      <div className="relative w-full shrink-0">
        <form
          onSubmit={handleSubmit}
          className="flex w-full overflow-hidden rounded-t-[24px] border border-primary-100 bg-white"
        >
          <input
            type="text"
            value={keyword}
            onChange={(e) => onKeywordChange(e.target.value)}
            placeholder="아파트, 지역, 지하철역, 학교 검색"
            className="h-[68px] flex-1 border-none bg-white px-7 text-[18px] text-gray-800 outline-none placeholder:text-[#a7b0b7]"
          />

          <button
            type="submit"
            className="flex h-[68px] w-[82px] shrink-0 items-center justify-center bg-primary-100 text-white transition hover:opacity-90"
            aria-label="검색"
          >
            <IoSearch size={34} />
          </button>
        </form>

        {isDropdownOpen && (
          <div className="absolute left-0 right-0 top-[76px] z-30 w-full overflow-hidden rounded-[16px] border border-primary-100 bg-white shadow-[0_10px_30px_rgba(0,0,0,0.12)]">
            {isNoResult ? (
              <div className="px-5 py-4 text-[15px] text-gray-500">검색 결과 없음</div>
            ) : (
              <ul className="max-h-[280px] overflow-y-auto py-2">
                {suggestions.map((place) => (
                  <li key={place.id}>
                    <button
                      type="button"
                      onClick={() => onSelectSuggestion(place)}
                      className="flex w-full flex-col items-start px-5 py-3 text-left transition hover:bg-background-100"
                    >
                      <span className="text-[15px] font-semibold text-gray-900">{place.place_name}</span>
                      <span className="mt-1 text-[13px] text-gray-500">
                        {place.road_address_name || place.address_name}
                      </span>
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}
      </div>

      <div className="flex w-full shrink-0 overflow-hidden border-x border-b border-primary-100 bg-white shadow-[0_8px_24px_rgba(0,0,0,0.08)]">
        <button
          type="button"
          onClick={() => handleTogglePanel('ai')}
          className={`flex h-[54px] flex-1 items-center justify-center border-r border-[#D9DEE3] text-[16px] font-medium transition-all duration-200 ${
            activePanel === 'ai' ? 'bg-primary-100/10 text-primary-700' : 'text-primary-600 hover:bg-background-100'
          }`}
        >
          AI 대화
        </button>

        <button
          type="button"
          onClick={() => handleTogglePanel('filter')}
          className={`flex h-[54px] flex-1 items-center justify-center text-[16px] font-medium transition-all duration-200 ${
            activePanel === 'filter' ? 'bg-primary-100/10 text-primary-700' : 'text-primary-600 hover:bg-background-100'
          }`}
        >
          검색 필터
        </button>
      </div>

      {activePanel === 'filter' && (
        <UserFilterPanel
          initialFilters={currentFilters}
          onSubmit={onFilterSubmit}
          onChange={setCurrentFilters}
        />
      )}

      {activePanel === 'ai' && (
        <div className="flex min-h-0 w-full flex-1 flex-col overflow-hidden rounded-b-[20px] border-x border-b border-primary-100 bg-white shadow-[0_8px_24px_rgba(0,0,0,0.08)]">
          <div ref={aiScrollRef} className="min-h-0 flex-1 space-y-4 overflow-y-auto px-5 py-5">
            {isLoadingHistory && (
              <p className="text-[14px] leading-6 text-gray-500">이전 대화를 불러오는 중입니다...</p>
            )}

            {!isLoadingHistory && chatTurns.length === 0 && (
              <p className="text-[14px] leading-6 text-gray-500">조건을 입력하면 AI가 추천 이유를 정리해드려요.</p>
            )}

            {chatTurns.map((turn) => (
              <div key={turn.id} className="space-y-4">
                <div className="flex justify-end">
                  <div className="max-w-[85%] rounded-2xl rounded-tr-sm bg-primary-100 px-4 py-3 text-[14px] leading-6 text-white">
                    {turn.userMessage}
                  </div>
                </div>

                {turn.visibleFirstParagraph && (
                  <div className="flex justify-start">
                    <div className="max-w-[90%] rounded-2xl rounded-tl-sm border border-[#D9DEE3] bg-[#F8FAFC] px-4 py-3 text-[14px] leading-6 text-gray-800">
                      {turn.assistantFirstParagraph}
                    </div>
                  </div>
                )}

                {turn.assistantCardParagraphs.length > 0 && (
                  <div className="space-y-3">
                    {turn.assistantCardParagraphs.slice(0, turn.visibleCardCount).map((paragraph, index) => (
                      (() => {
                        const houseId = extractHouseIdFromParagraph(paragraph);
                        const isClickable = houseId !== null;

                        return (
                          <article
                            key={`${turn.id}-${index}-${paragraph.slice(0, 20)}`}
                            className={`rounded-2xl border border-[#D9DEE3] bg-white p-4 ${
                              isClickable ? 'cursor-pointer transition hover:border-primary-300 hover:bg-primary-50/30' : ''
                            }`}
                            onClick={() => {
                              if (!isClickable || !onAiCardHouseSelect || !houseId) return;
                              onAiCardHouseSelect(houseId);
                            }}
                            onKeyDown={(event) => {
                              if (!isClickable || !onAiCardHouseSelect || !houseId) return;
                              if (event.key === 'Enter' || event.key === ' ') {
                                event.preventDefault();
                                onAiCardHouseSelect(houseId);
                              }
                            }}
                            role={isClickable ? 'button' : undefined}
                            tabIndex={isClickable ? 0 : undefined}
                            aria-label={isClickable ? `매물 ${houseId} 상세 보기` : undefined}
                          >
                            <p className="text-[14px] leading-6 text-gray-800">{paragraph}</p>
                          </article>
                        );
                      })()
                    ))}
                  </div>
                )}

                {turn.isStreaming && (
                  <div className="flex justify-start">
                    <div className="inline-flex items-center gap-1 rounded-full border border-[#D9DEE3] bg-[#F8FAFC] px-3 py-2">
                      <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-gray-400" />
                      <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-gray-400 [animation-delay:150ms]" />
                      <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-gray-400 [animation-delay:300ms]" />
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>

          <form onSubmit={handleAiSubmit} className="mt-auto flex items-center gap-2 border-t border-[#E5E7EB] px-4 py-3">
            <div className="relative flex-1">
              <input
                type="text"
                value={chatInput}
                onChange={(e) => setChatInput(e.target.value)}
                onFocus={() => setIsChatInputFocused(true)}
                onBlur={() => setIsChatInputFocused(false)}
                placeholder=""
                className={`h-11 w-full rounded-xl border border-[#D9DEE3] px-3 text-[14px] text-gray-800 outline-none transition focus:border-primary-300 ${
                  showGuideButtonInInput ? 'pr-10' : 'pr-3'
                }`}
              />

              {showGuideButtonInInput && (
                <button
                  type="button"
                  onClick={() => setIsGuideModalOpen(true)}
                  className="absolute right-2 top-1/2 -translate-y-1/2 cursor-pointer text-neutral-300 transition hover:text-neutral-500"
                  aria-label="스마트 검색 조건 안내 열기"
                  title="검색 조건 안내"
                >
                  <IoInformationCircle className="text-[26px]" />
                </button>
              )}
            </div>
            <button
              type="submit"
              disabled={isSubmittingAi}
              className="h-11 shrink-0 rounded-xl bg-primary-100 px-4 text-[14px] font-semibold text-white transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
            >
              전송
            </button>
          </form>
        </div>
      )}

      {isClient &&
        isGuideModalOpen &&
        createPortal(
          <div
            className="fixed inset-0 z-[9999] flex items-center justify-center bg-black/45 px-4"
            role="dialog"
            aria-modal="true"
            aria-label="스마트 매물 맞춤 검색 조건 안내"
            onClick={closeGuideModal}
          >
            <div
              className="max-h-[78vh] w-full max-w-[520px] overflow-y-auto rounded-2xl bg-white p-4 shadow-2xl"
              onClick={(event) => event.stopPropagation()}
            >
              <div className="mb-3 flex items-start justify-between gap-3">
                <h2 className="text-[18px] font-bold text-gray-900">검색 조건 안내</h2>
                <button
                  type="button"
                  onClick={closeGuideModal}
                  className="rounded-md border border-gray-200 px-2.5 py-1 text-[12px] font-medium text-gray-600 hover:bg-gray-100"
                >
                  닫기
                </button>
              </div>

              <p className="mb-3 text-[13px] leading-5 text-gray-600">
                대화에 아래 조건을 같이 넣으면 추천 정확도가 올라갑니다.
              </p>

              <section className="rounded-lg border border-[#E5E7EB] bg-[#F8FAFC] p-3">
                <h3 className="text-[15px] font-bold text-gray-900">주변 인프라</h3>
                <ul className="mt-1 list-disc space-y-1 pl-4 text-[13px] leading-5 text-gray-700">
                  <li>확인 항목: 버스, 지하철, 편의점, 세탁소, 카페, 병원, 약국</li>
                  <li>예시 데이터: 800m 내 52개, 최단 거리 231m</li>
                </ul>
                <p className="mt-2 rounded-md bg-blue-50 px-2 py-1.5 text-[12px] leading-5 text-blue-900">
                  예시: “강남구에서 병원이 많고, 편의점 거리가 50m 이내인 매물 추천해줘”
                </p>
              </section>

              <section className="mt-3 rounded-lg border border-[#E5E7EB] bg-[#F8FAFC] p-3">
                <h3 className="text-[15px] font-bold text-gray-900">출퇴근 정보</h3>
                <p className="mt-1 text-[13px] leading-5 text-gray-700">
                  실제 대중교통 기준으로 소요시간, 거리, 환승, 요금을 함께 확인합니다.
                </p>
                <div className="mt-2 grid grid-cols-2 gap-2 text-[12px] text-gray-800">
                  <div className="rounded-md border border-gray-200 bg-white px-2 py-1.5">소요 시간: 약 3분</div>
                  <div className="rounded-md border border-gray-200 bg-white px-2 py-1.5">이동 거리: 1.3km</div>
                  <div className="rounded-md border border-gray-200 bg-white px-2 py-1.5">환승 횟수: 1회</div>
                  <div className="rounded-md border border-gray-200 bg-white px-2 py-1.5">이용 요금: 1,550원</div>
                </div>
                <p className="mt-2 rounded-md bg-blue-50 px-2 py-1.5 text-[12px] leading-5 text-blue-900">
                  예시: “직장이 서울특별시 강남구 테헤란로인데, 30분 이내·1,500원 이내 매물 추천해줘”
                </p>
              </section>

              <p className="mt-3 rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-2 text-[12px] leading-5 text-emerald-900">
                인프라 조건과 출퇴근 조건을 같이 넣으면 결과가 더 안정적입니다.
              </p>
            </div>
          </div>,
          document.body,
        )}
    </div>
  );
}
