-- Hibernate TABLE-strategy sequence table (used by GenerationType.AUTO on MySQL)
CREATE TABLE hibernate_sequences (
    sequence_name VARCHAR(255) NOT NULL,
    next_val      BIGINT,
    PRIMARY KEY (sequence_name)
);

-- ──────────────────────────────────────────────
-- Tables with no FK dependencies
-- ──────────────────────────────────────────────

-- 'user' is a MySQL reserved word, so it must be quoted
CREATE TABLE `user` (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255),
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    username   VARCHAR(255),
    email      VARCHAR(255),
    password   VARCHAR(255),
    created_at DATETIME(6),
    PRIMARY KEY (id)
);

-- IDs managed by hibernate_sequences (no AUTO_INCREMENT)
CREATE TABLE employee (
    id         INT,
    name       VARCHAR(255),
    age        VARCHAR(255),
    created_at DATETIME(6),
    PRIMARY KEY (id)
);

-- @Entity(name="orders") → table name is 'orders'
CREATE TABLE orders (
    id         INT,
    name       VARCHAR(255),
    quantity   INT    NOT NULL,
    price      DOUBLE NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE posts (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    title      VARCHAR(200) NOT NULL,
    content    TEXT         NOT NULL,
    created_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE verify_code (
    id              BIGINT       NOT NULL,
    secret          VARCHAR(255) NOT NULL,
    service_id      VARCHAR(255) NOT NULL DEFAULT 'default',
    last_updated_at DATETIME(6)  NOT NULL,
    last_code       VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE stored_files (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    hash       VARCHAR(64) NOT NULL,
    path       VARCHAR(255) NOT NULL,
    created_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_stored_files_hash (hash)
);

CREATE TABLE upload_sessions (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    upload_id       VARCHAR(36)  NOT NULL,
    original_name   VARCHAR(255) NOT NULL,
    content_type    VARCHAR(255),
    total_size      BIGINT       NOT NULL,
    received_bytes  BIGINT       NOT NULL,
    status          VARCHAR(16)  NOT NULL,
    created_at      DATETIME(6),
    expired_at      DATETIME(6)  NOT NULL,
    minio_upload_id VARCHAR(512),
    part_etags      TEXT,
    PRIMARY KEY (id),
    UNIQUE KEY uk_upload_sessions_upload_id (upload_id)
);

CREATE TABLE inventories (
    id   VARCHAR(8)   NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE budget_settings (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    user_id      BIGINT NOT NULL,
    threshold    INT    NOT NULL,
    alert_hour   INT    NOT NULL,
    alert_minute INT    NOT NULL,
    enabled      BIT    NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_budget_settings_user_id (user_id)
);

CREATE TABLE budget_alert_logs (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    user_id         BIGINT      NOT NULL,
    date            DATE        NOT NULL,
    total_spent     INT         NOT NULL,
    status          VARCHAR(16) NOT NULL,
    retry_count     INT         NOT NULL DEFAULT 0,
    last_attempt_at DATETIME(6),
    created_at      DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE statistics_points (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    user_id       BIGINT,
    username      VARCHAR(255),
    amount        INT          NOT NULL,
    category_name VARCHAR(255) NOT NULL,
    calculated_at DATETIME(6)  NOT NULL,
    date          DATE         NOT NULL,
    PRIMARY KEY (id)
);

-- ──────────────────────────────────────────────
-- Tables that reference `user`
-- ──────────────────────────────────────────────

-- IDs managed by hibernate_sequences (no AUTO_INCREMENT)
CREATE TABLE product (
    id         BIGINT       NOT NULL,
    name       VARCHAR(255),
    price      DOUBLE       NOT NULL,
    created_at DATETIME(6),
    created_by BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_product_user FOREIGN KEY (created_by) REFERENCES `user` (id)
);

CREATE TABLE categories (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    name               VARCHAR(255) NOT NULL,
    icon               VARCHAR(255),
    type               VARCHAR(16)  NOT NULL,
    created_at         DATETIME(6),
    created_by_user_id BIGINT,
    PRIMARY KEY (id),
    UNIQUE KEY uk_categories_type_name (type, name),
    CONSTRAINT fk_category_user FOREIGN KEY (created_by_user_id) REFERENCES `user` (id)
);

CREATE TABLE expenditure_records (
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    user_id BIGINT       NOT NULL,
    saysay  VARCHAR(255) NOT NULL,
    name    VARCHAR(255) NOT NULL,
    money   INT          NOT NULL,
    payway  VARCHAR(32)  NOT NULL,
    date    DATE         NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_expenditure_user FOREIGN KEY (user_id) REFERENCES `user` (id)
);

-- ──────────────────────────────────────────────
-- Tables that reference posts
-- ──────────────────────────────────────────────

CREATE TABLE comments (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    author     VARCHAR(255) NOT NULL,
    content    TEXT         NOT NULL,
    created_at DATETIME(6),
    post_id    BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES posts (id)
);

-- ──────────────────────────────────────────────
-- Tables that reference stored_files
-- ──────────────────────────────────────────────

CREATE TABLE file_metadata (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    stored_file_id BIGINT       NOT NULL,
    original_name  VARCHAR(255) NOT NULL,
    content_type   VARCHAR(255),
    file_size      BIGINT,
    uploaded_at    DATETIME(6),
    uploaded_by    VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_file_metadata_stored_file FOREIGN KEY (stored_file_id) REFERENCES stored_files (id)
);

-- ──────────────────────────────────────────────
-- Join tables
-- ──────────────────────────────────────────────

CREATE TABLE expenditure_categories (
    expenditure_record_id BIGINT NOT NULL,
    category_id           BIGINT NOT NULL,
    PRIMARY KEY (expenditure_record_id, category_id),
    CONSTRAINT fk_exp_cat_record   FOREIGN KEY (expenditure_record_id) REFERENCES expenditure_records (id),
    CONSTRAINT fk_exp_cat_category FOREIGN KEY (category_id)           REFERENCES categories (id)
);

CREATE TABLE expenditure_attachments (
    expenditure_record_id BIGINT NOT NULL,
    file_metadata_id      BIGINT NOT NULL,
    PRIMARY KEY (expenditure_record_id, file_metadata_id),
    CONSTRAINT fk_exp_att_record FOREIGN KEY (expenditure_record_id) REFERENCES expenditure_records (id),
    CONSTRAINT fk_exp_att_file   FOREIGN KEY (file_metadata_id)      REFERENCES file_metadata (id)
);
