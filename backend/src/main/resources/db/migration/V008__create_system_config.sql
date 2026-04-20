CREATE TABLE IF NOT EXISTS system_config (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  config_key varchar(100) NOT NULL UNIQUE,
  config_value text NOT NULL,
  description varchar(500),
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO system_config (config_key, config_value, description) VALUES 
('EXAM_REPORT_URL', 'http://sichuangpacs.pkuih.edu.cn/IntegrationCenter/index.html#/views/eipTimeLine2?CMD=showlist&PW=webweb&DOMAINCODE=patientid&DOMAINID={{:patient_no}}', '检查报告查看URL'),
('TEST_REPORT_URL', 'http://10.2.48.64:8001/cdr/login/loginiihPortalIntegrated.html?viewId=V002&patientId={{:patient_no}}&domainId=02&styleId=01&display=0&userId={{:userId}}&visitTimes=&XExternalUrlFlag=1&systemId=SIIH&bsiihtype=0&download=1&visitTimes=', '检验报告查看URL');