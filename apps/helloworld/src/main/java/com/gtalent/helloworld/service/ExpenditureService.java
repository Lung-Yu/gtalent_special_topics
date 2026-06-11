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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
@Transactional
public class ExpenditureService {

    private static final Logger log = LoggerFactory.getLogger(ExpenditureService.class);

    private final ExpenditureRecordRepository expenditureRecordRepository;
    private final CategoryRepository categoryRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final MeterRegistry meterRegistry;

    public ExpenditureService(ExpenditureRecordRepository expenditureRecordRepository,
                              CategoryRepository categoryRepository,
                              FileMetadataRepository fileMetadataRepository,
                              MeterRegistry meterRegistry) {
        this.expenditureRecordRepository = expenditureRecordRepository;
        this.categoryRepository = categoryRepository;
        this.fileMetadataRepository = fileMetadataRepository;
        this.meterRegistry = meterRegistry;

        // 啟動時註冊 Gauge，Micrometer 每次 scrape 時自動呼叫 count()
        io.micrometer.core.instrument.Gauge.builder("expenditure.records.total",
                        expenditureRecordRepository, repo -> (double) repo.count())
                .description("資料庫中支出記錄總筆數")
                .register(meterRegistry);
    }

    public ExpenditureRecord create(User user, String name, int money,
                                    PaymentMethod payway, LocalDate date,
                                    List<String> categoryNames,
                                    List<Long> fileMetadataIds) {
        log.info("開始建立支出記錄: name={}, amount={}, payway={}, date={}", name, money, payway, date);

        String paywayTag = payway != null ? payway.name() : "UNKNOWN";

        Timer.Sample sample = Timer.start(meterRegistry);
        try {
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

            Counter.builder("expenditure.create.count")
                    .description("支出記錄建立次數")
                    .tag("payway", paywayTag)
                    .register(meterRegistry)
                    .increment();

            DistributionSummary.builder("expenditure.amount")
                    .description("支出金額分佈")
                    .tag("payway", paywayTag)
                    .baseUnit("TWD")
                    .register(meterRegistry)
                    .record(money);

            return saved;
        } finally {
            sample.stop(Timer.builder("expenditure.create.duration")
                    .description("建立支出記錄耗時")
                    .tag("payway", paywayTag)
                    .register(meterRegistry));
        }
    }

    @Transactional(readOnly = true)
    public Page<ExpenditureRecord> findByUser(User user, Pageable pageable) {
        Counter.builder("expenditure.query.count")
                .description("支出記錄查詢次數")
                .tag("type", "page")
                .register(meterRegistry)
                .increment();
        return expenditureRecordRepository.findByUser(user, pageable);
    }

    @Transactional(readOnly = true)
    public List<ExpenditureRecord> findByUserAndDate(User user, LocalDate date) {
        Counter.builder("expenditure.query.count")
                .description("支出記錄查詢次數")
                .tag("type", "by_date")
                .register(meterRegistry)
                .increment();
        return expenditureRecordRepository.findByUserAndDate(user, date);
    }

    @Transactional(readOnly = true)
    public List<ExpenditureRecord> findByUser(User user) {
        return expenditureRecordRepository.findByUser(user);
    }

    public void delete(Long id) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            expenditureRecordRepository.deleteById(id);
            Counter.builder("expenditure.delete.count")
                    .description("支出記錄刪除次數")
                    .register(meterRegistry)
                    .increment();
        } finally {
            sample.stop(Timer.builder("expenditure.delete.duration")
                    .description("刪除支出記錄耗時")
                    .register(meterRegistry));
        }
    }
}
