const { Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
        Header, Footer, AlignmentType, HeadingLevel, BorderStyle, WidthType,
        ShadingType, PageNumber, PageBreak, LevelFormat } = require('docx');
const fs = require('fs');

// 表格样式
const border = { style: BorderStyle.SINGLE, size: 1, color: 'CCCCCC' };
const borders = { top: border, bottom: border, left: border, right: border };
const headerShading = { fill: 'FFB366', type: ShadingType.CLEAR };
const headerTextStyle = { bold: true, size: 22, font: 'Arial' };

// 创建表格行
function createTableHeader(columns) {
  const colWidth = 9360 / columns.length;
  return new TableRow({
    children: columns.map(col => new TableCell({
      borders,
      width: { size: colWidth, type: WidthType.DXA },
      shading: headerShading,
      margins: { top: 80, bottom: 80, left: 120, right: 120 },
      children: [new Paragraph({
        alignment: AlignmentType.CENTER,
        children: [new TextRun({ text: col, ...headerTextStyle })]
      })]
    }))
  });
}

function createTableRow(cells) {
  const colWidth = 9360 / cells.length;
  return new TableRow({
    children: cells.map(cell => new TableCell({
      borders,
      width: { size: colWidth, type: WidthType.DXA },
      margins: { top: 80, bottom: 80, left: 120, right: 120 },
      children: [new Paragraph({
        children: [new TextRun({ text: cell || '-', size: 20, font: 'Arial' })]
      })]
    }))
  });
}

// 表定义数据
const tables = [
  {
    name: 'user',
    description: '用户账号表',
    fields: [
      ['id', 'BIGINT', '是', '自增', '主键'],
      ['username', 'VARCHAR(50)', '是', '-', '用户名，唯一'],
      ['password', 'VARCHAR(255)', '是', '-', '密码（BCrypt加密）'],
      ['is_super_admin', 'TINYINT(1)', '是', '0', '是否超级管理员'],
      ['his_staff_id', 'BIGINT', '否', 'NULL', '关联HIS医生ID'],
      ['role_id', 'BIGINT', '是', '-', '角色ID'],
      ['enabled', 'TINYINT(1)', '是', '1', '是否启用'],
      ['created_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '创建时间'],
      ['updated_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '更新时间']
    ],
    indexes: ['PRIMARY KEY (id)', 'UNIQUE KEY uk_username (username)', 'KEY idx_his_staff_id (his_staff_id)', 'KEY idx_role_id (role_id)']
  },
  {
    name: 'role',
    description: '角色表',
    fields: [
      ['id', 'BIGINT', '是', '自增', '主键'],
      ['name', 'VARCHAR(50)', '是', '-', '角色名称'],
      ['code', 'VARCHAR(50)', '是', '-', '角色编码，唯一'],
      ['is_default', 'TINYINT(1)', '是', '0', '是否默认角色'],
      ['description', 'VARCHAR(255)', '否', 'NULL', '角色描述'],
      ['created_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '创建时间'],
      ['updated_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '更新时间']
    ],
    indexes: ['PRIMARY KEY (id)', 'UNIQUE KEY uk_code (code)']
  },
  {
    name: 'permission',
    description: '权限表（功能模块）',
    fields: [
      ['id', 'BIGINT', '是', '自增', '主键'],
      ['code', 'VARCHAR(50)', '是', '-', '权限编码，唯一'],
      ['name', 'VARCHAR(50)', '是', '-', '权限名称'],
      ['description', 'VARCHAR(255)', '否', 'NULL', '权限描述'],
      ['created_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '创建时间']
    ],
    indexes: ['PRIMARY KEY (id)', 'UNIQUE KEY uk_code (code)']
  },
  {
    name: 'duty',
    description: '职责表（具体功能）',
    fields: [
      ['id', 'BIGINT', '是', '自增', '主键'],
      ['code', 'VARCHAR(50)', '是', '-', '职责编码，唯一'],
      ['name', 'VARCHAR(50)', '是', '-', '职责名称'],
      ['description', 'VARCHAR(255)', '否', 'NULL', '职责描述'],
      ['permission_id', 'BIGINT', '是', '-', '所属权限ID'],
      ['created_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '创建时间']
    ],
    indexes: ['PRIMARY KEY (id)', 'UNIQUE KEY uk_code (code)', 'KEY idx_permission_id (permission_id)']
  },
  {
    name: 'role_duty',
    description: '角色-职责关联表',
    fields: [
      ['id', 'BIGINT', '是', '自增', '主键'],
      ['role_id', 'BIGINT', '是', '-', '角色ID'],
      ['duty_id', 'BIGINT', '是', '-', '职责ID'],
      ['created_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '创建时间']
    ],
    indexes: ['PRIMARY KEY (id)', 'UNIQUE KEY uk_role_duty (role_id, duty_id)', 'KEY idx_duty_id (duty_id)']
  },
  {
    name: 'his_staff',
    description: 'HIS医生信息表',
    fields: [
      ['id', 'BIGINT', '是', '自增', '主键'],
      ['staff_code', 'VARCHAR(50)', '是', '-', '工号，唯一'],
      ['name', 'VARCHAR(50)', '是', '-', '姓名'],
      ['department_id', 'BIGINT', '是', '-', '科室ID'],
      ['title', 'VARCHAR(50)', '否', 'NULL', '职称'],
      ['phone', 'VARCHAR(20)', '否', 'NULL', '电话'],
      ['sync_time', 'DATETIME', '是', '-', '最后同步时间'],
      ['created_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '创建时间'],
      ['updated_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '更新时间']
    ],
    indexes: ['PRIMARY KEY (id)', 'UNIQUE KEY uk_staff_code (staff_code)', 'KEY idx_department_id (department_id)']
  },
  {
    name: 'department',
    description: '科室表',
    fields: [
      ['id', 'BIGINT', '是', '自增', '主键'],
      ['name', 'VARCHAR(50)', '是', '-', '科室名称'],
      ['code', 'VARCHAR(50)', '是', '-', '科室编码，唯一'],
      ['bed_count', 'INT', '是', '0', '床位数量'],
      ['created_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '创建时间'],
      ['updated_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '更新时间']
    ],
    indexes: ['PRIMARY KEY (id)', 'UNIQUE KEY uk_code (code)']
  },
  {
    name: 'doctor_department',
    description: '医生-科室关联表',
    fields: [
      ['id', 'BIGINT', '是', '自增', '主键'],
      ['doctor_id', 'BIGINT', '是', '-', '医生ID（关联user.id）'],
      ['department_id', 'BIGINT', '是', '-', '科室ID'],
      ['is_primary', 'TINYINT(1)', '是', '0', '是否主科室'],
      ['created_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '创建时间']
    ],
    indexes: ['PRIMARY KEY (id)', 'KEY idx_doctor_id (doctor_id)', 'KEY idx_department_id (department_id)']
  },
  {
    name: 'patient',
    description: '患者表（HIS同步）',
    fields: [
      ['id', 'BIGINT', '是', '自增', '主键'],
      ['patient_code', 'VARCHAR(50)', '是', '-', '患者编号，唯一'],
      ['name', 'VARCHAR(50)', '是', '-', '姓名'],
      ['gender', 'VARCHAR(10)', '是', '-', '性别'],
      ['age', 'INT', '是', '-', '年龄'],
      ['department_id', 'BIGINT', '是', '-', '科室ID'],
      ['bed_number', 'VARCHAR(20)', '是', '-', '床位号'],
      ['diagnosis', 'VARCHAR(255)', '是', '-', '诊断'],
      ['admission_date', 'DATE', '是', '-', '入院日期'],
      ['is_critical', 'TINYINT(1)', '是', '0', '是否重点患者'],
      ['sync_time', 'DATETIME', '是', '-', '最后同步时间'],
      ['created_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '创建时间'],
      ['updated_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '更新时间']
    ],
    indexes: ['PRIMARY KEY (id)', 'UNIQUE KEY uk_patient_code (patient_code)', 'KEY idx_department_id (department_id)', 'KEY idx_bed_number (bed_number)']
  },
  {
    name: 'vital_record',
    description: '生命体征记录表',
    fields: [
      ['id', 'BIGINT', '是', '自增', '主键'],
      ['patient_id', 'BIGINT', '是', '-', '患者ID'],
      ['record_time', 'DATETIME', '是', '-', '记录时间'],
      ['temperature', 'DECIMAL(4,1)', '否', 'NULL', '体温（℃）'],
      ['pulse', 'INT', '否', 'NULL', '脉搏（次/分）'],
      ['respiration', 'INT', '否', 'NULL', '呼吸（次/分）'],
      ['blood_pressure', 'VARCHAR(20)', '否', 'NULL', '血压（mmHg）'],
      ['spo2', 'INT', '否', 'NULL', '血氧饱和度（%）'],
      ['vitals_text', 'TEXT', '否', 'NULL', '生命体征文本'],
      ['created_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '创建时间']
    ],
    indexes: ['PRIMARY KEY (id)', 'KEY idx_patient_id (patient_id)', 'KEY idx_record_time (record_time)']
  },
  {
    name: 'shift_handover',
    description: '科室交班主表',
    fields: [
      ['id', 'BIGINT', '是', '自增', '主键'],
      ['department_id', 'BIGINT', '是', '-', '科室ID'],
      ['department_name', 'VARCHAR(50)', '是', '-', '科室名称'],
      ['from_doctor_id', 'BIGINT', '是', '-', '交班医生ID'],
      ['to_doctor_id', 'BIGINT', '否', 'NULL', '接班医生ID'],
      ['handover_date', 'DATE', '是', '-', '交班日期'],
      ['shift', 'VARCHAR(20)', '是', '-', '班次（白班/夜班）'],
      ['status', 'VARCHAR(20)', '是', 'DRAFT', '状态'],
      ['summary_json', 'JSON', '否', 'NULL', '科室统计信息'],
      ['created_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '创建时间'],
      ['updated_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '更新时间']
    ],
    indexes: ['PRIMARY KEY (id)', 'KEY idx_department_id (department_id)', 'KEY idx_handover_date (handover_date)', 'KEY idx_status (status)']
  },
  {
    name: 'handover_patient',
    description: '交班患者明细表',
    fields: [
      ['id', 'BIGINT', '是', '自增', '主键'],
      ['handover_id', 'BIGINT', '是', '-', '交班ID'],
      ['patient_id', 'BIGINT', '是', '-', '患者ID'],
      ['bed_number', 'VARCHAR(20)', '是', '-', '床位号'],
      ['patient_name', 'VARCHAR(50)', '是', '-', '患者姓名'],
      ['age', 'INT', '是', '-', '年龄'],
      ['gender', 'VARCHAR(10)', '是', '-', '性别'],
      ['diagnosis', 'VARCHAR(255)', '是', '-', '诊断'],
      ['is_critical', 'TINYINT(1)', '是', '0', '是否重点患者'],
      ['vitals', 'TEXT', '否', 'NULL', '生命体征（可编辑）'],
      ['current_condition', 'TEXT', '否', 'NULL', '目前情况（可编辑）'],
      ['observation_items', 'TEXT', '否', 'NULL', '需观察项（可编辑）'],
      ['mews_score', 'INT', '否', 'NULL', 'MEWS评分'],
      ['braden_score', 'INT', '否', 'NULL', 'Braden评分'],
      ['fall_risk', 'INT', '否', 'NULL', '跌倒风险评分'],
      ['created_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '创建时间'],
      ['updated_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '更新时间']
    ],
    indexes: ['PRIMARY KEY (id)', 'KEY idx_handover_id (handover_id)', 'KEY idx_patient_id (patient_id)']
  },
  {
    name: 'handover_todo',
    description: '交班待办事项表',
    fields: [
      ['id', 'BIGINT', '是', '自增', '主键'],
      ['handover_id', 'BIGINT', '是', '-', '交班ID'],
      ['patient_id', 'BIGINT', '否', 'NULL', '患者ID'],
      ['patient_bed', 'VARCHAR(20)', '否', 'NULL', '患者床位号'],
      ['content', 'VARCHAR(500)', '是', '-', '待办内容'],
      ['due_time', 'DATETIME', '否', 'NULL', '截止时间'],
      ['priority', 'VARCHAR(20)', '是', 'NORMAL', '优先级'],
      ['status', 'VARCHAR(20)', '是', 'PENDING', '状态'],
      ['created_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '创建时间'],
      ['updated_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '更新时间']
    ],
    indexes: ['PRIMARY KEY (id)', 'KEY idx_handover_id (handover_id)', 'KEY idx_patient_id (patient_id)', 'KEY idx_status (status)']
  }
];

// 预置数据
const presetData = {
  roles: [
    { id: 1, name: '超级管理员', code: 'SUPER_ADMIN', is_default: false, description: '系统超级管理员，拥有所有权限' },
    { id: 2, name: '普通医生', code: 'DOCTOR', is_default: true, description: '普通医生角色，HIS同步医生的默认角色' }
  ],
  permissions: [
    { id: 1, code: 'HANDOVER', name: '交班管理', description: '交班记录管理相关功能' },
    { id: 2, code: 'PATIENT', name: '患者管理', description: '患者信息管理相关功能' },
    { id: 3, code: 'TODO', name: '待办事项', description: '待办事项管理相关功能' },
    { id: 4, code: 'STATISTICS', name: '统计分析', description: '统计数据查看相关功能' },
    { id: 5, code: 'USER_MANAGE', name: '用户管理', description: '用户账号管理相关功能' },
    { id: 6, code: 'ROLE_MANAGE', name: '角色权限', description: '角色权限管理相关功能' },
    { id: 7, code: 'SYSTEM_SETTINGS', name: '系统设置', description: '系统配置管理相关功能' }
  ],
  duties: [
    { id: 1, code: 'HANDOVER_VIEW', name: '查看交班记录', permission_id: 1 },
    { id: 2, code: 'HANDOVER_CREATE', name: '发起交班', permission_id: 1 },
    { id: 3, code: 'HANDOVER_EDIT', name: '编辑交班记录', permission_id: 1 },
    { id: 4, code: 'HANDOVER_DELETE', name: '删除交班记录', permission_id: 1 },
    { id: 5, code: 'HANDOVER_REVIEW', name: '审核交班', permission_id: 1 },
    { id: 10, code: 'PATIENT_VIEW', name: '查看患者信息', permission_id: 2 },
    { id: 11, code: 'PATIENT_EXPORT', name: '导出患者数据', permission_id: 2 },
    { id: 20, code: 'TODO_VIEW', name: '查看待办事项', permission_id: 3 },
    { id: 21, code: 'TODO_HANDLE', name: '处理待办事项', permission_id: 3 },
    { id: 30, code: 'STATISTICS_VIEW', name: '查看统计报表', permission_id: 4 },
    { id: 31, code: 'STATISTICS_EXPORT', name: '导出统计报表', permission_id: 4 },
    { id: 40, code: 'USER_VIEW', name: '查看用户列表', permission_id: 5 },
    { id: 41, code: 'USER_CREATE', name: '新增用户', permission_id: 5 },
    { id: 42, code: 'USER_EDIT', name: '编辑用户', permission_id: 5 },
    { id: 43, code: 'USER_DELETE', name: '删除用户', permission_id: 5 },
    { id: 44, code: 'USER_ENABLE', name: '启用/禁用用户', permission_id: 5 },
    { id: 45, code: 'USER_RESET_PWD', name: '重置密码', permission_id: 5 },
    { id: 50, code: 'ROLE_VIEW', name: '查看角色列表', permission_id: 6 },
    { id: 51, code: 'ROLE_CREATE', name: '新增角色', permission_id: 6 },
    { id: 52, code: 'ROLE_EDIT', name: '编辑角色', permission_id: 6 },
    { id: 53, code: 'ROLE_DELETE', name: '删除角色', permission_id: 6 },
    { id: 54, code: 'ROLE_ASSIGN_DUTY', name: '分配职责', permission_id: 6 },
    { id: 60, code: 'SETTINGS_VIEW', name: '查看系统配置', permission_id: 7 },
    { id: 61, code: 'SETTINGS_EDIT', name: '修改系统配置', permission_id: 7 },
    { id: 62, code: 'INTERFACE_CONFIG', name: '接口配置管理', permission_id: 7 },
    { id: 63, code: 'INTERFACE_SYNC', name: '数据同步', permission_id: 7 }
  ],
  role_duties: {
    SUPER_ADMIN: '所有职责（1-63）',
    DOCTOR: '交班管理（1-5）、患者管理（10-11）、待办事项（20-21）、统计分析（30-31）'
  },
  users: [
    { id: 1, username: 'admin', password: 'admin', is_super_admin: true, role_id: 1, enabled: true }
  ]
};

// 创建文档
const doc = new Document({
  styles: {
    default: {
      document: {
        run: { font: 'Arial', size: 24 }
      }
    },
    paragraphStyles: [
      {
        id: 'Heading1',
        name: 'Heading 1',
        basedOn: 'Normal',
        next: 'Normal',
        quickFormat: true,
        run: { size: 32, bold: true, font: 'Arial', color: '2E75B6' },
        paragraph: { spacing: { before: 360, after: 240 }, outlineLevel: 0 }
      },
      {
        id: 'Heading2',
        name: 'Heading 2',
        basedOn: 'Normal',
        next: 'Normal',
        quickFormat: true,
        run: { size: 28, bold: true, font: 'Arial', color: '2E75B6' },
        paragraph: { spacing: { before: 240, after: 180 }, outlineLevel: 1 }
      },
      {
        id: 'Heading3',
        name: 'Heading 3',
        basedOn: 'Normal',
        next: 'Normal',
        quickFormat: true,
        run: { size: 24, bold: true, font: 'Arial', color: '404040' },
        paragraph: { spacing: { before: 180, after: 120 }, outlineLevel: 2 }
      }
    ]
  },
  numbering: {
    config: [
      {
        reference: 'bullets',
        levels: [{
          level: 0,
          format: LevelFormat.BULLET,
          text: '•',
          alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 720, hanging: 360 } } }
        }]
      },
      {
        reference: 'numbers',
        levels: [{
          level: 0,
          format: LevelFormat.DECIMAL,
          text: '%1.',
          alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 720, hanging: 360 } } }
        }]
      }
    ]
  },
  sections: [{
    properties: {
      page: {
        size: { width: 12240, height: 15840 },
        margin: { top: 1440, right: 1440, bottom: 1440, left: 1440 }
      }
    },
    headers: {
      default: new Header({
        children: [new Paragraph({
          alignment: AlignmentType.RIGHT,
          children: [new TextRun({ text: '医护智能交接班系统 - 数据库设计文档', size: 20, color: '808080' })]
        })]
      })
    },
    footers: {
      default: new Footer({
        children: [new Paragraph({
          alignment: AlignmentType.CENTER,
          children: [
            new TextRun({ text: '北京大学国际医院 | 第 ', size: 20, color: '808080' }),
            new TextRun({ children: [PageNumber.CURRENT], size: 20, color: '808080' }),
            new TextRun({ text: ' 页', size: 20, color: '808080' })
          ]
        })]
      })
    },
    children: [
      // 标题
      new Paragraph({
        alignment: AlignmentType.CENTER,
        children: [new TextRun({ text: '医护智能交接班质量管理系统', size: 40, bold: true, color: '2E75B6' })]
      }),
      new Paragraph({
        alignment: AlignmentType.CENTER,
        spacing: { after: 120 },
        children: [new TextRun({ text: '数据库设计文档', size: 36, bold: true, color: '404040' })]
      }),
      new Paragraph({
        alignment: AlignmentType.CENTER,
        spacing: { after: 480 },
        children: [new TextRun({ text: '北京大学国际医院 | 2026年3月', size: 22, color: '808080' })]
      }),

      // 1. 概述
      new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun('1. 概述')] }),
      
      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun('1.1 项目背景')] }),
      new Paragraph({
        children: [new TextRun('本项目为北京大学国际医院医护智能交接班质量管理系统，旨在实现交接班流程的标准化、智能化管理，提升交接班质量和效率。')]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun('1.2 技术架构')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun('前端：Vue 3 + TypeScript + Vite')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun('后端：Spring Boot 3 + JDK 17 + MyBatis-Plus')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun('数据库：MySQL 8.0')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun('缓存：Redis 7.x')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun('AI服务：阿里通义千问（DashScope）')] }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun('1.3 数据库表总览')] }),
      new Paragraph({ spacing: { after: 120 }, children: [new TextRun('本系统共设计 '), new TextRun({ text: '14张数据库表', bold: true }), new TextRun('，分为三大模块：')] }),
      
      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [3120, 3120, 3120],
        rows: [
          createTableHeader(['模块', '表数量', '主要功能']),
          createTableRow(['用户权限模块', '6张', '用户、角色、权限、职责管理']),
          createTableRow(['科室组织模块', '2张', '科室、医生-科室关联']),
          createTableRow(['交班业务模块', '6张', '患者、交班、待办事项'])
        ]
      }),

      new Paragraph({ children: [new PageBreak()] }),

      // 2. ER图
      new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun('2. 数据库ER图')] }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun('2.1 用户权限模块ER图')] }),
      new Paragraph({
        children: [new TextRun({ text: '权限模型设计：Permission（权限） → Duty（职责） → Role（角色）', bold: true, color: '2E75B6' })]
      }),
      new Paragraph({ spacing: { after: 120 }, children: [new TextRun('设计原则：权限包含职责，职责对应具体功能点，角色通过职责获得权限。')] }),

      new Paragraph({
        shading: { fill: 'F5F5F5', type: ShadingType.CLEAR },
        spacing: { before: 120, after: 120 },
        children: [new TextRun({ text: 
`┌────────────┐     ┌────────────┐     ┌────────────┐     ┌────────────┐
│   user     │────▶│    role    │────▶│  role_duty │────▶│    duty    │
│ (用户表)   │     │  (角色表)  │     │ (关联表)   │     │  (职责表)  │
└────────────┘     └────────────┘     └────────────┘     └────────────┘
                                                              │
                                                              ▼
                                                        ┌────────────┐
                                                        │ permission │
                                                        │  (权限表)  │
                                                        └────────────┘

┌────────────┐
│ his_staff  │◀──── user.his_staff_id（可选关联HIS医生信息）
│(HIS医生表) │
└────────────┘`, size: 18, font: 'Courier New' })]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun('2.2 科室组织模块ER图')] }),
      new Paragraph({
        shading: { fill: 'F5F5F5', type: ShadingType.CLEAR },
        spacing: { before: 120, after: 120 },
        children: [new TextRun({ text: 
`┌────────────┐     ┌─────────────────┐     ┌────────────┐
│ department │◀────│doctor_department│────▶│   user     │
│  (科室表)  │     │ (医生-科室关联) │     │  (用户表)  │
└────────────┘     └─────────────────┘     └────────────┘

说明：医生可在多个科室工作，通过 doctor_department 实现多对多关系。
      is_primary 字段标识医生的主科室（登录后默认进入）。`, size: 18, font: 'Courier New' })]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun('2.3 交班业务模块ER图')] }),
      new Paragraph({
        children: [new TextRun({ text: '核心设计：一次交班 = 整个科室所有患者', bold: true, color: '2E75B6' })]
      }),
      new Paragraph({ spacing: { after: 120 }, children: [new TextRun('以科室为维度进行交班，减少医生操作次数，一次确认完成整个科室交接。')] }),

      new Paragraph({
        shading: { fill: 'F5F5F5', type: ShadingType.CLEAR },
        spacing: { before: 120, after: 120 },
        children: [new TextRun({ text: 
`┌─────────────────┐     ┌──────────────────────┐     ┌────────────┐
│ shift_handover  │────▶│  handover_patient    │────▶│  patient   │
│ (科室交班主表)  │     │ (交班患者明细表)     │     │ (患者表)  │
│                 │     │                      │     │            │
│ department_id   │     │ handover_id          │     │ department │
│ from_doctor_id  │     │ patient_id           │     │ bed_number │
│ to_doctor_id    │     │ vitals（可编辑）     │     │ diagnosis  │
│ status          │     │ current_condition    │     │ ...        │
└─────────────────┘     │ observation_items    │     └────────────┘
        │                └──────────────────────┘            │
        │                         │                         │
        ▼                         ▼                         │
┌─────────────────┐     ┌──────────────────────┐            │
│  handover_todo  │◀────│   patient_id         │────────────┘
│ (待办事项表)    │     └──────────────────────┘
│                 │
│ handover_id     │
│ patient_id      │
│ content         │
│ due_time        │
│ status          │
└─────────────────┘

┌─────────────────┐
│  vital_record   │◀──── patient.id（生命体征历史记录）
│ (生命体征表)    │
└─────────────────┘`, size: 18, font: 'Courier New' })]
      }),

      new Paragraph({ children: [new PageBreak()] }),

      // 3. 表结构定义
      new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun('3. 数据库表结构定义')] }),
      
      // 遍历所有表
      ...tables.flatMap(table => [
        new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun(`3.${tables.indexOf(table) + 1} ${table.name}（${table.description}）`)] }),
        
        new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun('字段定义')] }),
        new Table({
          width: { size: 9360, type: WidthType.DXA },
          columnWidths: [1800, 2200, 1000, 1800, 2560],
          rows: [
            createTableHeader(['字段名', '类型', '必填', '默认值', '说明']),
            ...table.fields.map(f => createTableRow(f))
          ]
        }),

        new Paragraph({ heading: HeadingLevel.HEADING_3, spacing: { before: 180 }, children: [new TextRun('索引')] }),
        ...table.indexes.map(idx => new Paragraph({
          numbering: { reference: 'bullets', level: 0 },
          children: [new TextRun({ text: idx, size: 20 })]
        })),
        
        new Paragraph({ spacing: { after: 240 }, children: [] })
      ]),

      new Paragraph({ children: [new PageBreak()] }),

      // 4. 表关系说明
      new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun('4. 表关系说明')] }),

      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [2400, 2400, 2400, 2160],
        rows: [
          createTableHeader(['源表', '目标表', '关联字段', '关系类型']),
          createTableRow(['user', 'role', 'role_id → id', 'N:1']),
          createTableRow(['user', 'his_staff', 'his_staff_id → id', 'N:1（可选）']),
          createTableRow(['role', 'role_duty', 'id → role_id', '1:N']),
          createTableRow(['role_duty', 'duty', 'duty_id → id', 'N:1']),
          createTableRow(['duty', 'permission', 'permission_id → id', 'N:1']),
          createTableRow(['his_staff', 'department', 'department_id → id', 'N:1']),
          createTableRow(['patient', 'department', 'department_id → id', 'N:1']),
          createTableRow(['vital_record', 'patient', 'patient_id → id', 'N:1']),
          createTableRow(['shift_handover', 'department', 'department_id → id', 'N:1']),
          createTableRow(['shift_handover', 'user', 'from_doctor_id → id', 'N:1']),
          createTableRow(['shift_handover', 'user', 'to_doctor_id → id', 'N:1（可选）']),
          createTableRow(['handover_patient', 'shift_handover', 'handover_id → id', 'N:1']),
          createTableRow(['handover_patient', 'patient', 'patient_id → id', 'N:1']),
          createTableRow(['handover_todo', 'shift_handover', 'handover_id → id', 'N:1']),
          createTableRow(['handover_todo', 'patient', 'patient_id → id', 'N:1（可选）']),
          createTableRow(['doctor_department', 'user', 'doctor_id → id', 'N:1']),
          createTableRow(['doctor_department', 'department', 'department_id → id', 'N:1'])
        ]
      }),

      new Paragraph({ children: [new PageBreak()] }),

      // 5. 预置数据
      new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun('5. 预置数据')] }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun('5.1 角色（role）')] }),
      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [720, 1800, 1800, 1200, 4680],
        rows: [
          createTableHeader(['id', 'name', 'code', 'is_default', 'description']),
          ...presetData.roles.map(r => createTableRow([String(r.id), r.name, r.code, String(r.is_default), r.description]))
        ]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, spacing: { before: 240 }, children: [new TextRun('5.2 权限（permission）')] }),
      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [720, 1800, 1800, 5040],
        rows: [
          createTableHeader(['id', 'code', 'name', 'description']),
          ...presetData.permissions.map(p => createTableRow([String(p.id), p.code, p.name, p.description]))
        ]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, spacing: { before: 240 }, children: [new TextRun('5.3 职责（duty）')] }),
      new Paragraph({ children: [new TextRun('职责共32条，每条职责归属于一个权限模块：')] }),
      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [720, 2160, 2520, 1440, 2520],
        rows: [
          createTableHeader(['id', 'code', 'name', 'permission_id', '权限模块']),
          ...presetData.duties.map(d => {
            const perm = presetData.permissions.find(p => p.id === d.permission_id);
            return createTableRow([String(d.id), d.code, d.name, String(d.permission_id), perm ? perm.name : '-']);
          })
        ]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, spacing: { before: 240 }, children: [new TextRun('5.4 角色-职责分配（role_duty）')] }),
      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [2400, 6960],
        rows: [
          createTableHeader(['角色', '职责范围']),
          createTableRow(['超级管理员（SUPER_ADMIN）', presetData.role_duties.SUPER_ADMIN]),
          createTableRow(['普通医生（DOCTOR）', presetData.role_duties.DOCTOR])
        ]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, spacing: { before: 240 }, children: [new TextRun('5.5 初始用户（user）')] }),
      new Paragraph({ children: [new TextRun('系统预置一个超级管理员账号：')] }),
      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [720, 1800, 1800, 1440, 1080, 1080, 1440],
        rows: [
          createTableHeader(['id', 'username', 'password', 'is_super_admin', 'role_id', 'enabled', '说明']),
          createTableRow(['1', 'admin', 'admin', '1', '1', '1', '系统超级管理员'])
        ]
      }),

      new Paragraph({ children: [new PageBreak()] }),

      // 6. 状态枚举
      new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun('6. 状态枚举定义')] }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun('6.1 交班状态（shift_handover.status）')] }),
      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [2160, 3120, 4080],
        rows: [
          createTableHeader(['状态值', '状态名称', '说明']),
          createTableRow(['DRAFT', '草稿', 'AI自动生成草稿，医生可编辑']),
          createTableRow(['PENDING', '待交班', '医生确认，选择接班人']),
          createTableRow(['TRANSFERRING', '交班中', '已发送通知，等待接班确认']),
          createTableRow(['COMPLETED', '已完成', '接班确认完成，已归档'])
        ]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, spacing: { before: 240 }, children: [new TextRun('6.2 待办状态（handover_todo.status）')] }),
      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [2160, 3120, 4080],
        rows: [
          createTableHeader(['状态值', '状态名称', '说明']),
          createTableRow(['PENDING', '待处理', '待办事项待执行']),
          createTableRow(['IN_PROGRESS', '进行中', '正在执行']),
          createTableRow(['COMPLETED', '已完成', '执行完成']),
          createTableRow(['CANCELLED', '已取消', '取消执行'])
        ]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, spacing: { before: 240 }, children: [new TextRun('6.3 待办优先级（handover_todo.priority）')] }),
      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [2160, 3120, 4080],
        rows: [
          createTableHeader(['优先级值', '名称', '说明']),
          createTableRow(['HIGH', '高', '紧急事项，需立即处理']),
          createTableRow(['NORMAL', '普通', '常规事项']),
          createTableRow(['LOW', '低', '可延后处理'])
        ]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, spacing: { before: 240 }, children: [new TextRun('6.4 班次（shift_handover.shift）')] }),
      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [2160, 3120, 4080],
        rows: [
          createTableHeader(['班次值', '名称', '说明']),
          createTableRow(['MORNING', '白班', '8:00-16:00']),
          createTableRow(['EVENING', '夜班', '16:00-次日8:00'])
        ]
      }),

      new Paragraph({ children: [new PageBreak()] }),

      // 7. 扩展性设计
      new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun('7. 扩展性设计')] }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun('7.1 预留扩展点')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '更多角色：', bold: true }), new TextRun('可扩展科室主任、护士长、质控员等角色')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '细粒度权限：', bold: true }), new TextRun('职责可进一步细分（如查看/新增/编辑/删除）')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '数据权限：', bold: true }), new TextRun('可扩展为科室级数据权限控制')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '审批流程：', bold: true }), new TextRun('可扩展交班审批、用户创建审批流程')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: 'HIS对接：', bold: true }), new TextRun('Phase 2 可扩展HIS/LIS/PACS对接层')] }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun('7.2 配置化项')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '密码强度：', bold: true }), new TextRun('可配置密码复杂度要求')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: '登录锁定：', bold: true }), new TextRun('可配置登录失败次数限制')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: 'Token过期：', bold: true }), new TextRun('可配置JWT Token有效期')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun({ text: 'AI参数：', bold: true }), new TextRun('可配置AI模型、温度参数等')] }),

      // 结尾
      new Paragraph({ spacing: { before: 480 }, alignment: AlignmentType.CENTER, children: [new TextRun({ text: '— 文档结束 —', size: 22, color: '808080' })] })
    ]
  }]
});

// 生成文档
Packer.toBuffer(doc).then(buffer => {
  fs.writeFileSync('/Users/admin/workspace/his_agent_v4/docs/数据库设计文档.docx', buffer);
  console.log('文档已生成: /Users/admin/workspace/his_agent_v4/docs/数据库设计文档.docx');
});