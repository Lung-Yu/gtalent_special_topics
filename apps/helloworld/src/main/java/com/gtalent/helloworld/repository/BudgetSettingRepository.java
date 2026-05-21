package com.gtalent.helloworld.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gtalent.helloworld.domain.model.BudgetSetting;

public interface BudgetSettingRepository extends JpaRepository<BudgetSetting, Long> {

    Optional<BudgetSetting> findByUserId(Long userId);

    List<BudgetSetting> findAllByEnabledTrue();
}
