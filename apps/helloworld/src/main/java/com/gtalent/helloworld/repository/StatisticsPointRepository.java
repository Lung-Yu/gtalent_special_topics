package com.gtalent.helloworld.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gtalent.helloworld.domain.model.StatisticsPoint;

public interface StatisticsPointRepository extends JpaRepository<StatisticsPoint, Long> {

    Page<StatisticsPoint> findByDate(LocalDate date, Pageable pageable);

    List<StatisticsPoint> findByDate(LocalDate date);
}
