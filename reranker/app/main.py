import math
import os
from typing import List, Optional

from fastapi import FastAPI
from pydantic import BaseModel, Field
import torch
from transformers import AutoModelForSequenceClassification, AutoTokenizer


app = FastAPI(title="banggok-reranker")
MODEL_NAME = os.getenv("RERANKER_MODEL_NAME", "BAAI/bge-reranker-v2-m3")
MAX_LENGTH = int(os.getenv("RERANKER_MAX_LENGTH", "512"))

tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)
model = AutoModelForSequenceClassification.from_pretrained(MODEL_NAME)
model.eval()


class Candidate(BaseModel):
    id: str
    text: str
    originalScore: Optional[float] = None


class RerankRequest(BaseModel):
    query: str
    candidates: List[Candidate]
    topK: Optional[int] = Field(default=None, ge=1)


class RerankResult(BaseModel):
    id: str
    score: float
    rank: int


class RerankResponse(BaseModel):
    results: List[RerankResult]


@app.get("/api/v1/health")
def health() -> dict[str, str]:
    return {"status": "ok", "model": MODEL_NAME}


@app.post("/api/v1/rerank", response_model=RerankResponse)
def rerank(request: RerankRequest) -> RerankResponse:
    pairs = [[request.query, candidate.text] for candidate in request.candidates]
    scored_results = score_pairs(pairs, request.candidates)

    scored_results.sort(key=lambda item: item[1], reverse=True)
    top_k = request.topK or len(scored_results)

    return RerankResponse(
        results=[
            RerankResult(id=candidate_id, score=score, rank=index + 1)
            for index, (candidate_id, score) in enumerate(scored_results[:top_k])
        ]
    )


def score_pairs(pairs: List[List[str]], candidates: List[Candidate]) -> List[tuple[str, float]]:
    with torch.no_grad():
        inputs = tokenizer(
            pairs,
            padding=True,
            truncation=True,
            return_tensors="pt",
            max_length=MAX_LENGTH,
        )
        scores = model(**inputs, return_dict=True).logits.view(-1).float().tolist()

    scored_results = []
    for candidate, score in zip(candidates, scores):
        normalized_score = 1 / (1 + math.exp(-score))
        original_score = candidate.originalScore or 0.0
        final_score = round((normalized_score * 0.9) + (original_score * 0.1), 2)
        scored_results.append((candidate.id, final_score))

    return scored_results
