<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const maternals = ref([])
const rooms = ref([])
const dialogVisible = ref(false)
const form = ref(emptyForm())

function dateText(date = new Date()) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function emptyForm() {
  return { name: '', phone: '', roomId: '', checkInDate: dateText(), dueDate: '', babyBirthDate: '' }
}

const roomMap = computed(() => Object.fromEntries(rooms.value.map(room => [room.id, room])))

async function load() {
  try {
    const [maternalData, roomData] = await Promise.all([api.listMaternals(), api.listRooms()])
    maternals.value = maternalData
    rooms.value = roomData
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
    const payload = {
      ...form.value,
      roomId: Number(form.value.roomId),
      babyBirthDate: form.value.babyBirthDate || null
    }
    await api.createMaternal(payload)
    ElMessage.success('产妇档案已登记')
    dialogVisible.value = false
    await load()
    emit('changed')
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function runAction(row, action, text) {
  try {
    await ElMessageBox.confirm(`确认${text}“${row.name}”吗？`, text, { type: 'warning' })
    await action(row.id)
    ElMessage.success(`${text}成功`)
    await load()
    emit('changed')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(error.message)
  }
}

function statusType(status) {
  return { 待入住: 'warning', 在住: 'success', 已出院: 'info', 退住: 'danger' }[status] || ''
}

function roomText(roomId) {
  const room = roomMap.value[roomId]
  return room ? `${room.roomNo} · ${room.type}` : `#${roomId}`
}

const emit = defineEmits(['changed'])
onMounted(load)
</script>

<template>
  <section>
    <div class="page-head">
      <div>
        <h2>产妇档案</h2>
        <p>办理登记、入住、出院和退住，终态档案不可再变更。</p>
      </div>
      <el-button type="primary" @click="openCreate">登记产妇</el-button>
    </div>

    <div class="page-card table-wrap">
      <el-table :data="maternals">
        <el-table-column prop="name" label="姓名" min-width="90" />
        <el-table-column prop="phone" label="联系电话" min-width="135" />
        <el-table-column label="房间" min-width="130">
          <template #default="{ row }">{{ roomText(row.roomId) }}</template>
        </el-table-column>
        <el-table-column prop="checkInDate" label="入住日期" min-width="115" />
        <el-table-column prop="dueDate" label="预产期" min-width="115" />
        <el-table-column prop="babyBirthDate" label="分娩日期" min-width="115">
          <template #default="{ row }">{{ row.babyBirthDate || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }"><el-tag :type="statusType(row.status)">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="办理" min-width="205" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === '待入住'" link type="primary" @click="runAction(row, api.checkInMaternal, '办理入住')">入住</el-button>
            <el-button v-if="row.status === '在住'" link type="success" @click="runAction(row, api.dischargeMaternal, '办理出院')">出院</el-button>
            <el-button v-if="!['已出院', '退住'].includes(row.status)" link type="danger" @click="runAction(row, api.withdrawMaternal, '办理退住')">退住</el-button>
            <span v-if="['已出院', '退住'].includes(row.status)" class="terminal">已归档</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" title="登记产妇" width="520px">
      <el-form :model="form" label-width="88px">
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="安排房间">
          <el-select v-model="form.roomId" style="width: 100%" placeholder="选择房间">
            <el-option v-for="room in rooms" :key="room.id" :label="`${room.roomNo} · ${room.type} · ${room.status}`" :value="room.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="入住日期"><el-date-picker v-model="form.checkInDate" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="预产期"><el-date-picker v-model="form.dueDate" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="分娩日期"><el-date-picker v-model="form.babyBirthDate" value-format="YYYY-MM-DD" clearable style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存档案</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.table-wrap { padding: 8px 14px 14px; overflow: hidden; }
.terminal { color: var(--muted); font-size: 12px; }
</style>
