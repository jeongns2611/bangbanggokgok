package com.ssafy.backend.batch.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.backend.batch.dto.HouseContent;
import com.ssafy.backend.batch.entity.OutboxStatus;
import com.ssafy.backend.batch.entity.VectorOutbox;
import com.ssafy.backend.batch.repository.OutboxRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchService {
    // 변경됨: AiConfig에 등록된 Bean 이름("gpt5NanoChatClient")과 일치시켜 주입받습니다.
    private final ChatClient chatClient;

    private final VectorStore vectorStore; // VectorStore 주입

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    private final int BATCH_SIZE = 5; // 배치 처리할 데이터 수
    private final int MAX_BATCH_LOOPS = 2; // 하루 최대 배치 실행 횟수

    @Value("classpath:/prompt/sementic_chunk_system.txt")
    private Resource sementicChunkSystemPrompt;
    @Value("classpath:/prompt/sementic_chunk_user.txt")
    private Resource sementicChunkuserPrompt;

    public static SementicResults createStaticResults() {
        List<SementicItem> items = List.of(
                new SementicItem(1L, "통창 시티뷰와 신축급 컨디션의 호텔식 풀옵션 오피스텔. 루프탑 가든·헬스장 이용 가능해 워라밸에 적합, 강남권 출퇴근 수요에 유리. 서울 강남구 논현로, 전세 2억8000. 전용 42.1㎡(약 13평)로 10평대 방 찾는 분 추천. 지상층 매물, 2022년 준공. 관리비 12만(수도·전기), 월세 0으로 한 달 총 고정 지출 12만. 고정 지출 관리 용이."),
                new SementicItem(2L, "강남역 도보 3분 초역세권 오피스텔, 고층 통창 시티뷰와 화이트톤 인테리어·수납 강화로 1인 미니멀 라이프에 적합. 강남권 출퇴근 및 테헤란로 인근 업무지 접근 좋음. 서울 강남구 강남대로, 보증금 1000/월세 120. 전용 26.4㎡(약 8평). 지상층 매물, 2021년 준공. 관리비 15만(수도·전기) 포함, 한 달 총 고정 지출 135만으로 고정 지출 관리 기준 명확."),
                new SementicItem(3L, "조용한 주택가의 넓은 투룸 전세, 주방·거실 분리 구조로 요리하기 좋고 남향 채광이 하루 종일 들어 평온한 휴식에 적합. 강남권 출퇴근 동선에 유리하며 테헤란로 인근 업무지 접근도 무난. 서울 강남구 역삼로, 전세 3억5000. 전용 49.5㎡(약 15평)로 10평대 방+거실 여유. 지상층 매물, 2018년 준공. 관리비 7만(수도·전기), 월세 0으로 한 달 총 고정 지출 7만, 고정 지출 관리 최적."),
                new SementicItem(4L, "프라이빗 옥탑형 원룸으로 단독 테라스에서 홈카페·바비큐 가능, 강남 한복판 희소성 있는 아지트. 강남권 출퇴근에 좋고 테헤란로 인근 이동도 편한 입지. 서울 강남구 논현로, 보증금 500/월세 55. 전용 19.8㎡(약 6평) 소형이지만 지상층 매물로 접근성 무난. 2005년 준공. 관리비 3만(수도·전기) 별도, 한 달 총 고정 지출 58만으로 고정 지출 관리 쉬움."),
                new SementicItem(5L, "몸만 들어오는 프리미엄 풀옵션 단기 거주형 오피스텔. 호텔급 침구·최신 가전 완비, 보안 철저해 여성 1인도 안심. 테헤란로 인근 핵심 업무지라 강남권 출퇴근 최적. 서울 강남구 테헤란로, 보증금 150/월세 150. 전용 30.2㎡(약 9평)로 10평대 방 찾는 수요에 근접. 지상층 매물, 2023년 준공. 관리비 10만(수도·전기) 포함, 한 달 총 고정 지출 160만으로 고정 지출 관리 기준 제공."),
                new SementicItem(6L, "명품 학군과 생활 인프라를 갖춘 대단지 아파트 전세. 강남권 출퇴근 수요에 적합하며 지상층 매물로 선호도 높음. 서울 강남구 선릉로, 2012년 준공. 전세 9억5천, 월세 0. 전용 84.9㎡(약 25.7평). 관리비 25만(수도·전기)로 한 달 총 고정 지출 25만, 고정 지출 관리 용이. 남향 채광 기대, 테헤란로 인근 접근성도 좋음."),
                new SementicItem(7L, "강남 가성비 리모델링 월세로, 반지하 고정관념을 깨는 화사한 조명·제습 완비로 쾌적함 강조. 사회초년생 강남권 출퇴근에 유리. 서울 강남구 언주로, 1998년 준공. 보증금 2천/월 45. 전용 28.5㎡(약 8.6평)로 10평대 방 대안. 관리비 5만(수도·전기) 포함 한 달 총 고정 지출 50만, 고정 지출 관리에 강점. 테헤란로 인근 이동도 편리."),
                new SementicItem(8L, "층고 높은 복층 오피스텔 월세, 아래층 거실·업무공간+복층 침실로 분리돼 재택/업무에 최적. 그레이톤 인테리어와 숨은 수납으로 공간 활용도 높음. 서울 강남구 봉은사로, 2019년 준공, 지상층 매물. 보증금 5천/월 90. 전용 38.8㎡(약 11.7평)로 10평대 방 찾는 수요 적합. 관리비 13만(수도·전기) 포함 한 달 총 고정 지출 103만, 강남권 출퇴근·테헤란로 인근 접근성 우수."),
                new SementicItem(9L, "정겨운 골목 안쪽 1층 원룸 전세, 작은 마당 공유하며 구옥의 아늑함과 튼튼한 구조가 장점. 최근 도배·장판 교체로 깔끔. 서울 강남구 테헤란로로 테헤란로 인근 직장 강남권 출퇴근에 유리. 전세 1억8천, 월세 0. 전용 35.1㎡(약 10.6평)로 10평대 방 실사용 넉넉. 관리비 2만(수도·전기) 포함 한 달 총 고정 지출 2만, 고정 지출 관리 최상. 1층이라 지상층 매물 수요와도 맞음."),
                new SementicItem(10L, "반려동물 친화 빌라 월세, 상층부로 층간소음 걱정 적고 현관과 연결된 입주민 전용 옥상정원 산책 동선이 강점. 노출콘크리트+우드톤, 남향 채광과 시티뷰 기대, 보안(CCTV·무인택배)도 좋음. 서울 강남구 논현로, 2020년 준공, 지상층 매물. 보증금 5천/월 110. 전용 36.8㎡(약 11.1평) 10평대 방. 관리비 8만(수도·전기) 포함 한 달 총 고정 지출 118만, 고정 지출 관리 체크 필요. 강남권 출퇴근·테헤란로 인근 접근.")
        );

        return new SementicResults(items);
    }

    @Scheduled(cron = "0 0 4 * * *") // 매일 04:00 실행
    public void processDailyOutbox() {
        log.info("Batch Start: Processing Outbox Events");

        // 처리된 데이터는 상태가 변경되어 목록에서 사라지므로, 항상 0페이지를 반복 조회합니다.
        Pageable pageable = PageRequest.of(0, BATCH_SIZE, Sort.by("id").ascending());

        int loopCount = 0;
        while (loopCount < MAX_BATCH_LOOPS) {
            loopCount++;

            // [1단계] READY / READY_UPDATE 항목 처리 (추가 및 덮어쓰기)
            List<VectorOutbox> processingBatch = transactionTemplate.execute(status -> {
                List<OutboxStatus> targetStatuses = List.of(OutboxStatus.READY, OutboxStatus.READY_UPDATE);
                List<VectorOutbox> batch = outboxRepository.findByStatusIn(targetStatuses, pageable);

                if (batch.isEmpty()) {
                    return null;
                }

                // 조회된 항목들을 PROCESSING 상태로 변경
                batch.forEach(VectorOutbox::markProcessing);
                return batch;
            });

            if (processingBatch != null && !processingBatch.isEmpty()) {
                List<VectorItem> vectorItems = new ArrayList<>(10);
                processingBatch.forEach(ele -> {
                    vectorItems.add(new VectorItem(ele.getHouseId(), ele.getMetadata(), ""));
                });

                try {
                    SementicResults results = get10SementicChunkList(processingBatch);
                    if (results != null && results.results() != null) {
                        for (SementicItem item : results.results()) {
                            log.info("House ID: {}, Chunk: {}", item.houseId(), item.chunk());
                            vectorItems.stream()
                                    .filter(v -> v.getHouseId().equals(item.houseId()))
                                    .findFirst()
                                    .ifPresent(v -> {
                                        v.setContent(item.chunk());
                                    });
                        }
                    }

                    // READY_UPDATE 인 경우 덮어쓰기를 위해 uuidKey 가 동일하므로 vectorStore.add() 로 upsert 처리됩니다.
                    // (단, 명시적 삭제가 필요하다면 vectorStore.delete(ids) 후 add 할 수 있으나 add 가 upsert를 지원합니다.)
                    saveVectorStore(vectorItems);

                    transactionTemplate.execute(status -> {
                        processingBatch.forEach(VectorOutbox::complete);
                        outboxRepository.saveAll(processingBatch);
                        return null;
                    });
                    log.info("Processed ADD/UPDATE batch of {} items.", processingBatch.size());
                } catch (Exception e) {
                    log.error("Batch processing failed for ADD/UPDATE chunk", e);
                    transactionTemplate.execute(status -> {
                        processingBatch.forEach(item -> {
                            if (item.getRetryCount() >= 3) {
                                item.fail();
                            } else {
                                item.retry();
                            }
                        });
                        outboxRepository.saveAll(processingBatch);
                        return null;
                    });
                }
            }

            // [2단계] READY_DELETE 항목 처리 (삭제)
            List<VectorOutbox> deleteBatch = transactionTemplate.execute(status -> {
                List<VectorOutbox> batch = outboxRepository.findByStatus(OutboxStatus.READY_DELETE, pageable);
                if (batch.isEmpty()) {
                    return null;
                }
                batch.forEach(VectorOutbox::markProcessing);
                return batch;
            });

            if (deleteBatch != null && !deleteBatch.isEmpty()) {
                try {
                    List<String> uuidsToDelete = deleteBatch.stream()
                            .map(item -> UUID.nameUUIDFromBytes(String.valueOf(item.getHouseId()).getBytes()).toString())
                            .toList();

                    if (!uuidsToDelete.isEmpty()) {
                        vectorStore.delete(uuidsToDelete);
                        log.info("Deleted {} items from vector store.", uuidsToDelete.size());
                    }

                    transactionTemplate.execute(status -> {
                        deleteBatch.forEach(VectorOutbox::complete);
                        outboxRepository.saveAll(deleteBatch);
                        return null;
                    });
                } catch (Exception e) {
                    log.error("Batch processing failed for DELETE chunk", e);
                    transactionTemplate.execute(status -> {
                        deleteBatch.forEach(item -> {
                            if (item.getRetryCount() >= 3) {
                                item.fail();
                            } else {
                                // 삭제 재시도 상태 복구 (retry() 가 READY 로 바꾸므로, DELETE 관련은 별도 처리하거나 READY_DELETE 유지 고려)
                                // 기존 코드 재사용: retry는 status를 READY로 바꾸므로 문제될수 있음
                                item.markReadyDelete();
                            }
                        });
                        outboxRepository.saveAll(deleteBatch);
                        return null;
                    });
                }
            }

            // 둘 다 처리할 게 없으면 루프 종료
            if ((processingBatch == null || processingBatch.isEmpty()) && (deleteBatch == null || deleteBatch.isEmpty())) {
                break;
            }
        }
        log.info("Batch End: Processing Completed. Total loops: {}", loopCount);
    }

    public SementicResults get10SementicChunkList(List<VectorOutbox> outboxItems) {
        List<Map<String, Object>> promptDataList = new ArrayList<>();
        for (VectorOutbox item : outboxItems) {
            Map<String, Object> rawContent = item.getContent();
            HouseContent content = HouseContent.builder()
                    .houseType(safeString(rawContent.get("houseType")))
                    .rentType(safeString(rawContent.get("rentType")))
                    .floor(safeString(rawContent.get("floor")))
                    .address(safeString(rawContent.get("address")))
                    .deposit(safeInt(rawContent.get("deposit")))
                    .monthlyCost(safeInt(rawContent.get("monthlyCost")))
                    .managementCost(safeInt(rawContent.get("managementCost")))
                    .managementItems(safeString(rawContent.get("managementItems")))
                    .floorSize(safeDouble(rawContent.get("floorSize")))
                    .buildYear(safeInt(rawContent.get("buildYear")))
                    .build();

            Map<String, Object> map = new HashMap<>();
            map.put("houseId", item.getHouseId());
            map.put("description", rawContent.get("description"));
            map.put("data", content);

            promptDataList.add(map);
        }

        String houseDataListJson;

        try {
            houseDataListJson = objectMapper.writeValueAsString(promptDataList);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert batch data to JSON", e);
        }

        return chatClient.prompt()
                .system(spec -> spec.text(sementicChunkSystemPrompt))
                .user(spec -> spec.text(sementicChunkuserPrompt)
                        .params(
                                Map.of("HOUSE_DATA_LIST", houseDataListJson
                                )))
                .call()
                .entity(SementicResults.class);
    }

    private String safeString(Object value) {
        return value == null ? "" : value.toString();
    }

    private int safeInt(Object value) {
        if (value == null) return 0;
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double safeDouble(Object value) {
        if (value == null) return 0.0;
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    void saveVectorStore(List<VectorItem> vectorItems) {
        if (vectorItems == null || vectorItems.isEmpty()) {
            return;
        }

        List<Document> documents = vectorItems.stream()
                .map(item -> {
                    // 기존 데이터가 있으면 덮어쓰기(Upsert) 되도록 합니다.
                    String uuidKey = UUID.nameUUIDFromBytes(String.valueOf(item.getHouseId()).getBytes()).toString();

                    return new Document(
                            uuidKey, // 숫자가 아닌 유효한 UUID 문자열 사용
                            item.getContent(),
                            item.getMetaData()
                    );
                })
                .toList();

        if (documents.isEmpty()) {
            log.info("No documents to save with valid content.");
            return;
        }

        try {
            vectorStore.add(documents);
            log.info("Saved {} items to vector store.", documents.size());
        } catch (Exception e) {
            log.error("Failed to save to vector store", e);
            throw e;
        }
    }

    public record SementicItem(
            Long houseId,
            String chunk
    ) {
    }

    // 전체 응답용 record
    public record SementicResults(
            List<SementicItem> results
    ) {
    }


    @Getter
    @Setter
    @NoArgsConstructor
    private static class VectorItem {
        Long houseId;
        Map<String, Object> metaData;
        String content;

        public VectorItem(Long houseId, Map<String, Object> metaData, String content) {
            this.houseId = houseId;
            this.metaData = metaData != null ? new HashMap<>(metaData) : new HashMap<>();
            this.metaData.put("houseId", houseId);
            this.content = content;
        }
    }
}
