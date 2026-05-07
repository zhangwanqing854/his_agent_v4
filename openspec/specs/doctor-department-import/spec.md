## 新增需求

### 需求:用户可以导入科室人员关系CSV文件

系统必须允许用户上传符合模板格式的 CSV 文件批量导入科室人员关系到 doctor_department 表。

#### 场景:成功上传CSV文件
- **当** 用户在系统设置页面数据导入 Tab 点击"导入科室人员关系"按钮
- **那么** 系统显示科室人员关系导入对话框，支持拖拽或点击上传 CSV 文件

#### 场景:文件格式校验失败
- **当** 用户上传非 CSV 格式文件
- **那么** 系统提示"仅支持 CSV 格式文件"并清空已上传文件

#### 场景:文件大小超限
- **当** 用户上传的 CSV 文件数据超过 1000 条
- **那么** 系统提示"单次导入最多支持 1000 条数据，请分批导入"并拒绝导入

### 需求:系统必须按HIS科室人员关系同步的字段映射配置解析CSV

系统必须查询数据库 interface_config（config_code='HIS_DOCTOR_DEPT_SYNC'）、interface_mapping_table、interface_field_mapping 表获取字段映射配置，按配置将 CSV 字段映射处理。

#### 场景:字段映射配置查询成功
- **当** 系统开始导入流程
- **那么** 系统查询 HIS_DOCTOR_DEPT_SYNC 配置获取 4 个字段映射：code_user→code_user, code_dept→code_dept, name_user→name_user, name_dept→name_dept

#### 场景:字段映射配置缺失
- **当** HIS_DOCTOR_DEPT_SYNC 配置不存在或字段映射为空
- **那么** 系统提示"未找到科室人员关系映射配置，请联系管理员"并终止导入

### 求:系统必须通过CODE_USER和CODE_DEPT关联查找ID

系统必须通过 CODE_USER 查找 HisStaff → User 获取 doctor_id，通过 CODE_DEPT 查找 Department 获取 department_id。

#### 场景:人员编码存在且关联用户
- **当** CSV 行的 CODE_USER 在 his_staff 表存在且对应人员已关联 user 表
- **那么** 系统获取 user.id 作为 doctor_id

#### 场景:人员编码不存在
- **当** CSV 行的 CODE_USER 在 his_staff 表不存在
- **那么** 系统跳过该行，记录错误"人员编码不存在"到 errors 列表，failCount +1

#### 场景:人员未关联用户
- **当** CSV 行的 CODE_USER 对应的 his_staff 记录存在但未关联 user 表
- **那么** 系统跳过该行，记录错误"人员未关联用户账号"到 errors 列表，failCount +1

#### 场景:科室编码存在
- **当** CSV 行的 CODE_DEPT 在 department 表存在
- **那么** 系统获取 department.id 作为 department_id

#### 场景:科室编码不存在
- **当** CSV 行的 CODE_DEPT 在 department 表不存在
- **那么** 系统跳过该行，记录错误"科室编码不存在"到 errors 列表，failCount +1

### 需求:系统必须按doctor_id加department_id组合判断重复数据

系统必须按 doctor_department.doctor_id + department_id 组合判断重复数据，存在则跳过，不存在则插入。

#### 场景:新增科室人员关系
- **当** doctor_id + department_id 组合在 doctor_department 表不存在
- **那么** 系统插入新记录，is_primary 设为 false，insertCount +1

#### 场景:已存在科室人员关系
- **当** doctor_id + department_id 组合在 doctor_department 表已存在
- **那么** 系统跳过该记录，skipCount +1（不更新）

### 需求:系统必须仅校验CODE_USER和CODE_DEPT必需列

系统必须仅校验 CSV 的 CODE_USER 和 CODE_DEPT 列不为空，其他字段缺失时忽略。

#### 场景:CODE_USER为空
- **当** CSV 行的 CODE_USER 字段为空或 null
- **那么** 系统跳过该行，记录错误"CODE_USER 不能为空"到 errors 列表，failCount +1

#### 场景:CODE_DEPT为空
- **当** CSV 行的 CODE_DEPT 字段为空或 null
- **那么** 系统跳过该行，记录错误"CODE_DEPT 不能为空"到 errors 列表，failCount +1

#### 场景:NAME_USER或NAME_DEPT缺失
- **当** CSV 行的 NAME_USER 或 NAME_DEPT 字段缺失
- **那么** 系统忽略该字段，继续导入

### 需求:系统必须显示导入结果摘要

系统必须在导入完成后显示导入结果摘要，包括总计、新增、跳过、失败数量，并允许查看错误详情。

#### 场景:导入成功
- **当** 导入完成且 failCount = 0
- **那么** 系统显示成功弹窗："导入成功：新增 X 条，跳过 Y 条"

#### 场景:部分失败
- **当** 导入完成且 failCount > 0
- **那么** 系统显示警告弹窗："导入完成：失败 X 条"，提供"查看详情"按钮

#### 场景:查看错误详情
- **当** 用户点击"查看详情"按钮
- **那么** 系统显示 errors 列表，每条错误包含行号和错误原因

### 需求:系统必须提供CSV模板下载功能

系统必须提供科室人员关系 CSV 模板下载功能，模板文件路径为 docs/data/科室人员信息.csv。

#### 场景:下载模板
- **当** 用户在导入对话框点击"下载模板"链接
- **那么** 系统下载 docs/data/科室人员信息.csv 文件，文件名显示为"科室人员信息模板.csv"

### 需求:系统必须显示数据预览

系统必须在用户上传 CSV 文件后显示前 10 行数据预览，让用户确认导入内容。

#### 场景:显示预览
- **当** 用户上传 CSV 文件成功
- **那么** 系统显示数据预览表格，列头包括：CODE_USER、NAME_USER、CODE_DEPT、NAME_DEPT，最多显示 10 行数据

### 需求:系统必须在导入过程中显示进度

系统必须在导入过程中显示进度条和"正在导入..."状态文本。

#### 场景:显示导入进度
- **当** 用户点击"确认导入"按钮
- **那么** 系统显示进度条（indeterminate 模式）和"正在导入..."文本，禁用取消和导入按钮