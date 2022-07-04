package com.sap.rc.sample;

import java.util.List;

public interface AdvertisementRepositoryCustom {
    List<Advertisement> findByTitle(String string);
}
