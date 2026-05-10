package com.gtalent.helloworld.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gtalent.helloworld.domain.model.ExpenditureRecord;
import com.gtalent.helloworld.service.entities.User;

public interface ExpenditureRecordRepository extends JpaRepository<ExpenditureRecord, Long> {

    Page<ExpenditureRecord> findByUser(User user, Pageable pageable);

    List<ExpenditureRecord> findByUser(User user);

    List<ExpenditureRecord> findByUserAndDate(User user, LocalDate date);

    List<ExpenditureRecord> findByDate(LocalDate date);
}
