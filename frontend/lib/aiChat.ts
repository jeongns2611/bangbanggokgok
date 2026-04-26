import { fetchWithAuth } from '@/lib/fetchWithAuth';
import type {
  AiChatHistoryItem,
  AiChatRequest,
  AiChatResponseData,
  ApiResponse,
} from '@/types/api';

export async function requestAiChat(
  payload: AiChatRequest,
): Promise<ApiResponse<AiChatResponseData>> {
  

  const queryParams = new URLSearchParams({
    reranked: 'false',
    test: 'false',
  });

  const response = await fetchWithAuth(`/api/ai/chat?${queryParams.toString()}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });

  const result = (await response.json().catch(() => null)) as ApiResponse<AiChatResponseData> | null;

  
  

  if (!response.ok || !result) {
    const message =
      result && typeof result === 'object' && 'message' in result
        ? String(result.message)
        : 'AI 채팅 요청에 실패했습니다.';

    throw new Error(message);
  }

  return result;
}

export async function getAiChatHistory(): Promise<AiChatHistoryItem[]> {
  const response = await fetchWithAuth('/api/ai/chat', {
    method: 'GET',
  });

  const result = (await response.json().catch(() => null)) as
    | ApiResponse<unknown>
    | null;

  if (!response.ok || !result) {
    const message =
      result && typeof result === 'object' && 'message' in result
        ? String(result.message)
        : 'AI 채팅 히스토리 조회에 실패했습니다.';
    throw new Error(message);
  }

  const data = result.data;
  if (!Array.isArray(data)) {
    return [];
  }

  const historyItems: AiChatHistoryItem[] = [];

  data.forEach((item) => {
    if (!item || typeof item !== 'object') {
      return;
    }

    const typed = item as Record<string, unknown>;
    const id = Number(typed.id);
    const userMessage = typeof typed.userMessage === 'string' ? typed.userMessage : '';
    const aiMessage = typeof typed.aiMessage === 'string' ? typed.aiMessage : '';

    if (!Number.isFinite(id) || !userMessage || !aiMessage) {
      return;
    }

    historyItems.push({
      id,
      userMessage,
      aiMessage,
      createdAt: typeof typed.createdAt === 'string' ? typed.createdAt : undefined,
      updatedAt: typeof typed.updatedAt === 'string' ? typed.updatedAt : undefined,
    });
  });

  return historyItems;
}
