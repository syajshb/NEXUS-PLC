<script setup>
import { ref } from 'vue'
import { api } from '../utils/http.js'

const emit = defineEmits(['navigate'])

const loading = ref(false)
const toasts = ref([])
let tid = 0

function toast(msg, type = 'i') {
  const id = ++tid
  toasts.value.push({ id, msg, type })
  setTimeout(() => {
    toasts.value = toasts.value.filter(t => t.id !== id)
  }, 3500)
}

const form = ref({
  equipmentCode: '',
  alarmCode: '',
  alarmLevel: 1,
  alarmMessage: ''
})

const query = ref({ status: '' })
const handleForm = ref({ alarmId: '', handleUser: '', handleNote: '' })
const alarmList = ref([])

async function getAlarmList() {
  loading.value = true
  try {
    const p = {}
    if (query.value.status !== '') p.status = query.value.status
    const data = await api.alarm.get(p)
    alarmList.value = data.data || data || []
    toast(`查询到 ${alarmList.value.length} 条报警`)
  } catch (e) {
    toast('查询失败：' + (e.message || '未知错误'), 'er')
  }
  loading.value = false
}

async function postAlarm() {
  loading.value = true
  try {
    const body = { ...form.value, alarmLevel: parseInt(form.value.alarmLevel) }
    const data = await api.alarm.post(body)
    if (data.success !== false) {
      toast('报警记录已提交', 'ok')
      await getAlarmList()
    } else {
      toast('提交失败', 'er')
    }
  } catch (e) {
    toast('请求失败：' + (e.message || '未知错误'), 'er')
  }
  loading.value = false
}

async function handleAlarm() {
  if (!handleForm.value.alarmId || !handleForm.value.handleUser) {
    toast('请填写报警ID和处理人', 'er')
    return
  }
  loading.value = true
  try {
    const p = { handleUser: handleForm.value.handleUser }
    if (handleForm.value.handleNote) p.handleNote = handleForm.value.handleNote
    const data = await api.alarm.handle(handleForm.value.alarmId, p)
    if (data.success !== false) {
      toast('报警已处理', 'ok')
      await getAlarmList()
    } else {
      toast('处理失败', 'er')
    }
  } catch (e) {
    toast('请求失败：' + (e.message || '未知错误'), 'er')
  }
  loading.value = false
}

function alarmLevelClass(level) {
  const map = { 1: 'b-w1', 2: 'b-w2', 3: 'b-w3' }
  return map[level] || 'b-w1'
}

function alarmLevelLabel(level) {
  const map = { 1: '警告', 2: '严重', 3: '紧急' }
  return map[level] || '未知'
}

getAlarmList()
</script>

<template>
  <div class="page-container alarm-page">
    <div class="toast-w">
      <div v-for="t in toasts" :key="t.id" :class="['toast', t.type]">{{ t.msg }}</div>
    </div>

    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">🔔 报警管理</h1>
        <p class="page-subtitle">报警事件实时推送 · 快速响应处理</p>
      </div>
      <div class="header-right">
        <span class="nav-btns">
          <button class="nav-btn" @click="emit('navigate', 'monitor')">← 实时监控</button>
          <button class="nav-btn" @click="emit('navigate', 'energy')">能耗管理 →</button>
        </span>
      </div>
    </div>

    <div class="page-content">
      <div class="form-section">
        <div class="section-header">
          <h2 class="section-title">新建报警</h2>
          <span class="api-badge">POST /api/monitor/alarm</span>
        </div>
        <div class="form-grid">
          <div class="form-group">
            <label>设备编号</label>
            <input v-model="form.equipmentCode" class="form-input" placeholder="EQ-001" />
          </div>
          <div class="form-group">
            <label>报警代码</label>
            <input v-model="form.alarmCode" class="form-input" placeholder="TEMP_OVER" />
          </div>
          <div class="form-group">
            <label>报警级别</label>
            <select v-model="form.alarmLevel" class="form-input">
              <option value="1">WARNING - 警告</option>
              <option value="2">SEVERE - 严重</option>
              <option value="3">CRITICAL - 紧急</option>
            </select>
          </div>
          <div class="form-group">
            <label>报警信息</label>
            <input v-model="form.alarmMessage" class="form-input" placeholder="Temperature exceeded threshold" />
          </div>
          <div class="form-group submit-group">
            <button class="btn-primary" @click="postAlarm" :disabled="loading">
              <span v-if="loading" class="loading-dot"></span>
              <span v-else>提交报警</span>
            </button>
          </div>
        </div>
      </div>

      <div class="list-section">
        <div class="section-header">
          <h2 class="section-title">报警列表</h2>
          <div class="filter-bar">
            <select v-model="query.status" class="filter-input">
              <option value="">全部状态</option>
              <option value="0">待处理</option>
              <option value="1">已处理</option>
            </select