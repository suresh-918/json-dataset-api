package com.freightfox.jsondataset.service;

import com.freightfox.jsondataset.dto.CommonResponse;
import com.freightfox.jsondataset.dto.DatasetQueryResponse;

public interface DatasetQueryService {

    CommonResponse<DatasetQueryResponse> queryDataset(
            String datasetName,
            String groupBy,
            String sortBy,
            String order
    );
}