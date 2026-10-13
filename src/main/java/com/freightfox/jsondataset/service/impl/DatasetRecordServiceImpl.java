package com.freightfox.jsondataset.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.freightfox.jsondataset.dto.CommonResponse;
import com.freightfox.jsondataset.dto.InsertRecordResponse;
import com.freightfox.jsondataset.entity.DatasetRecord;
import com.freightfox.jsondataset.exception.InvalidRequestException;
import com.freightfox.jsondataset.repository.DatasetRecordRepository;
import com.freightfox.jsondataset.service.DatasetRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class DatasetRecordServiceImpl implements DatasetRecordService {

    private final DatasetRecordRepository datasetRecordRepository;

    public DatasetRecordServiceImpl(
            DatasetRecordRepository datasetRecordRepository) {
        this.datasetRecordRepository = datasetRecordRepository;
    }

    @Override
    @Transactional
    public CommonResponse<InsertRecordResponse> insertRecord(
            String datasetName, JsonNode record) {

        log.info("Inserting record into dataset: {}", datasetName);

        if (!StringUtils.hasText(datasetName)) {
            throw new InvalidRequestException(
                    "Dataset name must not be blank");
        }

        String normalizedDatasetName = datasetName.trim();

        if (normalizedDatasetName.length() > 150) {
            throw new InvalidRequestException(
                    "Dataset name must not exceed 150 characters");
        }

        if (record == null || !record.isObject()) {
            throw new InvalidRequestException(
                    "Request body must be a valid JSON object");
        }

        DatasetRecord entity = new DatasetRecord(
                normalizedDatasetName, record);

        DatasetRecord savedEntity =
                datasetRecordRepository.save(entity);

        InsertRecordResponse responseData = new InsertRecordResponse(
                savedEntity.getDatasetName(),
                savedEntity.getId()
        );

        log.info(
                "Record inserted successfully. Dataset: {}, Record ID: {}",
                savedEntity.getDatasetName(),
                savedEntity.getId());

        return new CommonResponse<>(
                201,
                "Record added successfully",
                responseData,
                true);
    }
}
