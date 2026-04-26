import { fetchWithAuth } from '@/lib/fetchWithAuth';
import type {
  HouseImagePayload,
  PresignedUrlItem,
  PresignedUrlRequest,
} from '@/types/api';


function getContentType(file: File): string {
  if (file.type) return file.type;

  const ext = file.name.split('.').pop()?.toLowerCase() ?? '';
  const map: Record<string, string> = {
    jpg: 'image/jpeg',
    jpeg: 'image/jpeg',
    png: 'image/png',
    webp: 'image/webp',
    gif: 'image/gif',
    svg: 'image/svg+xml',
  };
  return map[ext] ?? 'application/octet-stream';
}


export async function requestPresignedUrls(
  requests: PresignedUrlRequest[],
): Promise<PresignedUrlItem[]> {
  

  const res = await fetchWithAuth('/api/houses/presigned-url', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(requests),
  });

  

  if (!res.ok) {
    const err = await res.json().catch(() => null);
    
    throw new Error(
      err?.message ?? `Presigned URL 요청 실패 (status: ${res.status})`,
    );
  }

  const result = await res.json();
  
  
  const items: unknown = result?.data ?? result;
  if (!Array.isArray(items)) {
    throw new Error('Presigned URL 응답 형식이 올바르지 않습니다.');
  }
  return items as PresignedUrlItem[];
}


export async function uploadImageToS3(
  presignedUrl: string,
  file: File,
  contentType: string,
): Promise<void> {
  

  const res = await fetch(presignedUrl, {
    method: 'PUT',
    headers: { 'Content-Type': contentType },
    body: file,
  });

  

  if (!res.ok) {
    const rawError = await res.text().catch(() => '');
    const compactError = rawError.replace(/\s+/g, ' ').trim();
    
    throw new Error(
      `S3 업로드 실패: ${file.name} (status: ${res.status}) ${compactError}`,
    );
  }
}

export type ImageUploadInput = {
  file: File;
  isThumbnail: boolean;
};


export async function uploadHouseImages(
  images: ImageUploadInput[],
): Promise<HouseImagePayload[]> {
  if (images.length === 0) return [];

  

  const contentTypes = images.map(({ file }) => getContentType(file));

  
  const requests: PresignedUrlRequest[] = images.map(({ file, isThumbnail }, index) => ({
    fileName: file.name,
    contentType: contentTypes[index],
    isThumbnail,
  }));

  
  const presignedItems = await requestPresignedUrls(requests);

  if (presignedItems.length !== images.length) {
    throw new Error('Presigned URL 개수가 이미지 개수와 일치하지 않습니다.');
  }

  

  const imagePayloads: HouseImagePayload[] = presignedItems.map(({ objectKey, isThumbnail }) => ({
    objectKey,
    isThumbnail,
  }));

  
  const uploadResults = await Promise.allSettled(
    images.map(({ file }, index) => {
      const { url, objectKey } = presignedItems[index];
      if (!url || !objectKey) {
        throw new Error(`Presigned URL 항목이 올바르지 않습니다 (index: ${index})`);
      }

      return uploadImageToS3(url, file, contentTypes[index]);
    }),
  );

  const failedUploads = uploadResults
    .map((result, index) => ({ result, index }))
    .filter((entry): entry is { result: PromiseRejectedResult; index: number } => entry.result.status === 'rejected');

  if (failedUploads.length > 0) {
    const firstFailure = failedUploads[0];
    const failedObjectKey = presignedItems[firstFailure.index]?.objectKey;
    
    throw new Error(
      `이미지 업로드 실패: objectKey=${failedObjectKey}, reason=${String(firstFailure.result.reason)}`,
    );
  }

  

  
  return imagePayloads;
}
