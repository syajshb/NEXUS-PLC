<script setup>
import { ref, computed } from 'vue'

defineProps({
  now: { type: String, default: '' },
  active: { type: Boolean, default: false }
})

const emit = defineEmits(['navigate', 'back'])

const energyStats = ref({
  today: 45.2,
  week: 286,
  month: 1245,
  year: 15230
})

const savingsRate = ref(12.5)

const energyChart = ref([65, 72, 58, 80, 75, 68, 78, 85, 72, 68, 75, 82])

const deviceList = ref([
  { name: '生产线A', electricity: 25.5, water: 5.2, gas: 2.8, status: 'running' },
  { name: '生产线B', electricity: 18.3, water: 3.8, gas: 1.9, status: 'running' },
  { name: '空压机', electricity: 15.8, water: 0, gas: 0, status: 'running' },
  { name: '冷却系统', electricity: 12.4, water: 8.5, gas: 0, status: 'standby' },
  { name: '照明系统', electricity: 8.2, water: 0, gas: 0, status: 'running' }
])

const statusClass = (status) => {
  const map = { running: 'status-running', standby: 'status-standby', offline: 'status-offline' }
  return map[status] || 'status-offline'
}

const statusLabel = (status) => {
  const map = { running: '运行中', standby: '待机', offline: '离线' }
  return map[status] || status
}

const totalConsumption = computed(() => {
  return deviceList.value.reduce((sum, d) => sum + d.electricity, 0).toFixed(1)
})
</script>

<template>
  <div class="energy-page">
    <div class="page-header">
      <button class="back-btn" @click="emit('back')">← 返回</button>
      <div class="header-center">
        <h1 class="page-title">⚡ 能耗管理</h1>
        <span class="page-subtitle">Energy Management</span>
      </div>
      <div class="header-right">
        <span class="page-time">{{ now }}</span>
        <button class="nav-btn" @click="emit('navigate', 'control')">远程控制 →</button>
      </div>
    </div>

    <div class="page-content">
      <div class="overview-section">
        <div class="overview-card main-card">
          <div class="card-icon">💡</div>
          <div class="card-content">
            <div class="card-value">{{ totalConsumption }} <span class="card-unit">kW</span></div>
            <div class="card-label">当前总功耗</div>
          </div>
          <div class="card-trend trend-up">+3.2%</div>
        </div>

        <div class="overview-grid">
          <div class="overview-card">
            <div class="card-icon">📅</div>
            <div class="card-content">
              <div class="card-value">{{ energyStats.today }} <span class="card-unit">kWh</span></div>
              <div class="card-label">今日能耗</div>
            </div>
          </div>
          <div class="overview-card">
            <div class="card-icon">📆</div>
            <div class="card-content">
              <div class="card-value">{{ energyStats.week }} <span class="card-unit">kWh</span></div>
              <div class="card-label">本周能耗</div>
            </div>
          </div>
          <div class="overview-card">
            <div class="card-icon">📊</div>
            <div class="card-content">
              <div class="card-value">{{ energyStats.month }} <span class="card-unit">kWh</span></div>
              <div class="card-label">本月能耗</div>
            </div>
          </div>
          <div class="overview-card savings-card">
            <div class="card-icon">💰</div>
            <div class="card-content">
              <div class="card-value">{{ savingsRate }}<span class="card