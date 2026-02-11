package com.rk.WMS.history.repository;

import com.rk.WMS.history.model.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {


}
