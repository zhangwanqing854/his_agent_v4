## 上下文

HandoverManagement.vue 已包含交班详情弹窗模板（line 107-179），配置正确：
- `v-model="showViewDialog"` 控制显示
- `append-to-body` 确保弹窗渲染到 body
- handleView 函数已定义（line 478-490）

当前问题：
- "查看"按钮使用 `@click.stop.prevent="handleView(row)"` (line 49)
- `.stop.prevent` 修饰符可能干扰 Element Plus 按钮的内部事件处理
- 弹窗完全不显示或用户看不到内容

## 目标 / 非目标

**目标：**
- 修复"查看"按钮点击响应问题
- 确保交班详情弹窗正常显示
- 添加加载状态和错误提示

**非目标：**
- 不重构弹窗结构（现有模板已正确）
- 不创建新组件（如 HandoverViewDialog）
- 不使用 ReportPreview 替换（可选优化，不在本次修复范围）

## 决策

### 决策1: 移除 `.stop.prevent` 修饰符

**选择：** 移除 `@click.stop.prevent`，改用 `@click="handleView(row)"`

**理由：**
- Element Plus 的 `el-button` 组件有自己的事件处理逻辑
- `.stop` 阻止事件冒泡可能阻止 Element Plus 内部事件传播
- `.prevent` 阻止默认行为可能干扰按钮的激活状态
- 操作列的点击事件穿透问题可通过其他方式处理（如表格行点击）

**替代方案：**
- 保留修饰符，检查 Element Plus 版本兼容性 → 风险高，不确定
- 使用原生 button 元素 → 不符合 Element Plus 统一风格

### 册策2: 优化 handleView 函数

**选择：** 添加加载状态和错误提示

**理由：**
- 当前函数失败时仅 console.error，用户无感知
- 添加 `viewLoading` 状态显示加载过程
- 添加 ElMessage.error 提示加载失败

**实现细节：**
```typescript
const viewLoading = ref(false)

const handleView = async (row: HandoverDto) => {
  currentHandover.value = row
  showViewDialog.value = true
  viewLoading.value = true
  
  try {
    const res = await fetchHandoverPatients(row.id)
    if (res.code === 0) {
      currentHandover.value = { ...row, patients: res.data || [] }
    } else {
      ElMessage.error(res.message || '加载患者数据失败')
    }
  } catch (error) {
    ElMessage.error('加载患者数据失败')
    console.error('[HandoverManagement] 加载患者数据失败:', error)
  } finally {
    viewLoading.value = false
  }
}
```

### 决策3: 弹窗内添加加载状态显示

**选择：** 在患者列表区域添加 v-loading 指令

**理由：**
- 用户能看到数据加载过程
- 区分"弹窗已打开"和"数据已加载"

## 风险 / 权衡

| 风险 | 缓解措施 |
|-----|---------|
| 移除修饰符后事件穿透到表格行 | 表格行无点击事件，风险低 |
| 弹窗 z-index 被其他元素覆盖 | `append-to-body` 确保正确层级 |
| 异步加载失败弹窗已显示 | 添加错误提示，用户可关闭弹窗 |