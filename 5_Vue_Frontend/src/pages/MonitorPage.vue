<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { plcApi } from '../api/plcData.js'

const props = defineProps({
  now: { type: String, default: '' },
  active: { type: Boolean, default: false }
})

const emit = defineEmits(['navigate'])

const REFRESH_MS = Number(import.meta.env.VITE_DASH_REFRESH_MS) || 8000
const PAGE_SIZE = 10

const latest = ref(null)
const health = ref({ status: '', timestamp: '', service: '' })
const historyItems = ref([])
const historyTotal = ref(0)
const historyPage = ref(0)
const loadingLatest = ref(false)
const loadingHistory = ref(false)

let refreshTimer = null

const gaugeProgress = computed(() => {
  const temp = Number(latest.value?.temperature) || 0
  return Math.min(100, Math.round((temp / 100) * 100))
})

const gaugeColor = computed(() => {
  const progress = gaugeProgress.value
  if (progress > 80) return 'danger'
  if (progress > 60) return 'warning'
  return 'normal'
})

const statusLabel = (status) => {
  const map = { 0: '离线', 1: '运行中', 2: '待机', 3: '故障' }
  return map[status] ?? `状态 ${status}`
}

const statusClass = (status) => {
  const map = { 0: 'offline', 1: 'running', 2: 'standby', 3: 'fault' }
  return map[status] || 'offline'
}

const formatTs = (ts) => {
  if (!ts) return '--'
  try {
    return new Date(ts).toLocaleString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
  } catch {
    return ts
  }
}

const fmt = (v) => {
  if (v == null || v === '') return '--'
  const n = Number(v)
  return Number.isNaN(n) ? String(v) : n.toFixed(1)
}

async function fetchHealth() {
  try {
    const data = await plcApi.health()
    health.value = {
      status: data.status || 'DOWN',
      timestamp: data.timestamp || '',
      service: data.service || ''
    }
  } catch {
    health.value = { status: 'DOWN', timestamp: '', service: '' }
  }
}

async function fetchLatest() {
  loadingLatest.value = true
  try {
    const data = await plcApi.latest()
    latest.value = data
  } catch {
    latest.value = null
  }
  loadingLatest.value = false
}

async function fetchHistory() {
  loadingHistory.value = true
  try {
    const data = await plcApi.listPage(historyPage.value, PAGE_SIZE)
    historyItems.value = data.items || []
    historyTotal.value = data.total ?? historyItems.value.length
  } catch {
    historyItems.value = []
    historyTotal.value = 0
  }
  loadingHistory.value = false
}

async function refreshAll() {
  await Promise.all([fetchHealth(), fetchLatest(), fetchHistory()])
}

function goPage(p) {
  const next = Math.max(0, Math.min(p, Math.max(0, Math.ceil(historyTotal.value / PAGE_SIZE) - 1)))
  if (next === historyPage.value) return
  historyPage.value = next
  fetchHistory()
}

function startPolling() {
  refreshAll()
  if (refreshTimer) clearInterval(refreshTimer)
  refreshTimer = setInterval(async () => {
    await Promise.all([fetchHealth(), fetchLatest()])
  }, REFRESH_MS)
}

function stopPolling() {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

watch(() => props.active, (on) => {
  if (on) startPolling()
  else stopPolling()
}, { immediate: true })

onMounted(() => {
  if (props.active) refreshAll()
})

onUnmounted(() => stopPolling())
</script>

<template>
  <div class="full-page monitor-page">
    <div class="page-header">
      <div class="header-left">
        <div class="nav-breadcrumb">
          <span class="breadcrumb-item" @click="emit('navigate', 'intro')">首页</span>
          <span class="breadcrumb-arrow">→</span>
          <span class="breadcrumb-item active">实时监控</span>
        </div>
        <h1 class="page-title">📊 实时监控</h1>
        <p class="page-subtitle">设备状态实时监控 · 毫秒级数据更新</p>
      </div>
      <div class="header-right">
        <span class="health-pill" :class="health.status === 'UP' ? 'up' : 'dn'">
          <span class="dot" :class="health.status === 'UP' ? 'up' : 'dn'"></span>
          {{ health.status === 'UP' ? '系统在线' : '系统离线' }}
        </span>
        <span class="page-time">{{ now }}</span>
        <button class="nav-btn" @click="emit('navigate', 'alarm')">报警管理</button>
      </div>
    </div>

    <div class="page-content">
      <div class="main-gauge-section">
        <div class="gauge-container">
          <div class="gauge-ring" :class="gaugeColor">
            <svg viewBox="0 0 200 200">
              <circle cx="100" cy="100" r="80" fill="none" stroke="rgba(0,0,0,0.3)" stroke-width="12" />
              <circle
                cx="100" cy="100" r="80" fill="none"
                :stroke="gaugeColor === 'danger' ? '#ff4400' : gaugeColor === 'warning' ? '#ffdd00' : '#00f0ff'"
                stroke-width="12"
                stroke-linecap="round"
                :stroke-dasharray="`${gaugeProgress * 5.02} 502`"
                transform="rotate(-90 100 100)"
                class="gauge-progress"
              />
            </svg>
            <div class="gauge-center">
              <span class="gauge-value">{{ fmt(latest?.temperature) }}</span>
              <span class="gauge-unit">℃</span>
            </div>
          </div>
          <div class="gauge-info">
            <div class="gauge-label">当前温度</div>
            <div class="gauge-status" :class="statusClass(latest?.status)">
              <span class="status-indicator"></span>
              {{ statusLabel(latest?.status) }}
            </div>
          </div>
        </div>

        <div class="metrics-grid">
          <div class="metric-card">
            <span class="metric-icon">💧</span>
            <div class="metric-content">
              <span class="metric-value">{{ fmt(latest?.humidity) }}%</span>
              <span class="metric-label">湿度</span>
            </div>
          </div>
          <div class="metric-card">
            <span class="metric-icon">📊</span>
            <div class="metric-content">
              <span class="metric-value">{{ fmt(latest?.pressure) }}</span>
              <span class="metric-label">压力 (MPa)</span>
            </div>
          </div>
          <div class="metric-card">
            <span class="metric-icon">⚙️</span>
            <div class="metric-content">
              <span class="metric-value">{{ fmt(latest?.efficiency) }}%</span>
              <span class="metric-label">效率</span>
            </div>
          </div>
          <div class="metric-card">
            <span class="metric-icon">✓</span>
            <div class="metric-content">
              <span class="metric-value">{{ fmt(latest?.qualityRate) }}%</span>
              <span class="metric-label">良品率</span>
            </div>
          </div>
        </div>
      </div>

      <div class="chart-section">
        <div class="section-header">
          <h2 class="section-title">📈 温度趋势</h2>
          <button class="btn-refresh" @click="refreshAll" :disabled="loadingLatest || loadingHistory">
            <span v-if="loadingLatest || loadingHistory" class="loading-spinner"></span>
            <span v-else>刷新数据</span>
          </button>
        </div>
        <div class="chart-container">
          <div class="chart-y-axis">
            <span>100%</span>
            <span>75%</span>
            <span>50%</span>
            <span>25%</span>
            <span>0%</span>
          </div>
          <div class="chart-bars">
            <div
              v-for="(item, i) in historyItems.slice(0, 12)"
              :key="i"
              class="chart-bar-wrapper"
            >
              <div
                class="chart-bar"
                :style="{ height: Math.min(100, ((Number(item.temperature) || 0) / 100) * 100) + '%' }"
              ></div>
              <span class="bar-label">{{ formatTs(item.timestamp) }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="data-section">
        <div class="section-header">
          <h2 class="section-title">📋 实时数据列表</h2>
          <div class="pagination">
            <button class="page-btn" :disabled="historyPage <= 0" @click="goPage(historyPage - 1)">上一页</button>
            <span class="page-info">第 {{ historyPage + 1 }} 页</span>
            <button