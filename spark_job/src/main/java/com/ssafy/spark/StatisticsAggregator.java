package com.ssafy.spark;

import org.apache.spark.sql.*;
import static org.apache.spark.sql.functions.*;
import java.util.Properties;

/**
 * HDFS에 적재된 부동산 실거래가 원천 데이터(JSON)를 읽어와서
 * 각 지역구별, 월별로 평균 시세(전세, 월세 보증금, 월세액)를 집계한 뒤
 * 외부 PostgreSQL 데이터베이스로 전송하는 Spark ETL Job입니다.
 */
public class StatisticsAggregator {
    public static void main(String[] args) {
        // 1. Spark Session 초기화: 분석 엔진의 시작점
        SparkSession spark = SparkSession.builder()
                .appName("HdfsToExternalPostgresStatistics")
                .getOrCreate();

        // 2. 데이터 추출 (Extract): HDFS에서 모든 카테고리/년/월의 JSON 파일을 재귀적으로 읽음
        String inputPath = "hdfs://namenode:8020/raw/statistics/*/*/*/*.json";
        Dataset<Row> rawData = spark.read().json(inputPath);

        // [데이터 안정성 확보] 스키마 추론 시 존재하지 않는 컬럼에 대한 예외 처리
        // 원천 데이터(아파트, 오피스텔 등)마다 필드명이 다를 수 있으므로 없는 컬럼은 미리 null로 선언
        String[] cols = rawData.columns();
        java.util.List<String> colList = java.util.Arrays.asList(cols);
        String[] optionalCols = {"deposit", "guaranteeAmount", "guarantee", "monthlyRent", "monthly_rent", "dealYear", "dealMonth", "sggCd"};
        
        for (String c : optionalCols) {
            if (!colList.contains(c)) {
                rawData = rawData.withColumn(c, lit(null));
            }
        }

        // 3. 데이터 변환 (Transform): 각기 다른 필드명을 통합하고 숫자 포맷으로 정제
        Dataset<Row> cleanedData = rawData
                // [필드 통합] 여러 이름으로 들어오는 보증금/월세 필드 중 실제 값이 있는 것을 선택 (null/공백 제외)
                .withColumn("safe_deposit", when(col("deposit").isNull().or(col("deposit").equalTo("")), lit(null)).otherwise(col("deposit")))
                .withColumn("safe_guaranteeAmount", when(col("guaranteeAmount").isNull().or(col("guaranteeAmount").equalTo("")), lit(null)).otherwise(col("guaranteeAmount")))
                .withColumn("safe_guarantee", when(col("guarantee").isNull().or(col("guarantee").equalTo("")), lit(null)).otherwise(col("guarantee")))
                .withColumn("safe_monthlyRent", when(col("monthlyRent").isNull().or(col("monthlyRent").equalTo("")), lit(null)).otherwise(col("monthlyRent")))
                .withColumn("safe_monthly_rent", when(col("monthly_rent").isNull().or(col("monthly_rent").equalTo("")), lit(null)).otherwise(col("monthly_rent")))
                
                // [통합 필드 생성] coalesce를 사용하여 우선순위에 따라 보증금/월세 값 확정
                .withColumn("depositVal", coalesce(col("safe_deposit"), col("safe_guaranteeAmount"), col("safe_guarantee"), lit("0")))
                .withColumn("rentVal", coalesce(col("safe_monthlyRent"), col("safe_monthly_rent"), lit("0")))
                
                // [타입 변환] 콤마(,) 제거 후 숫자로 변환 (만원 단위)
                .withColumn("depositNum", regexp_replace(col("depositVal"), ",", "").cast("double"))
                .withColumn("rentNum", regexp_replace(col("rentVal"), ",", "").cast("double"))
                
                // [기준 정보 생성] 년도와 월을 합쳐 정수형(YYYYMM) 생성 및 지역 코드 준비
                .withColumn("baseYearMonth", col("dealYear").multiply(100).plus(col("dealMonth")))
                .withColumn("sigunguCode", col("sggCd"))
                .withColumn("fullSggCd", concat(col("sggCd"), lit("00000")));

        // 4. 데이터 집계 (Aggregate): 구(Gu)별, 월별 그룹화 후 통계치 계산
        Dataset<Row> statistics = cleanedData.groupBy("baseYearMonth", "sigunguCode", "fullSggCd")
                .agg(
                        // [평균 전세] 월세가 0원인 행들의 보증금 평균
                        coalesce(round(avg(when(col("rentNum").equalTo(0), col("depositNum")))), lit(0)).cast("int").alias("avg_jeonse"),
                        
                        // [평균 월세 보증금] 월세가 0보다 큰 행들의 보증금 평균
                        coalesce(round(avg(when(col("rentNum").gt(0), col("depositNum")))), lit(0)).cast("int").alias("avg_rent_deposit"),
                        
                        // [평균 월세액] 행들의 월세 금액 평균
                        coalesce(round(avg(col("rentNum"))), lit(0)).cast("int").alias("avg_monthly_cost"),
                        
                        // [거래 총량] 해당 지역/월의 총 거래 건수
                        count("*").cast("int").alias("trade_count")
                )
                // DB 테이블 스키마(snake_case)에 맞게 컬럼명 최종 변경 및 메타데이터 추가
                .withColumnRenamed("baseYearMonth", "base_year_month")
                .withColumnRenamed("sigunguCode", "sigungu_code")
                .withColumnRenamed("fullSggCd", "sgg_cd")
                .withColumn("created_by", lit(0L))
                .withColumn("updated_by", lit(0L))
                .withColumn("created_at", current_timestamp())
                .withColumn("updated_at", current_timestamp());

        // 5. 데이터 적재 (Load): 계산된 통계를 최종 PostgreSQL DB 테이블에 기록
        String jdbcUrl = "jdbc:postgresql://172.26.11.172:5432/banggok?sslmode=require";
        String targetTable = "region_monthly_trend";

        Properties connectionProperties = new Properties();
        connectionProperties.setProperty("user", "spark_writer");
        connectionProperties.setProperty("password", "EPyl6BMNlBRqk3S1YBvv");
        connectionProperties.setProperty("driver", "org.postgresql.Driver");

        // DB에 쓰기 수행 (Append 모드)
        statistics.write()
                .mode(SaveMode.Append)
                .jdbc(jdbcUrl, targetTable, connectionProperties);

        System.out.println(">>> Spark Job 완료: 지역구별 월별 고도화 통계 적재 성공");
        spark.stop();
    }
}

