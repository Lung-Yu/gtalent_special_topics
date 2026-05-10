package com.gtalent.helloworld.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gtalent.helloworld.domain.model.Category;
import com.gtalent.helloworld.domain.model.ExpenditureRecord;
import com.gtalent.helloworld.domain.valueobject.PaymentMethod;
import com.gtalent.helloworld.repository.CategoryRepository;
import com.gtalent.helloworld.repository.ExpenditureRecordRepository;
import com.gtalent.helloworld.service.entities.User;

@Service
@Transactional
public class ExpenditureService {

    private final ExpenditureRecordRepository expenditureRecordRepository;
    private final CategoryRepository categoryRepository;

    public ExpenditureService(ExpenditureRecordRepository expenditureRecordRepository,
                              CategoryRepository categoryRepository) {
        this.expenditureRecordRepository = expenditureRecordRepository;
        this.categoryRepository = categoryRepository;
    }

    public ExpenditureRecord create(User user, String name, int money,
                                    PaymentMethod payway, LocalDate date,
                                    List<String> categoryNames) {
        ExpenditureRecord expenditureRecord = new ExpenditureRecord(user, name, money, payway, date);

        if (categoryNames != null && !categoryNames.isEmpty()) {
            List<Category> categories = categoryRepository.findByNameIn(categoryNames);
            expenditureRecord.setCategories(categories);
        }

        return expenditureRecordRepository.save(expenditureRecord);
    }

    @Transactional(readOnly = true)
    public Page<ExpenditureRecord> findByUser(User user, Pageable pageable) {
        return expenditureRecordRepository.findByUser(user, pageable);
    }

    @Transactional(readOnly = true)
    public List<ExpenditureRecord> findByUserAndDate(User user, LocalDate date) {
        return expenditureRecordRepository.findByUserAndDate(user, date);
    }

    @Transactional(readOnly = true)
    public List<ExpenditureRecord> findByUser(User user) {
        return expenditureRecordRepository.findByUser(user);
    }

    public void delete(Long id) {
        expenditureRecordRepository.deleteById(id);
    }
}
