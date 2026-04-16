<template>
  <div class="scheduling-card">
    <div class="card-header">
      <div class="month-title">{{ yearMonth }} 月排班</div>
      <div class="department-name">{{ departmentName }}</div>
    </div>
    
    <div class="card-body">
      <div v-for="week in weeks" :key="week.weekNum" class="week-row">
        <div class="week-header">
          <span class="week-label">第{{ week.weekNum }}周</span>
          <span class="week-range">{{ week.startDate }}-{{ week.endDate }}</span>
        </div>
        <div class="days-row">
          <div 
            v-for="day in week.days" 
            :key="day.date"
            class="day-cell"
            :class="{ 
              'weekend': day.isWeekend,
              'empty': !day.staffName,
              'editable': editable
            }"
            @click="editable && handleDayClick(day)"
          >
            <div class="day-number">{{ day.day }}</div>
            <div class="day-name">{{ day.dayName }}</div>
            <div class="staff-name">
              <span v-if="day.staffName">{{ day.staffName }}</span>
              <span v-else class="placeholder">待安排</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface SchedulingDetail {
  dutyDate: string
  staffId: number | null
  staffName: string | null
  remark: string
}

interface Props {
  yearMonth: string
  departmentName: string
  details: SchedulingDetail[]
  editable?: boolean
}

interface Emits {
  (e: 'day-click', day: { date: string; staffId: number | null; staffName: string | null }): void
}

const props = withDefaults(defineProps<Props>(), {
  editable: false
})
const emit = defineEmits<Emits>()

const dayNames = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

interface DayInfo {
  date: string
  day: number
  dayName: string
  isWeekend: boolean
  staffId: number | null
  staffName: string | null
}

interface WeekInfo {
  weekNum: number
  startDate: number
  endDate: number
  days: DayInfo[]
}

const weeks = computed(() => {
  const [year, month] = props.yearMonth.split('-')
  const daysInMonth = new Date(parseInt(year), parseInt(month), 0).getDate()
  const weeksData: WeekInfo[] = []
  
  let currentWeek = 1
  let weekStart = 1
  
  while (weekStart <= daysInMonth) {
    const days: DayInfo[] = []
    let weekEnd = Math.min(weekStart + 6, daysInMonth)
    
    for (let d = weekStart; d <= weekEnd; d++) {
      const date = `${props.yearMonth}-${d.toString().padStart(2, '0')}`
      const dateObj = new Date(parseInt(year), parseInt(month) - 1, d)
      const dayOfWeek = dateObj.getDay()
      const adjustedDayOfWeek = dayOfWeek === 0 ? 6 : dayOfWeek - 1
      
      const detail = props.details.find(item => item.dutyDate === date)
      
      days.push({
        date,
        day: d,
        dayName: dayNames[adjustedDayOfWeek],
        isWeekend: adjustedDayOfWeek >= 5,
        staffId: detail?.staffId ?? null,
        staffName: detail?.staffName ?? null
      })
    }
    
    weeksData.push({
      weekNum: currentWeek,
      startDate: weekStart,
      endDate: weekEnd,
      days
    })
    
    currentWeek++
    weekStart = weekEnd + 1
  }
  
  return weeksData
})

const handleDayClick = (day: DayInfo) => {
  emit('day-click', {
    date: day.date,
    staffId: day.staffId,
    staffName: day.staffName
  })
}
</script>

<style scoped>
.scheduling-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 16px;
  min-width: 280px;
}

.card-header {
  text-align: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
  margin-bottom: 12px;
}

.month-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.department-name {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.week-row {
  margin-bottom: 12px;
}

.week-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
  font-size: 12px;
  color: #909399;
}

.week-label {
  font-weight: 500;
}

.week-range {
  color: #c0c4cc;
}

.days-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.day-cell {
  background: #f5f7fa;
  border-radius: 4px;
  padding: 8px 4px;
  text-align: center;
  transition: all 0.2s;
}

.day-cell.weekend {
  background: #fef0f0;
}

.day-cell.empty {
  opacity: 0.7;
}

.day-cell.editable {
  cursor: pointer;
}

.day-cell.editable:hover {
  background: #ecf5ff;
  transform: translateY(-1px);
}

.day-number {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.day-name {
  font-size: 10px;
  color: #909399;
  margin-top: 2px;
}

.staff-name {
  font-size: 12px;
  color: var(--color-primary-DEFAULT);
  margin-top: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.staff-name .placeholder {
  color: #c0c4cc;
  font-style: italic;
}
</style>