package com.multiverse.pixel.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScoreService {

    private static final String TABLE_NAME = "UserScores";
    private final DynamoDbClient dynamoDbClient;

    public ScoreService(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    /**
     * Atomically increment a user's score by 1.
     * Uses DynamoDB's UpdateItem with ADD operation (no race conditions).
     */
    public void incrementScore(String userId) {
        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Map.of("userId", AttributeValue.builder().s(userId).build()))
                .updateExpression("ADD score :inc")
                .expressionAttributeValues(Map.of(
                        ":inc", AttributeValue.builder().n("1").build()
                ))
                .build();

        dynamoDbClient.updateItem(request);
    }

    /**
     * Get the leaderboard (top users by score).
     * Note: This is a full table scan - only acceptable for small datasets.
     * In production, use DynamoDB Streams + aggregation table.
     */
    public List<Map<String, Object>> getLeaderboard(int limit) {
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();

        ScanResponse response = dynamoDbClient.scan(scanRequest);

        return response.items().stream()
                .map(item -> Map.<String, Object>of(
                        "userId", item.get("userId").s(),
                        "score", Long.parseLong(item.getOrDefault("score",
                                AttributeValue.builder().n("0").build()).n())
                ))
                .sorted((a, b) -> Long.compare((Long) b.get("score"), (Long) a.get("score")))
                .limit(limit)
                .toList();
    }

    private List<String> scanAllUserIds() {
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .attributesToGet("userId") // 只返回userId字段，不查其他数据
                .build();

        ScanResponse response = dynamoDbClient.scan(scanRequest);
        return response.items().stream()
                .map(item -> item.get("userId").s())
                .collect(Collectors.toList());

    }

    private void resetValOfUsers(List<String> userIds) {
        for (String userId : userIds) {
            // 直接构建Update请求，只改score为0，保留其他字段
            UpdateItemRequest updateRequest = UpdateItemRequest.builder()
                    .tableName(TABLE_NAME)
                    // 指定要更新的用户（主键）
                    .key(Map.of("userId", AttributeValue.builder().s(userId).build()))
                    // 更新表达式：把score设为0（只改这一个字段，不影响其他字段）
                    .updateExpression("SET score = :zero")
                    // 绑定参数：:zero对应数值0
                    .expressionAttributeValues(Map.of(":zero", AttributeValue.builder().n("0").build()))
                    .build();

            // 执行更新
            dynamoDbClient.updateItem(updateRequest);
        }
    }

    // 核心逻辑：遍历bot，将分值重置为0
    public void resetBotScores() {
        List<String> resUserIds = scanAllUserIds();

        resetValOfUsers(resUserIds);

        System.out.println("所有Bot分数已重置为0");
    }
}