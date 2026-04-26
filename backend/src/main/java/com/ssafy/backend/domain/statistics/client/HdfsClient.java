package com.ssafy.backend.domain.statistics.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * WebHDFS REST API를 통해 Hadoop HDFS와 통신하는 클라이언트입니다.
 */
@Slf4j
@Component
public class HdfsClient {

    private final WebClient webClient;
    private final String hdfsUrl;
    private final String hdfsUser;

    public HdfsClient(WebClient webClient,
                      @Value("${hadoop.hdfs.webhdfs-url}") String hdfsUrl,
                      @Value("${hadoop.hdfs.user}") String hdfsUser) {
        this.webClient = webClient.mutate().build();
        this.hdfsUrl = hdfsUrl;
        this.hdfsUser = hdfsUser;
    }

    /**
     * HDFS에 디렉토리를 생성합니다. (MKDIRS)
     *
     * @param path 생성할 디렉토리 경로
     * @return Mono<Void>
     */
    public Mono<Void> makeDirectories(String path) {
        String fullUrl = String.format("%s%s?op=MKDIRS&user.name=%s", hdfsUrl, path, hdfsUser);
        return webClient.put()
                .uri(fullUrl)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(v -> log.info("HDFS 디렉토리 준비 완료: {}", path))
                .then();
    }

    /**
     * HDFS에 파일을 생성하고 데이터를 씁니다. (CREATE)
     * 부모 디렉토리가 없을 수 있으므로 먼저 MKDIRS를 호출한 뒤 진행합니다.
     */
    public Mono<Void> writeJsonFile(String path, String jsonData) {
        String parentPath = path.substring(0, path.lastIndexOf("/"));
        
        return makeDirectories(parentPath)
                .then(Mono.defer(() -> {
                    String fullUrl = String.format("%s%s?op=CREATE&user.name=%s&overwrite=true", hdfsUrl, path, hdfsUser);

                    return webClient.put()
                            .uri(fullUrl)
                            .retrieve()
                            .toBodilessEntity()
                            .flatMap(response -> {
                                URI redirectUri = response.getHeaders().getLocation();
                                if (redirectUri == null) {
                                    return Mono.error(new RuntimeException("HDFS Redirect URL을 받지 못했습니다."));
                                }

                                // 내부 호스트명(datanode1, datanode2)을 외부에서 접근 가능한 호스트명으로 변환
                                String fixedUri = redirectUri.toString()
                                        .replace("datanode1", "j14a104a.p.ssafy.io")
                                        .replace("datanode2:9864", "j14a104a.p.ssafy.io:9865")
                                        .replace("datanode2", "j14a104a.p.ssafy.io");

                                log.info("HDFS DataNode Redirecting to: {}", fixedUri);

                                return webClient.put()
                                        .uri(URI.create(fixedUri))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(jsonData.getBytes(StandardCharsets.UTF_8))
                                        .retrieve()
                                        .onStatus(HttpStatusCode::isError, res -> {
                                            log.error("HDFS 파일 쓰기 실패: {}", res.statusCode());
                                            return Mono.error(new RuntimeException("HDFS Write Failed"));
                                        })
                                        .toBodilessEntity()
                                        .then();
                            });
                }))
                .doOnSuccess(v -> log.info("HDFS 파일 적재 성공: {}", path))
                .doOnError(e -> log.error("HDFS 파일 적재 중 오류: {}", e.getMessage()));
    }
}
