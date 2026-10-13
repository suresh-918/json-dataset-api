package com.freightfox.jsondataset.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.freightfox.jsondataset.dto.CommonResponse;
import com.freightfox.jsondataset.dto.InsertRecordResponse;
import com.freightfox.jsondataset.service.DatasetRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dataset")
public class DatasetRecordController {

    private final DatasetRecordService datasetRecordService;

    public DatasetRecordController(
            DatasetRecordService datasetRecordService) {
        this.datasetRecordService = datasetRecordService;
    }

    @PostMapping("/{datasetName}/record")
    public ResponseEntity<CommonResponse<InsertRecordResponse>> insertRecord(
            @PathVariable String datasetName,
            @RequestBody JsonNode record) {

        CommonResponse<InsertRecordResponse> response =
                datasetRecordService.insertRecord(datasetName, record);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
