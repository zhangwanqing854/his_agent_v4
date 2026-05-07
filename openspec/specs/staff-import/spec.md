## 新增需求

### 需求:用户可以导入人员信息CSV文件

系统必须允许用户上传符合模板格式的 CSV 文件批量导入人员信息到 his_staff 表。

#### 场景:成功上传CSV文件
- **当** 用户在系统设置页面数据导入 Tab 点击"导入人员信息"按钮
- **那么** 系统显示人员信息导入对话框，支持拖拽或点击上传 CSV 文件

#### 场景:文件格式校验失败
- **当** 用户上传非 CSV 格式文件
- **那么** 系统提示"仅支持 CSV 格式文件"并清空已上传文件

#### 场景:文件大小超限
- **当** 用户上传的 CSV 文件数据超过 1000 条
- **那么** 系统提示"单次导入最多支持 1000 条数据，请分批导入"并拒绝导入

### 需求:系统必须按HIS人员信息同步的字段映射配置解析CSV

系统必须查询数据库 interface_config（config_code='HIS_STAFF_SYNC'）、interface_mapping_table、interface_field_mapping 表获取字段映射配置，按配置将 CSV 字段映射到 his_staff 表字段。

#### 场景:字段映射配置查询成功
- **当** 系统开始导入流程
- **那么** 系统查询 HIS_STAFF_SYNC 配置获取 8 个字段映射：code_psn→staff_code, name_psn→name, sd_emptitle→title_code, name_emptitle→title, sd_humantype→employment_status_code, name_humantype→employment_status, code_user→code_user, name_user→name_user

#### 场景:字段映射配置缺失
- **当** HIS_STAFF_SYNC 配置不存在或字段映射为空
- **那么** 系统提示"未找到人员信息映射配置，请联系管理员"并终止导入

### 需求:系统必须按staff_code唯一键判断重复数据

系统必须按 his_staff.staff_code（CSV 的 CODE_PSN 字段映射值）判断重复数据，更新策略为"更新"。

#### 场景:新增人员数据
- **当** CSV 行的 CODE_PSN 对应的 staff_code 在 his_staff 表不存在
- **那么** 系统插入新记录到 his_staff 表，insertCount +1

#### 场景:更新已有人员数据
- **当** CSV 行的 CODE_PSN 对应的 staff_code 在 his_staff 表已存在
- **那么** 系统更新该记录的所有映射字段（除 staff_code 外），updateCount +1

#### 场景:staff_code为空
- **当** CSV 行的 CODE_PSN 字段为空或 null
- **那么** 系统跳过该行，记录错误"人员编码不能为空"到 errors 列表，failCount +1

### 需求:系统必须仅校验CODE_USER必需列

系统必须仅校验 CSV 的 CODE_USER 列不为空，其他字段缺失时设为 null 或使用默认值。

#### 场景:CODE_USER为空
- **当** CSV 行的 CODE_USER 字段为空或 null
- **那么** 系统跳过该行，记录错误"CODE_USER 不能为空"到 errors 列表，failCount +1

#### 场景:其他字段缺失
- **当** CSV 行的 NAME_PSN、SD_EMPTITLE 等字段缺失
- **那么** 系统将该字段映射的 his_staff 字段设为 null，继续导入

### 需求:系统必须同步更新user表

系统必须在导入 his_staff 数据后，同步创建、启用或禁用关联的 user 表记录。

#### 场景:创建新用户账号
- **当** his_staff 新记录插入且 staff 未关联 user 表记录
- **那么** 系统创建 user 记录，username 设为 name_user（或 staff_code），usercode 设为 code_user，role 设为 DOCTOR，password 设为 "123456"，his_staff_id 关联新记录

#### 场景:在职人员启用用户账号
- **当** his_staff 的 employment_status_code = "0"（在职）
- **那么** 系统将关联 user 记录的 enabled 设为 true

#### 场景:离职人员禁用用户账号
- **当** his_staff 的 employment_status = "离职" 或 employment_status_code != "0"
- **那么** 系统将关联 user 记录的 enabled 设为 false

### 需求:系统必须显示导入结果摘要

系统必须在导入完成后显示导入结果摘要，包括总计、新增、更新、跳过、失败数量，并允许查看错误详情。

#### 场景:导入成功
- **当** 导入完成且 failCount = 0
- **那么** 系统显示成功弹窗："导入成功：新增 X 条，更新 Y 条"

#### 场景:部分失败
- **当** 导入完成且 failCount > 0
- **那么** 系统显示警告弹窗："导入完成：失败 X 条"，提供"查看详情"按钮

#### 场景:查看错误详情
- **当** 用户点击"查看详情"按钮
- **那么** 系统显示 errors 列表，每条错误包含行号和错误原因

### 需求:系统必须提供CSV模板下载功能

系统必须提供人员信息 CSV 模板下载功能，模板文件路径为 docs/data/人员信息.csv。

#### 场景:下载模板
- **当** 用户在导入对话框点击"下载模板"链接
- **那么** 系统下载 docs/data/人员信息.csv 文件，文件名显示为"人员信息模板.csv"

### 需求:系统必须显示数据预览

系统必须在用户上传 CSV 文件后显示前 10 行数据预览，让用户确认导入内容。

#### 场景:显示预览
- **当** 用户上传 CSV 文件成功
- **那么** 系统显示数据预览表格，列头包括：CODE_PSN、NAME_PSN、CODE_USER、NAME_USER 等，最多显示 10 行数据

### 需求:系统必须在导入过程中显示进度

系统必须在导入过程中显示进度条和"正在导入..."状态文本。

#### 场景:显示导入进度
- **当** 用户点击"确认导入"按钮
- **那么** 系统显示进度条（indeterminate 模式）和"正在导入..."文本，禁用取消和导入按钮