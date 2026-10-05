<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const tasks = ref([])
const maternals = ref([])
const stats = ref({ completedCount: 0, totalCount: 0, completionRate: 0 })
const dialogVisible = ref(false)
const careTypes = ['伤口护理', '乳房护理', '新生儿护理', '产后恢复', '心理疏导']
const today = localDate()
const form = ref(emptyForm())

function localDate() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function emptyForm() {
  return { maternalId: '', nurse: '', careType: '伤口护理', planDate: today }
}

const residents = computed(() => maternals.value.filter(item => item.status === '在住'))
const maternalMap = computed(() => Object.fromEntries(maternals.value.map(item => [item.id, item.name])))

async function load() {
  try {
    const [taskData, statData, maternalData] = await Promise.all([
      api.listCareTasks(today), api.getCareCompletionRate(today), api.listMaternals()
    ])
    tasks.value = taskData
    stats.value = statData
    maternals.value = maternalData
  } catch (error) {
    ElMessage.error(error.message)
  }
}

function openCreate() {
  form.value = emptyForm()
  dialogVisible.value = true
}

async function save() {
  try {
    await api.createCareTask({ ...form.value, maternalId: Number(form.value.maternalId) })
    ElMessage.success('护理任务已安排')
    dialogVisible.value = false
    await load()
    emit('changed')
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function complete(task) {
  try {
    await api.completeCareTask(task.id)
    ElMessage.success('护理任务打卡完成')
    await load()
    emit('changed')
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function skip(task) {
  try {
    await api.skipCareTask(task.id)
    ElMessage.success('护理任务已跳过')
    await load()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

function formatTime(value) {
  return value ? value.replace('T', ' ').slice(0, 16) : '尚未打卡'
}

const emit = defineEmits(['changed'])
onMounted(load)
</script>

<template>
  <section>
    <div class="page-head">
      <div>
        <h2>今日护理打卡</h2>
        <p>{{ today }} · 勾选完成护理任务，进度环将实时更新。</p>
      </div>
      <el-button type="primary" @click="openCreate">安排护理</el-button>
    </div>

    <div class="care-layout">
      <aside class="progress-panel page-card">
        <span class="eyebrow">今日完成率</span>
        <el-progress
          type="circle"
          :percentage="stats.completionRate || 0"
          :width="162"
          :stroke-width="13"
          color="#E91E63"
        />
        <strong>{{ stats.completedCount }} / {{ stats.totalCount }} 项</strong>
        <p>每次完成打卡后自动统计</p>
      </aside>

      <div class="checklist page-card">
        <div v-if="tasks.length === 0" class="empty-state">今日暂无护理任务</div>
        <article v-for="task in tasks" :key="task.id" class="task" :class="task.status">
          <el-checkbox :model-value="task.status === '已完成'" disabled />
          <div class="task-main">
            <div class="task-title">
              <strong>{{ task.careType }}</strong>
              <el-tag v-if="task.status === '已跳过'" type="info" size="small">已跳过</el-tag>
              <el-tag v-else-if="task.status === '已完成'" type="success" size="small">已完成</el-tag>
              <el-tag v-else type="warning" size="small">待执行</el-tag>
            </div>
            <p>{{ maternalMap[task.maternalId] || `产妇 #${task.maternalId}` }} · 责任护士：{{ task.nurse }}</p>
            <small>{{ formatTime(task.finishTime) }}</small>
          </div>
          <div class="task-actions">
            <el-button v-if="task.status === '待执行'" type="primary" @click="complete(task)">完成打卡</el-button>
            <el-button v-if="task.status === '待执行'" plain @click="skip(task)">跳过</el-button>
          </div>
        </article>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" title="安排今日护理" width="500px">
      <el-form :model="form" label-width="88px">
        <el-form-item label="在住产妇">
          <el-select v-model="form.maternalId" style="width: 100%" placeholder="选择产妇">
            <el-option v-for="item in residents" :key="item.id" :label="`${item.name} · 房间 #${item.roomId}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="责任护士"><el-input v-model="form.nurse" placeholder="输入护士姓名" /></el-form-item>
        <el-form-item label="护理类型">
          <el-select v-model="form.careType" style="width: 100%">
            <el-option v-for="item in careTypes" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划日期"><el-date-picker v-model="form.planDate" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确认安排</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.care-layout { display: grid; grid-template-columns: 235px minmax(0, 1fr); gap: 18px; align-items: start; }
.progress-panel { padding: 25px 18px; display: flex; flex-direction: column; align-items: center; gap: 16px; }
.progress-panel .eyebrow { color: var(--muted); font-size: 13px; }
.progress-panel strong { color: var(--primary-deep); font-size: 20px; }
.progress-panel p { margin: 0; color: var(--muted); font-size: 12px; }
.checklist { padding: 8px 18px; }
.task { min-height: 96px; display: flex; align-items: center; gap: 10px; padding: 15px 4px; border-bottom: 1px solid var(--line); }
.task:last-child { border-bottom: 0; }
.task.已完成 .task-main { opacity: .65; }
.task.已完成 .task-title strong { text-decoration: line-through; }
.task-main { min-width: 0; flex: 1; }
.task-title { display: flex; align-items: center; gap: 9px; }
.task-title strong { font-size: 16px; }
.task-main p { margin: 7px 0 4px; color: var(--muted); }
.task-main small { color: #A88A96; }
.task-actions { display: flex; gap: 7px; }
.empty-state { padding: 50px 0; text-align: center; color: var(--muted); }
@media (max-width: 760px) {
  .care-layout { grid-template-columns: 1fr; }
  .task { align-items: flex-start; flex-wrap: wrap; }
  .task-actions { width: 100%; justify-content: flex-end; }
}
</style>
