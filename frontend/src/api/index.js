import axios from 'axios'

const http = axios.create({ baseURL: '/api', timeout: 20000 })

http.interceptors.response.use(
  response => response,
  error => {
    const message = error?.response?.data?.message || error?.message || '请求失败'
    return Promise.reject(new Error(message))
  }
)

async function call(promise) {
  const response = await promise
  const body = response.data
  if (!body || body.ok === false) {
    throw new Error(body?.message || '请求失败')
  }
  return body.data
}

const withParams = params => ({ params })

export default {
  listRooms: () => call(http.get('/rooms')),
  createRoom: data => call(http.post('/rooms', data)),
  updateRoom: (id, data) => call(http.put(`/rooms/${id}`, data)),

  listMaternals: () => call(http.get('/maternals')),
  createMaternal: data => call(http.post('/maternals', data)),
  updateMaternal: (id, data) => call(http.put(`/maternals/${id}`, data)),
  checkInMaternal: id => call(http.post(`/maternals/${id}/check-in`)),
  dischargeMaternal: id => call(http.post(`/maternals/${id}/discharge`)),
  withdrawMaternal: id => call(http.post(`/maternals/${id}/withdraw`)),

  listCareTasks: planDate => call(http.get('/care-tasks', planDate ? withParams({ planDate }) : {})),
  getCareCompletionRate: planDate => call(http.get('/care-tasks/completion-rate', planDate ? withParams({ planDate }) : {})),
  createCareTask: data => call(http.post('/care-tasks', data)),
  updateCareTask: (id, data) => call(http.put(`/care-tasks/${id}`, data)),
  completeCareTask: id => call(http.post(`/care-tasks/${id}/complete`)),
  skipCareTask: id => call(http.post(`/care-tasks/${id}/skip`)),

  listMeals: serveDate => call(http.get('/meals', serveDate ? withParams({ serveDate }) : {})),
  createMeal: data => call(http.post('/meals', data)),
  updateMeal: (id, data) => call(http.put(`/meals/${id}`, data)),
  deliverMeal: id => call(http.post(`/meals/${id}/deliver`)),
  cancelMeal: id => call(http.post(`/meals/${id}/cancel`))
}
