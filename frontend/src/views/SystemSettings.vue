<template>
  <div class="settings-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button text @click="router.push('/')">
          <el-icon><ArrowLeft /></el-icon>
          返回首页
        </el-button>
      </div>
      <h1 class="page-title">
        <el-icon class="title-icon"><Setting /></el-icon>
        系统设置
      </h1>
      <div class="header-right"></div>
    </div>

    <!-- 主内容区 -->
    <div class="settings-content">
      <el-tabs v-model="activeTab" class="settings-tabs">
        <!-- 接口配置 Tab -->
        <el-tab-pane label="接口配置" name="config">
          <div class="config-section">
            <!-- 筛选和操作栏 -->
            <div class="filter-bar">
              <div class="filter-left">
                <el-select v-model="configFilter.system" placeholder="三方系统" clearable style="width: 140px">
                  <el-option label="HIS系统" value="HIS" />
                  <el-option label="移动护理" value="MOBILE_NURSING" />
                  <el-option label="CDR系统" value="CDR" />
                  <el-option label="其他" value="OTHER" />
                </el-select>
                <el-select v-model="configFilter.mode" placeholder="接口模式" clearable style="width: 120px">
                  <el-option label="数据拉取" value="PULL" />
                  <el-option label="数据推送" value="PUSH" />
                </el-select>
                <el-select v-model="configFilter.enabled" placeholder="状态" clearable style="width: 100px">
                  <el-option label="已启用" :value="true" />
                  <el-option label="已禁用" :value="false" />
                </el-select>
              </div>
              <div class="filter-right">
                <el-button @click="handleSyncAll" :loading="syncLoading">
                  <el-icon><Refresh /></el-icon>
                  同步全部数据
                </el-button>
                <el-button type="primary" @click="handleAddConfig">
                  <el-icon><Plus /></el-icon>
                  新增配置
                </el-button>
              </div>
            </div>

            <!-- 配置列表 -->
            <el-table
              :data="filteredConfigList"
              style="width: 100%"
              class="config-table"
              :header-cell-style="{ background: '#fafafa', color: '#303133', fontWeight: '600' }"
            >
              <el-table-column prop="configName" label="接口名称" min-width="160">
                <template #default="{ row }">
                  <div class="config-name">
                    <span class="name-text">{{ row.configName }}</span>
                    <el-tag v-if="!row.enabled" type="info" size="small">已禁用</el-tag>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="system" label="三方系统" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getSystemTagType(row.system)" effect="light" size="small">
                    {{ systemLabelMap[row.system] || row.system }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="mode" label="模式" width="80" align="center">
                <template #default="{ row }">
                  <span :class="['mode-tag', row.mode ? row.mode.toLowerCase() : '']">
                    {{ row.mode === 'PULL' ? '拉取' : '推送' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="dataType" label="数据类型" width="100" align="center">
                <template #default="{ row }">
                  <span class="data-type-text">{{ dataTypeLabelMap[row.dataType] || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="同步状态" width="120" align="center">
                <template #default="{ row }">
                  <div v-if="row.mode === 'PULL'" class="sync-status">
                    <el-tag :type="getSyncStatusType(row.lastSyncStatus)" effect="light" size="small">
                      {{ syncStatusLabelMap[row.lastSyncStatus] || '未同步' }}
                    </el-tag>
                  </div>
                  <span v-else class="no-data">-</span>
                </template>
              </el-table-column>
              <el-table-column label="最后同步" width="140" align="center">
                <template #default="{ row }">
                  <div v-if="row.mode === 'PULL' && row.lastSyncTime">
                    <span class="time-text">{{ formatTime(row.lastSyncTime) }}</span>
                  </div>
                  <span v-else class="no-data">-</span>
                </template>
              </el-table-column>
              <el-table-column label="记录数" width="80" align="center">
                <template #default="{ row }">
                  <span v-if="row.mode === 'PULL'" class="count-text">{{ row.lastSyncCount || 0 }}</span>
                  <span v-else class="no-data">-</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="240" align="center" fixed="right">
                <template #default="{ row }">
                  <el-button
                    v-if="row.mode === 'PULL'"
                    type="success"
                    link
                    size="small"
                    @click="handleSync(row)"
                  >
                    <el-icon><Refresh /></el-icon>
                    同步
                  </el-button>
                  <el-button type="primary" link size="small" @click="handleTestConfig(row)">
                    <el-icon><Connection /></el-icon>
                    测试
                  </el-button>
                  <el-button type="primary" link size="small" @click="handleEditConfig(row)">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-button>
                  <el-button
                    :type="row.enabled ? 'warning' : 'success'"
                    link
                    size="small"
                    @click="handleToggleConfig(row)"
                  >
                    {{ row.enabled ? '禁用' : '启用' }}
                  </el-button>
                  <el-button type="danger" link size="small" @click="handleDeleteConfig(row)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 调用日志 Tab -->
        <el-tab-pane label="调用日志" name="logs">
          <div class="logs-section">
            <!-- 筛选栏 -->
            <div class="filter-bar">
              <div class="filter-left">
                <el-select v-model="logFilter.configId" placeholder="选择接口" clearable style="width: 200px" filterable>
                  <el-option
                    v-for="config in configList"
                    :key="config.id"
                    :label="config.configName"
                    :value="config.id"
                  />
                </el-select>
                <el-date-picker
                  v-model="logFilter.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  style="width: 260px"
                  value-format="YYYY-MM-DD"
                />
                <el-select v-model="logFilter.status" placeholder="调用状态" clearable style="width: 120px">
                  <el-option label="成功" value="SUCCESS" />
                  <el-option label="失败" value="FAILED" />
                  <el-option label="超时" value="TIMEOUT" />
                </el-select>
              </div>
              <div class="filter-right">
                <el-button @click="handleRefreshLogs">
                  <el-icon><Refresh /></el-icon>
                  刷新
                </el-button>
              </div>
            </div>

            <!-- 日志列表 -->
            <el-table
              :data="filteredLogList"
              style="width: 100%"
              class="log-table"
              :header-cell-style="{ background: '#fafafa', color: '#303133', fontWeight: '600' }"
            >
              <el-table-column prop="callTime" label="调用时间" width="180" align="center">
                <template #default="{ row }">
                  <span class="time-text">{{ row.callTime }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="configName" label="接口名称" min-width="180">
                <template #default="{ row }">
                  <span class="config-name-text">{{ row.configName }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getStatusTagType(row.status)" effect="light" size="small">
                    {{ statusLabelMap[row.status] }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="duration" label="耗时" width="100" align="center">
                <template #default="{ row }">
                  <span class="duration-text">{{ row.duration }}ms</span>
                </template>
              </el-table-column>
              <el-table-column prop="errorMessage" label="错误信息" min-width="200">
                <template #default="{ row }">
                  <span v-if="row.errorMessage" class="error-text">{{ row.errorMessage }}</span>
                  <span v-else class="no-data">-</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="center">
                <template #default="{ row }">
                  <el-button type="primary" link size="small" @click="handleViewLog(row)">
                    <el-icon><View /></el-icon>
                    详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination-wrapper">
              <el-pagination
                v-model:current-page="logPagination.page"
                v-model:page-size="logPagination.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="logPagination.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleLogPageSizeChange"
                @current-change="handleLogPageChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 系统配置 Tab -->
        <el-tab-pane label="系统配置" name="system">
          <div class="system-config-section">
            <div class="config-card">
              <div class="card-header">
                <el-icon class="header-icon"><Timer /></el-icon>
                <span class="header-title">交班时间设置</span>
              </div>
              <div class="card-content">
                <div class="config-row">
                  <div class="config-item">
                    <label>早班交班时间</label>
                    <el-time-select
                      v-model="systemConfig.morningShift"
                      start="06:00"
                      step="00:30"
                      end="12:00"
                      placeholder="选择时间"
                    />
                  </div>
                  <div class="config-item">
                    <label>晚班交班时间</label>
                    <el-time-select
                      v-model="systemConfig.eveningShift"
                      start="16:00"
                      step="00:30"
                      end="22:00"
                      placeholder="选择时间"
                    />
                  </div>
                  <div class="config-item">
                    <label>同步提前时间</label>
                    <el-input-number v-model="systemConfig.syncAdvanceMinutes" :min="5" :max="60" />
                    <span class="unit">分钟</span>
                  </div>
                </div>
                <div class="config-hint">
                  <el-icon><InfoFilled /></el-icon>
                  <span>系统将在交班时间前 {{ systemConfig.syncAdvanceMinutes }} 分钟自动同步数据</span>
                </div>
              </div>
            </div>

            <div class="config-card">
              <div class="card-header">
                <el-icon class="header-icon"><UserIcon /></el-icon>
                <span class="header-title">AI 服务配置</span>
              </div>
              <div class="card-content">
                <div class="config-row">
                  <div class="config-item large">
                    <label>AI 服务提供商</label>
                    <el-select v-model="systemConfig.aiProvider" placeholder="选择AI服务">
                      <el-option label="阿里通义千问 (DashScope)" value="dashscope" />
                      <el-option label="百度文心一言" value="wenxin" />
                      <el-option label="智谱 GLM" value="zhipu" />
                    </el-select>
                  </div>
                  <div class="config-item large">
                    <label>API Key</label>
                    <el-input v-model="systemConfig.aiApiKey" type="password" placeholder="请输入API Key" show-password />
                  </div>
                </div>
              </div>
            </div>

            <div class="config-card">
              <div class="card-header">
                <el-icon class="header-icon"><Bell /></el-icon>
                <span class="header-title">通知设置</span>
              </div>
              <div class="card-content">
                <div class="config-row">
                  <div class="config-item">
                    <label>待办提醒方式</label>
                    <el-checkbox-group v-model="systemConfig.notifyMethods">
                      <el-checkbox label="站内消息" value="in_app" />
                      <el-checkbox label="企业微信" value="wechat" />
                      <el-checkbox label="短信" value="sms" />
                    </el-checkbox-group>
                  </div>
                </div>
              </div>
            </div>

            <div class="config-card">
              <div class="card-header">
                <el-icon class="header-icon"><Message /></el-icon>
                <span class="header-title">短信通知配置</span>
                <el-button type="primary" link size="small" style="margin-left: auto" @click="router.push('/sms-config')">
                  详细配置 →
                </el-button>
              </div>
              <div class="card-content">
                <div class="config-row">
                  <div class="config-item">
                    <label>短信功能</label>
                    <el-switch v-model="smsConfig.enabled" @change="handleSmsEnabledChange" />
                    <span class="status-text">{{ smsConfig.enabled ? '已启用' : '已禁用' }}</span>
                  </div>
                  <div class="config-item">
                    <label>短信平台</label>
                    <el-tag type="info">{{ smsConfig.provider === 'aliyun' ? '阿里云' : '腾讯云' }}</el-tag>
                  </div>
                  <div class="config-item">
                    <label>短信签名</label>
                    <span class="value-text">{{ smsConfig.signName || '未配置' }}</span>
                  </div>
                </div>
                <div class="config-hint">
                  <el-icon><InfoFilled /></el-icon>
                  <span>交班提交后将自动发送短信通知接班医生，点击"详细配置"可配置短信平台参数</span>
                </div>
              </div>
            </div>

            <div class="config-card">
              <div class="card-header">
                <el-icon class="header-icon"><Document /></el-icon>
                <span class="header-title">报告查看配置</span>
              </div>
              <div class="card-content">
                <div class="config-row">
                  <div class="config-item large">
                    <label>检查报告URL</label>
                    <el-input 
                      v-model="reportConfig.examReportUrl" 
                      placeholder="请输入检查报告URL，使用 {{:patient_no}} 作为患者编码变量" 
                    />
                  </div>
                </div>
                <div class="config-row">
                  <div class="config-item large">
                    <label>检验报告URL</label>
                    <el-input 
                      v-model="reportConfig.testReportUrl" 
                      placeholder="请输入检验报告URL，使用 {{:patient_no}} 和 {{:userId}} 作为变量" 
                    />
                  </div>
                </div>
                <div class="config-hint">
                  <el-icon><InfoFilled /></el-icon>
                  <span>URL中的 &#123;&#123;:patient_no&#125;&#125; 将替换为患者编码，&#123;&#123;:userId&#125;&#125; 将替换为当前登录医生的用户编码</span>
                </div>
              </div>
            </div>

            <div class="config-actions">
              <el-button @click="handleResetConfig">恢复默认</el-button>
              <el-button type="primary" @click="handleSaveConfig">
                <el-icon><Check /></el-icon>
                保存配置
              </el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 接口配置编辑弹窗 -->
    <InterfaceConfigDialog
      v-model="configDialogVisible"
      :config="currentConfig"
      @success="handleConfigSuccess"
    />

    <!-- 调用日志详情弹窗 -->
    <CallLogDialog v-model="logDialogVisible" :log="currentLog" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Setting,
  Plus,
  Edit,
  Delete,
  Connection,
  View,
  Refresh,
  ArrowLeft,
  Timer,
  InfoFilled,
  User as UserIcon,
  Bell,
  Check,
  Message,
  Document
} from '@element-plus/icons-vue'
import InterfaceConfigDialog from '@/components/settings/InterfaceConfigDialog.vue'
import CallLogDialog from '@/components/settings/CallLogDialog.vue'
import type { InterfaceConfig } from '@/types/interface-config'
import { getInterfaceConfigList, deleteInterfaceConfig, updateInterfaceConfig, testInterfaceConnection } from '@/api/interface-config'
import { getSmsConfig, updateSmsConfig } from '@/api/smsConfig'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// Tab 状态
const activeTab = ref('system')
const syncLoading = ref(false)

// 配置筛选
const configFilter = reactive({
  system: '',
  mode: '',
  enabled: undefined as boolean | undefined
})

// 日志筛选
const logFilter = reactive({
  configId: null as number | null,
  dateRange: null as [string, string] | null,
  status: ''
})

// 分页
const logPagination = reactive({
  page: 1,
  pageSize: 10,
  total: 35
})

// 弹窗状态
const configDialogVisible = ref(false)
const logDialogVisible = ref(false)
const currentConfig = ref<InterfaceConfig | null>(null)
const currentLog = ref<CallLog | null>(null)

// 系统配置
const systemConfig = reactive({
  morningShift: '08:00',
  eveningShift: '17:00',
  syncAdvanceMinutes: 30,
  aiProvider: 'dashscope',
  aiApiKey: '',
  notifyMethods: ['in_app']
})

const smsConfig = reactive({
  enabled: false,
  provider: 'aliyun',
  signName: ''
})

const reportConfig = reactive({
  examReportUrl: '',
  testReportUrl: ''
})

const loadSmsConfig = async () => {
  try {
    const res = await getSmsConfig()
    if (res.code === 0 && res.data) {
      smsConfig.enabled = res.data.enabled
      smsConfig.provider = res.data.provider
      for (const c of res.data.configs || []) {
        if (c.key === 'aliyun_sign_name') {
          smsConfig.signName = c.value || ''
        }
      }
    }
  } catch (error) {
    console.error('Load SMS config error:', error)
  }
}

const loadReportConfig = async () => {
  try {
    const res = await fetch('/api/system-config')
    const result = await res.json()
    if (result.code === 0 && result.data) {
      reportConfig.examReportUrl = result.data.EXAM_REPORT_URL || ''
      reportConfig.testReportUrl = result.data.TEST_REPORT_URL || ''
    }
  } catch (error) {
    console.error('Load report config error:', error)
  }
}

const handleSmsEnabledChange = async (enabled: boolean) => {
  try {
    await updateSmsConfig({
      enabled: enabled,
      provider: smsConfig.provider
    })
    ElMessage.success(enabled ? '短信功能已启用' : '短信功能已禁用')
  } catch (error) {
    ElMessage.error('配置保存失败')
    smsConfig.enabled = !enabled
  }
}

// 类型定义
interface CallLog {
  id: number
  configId: number
  configName: string
  callTime: string
  duration: number
  status: 'SUCCESS' | 'FAILED' | 'TIMEOUT'
  request: string
  response: string
  errorMessage?: string
}

// 接口配置列表
const configList = ref<InterfaceConfig[]>([])

// 加载配置列表
const loadConfigList = async () => {
  try {
    const result = await getInterfaceConfigList()
    console.log('API result:', result)
    if (result.code === 0) {
      configList.value = result.data.map(config => {
        // 解析 JSON 字符串字段
        let soapParams = config.soapParams
        if (typeof soapParams === 'string' && soapParams) {
          try {
            soapParams = JSON.parse(soapParams)
          } catch {
            soapParams = []
          }
        }
        
        return {
          ...config,
          soapParams,
          lastSyncTime: config.lastSyncTime || new Date(Date.now() - 3600000).toISOString(),
          lastSyncStatus: config.lastSyncStatus || 'SUCCESS',
          lastSyncCount: config.lastSyncCount || 0
        }
      })
      console.log('Loaded configs:', configList.value.length)
    }
  } catch (error) {
    console.error('Load error:', error)
    ElMessage.error('加载配置列表失败')
  }
}

onMounted(() => {
  loadConfigList()
  loadSmsConfig()
  loadReportConfig()
})

// Mock 调用日志数据
const logList = ref<CallLog[]>([
  { id: 1, configId: 3, configName: '拉取患者列表', callTime: '2026-03-25 14:30:22', duration: 125, status: 'SUCCESS', request: '{}', response: '{"code":0,"data":[...]}' },
  { id: 2, configId: 2, configName: '拉取医护人员信息', callTime: '2026-03-25 14:25:18', duration: 230, status: 'SUCCESS', request: '{}', response: '{"code":0,"data":[...]}' },
  { id: 3, configId: 4, configName: '拉取诊断信息', callTime: '2026-03-25 13:45:10', duration: 450, status: 'SUCCESS', request: '{}', response: '{"code":0,"data":[...]}' },
  { id: 4, configId: 6, configName: '拉取体征数据', callTime: '2026-03-25 10:30:00', duration: 0, status: 'TIMEOUT', request: '{}', response: '', errorMessage: '连接超时，已重试3次' },
  { id: 5, configId: 7, configName: '拉取检查报告', callTime: '2026-03-25 09:45:22', duration: 180, status: 'SUCCESS', request: '{}', response: '{"code":0,"data":[...]}' },
  { id: 6, configId: 8, configName: '拉取检验报告', callTime: '2026-03-25 09:30:15', duration: 0, status: 'FAILED', request: '{}', response: '', errorMessage: '认证失败：Token已过期' },
  { id: 7, configId: 9, configName: '推送交班报告', callTime: '2026-03-25 08:00:00', duration: 320, status: 'SUCCESS', request: '{}', response: '{"code":0}' },
  { id: 8, configId: 10, configName: '推送交班报告', callTime: '2026-03-25 08:00:05', duration: 280, status: 'SUCCESS', request: '{}', response: '{"code":0}' }
])

// 映射
const systemLabelMap: Record<string, string> = {
  HIS: 'HIS系统',
  MOBILE_NURSING: '移动护理',
  CDR: 'CDR系统',
  OTHER: '其他'
}

const dataTypeLabelMap: Record<string, string> = {
  DEPARTMENT: '科室信息',
  STAFF: '医护人员',
  PATIENT: '患者列表',
  TRANSFER: '转科信息',
  DOCTOR_DEPT: '科室人员关系',
  DIAGNOSIS: '诊断信息',
  MEDICAL_RECORD: '病例信息',
  VISIT: '就诊信息',
  ORDER: '医嘱信息',
  VITAL_SIGN: '体征数据',
  EXAM_REPORT: '检查报告',
  LAB_REPORT: '检验报告'
}

const statusLabelMap: Record<string, string> = {
  SUCCESS: '成功',
  FAILED: '失败',
  TIMEOUT: '超时'
}

const syncStatusLabelMap: Record<string, string> = {
  SUCCESS: '成功',
  FAILED: '失败',
  PARTIAL: '部分成功'
}

// 筛选后的配置列表
const filteredConfigList = computed(() => {
  return configList.value.filter(config => {
    if (configFilter.system && config.system !== configFilter.system) return false
    if (configFilter.mode && config.mode !== configFilter.mode) return false
    if (configFilter.enabled !== undefined && config.enabled !== configFilter.enabled) return false
    return true
  })
})

// 筛选后的日志列表
const filteredLogList = computed(() => {
  return logList.value.filter(log => {
    if (logFilter.configId && log.configId !== logFilter.configId) return false
    if (logFilter.status && log.status !== logFilter.status) return false
    return true
  })
})

// 获取系统标签类型
const getSystemTagType = (system: string): '' | 'success' | 'warning' | 'info' | 'danger' => {
  const map: Record<string, '' | 'success' | 'warning' | 'info' | 'danger'> = {
    HIS: 'warning',
    MOBILE_NURSING: 'success',
    CDR: 'info',
    OTHER: ''
  }
  return map[system] || ''
}

// 获取状态标签类型
const getStatusTagType = (status: string): '' | 'success' | 'warning' | 'info' | 'danger' => {
  const map: Record<string, '' | 'success' | 'warning' | 'info' | 'danger'> = {
    SUCCESS: 'success',
    FAILED: 'danger',
    TIMEOUT: 'warning'
  }
  return map[status] || ''
}

// 获取同步状态标签类型
const getSyncStatusType = (status: string | undefined): '' | 'success' | 'warning' | 'info' | 'danger' => {
  const map: Record<string, '' | 'success' | 'warning' | 'info' | 'danger'> = {
    SUCCESS: 'success',
    FAILED: 'danger',
    PARTIAL: 'warning'
  }
  return map[status || ''] || 'info'
}

// 格式化时间
const formatTime = (time: string): string => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  
  const days = Math.floor(hours / 24)
  return `${days}天前`
}

// 新增配置
const handleAddConfig = () => {
  currentConfig.value = null
  configDialogVisible.value = true
}

// 编辑配置
const handleEditConfig = (config: InterfaceConfig) => {
  currentConfig.value = { ...config }
  configDialogVisible.value = true
}

// 测试连接
const handleTestConfig = async (config: InterfaceConfig) => {
  ElMessage.info(`正在测试接口: ${config.configName}`)
  
  try {
    const result = await testInterfaceConnection(config.id!)
    if (result.code === 0 && result.data?.success) {
      const { message, responseTime, statusCode } = result.data
      const timeInfo = responseTime ? ` (${responseTime}ms)` : ''
      const statusInfo = statusCode ? ` [HTTP ${statusCode}]` : ''
      ElMessage.success(`测试成功: ${message}${timeInfo}${statusInfo}`)
    } else {
      const errorMsg = result.data?.message || result.message || '接口连接失败'
      const detail = result.data?.errorDetail ? `\n${result.data.errorDetail}` : ''
      ElMessage.error(`${errorMsg}${detail}`)
    }
  } catch (error: any) {
    ElMessage.error(error.message || '接口连接失败')
  }
}

// 同步单个接口
const handleSync = async (config: InterfaceConfig) => {
  ElMessage.info(`正在同步: ${config.configName}`)
  
  const deptCode = authStore.currentDepartment?.code || ''
  
  try {
    const response = await fetch(`/api/sync/execute/${config.id}?deptCode=${encodeURIComponent(deptCode)}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    const result = await response.json()
    
    if (result.code === 0 && result.data?.success) {
      const syncResult = result.data
      config.lastSyncTime = new Date().toISOString()
      config.lastSyncStatus = 'SUCCESS'
      config.lastSyncCount = syncResult.totalCount || 0
      
      const message = syncResult.message || `同步完成：共${syncResult.totalCount || 0}条`
      if (syncResult.totalCount === 0) {
        ElMessage.info(message)
      } else {
        ElMessage.success(
          `同步完成：共${syncResult.totalCount || 0}条，新增${syncResult.insertCount || 0}条，更新${syncResult.updateCount || 0}条，跳过${syncResult.skipCount || 0}条`
        )
      }
      
      loadConfigList()
    } else {
      config.lastSyncStatus = 'FAILED'
      const errorMsg = result.message || '同步失败'
      if (errorMsg.includes('无新数据') || errorMsg.includes('空数据')) {
        ElMessage.info(errorMsg)
        config.lastSyncStatus = 'SUCCESS'
      } else {
        ElMessage.error(errorMsg)
      }
    }
  } catch (error: any) {
    config.lastSyncStatus = 'FAILED'
    ElMessage.error(error.message || '同步失败')
  }
}

// 同步全部
const handleSyncAll = async () => {
  syncLoading.value = true
  ElMessage.info('正在同步全部数据...')
  
  const deptCode = authStore.currentDepartment?.code || ''
  
  try {
    const pullConfigs = configList.value.filter(c => c.mode === 'PULL' && c.enabled)
    let successCount = 0
    let failCount = 0
    
    for (const config of pullConfigs) {
      try {
        const response = await fetch(`/api/sync/execute/${config.id}?deptCode=${encodeURIComponent(deptCode)}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          }
        })
        
        const result = await response.json()
        
        if (result.code === 0 && result.data?.success) {
          config.lastSyncTime = new Date().toISOString()
          config.lastSyncStatus = 'SUCCESS'
          config.lastSyncCount = result.data.totalCount || 0
          successCount++
        } else {
          const errorMsg = result.message || '同步失败'
          if (errorMsg.includes('无新数据') || errorMsg.includes('空数据')) {
            config.lastSyncTime = new Date().toISOString()
            config.lastSyncStatus = 'SUCCESS'
            config.lastSyncCount = 0
            successCount++
          } else {
            config.lastSyncStatus = 'FAILED'
            failCount++
          }
        }
      } catch {
        config.lastSyncStatus = 'FAILED'
        failCount++
      }
    }
    
    syncLoading.value = false
    
    if (failCount === 0) {
      ElMessage.success(`同步完成，共更新 ${pullConfigs.length} 个接口`)
    } else {
      ElMessage.warning(`同步完成：成功${successCount}个，失败${failCount}个`)
    }
    
    loadConfigList()
  } catch (error: any) {
    syncLoading.value = false
    ElMessage.error(error.message || '同步失败')
  }
}

// 启用/禁用配置
const handleToggleConfig = async (config: InterfaceConfig) => {
  const action = config.enabled ? '禁用' : '启用'
  
  try {
    await ElMessageBox.confirm(`确定要${action}接口"${config.configName}"吗？`, '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await updateInterfaceConfig(config.id!, { enabled: !config.enabled })
    config.enabled = !config.enabled
    ElMessage.success(`已${action}`)
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || `${action}失败`)
    }
  }
}

// 删除配置
const handleDeleteConfig = async (config: InterfaceConfig) => {
  try {
    await ElMessageBox.confirm(`确定要删除接口"${config.configName}"吗？此操作不可恢复。`, '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteInterfaceConfig(config.id!)
    const index = configList.value.findIndex(c => c.id === config.id)
    if (index > -1) {
      configList.value.splice(index, 1)
    }
    ElMessage.success('删除成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 配置保存成功
const handleConfigSuccess = () => {
  loadConfigList()
}

// 查看日志详情
const handleViewLog = (log: CallLog) => {
  currentLog.value = { ...log }
  logDialogVisible.value = true
}

// 刷新日志
const handleRefreshLogs = () => {
  ElMessage.success('日志已刷新')
}

// 分页
const handleLogPageChange = (page: number) => {
  logPagination.page = page
}

const handleLogPageSizeChange = (size: number) => {
  logPagination.pageSize = size
  logPagination.page = 1
}

// 系统配置
const handleSaveConfig = async () => {
  try {
    const res = await fetch('/api/system-config', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        EXAM_REPORT_URL: reportConfig.examReportUrl,
        TEST_REPORT_URL: reportConfig.testReportUrl
      })
    })
    const result = await res.json()
    if (result.code === 0) {
      ElMessage.success('系统配置保存成功')
    } else {
      ElMessage.error(result.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleResetConfig = () => {
  systemConfig.morningShift = '08:00'
  systemConfig.eveningShift = '17:00'
  systemConfig.syncAdvanceMinutes = 30
  systemConfig.aiProvider = 'dashscope'
  systemConfig.aiApiKey = ''
  systemConfig.notifyMethods = ['in_app']
  reportConfig.examReportUrl = 'http://sichuangpacs.pkuih.edu.cn/IntegrationCenter/index.html#/views/eipTimeLine2?CMD=showlist&PW=webweb&DOMAINCODE=patientid&DOMAINID={{:patient_no}}'
  reportConfig.testReportUrl = 'http://10.2.48.64:8001/cdr/login/loginiihPortalIntegrated.html?viewId=V002&patientId={{:patient_no}}&domainId=02&styleId=01&display=0&userId={{:userId}}&visitTimes=&XExternalUrlFlag=1&systemId=SIIH&bsiihtype=0&download=1&visitTimes='
  ElMessage.success('已恢复默认配置')
}
</script>

<style>
.settings-container {
  min-height: 100vh;
  background: var(--bg-secondary);
}

.page-header {
  height: 64px;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-left,
.header-right {
  width: 100px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}

.title-icon {
  color: var(--color-primary-DEFAULT);
}

.settings-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}

.settings-tabs {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  padding: 20px;
}

.settings-tabs .el-tabs__header {
  margin-bottom: 20px;
}

.settings-tabs .el-tabs__nav-wrap::after {
  height: 1px;
}

.settings-tabs .el-tabs__item {
  font-size: 15px;
  font-weight: 500;
}

.settings-tabs .el-tabs__item.is-active {
  color: var(--color-primary-DEFAULT);
}

.settings-tabs .el-tabs__active-bar {
  background-color: var(--color-primary-DEFAULT);
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.filter-left {
  display: flex;
  gap: 12px;
}

.filter-right {
  display: flex;
  gap: 12px;
}

.config-table,
.log-table {
  border-radius: var(--radius-md);
  overflow: hidden;
}

.config-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name-text {
  font-weight: 500;
  color: var(--text-primary);
}

.mode-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.mode-tag.pull {
  background: #e6f7ff;
  color: #1890ff;
}

.mode-tag.push {
  background: #f6ffed;
  color: #52c41a;
}

.data-type-text {
  font-size: 13px;
  color: var(--text-secondary);
}

.sync-status {
  display: flex;
  justify-content: center;
}

.url-text {
  color: var(--text-secondary);
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
  display: inline-block;
}

.method-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.method-badge.get {
  background: #e6f7ff;
  color: #1890ff;
}

.method-badge.post {
  background: #f6ffed;
  color: #52c41a;
}

.method-badge.put {
  background: #fff7e6;
  color: #fa8c16;
}

.method-badge.delete {
  background: #fff1f0;
  color: #f5222d;
}

.time-text {
  color: var(--text-secondary);
  font-size: 13px;
}

.count-text {
  font-weight: 500;
  color: var(--text-primary);
}

.no-data {
  color: var(--text-placeholder);
}

.config-name-text {
  font-weight: 500;
  color: var(--text-primary);
}

.duration-text {
  color: var(--text-secondary);
}

.error-text {
  color: var(--color-danger);
  font-size: 13px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
}

/* 系统配置样式 */
.system-config-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.config-card {
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
}

.header-icon {
  color: var(--color-primary-DEFAULT);
  font-size: 20px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.card-content {
  padding: 8px 0;
}

.config-row {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.config-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 180px;
}

.config-item.large {
  flex: 1;
  min-width: 300px;
}

.config-item label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
}

.config-item .unit {
  font-size: 13px;
  color: var(--text-secondary);
  margin-left: 8px;
}

.config-item .status-text {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.config-item .value-text {
  font-size: 14px;
  color: var(--text-primary);
}

.config-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 12px;
  padding: 10px 12px;
  background: var(--color-primary-50);
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--color-primary-600);
}

.config-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid var(--border-light);
}
</style>