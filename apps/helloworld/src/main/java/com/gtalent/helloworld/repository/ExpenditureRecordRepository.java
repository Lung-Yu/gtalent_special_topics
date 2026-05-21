package com.gtalent.helloworld.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gtalent.helloworld.domain.model.ExpenditureRecord;
import com.gtalent.helloworld.service.entities.User;

public interface ExpenditureRecordRepository extends JpaRepository<ExpenditureRecord, Long> {

    Page<ExpenditureRecord> findByUser(User user, Pageable pageable);

    List<ExpenditureRecord> findByUser(User user);

    List<ExpenditureRecord> findByUserAndDate(User user, LocalDate date);

    List<ExpenditureRecord> findByDate(LocalDate date);

    @Query("SELECT COALESCE(SUM(e.money), 0) FROM ExpenditureRecord e WHERE e.user.id = :userId AND e.date = :date")
    int sumMoneyByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}
