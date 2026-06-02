package com.gtalent.helloworld.controller.resp;

import java.time.LocalDate;
import java.util.List;

import com.gtalent.helloworld.domain.model.Category;
import com.gtalent.helloworld.domain.model.ExpenditureRecord;
import com.gtalent.helloworld.domain.model.FileMetadata;
import com.gtalent.helloworld.domain.valueobject.PaymentMethod;

public class ExpenditureResp {

    public static class AttachmentInfo {
        private Long id;
        private String originalName;
        private String contentType;
        private Long fileSize;

        public static AttachmentInfo from(FileMetadata m) {
            AttachmentInfo info = new AttachmentInfo();
            info.id = m.getId();
            info.originalName = m.getOriginalName();
            info.contentType = m.getContentType();
            info.fileSize = m.getFileSize();
            return info;
        }

        public Long getId() { return id; }
        public String getOriginalName() { return originalName; }
        public String getContentType() { return contentType; }
        public Long getFileSize() { return fileSize; }
    }

    private Long id;
    private String username;
    private String name;
    private int money;
    private PaymentMethod payway;
    private LocalDate date;
    private List<String> categoryNames;
    private List<AttachmentInfo> attachments;

    public static ExpenditureResp from(ExpenditureRecord expenditureRecord) {
        ExpenditureResp resp = new ExpenditureResp();
        resp.id = expenditureRecord.getId();
        resp.username = expenditureRecord.getUser() != null ? expenditureRecord.getUser().getUsername() : null;
        resp.name = expenditureRecord.getName();
        resp.money = expenditureRecord.getMoney();
        resp.payway = expenditureRecord.getPayway();
        resp.date = expenditureRecord.getDate();
        resp.categoryNames = expenditureRecord.getCategories().stream()
                .map(Category::getName)
                .toList();
        resp.attachments = expenditureRecord.getAttachments().stream()
                .map(AttachmentInfo::from)
                .toList();
        return resp;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public int getMoney() { return money; }
    public PaymentMethod getPayway() { return payway; }
    public LocalDate getDate() { return date; }
    public List<String> getCategoryNames() { return categoryNames; }
    public List<AttachmentInfo> getAttachments() { return attachments; }
}
