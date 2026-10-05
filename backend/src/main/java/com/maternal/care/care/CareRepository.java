package com.maternal.care.care;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CareRepository extends JpaRepository<CareTask, Long> {
    List<CareTask> findAllByOrderByPlanDateDescIdDesc();

    List<CareTask> findByPlanDateOrderByIdAsc(LocalDate planDate);

    long countByPlanDate(LocalDate planDate);

    long countByPlanDateAndStatus(LocalDate planDate, String status);
}
