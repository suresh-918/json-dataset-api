package com.freightfox.jsondataset.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DatasetQueryResponse(
        Map<String, List<JsonNode>> groupedRecords,
        List<JsonNode> sortedRecords
) {
}