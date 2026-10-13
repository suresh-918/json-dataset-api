package com.freightfox.jsondataset.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.freightfox.jsondataset.dto.CommonResponse;
import com.freightfox.jsondataset.dto.InsertRecordResponse;

public interface DatasetRecordService {

    CommonResponse<InsertRecordResponse> insertRecord(
            String datasetName,
            JsonNode record
    );

}
