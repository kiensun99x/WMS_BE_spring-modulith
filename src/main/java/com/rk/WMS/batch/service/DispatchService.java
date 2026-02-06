package com.rk.WMS.batch.service;


import java.util.List;

public interface DispatchService {

    void manualDispatch(List<Integer> orderIds, Integer warehouseId);

    void autoDispatch();
}

