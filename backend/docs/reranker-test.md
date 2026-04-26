# Reranker Test

## 1. 응답 형식 테스트

FastAPI 서버 없이도 백엔드 `FastApiRerankerClient`가 어떤 요청을 보내고 어떤 응답을 받는지 확인할 수 있다.

```powershell
$env:GRADLE_USER_HOME='.gradle-home-reranker-test'
.\gradlew.bat --no-daemon test --tests "com.ssafy.backend.domain.ai.service.FastApiRerankerClientTest"
```

검증 내용:

- 요청 URL이 `/api/v1/rerank`인지
- 요청 JSON에 `query`, `candidates`, `topK`가 들어가는지
- 응답 JSON `results[].id`, `results[].score`, `results[].rank`가 `RerankResponse`로 잘 매핑되는지

## 2. 실제 reranker 응답/속도 테스트

먼저 FastAPI reranker를 띄운다. 기본 주소는 `http://localhost:8000`이다.

예시:

```powershell
cd ..\reranker
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```

그다음 백엔드에서 수동 스모크 테스트를 돌린다.

```powershell
cd ..\backend
$env:RERANKER_BASE_URL='http://localhost:8000'
$env:GRADLE_USER_HOME='.gradle-home-reranker-manual'
.\gradlew.bat --no-daemon test --tests "com.ssafy.backend.domain.ai.service.FastApiRerankerManualTest" -Dreranker.manual.enabled=true -Dreranker.manual.iterations=5
```

출력 예시:

```text
RERANKER_BASE_URL=http://localhost:8000
iterations=5
avgLatencyMs=128
response=RerankResponse[results=[Result[id=house-101, score=0.9142, rank=1], Result[id=house-103, score=0.8871, rank=2], Result[id=house-102, score=0.8014, rank=3]]]
```

## 3. 실제 응답 형태

백엔드가 기대하는 reranker 응답 형태는 아래와 같다.

```json
{
  "results": [
    { "id": "house-101", "score": 0.9142, "rank": 1 },
    { "id": "house-103", "score": 0.8871, "rank": 2 }
  ]
}
```

## 4. 참고

- 첫 요청은 모델 로딩 때문에 매우 느릴 수 있다.
- `avgLatencyMs`는 테스트 요청 자체 기준 평균 응답 시간이다.
- 현재 수동 테스트는 고정 샘플 3건으로 요청한다. 후보 개수를 늘리고 싶으면 `FastApiRerankerManualTest`의 샘플 데이터를 바꾸면 된다.
