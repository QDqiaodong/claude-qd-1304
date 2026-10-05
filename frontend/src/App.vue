<script setup>
import { computed, onMounted, ref } from 'vue'
import router from './router'
import api from './api'

import RoomView from './views/RoomView.vue'
import MaternalView from './views/MaternalView.vue'
import CareView from './views/CareView.vue'
import MealView from './views/MealView.vue'

const modules = [
  { path: '/rooms', label: '房间管理', mark: '房', detail: '房态与月费', comp: RoomView },
  { path: '/maternals', label: '产妇档案', mark: '人', detail: '入住与出院', comp: MaternalView },
  { path: '/care', label: '护理打卡', mark: '护', detail: '任务与进度', comp: CareView },
  { path: '/meals', label: '月子餐', mark: '餐', detail: '配餐与配送', comp: MealView }
]

const currentPath = ref(router.currentRoute.value.path)
const counts = ref({})

const currentView = computed(() => {
  const item = modules.find(module => module.path === currentPath.value)
  return item ? item.comp : RoomView
})

async function loadCounts() {
  try {
    const [rooms, maternals, careTasks, meals] = await Promise.all([
      api.listRooms(), api.listMaternals(), api.listCareTasks(), api.listMeals()
    ])
    counts.value = {
      '/rooms': rooms.length,
      '/maternals': maternals.length,
      '/care': careTasks.length,
      '/meals': meals.length
    }
  } catch (e) {
    // 模块计数失败不影响当前业务页面。
  }
}

function navigate(path) {
  if (path !== currentPath.value) {
    router.push(path)
  }
}

router.afterEach(to => {
  currentPath.value = to.path
})

onMounted(async () => {
  if (currentPath.value === '/') {
    await router.replace('/rooms')
  }
  currentPath.value = router.currentRoute.value.path
  await loadCounts()
})
</script>

<template>
  <div class="app-shell">
    <header class="brand-header">
      <div class="brand-mark">悦</div>
      <div>
        <h1>产后护理中心</h1>
        <p>细致照护每一天</p>
      </div>
      <span class="service-pill">住院照护管理</span>
    </header>

    <nav class="module-cards" aria-label="业务模块">
      <button
        v-for="item in modules"
        :key="item.path"
        class="module-card"
        :class="{ active: currentPath === item.path }"
        type="button"
        @click="navigate(item.path)"
      >
        <span class="module-mark">{{ item.mark }}</span>
        <span class="module-copy">
          <strong>{{ item.label }}</strong>
          <small>{{ item.detail }}</small>
        </span>
        <span class="module-count">{{ counts[item.path] ?? 0 }}</span>
      </button>
    </nav>

    <main class="content">
      <component :is="currentView" @changed="loadCounts" />
    </main>
  </div>
</template>

<style scoped>
.app-shell { min-height: 100vh; }
.brand-header {
  max-width: 1240px;
  margin: 0 auto;
  padding: 28px 24px 18px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.brand-mark {
  width: 48px;
  height: 48px;
  display: grid;
  place-items: center;
  border-radius: 16px;
  color: #fff;
  font-size: 22px;
  font-weight: 800;
  background: linear-gradient(145deg, var(--primary), var(--primary-deep));
  box-shadow: 0 10px 25px rgba(233, 30, 99, .24);
}
h1 { margin: 0; font-size: 23px; letter-spacing: 1px; }
p { margin: 4px 0 0; color: var(--muted); font-size: 13px; }
.service-pill {
  margin-left: auto;
  padding: 7px 13px;
  border-radius: 999px;
  color: var(--primary-deep);
  background: var(--primary-wash);
  font-size: 12px;
  font-weight: 600;
}
.module-cards {
  max-width: 1240px;
  margin: 8px auto 0;
  padding: 0 24px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}
.module-card {
  min-height: 82px;
  padding: 14px 16px;
  border: 1px solid var(--line);
  border-radius: 18px;
  background: #fff;
  color: var(--ink);
  display: flex;
  align-items: center;
  gap: 12px;
  text-align: left;
  cursor: pointer;
  box-shadow: 0 5px 18px rgba(83, 35, 55, .06);
  transition: transform .2s ease, box-shadow .2s ease, border-color .2s ease;
}
.module-card:hover { border-color: var(--primary-soft); }
.module-card.active {
  transform: translateY(-9px);
  border-color: var(--primary);
  box-shadow: 0 16px 30px rgba(233, 30, 99, .18);
}
.module-mark {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 13px;
  color: var(--primary);
  background: var(--primary-wash);
  font-size: 18px;
  font-weight: 800;
}
.module-card.active .module-mark { color: #fff; background: var(--primary); }
.module-copy { min-width: 0; display: flex; flex-direction: column; gap: 5px; }
.module-copy strong { font-size: 15px; }
.module-copy small { color: var(--muted); }
.module-count {
  margin-left: auto;
  min-width: 28px;
  padding: 4px 8px;
  border-radius: 999px;
  color: var(--primary-deep);
  background: var(--primary-wash);
  text-align: center;
  font-size: 12px;
  font-weight: 700;
}
.content { max-width: 1240px; margin: 0 auto; padding: 28px 24px 40px; }
@media (max-width: 820px) {
  .module-cards { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .module-card.active { transform: translateY(-5px); }
}
@media (max-width: 520px) {
  .service-pill { display: none; }
  .module-cards { grid-template-columns: 1fr; }
}
</style>
