package com.matchpet.infrastructure.adapters.output.neo4j;

import com.matchpet.domain.model.AdoptionRequest;
import com.matchpet.domain.model.AdoptionRequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.data.neo4j.core.Neo4jClient;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Neo4jAdoptionRequestAdapterMockTest {

    @Mock
    private Neo4jClient neo4jClient;

    @Mock
    private Neo4jClient.UnboundRunnableSpec unboundRunnableSpec;

    @Mock
    private Neo4jClient.OngoingBindSpec ongoingBindSpec;

    @Mock
    private Neo4jClient.BindSpec bindSpec;

    @Mock
    private Neo4jClient.MappingSpec<AdoptionRequest> mappingSpec;

    @Mock
    private Neo4jClient.RecordFetchSpec<AdoptionRequest> recordFetchSpec;

    private Neo4jAdoptionRequestAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new Neo4jAdoptionRequestAdapter(neo4jClient);
    }

    @SuppressWarnings("unchecked")
    @Test
    void createOrGetPending_ShouldExecuteCorrectQuery() {
        // Arrange
        String userId = "user-1";
        String petId = "pet-1";
        String requestId = userId + ":" + petId;

        setupMockChain();

        AdoptionRequest expectedRequest = new AdoptionRequest(requestId, userId, petId, AdoptionRequestStatus.PENDING, Instant.now(), Instant.now());
        
        // Mock the mapping logic
        when(recordFetchSpec.one()).thenReturn(Optional.of(expectedRequest));

        // Act
        AdoptionRequest result = adapter.createOrGetPending(userId, petId);

        // Assert
        assertNotNull(result);
        assertEquals(requestId, result.id());
        
        verify(neo4jClient).query(contains("MERGE (ar:AdoptionRequest {id: $requestId})"));
        verify(unboundRunnableSpec, atLeastOnce()).bind(eq(userId));
        verify(unboundRunnableSpec, atLeastOnce()).bind(eq(petId));
        verify(unboundRunnableSpec, atLeastOnce()).bind(eq(requestId));
    }

    @SuppressWarnings("unchecked")
    @Test
    void accept_ShouldUpdateStatusAndCreateMatchedRelationship() {
        // Arrange
        String userId = "user-1";
        String petId = "pet-1";
        String requestId = userId + ":" + petId;
        String shelterId = "shelter-1";

        setupMockChain();

        AdoptionRequest expectedRequest = new AdoptionRequest(requestId, userId, petId, AdoptionRequestStatus.ACCEPTED, Instant.now(), Instant.now());
        when(recordFetchSpec.one()).thenReturn(Optional.of(expectedRequest));

        // Act
        AdoptionRequest result = adapter.accept(requestId, shelterId);

        // Assert
        assertNotNull(result);
        assertEquals(AdoptionRequestStatus.ACCEPTED, result.status());
        
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(neo4jClient).query(queryCaptor.capture());
        String query = queryCaptor.getValue();
        
        assertTrue(query.contains("SET ar.status = $status"));
        assertTrue(query.contains("MERGE (u)-[:MATCHED]->(p)"));
        verify(unboundRunnableSpec).bind(eq(requestId));
        verify(unboundRunnableSpec).bind(eq(AdoptionRequestStatus.ACCEPTED.name()));
    }

    @SuppressWarnings("unchecked")
    private void setupMockChain() {
        when(neo4jClient.query(anyString())).thenReturn(unboundRunnableSpec);
        when(unboundRunnableSpec.bind(any())).thenReturn(ongoingBindSpec);
        when(ongoingBindSpec.to(anyString())).thenReturn((Neo4jClient.UnboundRunnableSpec) unboundRunnableSpec);
        
        // This is the key fix: in the real API, fetchAs is called on UnboundRunnableSpec
        when(unboundRunnableSpec.fetchAs(any(Class.class))).thenReturn(mappingSpec);
        when(mappingSpec.mappedBy(any())).thenReturn(recordFetchSpec);
    }

    @Test
    void mappedBy_ShouldCorrectlyMapRecordToAdoptionRequest() {
        // This tests the BiFunction passed to mappedBy
        // Arrange
        String userId = "user-1";
        String petId = "pet-1";
        String requestId = userId + ":" + petId;
        Instant now = Instant.now();
        ZonedDateTime zdt = now.atZone(ZoneId.systemDefault());

        Record record = mock(Record.class);
        Value nodeValue = mock(Value.class);
        Value idValue = mock(Value.class);
        Value userIdValue = mock(Value.class);
        Value petIdValue = mock(Value.class);
        Value statusValue = mock(Value.class);
        Value createdAtValue = mock(Value.class);
        Value updatedAtValue = mock(Value.class);

        when(record.get("ar")).thenReturn(nodeValue);
        when(nodeValue.get("id")).thenReturn(idValue);
        when(nodeValue.get("userId")).thenReturn(userIdValue);
        when(nodeValue.get("petId")).thenReturn(petIdValue);
        when(nodeValue.get("status")).thenReturn(statusValue);
        when(nodeValue.get("createdAt")).thenReturn(createdAtValue);
        when(nodeValue.get("updatedAt")).thenReturn(updatedAtValue);

        when(idValue.asString()).thenReturn(requestId);
        when(userIdValue.asString()).thenReturn(userId);
        when(petIdValue.asString()).thenReturn(petId);
        when(statusValue.asString()).thenReturn("PENDING");
        when(createdAtValue.asZonedDateTime()).thenReturn(zdt);
        when(updatedAtValue.asZonedDateTime()).thenReturn(zdt);

        setupMockChain();
        AdoptionRequest expectedRequest = new AdoptionRequest(requestId, userId, petId, AdoptionRequestStatus.PENDING, now, now);
        when(recordFetchSpec.one()).thenReturn(Optional.of(expectedRequest)); // Added missing return

        adapter.createOrGetPending(userId, petId);

        ArgumentCaptor<BiFunction<TypeSystem, Record, AdoptionRequest>> mapperCaptor = ArgumentCaptor.forClass(BiFunction.class);
        verify(mappingSpec).mappedBy(mapperCaptor.capture());

        BiFunction<TypeSystem, Record, AdoptionRequest> mapper = mapperCaptor.getValue();

        // Act
        AdoptionRequest result = mapper.apply(mock(TypeSystem.class), record);

        // Assert
        assertEquals(requestId, result.id());
        assertEquals(userId, result.userId());
        assertEquals(petId, result.petId());
        assertEquals(AdoptionRequestStatus.PENDING, result.status());
        // ZonedDateTime to Instant might have some nanosecond differences depending on system, but should be close enough or exact
        assertEquals(zdt.toInstant(), result.createdAt());
    }
}
