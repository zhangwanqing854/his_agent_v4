const { Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
        Header, Footer, AlignmentType, HeadingLevel, BorderStyle, WidthType,
        ShadingType, PageNumber, PageBreak, LevelFormat } = require('docx');
const fs = require('fs');

const border = { style: BorderStyle.SINGLE, size: 1, color: 'CCCCCC' };
const borders = { top: border, bottom: border, left: border, right: border };
const headerShading = { fill: 'FFB366', type: ShadingType.CLEAR };
const headerTextStyle = { bold: true, size: 22, font: 'Arial' };

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

const tables = [
  {
    name: 'patient',
    description: '患者基本信息表',
    group: 'HIS同步表组',
    fields: [
      ['id', 'BIGINT', '是', '自增', '主键'],
      ['patient_no', 'VARCHAR(50)', '是', '-', '患者编号（住院号），唯一'],
      ['name', 'VARCHAR(50)', '是', '-', '姓名'],
      ['gender', 'VARCHAR(10)', '是', '-', '性别（男/女）'],
      ['birth_date', 'DATE', '否', 'NULL', '出生日期'],
      ['age', 'INT', '否', 'NULL', '年龄（冗余字段）'],
      ['id_card', 'VARCHAR(20)', '否', 'NULL', '身份证号'],
      ['phone', 'VARCHAR(20)', '否', 'NULL', '联系电话'],
      ['address', 'VARCHAR(200)', '否', 'NULL', '家庭住址'],
      ['emergency_contact', 'VARCHAR(50)', '否', 'NULL', '紧急联系人'],
      ['emergency_phone', 'VARCHAR(20)', '否', 'NULL', '紧急联系电话'],
      ['sync_time', 'DATETIME', '是', '-', '最后同步时间'],
      ['created_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '创建时间'],
      ['updated_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '更新时间']
    ],
    indexes: ['PRIMARY KEY (id)', 'UNIQUE KEY uk_patient_no (patient_no)', 'KEY idx_id_card (id_card)']
  },
  {
    name: 'visit',
    description: '就诊/住院信息表',
    group: 'HIS同步表组',
    fields: [
      ['id', 'BIGINT', '是', '自增', '主键'],
      ['visit_no', 'VARCHAR(50)', '是', '-', '就诊号/住院号，唯一'],
      ['patient_id', 'BIGINT', '是', '-', '患者ID'],
      ['dept_id', 'BIGINT', '是', '-', '科室ID'],
      ['dept_name', 'VARCHAR(50)', '是', '-', '科室名称（冗余）'],
      ['bed_no', 'VARCHAR(20)', '否', 'NULL', '床位号'],
      ['admission_datetime', 'DATETIME', '是', '-', '入院时间'],
      ['discharge_datetime', 'DATETIME', '否', 'NULL', '出院时间'],
      ['doctor_id', 'BIGINT', '否', 'NULL', '主治医生ID'],
      ['doctor_name', 'VARCHAR(50)', '否', 'NULL', '主治医生姓名'],
      ['nurse_level', 'VARCHAR(20)', '是', '-', '护理级别（特级/一级/二级/三级）'],
      ['patient_status', 'VARCHAR(20)', '是', '-', '患者状态（在院/出院/转科/死亡）'],
      ['is_critical', 'TINYINT(1)', '是', '0', '是否危重患者'],
      ['sync_time', 'DATETIME', '是', '-', '最后同步时间'],
      ['created_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '创建时间'],
      ['updated_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '更新时间']
    ],
    indexes: ['PRIMARY KEY (id)', 'UNIQUE KEY uk_visit_no (visit_no)', 'KEY idx_patient_id (patient_id)', 'KEY idx_dept_id (dept_id)', 'KEY idx_nurse_level (nurse_level)']
  },
  {
    name: 'transfer_record',
    description: '转科记录表',
    group: 'HIS同步表组',
    fields: [
      ['id', 'BIGINT', '是', '自增', '主键'],
      ['visit_id', 'BIGINT', '是', '-', '就诊ID'],
      ['patient_id', 'BIGINT', '是', '-', '患者ID'],
      ['from_dept_id', 'BIGINT', '否', 'NULL', '转出科室ID'],
      ['from_dept_name', 'VARCHAR(50)', '否', 'NULL', '转出科室名称'],
      ['to_dept_id', 'BIGINT', '是', '-', '转入科室ID'],
      ['to_dept_name', 'VARCHAR(50)', '是', '-', '转入科室名称'],
      ['transfer_time', 'DATETIME', '是', '-', '转科时间'],
      ['transfer_type', 'VARCHAR(20)', '是', '-', '转科类型（入科/转出/转入）'],
      ['reason', 'VARCHAR(500)', '否', 'NULL', '转科原因'],
      ['sync_time', 'DATETIME', '是', '-', '最后同步时间'],
      ['created_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '创建时间']
    ],
    indexes: ['PRIMARY KEY (id)', 'KEY idx_visit_id (visit_id)', 'KEY idx_patient_id (patient_id)', 'KEY idx_to_dept_id (to_dept_id)', 'KEY idx_transfer_time (transfer_time)']
  },
  {
    name: 'diagnosis_main',
    description: '诊断主表',
    group: 'HIS同步表组',
    fields: [
      ['id', 'BIGINT', '是', '自增', '主键'],
      ['visit_id', 'BIGINT', '是', '-', '就诊ID'],
      ['patient_id', 'BIGINT', '是', '-', '患者ID'],
      ['diagnosis_type', 'VARCHAR(50)', '是', '-', '诊断类型（入院诊断/出院诊断等）'],
      ['diagnosis_time', 'DATETIME', '是', '-', '诊断时间'],
      ['doctor_id', 'BIGINT', '否', 'NULL', '诊断医生ID'],
      ['status', 'VARCHAR(20)', '是', '-', '状态（有效/作废）'],
      ['sync_time', 'DATETIME', '是', '-', '最后同步时间'],
      ['created_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '创建时间']
    ],
    indexes: ['PRIMARY KEY (id)', 'KEY idx_visit_id (visit_id)', 'KEY idx_patient_id (patient_id)']
  },
  {
    name: 'diagnosis_item',
    description: '诊断子表',
    group: 'HIS同步表组',
    fields: [
      ['id', 'BIGINT', '是', '自增', '主键'],
      ['main_id', 'BIGINT', '是', '-', '诊断主表ID'],
      ['diagnosis_code', 'VARCHAR(50)', '是', '-', '诊断编码（ICD-10）'],
      ['diagnosis_name', 'VARCHAR(200)', '是', '-', '诊断名称'],
      ['is_main', 'TINYINT(1)', '是', '0', '是否主诊断'],
      ['sort_order', 'INT', '是', '0', '排序号'],
      ['created_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '创建时间']
    ],
    indexes: ['PRIMARY KEY (id)', 'KEY idx_main_id (main_id)', 'KEY idx_is_main (is_main)']
  },
  {
    name: 'order_main',
    description: '医嘱主表',
    group: 'HIS同步表组',
    fields: [
      ['id', 'BIGINT', '是', '自增', '主键'],
      ['visit_id', 'BIGINT', '是', '-', '就诊ID'],
      ['patient_id', 'BIGINT', '是', '-', '患者ID'],
      ['order_no', 'VARCHAR(50)', '是', '-', '医嘱号'],
      ['order_type', 'VARCHAR(20)', '是', '-', '医嘱类型（长期医嘱/临时医嘱）'],
      ['order_category', 'VARCHAR(50)', '是', '-', '医嘱分类（药品/检查/检验等）'],
      ['doctor_id', 'BIGINT', '是', '-', '开嘱医生ID'],
      ['doctor_name', 'VARCHAR(50)', '是', '-', '开嘱医生姓名'],
      ['start_time', 'DATETIME', '是', '-', '医嘱开始时间'],
      ['end_time', 'DATETIME', '否', 'NULL', '医嘱结束时间'],
      ['status', 'VARCHAR(20)', '是', '-', '状态（有效/停止/作废）'],
      ['sync_time', 'DATETIME', '是', '-', '最后同步时间'],
      ['created_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '创建时间']
    ],
    indexes: ['PRIMARY KEY (id)', 'UNIQUE KEY uk_order_no (order_no)', 'KEY idx_visit_id (visit_id)', 'KEY idx_order_type (order_type)', 'KEY idx_start_time (start_time)']
  },
  {
    name: 'order_item',
    description: '医嘱子表',
    group: 'HIS同步表组',
    fields: [
      ['id', 'BIGINT', '是', '自增', '主键'],
      ['main_id', 'BIGINT', '是', '-', '医嘱主表ID'],
      ['item_code', 'VARCHAR(50)', '是', '-', '项目编码'],
      ['item_name', 'VARCHAR(200)', '是', '-', '项目名称'],
      ['specification', 'VARCHAR(100)', '否', 'NULL', '规格'],
      ['dosage', 'VARCHAR(50)', '否', 'NULL', '剂量'],
      ['dosage_unit', 'VARCHAR(20)', '否', 'NULL', '剂量单位'],
      ['frequency', 'VARCHAR(50)', '否', 'NULL', '频次（QD/BID/TID等）'],
      ['route', 'VARCHAR(50)', '否', 'NULL', '给药途径'],
      ['order_time', 'DATETIME', '是', '-', '医嘱时间'],
      ['execute_time', 'DATETIME', '否', 'NULL', '执行时间'],
      ['is_temporary', 'TINYINT(1)', '是', '0', '是否临时医嘱'],
      ['created_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '创建时间']
    ],
    indexes: ['PRIMARY KEY (id)', 'KEY idx_main_id (main_id)', 'KEY idx_order_time (order_time)']
  },
  {
    name: 'shift_handover',
    description: '交班主表',
    group: '交班业务表组',
    fields: [
      ['id', 'BIGINT', '是', '自增', '主键'],
      ['dept_id', 'BIGINT', '是', '-', '科室ID'],
      ['dept_name', 'VARCHAR(50)', '是', '-', '科室名称'],
      ['handover_date', 'DATE', '是', '-', '交班日期'],
      ['shift', 'VARCHAR(20)', '是', '-', '班次（白班/夜班）'],
      ['from_doctor_id', 'BIGINT', '是', '-', '交班医生ID'],
      ['from_doctor_name', 'VARCHAR(50)', '是', '-', '交班医生姓名'],
      ['to_doctor_id', 'BIGINT', '否', 'NULL', '接班医生ID'],
      ['to_doctor_name', 'VARCHAR(50)', '否', 'NULL', '接班医生姓名'],
      ['status', 'VARCHAR(20)', '是', 'DRAFT', '状态'],
      ['summary_json', 'JSON', '否', 'NULL', '科室统计信息'],
      ['created_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '创建时间'],
      ['updated_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '更新时间']
    ],
    indexes: ['PRIMARY KEY (id)', 'KEY idx_dept_id (dept_id)', 'KEY idx_handover_date (handover_date)', 'KEY idx_status (status)']
  },
  {
    name: 'handover_patient',
    description: '交班患者明细表',
    group: '交班业务表组',
    fields: [
      ['id', 'BIGINT', '是', '自增', '主键'],
      ['handover_id', 'BIGINT', '是', '-', '交班ID'],
      ['visit_id', 'BIGINT', '是', '-', '就诊ID'],
      ['patient_id', 'BIGINT', '是', '-', '患者ID'],
      ['filter_reason', 'VARCHAR(50)', '是', '-', '筛选原因'],
      ['bed_no', 'VARCHAR(20)', '是', '-', '床位号'],
      ['patient_name', 'VARCHAR(50)', '是', '-', '患者姓名'],
      ['gender', 'VARCHAR(10)', '是', '-', '性别'],
      ['age', 'INT', '是', '-', '年龄'],
      ['diagnosis', 'VARCHAR(500)', '否', 'NULL', '主诊断'],
      ['vitals', 'TEXT', '否', 'NULL', '生命体征（可编辑）'],
      ['current_condition', 'TEXT', '否', 'NULL', '目前情况（含前12h临时医嘱）'],
      ['observation_items', 'TEXT', '否', 'NULL', '需观察项（可编辑）'],
      ['mews_score', 'INT', '否', 'NULL', 'MEWS评分'],
      ['braden_score', 'INT', '否', 'NULL', 'Braden评分'],
      ['fall_risk', 'INT', '否', 'NULL', '跌倒风险评分'],
      ['created_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '创建时间'],
      ['updated_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '更新时间']
    ],
    indexes: ['PRIMARY KEY (id)', 'KEY idx_handover_id (handover_id)', 'KEY idx_visit_id (visit_id)', 'KEY idx_filter_reason (filter_reason)']
  },
  {
    name: 'user',
    description: '用户账号表',
    group: '用户权限表组',
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
    indexes: ['PRIMARY KEY (id)', 'UNIQUE KEY uk_username (username)', 'KEY idx_role_id (role_id)']
  },
  {
    name: 'role',
    description: '角色表',
    group: '用户权限表组',
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
    description: '权限表',
    group: '用户权限表组',
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
    description: '职责表',
    group: '用户权限表组',
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
    group: '用户权限表组',
    fields: [
      ['id', 'BIGINT', '是', '自增', '主键'],
      ['role_id', 'BIGINT', '是', '-', '角色ID'],
      ['duty_id', 'BIGINT', '是', '-', '职责ID'],
      ['created_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '创建时间']
    ],
    indexes: ['PRIMARY KEY (id)', 'UNIQUE KEY uk_role_duty (role_id, duty_id)']
  },
  {
    name: 'his_staff',
    description: 'HIS医生信息表',
    group: '用户权限表组',
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
    group: '科室组织表组',
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
    group: '科室组织表组',
    fields: [
      ['id', 'BIGINT', '是', '自增', '主键'],
      ['doctor_id', 'BIGINT', '是', '-', '医生ID'],
      ['department_id', 'BIGINT', '是', '-', '科室ID'],
      ['is_primary', 'TINYINT(1)', '是', '0', '是否主科室'],
      ['created_at', 'DATETIME', '是', 'CURRENT_TIMESTAMP', '创建时间']
    ],
    indexes: ['PRIMARY KEY (id)', 'KEY idx_doctor_id (doctor_id)', 'KEY idx_department_id (department_id)']
  }
];

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
          children: [new TextRun({ text: '医护智能交接班系统 - 数据库设计文档 V2', size: 20, color: '808080' })]
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
      new Paragraph({
        alignment: AlignmentType.CENTER,
        children: [new TextRun({ text: '医护智能交接班质量管理系统', size: 40, bold: true, color: '2E75B6' })]
      }),
      new Paragraph({
        alignment: AlignmentType.CENTER,
        spacing: { after: 120 },
        children: [new TextRun({ text: '数据库设计文档 V2', size: 36, bold: true, color: '404040' })]
      }),
      new Paragraph({
        alignment: AlignmentType.CENTER,
        spacing: { after: 480 },
        children: [new TextRun({ text: '北京大学国际医院 | 2026年4月', size: 22, color: '808080' })]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun('1. 设计原则')] }),
      
      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun('1.1 核心原则')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun('HIS数据同步：患者、就诊、转科、诊断、医嘱等数据从HIS系统同步')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun('交班业务独立：交班报告基于同步数据生成，交班筛选有明确业务规则')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun('患者与就诊分离：患者只存身份信息，就诊信息独立成表')] }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun('1.2 交班筛选规则')] }),
      new Paragraph({ spacing: { after: 120 }, children: [new TextRun({ text: '重要：', bold: true, color: 'F56C6C' }), new TextRun('只有满足以下条件之一的患者才需要交班，不是所有患者都交班。')] }),
      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [3120, 6240],
        rows: [
          createTableHeader(['筛选条件', '说明']),
          createTableRow(['昨日新入科患者', '通过转科记录判断，transfer_time在昨日且转入本科室']),
          createTableRow(['I级护理患者', '就诊表中 nurse_level = "一级"'])
        ]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, spacing: { before: 240 }, children: [new TextRun('1.3 交班报告内容')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun('患者基本信息（床号、姓名、性别、年龄）')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun('诊断信息（主诊断）')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun('前12小时临时医嘱')] }),

      new Paragraph({ children: [new PageBreak()] }),

      new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun('2. 数据库表总览')] }),
      new Paragraph({ spacing: { after: 120 }, children: [new TextRun('本系统共设计 '), new TextRun({ text: '17张数据库表', bold: true }), new TextRun('，分为四大模块：')] }),
      
      new Table({
        width: { size: 9360, type: WidthType.DXA },
        columnWidths: [3120, 1560, 4680],
        rows: [
          createTableHeader(['表组', '表数量', '主要功能']),
          createTableRow(['HIS同步表组', '7张', 'patient, visit, transfer_record, diagnosis_main, diagnosis_item, order_main, order_item']),
          createTableRow(['交班业务表组', '2张', 'shift_handover, handover_patient']),
          createTableRow(['用户权限表组', '6张', 'user, role, permission, duty, role_duty, his_staff']),
          createTableRow(['科室组织表组', '2张', 'department, doctor_department'])
        ]
      }),

      new Paragraph({ children: [new PageBreak()] }),

      new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun('3. ER图')] }),
      
      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun('3.1 HIS同步数据ER图')] }),
      new Paragraph({
        shading: { fill: 'F5F5F5', type: ShadingType.CLEAR },
        spacing: { before: 120, after: 120 },
        children: [new TextRun({ text: 
`┌──────────────┐      ┌──────────────┐      ┌──────────────────┐
│   patient    │─────▶│    visit     │─────▶│  transfer_record │
│  (患者表)    │      │  (就诊表)    │      │   (转科记录表)   │
└──────────────┘      └──────┬───────┘      └──────────────────┘
                             │
              ┌──────────────┼──────────────┐
              ▼              ▼              ▼
     ┌────────────────┐ ┌──────────────┐ ┌──────────────┐
     │ diagnosis_main │ │ order_main   │ │ his_staff    │
     │  (诊断主表)    │ │  (医嘱主表)  │ │ (医护信息)   │
     └───────┬────────┘ └──────┬───────┘ └──────────────┘
             │                 │
             ▼                 ▼
     ┌────────────────┐ ┌──────────────┐
     │ diagnosis_item │ │ order_item   │
     │  (诊断子表)    │ │  (医嘱子表)  │
     └────────────────┘ └──────────────┘

关键筛选字段：
- visit.nurse_level = '一级'     → I级护理患者
- transfer_record.transfer_time  → 判断昨日新入科
- order_main.order_type          → 区分长期/临时医嘱
- order_item.order_time          → 筛选前12小时医嘱`, size: 18, font: 'Courier New' })]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun('3.2 交班业务ER图')] }),
      new Paragraph({
        shading: { fill: 'F5F5F5', type: ShadingType.CLEAR },
        spacing: { before: 120, after: 120 },
        children: [new TextRun({ text: 
`┌──────────────────┐      ┌──────────────────┐
│  shift_handover  │─────▶│ handover_patient │
│  (交班主表)      │      │ (交班患者明细)   │
└──────────────────┘      └──────────────────┘

handover_patient.filter_reason 字段值：
- 'NEW_ADMISSION' : 昨日新入科
- 'LEVEL1_NURSING': I级护理
- 'BOTH'          : 同时满足两个条件

handover_patient.current_condition 字段：
- 包含：目前情况 + 前12小时临时医嘱
- 生成交班报告时查询临时医嘱，合并写入此字段`, size: 18, font: 'Courier New' })]
      }),

      new Paragraph({ children: [new PageBreak()] }),

      new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun('4. 表结构定义')] }),
      
      ...tables.flatMap(table => [
        new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun(`4.${tables.indexOf(table) + 1} ${table.name}（${table.description}）`)] }),
        new Paragraph({ children: [new TextRun({ text: `所属表组：${table.group}`, size: 20, color: '808080' })] }),
        new Table({
          width: { size: 9360, type: WidthType.DXA },
          columnWidths: [1800, 2200, 800, 2000, 2560],
          rows: [
            createTableHeader(['字段名', '类型', '必填', '默认值', '说明']),
            ...table.fields.map(f => createTableRow(f))
          ]
        }),
        new Paragraph({ spacing: { before: 120, after: 240 }, children: [new TextRun({ text: '索引：' + table.indexes.join(' | '), size: 20, color: '808080' })] })
      ]),

      new Paragraph({ children: [new PageBreak()] }),

      new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun('5. 交班筛选逻辑SQL')] }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun('5.1 筛选需要交班的患者')] }),
      new Paragraph({
        shading: { fill: 'F5F5F5', type: ShadingType.CLEAR },
        spacing: { before: 120, after: 120 },
        children: [new TextRun({ text: 
`SELECT DISTINCT v.id as visit_id, v.patient_id, v.bed_no, v.nurse_level,
       CASE 
           WHEN tr.id IS NOT NULL AND v.nurse_level = '一级' THEN 'BOTH'
           WHEN tr.id IS NOT NULL THEN 'NEW_ADMISSION'
           ELSE 'LEVEL1_NURSING'
       END as filter_reason
FROM visit v
LEFT JOIN transfer_record tr ON tr.visit_id = v.id 
    AND tr.to_dept_id = v.dept_id
    AND DATE(tr.transfer_time) = CURDATE() - INTERVAL 1 DAY
WHERE v.dept_id = #{currentDeptId}
  AND v.patient_status = '在院'
  AND (
      DATE(tr.transfer_time) = CURDATE() - INTERVAL 1 DAY
      OR v.nurse_level = '一级'
  );`, size: 18, font: 'Courier New' })]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun('5.2 获取前12小时临时医嘱')] }),
      new Paragraph({
        shading: { fill: 'F5F5F5', type: ShadingType.CLEAR },
        spacing: { before: 120, after: 120 },
        children: [new TextRun({ text: 
`SELECT oi.*, om.order_no, om.order_category
FROM order_item oi
JOIN order_main om ON oi.main_id = om.id
WHERE om.visit_id = #{visitId}
  AND om.order_type = '临时医嘱'
  AND oi.order_time >= NOW() - INTERVAL 12 HOUR
  AND om.status = '有效'
ORDER BY oi.order_time DESC;`, size: 18, font: 'Courier New' })]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun('5.3 获取主诊断')] }),
      new Paragraph({
        shading: { fill: 'F5F5F5', type: ShadingType.CLEAR },
        spacing: { before: 120, after: 120 },
        children: [new TextRun({ text: 
`SELECT di.diagnosis_name
FROM diagnosis_item di
JOIN diagnosis_main dm ON di.main_id = dm.id
WHERE dm.visit_id = #{visitId}
  AND di.is_main = 1
  AND dm.status = '有效'
ORDER BY dm.diagnosis_time DESC
LIMIT 1;`, size: 18, font: 'Courier New' })]
      }),

      new Paragraph({ children: [new PageBreak()] }),

      new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun('6. 关键设计说明')] }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun('6.1 为什么要分离患者表和就诊表？')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun('一对多关系：一个患者可以多次住院，每次住院是独立的就诊记录')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun('数据性质不同：患者身份信息相对稳定，就诊信息动态变化')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun('便于HIS同步：HIS系统中患者信息和就诊信息本身就是分开的')] }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun('6.2 为什么要设计转科记录表？')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun('业务需要：判断"昨日新入科"患者需要转科历史')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun('完整记录：记录患者完整的转科轨迹')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun('查询效率：独立表便于按时间、科室查询')] }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun('6.3 为什么医嘱要区分长期/临时？')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun('交班内容需要：交班报告只展示前12小时临时医嘱')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun('医嘱性质不同：长期医嘱持续有效，临时医嘱执行一次')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun('HIS标准设计：符合HIS系统医嘱管理标准')] }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun('6.4 filter_reason字段的作用')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun('追溯原因：记录患者为什么被选中交班')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun('统计分析：便于统计新入科、I级护理患者数量')] }),
      new Paragraph({ numbering: { reference: 'bullets', level: 0 }, children: [new TextRun('审核需要：交班报告需要展示筛选依据')] }),

      new Paragraph({ spacing: { before: 480 }, alignment: AlignmentType.CENTER, children: [new TextRun({ text: '— 文档结束 —', size: 22, color: '808080' })] })
    ]
  }]
});

Packer.toBuffer(doc).then(buffer => {
  fs.writeFileSync('/Users/admin/workspace/his_agent_v4/docs/数据库设计文档_V2.docx', buffer);
  console.log('文档已生成: /Users/admin/workspace/his_agent_v4/docs/数据库设计文档_V2.docx');
});