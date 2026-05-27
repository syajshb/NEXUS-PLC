const envBase = import.meta.env.VITE_API_BASE_URL
const BASE_URL =
  typeof envBase === 'string' && envBase.trim() !== ''
    ? envBase.trim().replace(/\/$/, '')
    : '/api'

class HttpClient {
  constructor() {
    this.baseURL = BASE_URL
    this.timeout = 30000
    this.token = null
  }

  setToken(token) {
    this.token = token
  }

  getToken() {
    return this.token || localStorage.getItem('token')
  }

  async request(method, url, data = null, params = null) {
    const fullUrl = this.buildUrl(url, params)
    const headers = this.buildHeaders()

    const options = {
      method: method.toUpperCase(),
      headers,
      timeout: this.timeout,
      credentials: 'include'
    }

    if (data) {
      options.body = typeof data === 'string' ? data : JSON.stringify(data)
    }

    try {
      const response = await fetch(fullUrl, options)
      return this.handleResponse(response)
    } catch (error) {
      return this.handleError(error)
    }
  }

  buildUrl(url, params) {
    let fullUrl = this.baseURL + url
    if (params) {
      const queryString = Object.entries(params)
        .filter(([, v]) => v !== '' && v !== null && v !== undefined)
        .map(([k, v]) => `${k}=${encodeURIComponent(v)}`)
        .join('&')
      if (queryString) {
        fullUrl += '?' + queryString
      }
    }
    return fullUrl
  }

  buildHeaders() {
    const headers = {
      'Content-Type': 'application/json;charset=UTF-8'
    }
    const token = this.getToken()
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }
    return headers
  }

  async handleResponse(response) {
    const text = await response.text()
    let data
    try {
      data = text ? JSON.parse(text) : {}
    } catch {
      data = { message: text }
    }

    if (!response.ok) {
      const error = new Error(data.message || `HTTP Error ${response.status}`)
      error.status = response.status
      error.data = data
      throw error
    }

    return data
  }

  handleError(error) {
    console.error('HTTP Request Error:', error)
    throw new Error(error.message || '网络请求失败，请检查网络连接')
  }

  get(url, params = null) {
    return this.request('GET', url, null, params)
  }

  /** GET that returns null on 404 instead of throwing */
  async getAllow404(url, params = null) {
    const fullUrl = this.buildUrl(url, params)
    const headers = this.buildHeaders()
    try {
      const response = await fetch(fullUrl, {
        method: 'GET',
        headers,
        credentials: 'include'
      })
      if (response.status === 404) return null
      return this.handleResponse(response)
    } catch (error) {
      return this.handleError(error)
    }
  }

  post(url, data = null, params = null) {
    return this.request('POST', url, data, params)
  }

  put(url, data = null, params = null) {
    return this.request('PUT', url, data, params)
  }

  delete(url, params = null) {
    return this.request('DELETE', url, null, params)
  }
}

const httpClient = new HttpClient()

export default httpClient

export const api = {
  health: () => httpClient.get('/health'),

  alarm: {
    post: (data) => httpClient.post('/monitor/alarm', data),
    get: (params) => httpClient.get('/monitor/alarm', params),
    count: () => httpClient.get('/monitor/alarm/count'),
    handle: (id, params) => httpClient.put(`/monitor/alarm/${id}/handle`, null, params)
  },

  energy: {
    post: (data) => httpClient.post('/monitor/energy', data),
    get: (params) => httpClient.get('/monitor/energy', params),
    total: (params) => httpClient.get('/monitor/energy/total', params)
  },

  command: {
    post: (params) => httpClient.post('/control/command', null, params),
    get: (params) => httpClient.get('/control/command', params),
    detail: (id) => httpClient.get(`/control/command/${id}`),
    update: (id, data) => httpClient.put(`/control/command/${id}`, data),
    history: (code) => httpClient.get(`/control/command/equipment/${code}`)
  },

  equipment: {
    get: (params) => httpClient.get('/equipment', params),
    detail: (code) => httpClient.get(`/equipment/${code}`),
    post: (data) => httpClient.post('/equipment', data),
    put: (data) => httpClient.put('/equipment', data),
    delete: (code) => httpClient.delete(`/equipment/${code}`)
  }
}

export function handleApiError(error, defaultMessage = '操作失败') {
  console.error('API Error:', error)
  
  const messages = {
    400: '请求参数错误',
    401: '未授权，请重新登录',
    403: '权限不足',
    404: '资源不存在',
    500: '服务器内部错误',
    502: '网关错误',
    503: '服务不可用'
  }
  
  if (error.status && messages[error.status]) {
    return messages[error.status]
  }
  
  if (error.data && error.data.message) {
    return error.data.message
  }
  
  return error.message || defaultMessage
}