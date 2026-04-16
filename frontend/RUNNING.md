# 🏥 医护智能交接班系统 - 前端运行说明

## ✅ 项目已启动

**访问地址**: http://localhost:5173

### 默认登录账号
- **账号**: `admin`
- **密码**: `admin`
- **验证码**: 点击验证码图片可刷新

---

## 🎨 已实现功能

### 1. 登录页面
- ✅ 账号密码登录
- ✅ 图形验证码
- ✅ 淡橙色主题 + 磨砂玻璃效果
- ✅ 圆角风格 UI

### 2. 仪表盘（首页）
- ✅ 统计数据展示（在院患者、交班数量等）
- ✅ 快捷操作入口
- ✅ 用户信息显示

### 3. 患者管理
- ✅ 患者列表展示
- ✅ 科室/床号筛选
- ✅ 风险评分显示（MEWS、Braden）
- ✅ 分页功能

### 4. 交班管理
- ✅ 交班列表
- ✅ 状态标签（草稿/待处理/交班中/已完成）
- ✅ 接班/退回操作

---

## 🎨 UI 风格特点

### 配色方案
- **主色调**: 淡橙色 `#FFB366` / `#FF9443`
- **背景色**: 白色 + 浅灰
- **辅助色**: 绿/橙/红/蓝

### 视觉效果
- **磨砂玻璃**: `backdrop-filter: blur(10px)`
- **圆角**: `12px` 统一圆角
- **渐变按钮**: 橙色渐变背景
- **柔和阴影**: 提升层次感

---

## 📁 项目结构

```
frontend/
├── src/
│   ├── api/              # API 请求层
│   │   ├── auth.ts       # 认证 API
│   │   └── request.ts    # Axios 封装
│   ├── mock/             # Mock 数据
│   │   ├── auth.ts       # 登录 Mock
│   │   ├── patient.ts    # 患者 Mock
│   │   └── handover.ts   # 交班 Mock
│   ├── router/           # 路由配置
│   ├── stores/           # Pinia 状态管理
│   │   └── auth.ts       # 认证状态
│   ├── styles/           # 样式文件
│   │   └── theme.css     # 主题样式
│   ├── views/            # 页面视图
│   │   ├── Login.vue           # 登录页
│   │   ├── Dashboard.vue       # 仪表盘
│   │   ├── PatientManagement.vue  # 患者管理
│   │   └── HandoverManagement.vue # 交班管理
│   ├── App.vue
│   └── main.ts
├── package.json
├── vite.config.ts
└── ...
```

---

## 🚀 开发命令

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview

# 代码检查
npm run lint
```

---

## 🔧 技术栈

- **框架**: Vue 3.4 + TypeScript
- **构建工具**: Vite 5
- **UI 组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP 客户端**: Axios
- **Mock 服务**: vite-plugin-mock + Mock.js
- **图标**: Element Plus Icons

---

## 📝 下一步计划

### Phase 1 - 核心功能（进行中）
- [x] 项目初始化和主题配置
- [x] 登录认证
- [x] 患者列表
- [x] 交班列表
- [ ] 患者详情和编辑
- [ ] 交班报告编辑（AI 生成）
- [ ] 语音录入（讯飞）
- [ ] 风险评估
- [ ] 待办事项

### Phase 2 - 后端对接
- [ ] Spring Boot 后端实现
- [ ] 数据库设计
- [ ] 真实 API 对接
- [ ] 阿里大模型集成
- [ ] 讯飞语音集成

---

## ⚠️ 注意事项

1. **Mock 数据**: 当前使用 Mock 数据，刷新会重置
2. **验证码**: 每次刷新都会变化，点击可重新获取
3. **代理配置**: 开发环境代理到 `http://localhost:8080`（后端）
4. **主题定制**: 修改 `src/styles/theme.css` 可调整主题色

---

## 🎯 访问方式

1. 打开浏览器访问：http://localhost:5173
2. 使用默认账号登录：`admin` / `admin`
3. 输入验证码（点击图片可刷新）
4. 点击登录即可进入系统

---

## 📞 问题反馈

如有问题，请检查：
1. 控制台是否有错误信息
2. 网络请求是否成功（F12 -> Network）
3. Mock 服务是否正常加载
