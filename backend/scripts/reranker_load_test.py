import argparse
import json
import math
import statistics
import time
import urllib.error
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed


DEFAULT_QUERY = "강남역까지 30분 이내 출퇴근 가능하고 편의점이 가깝고 월세 80 이하인 원룸"
DEFAULT_CANDIDATES = [
    {"id": "house-101", "text": "역삼역 도보 8분, 강남역 지하철 14분, 편의점 1분, 월세 75, 원룸 22m2", "originalScore": 0.81},
    {"id": "house-102", "text": "선릉역 도보 5분, 강남역 지하철 18분, 편의점 3분, 월세 78, 투룸 31m2", "originalScore": 0.76},
    {"id": "house-103", "text": "강남역 버스 18분, 편의점 30초, 월세 72, 오피스텔 원룸 24m2", "originalScore": 0.79},
    {"id": "house-104", "text": "신논현역 도보 10분, 강남역 도보 15분, 편의점 2분, 월세 83, 원룸 20m2", "originalScore": 0.84},
    {"id": "house-105", "text": "양재역 도보 6분, 강남역 지하철 9분, 편의점 4분, 월세 68, 원룸 19m2", "originalScore": 0.74},
    {"id": "house-106", "text": "사당역 도보 4분, 강남역 지하철 24분, 편의점 1분, 월세 62, 원룸 18m2", "originalScore": 0.71},
    {"id": "house-107", "text": "건대입구역 도보 7분, 강남역 지하철 33분, 편의점 2분, 월세 70, 원룸 23m2", "originalScore": 0.73},
    {"id": "house-108", "text": "강남역 도보 11분, 편의점 5분, 월세 88, 원룸 21m2", "originalScore": 0.86},
    {"id": "house-109", "text": "교대역 도보 9분, 강남역 지하철 6분, 편의점 1분, 월세 79, 원룸 20m2", "originalScore": 0.82},
    {"id": "house-110", "text": "서울대입구역 도보 3분, 강남역 버스 28분, 편의점 30초, 월세 65, 원룸 17m2", "originalScore": 0.69},
]


def percentile(sorted_values: list[float], p: float) -> float:
    if not sorted_values:
        return 0.0
    if len(sorted_values) == 1:
        return sorted_values[0]
    rank = (len(sorted_values) - 1) * p
    low = math.floor(rank)
    high = math.ceil(rank)
    if low == high:
        return sorted_values[low]
    low_value = sorted_values[low]
    high_value = sorted_values[high]
    return low_value + (high_value - low_value) * (rank - low)


def build_payload(top_k: int, candidate_count: int) -> bytes:
    candidates = DEFAULT_CANDIDATES[:candidate_count]
    payload = {
        "query": DEFAULT_QUERY,
        "candidates": candidates,
        "topK": min(top_k, len(candidates)),
    }
    return json.dumps(payload, ensure_ascii=False).encode("utf-8")


def send_request(url: str, payload: bytes, timeout: float) -> dict:
    request = urllib.request.Request(
        url=url,
        data=payload,
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    started_at = time.perf_counter()
    try:
        with urllib.request.urlopen(request, timeout=timeout) as response:
            body = response.read().decode("utf-8")
            elapsed_ms = (time.perf_counter() - started_at) * 1000
            parsed = json.loads(body)
            top_result = parsed.get("results", [{}])[0] if parsed.get("results") else {}
            return {
                "ok": True,
                "status": response.status,
                "elapsed_ms": elapsed_ms,
                "top_id": top_result.get("id"),
                "top_score": top_result.get("score"),
            }
    except urllib.error.HTTPError as error:
        elapsed_ms = (time.perf_counter() - started_at) * 1000
        return {
            "ok": False,
            "status": error.code,
            "elapsed_ms": elapsed_ms,
            "error": error.read().decode("utf-8", errors="replace"),
        }
    except Exception as error:  # noqa: BLE001
        elapsed_ms = (time.perf_counter() - started_at) * 1000
        return {
            "ok": False,
            "status": None,
            "elapsed_ms": elapsed_ms,
            "error": str(error),
        }


def run_load_test(base_url: str, concurrency: int, requests_count: int, timeout: float, top_k: int, candidate_count: int) -> None:
    url = base_url.rstrip("/") + "/api/v1/rerank"
    payload = build_payload(top_k=top_k, candidate_count=candidate_count)

    print(f"baseUrl={base_url}")
    print(f"requests={requests_count}")
    print(f"concurrency={concurrency}")
    print(f"candidateCount={candidate_count}")
    print(f"topK={min(top_k, candidate_count)}")

    started_at = time.perf_counter()
    results = []

    with ThreadPoolExecutor(max_workers=concurrency) as executor:
        futures = [executor.submit(send_request, url, payload, timeout) for _ in range(requests_count)]
        for future in as_completed(futures):
            results.append(future.result())

    total_elapsed_ms = (time.perf_counter() - started_at) * 1000
    successes = [result for result in results if result["ok"]]
    failures = [result for result in results if not result["ok"]]
    latencies = sorted(result["elapsed_ms"] for result in results)

    print(f"totalElapsedMs={total_elapsed_ms:.2f}")
    print(f"successCount={len(successes)}")
    print(f"failureCount={len(failures)}")
    print(f"throughputRps={requests_count / (total_elapsed_ms / 1000):.2f}")
    print(f"avgLatencyMs={statistics.fmean(latencies):.2f}")
    print(f"minLatencyMs={latencies[0]:.2f}")
    print(f"p50LatencyMs={percentile(latencies, 0.50):.2f}")
    print(f"p95LatencyMs={percentile(latencies, 0.95):.2f}")
    print(f"p99LatencyMs={percentile(latencies, 0.99):.2f}")
    print(f"maxLatencyMs={latencies[-1]:.2f}")

    if successes:
        sample = successes[0]
        print(f"sampleTopResultId={sample['top_id']}")
        print(f"sampleTopResultScore={sample['top_score']}")

    if failures:
        print("failures:")
        for index, failure in enumerate(failures[:5], start=1):
            print(
                f"  {index}. status={failure['status']} elapsedMs={failure['elapsed_ms']:.2f} "
                f"error={failure['error']}"
            )


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Simple concurrent load test for the reranker FastAPI server.")
    parser.add_argument("--base-url", default="http://localhost:8000", help="Base URL of reranker server")
    parser.add_argument("--concurrency", type=int, default=5, help="Number of concurrent workers")
    parser.add_argument("--requests", type=int, default=20, help="Total request count")
    parser.add_argument("--timeout", type=float, default=30.0, help="Per-request timeout in seconds")
    parser.add_argument("--top-k", type=int, default=10, help="Requested topK")
    parser.add_argument("--candidate-count", type=int, default=10, help="How many candidates to include")
    return parser.parse_args()


if __name__ == "__main__":
    arguments = parse_args()
    run_load_test(
        base_url=arguments.base_url,
        concurrency=arguments.concurrency,
        requests_count=arguments.requests,
        timeout=arguments.timeout,
        top_k=arguments.top_k,
        candidate_count=arguments.candidate_count,
    )
