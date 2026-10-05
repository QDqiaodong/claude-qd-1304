<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const rooms = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const roomTypes = ['标准间', '单间', '套房']
const roomStatuses = ['空闲', '在住', '清洁中', '维修']
const form = ref(emptyForm())

function emptyForm() {
  return { roomNo: '', type: '标准间', floor: '', monthlyFee: 12800, status: '空闲' }
}

async function load() {
  try {
    rooms.value = await api.listRooms()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

function openCreate() {
  editingId.value = null
  form.value = emptyForm()
  dialogVisible.value = true
}

function openEdit(room) {
  editingId.value = room.id
  form.value = {
    roomNo: room.roomNo,
    type: room.type,
    floor: room.floor,
    monthlyFee: Number(room.monthlyFee),
    status: room.status
  }
  dialogVisible.value = true
}

async function save() {
  try {
    const payload = { ...form.value, monthlyFee: Number(form.value.monthlyFee) }
    if (editingId.value) {
      await api.updateRoom(editingId.value, payload)
      ElMessage.success('房间信息已更新')
    } else {
      await api.createRoom(payload)
      ElMessage.success('房间已新增')
    }
    dialogVisible.value = false
    await load()
    emit('changed')
  } catch (error) {
    ElMessage.error(error.message)
  }
}

function statusType(status) {
  return { 空闲: 'success', 在住: 'danger', 清洁中: 'warning', 维修: 'info' }[status] || ''
}

const emit = defineEmits(['changed'])
onMounted(load)
</script>

<template>
  <section>
    <div class="page-head">
      <div>
        <h2>房间管理</h2>
        <p>掌握房态、房型与月费，入住只开放空闲房间。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增房间</el-button>
    </div>

    <div class="summary-grid">
      <div v-for="status in roomStatuses" :key="status" class="summary page-card">
        <span>{{ status }}</span>
        <strong>{{ rooms.filter(room => room.status === status).length }}</strong>
      </div>
    </div>

    <div class="page-card table-wrap">
      <el-table :data="rooms">
        <el-table-column prop="roomNo" label="房号" min-width="100" />
        <el-table-column prop="type" label="房型" min-width="100" />
        <el-table-column prop="floor" label="楼层" min-width="100" />
        <el-table-column label="月费" min-width="130">
          <template #default="{ row }">¥ {{ Number(row.monthlyFee).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column label="房态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="light">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="right">
          <template #default="{ row }"><el-button link type="primary" @click="openEdit(row)">编辑</el-button></template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑房间' : '新增房间'" width="480px">
      <el-form :model="form" label-width="76px">
        <el-form-item label="房号"><el-input v-model="form.roomNo" placeholder="如 401" /></el-form-item>
        <el-form-item label="房型">
          <el-select v-model="form.type" style="width: 100%">
            <el-option v-for="item in roomTypes" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层"><el-input v-model="form.floor" placeholder="如 四层" /></el-form-item>
        <el-form-item label="月费"><el-input-number v-model="form.monthlyFee" :min="1" :step="1000" style="width: 100%" /></el-form-item>
        <el-form-item label="房态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="item in roomStatuses" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 16px; }
.summary { padding: 16px 18px; display: flex; justify-content: space-between; align-items: center; color: var(--muted); }
.summary strong { color: var(--primary); font-size: 25px; }
.table-wrap { padding: 8px 14px 14px; overflow: hidden; }
@media (max-width: 700px) { .summary-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
