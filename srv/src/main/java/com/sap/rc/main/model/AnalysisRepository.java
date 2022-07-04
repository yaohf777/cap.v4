package com.sap.rc.main.model;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface AnalysisRepository
        extends PagingAndSortingRepository<Analysis, Long> {
    
}
