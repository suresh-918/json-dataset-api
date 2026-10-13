package com.freightfox.jsondataset.repository;

import com.freightfox.jsondataset.entity.DatasetRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DatasetRecordRepository extends JpaRepository<DatasetRecord, Long> {

    List<DatasetRecord> findByDatasetName(String datasetName);
}
