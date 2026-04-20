<template>
  <div class="print-wrap">
    <div class="toolbar">
      <el-select v-model="m1" placeholder="第一月份" style="width:150px" filterable>
        <el-option v-for="i in list" :key="i.id" :label="ym(i.yearMonth)" :value="i.id" />
      </el-select>
      <el-select v-model="m2" placeholder="第二月份" style="width:150px" clearable filterable>
        <el-option v-for="i in list" :key="i.id" :label="ym(i.yearMonth)" :value="i.id" />
      </el-select>
      <el-button type="primary" @click="print" :disabled="!m1">打印</el-button>
      <el-button @click="$emit('close')">关闭</el-button>
    </div>
    <div class="preview">
      <div class="paper" id="printArea">
        <div class="sec">
          <div class="hd">{{ sortedFirst?.yearMonth?ym(sortedFirst.yearMonth):'' }} {{ sortedFirst?.departmentName||'' }}</div>
          <table v-if="sortedW1.length">
            <thead><tr><th>周一</th><th>周二</th><th>周三</th><th>周四</th><th>周五</th><th class="wk">周六</th><th class="wk">周日</th></tr></thead>
            <tbody>
              <tr v-for="(w,i) in sortedW1" :key="i">
                <td v-for="(d,j) in w" :key="j" :class="{wk:d.isWeekend}">
                  <template v-if="d.day"><b>{{ d.day }}</b><br><small>{{ d.staffName||'-' }}</small></template>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <hr v-if="sortedSecond" />
        <div class="sec" v-if="sortedSecond">
          <div class="hd">{{ sortedSecond?.yearMonth?ym(sortedSecond.yearMonth):'' }} {{ sortedSecond?.departmentName||'' }}</div>
          <table v-if="sortedW2.length">
            <thead><tr><th>周一</th><th>周二</th><th>周三</th><th>周四</th><th>周五</th><th class="wk">周六</th><th class="wk">周日</th></tr></thead>
            <tbody>
              <tr v-for="(w,i) in sortedW2" :key="i">
                <td v-for="(d,j) in w" :key="j" :class="{wk:d.isWeekend}">
                  <template v-if="d.day"><b>{{ d.day }}</b><br><small>{{ d.staffName||'-' }}</small></template>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchSchedulingDetail } from '@/api/scheduling'

const props = defineProps<{ list: any[], defaultMonth?: string }>()
defineEmits(['close'])

const m1 = ref<number|null>(null)
const m2 = ref<number|null>(null)
const d1 = ref<any>(null)
const d2 = ref<any>(null)

const ym = (s:string) => s ? `${s.split('-')[0]}年${parseInt(s.split('-')[1])}月` : ''

const weeks = (ym:string, det:any[]) => {
  if(!ym||!det) return []
  const [y,m] = ym.split('-').map(Number), tot = new Date(y,m,0).getDate()
  const off = new Date(y,m-1,1).getDay()
  const fst = off===0?6:off-1
  const res:any[] = []
  let d=1
  while(d<=tot){
    const w:any[] = []
    for(let i=0;i<7;i++){
      if(res.length===0&&i<fst) w.push({day:null,isWeekend:i>=5,staffName:null})
      else if(d<=tot){
        const ds = `${ym}-${String(d).padStart(2,'0')}`
        const dt = det.find(x=>x.dutyDate===ds)
        w.push({day:d,isWeekend:i>=5,staffName:dt?.staffName||null})
        d++
      }else w.push({day:null,isWeekend:i>=5,staffName:null})
    }
    res.push(w)
  }
  return res
}

const w1 = computed(()=> d1.value?weeks(d1.value.yearMonth,d1.value.details||[]):[])
const w2 = computed(()=> d2.value?weeks(d2.value.yearMonth,d2.value.details||[]):[])

const sortedFirst = computed(() => {
  if (!d1.value) return null
  if (!d2.value) return d1.value
  return d1.value.yearMonth < d2.value.yearMonth ? d1.value : d2.value
})

const sortedSecond = computed(() => {
  if (!d1.value || !d2.value) return null
  return d1.value.yearMonth < d2.value.yearMonth ? d2.value : d1.value
})

const sortedW1 = computed(() => sortedFirst.value ? weeks(sortedFirst.value.yearMonth, sortedFirst.value.details || []) : [])
const sortedW2 = computed(() => sortedSecond.value ? weeks(sortedSecond.value.yearMonth, sortedSecond.value.details || []) : [])

const load = async(id:number) => {
  const r = await fetchSchedulingDetail(id)
  return r.code===0?r.data:null
}

watch(m1, async v => { d1.value = v?await load(v):null })
watch(m2, async v => { d2.value = v?await load(v):null })

onMounted(async()=>{
  const df = props.list?.find(s=>s.yearMonth===props.defaultMonth)
  if(df){
    m1.value = df.id
    const idx = props.list.findIndex(s=>s.id===df.id)
    if(idx>=0&&idx+1<props.list.length) m2.value = props.list[idx+1].id
  }else if(props.list?.length){
    m1.value = props.list[0].id
    if(props.list.length>1) m2.value = props.list[1].id
  }
  if(m1.value) d1.value = await load(m1.value)
  if(m2.value) d2.value = await load(m2.value)
})

const print = () => {
  if(!m1.value) return ElMessage.warning('请选择月份')
  window.print()
}
</script>

<style scoped>
.print-wrap{
  position:fixed;
  inset:0;
  background:#f5f5f5;
  z-index:9999;
  display:flex;
  flex-direction:column;
}
.toolbar{
  padding:10px;
  background:#fff;
  display:flex;
  gap:8px;
  justify-content:center;
}
@media print{.toolbar{display:none}}
.preview{
  flex:1;
  display:flex;
  justify-content:center;
  padding:10px;
  overflow:auto;
}
@media print{.preview{padding:0;overflow:visible}}
.paper{
  width:190mm;
  background:#fff;
  padding:5mm;
}
@media print{.paper{width:100%;padding:0 5mm;box-shadow:none}}
.sec{margin-bottom:2mm}
.hd{text-align:center;font:bold 13pt;padding:2mm 0;border-bottom:1px solid #000}
@media print{.hd{font-size:13pt;padding:2.5mm 0}}
hr{margin:3mm 0;border:none;border-top:1px solid #000}
table{width:100%;border-collapse:collapse;font-size:10pt}
@media print{table{font-size:11pt}}
th,td{border:1px solid #000;padding:2.5mm 2mm;text-align:center}
@media print{th,td{padding:3mm 2mm}}
th{font-size:10pt;background:#f0f0f0;font-weight:bold}
@media print{th{font-size:11pt}}
.wk{background:#f8f8f8}
b{font-size:11pt}
@media print{b{font-size:12pt;font-weight:bold}}
small{font-size:10pt}
@media print{small{font-size:11pt}}
</style>

<style>
/* 全局打印样式 */
@media print {
  /* 打印组件在 body 外（通过 Teleport），隐藏 #app 即可 */
  #app { display: none !important; }
  
  /* 打印组件调整 */
  .print-wrap {
    position: static !important;
    background: #fff !important;
    z-index: auto !important;
    inset: auto !important;
    display: block !important;
    width: 100% !important;
    height: auto !important;
  }
  
  .toolbar { display: none !important; }
  
  .preview {
    padding: 0 !important;
    overflow: visible !important;
    height: auto !important;
    flex: none !important;
    display: block !important;
  }
  
  .paper {
    box-shadow: none !important;
    width: 100% !important;
    padding: 5mm !important;
  }
  
  .sec {
    margin-bottom: 5mm !important;
  }
}

@page {
  size: A4 portrait;
  margin: 10mm;
}
</style>