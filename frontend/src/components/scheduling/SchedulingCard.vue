<template>
  <div class="scheduling-card">
    <div class="card-header">
      <div class="month-title">{{ formatYearMonth(yearMonth) }} {{ departmentName }}</div>
    </div>
    
    <table class="calendar-table">
      <thead>
        <tr>
          <th>周一</th>
          <th>周二</th>
          <th>周三</th>
          <th>周四</th>
          <th>周五</th>
          <th class="weekend-header">周六</th>
          <th class="weekend-header">周日</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(week, weekIndex) in calendarWeeks" :key="weekIndex">
          <td 
            v-for="(day, dayIndex) in week" 
            :key="dayIndex"
            :class="{ 
              'weekend': day.isWeekend,
              'empty': !day.day,
              'my-duty': day.isMyDuty,
              'editable': editable && day.day
            }"
            @click="editable && day.day && handleDayClick(day)"
          >
            <template v-if="day.day">
              <div class="day-number">{{ day.day }}</div>
              <div class="staff-name">
                <span v-if="day.staffName">{{ day.staffName }}</span>
                <span v-else class="placeholder">-</span>
              </div>
            </template>
          </td>
        </tr>
      </tbody>
    </table>
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
  currentStaffId?: number | null
}

interface Emits {
  (e: 'day-click', day: { date: string; staffId: number | null; staffName: string | null }): void
}

const props = withDefaults(defineProps<Props>(), {
  editable: false,
  currentStaffId: null
})
const emit = defineEmits<Emits>()

interface CalendarDay {
  date: string | null
  day: number | null
  isWeekend: boolean
  staffId: number | null
  staffName: string | null
  isMyDuty: boolean
}

const formatYearMonth = (ym: string) => {
  if (!ym) return ''
  const [year, month] = ym.split('-')
  return `${year}年${parseInt(month)}月`
}

// 周历计算逻辑，复用 SchedulingPrint.vue 的 weeks 函数
const calendarWeeks = computed(() => {
  if (!props.yearMonth) return []
  
  const [year, month] = props.yearMonth.split('-').map(Number)
  const totalDays = new Date(year, month, 0).getDate()
  const firstDayOffset = new Date(year, month - 1, 1).getDay()
  const firstDayIndex = firstDayOffset === 0 ? 6 : firstDayOffset - 1
  
  const result: CalendarDay[][] = []
  let currentDay = 1
  
  while (currentDay <= totalDays) {
    const week: CalendarDay[] = []
    
    for (let i = 0; i < 7; i++) {
      // 第一周的前几天可能是空的
      if (result.length === 0 && i < firstDayIndex) {
        week.push({
          date: null,
          day: null,
          isWeekend: i >= 5,
          staffId: null,
          staffName: null,
          isMyDuty: false
        })
      } else if (currentDay <= totalDays) {
        const dateStr = `${props.yearMonth}-${String(currentDay).padStart(2, '0')}`
        const detail = props.details.find(d => d.dutyDate === dateStr)
        
        week.push({
          date: dateStr,
          day: currentDay,
          isWeekend: i >= 5,
          staffId: detail?.staffId ?? null,
          staffName: detail?.staffName ?? null,
          isMyDuty: detail?.staffId !== null && detail?.staffId === props.currentStaffId
        })
        currentDay++
      } else {
        // 月末最后几天可能是空的
        week.push({
          date: null,
          day: null,
          isWeekend: i >= 5,
          staffId: null,
          staffName: null,
          isMyDuty: false
        })
      }
    }
    
    result.push(week)
  }
  
  return result
})

const handleDayClick = (day: CalendarDay) => {
  if (!day.date) return
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
  padding: 20px;
  width: 100%;
}

.card-header {
  text-align: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
  margin-bottom: 16px;
}

.month-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.calendar-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.calendar-table th {
  padding: 12px 8px;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
}

.calendar-table th.weekend-header {
  background: #fef0f0;
  color: #f56c6c;
}

.calendar-table td {
  padding: 16px 8px;
  text-align: center;
  border: 1px solid #ebeef5;
  min-height: 60px;
  vertical-align: middle;
  transition: all 0.2s;
}

.calendar-table td.weekend {
  background: #fef9f9;
}

.calendar-table td.empty {
  background: #fafafa;
}

.calendar-table td.editable {
  cursor: pointer;
}

.calendar-table td.editable:hover {
  background: #ecf5ff;
}

.calendar-table td.my-duty {
  background: var(--color-primary-100);
  border: 2px solid var(--color-primary-DEFAULT);
  box-shadow: inset 0 0 8px rgba(255, 179, 102, 0.3);
}

.calendar-table td.my-duty .day-number {
  color: var(--color-primary-dark);
  font-weight: 700;
}

.calendar-table td.my-duty .staff-name {
  font-weight: 600;
  color: var(--color-primary-dark);
}

.day-number {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.staff-name {
  font-size: 14px;
  color: var(--color-primary-DEFAULT);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.staff-name .placeholder {
  color: #c0c4cc;
}
</style>
