package com.rk.WMS.batch.service;


import java.util.List;

public interface DispatchService {

    /**
     * Manual dispatch orders to a specific warehouse
     */
    void manualDispatch(List<Integer> orderIds, Integer warehouseId);

    /**
     * Auto dispatch orders
     */
    void autoDispatch();
}

