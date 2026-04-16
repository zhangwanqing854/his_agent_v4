-- 短信发送日志表
CREATE TABLE IF NOT EXISTS sms_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    handover_id BIGINT NOT NULL COMMENT '关联的交班记录ID',
    phone VARCHAR(20) NOT NULL COMMENT '接收短信的电话号码',
    content TEXT NOT NULL COMMENT '短信内容',
    status VARCHAR(20) NOT NULL COMMENT '发送状态: SUCCESS/FAILED',
    error_message TEXT COMMENT '错误信息',
    sent_at DATETIME COMMENT '发送时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信发送日志表';

-- 短信配置表
CREATE TABLE IF NOT EXISTS sms_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(50) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT NOT NULL COMMENT '配置值',
    description VARCHAR(200) COMMENT '配置说明',
    is_sensitive BOOLEAN DEFAULT FALSE COMMENT '是否敏感字段(密钥等)',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    updated_by BIGINT COMMENT '更新人ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信配置表';

-- 初始化默认配置数据
INSERT INTO sms_config (config_key, config_value, description, is_sensitive) VALUES
('enabled', 'false', '是否启用短信功能', false),
('provider', 'aliyun', '短信平台: aliyun/tencent', false),
('aliyun_access_key_id', '', '阿里云 AccessKey ID', true),
('aliyun_access_key_secret', '', '阿里云 AccessKey Secret', true),
('aliyun_sign_name', '北京大学国际医院', '短信签名', false),
('aliyun_template_code', '', '短信模板Code', false)
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);