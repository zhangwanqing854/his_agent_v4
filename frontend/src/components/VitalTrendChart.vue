<script setup lang="ts">
import { computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { VitalRecord } from '@/types/patient'

use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

interface Props {
  vitalsHistory: VitalRecord[]
  metric: 'temperature' | 'pulse' | 'bloodPressure' | 'oxygen'
}

const props = defineProps<Props>()

const hasData = computed(() => props.vitalsHistory && props.vitalsHistory.length >= 2)

const chartOption = computed(() => {
  if (!hasData.value) return null
  
  const times = props.vitalsHistory.map(v => v.time.slice(5, 16))
  
  if (props.metric === 'bloodPressure') {
    return {
      tooltip: { trigger: 'axis' },
      legend: { data: ['收缩压', '舒张压'], top: 0 },
      xAxis: { type: 'category', data: times, axisLabel: { fontSize: 10 } },
      yAxis: { type: 'value', name: 'mmHg' },
      grid: { left: '10%', right: '5%', bottom: '15%', top: '20%', containLabel: true },
      series: [
        {
          name: '收缩压',
          type: 'line',
          data: props.vitalsHistory.map(v => v.systolicBp),
          smooth: true,
          lineStyle: { color: '#F56C6C', width: 2 },
          itemStyle: { color: '#F56C6C' }
        },
        {
          name: '舒张压',
          type: 'line',
          data: props.vitalsHistory.map(v => v.diastolicBp),
          smooth: true,
          lineStyle: { color: '#67C23A', width: 2 },
          itemStyle: { color: '#67C23A' }
        }
      ]
    }
  }
  
  const metricConfigs = {
    temperature: { name: '体温(°C)', data: props.vitalsHistory.map(v => v.temperature), color: '#FFB366' },
    pulse: { name: '脉搏(次/分)', data: props.vitalsHistory.map(v => v.pulse), color: '#409EFF' },
    oxygen: { name: '血氧(%)', data: props.vitalsHistory.map(v => v.oxygenSaturation), color: '#E6A23C' }
  }
  
  const config = metricConfigs[props.metric]
  
  return {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: times, axisLabel: { fontSize: 10 } },
    yAxis: { type: 'value', name: config.name.split('(')[0] },
    grid: { left: '10%', right: '5%', bottom: '15%', top: '10%', containLabel: true },
    series: [
      {
        name: config.name,
        type: 'line',
        data: config.data,
        smooth: true,
        lineStyle: { color: config.color, width: 2 },
        itemStyle: { color: config.color },
        areaStyle: { color: config.color, opacity: 0.1 }
      }
    ]
  }
})
</script>

<template>
  <div v-if="hasData && chartOption">
    <v-chart :option="chartOption" style="height: 200px" autoresize />
  </div>
  <div v-else class="empty-chart">
    <el-empty description="暂无足够数据生成趋势图" :image-size="80" />
  </div>
</template>

<style scoped>
.empty-chart {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}
</style>