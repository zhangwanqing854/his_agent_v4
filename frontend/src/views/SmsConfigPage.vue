<template>
  <div class="sms-config-page">
    <div class="page-header">
      <h1>短信配置</h1>
    </div>

    <div class="config-content">
      <el-card>
        <div class="config-section">
          <div class="config-item">
            <span class="config-label">启用短信通知</span>
            <el-switch v-model="config.enabled" />
          </div>

          <div class="config-item">
            <span class="config-label">短信平台</span>
            <el-radio-group v-model="config.provider">
              <el-radio value="aliyun">阿里云</el-radio>
              <el-radio value="tencent">腾讯云</el-radio>
            </el-radio-group>
          </div>
        </div>

        <el-divider />

        <div v-if="config.provider === 'aliyun'" class="platform-config">
          <h3>阿里云配置</h3>
          
          <div class="config-item">
            <span class="config-label">AccessKey ID</span>
            <div class="input-with-toggle">
              <el-input
                v-if="showSecret.accessKeyId"
                v-model="config.aliyunAccessKeyId"
                placeholder="请输入 AccessKey ID"
              />
              <el-input
                v-else
                :model-value="maskValue(config.aliyunAccessKeyId)"
                disabled
              />
              <el-button link @click="showSecret.accessKeyId = !showSecret.accessKeyId">
                {{ showSecret.accessKeyId ? '隐藏' : '显示' }}
              </el-button>
            </div>
          </div>

          <div class="config-item">
            <span class="config-label">AccessKey Secret</span>
            <div class="input-with-toggle">
              <el-input
                v-if="showSecret.accessKeySecret"
                v-model="config.aliyunAccessKeySecret"
                type="password"
                placeholder="请输入 AccessKey Secret"
              />
              <el-input
                v-else
                :model-value="maskValue(config.aliyunAccessKeySecret)"
                disabled
              />
              <el-button link @click="showSecret.accessKeySecret = !showSecret.accessKeySecret">
                {{ showSecret.accessKeySecret ? '隐藏' : '显示' }}
              </el-button>
            </div>
          </div>

          <div class="config-item">
            <span class="config-label">短信签名</span>
            <el-input v-model="config.aliyunSignName" placeholder="请输入短信签名" />
          </div>

          <div class="config-item">
            <span class="config-label">模板 Code</span>
            <el-input v-model="config.aliyunTemplateCode" placeholder="请输入短信模板Code" />
          </div>
        </div>

        <div class="action-buttons">
          <el-button type="primary" :loading="saving" @click="handleSave">
            保存配置
          </el-button>
          <el-button @click="showTestDialog = true">
            测试发送
          </el-button>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="showTestDialog" title="测试发送短信" width="400px">
      <el-form>
        <el-form-item label="测试手机号">
          <el-input v-model="testPhone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTestDialog = false">取消</el-button>
        <el-button type="primary" :loading="testing" @click="handleTestSend">
          发送
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSmsConfig, updateSmsConfig, testSmsSend, type SmsConfigResponse } from '@/api/smsConfig'

const saving = ref(false)
const testing = ref(false)
const showTestDialog = ref(false)
const testPhone = ref('')

const showSecret = reactive({
  accessKeyId: false,
  accessKeySecret: false
})

const config = reactive({
  enabled: false,
  provider: 'aliyun',
  aliyunAccessKeyId: '',
  aliyunAccessKeySecret: '',
  aliyunSignName: '北京大学国际医院',
  aliyunTemplateCode: ''
})

onMounted(async () => {
  await loadConfig()
})

const loadConfig = async () => {
  try {
    const res = await getSmsConfig()
    if (res.code === 0 && res.data) {
      config.enabled = res.data.enabled
      config.provider = res.data.provider
      
      for (const c of res.data.configs || []) {
        if (c.key === 'aliyun_access_key_id') {
          config.aliyunAccessKeyId = c.value || ''
        } else if (c.key === 'aliyun_access_key_secret') {
          config.aliyunAccessKeySecret = c.value || ''
        } else if (c.key === 'aliyun_sign_name') {
          config.aliyunSignName = c.value || ''
        } else if (c.key === 'aliyun_template_code') {
          config.aliyunTemplateCode = c.value || ''
        }
      }
    }
  } catch (error) {
    ElMessage.error('加载配置失败')
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    const res = await updateSmsConfig({
      enabled: config.enabled,
      provider: config.provider,
      aliyunAccessKeyId: config.aliyunAccessKeyId,
      aliyunAccessKeySecret: config.aliyunAccessKeySecret,
      aliyunSignName: config.aliyunSignName,
      aliyunTemplateCode: config.aliyunTemplateCode
    })
    if (res.code === 0) {
      ElMessage.success('配置保存成功')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleTestSend = async () => {
  if (!testPhone.value) {
    ElMessage.warning('请输入测试手机号')
    return
  }
  
  testing.value = true
  try {
    const res = await testSmsSend(testPhone.value)
    if (res.code === 0) {
      ElMessage.success('测试短信发送成功')
      showTestDialog.value = false
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (error) {
    ElMessage.error('发送失败')
  } finally {
    testing.value = false
  }
}

const maskValue = (value: string) => {
  if (!value || value.length <= 8) {
    return value ? value.substring(0, 4) + '****' : ''
  }
  return value.substring(0, 4) + '****' + value.substring(value.length - 2)
}
</script>

<style scoped>
.sms-config-page {
  padding: 24px;
  background: var(--bg-secondary);
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
}

.config-content {
  max-width: 800px;
}

.config-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.config-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.config-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  min-width: 120px;
}

.input-with-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.input-with-toggle .el-input {
  flex: 1;
}

.platform-config {
  margin-top: 16px;
}

.platform-config h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}
</style>