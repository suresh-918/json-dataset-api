package com.freightfox.jsondataset.controller;

import com.freightfox.jsondataset.dto.CommonResponse;
import com.freightfox.jsondataset.dto.DatasetQueryResponse;
import com.freightfox.jsondataset.service.DatasetQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dataset")
public class DatasetQueryController {

    private final DatasetQueryService datasetQueryService;

    public DatasetQueryController(
            DatasetQueryService datasetQueryService) {
        this.datasetQueryService = datasetQueryService;
    }

    @GetMapping("/{datasetName}/query")
    public ResponseEntity<CommonResponse<DatasetQueryResponse>> queryDataset(
            @PathVariable String datasetName,
            @RequestParam(required = false) String groupBy,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String order) {

        CommonResponse<DatasetQueryResponse> response =
                datasetQueryService.queryDataset(
                        datasetName,
                        groupBy,
                        sortBy,
                        order
                );

        return ResponseEntity
                .status(HttpStatus.valueOf(response.getStatus()))
                .body(response);
    }
}
