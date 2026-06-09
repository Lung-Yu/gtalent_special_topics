package com.gtalent.helloworld.service;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gtalent.helloworld.domain.model.Category;
import com.gtalent.helloworld.domain.model.ExpenditureRecord;
import com.gtalent.helloworld.domain.model.FileMetadata;
import com.gtalent.helloworld.domain.valueobject.PaymentMethod;
import com.gtalent.helloworld.repository.CategoryRepository;
import com.gtalent.helloworld.repository.ExpenditureRecordRepository;
import com.gtalent.helloworld.repository.FileMetadataRepository;
import com.gtalent.helloworld.service.entities.User;

@Service
@Transactional
public class ExpenditureService {

    private static final Logger log = LoggerFactory.getLogger(ExpenditureService.class);

    private final ExpenditureRecordRepository expenditureRecordRepository;
    private final CategoryRepository categoryRepository;
    private final FileMetadataRepository fileMetadataRepository;

    public ExpenditureService(ExpenditureRecordRepository expenditureRecordRepository,
                              CategoryRepository categoryRepository,
                              FileMetadataRepository fileMetadataRepository) {
        this.expenditureRecordRepository = expenditureRecordRepository;
        this.categoryRepository = categoryRepository;
        this.fileMetadataRepository = fileMetadataRepository;
    }

    public ExpenditureRecord create(User user, String name, int money,
                                    PaymentMethod payway, LocalDate date,
                                    List<String> categoryNames,
                                    List<Long> fileMetadataIds) {
        log.info("開始建立支出記錄: name={}, amount={}, payway={}, date={}", name, money, payway, date);

        ExpenditureRecord expenditureRecord = new ExpenditureRecord(user, name, money, payway, date);

        if (categoryNames != null && !categoryNames.isEmpty()) {
            List<Category> categories = categoryRepository.findByNameIn(categoryNames);
            expenditureRecord.setCategories(categories);
        }

        if (fileMetadataIds != null && !fileMetadataIds.isEmpty()) {
            List<FileMetadata> attachments = fileMetadataRepository.findAllById(fileMetadataIds);
            expenditureRecord.setAttachments(attachments);
        }

        ExpenditureRecord saved = expenditureRecordRepository.save(expenditureRecord);
        log.info("支出記錄已儲存至資料庫: id={}", saved.getId());
        return saved;
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
