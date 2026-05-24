package com.matchpet.infrastructure.adapters.output.neo4j;

import com.matchpet.application.ports.output.ChatPersistencePort;
import com.matchpet.domain.model.AdoptionRequestStatus;
import com.matchpet.domain.model.Message;
import org.neo4j.driver.Record;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class Neo4jMessageAdapter implements ChatPersistencePort {

    private final Neo4jClient neo4jClient;

    public Neo4jMessageAdapter(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    @Override
    public Optional<ChatRequestContext> findRequestContext(String requestId) {
        String query = """
                MATCH (ar:AdoptionRequest {id: $requestId})
                MATCH (u:User)-[:REQUESTED]->(ar)
                MATCH (ar)-[:FOR]->(p:Pet)-[:LOCATED_IN]->(s:Shelter)
                RETURN ar.id AS requestId, ar.status AS status, u.id AS adopterId, s.id AS shelterId
                """;

        return neo4jClient.query(query)
                .bind(requestId).to("requestId")
                .fetchAs(ChatRequestContext.class)
                .mappedBy((typeSystem, record) -> mapRequestContext(record))
                .one();
    }

    @Override
    public Message saveMessage(String requestId, Message message) {
        String query = """
                MATCH (ar:AdoptionRequest {id: $requestId})
                CREATE (ar)-[:CONTAINS]->(m:Message {
                    id: $id,
                    senderId: $senderId,
                    content: $content,
                    timestamp: datetime($timestamp)
                })
                RETURN m.id AS id, m.senderId AS senderId, m.content AS content, m.timestamp AS timestamp
                """;

        return neo4jClient.query(query)
                .bindAll(Map.of(
                        "requestId", requestId,
                        "id", message.id(),
                        "senderId", message.senderId(),
                        "content", message.content(),
                        "timestamp", message.timestamp().toString()
                ))
                .fetchAs(Message.class)
                .mappedBy((typeSystem, record) -> mapMessage(record))
                .one()
                .orElseThrow();
    }

    @Override
    public List<Message> getChatHistory(String requestId, int skip, int limit) {
        String query = """
                MATCH (ar:AdoptionRequest {id: $requestId})-[:CONTAINS]->(m:Message)
                RETURN m.id AS id, m.senderId AS senderId, m.content AS content, m.timestamp AS timestamp
                ORDER BY m.timestamp DESC
                SKIP $skip
                LIMIT $limit
                """;

        return new ArrayList<>(neo4jClient.query(query)
                .bind(requestId).to("requestId")
                .bind(skip).to("skip")
                .bind(limit).to("limit")
                .fetchAs(Message.class)
                .mappedBy((typeSystem, record) -> mapMessage(record))
                .all());
    }

    private ChatRequestContext mapRequestContext(Record record) {
        return new ChatRequestContext(
                record.get("requestId").asString(),
                AdoptionRequestStatus.valueOf(record.get("status").asString()),
                record.get("adopterId").asString(),
                record.get("shelterId").asString()
        );
    }

    private Message mapMessage(Record record) {
        return new Message(
                record.get("id").asString(),
                record.get("senderId").asString(),
                record.get("content").asString(),
                record.get("timestamp").asZonedDateTime().toInstant()
        );
    }
}
