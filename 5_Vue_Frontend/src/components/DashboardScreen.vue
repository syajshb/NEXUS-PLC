<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { plcApi } from '../api/plcData'
import { handleApiError } from '../utils/http'

const props = defineProps({
  now: { type: String, default: '' },
  active: { type: Boolean, default: false }
})

const REFRESH_MS = Number(import.meta.env.VITE_DASH_REFRESH_MS) || 8000
const PAGE_SIZE = 20

const latest = ref(null)
const health = ref({ status: '', timestamp: '', service: '' })
const historyItems = ref([])
const historyTotal = ref(0)
const historyPage = ref(0)
const loadingLatest = ref(false)
const loadingHistory = ref(false)
const latestError = ref('')
const historyError = ref('')
const lastRefresh = ref('')
const detailId = ref('')
const detailRow = ref(null)
const detailError = ref('')

const alarmHistory = ref([
  { time: '10:23:45', desc: '温度过高报警', level: 2 },
  { time: '10:20:12', desc: '压力异常预警', level: 1 },
  { time: '10:15:33', desc: '设备维护提醒', level: 0 },
  { time: '10:10:08', desc: '能耗超标警告', level: 1 }
])

const controlState = ref({
  power: false,
  mode: 'auto',
  speed: 60
})

const gaugeProgress = computed(() => {
  const temp = Number(latest.value?.temperature) || 0
  return Math.min(100, Math.round((temp / 100) * 100))
})

let refreshTimer = null

const hasAlarm = computed(() => latest.value && Number(latest.value.alarmActive) !== 0)

const headlineKpis = computed(() => {
  const d = latest.value
  if (!d) {
    return [
      { key: 'temperature', icon: '🌡️', label: '温度', value: '--', unit: '℃' },
      { key: 'humidity', icon: '💧', label: '湿度', value: '--', unit: '%' },
      { key: 'pressure', icon: '📊', label: '压力', value: '--', unit: 'MPa' },
      { key: 'efficiency', icon: '⚙️', label: '效率', value: '--', unit: '%' },
      { key: 'qualityRate', icon: '✓', label: '良品率', value: '--', unit: '%' },
      { key: 'speed', icon: '⏩', label: '速度', value: '--', unit: '' },
      { key: 'alarm', icon: '🔔', label: '报警', value: '--', unit: '', alarm: false },
      { key: 'source', icon: '📡', label: '数据源', value: '--', unit: '' }
    ]
  }
  return [
    { key: 'temperature', icon: '🌡️', label: '温度', value: fmt(d.temperature), unit: '℃' },
    { key: 'humidity', icon: '💧', label: '湿度', value: fmt(d.humidity), unit: '%' },
    { key: 'pressure', icon: '📊', label: '压力', value: fmt(d.pressure), unit: 'MPa' },
    { key: 'efficiency', icon: '⚙️', label: '效率', value: fmt(d.efficiency), unit: '%' },
    { key: 'qualityRate', icon: '✓', label: '良品率', value: fmt(d.qualityRate), unit: '%' },
    { key: 'speed', icon: '⏩', label: '速度', value: fmt(d.speed), unit: '' },
    {
      key: 'alarm',
      icon: '🔔',
      label: '报警',
      value: Number(d.alarmActive) !== 0 ? `L${d.alarmLevel ?? 0}` : '正常',
      unit: d.alarmCounter != null ? `#${d.alarmCounter}` : '',
      alarm: Number(d.alarmActive) !== 0
    },
    { key: 'source', icon: '📡', label: '数据源', value: d.source || '--', unit: '' }
  ]
})

const secondaryKpis = computed(() => {
  const d = latest.value
  if (!d) {
    return [
      { label: '采集时间', value: '--' },
      { label: '运行状态', value: '--' },
      { label: '产量', value: '--' },
      { label: '功耗', value: '--' },
      { label: '电/水/气', value: '--' },
      { label: '运行时长', value: '--' }
    ]
  }
  return [
    { label: '采集时间', value: formatTs(d.timestamp) },
    { label: '运行状态', value: statusLabel(d.status) },
    { label: '产量', value: d.productCount ?? '--' },
    { label: '功耗', value: d.powerConsumption != null ? `${fmt(d.powerConsumption)} kW` : '--' },
    {
      label: '电/水/气',
      value: [d.electricity, d.water, d.gas].map(v => (v != null ? fmt(v) : '--')).join(' / ')
    },
    { label: '运行时长', value: d.runHours != null ? `${fmt(d.runHours)} h` : '--' }
  ]
})

const chartBars = computed(() => {
  const temps = historyItems.value
    .map(r => (r.temperature != null ? Number(r.temperature) : null))
    .filter(v => v != null && !Number.isNaN(v))
    .slice(0, 20)
  if (!temps.length) return Array(20).fill(30)
  const max = Math.max(...temps, 1)
  return temps.map(t => Math.min(100, Math.round((t / max) * 100)))
})

const totalPages = computed(() =>
  Math.max(1, Math.ceil(historyTotal.value / PAGE_SIZE))
)

function fmt(v) {
  if (v == null || v === '') return '--'
  const n = Number(v)
  return Number.isNaN(n) ? String(v) : n.toFixed(1)
}

function formatTs(ts) {
  if (!ts) return '--'
  try {
    return new Date(ts).toLocaleString('zh-CN')
  } catch {
    return ts
  }
}

function statusLabel(status) {
  const map = { 0: '离线', 1: '运行', 2: '待机', 3: '故障' }
  return map[status] ?? `状态 ${status}`
}

function statusClass(status) {
  const map = { 0: 'offline', 1: 'running', 2: 'standby', 3: 'fault' }
  return map[status] || 'offline'
}

function alarmRowClass(row) {
  return Number(row.alarmActive) !== 0 ? 'row-alarm' : ''
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
  latestError.value = ''
  try {
    const data = await plcApi.latest()
    latest.value = data
    if (!data) latestError.value = '暂无最新 PLC 数据（接口返回 404）'
  } catch (e) {
    latest.value = null
    latestError.value = handleApiError(e, '获取最新数据失败')
  }
  loadingLatest.value = false
}

async function fetchHistory() {
  loadingHistory.value = true
  historyError.value = ''
  try {
    const data = await plcApi.listPage(historyPage.value, PAGE_SIZE)
    historyItems.value = data.items || []
    historyTotal.value = data.total ?? historyItems.value.length
  } catch (e) {
    historyItems.value = []
    historyTotal.value = 0
    historyError.value = handleApiError(e, '获取历史数据失败')
  }
  loadingHistory.value = false
}

async function fetchDetail() {
  detailError.value = ''
  detailRow.value = null
  const id = detailId.value.trim()
  if (!id) {
    detailError.value = '请输入记录 ID'
    return
  }
  try {
    const row = await plcApi.getById(id)
    if (!row) detailError.value = `未找到 ID=${id} 的记录（404）`
    else detailRow.value = row
  } catch (e) {
    detailError.value = handleApiError(e, '查询失败')
  }
}

async function refreshAll() {
  await Promise.all([fetchHealth(), fetchLatest(), fetchHistory()])
  lastRefresh.value = new Date().toLocaleTimeString('zh-CN')
}

function togglePower() {
  controlState.value.power = !controlState.value.power
}

function applyControl() {
  console.log('应用控制:', controlState.value)
}

function resetControl() {
  controlState.value = {
    power: false,
    mode: 'auto',
    speed: 60
  }
}

function acknowledgeAlarms() {
  alarmHistory.value = []
  if (latest.value) {
    latest.value.alarmActive = 0
    latest.value.alarmCounter = 0
  }
}

function goPage(p) {
  const next = Math.min(Math.max(0, p), totalPages.value - 1)
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

watch(
  () => props.active,
  (on) => {
    if (on) startPolling()
    else stopPolling()
  },
  { immediate: true }
)

onMounted(() => {
  if (props.active) fetchHistory()
})

onUnmounted(() => stopPolling())
</script>

<template>
  <div class="big-screen" :class="{ 'alarm-active': hasAlarm }">
    <div class="screen-header">
      <div>
        <h2 class="screen-title">展示大屏 · PLC 实时监控</h2>
        <p v-if="latestError" class="dash-hint dash-hint-warn">{{ latestError }}</p>
      </div>
      <div class="screen-header-meta">
        <span class="health-pill" :class="health.status === 'UP' ? 'up' : 'dn'">
          <span class="dot" :class="health.status === 'UP' ? 'up' : 'dn'"></span>
          {{ health.status === 'UP' ? '后端正常' : '后端未连接' }}
          <span v-if="health.service" class="health-svc">· {{ health.service }}</span>
        </span>
        <span class="screen-badge">{{ now }}</span>
        <span v-if="lastRefresh" class="screen-refresh">刷新 {{ lastRefresh }} · {{ REFRESH_MS / 1000 }}s</span>
        <button class="btn bs bsm" :disabled="loadingLatest || loadingHistory" @click="refreshAll">
          <span v-if="loadingLatest || loadingHistory" class="ld"></span>
          <span v-else>立即刷新</span>
        </button>
      </div>
    </div>

    <div v-if="hasAlarm" class="alarm-banner">
      <span class="alarm-banner-icon">⚠</span>
      <span>
        报警激活 · 级别 {{ latest?.alarmLevel ?? '--' }} · 计数 {{ latest?.alarmCounter ?? '--' }}
        <span v-if="latest?.source"> · {{ latest.source }}</span>
      </span>
    </div>

    <div class="screen-quadrants">
      <div class="quadrant quadrant-monitor">
        <div class="quadrant-header">
          <div class="quadrant-icon">🔵</div>
          <h3 class="quadrant-title">实时监控</h3>
          <span class="quadrant-subtitle">Real-time Monitoring</span>
        </div>
        <div class="monitor-content">
          <div class="monitor-gauge">
            <div class="gauge-ring">
              <div class="gauge-progress" :style="{ '--progress': gaugeProgress }"></div>
              <div class="gauge-center">
                <span class="gauge-value">{{ fmt(latest?.temperature) }}</span>
                <span class="gauge-unit">℃</span>
              </div>
            </div>
          </div>
          <div class="monitor-metrics">
            <div class="metric-item">
              <span class="metric-icon">💧</span>
              <span class="metric-value">{{ fmt(latest?.humidity) }}%</span>
              <span class="metric-label">湿度</span>
            </div>
            <div class="metric-item">
              <span class="metric-icon">📊</span>
              <span class="metric-value">{{ fmt(latest?.pressure) }} MPa</span>
              <span class="metric-label">压力</span>
            </div>
            <div class="metric-item">
              <span class="metric-icon">⚙️</span>
              <span class="metric-value">{{ fmt(latest?.efficiency) }}%</span>
              <span class="metric-label">效率</span>
            </div>
          </div>
          <div class="monitor-status">
            <span :class="['status-indicator', statusClass(latest?.status)]"></span>
            <span class="status-text">{{ statusLabel(latest?.status) }}</span>
          </div>
        </div>
      </div>

      <div class="quadrant quadrant-alarm">
        <div class="quadrant-header">
          <div class="quadrant-icon">�</div>
          <h3 class="quadrant-title">报警管理</h3>
          <span class="quadrant-subtitle">Alarm Management</span>
        </div>
        <div class="alarm-content">
          <div class="alarm-summary" :class="{ 'alarm-active': hasAlarm }">
            <div class="alarm-count">
              <span class="count-number">{{ latest?.alarmCounter ?? 0 }}</span>
              <span class="count-label">活跃报警</span>
            </div>
            <div class="alarm-level">
              <span class="level-badge" :class="`level-${latest?.alarmLevel || 0}`">
                L{{ latest?.alarmLevel ?? '--' }}
              </span>
              <span class="level-label">最高级别</span>
            </div>
          </div>
          <div class="alarm-history">
            <div class="alarm-item" v-for="(item, i) in alarmHistory.slice(0, 4)" :key="i">
              <span class="alarm-time">{{ item.time }}</span>
              <span class="alarm-desc">{{ item.desc }}</span>
              <span class="alarm-tag" :class="`level-${item.level}`">L{{ item.level }}</span>
            </div>
          </div>
          <button class="alarm-action" @click="acknowledgeAlarms">确认所有报警</button>
        </div>
      </div>

      <div class="quadrant quadrant-energy">
        <div class="quadrant-header">
          <div class="quadrant-icon">�</div>
          <h3 class="quadrant-title">能耗管理</h3>
          <span class="quadrant-subtitle">Energy Management</span>
        </div>
        <div class="energy-content">
          <div class="energy-total">
            <span class="energy-value">{{ fmt(latest?.powerConsumption) }}</span>
            <span class="energy-unit">kW</span>
            <span class="energy-label">当前功耗</span>
          </div>
          <div class="energy-bars">
            <div class="bar-item">
              <div class="bar-label">今日</div>
              <div class="bar-track">
                <div class="bar-fill" style="width: 75%"></div>
              </div>
              <div class="bar-value">45.2 kWh</div>
            </div>
            <div class="bar-item">
              <div class="bar-label">本周</div>
              <div class="bar-track">
                <div class="bar-fill" style="width: 62%"></div>
              </div>
              <div class="bar-value">286 kWh</div>
            </div>
            <div class="bar-item">
              <div class="bar-label">本月</div>
              <div class="bar-track">
                <div class="bar-fill" style="width: 48%"></div>
              </div>
              <div class="bar-value">1,245 kWh</div>
            </div>
          </div>
          <div class="energy-savings">
            <span class="savings-icon">💰</span>
            <span class="savings-text">本月节能 <strong>12.5%</strong></span>
          </div>
        </div>
      </div>

      <div class="quadrant quadrant-control">
        <div class="quadrant-header">
          <div class="quadrant-icon">🟢</div>
          <h3 class="quadrant-title">远程控制</h3>
          <span class="quadrant-subtitle">Remote Control</span>
        </div>
        <div class="control-content">
          <div class="control-group">
            <span class="control-label">设备开关</span>
            <button
              class="control-switch"
              :class="{ 'switch-on': controlState.power }"
              @click="togglePower"
            >
              <span class="switch-knob"></span>
            </button>
          </div>
          <div class="control-group">
            <span class="control-label">运行模式</span>
            <select class="control-select" v-model="controlState.mode">
              <option value="auto">自动模式</option>
              <option value="manual">手动模式</option>
              <option value="eco">节能模式</option>
            </select>
          </div>
          <div class="control-group">
            <span class="control-label">转速调节</span>
            <input
              type="range"
              class="control-slider"
              min="0"
              max="100"
              v-model="controlState.speed"
            />
            <span class="slider-value">{{ controlState.speed }}%</span>
          </div>
          <div class="control-actions">
            <button class="control-btn control-btn-primary" @click="applyControl">应用控制</button>
            <button class="control-btn control-btn-secondary" @click="resetControl">重置</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.big-screen {
  padding: 16px 20px;
  position: relative;
  z-index: 5;
}

.screen-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: var(--panel);
  border: 1px solid var(--edge);
  position: relative;
}

.screen-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--neon), var(--neon2), transparent);
}

.screen-title {
  margin: 0;
  font-family: var(--orb);
  font-size: 16px;
  font-weight: 700;
  color: var(--neon);
  letter-spacing: 3px;
  text-transform: uppercase;
}

.screen-header-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.health-pill {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 2px;
  font-size: 11px;
  font-family: var(--mono);
  letter-spacing: 1px;
  border: 1px solid;
}

.health-pill.up {
  background: rgba(0, 255, 136, 0.08);
  color: var(--neon2);
  border-color: rgba(0, 255, 136, 0.3);
}

.health-pill.dn {
  background: rgba(255, 68, 0, 0.08);
  color: var(--plasma);
  border-color: rgba(255, 68, 0, 0.3);
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.dot.up {
  background: var(--neon2);
  box-shadow: 0 0 8px var(--neon2);
}

.dot.dn {
  background: var(--plasma);
  box-shadow: 0 0 8px var(--plasma);
}

.health-svc {
  color: var(--txt3);
}

.screen-badge {
  font-family: var(--mono);
  font-size: 11px;
  color: var(--txt2);
  letter-spacing: 1px;
}

.screen-refresh {
  font-size: 11px;
  color: var(--txt3);
  font-family: var(--mono);
}

.alarm-banner {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: rgba(255, 68, 0, 0.1);
  border: 1px solid rgba(255, 68, 0, 0.4);
  color: var(--plasma);
  margin-bottom: 16px;
  font-family: var(--mono);
  font-size: 12px;
  letter-spacing: 1px;
  animation: blink 1.5s ease-in-out infinite;
}

.alarm-banner-icon {
  font-size: 14px;
}

.screen-grid {
  display: grid;
  gap: 2px;
  margin-bottom: 2px;
}

.screen-grid-kpi {
  grid-template-columns: repeat(4, 1fr);
}

.screen-grid-secondary {
  grid-template-columns: repeat(6, 1fr);
  margin-bottom: 16px;
}

.stat-panel {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: var(--card);
  border: 1px solid var(--edge);
  position: relative;
  transition: all 0.3s;
}

.stat-panel::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 30%;
  height: 2px;
  background: linear-gradient(90deg, var(--neon), transparent);
}

.stat-panel:hover {
  background: var(--card2);
  border-color: var(--rim);
}

.stat-panel-alarm {
  background: rgba(255, 68, 0, 0.08);
  border-color: rgba(255, 68, 0, 0.4);
}

.stat-panel-alarm::before {
  background: linear-gradient(90deg, var(--plasma), transparent);
}

.stat-icon {
  font-size: 22px;
  opacity: 0.9;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--neon);
  font-family: var(--mono);
  letter-spacing: 1px;
}

.stat-unit {
  font-size: 11px;
  color: var(--txt2);
  margin-left: 4px;
}

.stat-label {
  font-size: 10px;
  color: var(--txt3);
  font-family: var(--mono);
  letter-spacing: 1px;
  text-transform: uppercase;
  margin-top: 2px;
}

.ms {
  background: var(--card);
  border: 1px solid var(--edge);
  padding: 14px 16px;
  position: relative;
  transition: all 0.3s;
}

.ms::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--neon2), transparent);
  opacity: 0.5;
}

.ms:hover {
  background: var(--card2);
}

.ms-v {
  font-family: var(--mono);
  font-size: 14px;
  font-weight: 600;
  color: var(--neon2);
  letter-spacing: 1px;
}

.ms-l {
  font-size: 10px;
  color: var(--txt3);
  font-family: var(--mono);
  letter-spacing: 1px;
  text-transform: uppercase;
  margin-top: 4px;
}

.screen-row {
  display: flex;
  gap: 2px;
  margin-bottom: 2px;
}

.chart-panel {
  flex: 1;
  padding: 16px 20px;
  background: var(--card);
  border: 1px solid var(--edge);
  position: relative;
}

.chart-panel::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 60px;
  height: 2px;
  background: linear-gradient(90deg, var(--neon), transparent);
}

.chart-panel.large {
  flex: 2;
}

.chart-panel.full {
  flex: 1;
  min-width: 100%;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--edge);
}

.panel-header h3 {
  margin: 0;
  font-family: var(--orb);
  font-size: 12px;
  font-weight: 600;
  color: var(--txt);
  letter-spacing: 2px;
  text-transform: uppercase;
}

.chart-container {
  height: 140px;
}

.chart-placeholder {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  height: 100%;
  padding-top: 20px;
}

.chart-bar {
  width: 6px;
  border-radius: 3px 3px 0 0;
  transition: height 0.5s ease;
}

.snapshot-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.snapshot-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid var(--edge);
  font-size: 12px;
}

.snapshot-row:last-child {
  border-bottom: none;
}

.snapshot-row span:first-child {
  color: var(--txt3);
  font-family: var(--mono);
  font-size: 10px;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.snapshot-row span:last-child {
  color: var(--neon);
  font-family: var(--mono);
  font-size: 12px;
}

.pager {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pager-info {
  font-size: 11px;
  color: var(--txt2);
  font-family: var(--mono);
  letter-spacing: 1px;
}

.tbl {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
}

.tbl th {
  padding: 10px 8px;
  text-align: left;
  background: var(--panel);
  color: var(--txt2);
  font-family: var(--mono);
  font-size: 10px;
  font-weight: 500;
  letter-spacing: 1px;
  text-transform: uppercase;
  border-bottom: 1px solid var(--edge);
}

.tbl td {
  padding: 10px 8px;
  border-bottom: 1px solid var(--edge);
  color: var(--txt);
  font-size: 12px;
}

.tbl .mono {
  font-family: var(--mono);
  font-size: 11px;
}

.tbl .ts-col {
  color: var(--txt2);
  font-size: 10px;
}

.status-badge {
  padding: 3px 8px;
  border-radius: 2px;
  font-family: var(--mono);
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 1px;
}

.status-badge.running {
  background: rgba(0, 255, 136, 0.12);
  color: var(--neon2);
  border: 1px solid rgba(0, 255, 136, 0.3);
}

.status-badge.offline {
  background: rgba(90, 144, 176, 0.1);
  color: var(--txt3);
  border: 1px solid var(--edge);
}

.status-badge.standby {
  background: rgba(255, 221, 0, 0.1);
  color: var(--volt);
  border: 1px solid rgba(255, 221, 0, 0.3);
}

.status-badge.fault {
  background: rgba(255, 68, 0, 0.1);
  color: var(--plasma);
  border: 1px solid rgba(255, 68, 0, 0.3);
}

.row-alarm {
  background: rgba(255, 68, 0, 0.05);
}

.row-alarm:hover {
  background: rgba(255, 68, 0, 0.1);
}

.empty {
  text-align: center;
  color: var(--txt3);
  font-family: var(--mono);
  padding: 30px 0;
  letter-spacing: 1px;
}

.detail-lookup-card {
  flex: 1;
  padding: 16px 20px;
  background: var(--card);
  border: 1px solid var(--edge);
}

.c-title {
  font-family: var(--orb);
  font-size: 12px;
  font-weight: 600;
  color: var(--txt);
  letter-spacing: 2px;
  text-transform: uppercase;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--edge);
}

.fr {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  flex-wrap: wrap;
}

.fg {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  min-width: 120px;
}

.fg label {
  font-family: var(--mono);
  font-size: 9px;
  color: var(--txt3);
  letter-spacing: 1px;
  text-transform: uppercase;
}

.fi {
  background: var(--panel);
  border: 1px solid var(--edge);
  color: var(--txt);
  padding: 8px 12px;
  font-family: var(--mono);
  font-size: 12px;
  outline: none;
  transition: border-color 0.3s;
}

.fi:focus {
  border-color: var(--neon);
}

.fi::placeholder {
  color: var(--txt3);
}

.btn {
  font-family: var(--mono);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 1px;
  padding: 8px 16px;
  border: 1px solid var(--edge);
  background: var(--panel);
  color: var(--txt);
  cursor: pointer;
  transition: all 0.3s;
  text-transform: uppercase;
}

.btn:hover:not(:disabled) {
  border-color: var(--neon);
  color: var(--neon);
  box-shadow: 0 0 10px rgba(0, 240, 255, 0.2);
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn.bs {
  background: rgba(0, 240, 255, 0.1);
  border-color: rgba(0, 240, 255, 0.4);
  color: var(--neon);
}

.btn.bo {
  background: transparent;
  border-color: var(--edge);
}

.screen-quadrants {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 12px;
  height: calc(100vh - 220px);
}

.quadrant {
  background: var(--panel);
  border: 1px solid var(--edge);
  padding: 16px;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.quadrant::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--neon), var(--neon2), transparent);
}

.quadrant-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--edge);
}

.quadrant-title {
  margin: 0;
  font-family: var(--orb);
  font-size: 13px;
  font-weight: 700;
  color: var(--txt);
  letter-spacing: 2px;
  text-transform: uppercase;
}

.kpi-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 12px;
}

.kpi-grid-small {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
  margin-bottom: 10px;
}

.secondary-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
  margin-bottom: 12px;
}

.stat-panel-small {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: var(--card);
  border: 1px solid var(--edge);
  position: relative;
  transition: all 0.3s;
}

.stat-panel-small::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 20%;
  height: 1.5px;
  background: linear-gradient(90deg, var(--neon), transparent);
}

.stat-panel-small:hover {
  background: var(--card2);
  border-color: var(--rim);
}

.stat-icon-small {
  font-size: 16px;
  opacity: 0.8;
}

.stat-info-small {
  flex: 1;
}

.stat-value-small {
  font-size: 16px;
  font-weight: 600;
  color: var(--neon);
  font-family: var(--mono);
  letter-spacing: 0.5px;
}

.stat-unit-small {
  font-size: 9px;
  color: var(--txt2);
  margin-left: 3px;
}

.stat-label-small {
  font-size: 8px;
  color: var(--txt3);
  font-family: var(--mono);
  letter-spacing: 1px;
  text-transform: uppercase;
  margin-top: 1px;
}

.chart-panel-inner {
  background: var(--card);
  border: 1px solid var(--edge);
  padding: 12px;
  margin-bottom: 8px;
}

.chart-header {
  font-family: var(--mono);
  font-size: 10px;
  color: var(--txt2);
  letter-spacing: 1px;
  margin-bottom: 12px;
  text-transform: uppercase;
}

.detail-lookup-area {
  background: var(--card);
  border: 1px solid var(--edge);
  padding: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.lookup-title {
  font-family: var(--mono);
  font-size: 10px;
  color: var(--txt2);
  letter-spacing: 1px;
  margin-bottom: 10px;
  text-transform: uppercase;
}

.lookup-input-wrap {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.lookup-input-wrap .fi {
  flex: 1;
}

.detail-result {
  flex: 1;
  overflow: auto;
  background: var(--panel);
  border: 1px solid var(--edge);
  padding: 10px;
}

.detail-result pre {
  font-family: var(--mono);
  font-size: 10px;
  color: var(--neon2);
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
}

.btn.bsm {
  padding: 6px 12px;
  font-size: 10px;
}

.ld {
  display: inline-block;
  width: 12px;
  height: 12px;
  border: 2px solid var(--edge);
  border-top-color: var(--neon);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.detail-json {
  margin-top: 12px;
  padding: 12px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--edge);
  border-radius: 2px;
  overflow-x: auto;
}

.detail-json pre {
  margin: 0;
  font-family: var(--mono);
  font-size: 11px;
  color: var(--neon);
  white-space: pre-wrap;
}

.dash-hint {
  font-size: 11px;
  color: var(--txt3);
  font-family: var(--mono);
  padding: 8px 0;
}

.dash-hint-warn {
  color: var(--warn);
}

.badge {
  display: inline-block;
  padding: 2px 6px;
  border-radius: 2px;
  font-family: var(--mono);
  font-size: 10px;
  font-weight: 600;
}

.b-flt {
  background: rgba(255, 68, 0, 0.15);
  color: var(--plasma);
  border: 1px solid rgba(255, 68, 0, 0.3);
}

.b-ok {
  background: rgba(0, 255, 136, 0.1);
  color: var(--neon2);
  border: 1px solid rgba(0, 255, 136, 0.25);
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
