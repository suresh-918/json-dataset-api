package com.freightfox.jsondataset.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightfox.jsondataset.dto.CommonResponse;
import com.freightfox.jsondataset.dto.DatasetQueryResponse;
import com.freightfox.jsondataset.entity.DatasetRecord;
import com.freightfox.jsondataset.exception.InvalidRequestException;
import com.freightfox.jsondataset.repository.DatasetRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatasetQueryServiceImplTest {

    @Mock
    private DatasetRecordRepository datasetRecordRepository;

    @InjectMocks
    private DatasetQueryServiceImpl datasetQueryService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    private DatasetRecord createRecord(String json) throws Exception {
        JsonNode data = objectMapper.readTree(json);
        DatasetRecord record = new DatasetRecord("employee_dataset", data);
        return record;
    }

    private void mockRecords() throws Exception {
        when(datasetRecordRepository.findByDatasetName("employee_dataset"))
                .thenReturn(List.of(
                        createRecord("""
                                {"name":"Suresh","department":"IT","age":25}
                                """),
                        createRecord("""
                                {"name":"Ravi","department":"HR","age":30}
                                """),
                        createRecord("""
                                {"name":"Priya","department":"IT","age":22}
                                """)
                ));
    }

    @Test
    void queryDataset_shouldGroupRecordsByDepartment() throws Exception {
        mockRecords();

        CommonResponse<DatasetQueryResponse> response =
                datasetQueryService.queryDataset(
                        "employee_dataset", "department", null, null);

        assertEquals(200, response.getStatus());
        assertTrue(response.isSuccess());

        var groupedRecords = response.getData().groupedRecords();

        assertEquals(2, groupedRecords.size());
        assertEquals(2, groupedRecords.get("IT").size());
        assertEquals(1, groupedRecords.get("HR").size());
        assertNull(response.getData().sortedRecords());

        verify(datasetRecordRepository)
                .findByDatasetName("employee_dataset");
    }

    @Test
    void queryDataset_shouldSortRecordsByAgeAscending() throws Exception {
        mockRecords();

        CommonResponse<DatasetQueryResponse> response =
                datasetQueryService.queryDataset(
                        "employee_dataset", null, "age", "asc");

        List<JsonNode> sortedRecords =
                response.getData().sortedRecords();

        assertEquals(22, sortedRecords.get(0).get("age").asInt());
        assertEquals(25, sortedRecords.get(1).get("age").asInt());
        assertEquals(30, sortedRecords.get(2).get("age").asInt());
        assertNull(response.getData().groupedRecords());
    }

    @Test
    void queryDataset_shouldSortRecordsByAgeDescending() throws Exception {
        mockRecords();

        CommonResponse<DatasetQueryResponse> response =
                datasetQueryService.queryDataset(
                        "employee_dataset", null, "age", "desc");

        List<JsonNode> sortedRecords =
                response.getData().sortedRecords();

        assertEquals(30, sortedRecords.get(0).get("age").asInt());
        assertEquals(25, sortedRecords.get(1).get("age").asInt());
        assertEquals(22, sortedRecords.get(2).get("age").asInt());
    }

    @Test
    void queryDataset_shouldThrowExceptionWhenNoOperationProvided() {
        assertThrows(
                InvalidRequestException.class,
                () -> datasetQueryService.queryDataset(
                        "employee_dataset", null, null, null)
        );

        verifyNoInteractions(datasetRecordRepository);
    }

    @Test
    void queryDataset_shouldThrowExceptionWhenBothOperationsProvided() {
        assertThrows(
                InvalidRequestException.class,
                () -> datasetQueryService.queryDataset(
                        "employee_dataset", "department", "age", "asc")
        );

        verifyNoInteractions(datasetRecordRepository);
    }

    @Test
    void queryDataset_shouldThrowExceptionForInvalidSortOrder()
            throws Exception {
        mockRecords();

        assertThrows(
                InvalidRequestException.class,
                () -> datasetQueryService.queryDataset(
                        "employee_dataset", null, "age", "random")
        );
    }

    @Test
    void queryDataset_shouldReturnEmptyResultsWhenDatasetHasNoRecords() {
        when(datasetRecordRepository.findByDatasetName("empty_dataset"))
                .thenReturn(List.of());

        CommonResponse<DatasetQueryResponse> response =
                datasetQueryService.queryDataset(
                        "empty_dataset", null, "age", "asc");

        assertTrue(response.isSuccess());
        assertTrue(response.getData().sortedRecords().isEmpty());
    }

    @Test
    void queryDataset_shouldGroupRecordsWithMissingField()
            throws Exception {

        when(datasetRecordRepository.findByDatasetName("employee_dataset"))
                .thenReturn(List.of(
                        createRecord("""
                            {"name":"Suresh","department":"IT","age":25}
                            """),
                        createRecord("""
                            {"name":"Ravi","age":30}
                            """)
                ));

        CommonResponse<DatasetQueryResponse> response =
                datasetQueryService.queryDataset(
                        "employee_dataset", "department", null, null);

        var groupedRecords = response.getData().groupedRecords();

        assertEquals(2, groupedRecords.size());
        assertEquals(1, groupedRecords.get("IT").size());
        assertEquals(1, groupedRecords.get("(missing)").size());
    }

    @Test
    void queryDataset_shouldHandleMissingSortField() throws Exception {

        when(datasetRecordRepository.findByDatasetName("employee_dataset"))
                .thenReturn(List.of(
                        createRecord("""
                            {"name":"Suresh","age":25}
                            """),
                        createRecord("""
                            {"name":"Ravi"}
                            """)
                ));

        CommonResponse<DatasetQueryResponse> response =
                datasetQueryService.queryDataset(
                        "employee_dataset", null, "age", "asc");

        List<JsonNode> sortedRecords =
                response.getData().sortedRecords();

        assertEquals(25, sortedRecords.get(0).get("age").asInt());
        assertNull(sortedRecords.get(1).get("age"));
    }

    @Test
    void queryDataset_shouldThrowExceptionWhenDatasetNameIsBlank() {

        assertThrows(
                InvalidRequestException.class,
                () -> datasetQueryService.queryDataset(
                        " ", null, "age", "asc")
        );

        verifyNoInteractions(datasetRecordRepository);
    }

    @Test
    void queryDataset_shouldThrowExceptionWhenDatasetNameExceedsLimit() {

        String datasetName = "a".repeat(151);

        assertThrows(
                InvalidRequestException.class,
                () -> datasetQueryService.queryDataset(
                        datasetName, null, "age", "asc")
        );

        verifyNoInteractions(datasetRecordRepository);
    }

    @Test
    void queryDataset_shouldSortStringValues() throws Exception {
        when(datasetRecordRepository.findByDatasetName("employee_dataset"))
                .thenReturn(List.of(
                        createRecord("""
                            {"name":"Suresh","department":"IT"}
                            """),
                        createRecord("""
                            {"name":"Ravi","department":"HR"}
                            """)
                ));

        var response = datasetQueryService.queryDataset(
                "employee_dataset", null, "department", "asc");

        var sorted = response.getData().sortedRecords();

        assertEquals("HR", sorted.get(0).get("department").asText());
        assertEquals("IT", sorted.get(1).get("department").asText());
    }

    @Test
    void queryDataset_shouldSortBooleanValues() throws Exception {
        when(datasetRecordRepository.findByDatasetName("employee_dataset"))
                .thenReturn(List.of(
                        createRecord("""
                            {"active":true}
                            """),
                        createRecord("""
                            {"active":false}
                            """)
                ));

        var response = datasetQueryService.queryDataset(
                "employee_dataset", null, "active", "asc");

        var sorted = response.getData().sortedRecords();

        assertFalse(sorted.get(0).get("active").asBoolean());
        assertTrue(sorted.get(1).get("active").asBoolean());
    }

    @Test
    void queryDataset_shouldSortWhenValuesHaveDifferentTypes()
            throws Exception {
        when(datasetRecordRepository.findByDatasetName("employee_dataset"))
                .thenReturn(List.of(
                        createRecord("""
                            {"value":"abc"}
                            """),
                        createRecord("""
                            {"value":10}
                            """)
                ));

        var response = datasetQueryService.queryDataset(
                "employee_dataset", null, "value", "asc");

        assertEquals(2, response.getData().sortedRecords().size());
    }

    @Test
    void queryDataset_shouldHandleNullAndMissingSortValues()
            throws Exception {
        when(datasetRecordRepository.findByDatasetName("employee_dataset"))
                .thenReturn(List.of(
                        createRecord("""
                            {"name":"Suresh","age":null}
                            """),
                        createRecord("""
                            {"name":"Ravi","age":25}
                            """),
                        createRecord("""
                            {"name":"Priya"}
                            """)
                ));

        var response = datasetQueryService.queryDataset(
                "employee_dataset", null, "age", "asc");

        var sorted = response.getData().sortedRecords();

        assertEquals(25, sorted.get(0).get("age").asInt());
        assertEquals("Suresh", sorted.get(1).get("name").asText());
        assertEquals("Priya", sorted.get(2).get("name").asText());
    }

    @Test
    void queryDataset_shouldHandleExplicitNullGroupValue()
            throws Exception {

        when(datasetRecordRepository.findByDatasetName("employee_dataset"))
                .thenReturn(List.of(
                        createRecord("""
                            {"name":"Suresh","department":null}
                            """),
                        createRecord("""
                            {"name":"Ravi","department":"IT"}
                            """)
                ));

        var response = datasetQueryService.queryDataset(
                "employee_dataset", "department", null, null);

        var groups = response.getData().groupedRecords();

        assertTrue(groups.containsKey("null"));
        assertTrue(groups.containsKey("IT"));
    }

    @Test
    void queryDataset_shouldGroupNumericValues()
            throws Exception {

        when(datasetRecordRepository.findByDatasetName("employee_dataset"))
                .thenReturn(List.of(
                        createRecord("""
                            {"name":"Suresh","level":1}
                            """),
                        createRecord("""
                            {"name":"Ravi","level":2}
                            """)
                ));

        var response = datasetQueryService.queryDataset(
                "employee_dataset", "level", null, null);

        var groups = response.getData().groupedRecords();

        assertTrue(groups.containsKey("1"));
        assertTrue(groups.containsKey("2"));
    }

    @Test
    void queryDataset_shouldUseAscendingOrderWhenOrderIsMissing()
            throws Exception {

        mockRecords();

        var response = datasetQueryService.queryDataset(
                "employee_dataset", null, "age", null);

        var sorted = response.getData().sortedRecords();

        assertEquals(22, sorted.get(0).get("age").asInt());
        assertEquals(30, sorted.get(2).get("age").asInt());
    }

    @Test
    void queryDataset_shouldDefaultToAscendingWhenSortOrderIsBlank()
            throws Exception {

        mockRecords();

        var response = datasetQueryService.queryDataset(
                "employee_dataset", null, "age", " ");

        var sorted = response.getData().sortedRecords();

        assertTrue(response.isSuccess());
        assertEquals(22, sorted.get(0).get("age").asInt());
        assertEquals(30, sorted.get(2).get("age").asInt());
    }

    @Test
    void queryDataset_shouldRejectBlankGroupByAndSortBy() {

        assertThrows(
                InvalidRequestException.class,
                () -> datasetQueryService.queryDataset(
                        "employee_dataset", " ", " ", null)
        );

        verifyNoInteractions(datasetRecordRepository);
    }

    @Test
    void queryDataset_shouldCompareTwoMissingValues()
            throws Exception {

        when(datasetRecordRepository.findByDatasetName("employee_dataset"))
                .thenReturn(List.of(
                        createRecord("""
                            {"name":"Suresh"}
                            """),
                        createRecord("""
                            {"name":"Ravi"}
                            """)
                ));

        var response = datasetQueryService.queryDataset(
                "employee_dataset", null, "age", "asc");

        assertEquals(2, response.getData().sortedRecords().size());
    }

    @Test
    void queryDataset_shouldCompareTwoExplicitNullValues()
            throws Exception {

        when(datasetRecordRepository.findByDatasetName("employee_dataset"))
                .thenReturn(List.of(
                        createRecord("""
                            {"name":"Suresh","age":null}
                            """),
                        createRecord("""
                            {"name":"Ravi","age":null}
                            """)
                ));

        var response = datasetQueryService.queryDataset(
                "employee_dataset", null, "age", "asc");

        assertEquals(2, response.getData().sortedRecords().size());
    }

    @Test
    void queryDataset_shouldCompareNullAndMissingValues()
            throws Exception {

        when(datasetRecordRepository.findByDatasetName("employee_dataset"))
                .thenReturn(List.of(
                        createRecord("""
                            {"name":"Suresh","age":null}
                            """),
                        createRecord("""
                            {"name":"Ravi"}
                            """)
                ));

        var response = datasetQueryService.queryDataset(
                "employee_dataset", null, "age", "asc");

        assertEquals(2, response.getData().sortedRecords().size());
    }

    @Test
    void queryDataset_shouldSortMixedTypesInDescendingOrder()
            throws Exception {

        when(datasetRecordRepository.findByDatasetName("employee_dataset"))
                .thenReturn(List.of(
                        createRecord("""
                            {"value":10}
                            """),
                        createRecord("""
                            {"value":"abc"}
                            """)
                ));

        var response = datasetQueryService.queryDataset(
                "employee_dataset", null, "value", "desc");

        assertEquals(2, response.getData().sortedRecords().size());
    }

    @Test
    void queryDataset_shouldCompareValueAgainstNullAndMissingValues()
            throws Exception {

        when(datasetRecordRepository.findByDatasetName("employee_dataset"))
                .thenReturn(List.of(
                        createRecord("""
                            {"name":"Suresh","age":25}
                            """),
                        createRecord("""
                            {"name":"Ravi","age":null}
                            """),
                        createRecord("""
                            {"name":"Priya"}
                            """)
                ));

        var response = datasetQueryService.queryDataset(
                "employee_dataset", null, "age", "asc");

        var sorted = response.getData().sortedRecords();

        assertEquals(25, sorted.get(0).get("age").asInt());
        assertEquals(3, sorted.size());
    }

    @Test
    void queryDataset_shouldCompareTextWithDifferentType()
            throws Exception {

        when(datasetRecordRepository.findByDatasetName("employee_dataset"))
                .thenReturn(List.of(
                        createRecord("""
                            {"value":"abc"}
                            """),
                        createRecord("""
                            {"value":10}
                            """)
                ));

        var response = datasetQueryService.queryDataset(
                "employee_dataset", null, "value", "asc");

        assertEquals(2, response.getData().sortedRecords().size());
    }

    @Test
    void queryDataset_shouldCompareBooleanWithDifferentType()
            throws Exception {

        when(datasetRecordRepository.findByDatasetName("employee_dataset"))
                .thenReturn(List.of(
                        createRecord("""
                            {"value":true}
                            """),
                        createRecord("""
                            {"value":"yes"}
                            """)
                ));

        var response = datasetQueryService.queryDataset(
                "employee_dataset", null, "value", "asc");

        assertEquals(2, response.getData().sortedRecords().size());
    }

    @Test
    void queryDataset_shouldSortBooleanValuesCorrectly()
            throws Exception {

        when(datasetRecordRepository.findByDatasetName("employee_dataset"))
                .thenReturn(List.of(
                        createRecord("""
                            {"name":"Suresh","active":true}
                            """),
                        createRecord("""
                            {"name":"Ravi","active":false}
                            """)
                ));

        var response = datasetQueryService.queryDataset(
                "employee_dataset", null, "active", "asc");

        var sorted = response.getData().sortedRecords();

        assertFalse(sorted.get(0).get("active").asBoolean());
        assertTrue(sorted.get(1).get("active").asBoolean());
    }

    @Test
    void queryDataset_shouldSortTextValuesIgnoringCase()
            throws Exception {

        when(datasetRecordRepository.findByDatasetName("employee_dataset"))
                .thenReturn(List.of(
                        createRecord("""
                            {"name":"suresh"}
                            """),
                        createRecord("""
                            {"name":"Ravi"}
                            """)
                ));

        var response = datasetQueryService.queryDataset(
                "employee_dataset", null, "name", "asc");

        var sorted = response.getData().sortedRecords();

        assertEquals("Ravi", sorted.get(0).get("name").asText());
        assertEquals("suresh", sorted.get(1).get("name").asText());
    }


    @Test
    void queryDataset_shouldCompareNullValueAgainstNumber() throws Exception {
        when(datasetRecordRepository.findByDatasetName("employee_dataset"))
                .thenReturn(List.of(
                        createRecord("""
                        {"name":"Suresh","age":null}
                        """),
                        createRecord("""
                        {"name":"Ravi","age":25}
                        """)
                ));

        var response = datasetQueryService.queryDataset(
                "employee_dataset", null, "age", "asc");

        var sorted = response.getData().sortedRecords();

        assertEquals(25, sorted.get(0).get("age").asInt());
        assertTrue(sorted.get(1).get("age").isNull());
    }


    @Test
    void queryDataset_shouldCompareStringsIgnoringCase() throws Exception {
        when(datasetRecordRepository.findByDatasetName("employee_dataset"))
                .thenReturn(List.of(
                        createRecord("""
                        {"name":"banana"}
                        """),
                        createRecord("""
                        {"name":"Apple"}
                        """)
                ));

        var response = datasetQueryService.queryDataset(
                "employee_dataset", null, "name", "asc");

        var sorted = response.getData().sortedRecords();

        assertEquals("Apple", sorted.get(0).get("name").asText());
        assertEquals("banana", sorted.get(1).get("name").asText());
    }

}
