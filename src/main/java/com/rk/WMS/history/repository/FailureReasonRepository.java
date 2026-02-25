package com.rk.WMS.history.repository;

import com.rk.WMS.history.model.FailureReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FailureReasonRepository extends JpaRepository<FailureReason, Long> {
}