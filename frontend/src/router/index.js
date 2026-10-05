import { createRouter, createWebHashHistory } from 'vue-router'

import RoomView from '../views/RoomView.vue'
import MaternalView from '../views/MaternalView.vue'
import CareView from '../views/CareView.vue'
import MealView from '../views/MealView.vue'

const routes = [
  { path: '/', redirect: '/rooms' },
  { path: '/rooms', name: 'rooms', component: RoomView },
  { path: '/maternals', name: 'maternals', component: MaternalView },
  { path: '/care', name: 'care', component: CareView },
  { path: '/meals', name: 'meals', component: MealView },
  { path: '/:pathMatch(.*)*', redirect: '/rooms' }
]

export default createRouter({
  history: createWebHashHistory(),
  routes
})
