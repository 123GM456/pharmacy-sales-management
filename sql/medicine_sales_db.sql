-- =============================================================
-- 医药销售管理系统 · 数据库初始化脚本
-- 数据库：medicine_sales_db
-- 字符集：utf8mb4   存储引擎：InnoDB
-- 执行方式：mysql -u root -p < sql/medicine_sales_db.sql
-- 脚本可重复执行（会先删除同名表）
-- =============================================================

CREATE DATABASE IF NOT EXISTS medicine_sales_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE medicine_sales_db;

-- 按外键依赖的逆序删除，保证脚本可以重复执行
DROP TABLE IF EXISTS sale_record;
DROP TABLE IF EXISTS medicine;
DROP TABLE IF EXISTS sys_user;

-- -------------------------------------------------------------
-- 1. sys_user 用户表
-- -------------------------------------------------------------
CREATE TABLE sys_user (
    id          INT          NOT NULL AUTO_INCREMENT     COMMENT '用户编号',
    username    VARCHAR(50)  NOT NULL                    COMMENT '登录用户名',
    password    VARCHAR(100) NOT NULL                    COMMENT '登录密码（BCrypt哈希）',
    real_name   VARCHAR(50)  NOT NULL                    COMMENT '用户真实姓名',
    phone       VARCHAR(20)  NOT NULL                    COMMENT '用户手机号',
    role        TINYINT      DEFAULT 0                   COMMENT '用户角色:1管理员，0普通用户',
    status      TINYINT      DEFAULT 1                   COMMENT '用户状态：1启用，0禁用',
    created_time DATETIME     DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
    updated_time DATETIME     DEFAULT CURRENT_TIMESTAMP   COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- -------------------------------------------------------------
-- 2. medicine 药品表
-- -------------------------------------------------------------
CREATE TABLE medicine (
    id              INT           NOT NULL AUTO_INCREMENT   COMMENT '药品编号',
    name            VARCHAR(100)  NOT NULL                  COMMENT '药品名称',
    category        VARCHAR(50)   NOT NULL                  COMMENT '药品类别',
    specification   VARCHAR(100)  NOT NULL                  COMMENT '药品规格',
    manufacturer    VARCHAR(100)  NOT NULL                  COMMENT '生产厂家',
    batch_number    VARCHAR(50)   NOT NULL                  COMMENT '生产批号',
    purchase_price  DECIMAL(10,2) NOT NULL                  COMMENT '进货价格',
    sale_price      DECIMAL(10,2) NOT NULL                  COMMENT '销售价格',
    stock           INT           NOT NULL DEFAULT 0        COMMENT '当前库存',
    warning_stock   INT           NOT NULL DEFAULT 10       COMMENT '库存预警值',
    production_date DATE          NOT NULL                  COMMENT '生产日期',
    expiry_date     DATE          NOT NULL                  COMMENT '有效期',
    status          TINYINT       DEFAULT 1                 COMMENT '药品状态：1在售，0停用',
    created_time    DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time    DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    KEY idx_medicine_name (name),
    KEY idx_medicine_category (category)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '药品表';

-- -------------------------------------------------------------
-- 3. sale_record 销售记录表
-- -------------------------------------------------------------
CREATE TABLE sale_record (
    id           INT           NOT NULL AUTO_INCREMENT     COMMENT '销售记录编号',
    medicine_id  INT           NOT NULL                    COMMENT '药品编号',
    operator_id  INT           NOT NULL                    COMMENT '操作员编号',
    quantity     INT           NOT NULL                    COMMENT '销售数量',
    unit_price   DECIMAL(10,2) NOT NULL                    COMMENT '销售单价',
    total_amount DECIMAL(12,2) NOT NULL                    COMMENT '销售总金额',
    sale_time    DATETIME      NOT NULL                    COMMENT '销售时间',
    remark       VARCHAR(255)                              COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_sale_medicine (medicine_id),
    KEY idx_sale_operator (operator_id),
    KEY idx_sale_time (sale_time),
    CONSTRAINT fk_sale_medicine FOREIGN KEY (medicine_id) REFERENCES medicine (id),
    CONSTRAINT fk_sale_operator FOREIGN KEY (operator_id) REFERENCES sys_user (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '销售记录表';

-- =============================================================
-- 初始化测试数据
-- =============================================================
-- password 列存放 BCrypt 哈希串：admin 对应明文 admin123，cashier 对应明文 123456
INSERT INTO sys_user (username, password, real_name, phone, role, status) VALUES
('admin',   '$2a$10$6bONxgpAYyL2dQDJVXcA8ecnwQ8nk6xKwhB6yytzp3yoYUOg25L1a', '系统管理员', '13800138001', 1, 1),
('cashier', '$2a$10$59uS1fa.A4LG6owMnvD02ehzgPlG0BEPxhonfI.L6Fx3R168LMzka', '张小明',    '13900139002', 0, 1);

INSERT INTO medicine
    (name, category, specification, manufacturer, batch_number, purchase_price, sale_price,
     stock, warning_stock, production_date, expiry_date, status)
VALUES
('阿莫西林胶囊',   '抗生素',   '0.25g*24粒',  '华北制药股份有限公司',       '20250315', 8.50, 12.50, 100, 20, '2025-03-15', '2027-03-14', 1),
('布洛芬缓释胶囊', '解热镇痛', '0.3g*20粒',   '中美天津史克制药有限公司',   '20250502', 6.00,  9.90,  60, 15, '2025-05-02', '2027-05-01', 1),
('维生素C片',      '维生素类', '100mg*100片', '哈药集团制药总厂',           '20250118', 3.20,  5.50,   8, 10, '2025-01-18', '2027-01-17', 1);

INSERT INTO sale_record
    (medicine_id, operator_id, quantity, unit_price, total_amount, sale_time, remark)
VALUES
(1, 2, 5, 12.50, 62.50, '2026-09-01 10:20:00', '现金支付'),
(2, 2, 2,  9.90, 19.80, '2026-09-05 15:40:00', NULL);

-- =============================================================
-- 数据验证（可选，手动取消注释后执行）
-- =============================================================

-- ① 查看所有数据表，确认3张表（sys_user、medicine、sale_record）均已成功创建
-- SHOW TABLES;

-- ② 检查用户表初始化数据是否正确插入（预期：2条记录 —— admin 和 cashier）
-- SELECT * FROM sys_user;

-- ③ 检查药品表初始化数据是否正确插入（预期：3条记录 —— 阿莫西林、布洛芬、维生素C）
-- SELECT * FROM medicine;

-- ④ 执行销售汇总查询，验证多表关联 JOIN 语法及聚合函数是否正常工作
--    该查询会统计 2026 年 9 月期间每种药品的总销售数量和总销售金额
-- SELECT m.name, SUM(s.quantity) AS total_qty, SUM(s.total_amount) AS total_amount
--   FROM sale_record s JOIN medicine m ON m.id = s.medicine_id
--  WHERE s.sale_time BETWEEN '2026-09-01 00:00:00' AND '2026-09-30 23:59:59'
--  GROUP BY m.name;