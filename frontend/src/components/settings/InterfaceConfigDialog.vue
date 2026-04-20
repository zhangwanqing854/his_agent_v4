<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑接口配置' : '新增接口配置'"
    width="1000px"
    :close-on-click-modal="false"
    class="config-dialog"
    @closed="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      label-position="top"
      class="config-form"
    >
      <div class="form-section">
        <div class="section-title">
          <el-icon><Setting /></el-icon>
          基本信息
        </div>
        <div class="form-grid">
          <el-form-item label="配置编码" prop="configCode">
            <el-input v-model="form.configCode" placeholder="如 HIS_ORDER_SYNC" :disabled="isEdit" />
          </el-form-item>
          <el-form-item label="配置名称" prop="configName">
            <el-input v-model="form.configName" placeholder="如 HIS医嘱数据同步" />
          </el-form-item>
          <el-form-item label="三方系统" prop="system">
            <el-select v-model="form.system" placeholder="请选择">
              <el-option v-for="opt in SYSTEM_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="数据类型" prop="dataType">
            <el-select v-model="form.dataType" placeholder="请选择">
              <el-option v-for="opt in DATA_TYPE_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="同步模式" prop="syncMode">
            <el-select v-model="form.syncMode" placeholder="请选择">
              <el-option label="手动同步" value="MANUAL" />
              <el-option label="定时同步" value="SCHEDULED" />
              <el-option label="按需同步" value="ON_DEMAND" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="form.syncMode === 'SCHEDULED'" label="定时规则">
            <el-input v-model="form.syncSchedule" placeholder="Cron表达式，如 0 30 7,16 * * ?" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="通讯协议" prop="protocol">
            <el-select v-model="form.protocol" placeholder="请选择">
              <el-option label="HTTP" value="HTTP" />
              <el-option label="HTTPS" value="HTTPS" />
              <el-option label="WebSocket" value="WEBSOCKET" />
            </el-select>
          </el-form-item>
          <el-form-item label="接口协议" prop="apiProtocol">
            <el-select v-model="form.apiProtocol" placeholder="请选择">
              <el-option label="REST" value="REST" />
              <el-option label="SOAP" value="SOAP" />
              <el-option label="HL7" value="HL7" />
              <el-option label="FHIR" value="FHIR" />
            </el-select>
          </el-form-item>
          <el-form-item label="请求方法" prop="method">
            <el-select v-model="form.method" placeholder="请选择" :disabled="isMethodDisabled">
              <el-option label="GET" value="GET" />
              <el-option label="POST" value="POST" />
              <el-option label="PUT" value="PUT" />
              <el-option label="DELETE" value="DELETE" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="接口地址" prop="url">
          <el-input v-model="form.url" placeholder="如 https://his.hospital.com/api/orders" />
        </el-form-item>
        
        <!-- SOAP配置 -->
        <div v-if="form.apiProtocol === 'SOAP'" class="soap-config">
          <div class="form-grid">
            <el-form-item label="SOAP Action">
              <el-input v-model="form.soapAction" placeholder="如 getDept" />
            </el-form-item>
            <el-form-item label="命名空间">
              <el-input v-model="form.soapNamespace" placeholder="如 http://i.sync.common.pkuih.iih/" />
            </el-form-item>
          </div>
          <el-form-item label="SOAP参数">
            <div class="soap-params">
              <div v-for="(param, index) in form.soapParams" :key="index" class="param-row">
                <el-input v-model="param.name" placeholder="参数名" style="width: 120px" />
                <el-input v-model="param.value" placeholder="参数值" style="flex: 1" />
                <el-button type="danger" link @click="form.soapParams?.splice(index, 1)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
              <el-button type="primary" link @click="addSoapParam">
                <el-icon><Plus /></el-icon>
                添加参数
              </el-button>
            </div>
          </el-form-item>
        </div>
      </div>

      <div v-if="form.apiProtocol !== 'SOAP'" class="form-section">
        <div class="section-title">
          <el-icon><Document /></el-icon>
          入参模板
          <span class="section-hint" v-pre>支持变量替换，如 { "deptId": "{{deptId}}" }</span>
        </div>
        <el-form-item prop="requestTemplate">
          <el-input
            v-model="form.requestTemplate"
            type="textarea"
            :rows="3"
            placeholder="JSON格式的请求模板"
            class="code-input"
          />
        </el-form-item>
      </div>

      <div class="form-section">
        <div class="section-title">
          <el-icon><Connection /></el-icon>
          字段映射
          <span class="section-hint">支持主子表映射，子表通过关联字段关联主表</span>
        </div>
        
        <div class="mapping-container">
          <el-button type="primary" @click="addMappingTable">
            <el-icon><Plus /></el-icon>
            添加目标表
          </el-button>
          
          <div v-for="(table, tIndex) in form.mappingTables" :key="tIndex" class="mapping-table-item">
            <div class="table-header">
              <div class="table-info">
                <el-select v-model="table.targetTable" placeholder="选择目标表" style="width: 200px" filterable>
                  <el-option v-for="opt in TARGET_TABLE_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
                </el-select>
                <el-input v-model="table.dataPath" placeholder="数据路径，如 data.orders" style="width: 180px">
                  <template #prepend>路径</template>
                </el-input>
                <el-checkbox v-model="table.isArray">数组数据</el-checkbox>
              </div>
              <div class="table-actions">
                <el-button type="primary" link @click="addChildTable(tIndex)">
                  <el-icon><Plus /></el-icon>
                  添加子表
                </el-button>
                <el-button type="danger" link @click="removeMappingTable(tIndex)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </div>
            
            <div class="field-mappings">
              <div class="field-header">
                <span class="col-source">源字段</span>
                <span class="col-target">目标字段</span>
                <span class="col-transform">转换类型</span>
                <span class="col-required">必填</span>
                <span class="col-action" style="width: 60px">操作</span>
              </div>
              <div v-for="(field, fIndex) in table.fieldMappings" :key="fIndex" class="field-row">
                <el-input v-model="field.sourceField" placeholder="如 orderId" class="col-source" />
                <el-input v-model="field.targetField" placeholder="如 order_no" class="col-target" />
                <el-select v-model="field.transformType" class="col-transform">
                  <el-option v-for="opt in TRANSFORM_TYPE_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
                </el-select>
                <el-checkbox v-model="field.isRequired" class="col-required" />
                <el-button type="danger" link @click="removeFieldMapping(tIndex, fIndex)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
              <el-button type="primary" link class="add-field-btn" @click="addFieldMapping(tIndex)">
                <el-icon><Plus /></el-icon>
                添加字段映射
              </el-button>
            </div>
            
            <div v-if="table.children && table.children.length > 0" class="child-tables">
              <div v-for="(child, cIndex) in table.children" :key="cIndex" class="child-table-item">
                <div class="child-header">
                  <span class="child-label">子表</span>
                  <el-select v-model="child.targetTable" placeholder="选择子表" style="width: 180px" filterable>
                    <el-option v-for="opt in TARGET_TABLE_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
                  </el-select>
                  <el-input v-model="child.dataPath" placeholder="数据路径，如 items" style="width: 140px">
                    <template #prepend>路径</template>
                  </el-input>
                  <el-input v-model="child.relationField" placeholder="关联字段" style="width: 120px">
                    <template #prepend>关联</template>
                  </el-input>
                  <el-button type="danger" link @click="removeChildTable(tIndex, cIndex)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
                <div class="field-mappings child-fields">
                  <div class="field-header">
                    <span class="col-source">源字段</span>
                    <span class="col-target">目标字段</span>
                    <span class="col-transform">转换类型</span>
                    <span class="col-required">必填</span>
                    <span class="col-action" style="width: 60px">操作</span>
                  </div>
                  <div v-for="(field, fIndex) in child.fieldMappings" :key="fIndex" class="field-row">
                    <el-input v-model="field.sourceField" placeholder="如 itemCode" class="col-source" />
                    <el-input v-model="field.targetField" placeholder="如 item_code" class="col-target" />
                    <el-select v-model="field.transformType" class="col-transform">
                      <el-option v-for="opt in TRANSFORM_TYPE_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
                    </el-select>
                    <el-checkbox v-model="field.isRequired" class="col-required" />
                    <el-button type="danger" link @click="removeChildFieldMapping(tIndex, cIndex, fIndex)">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                  <el-button type="primary" link class="add-field-btn" @click="addChildFieldMapping(tIndex, cIndex)">
                    <el-icon><Plus /></el-icon>
                    添加字段映射
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="form.mode === 'PULL'" class="form-section">
        <div class="section-title">
          <el-icon><Clock /></el-icon>
          增量同步配置
        </div>
        <div class="form-grid">
          <el-form-item label="起始时间参数">
            <el-input v-model="form.syncTimeParamStart" placeholder="如 arg0" />
          </el-form-item>
          <el-form-item label="结束时间参数">
            <el-input v-model="form.syncTimeParamEnd" placeholder="如 arg1" />
          </el-form-item>
          <el-form-item label="时间格式">
            <el-input v-model="form.syncTimeFormat" placeholder="如 yyyy-MM-dd HH:mm:ss" />
          </el-form-item>
        </div>
        <div class="form-grid">
          <el-form-item label="首次同步天数">
            <el-input-number v-model="form.firstSyncDays" :min="0" :max="365" />
          </el-form-item>
          <el-form-item label="增量同步小时数">
            <el-input-number v-model="form.incrementalSyncHours" :min="1" :max="72" />
          </el-form-item>
        </div>
        <div class="sync-hint">
          <el-icon><InfoFilled /></el-icon>
          <span>首次同步天数设为 <b>0</b> 表示拉取全部数据，否则拉取指定天数内的数据；后续每次同步只拉取 <b>{{ form.incrementalSyncHours || 24 }}</b> 小时内变更的数据</span>
        </div>
      </div>

      <div class="form-section">
        <div class="section-title">
          <el-icon><Warning /></el-icon>
          错误策略
        </div>
        <div class="form-grid-3">
          <el-form-item label="重试间隔（秒）">
            <el-input-number v-model="form.retryInterval" :min="1" :max="300" />
          </el-form-item>
          <el-form-item label="最大重试次数">
            <el-input-number v-model="form.maxRetries" :min="0" :max="10" />
          </el-form-item>
          <el-form-item label="失败处理">
            <el-select v-model="form.onFailure">
              <el-option label="停止调用" value="STOP" />
              <el-option label="跳过继续" value="SKIP" />
              <el-option label="发送告警" value="ALERT" />
            </el-select>
          </el-form-item>
        </div>
      </div>
      
      <!-- 测试同步 -->
      <div class="form-section">
        <div class="section-title">
          <el-icon><Connection /></el-icon>
          测试同步
        </div>
        <div class="test-sync-area">
          <el-button type="success" :loading="testingSync" @click="handleTestSync">
            {{ testingSync ? '测试中...' : '测试同步' }}
          </el-button>
          <div v-if="syncResult" class="sync-result">
            <div :class="['result-status', syncResult.success ? 'success' : 'error']">
              {{ syncResult.success ? '成功' : '失败' }}: {{ syncResult.message }}
            </div>
            <div v-if="syncResult.count" class="result-count">
              获取 {{ syncResult.count }} 条数据，耗时 {{ syncResult.time }}
            </div>
            <div v-if="syncResult.data && syncResult.data.length > 0" class="result-preview">
              <div class="preview-title">数据预览（前5条）：</div>
              <pre>{{ JSON.stringify(syncResult.data.slice(0, 5), null, 2) }}</pre>
            </div>
          </div>
        </div>
      </div>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Setting, Warning, Document, Connection, Delete, Plus, Clock, InfoFilled } from '@element-plus/icons-vue'
import type { InterfaceConfig, MappingTable, FieldMapping } from '@/types/interface-config'
import { SYSTEM_OPTIONS, DATA_TYPE_OPTIONS, TARGET_TABLE_OPTIONS, TRANSFORM_TYPE_OPTIONS } from '@/types/interface-config'

interface Props {
  modelValue: boolean
  config: InterfaceConfig | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = ref(props.modelValue)
const formRef = ref<FormInstance>()

const isEdit = computed(() => !!props.config?.id)

const createDefaultFieldMapping = (): FieldMapping => ({
  sourceField: '',
  targetField: '',
  transformType: 'DIRECT',
  isRequired: true,
  sortOrder: 0
})

const createDefaultMappingTable = (): MappingTable => ({
  mappingCode: '',
  mappingName: '',
  targetTable: '',
  dataPath: '',
  isArray: true,
  sortOrder: 0,
  fieldMappings: [createDefaultFieldMapping()],
  children: []
})

const defaultForm: InterfaceConfig = {
  configCode: '',
  configName: '',
  system: 'HIS',
  mode: 'PULL',
  protocol: 'HTTPS',
  apiProtocol: 'REST',
  method: 'POST',
  url: '',
  dataType: undefined,
  syncMode: 'MANUAL',
  syncSchedule: '',
  requestTemplate: '{}',
  authType: 'NONE',
  retryInterval: 5,
  maxRetries: 3,
  onFailure: 'ALERT',
  enabled: true,
  mappingTables: [],
  soapAction: '',
  soapNamespace: '',
  soapParams: [],
  syncTimeParamStart: 'arg0',
  syncTimeParamEnd: 'arg1',
  syncTimeFormat: 'yyyy-MM-dd HH:mm:ss',
  firstSyncDays: 30,
  incrementalSyncHours: 24,
  isFirstSync: true
}

const form = reactive<InterfaceConfig>({ ...defaultForm })

const isMethodDisabled = computed(() => {
  return form.apiProtocol === 'SOAP' || form.apiProtocol === 'HL7'
})

watch(
  () => form.apiProtocol,
  (newProtocol) => {
    if (newProtocol === 'SOAP' || newProtocol === 'HL7') {
      form.method = undefined
    } else if (!form.method && newProtocol) {
      form.method = 'POST'
    }
  }
)

const rules: FormRules = {
  configCode: [{ required: true, message: '请输入配置编码', trigger: 'blur' }],
  configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  system: [{ required: true, message: '请选择三方系统', trigger: 'change' }],
  protocol: [{ required: true, message: '请选择通讯协议', trigger: 'change' }],
  apiProtocol: [{ required: true, message: '请选择接口协议', trigger: 'change' }],
  url: [{ required: true, message: '请输入接口地址', trigger: 'blur' }]
}

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val && props.config) {
      const configData = JSON.parse(JSON.stringify(props.config))
      // 解析 soapParams 字符串为数组（后端返回的是 JSON 字符串）
      if (typeof configData.soapParams === 'string' && configData.soapParams) {
        try {
          configData.soapParams = JSON.parse(configData.soapParams)
        } catch {
          configData.soapParams = []
        }
      }
      Object.assign(form, configData)
    }
  }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
  if (!val) {
    resetForm()
  }
})

const resetForm = () => {
  Object.assign(form, JSON.parse(JSON.stringify(defaultForm)))
  form.soapParams = []
}

const handleClose = () => {
  visible.value = false
}

const addMappingTable = () => {
  form.mappingTables.push(createDefaultMappingTable())
}

const removeMappingTable = (index: number) => {
  form.mappingTables.splice(index, 1)
}

const addChildTable = (parentIndex: number) => {
  const parentTable = form.mappingTables[parentIndex]
  if (!parentTable) return
  if (!parentTable.children) {
    parentTable.children = []
  }
  parentTable.children.push({
    mappingCode: '',
    mappingName: '',
    targetTable: '',
    dataPath: '',
    isArray: true,
    parentMappingId: parentIndex,
    relationField: '',
    sortOrder: 0,
    fieldMappings: [createDefaultFieldMapping()]
  })
}

const removeChildTable = (parentIndex: number, childIndex: number) => {
  form.mappingTables[parentIndex]?.children?.splice(childIndex, 1)
}

const addFieldMapping = (tableIndex: number) => {
  form.mappingTables[tableIndex]?.fieldMappings.push(createDefaultFieldMapping())
}

const removeFieldMapping = (tableIndex: number, fieldIndex: number) => {
  form.mappingTables[tableIndex]?.fieldMappings.splice(fieldIndex, 1)
}

const addChildFieldMapping = (parentIndex: number, childIndex: number) => {
  const childTable = form.mappingTables[parentIndex]?.children?.[childIndex]
  if (childTable) {
    childTable.fieldMappings.push(createDefaultFieldMapping())
  }
}

const removeChildFieldMapping = (parentIndex: number, childIndex: number, fieldIndex: number) => {
  const childTable = form.mappingTables[parentIndex]?.children?.[childIndex]
  if (childTable) {
    childTable.fieldMappings.splice(fieldIndex, 1)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (form.requestTemplate?.trim()) {
          JSON.parse(form.requestTemplate)
        }
      } catch {
        ElMessage.error('入参模板 JSON 格式不正确')
        return
      }

      if (form.mappingTables.length === 0) {
        ElMessage.warning('请至少添加一个目标表映射')
        return
      }

      saving.value = true
      
      try {
        const { createInterfaceConfig, updateInterfaceConfig } = await import('@/api/interface-config')
        
        const submitData = {
          ...form,
          soapParams: form.soapParams && form.soapParams.length > 0 
            ? JSON.stringify(form.soapParams) 
            : null
        }
        
        if (isEdit.value && props.config?.id) {
          await updateInterfaceConfig(props.config.id, submitData)
          ElMessage.success('更新成功')
        } else {
          await createInterfaceConfig(submitData)
          ElMessage.success('创建成功')
        }
        
        emit('success')
        handleClose()
      } catch (error: any) {
        ElMessage.error(error.message || '保存失败')
      } finally {
        saving.value = false
      }
    }
  })
}

const saving = ref(false)

const addSoapParam = () => {
  if (!form.soapParams) form.soapParams = []
  form.soapParams.push({ name: '', value: '' })
}

const testingSync = ref(false)
const syncResult = ref<any>(null)

const handleTestSync = async () => {
  if (!form.url) {
    ElMessage.warning('请输入接口地址')
    return
  }
  
  if (form.apiProtocol === 'SOAP' && !form.soapAction) {
    ElMessage.warning('请输入SOAP Action')
    return
  }
  
  testingSync.value = true
  syncResult.value = null
  
  try {
    const requestBody = {
      ...form,
      soapParams: form.soapParams ? JSON.stringify(form.soapParams) : '[]'
    }
    
    const response = await fetch('/api/interface-configs/test-sync', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestBody)
    })
    
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      const data = result.data
      syncResult.value = {
        success: data.success,
        message: data.message,
        count: data.dataCount,
        time: `${data.responseTime}ms`,
        data: data.dataPreview || []
      }
      
      if (data.success) {
        ElMessage.success(data.message)
      } else {
        ElMessage.error(data.message)
      }
    } else {
      syncResult.value = {
        success: false,
        message: result.message || '测试失败'
      }
      ElMessage.error(result.message || '测试失败')
    }
  } catch (error: any) {
    syncResult.value = {
      success: false,
      message: error.message || '测试失败'
    }
    ElMessage.error(error.message || '测试失败')
  } finally {
    testingSync.value = false
  }
}
</script>

<style>
.config-dialog .el-dialog__body {
  padding: 16px 24px;
  max-height: 70vh;
  overflow-y: auto;
}

.config-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-section {
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  padding: 16px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title .el-icon {
  color: var(--color-primary-DEFAULT);
}

.section-hint {
  font-size: 12px;
  font-weight: 400;
  color: var(--text-secondary);
  margin-left: 8px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.form-grid-3 {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.form-grid .el-form-item,
.form-grid-3 .el-form-item {
  margin-bottom: 0;
}

.form-grid .el-select,
.form-grid-3 .el-select,
.form-grid .el-input-number {
  width: 100%;
}

.code-input .el-textarea__inner {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  background: #1e1e1e;
  color: #d4d4d4;
  border: none;
  border-radius: var(--radius-sm);
}

.mapping-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.mapping-table-item {
  background: var(--bg-primary);
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-light);
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-light);
}

.table-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.table-actions {
  display: flex;
  gap: 8px;
}

.field-mappings {
  padding: 12px;
}

.field-header {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid var(--border-light);
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.field-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  border-bottom: 1px solid var(--border-light);
}

.field-row:last-of-type {
  border-bottom: none;
}

.col-source {
  flex: 1;
}

.col-target {
  flex: 1;
}

.col-transform {
  width: 120px;
}

.col-required {
  width: 50px;
  display: flex;
  justify-content: center;
}

.col-action {
  width: 60px;
  display: flex;
  justify-content: center;
}

.add-field-btn {
  margin-top: 8px;
}

.child-tables {
  padding: 0 12px 12px 12px;
}

.child-table-item {
  background: #fafafa;
  border-radius: var(--radius-sm);
  border: 1px dashed var(--border-light);
  margin-top: 12px;
}

.child-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  border-bottom: 1px dashed var(--border-light);
}

.child-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-primary-DEFAULT);
  padding: 2px 8px;
  background: var(--color-primary-50);
  border-radius: var(--radius-sm);
}

.child-fields {
  background: transparent;
}

.soap-config {
  margin-top: 16px;
  padding: 12px;
  background: #fafafa;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-light);
}

.soap-params {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.param-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.test-sync-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sync-hint {
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

.sync-hint .el-icon {
  color: var(--color-primary-DEFAULT);
}

.sync-result {
  padding: 12px;
  background: var(--bg-primary);
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-light);
}

.result-status {
  font-weight: 500;
  margin-bottom: 8px;
}

.result-status.success {
  color: #67C23A;
}

.result-status.error {
  color: #F56C6C;
}

.result-count {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.result-preview {
  max-height: 200px;
  overflow: auto;
}

.preview-title {
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 8px;
}

.result-preview pre {
  margin: 0;
  padding: 8px;
  background: #1e1e1e;
  color: #d4d4d4;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-family: 'Monaco', 'Menlo', monospace;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>