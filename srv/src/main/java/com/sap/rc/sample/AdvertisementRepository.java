package com.sap.rc.sample;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface AdvertisementRepository
        extends PagingAndSortingRepository<Advertisement, Long>, AdvertisementRepositoryCustom {

}