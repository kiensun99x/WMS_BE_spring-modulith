package com.rk.WMS.order.repository;

import com.rk.WMS.order.model.ErrorFileImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorFileImportRepository extends JpaRepository<ErrorFileImport, Long> {

}
