ALTER TABLE vital_signs ADD COLUMN his_id VARCHAR(50);

CREATE INDEX idx_his_id ON vital_signs(his_id);