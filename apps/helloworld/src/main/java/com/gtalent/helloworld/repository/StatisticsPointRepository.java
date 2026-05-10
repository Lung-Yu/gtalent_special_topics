package com.gtalent.helloworld.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gtalent.helloworld.domain.model.StatisticsPoint;

public interface StatisticsPointRepository extends JpaRepository<StatisticsPoint, Long> {

    List<StatisticsPoint> findByDate(LocalDate date);
}
