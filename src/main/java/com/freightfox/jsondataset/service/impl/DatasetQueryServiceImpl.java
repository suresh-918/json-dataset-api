package com.freightfox.jsondataset.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.freightfox.jsondataset.dto.CommonResponse;
import com.freightfox.jsondataset.dto.DatasetQueryResponse;
import com.freightfox.jsondataset.entity.DatasetRecord;
import com.freightfox.jsondataset.exception.InvalidRequestException;
import com.freightfox.jsondataset.repository.DatasetRecordRepository;
import com.freightfox.jsondataset.service.DatasetQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DatasetQueryServiceImpl implements DatasetQueryService {

    private final DatasetRecordRepository datasetRecordRepository;

    public DatasetQueryServiceImpl(
            DatasetRecordRepository datasetRecordRepository) {
        this.datasetRecordRepository = datasetRecordRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CommonResponse<DatasetQueryResponse> queryDataset(
            String datasetName,
            String groupBy,
            String sortBy,
            String order) {

        if (!StringUtils.hasText(datasetName)) {
            throw new InvalidRequestException(
                    "Dataset name must not be blank");
        }

        datasetName = datasetName.trim();

        if (datasetName.length() > 150) {
            throw new InvalidRequestException(
                    "Dataset name must not exceed 150 characters");
        }

        boolean groupingRequested = StringUtils.hasText(groupBy);
        boolean sortingRequested = StringUtils.hasText(sortBy);

        if (groupingRequested == sortingRequested) {
            throw new InvalidRequestException(
                    "Provide exactly one parameter: groupBy or sortBy");
        }

        List<DatasetRecord> records =
                datasetRecordRepository.findByDatasetName(datasetName);

        DatasetQueryResponse responseData;

        if (groupingRequested) {
            responseData = groupRecords(records, groupBy.trim());
        } else {
            responseData = sortRecords(
                    records, sortBy.trim(), order);
        }

        log.info("Dataset query completed: {}", datasetName);

        return new CommonResponse<>(
                200,
                "Dataset queried successfully",
                responseData,
                true
        );
    }

    private DatasetQueryResponse groupRecords(
            List<DatasetRecord> records,
            String fieldName) {

        Map<String, List<JsonNode>> groupedRecords =
                new LinkedHashMap<>();

        for (DatasetRecord record : records) {
            JsonNode data = record.getRecordData();
            JsonNode fieldValue = data.get(fieldName);

            String groupKey;

            if (fieldValue == null) {
                groupKey = "(missing)";
            } else if (fieldValue.isNull()) {
                groupKey = "null";
            } else if (fieldValue.isTextual()) {
                groupKey = fieldValue.asText();
            } else {
                groupKey = fieldValue.toString();
            }

            groupedRecords
                    .computeIfAbsent(
                            groupKey, key -> new ArrayList<>())
                    .add(data);
        }

        return new DatasetQueryResponse(groupedRecords, null);
    }

    private DatasetQueryResponse sortRecords(
            List<DatasetRecord> records,
            String fieldName,
            String order) {

        String sortOrder =
                StringUtils.hasText(order)
                        ? order.trim().toLowerCase()
                        : "asc";

        if (!sortOrder.equals("asc")
                && !sortOrder.equals("desc")) {
            throw new InvalidRequestException(
                    "Order must be either 'asc' or 'desc'");
        }

        Comparator<DatasetRecord> comparator =
                (first, second) -> compareValues(
                        first.getRecordData().get(fieldName),
                        second.getRecordData().get(fieldName));

        if ("desc".equals(sortOrder)) {
            comparator = (first, second) -> compareValues(
                    second.getRecordData().get(fieldName),
                    first.getRecordData().get(fieldName));
        }

        List<JsonNode> sortedRecords = records.stream()
                .sorted(comparator)
                .map(DatasetRecord::getRecordData)
                .toList();

        return new DatasetQueryResponse(null, sortedRecords);
    }

    private int compareValues(JsonNode first, JsonNode second) {
        if (first == null || first.isNull()) {
            return (second == null || second.isNull()) ? 0 : 1;
        }

        if (second == null || second.isNull()) {
            return -1;
        }

        if (first.isNumber() && second.isNumber()) {
            return first.decimalValue()
                    .compareTo(second.decimalValue());
        }

        if (first.isTextual() && second.isTextual()) {
            return first.asText()
                    .compareToIgnoreCase(second.asText());
        }

        if (first.isBoolean() && second.isBoolean()) {
            return Boolean.compare(
                    first.booleanValue(),
                    second.booleanValue());
        }

        return first.toString().compareTo(second.toString());
    }
}
