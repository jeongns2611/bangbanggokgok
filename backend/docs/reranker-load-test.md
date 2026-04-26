# Reranker Load Test

로컬에서 띄운 FastAPI reranker에 동시 요청을 보내 평균 응답시간, P95, P99, 처리량을 확인한다.

## 실행 전

reranker 서버가 먼저 떠 있어야 한다.

```powershell
Invoke-WebRequest -UseBasicParsing http://localhost:8000/api/v1/health
```

정상이면 `200 OK` 와 `{"status":"ok", ...}` 가 나온다.

## 기본 실행

```powershell
cd C:\Users\SSAFY\Desktop\ssafy_git\S14P21A104\backend
python .\scripts\reranker_load_test.py
```

기본값:

- `requests=20`
- `concurrency=5`
- `candidateCount=10`
- `topK=10`

## 예시

동시 10개 요청으로 총 50번 호출:

```powershell
python .\scripts\reranker_load_test.py --concurrency 10 --requests 50
```

후보 수 20개를 보고 싶으면 테스트 데이터부터 늘려야 한다. 현재 스크립트 기본 샘플은 10개 후보까지만 포함한다.

## 출력 의미

- `totalElapsedMs`: 전체 부하 테스트 완료 시간
- `successCount`: 성공한 요청 수
- `failureCount`: 실패한 요청 수
- `throughputRps`: 초당 처리 요청 수
- `avgLatencyMs`: 평균 응답 시간
- `p50LatencyMs`: 중앙값
- `p95LatencyMs`: 상위 95% 지점 응답 시간
- `p99LatencyMs`: 상위 99% 지점 응답 시간
- `sampleTopResultId`: 성공 응답 중 샘플 1개의 1위 후보 ID
- `sampleTopResultScore`: 그 샘플의 1위 점수

## 권장 비교 시나리오

아래처럼 단계별로 돌리면 병목이 잘 보인다.

```powershell
python .\scripts\reranker_load_test.py --concurrency 1 --requests 10
python .\scripts\reranker_load_test.py --concurrency 5 --requests 20
python .\scripts\reranker_load_test.py --concurrency 10 --requests 50
python .\scripts\reranker_load_test.py --concurrency 20 --requests 100 --timeout 60
```

해석 기준:

- `avgLatencyMs`, `p95LatencyMs`가 급격히 오르면 대기열이 쌓이는 상태다.
- `failureCount`가 생기면 타임아웃이나 서버 오류를 의심해야 한다.
- `throughputRps`가 더 이상 늘지 않으면 현재 서버 처리 한계에 도달한 것이다.
