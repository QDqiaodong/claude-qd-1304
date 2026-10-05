<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const meals = ref([])
const maternals = ref([])
const dialogVisible = ref(false)
const mealTypes = ['早餐', '午餐', '晚餐', '加餐']
const selectedDate = ref(localDate())
const form = ref(emptyForm())

function localDate() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function emptyForm() {
  return { maternalId: '', mealName: '', mealType: '早餐', serveDate: selectedDate.value, calories: 400 }
}

const residents = computed(() => maternals.value.filter(item => item.status === '在住'))
const maternalMap = computed(() => Object.fromEntries(maternals.value.map(item => [item.id, item.name])))

async function load() {
  try {
    const [mealData, maternalData] = await Promise.all([
      api.listMeals(selectedDate.value), api.listMaternals()
    ])
    meals.value = mealData
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
    await api.createMeal({
      ...form.value,
      maternalId: Number(form.value.maternalId),
      calories: Number(form.value.calories)
    })
    ElMessage.success('月子餐已安排')
    dialogVisible.value = false
    await load()
    emit('changed')
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function deliver(meal) {
  try {
    await api.deliverMeal(meal.id)
    ElMessage.success('餐食已送达')
    await load()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

async function cancel(meal) {
  try {
    await api.cancelMeal(meal.id)
    ElMessage.success('餐食已取消')
    await load()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

function statusType(status) {
  return { 待配送: 'warning', 已配送: 'success', 已取消: 'info' }[status] || ''
}

const emit = defineEmits(['changed'])
onMounted(load)
</script>

<template>
  <section>
    <div class="page-head">
      <div>
        <h2>月子餐配送</h2>
        <p>按日期查看餐食，已取消不可配送，已配送不可再变更。</p>
      </div>
      <div class="head-actions">
        <el-date-picker v-model="selectedDate" value-format="YYYY-MM-DD" @change="load" />
        <el-button type="primary" @click="openCreate">新增配餐</el-button>
      </div>
    </div>

    <div class="meal-grid">
      <article v-for="meal in meals" :key="meal.id" class="meal-card page-card">
        <div class="meal-top">
          <span class="meal-type">{{ meal.mealType }}</span>
          <el-tag :type="statusType(meal.status)" size="small">{{ meal.status }}</el-tag>
        </div>
        <h3>{{ meal.mealName }}</h3>
        <p>{{ maternalMap[meal.maternalId] || `产妇 #${meal.maternalId}` }}</p>
        <div class="nutrition"><strong>{{ meal.calories }}</strong><span>千卡</span></div>
        <div v-if="meal.status === '待配送'" class="actions">
          <el-button type="primary" @click="deliver(meal)">确认送达</el-button>
          <el-button plain @click="cancel(meal)">取消</el-button>
        </div>
      </article>
      <div v-if="meals.length === 0" class="empty page-card">该日期暂无配餐</div>
    </div>

    <el-dialog v-model="dialogVisible" title="新增月子餐" width="500px">
      <el-form :model="form" label-width="82px">
        <el-form-item label="在住产妇">
          <el-select v-model="form.maternalId" style="width: 100%" placeholder="选择产妇">
            <el-option v-for="item in residents" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="餐名"><el-input v-model="form.mealName" placeholder="输入营养餐名称" /></el-form-item>
        <el-form-item label="餐别">
          <el-select v-model="form.mealType" style="width: 100%">
            <el-option v-for="item in mealTypes" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="配送日期"><el-date-picker v-model="form.serveDate" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="热量"><el-input-number v-model="form.calories" :min="1" :step="50" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存配餐</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.head-actions { display: flex; gap: 10px; }
.meal-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(235px, 1fr)); gap: 15px; }
.meal-card { position: relative; min-height: 235px; padding: 18px; overflow: hidden; }
.meal-card::after { content: ''; position: absolute; width: 90px; height: 90px; right: -34px; bottom: -34px; border-radius: 50%; background: var(--primary-wash); }
.meal-top { display: flex; justify-content: space-between; align-items: center; }
.meal-type { color: var(--primary); font-weight: 700; }
h3 { margin: 22px 0 7px; font-size: 17px; }
p { margin: 0; color: var(--muted); }
.nutrition { margin-top: 18px; color: var(--primary-deep); }
.nutrition strong { font-size: 28px; margin-right: 5px; }
.actions { position: relative; z-index: 1; margin-top: 18px; }
.empty { grid-column: 1 / -1; padding: 55px; text-align: center; color: var(--muted); }
@media (max-width: 620px) { .page-head { flex-direction: column; } .head-actions { width: 100%; flex-wrap: wrap; } }
</style>
