package com.rk.WMS.batch.service;


import java.util.List;

public interface DispatchService {

    void manualDispatch(List<Long> orderIds, Long warehouseId);

    void autoDispatch();
}

