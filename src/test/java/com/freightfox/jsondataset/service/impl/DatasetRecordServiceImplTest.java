package com.freightfox.jsondataset.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightfox.jsondataset.dto.CommonResponse;
import com.freightfox.jsondataset.dto.InsertRecordResponse;
import com.freightfox.jsondataset.entity.DatasetRecord;
import com.freightfox.jsondataset.exception.InvalidRequestException;
import com.freightfox.jsondataset.repository.DatasetRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatasetRecordServiceImplTest {

    @Mock
    private DatasetRecordRepository datasetRecordRepository;

    @InjectMocks
    private DatasetRecordServiceImpl datasetRecordService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void insertRecord_shouldReturnSuccess_whenRecordIsValid()
            throws Exception {

        JsonNode record = objectMapper.readTree("""
                {
                    "name": "Suresh",
                    "department": "IT",
                    "age": 25
                }
                """);

        when(datasetRecordRepository.save(any(DatasetRecord.class)))
                .thenAnswer(invocation -> {
                    DatasetRecord entity = invocation.getArgument(0);
                    entity.setId(1L);
                    return entity;
                });

        CommonResponse<InsertRecordResponse> response =
                datasetRecordService.insertRecord(
                        "employee_dataset", record);

        assertEquals(201, response.getStatus());
        assertEquals("Record added successfully", response.getMessage());
        assertTrue(response.isSuccess());
        assertEquals("employee_dataset",
                     response.getData().dataset());
        assertEquals(1L, response.getData().recordId());

        verify(datasetRecordRepository, times(1))
                .save(any(DatasetRecord.class));
    }

    @Test
    void insertRecord_shouldThrowException_whenDatasetNameIsBlank() {
        JsonNode record = objectMapper.createObjectNode();

        assertThrows(
                InvalidRequestException.class,
                () -> datasetRecordService.insertRecord(" ", record)
        );

        verify(datasetRecordRepository, never())
                .save(any(DatasetRecord.class));
    }

    @Test
    void insertRecord_shouldThrowException_whenDatasetNameExceedsLimit() {
        String datasetName = "a".repeat(151);
        JsonNode record = objectMapper.createObjectNode();

        assertThrows(
                InvalidRequestException.class,
                () -> datasetRecordService.insertRecord(datasetName, record)
        );

        verify(datasetRecordRepository, never())
                .save(any(DatasetRecord.class));
    }

    @Test
    void insertRecord_shouldThrowException_whenRecordIsNotObject()
            throws Exception {

        JsonNode record = objectMapper.readTree("""
                [
                    {"name": "Suresh"},
                    {"name": "Ravi"}
                ]
                """);

        assertThrows(
                InvalidRequestException.class,
                () -> datasetRecordService.insertRecord(
                        "employee_dataset", record)
        );

        verify(datasetRecordRepository, never())
                .save(any(DatasetRecord.class));
    }
}
