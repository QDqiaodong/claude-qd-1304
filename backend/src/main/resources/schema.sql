SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

CREATE DATABASE IF NOT EXISTS maternal_care
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE maternal_care;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS care;
DROP TABLE IF EXISTS meal;
DROP TABLE IF EXISTS maternal;
DROP TABLE IF EXISTS room;
SET FOREIGN_KEY_CHECKS = 1;

-- 房间：父表，须先于产妇建表
CREATE TABLE room (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_no      VARCHAR(32) NOT NULL UNIQUE COMMENT '房号',
    type         VARCHAR(16) NOT NULL COMMENT '房型：标准间/单间/套房',
    floor        VARCHAR(32) NOT NULL COMMENT '楼层',
    monthly_fee  DECIMAL(10,2) NOT NULL COMMENT '月费，必须大于0',
    status       VARCHAR(16) NOT NULL COMMENT '状态：空闲/在住/清洁中/维修',
    created_at   DATETIME NOT NULL COMMENT '创建时间',
    updated_at   DATETIME NOT NULL COMMENT '更新时间',
    CONSTRAINT chk_room_monthly_fee CHECK (monthly_fee > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='月子中心房间';

-- 产妇：入住时仅允许关联空闲房间，已出院和退住为终态
CREATE TABLE maternal (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(64) NOT NULL COMMENT '产妇姓名',
    phone            VARCHAR(32) NOT NULL COMMENT '联系电话',
    room_id          BIGINT NOT NULL COMMENT '房间ID',
    check_in_date    DATE NOT NULL COMMENT '入住日期',
    due_date         DATE NOT NULL COMMENT '预产期',
    baby_birth_date  DATE NULL COMMENT '分娩日期，可空且不得早于入住日期',
    status           VARCHAR(16) NOT NULL COMMENT '状态：待入住/在住/已出院/退住',
    created_at       DATETIME NOT NULL COMMENT '创建时间',
    updated_at       DATETIME NOT NULL COMMENT '更新时间',
    INDEX idx_maternal_room (room_id),
    CONSTRAINT fk_maternal_room FOREIGN KEY (room_id) REFERENCES room (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产妇档案';

-- 护理任务：仅允许给在住产妇排任务
CREATE TABLE care (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    maternal_id  BIGINT NOT NULL COMMENT '产妇ID',
    nurse        VARCHAR(64) NOT NULL COMMENT '责任护士',
    care_type    VARCHAR(32) NOT NULL COMMENT '护理类型：伤口护理/乳房护理/新生儿护理/产后恢复/心理疏导',
    plan_date    DATE NOT NULL COMMENT '计划护理日期',
    status       VARCHAR(16) NOT NULL COMMENT '状态：待执行/已完成/已跳过',
    finish_time  DATETIME NULL COMMENT '完成时间',
    created_at   DATETIME NOT NULL COMMENT '创建时间',
    updated_at   DATETIME NOT NULL COMMENT '更新时间',
    INDEX idx_care_maternal (maternal_id),
    INDEX idx_care_plan_date (plan_date),
    CONSTRAINT fk_care_maternal FOREIGN KEY (maternal_id) REFERENCES maternal (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护理任务';

-- 月子餐：仅允许给在住产妇配餐
CREATE TABLE meal (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    maternal_id  BIGINT NOT NULL COMMENT '产妇ID',
    meal_name    VARCHAR(96) NOT NULL COMMENT '餐名',
    meal_type    VARCHAR(16) NOT NULL COMMENT '餐别：早餐/午餐/晚餐/加餐',
    serve_date   DATE NOT NULL COMMENT '配送日期',
    calories     INT NOT NULL COMMENT '热量，单位千卡且必须大于0',
    status       VARCHAR(16) NOT NULL COMMENT '状态：待配送/已配送/已取消',
    created_at   DATETIME NOT NULL COMMENT '创建时间',
    updated_at   DATETIME NOT NULL COMMENT '更新时间',
    INDEX idx_meal_maternal (maternal_id),
    INDEX idx_meal_serve_date (serve_date),
    CONSTRAINT chk_meal_calories CHECK (calories > 0),
    CONSTRAINT fk_meal_maternal FOREIGN KEY (maternal_id) REFERENCES maternal (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='月子餐';

-- 房间种子：含空闲、在住、维修、清洁中，便于验证仅空闲可入住
INSERT INTO room (id, room_no, type, floor, monthly_fee, status, created_at, updated_at) VALUES
(1, '101', '标准间', '一层', 12800.00, '在住',   NOW(), NOW()),
(2, '102', '单间',   '一层', 16800.00, '空闲',   NOW(), NOW()),
(3, '201', '套房',   '二层', 26800.00, '维修',   NOW(), NOW()),
(4, '202', '标准间', '二层', 13800.00, '清洁中', NOW(), NOW()),
(5, '301', '套房',   '三层', 28800.00, '清洁中', NOW(), NOW()),
(6, '302', '单间',   '三层', 17800.00, '清洁中', NOW(), NOW());

-- 产妇种子：含在住、待入住、已出院、退住，以及分别关联维修/清洁中房间的待入住记录
INSERT INTO maternal (id, name, phone, room_id, check_in_date, due_date, baby_birth_date, status, created_at, updated_at) VALUES
(1, '王静', '13800001001', 1, DATE_SUB(CURDATE(), INTERVAL 12 DAY), DATE_SUB(CURDATE(), INTERVAL 3 DAY),  DATE_SUB(CURDATE(), INTERVAL 2 DAY),  '在住',   NOW(), NOW()),
(2, '李娜', '13800001002', 2, CURDATE(),                              DATE_SUB(CURDATE(), INTERVAL -5 DAY), NULL,                                    '待入住', NOW(), NOW()),
(3, '赵敏', '13800001003', 5, DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_SUB(CURDATE(), INTERVAL 18 DAY), '已出院', NOW(), NOW()),
(4, '陈芳', '13800001004', 6, DATE_SUB(CURDATE(), INTERVAL 25 DAY), DATE_SUB(CURDATE(), INTERVAL 16 DAY), DATE_SUB(CURDATE(), INTERVAL 15 DAY), '退住',   NOW(), NOW()),
(5, '周琳', '13800001005', 3, CURDATE(),                              DATE_SUB(CURDATE(), INTERVAL -7 DAY), NULL,                                    '待入住', NOW(), NOW()),
(6, '孙悦', '13800001006', 4, CURDATE(),                              DATE_SUB(CURDATE(), INTERVAL -10 DAY), NULL,                                   '待入住', NOW(), NOW());

-- 今日护理清单共5项，已完成2项，初始完成率40%；含已跳过任务验证不可回退
INSERT INTO care (id, maternal_id, nurse, care_type, plan_date, status, finish_time, created_at, updated_at) VALUES
(1, 1, '刘护士', '伤口护理',   CURDATE(),                              '待执行', NULL,  NOW(), NOW()),
(2, 1, '刘护士', '乳房护理',   CURDATE(),                              '已完成', NOW(), NOW(), NOW()),
(3, 1, '张护士', '新生儿护理', CURDATE(),                              '已完成', NOW(), NOW(), NOW()),
(4, 1, '张护士', '产后恢复',   CURDATE(),                              '已跳过', NULL,  NOW(), NOW()),
(5, 1, '陈护士', '心理疏导',   CURDATE(),                              '待执行', NULL,  NOW(), NOW()),
(6, 1, '陈护士', '产后恢复',   DATE_SUB(CURDATE(), INTERVAL 1 DAY), '已完成', NOW(), NOW(), NOW());

-- 月子餐种子：含待配送、已配送、已取消，验证取消后不可配送且配送后不可修改
INSERT INTO meal (id, maternal_id, meal_name, meal_type, serve_date, calories, status, created_at, updated_at) VALUES
(1, 1, '小米南瓜粥配蒸蛋', '早餐', CURDATE(),                              420, '待配送', NOW(), NOW()),
(2, 1, '鲫鱼豆腐汤配杂粮饭', '午餐', CURDATE(),                              680, '已配送', NOW(), NOW()),
(3, 1, '山药炖鸡配时蔬',     '晚餐', CURDATE(),                              620, '已取消', NOW(), NOW()),
(4, 1, '红枣银耳羹',         '加餐', CURDATE(),                              260, '待配送', NOW(), NOW()),
(5, 1, '黑芝麻糊配全麦包',   '早餐', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 390, '已配送', NOW(), NOW()),
(6, 1, '清蒸鲈鱼套餐',       '晚餐', DATE_SUB(CURDATE(), INTERVAL -1 DAY), 610, '待配送', NOW(), NOW());
