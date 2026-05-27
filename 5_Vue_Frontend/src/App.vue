<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { plcApi } from './api/plcData.js'
import { api } from './utils/http.js'
import DashboardScreen from './components/DashboardScreen.vue'
import NumPyLearnPage from './pages/NumPyLearnPage.vue'
import StockAnalysisPage from './pages/StockAnalysisPage.vue'

const page = ref('intro')
const panel = ref('health')
const pageDirection = ref('next')
const firstEnter = ref(true)
const now = ref('')
const toasts = ref([])
const visibleSections = ref({})
let tid = 0

function toast(msg, type = 'i') {
  const id = ++tid
  toasts.value.push({ id, msg, type })
  setTimeout(() => {
    toasts.value = toasts.value.filter(t => t.id !== id)
  }, 3500)
}

const ld = ref({})
const setLd = (k, v) => ld.value[k] = v

const health = ref({ s: '', t: '', svc: '' })
const hr = ref('点击按钮发送请求...')
const plcDataList = ref([])
const plcDataPage = ref(0)
const plcDataSize = ref(20)
const plcDataTotal = ref(0)
const currentPlcData = ref(null)
const queryId = ref('')

const unhandled = ref(0)

const f = ref({
  alarm: { equipmentCode: '', alarmCode: '', alarmLevel: 1, alarmMessage: '' },
  handle: { alarmId: '', handleUser: '', handleNote: '' },
  energy: { equipmentCode: '', electricity: '', water: '', gas: '' },
  cmd: { equipmentCode: '', commandType: '', commandValue: '' },
  cmdUpdate: { id: '', status: 2, responseMessage: '' },
  eq: { equipmentCode: '', equipmentName: '', equipmentType: '', location: '', ipAddress: '', description: '' }
})

const q = ref({
  alarm: { status: '' },
  energy: { code: '', limit: 100 },
  energyTotal: { code: '', start: '', end: '' },
  cmd: { code: '' },
  cmdDetail: { id: '' },
  cmdHistory: { code: '' },
  eq: { type: '' },
  eqDetail: { code: '' },
  eqDel: { code: '' }
})

const t = ref({ alarm: 'post', energy: 'post', cmd: 'post', eq: 'list' })

const lists = ref({ alarm: [], energy: [], cmd: [], eq: [] })

async function getAlarmList() {
  setLd('alarm', true)
  try {
    const p = {}
    if (q.value.alarm.status !== '') p.status = q.value.alarm.status
    const data = await api.alarm.get(p)
    lists.value.alarm = data.data || data || []
    toast(`查询到 ${lists.value.alarm.length} 条报警`)
  } catch (e) {
    toast('查询失败：' + (e.message || '未知错误'), 'er')
  }
  setLd('alarm', false)
}

async function postAlarm() {
  setLd('alarm', true)
  try {
    const body = { ...f.value.alarm, alarmLevel: parseInt(f.value.alarm.alarmLevel) }
    const data = await api.alarm.post(body)
    if (data.success !== false) {
      toast('报警记录已提交', 'ok')
      await getAlarmList()
      await getAlarmCount()
    } else {
      toast('提交失败', 'er')
    }
  } catch (e) {
    toast('请求失败：' + (e.message || '未知错误'), 'er')
  }
  setLd('alarm', false)
}

async function handleAlarm() {
  if (!f.value.handle.alarmId || !f.value.handle.handleUser) {
    toast('请填写报警ID和处理人', 'er')
    return
  }
  setLd('alarm', true)
  try {
    const p = { handleUser: f.value.handle.handleUser }
    if (f.value.handle.handleNote) p.handleNote = f.value.handle.handleNote
    const data = await api.alarm.handle(f.value.handle.alarmId, p)
    if (data.success !== false) {
      toast('报警已处理', 'ok')
      await getAlarmList()
      await getAlarmCount()
    } else {
      toast('处理失败', 'er')
    }
  } catch (e) {
    toast('请求失败：' + (e.message || '未知错误'), 'er')
  }
  setLd('alarm', false)
}

async function getAlarmCount() {
  try {
    const data = await api.alarm.count()
    unhandled.value = data.count ?? 0
  } catch (e) {
    console.error(e)
  }
}

async function getEnergyList() {
  if (!q.value.energy.code) {
    toast('请输入设备编号', 'er')
    return
  }
  setLd('energy', true)
  try {
    const p = { equipmentCode: q.value.energy.code }
    if (q.value.energy.limit) p.limit = parseInt(q.value.energy.limit)
    const data = await api.energy.get(p)
    lists.value.energy = data.data || data || []
    toast(`查询到 ${lists.value.energy.length} 条记录`)
  } catch (e) {
    toast('查询失败：' + (e.message || '未知错误'), 'er')
  }
  setLd('energy', false)
}

async function postEnergy() {
  setLd('energy', true)
  try {
    const body = { ...f.value.energy, electricity: parseFloat(f.value.energy.electricity) }
    if (f.value.energy.water) body.water = parseFloat(f.value.energy.water)
    if (f.value.energy.gas) body.gas = parseFloat(f.value.energy.gas)
    const data = await api.energy.post(body)
    if (data.success !== false) {
      toast('能耗数据已上报', 'ok')
      await getEnergyList()
    } else {
      toast('上报失败', 'er')
    }
  } catch (e) {
    toast('请求失败：' + (e.message || '未知错误'), 'er')
  }
  setLd('energy', false)
}

async function getEnergyTotal() {
  if (!q.value.energyTotal.code || !q.value.energyTotal.start || !q.value.energyTotal.end) {
    toast('请填写完整的查询条件', 'er')
    return
  }
  setLd('energy', true)
  try {
    const data = await api.energy.total({
      equipmentCode: q.value.energyTotal.code,
      startTime: q.value.energyTotal.start,
      endTime: q.value.energyTotal.end
    })
    if (data.success !== false) {
      toast('查询完成', 'ok')
    } else {
      toast('查询失败', 'er')
    }
  } catch (e) {
    toast('查询失败：' + (e.message || '未知错误'), 'er')
  }
  setLd('energy', false)
}

async function getCmdList() {
  setLd('cmd', true)
  try {
    const p = {}
    if (q.value.cmd.code) p.equipmentCode = q.value.cmd.code
    const data = await api.command.get(p)
    lists.value.cmd = data.data || data || []
    toast(`查询到 ${lists.value.cmd.length} 条指令`)
  } catch (e) {
    toast('查询失败：' + (e.message || '未知错误'), 'er')
  }
  setLd('cmd', false)
}

async function postCmd() {
  if (!f.value.cmd.equipmentCode || !f.value.cmd.commandType) {
    toast('请填写设备编号和指令类型', 'er')
    return
  }
  setLd('cmd', true)
  try {
    const p = {
      equipmentCode: f.value.cmd.equipmentCode,
      commandType: f.value.cmd.commandType
    }
    if (f.value.cmd.commandValue) p.commandValue = f.value.cmd.commandValue
    const data = await api.command.post(p)
    if (data.success !== false) {
      toast('指令创建成功', 'ok')
      await getCmdList()
    } else {
      toast('创建失败', 'er')
    }
  } catch (e) {
    toast('请求失败：' + (e.message || '未知错误'), 'er')
  }
  setLd('cmd', false)
}

async function updateCmd() {
  if (!f.value.cmdUpdate.id) {
    toast('请输入指令ID', 'er')
    return
  }
  setLd('cmd', true)
  try {
    const body = {
      status: parseInt(f.value.cmdUpdate.status)
    }
    if (f.value.cmdUpdate.responseMessage) body.responseMessage = f.value.cmdUpdate.responseMessage
    const data = await api.command.update(f.value.cmdUpdate.id, body)
    if (data.success !== false) {
      toast('状态更新成功', 'ok')
      await getCmdList()
    } else {
      toast('更新失败', 'er')
    }
  } catch (e) {
    toast('请求失败：' + (e.message || '未知错误'), 'er')
  }
  setLd('cmd', false)
}

async function getEqList() {
  setLd('eq', true)
  try {
    const p = {}
    if (q.value.eq.type) p.equipmentType = q.value.eq.type
    const data = await api.equipment.get(p)
    lists.value.eq = data.data || data || []
    toast(`查询到 ${lists.value.eq.length} 台设备`)
  } catch (e) {
    toast('查询失败：' + (e.message || '未知错误'), 'er')
  }
  setLd('eq', false)
}

async function submitEq() {
  setLd('eq', true)
  try {
    const body = { ...f.value.eq }
    const data = t.value.eq === 'add'
      ? await api.equipment.post(body)
      : await api.equipment.put(body)
    if (data.success !== false) {
      toast(t.value.eq === 'add' ? '设备添加成功' : '设备更新成功', 'ok')
      await getEqList()
    } else {
      toast('操作失败', 'er')
    }
  } catch (e) {
    toast('请求失败：' + (e.message || '未知错误'), 'er')
  }
  setLd('eq', false)
}

async function deleteEq() {
  if (!q.value.eqDel.code) {
    toast('请输入设备编号', 'er')
    return
  }
  setLd('eq', true)
  try {
    const data = await api.equipment.delete(q.value.eqDel.code)
    if (data.success !== false) {
      toast('设备已删除', 'ok')
      await getEqList()
    } else {
      toast('删除失败', 'er')
    }
  } catch (e) {
    toast('请求失败：' + (e.message || '未知错误'), 'er')
  }
  setLd('eq', false)
}

function alarmLevelClass(level) {
  const map = { 1: 'b-w1', 2: 'b-w2', 3: 'b-w3' }
  return map[level] || 'b-w1'
}

function alarmLevelLabel(level) {
  const map = { 1: '警告', 2: '严重', 3: '紧急' }
  return map[level] || '未知'
}

function cmdStatusClass(status) {
  const map = { 0: 'b-c0', 1: 'b-c1', 2: 'b-c2', 3: 'b-c3' }
  return map[status] || 'b-c0'
}

function cmdStatusLabel(status) {
  const map = { 0: '待执行', 1: '已发送', 2: '执行成功', 3: '执行失败' }
  return map[status] || '未知'
}

const statsData = ref([
  { value: '8+', label: '功能模块', fill: 80 },
  { value: '25+', label: 'API接口', fill: 85 },
  { value: 'REST', label: '接口规范', fill: 100 },
  { value: '6', label: '数据链路', fill: 75 }
])

const featuresData = ref([
  { icon: '📡', title: '实时监控', desc: '设备状态实时监控，毫秒级数据更新', api: '/api/plc-data' },
  { icon: '🔔', title: '报警处理', desc: '报警事件实时推送，快速响应处理', api: '/api/monitor/alarm' },
  { icon: '⚡', title: '能耗管理', desc: '能耗智能管理，节能减排优化', api: '/api/monitor/energy' },
  { icon: '🎮', title: '远程控制', desc: '控制指令下发，远程设备控制', api: '/api/control/command' }
])

const dataFeatures = ref([
  { icon: '🔢', title: 'NumPy 学习', desc: '第2章：NumPy 数组操作全面讲解，含交互式可视化', tag: 'CH2' },
  { icon: '📈', title: '股票分析', desc: '收益率与波动率计算，金融数据可视化交互系统', tag: 'CASE' },
  { icon: '🧊', title: '数组可视化', desc: '多维数组结构交互式展示，直观理解 reshape/transpose', tag: 'VIS' },
  { icon: '🤖', title: 'AI-Coding', desc: 'CodeBuddy + CLI 推进 AI 辅助数据分析与代码调试', tag: 'AI' }
])

const flowSteps = ref([
  { icon: '⚙️', name: 'PLC', description: '可编程逻辑控制器，采集现场设备数据', protocol: 'OPC UA' },
  { icon: '🐍', name: 'Python', description: '数据采集服务，实时数据处理', protocol: 'HTTP/REST' },
  { icon: '💾', name: 'MySQL', description: '关系型数据库，持久化存储', protocol: 'MySQL' },
  { icon: '🔧', name: '后端', description: 'Java服务层，业务逻辑处理', protocol: 'API' },
  { icon: '🎨', name: '前端', description: '可视化展示，实时监控大屏', protocol: 'WebSocket' }
])

const panelTitle = computed(() => {
  const titles = {
    health: 'HEALTH CHECK',
    dashboard: 'DASHBOARD',
    plcdata: 'PLC DATA',
    alarm: 'ALARM',
    energy: 'ENERGY',
    command: 'CONTROL',
    equipment: 'EQUIPMENT',
    numpylearn: 'NUMPY CH2',
    stock: 'STOCK ANALYSIS'
  }
  return titles[panel.value] || panel.value
})

let timer = null

function scrollToFeatures() {
  const element = document.getElementById('features')
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
  }
}

function goToDashboard() {
  pageDirection.value = 'next'
  if (firstEnter.value) {
    firstEnter.value = false
    page.value = 'dashboard'
  } else {
    page.value = 'dashboard'
  }
}

function goToIntro() {
  pageDirection.value = 'prev'
  page.value = 'intro'
}

async function doHealth() {
  setLd('health', true)
  try {
    const data = await plcApi.health()
    health.value = { s: data.status || 'DOWN', t: data.timestamp || '', svc: data.service || '' }
    hr.value = data.status === 'UP' ? '✓ SERVICE ONLINE' : '✗ SERVICE OFFLINE'
    toast(data.status === 'UP' ? 'SERVICE ONLINE' : 'SERVICE OFFLINE', data.status === 'UP' ? 'ok' : 'er')
  } catch (e) {
    hr.value = 'CONNECTION FAILED'
    health.value.s = 'DOWN'
    toast('CONNECTION FAILED', 'er')
  }
  setLd('health', false)
}

async function getPlcDataList() {
  setLd('plcdata', true)
  try {
    const data = await plcApi.listPage(plcDataPage.value, plcDataSize.value)
    plcDataList.value = data.items || data || []
    plcDataTotal.value = data.total || plcDataList.value.length
    toast(`FETCHED ${plcDataList.value.length} RECORDS`)
  } catch (e) {
    toast('QUERY FAILED: ' + (e.message || 'UNKNOWN'), 'er')
  }
  setLd('plcdata', false)
}

async function getPlcDataById() {
  if (!queryId.value) {
    toast('ENTER RECORD ID', 'er')
    return
  }
  setLd('plcdata', true)
  try {
    const data = await plcApi.getById(queryId.value)
    if (data) {
      currentPlcData.value = data
      toast('QUERY SUCCESS')
    } else {
      currentPlcData.value = null
      toast('RECORD NOT FOUND', 'er')
    }
  } catch (e) {
    toast('QUERY FAILED: ' + (e.message || 'UNKNOWN'), 'er')
  }
  setLd('plcdata', false)
}

function selectService(index) {
  const step = flowSteps.value[index]
  if (step) {
    page.value = 'dashboard'
    panel.value = 'dashboard'
  }
}

function statusLabel(status) {
  const map = { 0: 'OFFLINE', 1: 'RUNNING', 2: 'STANDBY', 3: 'FAULT' }
  return map[status] || `STATUS ${status}`
}

function statusClass(status) {
  const map = { 0: 'offline', 1: 'running', 2: 'standby', 3: 'fault' }
  return map[status] || 'offline'
}

function prevPage() {
  if (plcDataPage.value > 0) {
    plcDataPage.value--
    getPlcDataList()
  }
}

function nextPage() {
  const maxPage = Math.max(0, Math.ceil(plcDataTotal.value / plcDataSize) - 1)
  if (plcDataPage.value < maxPage) {
    plcDataPage.value++
    getPlcDataList()
  }
}

onMounted(() => {
  timer = setInterval(() => {
    now.value = new Date().toLocaleString('zh-CN', {
      year: 'numeric', month: '2-digit', day: '2-digit',
      hour: '2-digit', minute: '2-digit', second: '2-digit'
    })
  }, 1000)

  if (page.value === 'dashboard') {
    doHealth()
    getAlarmCount()
  }
})

watch(page, (p) => {
  if (p === 'dashboard') {
    doHealth()
    getAlarmCount()
  }
})

function handleScroll() {
  const sections = document.querySelectorAll('.scroll-animate')
  sections.forEach((section) => {
    const rect = section.getBoundingClientRect()
    const id = section.getAttribute('data-section-id')
    if (rect.top < window.innerHeight * 0.85 && rect.bottom > 0) {
      visibleSections.value[id] = true
    }
  })
}

onMounted(() => {
  timer = setInterval(() => {
    now.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
  }, 1000)
  doHealth()
  getAlarmCount()
  
  setTimeout(() => {
    handleScroll()
    window.addEventListener('scroll', handleScroll, { passive: true })
  }, 100)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <div id="app">
    <div class="toast-w">
      <div v-for="t in toasts" :key="t.id" :class="['toast', t.type]">{{ t.msg }}</div>
    </div>

    <transition :name="pageDirection === 'prev' ? 'slide-right' : ''" mode="out-in">
      <div v-if="page === 'intro'" key="intro" class="page-wrapper">
      <div class="hero-bg-text">NEXUS</div>
      <div class="hero-bg-grid"></div>

      <nav class="i-nav">
        <div class="i-logo">
          <span class="logo-icon">◈</span>
          <span class="logo-text">NEXUS</span>
          <span class="logo-accent">PLC SYSTEM</span>
        </div>
        <div class="i-nav-links">
          <a href="#features" class="nav-link">FEATURES</a>
          <a href="#services" class="nav-link">SERVICES</a>
          <a href="#stats" class="nav-link">STATS</a>
        </div>
        <div class="i-badge">v1.0.0</div>
      </nav>

      <section class="hero-section">
        <div class="hero-left">
          <div class="hero-eyebrow">INDUSTRIAL CYBER CONTROL SYSTEM</div>
          <h1 class="hero-title">
            <span class="t-neon">INDUSTRIAL</span>
            <span class="t-neon">NEXUS</span>
            <span class="t-dim">DATA MONITOR SYSTEM</span>
          </h1>
          <p class="hero-desc">
            全栈工业物联网平台 · 集成<span>设备状态监控</span>、<span>生产数据采集</span>、<span>能耗管理</span>、<span>报警处理</span>及<span>控制指令下发</span>等核心功能模块。
          </p>
          <div class="hero-actions">
            <button class="btn-primary" @click="goToDashboard">
              LAUNCH SYSTEM
            </button>
            <button class="btn-secondary" @click="scrollToFeatures">
              LEARN MORE
            </button>
          </div>
        </div>

        <div class="hero-right">
          <div class="plc-visual">
            <div class="visual-ring ring-1"></div>
            <div class="visual-ring ring-2"></div>
            <div class="visual-ring ring-3"></div>
            <div class="visual-center">
              <div class="center-icon">◉</div>
            </div>
            <div class="floating-data">
              <div class="data-item" v-for="i in 6" :key="i" :style="{ '--delay': i * 0.2 + 's', '--x': (i % 3 - 1) * 120 + 'px', '--y': (i > 3 ? -80 : 80) + 'px' }">
                <span class="data-value">{{ Math.floor(Math.random() * 100) }}%</span>
                <span class="data-label">DATA {{ i }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <div id="stats" class="intro-stats">
        <div class="stat-block" v-for="stat in statsData" :key="stat.label">
          <div class="sb-num">{{ stat.value }}</div>
          <div class="sb-lbl">{{ stat.label }}</div>
          <div class="sb-bar"><div class="sb-fill" :style="{ width: stat.fill + '%' }"></div></div>
        </div>
      </div>

      <section id="features" class="modules-section">
        <div class="section-header">
          <span class="section-label">MODULES</span>
          <div class="section-line"></div>
          <span class="section-num">04 / 06</span>
        </div>
        <div class="mods-grid">
          <div class="mod-tile" v-for="feature in featuresData" :key="feature.title" @click="goToDashboard(); panel='plcdata'">
            <div class="mt-corner"></div>
            <span class="mt-icon">{{ feature.icon }}</span>
            <div class="mt-name">{{ feature.title }}</div>
            <div class="mt-desc">{{ feature.desc }}</div>
            <span class="mt-api">{{ feature.api }}</span>
          </div>
        </div>
      </section>

      <section class="modules-section data-learn-section">
        <div class="section-header">
          <span class="section-label">DATA ANALYSIS</span>
          <div class="section-line"></div>
          <span class="section-num">04 MODULES</span>
        </div>
        <div class="data-learn-intro">
          <p class="data-learn-desc">
            基于 <span>CodeBuddy</span> 及其 CLI 工具，在 AI-Coding 框架下推进<span>数据分析与数据挖掘</span>学习。
            本项目涵盖 NumPy 第2章完整知识体系，从数组创建到金融案例实战，并提供多维数组结构的交互式可视化。
            参考 <span>Mermaid.js</span> 流程图 & <span>Manim</span> 动画引擎的设计理念，辅助源代码理解。
          </p>
        </div>
        <div class="mods-grid">
          <div class="mod-tile" v-for="df in dataFeatures" :key="df.title" @click="goToDashboard(); panel='numpylearn'">
            <div class="mt-corner"></div>
            <span class="mt-icon">{{ df.icon }}</span>
            <div class="mt-name">{{ df.title }}</div>
            <div class="mt-desc">{{ df.desc }}</div>
            <span class="mt-tag">{{ df.tag }}</span>
          </div>
        </div>
      </section>

      <section id="services" class="modules-section">
        <div class="section-header">
          <span class="section-label">DATA FLOW</span>
          <div class="section-line"></div>
          <span class="section-num">05 NODES</span>
        </div>
        <div class="flow-container">
          <template v-for="(step, index) in flowSteps" :key="step.name">
            <div class="flow-step" @click="selectService(index)">
              <div class="flow-icon">
                <span>{{ step.icon }}</span>
                <div class="flow-pulse"></div>
              </div>
              <div class="flow-info">
                <div class="flow-name">{{ step.name }}</div>
                <div class="flow-desc">{{ step.description }}</div>
                <div class="flow-protocol">{{ step.protocol }}</div>
              </div>
            </div>
            <div v-if="index < flowSteps.length - 1" class="flow-arrow">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M5 12h14M12 5l7 7-7 7"/>
              </svg>
            </div>
          </template>
        </div>
      </section>

      <section class="translation-section">
        <div class="section-header">
          <span class="section-label">GLOSSARY</span>
          <div class="section-line"></div>
          <span class="section-num">TERMS</span>
        </div>
        <div class="glossary-grid">
          <div class="glossary-item">
            <div class="gi-en">Real-time Monitoring</div>
            <div class="gi-zh">实时监控</div>
            <div class="gi-desc">设备状态实时监控，毫秒级数据更新</div>
          </div>
          <div class="glossary-item">
            <div class="gi-en">Alarm Management</div>
            <div class="gi-zh">报警管理</div>
            <div class="gi-desc">报警事件实时推送，快速响应处理</div>
          </div>
          <div class="glossary-item">
            <div class="gi-en">Energy Consumption</div>
            <div class="gi-zh">能耗管理</div>
            <div class="gi-desc">能耗智能管理，节能减排优化</div>
          </div>
          <div class="glossary-item">
            <div class="gi-en">Remote Control</div>
            <div class="gi-zh">远程控制</div>
            <div class="gi-desc">控制指令下发，远程设备控制</div>
          </div>
        </div>
      </section>

      <section class="cta-section">
        <div class="cta-content">
          <h2 class="cta-title">READY TO LAUNCH?</h2>
          <p class="cta-desc">进入系统，体验工业数据监控的强大功能</p>
          <button class="cta-btn" @click="goToDashboard">
            START NOW
          </button>
        </div>
      </section>

      <footer class="intro-footer">
        <div class="foot-copy">NEXUS-PLC INDUSTRIAL CONTROL SYSTEM</div>
        <div class="foot-ver">v1.0.0</div>
      </footer>
    </div>
    
    <div v-else-if="page === 'dashboard'" key="dashboard" class="dash">
      <div class="dash-top">
            <span class="top-brand">NEXUS</span>
            <div class="top-divider"></div>
            <span class="top-page-title">{{ panelTitle }}</span>
            <div class="top-spacer"></div>
            <span class="top-clock">{{ now }}</span>
            <div class="top-status">
              <span class="status-dot"></span>
              {{ health.s === 'UP' ? 'ONLINE' : 'OFFLINE' }}
            </div>
            <button class="top-btn" @click="goToIntro">← HOME</button>
          </div>

      <div class="icon-sidebar">
        <div class="ic-btn" :class="{ active: panel === 'health' }" @click="panel='health'">
          <span class="ic-icon">⚡</span>
          <span class="ic-lbl">HEALTH</span>
        </div>
        <div class="ic-sep"></div>
        <div class="ic-btn" :class="{ active: panel === 'dashboard' }" @click="panel='dashboard'">
          <span class="ic-icon">🖥️</span>
          <span class="ic-lbl">SCREEN</span>
        </div>
        <div class="ic-btn" :class="{ active: panel === 'plcdata' }" @click="panel='plcdata'">
          <span class="ic-icon">📊</span>
          <span class="ic-lbl">PLC</span>
        </div>
        <div class="ic-btn" :class="{ active: panel === 'alarm' }" @click="panel='alarm'">
          <span class="ic-icon">🔔</span>
          <span v-if="unhandled > 0" class="ic-badge">{{ unhandled }}</span>
          <span class="ic-lbl">ALARM</span>
        </div>
        <div class="ic-btn" :class="{ active: panel === 'energy' }" @click="panel='energy'">
          <span class="ic-icon">⚡</span>
          <span class="ic-lbl">ENERGY</span>
        </div>
        <div class="ic-sep"></div>
        <div class="ic-btn" :class="{ active: panel === 'command' }" @click="panel='command'">
          <span class="ic-icon">🎮</span>
          <span class="ic-lbl">CONTROL</span>
        </div>
        <div class="ic-btn" :class="{ active: panel === 'equipment' }" @click="panel='equipment'">
          <span class="ic-icon">🔧</span>
          <span class="ic-lbl">EQUIP</span>
        </div>
        <div class="ic-sep"></div>
        <div class="ic-btn" :class="{ active: panel === 'numpylearn' }" @click="panel='numpylearn'">
          <span class="ic-icon">📊</span>
          <span class="ic-lbl">NUMPY</span>
        </div>
        <div class="ic-btn" :class="{ active: panel === 'stock' }" @click="panel='stock'">
          <span class="ic-icon">📈</span>
          <span class="ic-lbl">STOCK</span>
        </div>
      </div>

      <div class="dash-main">
        <!-- HEALTH PANEL -->
        <div class="dpanel" :class="{ active: panel === 'health' }">
          <div class="dpanel-card">
            <div class="dc-head">
              <span class="dc-title">SERVICE HEALTH CHECK</span>
              <span class="dc-badge" :class="{ live: health.s === 'UP' }">{{ health.s === 'UP' ? 'LIVE' : 'DOWN' }}</span>
            </div>
            <div class="fr">
              <button class="btn bp" @click="doHealth" :disabled="ld.health">
                <span v-if="ld.health" class="ld"></span>
                <span v-else>GET /api/health</span>
              </button>
            </div>
            <div class="res-lbl">RESPONSE</div>
            <div :class="['res', health.s === 'UP' ? 'ok' : health.s ? 'er' : '']">{{ hr }}</div>
            <div class="g3" style="margin-top: 20px">
              <div class="ms">
                <div class="ms-v" :style="{ color: health.s === 'UP' ? 'var(--neon2)' : 'var(--plasma)' }">{{ health.s || '--' }}</div>
                <div class="ms-l">STATUS</div>
              </div>
              <div class="ms">
                <div class="ms-v" style="font-size: 13px">{{ health.t ? health.t.substring(0, 19) : '--' }}</div>
                <div class="ms-l">TIMESTAMP</div>
              </div>
              <div class="ms">
                <div class="ms-v" style="font-size: 11px">{{ health.svc || 'N/A' }}</div>
                <div class="ms-l">SERVICE</div>
              </div>
            </div>
          </div>
        </div>

        <!-- DASHBOARD SCREEN -->
        <div class="dpanel" :class="{ active: panel === 'dashboard' }">
          <DashboardScreen :active="panel === 'dashboard'" :now="now" />
        </div>

        <!-- PLC DATA PANEL -->
        <div class="dpanel" :class="{ active: panel === 'plcdata' }">
          <div class="dpanel-card">
            <div class="dc-head">
              <span class="dc-title">PLC DATA LIST</span>
              <span class="dc-badge">GET /api/plc-data</span>
            </div>
            <div class="fr">
              <div class="fg">
                <label>PAGE</label>
                <input class="fi" type="number" v-model.number="plcDataPage" min="0">
              </div>
              <div class="fg">
                <label>SIZE</label>
                <input class="fi" type="number" v-model.number="plcDataSize" min="1" max="100">
              </div>
              <div class="fg" style="justify-content: flex-end">
                <label>&nbsp;</label>
                <button class="btn bp" @click="getPlcDataList" :disabled="ld.plcdata">
                  <span v-if="ld.plcdata" class="ld"></span>
                  <span v-else>QUERY</span>
                </button>
              </div>
            </div>
            <div class="fr" style="gap: 0.5rem">
              <button class="btn bo bsm" @click="prevPage" :disabled="plcDataPage <= 0">← PREV</button>
              <span class="pager-info">PAGE {{ plcDataPage + 1 }} / {{ Math.max(1, Math.ceil(plcDataTotal / plcDataSize)) }}</span>
              <button class="btn bo bsm" @click="nextPage" :disabled="plcDataPage >= Math.max(0, Math.ceil(plcDataTotal / plcDataSize) - 1)">NEXT →</button>
              <span class="pager-info">{{ plcDataTotal }} TOTAL</span>
            </div>
            <table class="tbl plc-history-tbl">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>TIME</th>
                  <th>STATUS</th>
                  <th>TEMP</th>
                  <th>HUMID</th>
                  <th>PRESS</th>
                  <th>EFF</th>
                  <th>QUALITY</th>
                  <th>ALARM</th>
                  <th>SOURCE</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="d in plcDataList" :key="d.id" :class="{ 'row-alarm': d.alarmActive }">
                  <td class="mono">{{ d.id }}</td>
                  <td class="mono ts-col">{{ d.timestamp }}</td>
                  <td><span :class="['status-badge', statusClass(d.status)]">{{ statusLabel(d.status) }}</span></td>
                  <td>{{ d.temperature !== undefined ? d.temperature : '--' }}℃</td>
                  <td>{{ d.humidity !== undefined ? d.humidity : '--' }}%</td>
                  <td>{{ d.pressure !== undefined ? d.pressure : '--' }}</td>
                  <td>{{ d.efficiency !== undefined ? d.efficiency : '--' }}%</td>
                  <td>{{ d.qualityRate !== undefined ? d.qualityRate : '--' }}%</td>
                  <td>
                    <span v-if="d.alarmActive" class="badge b-flt">L{{ d.alarmLevel }}</span>
                    <span v-else class="badge b-ok">OK</span>
                  </td>
                  <td>{{ d.source || '--' }}</td>
                </tr>
                <tr v-if="!plcDataList.length && !ld.plcdata">
                  <td colspan="10" class="empty">NO DATA - CLICK QUERY</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="dpanel-card">
            <div class="dc-head">
              <span class="dc-title">QUERY BY ID</span>
              <span class="dc-badge">GET /api/plc-data/{id}</span>
            </div>
            <div class="fr">
              <div class="fg">
                <label>RECORD ID</label>
                <input class="fi" type="number" v-model.number="queryId" placeholder="1" @keyup.enter="getPlcDataById">
              </div>
              <div class="fg" style="justify-content: flex-end">
                <label>&nbsp;</label>
                <button class="btn bp" @click="getPlcDataById" :disabled="ld.plcdata">
                  <span v-if="ld.plcdata" class="ld"></span>
                  <span v-else>QUERY</span>
                </button>
              </div>
            </div>
            <div v-if="currentPlcData" class="res ok detail-json">
              <pre>{{ JSON.stringify(currentPlcData, null, 2) }}</pre>
            </div>
          </div>
        </div>

        <!-- ALARM PANEL -->
        <div class="dpanel" :class="{ active: panel === 'alarm' }">
          <div class="dpanel-card">
            <div class="dc-head">
              <span class="dc-title">NEW ALARM</span>
              <span class="dc-badge">POST /api/monitor/alarm</span>
            </div>
            <div class="fr">
              <div class="fg"><label>EQUIP CODE</label><input class="fi" v-model="f.alarm.equipmentCode" placeholder="EQ-001"></div>
              <div class="fg"><label>ALARM CODE</label><input class="fi" v-model="f.alarm.alarmCode" placeholder="TEMP_OVER"></div>
              <div class="fg"><label>LEVEL</label>
                <select class="fi" v-model="f.alarm.alarmLevel">
                  <option value="1">WARNING</option>
                  <option value="2">SEVERE</option>
                  <option value="3">CRITICAL</option>
                </select>
              </div>
            </div>
            <div class="fr">
              <div class="fg"><label>MESSAGE</label><input class="fi" v-model="f.alarm.alarmMessage" placeholder="Temperature exceeded threshold"></div>
              <div class="fg" style="justify-content: flex-end"><label>&nbsp;</label><button class="btn bp" @click="postAlarm" :disabled="ld.alarm"><span v-if="ld.alarm" class="ld"></span><span v-else>SUBMIT</span></button></div>
            </div>
          </div>

          <div class="dpanel-card">
            <div class="dc-head">
              <span class="dc-title">ALARM LIST</span>
              <span class="dc-badge">GET /api/monitor/alarm</span>
            </div>
            <div class="fr">
              <div class="fg"><label>STATUS</label>
                <select class="fi" v-model="q.alarm.status">
                  <option value="">ALL</option>
                  <option value="0">PENDING</option>
                  <option value="1">HANDLED</option>
                </select>
              </div>
              <div class="fg" style="justify-content: flex-end"><label>&nbsp;</label><button class="btn bp" @click="getAlarmList" :disabled="ld.alarm"><span v-if="ld.alarm" class="ld"></span><span v-else>QUERY</span></button></div>
            </div>
            <table class="tbl">
              <thead>
                <tr><th>ID</th><th>EQUIP</th><th>CODE</th><th>LEVEL</th><th>MESSAGE</th><th>STATUS</th><th>TIME</th></tr>
              </thead>
              <tbody>
                <tr v-for="a in lists.alarm" :key="a.id">
                  <td class="mono">{{ a.id }}</td>
                  <td>{{ a.equipmentCode || '--' }}</td>
                  <td>{{ a.alarmCode || '--' }}</td>
                  <td><span :class="['badge', alarmLevelClass(a.alarmLevel)]">{{ alarmLevelLabel(a.alarmLevel) }}</span></td>
                  <td>{{ a.alarmMessage || '--' }}</td>
                  <td><span :class="['badge', a.status === 0 ? 'b-flt' : 'b-ok']">{{ a.status === 0 ? 'PENDING' : 'HANDLED' }}</span></td>
                  <td class="mono ts-col">{{ a.createTime || '--' }}</td>
                </tr>
                <tr v-if="!lists.alarm.length && !ld.alarm"><td colspan="7" class="empty">NO DATA</td></tr>
              </tbody>
            </table>
          </div>

          <div class="dpanel-card">
            <div class="dc-head">
              <span class="dc-title">HANDLE ALARM</span>
              <span class="dc-badge">PUT /api/monitor/alarm/{id}/handle</span>
            </div>
            <div class="fr">
              <div class="fg"><label>ALARM ID</label><input class="fi" type="number" v-model="f.handle.alarmId" placeholder="1"></div>
              <div class="fg"><label>HANDLER</label><input class="fi" v-model="f.handle.handleUser" placeholder="Zhang San"></div>
              <div class="fg"><label>NOTE</label><input class="fi" v-model="f.handle.handleNote" placeholder="Inspected and handled"></div>
              <div class="fg" style="justify-content: flex-end"><label>&nbsp;</label><button class="btn bp" @click="handleAlarm" :disabled="ld.alarm"><span v-if="ld.alarm" class="ld"></span><span v-else>CONFIRM</span></button></div>
            </div>
          </div>
        </div>

        <!-- ENERGY PANEL -->
        <div class="dpanel" :class="{ active: panel === 'energy' }">
          <div class="dpanel-card">
            <div class="dc-head">
              <span class="dc-title">REPORT ENERGY</span>
              <span class="dc-badge">POST /api/monitor/energy</span>
            </div>
            <div class="fr">
              <div class="fg"><label>EQUIP CODE</label><input class="fi" v-model="f.energy.equipmentCode" placeholder="EQ-001"></div>
              <div class="fg"><label>ELECTRICITY kWh</label><input class="fi" type="number" step="0.01" v-model="f.energy.electricity" placeholder="125.5"></div>
              <div class="fg"><label>WATER m³</label><input class="fi" type="number" step="0.01" v-model="f.energy.water" placeholder="10.2"></div>
              <div class="fg"><label>GAS m³</label><input class="fi" type="number" step="0.01" v-model="f.energy.gas" placeholder="5.8"></div>
              <div class="fg" style="justify-content: flex-end"><label>&nbsp;</label><button class="btn bp" @click="postEnergy" :disabled="ld.energy"><span v-if="ld.energy" class="ld"></span><span v-else>REPORT</span></button></div>
            </div>
          </div>

          <div class="dpanel-card">
            <div class="dc-head">
              <span class="dc-title">QUERY HISTORY</span>
              <span class="dc-badge">GET /api/monitor/energy</span>
            </div>
            <div class="fr">
              <div class="fg"><label>EQUIP CODE</label><input class="fi" v-model="q.energy.code" placeholder="EQ-001"></div>
              <div class="fg"><label>LIMIT</label><input class="fi" type="number" v-model="q.energy.limit" placeholder="100"></div>
              <div class="fg" style="justify-content: flex-end"><label>&nbsp;</label><button class="btn bp" @click="getEnergyList" :disabled="ld.energy"><span v-if="ld.energy" class="ld"></span><span v-else>QUERY</span></button></div>
            </div>
            <table class="tbl">
              <thead>
                <tr><th>ID</th><th>EQUIP</th><th>ELEC(kWh)</th><th>WATER(m³)</th><th>GAS(m³)</th><th>TIME</th></tr>
              </thead>
              <tbody>
                <tr v-for="e in lists.energy" :key="e.id">
                  <td class="mono">{{ e.id }}</td>
                  <td>{{ e.equipmentCode || '--' }}</td>
                  <td>{{ e.electricity ?? '--' }}</td>
                  <td>{{ e.water ?? '--' }}</td>
                  <td>{{ e.gas ?? '--' }}</td>
                  <td class="mono ts-col">{{ e.createTime || '--' }}</td>
                </tr>
                <tr v-if="!lists.energy.length && !ld.energy"><td colspan="6" class="empty">NO DATA</td></tr>
              </tbody>
            </table>
          </div>

          <div class="dpanel-card">
            <div class="dc-head">
              <span class="dc-title">PERIOD STATS</span>
              <span class="dc-badge">GET /api/monitor/energy/total</span>
            </div>
            <div class="fr">
              <div class="fg"><label>EQUIP CODE</label><input class="fi" v-model="q.energyTotal.code" placeholder="EQ-001"></div>
              <div class="fg"><label>START</label><input class="fi" type="datetime-local" v-model="q.energyTotal.start"></div>
              <div class="fg"><label>END</label><input class="fi" type="datetime-local" v-model="q.energyTotal.end"></div>
              <div class="fg" style="justify-content: flex-end"><label>&nbsp;</label><button class="btn bp" @click="getEnergyTotal" :disabled="ld.energy"><span v-if="ld.energy" class="ld"></span><span v-else>STATS</span></button></div>
            </div>
          </div>
        </div>

        <!-- COMMAND PANEL -->
        <div class="dpanel" :class="{ active: panel === 'command' }">
          <div class="dpanel-card">
            <div class="dc-head">
              <span class="dc-title">CREATE COMMAND</span>
              <span class="dc-badge">POST /api/control/command</span>
            </div>
            <div class="fr">
              <div class="fg"><label>EQUIP CODE</label><input class="fi" v-model="f.cmd.equipmentCode" placeholder="EQ-001"></div>
              <div class="fg"><label>TYPE</label><input class="fi" v-model="f.cmd.commandType" placeholder="START/STOP/SET_SPEED"></div>
              <div class="fg"><label>VALUE</label><input class="fi" v-model="f.cmd.commandValue" placeholder="50"></div>
              <div class="fg" style="justify-content: flex-end"><label>&nbsp;</label><button class="btn bp" @click="postCmd" :disabled="ld.cmd"><span v-if="ld.cmd" class="ld"></span><span v-else>CREATE</span></button></div>
            </div>
          </div>

          <div class="dpanel-card">
            <div class="dc-head">
              <span class="dc-title">COMMAND LIST</span>
              <span class="dc-badge">GET /api/control/command</span>
            </div>
            <div class="fr">
              <div class="fg"><label>EQUIP CODE</label><input class="fi" v-model="q.cmd.code" placeholder="Filter by equip"></div>
              <div class="fg" style="justify-content: flex-end"><label>&nbsp;</label><button class="btn bp" @click="getCmdList" :disabled="ld.cmd"><span v-if="ld.cmd" class="ld"></span><span v-else>QUERY</span></button></div>
            </div>
            <table class="tbl">
              <thead>
                <tr><th>ID</th><th>EQUIP</th><th>TYPE</th><th>VALUE</th><th>STATUS</th><th>RESPONSE</th><th>TIME</th></tr>
              </thead>
              <tbody>
                <tr v-for="c in lists.cmd" :key="c.id">
                  <td class="mono">{{ c.id }}</td>
                  <td>{{ c.equipmentCode || '--' }}</td>
                  <td>{{ c.commandType || '--' }}</td>
                  <td>{{ c.commandValue || '--' }}</td>
                  <td><span :class="['badge', cmdStatusClass(c.status)]">{{ cmdStatusLabel(c.status) }}</span></td>
                  <td style="max-width: 150px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap">{{ c.responseMessage || '--' }}</td>
                  <td class="mono ts-col">{{ c.createTime || '--' }}</td>
                </tr>
                <tr v-if="!lists.cmd.length && !ld.cmd"><td colspan="7" class="empty">NO DATA</td></tr>
              </tbody>
            </table>
          </div>

          <div class="dpanel-card">
            <div class="dc-head">
              <span class="dc-title">UPDATE STATUS</span>
              <span class="dc-badge">PUT /api/control/command/{id}</span>
            </div>
            <div class="fr">
              <div class="fg"><label>CMD ID</label><input class="fi" type="number" v-model="f.cmdUpdate.id" placeholder="1"></div>
              <div class="fg"><label>STATUS</label>
                <select class="fi" v-model="f.cmdUpdate.status">
                  <option value="1">SENT</option>
                  <option value="2">SUCCESS</option>
                  <option value="3">FAILED</option>
                </select>
              </div>
              <div class="fg"><label>RESPONSE</label><input class="fi" v-model="f.cmdUpdate.responseMessage" placeholder="Execution completed"></div>
              <div class="fg" style="justify-content: flex-end"><label>&nbsp;</label><button class="btn bp" @click="updateCmd" :disabled="ld.cmd"><span v-if="ld.cmd" class="ld"></span><span v-else>UPDATE</span></button></div>
            </div>
          </div>
        </div>

        <!-- EQUIPMENT PANEL -->
        <div class="dpanel" :class="{ active: panel === 'equipment' }">
          <div class="dpanel-card">
            <div class="dc-head">
              <span class="dc-title">EQUIPMENT FORM</span>
              <span class="dc-badge">POST/PUT /api/equipment</span>
            </div>
            <div class="fr">
              <div class="fg"><label>CODE</label><input class="fi" v-model="f.eq.equipmentCode" placeholder="EQ-001" :disabled="t.eq === 'edit'"></div>
              <div class="fg"><label>NAME</label><input class="fi" v-model="f.eq.equipmentName" placeholder="Line 1"></div>
              <div class="fg"><label>TYPE</label><input class="fi" v-model="f.eq.equipmentType" placeholder="PLC/SENSOR"></div>
            </div>
            <div class="fr">
              <div class="fg"><label>LOCATION</label><input class="fi" v-model="f.eq.location" placeholder="Workshop A"></div>
              <div class="fg"><label>IP</label><input class="fi" v-model="f.eq.ipAddress" placeholder="192.168.1.100"></div>
              <div class="fg"><label>DESC</label><input class="fi" v-model="f.eq.description" placeholder="Notes"></div>
              <div class="fg" style="justify-content: flex-end">
                <label>&nbsp;</label>
                <div style="display: flex; gap: 0.5rem">
                  <button class="btn bo" @click="t.eq='add'; f.eq={equipmentCode:'',equipmentName:'',equipmentType:'',location:'',ipAddress:'',description:''}">ADD</button>
                  <button class="btn bp" @click="submitEq" :disabled="ld.eq"><span v-if="ld.eq" class="ld"></span><span v-else>SUBMIT</span></button>
                </div>
              </div>
            </div>
          </div>

          <div class="dpanel-card">
            <div class="dc-head">
              <span class="dc-title">EQUIPMENT LIST</span>
              <span class="dc-badge">GET /api/equipment</span>
            </div>
            <div class="fr">
              <div class="fg"><label>TYPE FILTER</label><input class="fi" v-model="q.eq.type" placeholder="PLC/SENSOR"></div>
              <div class="fg" style="justify-content: flex-end"><label>&nbsp;</label><button class="btn bp" @click="getEqList" :disabled="ld.eq"><span v-if="ld.eq" class="ld"></span><span v-else>QUERY</span></button></div>
            </div>
            <table class="tbl">
              <thead>
                <tr><th>CODE</th><th>NAME</th><th>TYPE</th><th>LOCATION</th><th>IP</th><th>DESC</th></tr>
              </thead>
              <tbody>
                <tr v-for="e in lists.eq" :key="e.equipmentCode">
                  <td class="mono">{{ e.equipmentCode }}</td>
                  <td>{{ e.equipmentName || '--' }}</td>
                  <td>{{ e.equipmentType || '--' }}</td>
                  <td>{{ e.location || '--' }}</td>
                  <td>{{ e.ipAddress || '--' }}</td>
                  <td>{{ e.description || '--' }}</td>
                </tr>
                <tr v-if="!lists.eq.length && !ld.eq"><td colspan="6" class="empty">NO DATA</td></tr>
              </tbody>
            </table>
          </div>

          <div class="dpanel-card">
            <div class="dc-head">
              <span class="dc-title">DELETE EQUIPMENT</span>
              <span class="dc-badge">DELETE /api/equipment/{code}</span>
            </div>
            <div class="fr">
              <div class="fg"><label>CODE</label><input class="fi" v-model="q.eqDel.code" placeholder="EQ-001"></div>
              <div class="fg" style="justify-content: flex-end"><label>&nbsp;</label><button class="btn bd" @click="deleteEq" :disabled="ld.eq">DELETE</button></div>
            </div>
          </div>
        </div>

        <!-- NUMPY LEARN PANEL -->
        <div class="dpanel" :class="{ active: panel === 'numpylearn' }">
          <NumPyLearnPage />
        </div>

        <!-- STOCK ANALYSIS PANEL -->
        <div class="dpanel" :class="{ active: panel === 'stock' }">
          <StockAnalysisPage />
        </div>
      </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.page-wrapper {
  position: fixed;
  inset: 0;
  overflow-y: auto;
  overflow-x: hidden;
  transform: translateZ(0);
  backface-visibility: hidden;
  scroll-behavior: smooth;
}

.slide-left-enter-active,
.slide-left-leave-active,
.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform 0.4s cubic-bezier(0.23, 1, 0.32, 1), opacity 0.35s cubic-bezier(0.23, 1, 0.32, 1);
  position: absolute;
  inset: 0;
  will-change: transform, opacity;
  backface-visibility: hidden;
  transform-style: preserve-3d;
  transform: translateZ(0);
  contain: layout style paint;
}

.slide-left-enter-from {
  transform: translateX(100%);
  opacity: 0;
}

.slide-left-leave-to {
  transform: translateX(-100%);
  opacity: 0;
}

.slide-right-enter-from {
  transform: translateX(-100%);
  opacity: 0;
}

.slide-right-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

.slide-left-enter-to,
.slide-left-leave-from,
.slide-right-enter-to,
.slide-right-leave-from {
  transform: translateX(0);
  opacity: 1;
}

.translation-section {
  padding: 30px 80px;
  position: relative;
  z-index: 5;
  background: var(--panel);
  border-top: 1px solid var(--edge);
  border-bottom: 1px solid var(--edge);
}

.glossary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  padding: 20px 0;
}

.glossary-item {
  background: var(--card);
  padding: 24px;
  border: 1px solid var(--edge);
  transition: all 0.3s;
  position: relative;
}

.glossary-item:hover {
  border-color: var(--neon);
  background: var(--card2);
}

.glossary-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 3px;
  height: 100%;
  background: linear-gradient(180deg, var(--neon), transparent);
  opacity: 0;
  transition: opacity 0.3s;
}

.glossary-item:hover::before {
  opacity: 1;
}

.gi-en {
  font-family: var(--orb);
  font-size: 13px;
  font-weight: 700;
  color: var(--neon);
  letter-spacing: 1px;
  margin-bottom: 8px;
}

.gi-zh {
  font-family: var(--exo);
  font-size: 16px;
  font-weight: 600;
  color: var(--txt);
  margin-bottom: 10px;
}

.gi-desc {
  font-size: 12px;
  color: var(--txt3);
  line-height: 1.5;
}

.intro .hero-section {
  animation: heroFadeIn 0.6s cubic-bezier(0.23, 1, 0.32, 1) forwards;
  will-change: opacity, transform;
}

.intro .hero-left {
  animation: contentSlideIn 0.5s cubic-bezier(0.23, 1, 0.32, 1) 0.15s forwards;
  opacity: 0;
  transform: translateX(-20px) translateZ(0);
  will-change: opacity, transform;
}

.intro .hero-right {
  animation: contentSlideIn 0.5s cubic-bezier(0.23, 1, 0.32, 1) 0.25s forwards;
  opacity: 0;
  transform: translateX(20px) translateZ(0);
  will-change: opacity, transform;
}

.intro .cta-section {
  animation: ctaFadeIn 0.6s cubic-bezier(0.23, 1, 0.32, 1) 0.4s forwards;
  opacity: 0;
  will-change: opacity, transform;
  transform: translateY(30px);
}

@keyframes heroFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px) translateZ(0);
  }
  to {
    opacity: 1;
    transform: translateY(0) translateZ(0);
  }
}

@keyframes contentSlideIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
    transform: translateX(0) translateZ(0);
  }
}

@keyframes statsFadeIn {
  from {
    opacity: 0;
    transform: translateY(15px) translateZ(0);
  }
  to {
    opacity: 1;
    transform: translateY(0) translateZ(0);
  }
}

@keyframes ctaFadeIn {
  from {
    opacity: 0;
    transform: translateY(30px) translateZ(0);
  }
  to {
    opacity: 1;
    transform: translateY(0) translateZ(0);
  }
}

.scroll-animate {
  opacity: 1;
  transform: translateY(0);
  transition: opacity 0.6s cubic-bezier(0.23, 1, 0.32, 1), transform 0.6s cubic-bezier(0.23, 1, 0.32, 1);
  will-change: opacity, transform;
}

.scroll-animate.scroll-hidden {
  opacity: 0;
  transform: translateY(30px);
}

@keyframes modulesFadeIn {
  from {
    opacity: 0;
    transform: translateY(15px) translateZ(0);
  }
  to {
    opacity: 1;
    transform: translateY(0) translateZ(0);
  }
}

.flow-container {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 32px;
  background: var(--card);
  border: 1px solid var(--edge);
}

.flow-step {
  flex: 1;
  padding: 24px;
  background: var(--panel);
  border: 1px solid var(--edge);
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.flow-step:hover {
  border-color: var(--neon);
  background: var(--card2);
}

.flow-step:hover .flow-icon {
  transform: scale(1.1);
}

.flow-icon {
  font-size: 32px;
  margin-bottom: 12px;
  transition: transform 0.3s;
}

.flow-pulse {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--neon), transparent);
  opacity: 0;
  transition: opacity 0.3s;
}

.flow-step:hover .flow-pulse {
  opacity: 1;
}

.flow-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.flow-name {
  font-family: var(--orb);
  font-size: 14px;
  font-weight: 700;
  color: var(--neon);
  letter-spacing: 2px;
}

.flow-desc {
  font-size: 12px;
  color: var(--txt3);
  line-height: 1.4;
}

.flow-protocol {
  font-family: var(--mono);
  font-size: 10px;
  color: var(--txt3);
  margin-top: 8px;
}

.flow-arrow {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  color: var(--neon);
}

.flow-arrow svg {
  width: 100%;
  height: 100%;
}

.hero-section {
  min-height: 65vh;
  display: grid;
  grid-template-columns: 1fr 1fr;
  position: relative;
  z-index: 5;
  padding: 100px 48px 60px 80px;
}

.hero-left {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.hero-eyebrow {
  font-family: var(--mono);
  font-size: 11px;
  letter-spacing: 5px;
  color: var(--neon);
  text-transform: uppercase;
  margin-bottom: 28px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.hero-eyebrow::before {
  content: '';
  width: 32px;
  height: 1px;
  background: var(--neon);
}

.hero-title {
  font-family: var(--orb);
  font-weight: 900;
  font-size: clamp(48px, 5.5vw, 80px);
  line-height: 0.95;
  letter-spacing: -1px;
  margin-bottom: 24px;
  color: #fff;
}

.hero-title .t-neon {
  display: block;
  background: linear-gradient(90deg, var(--neon) 0%, #80ffee 50%, var(--neon2) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: drop-shadow(0 0 30px rgba(0, 240, 255, 0.4));
}

.hero-title .t-dim {
  display: block;
  color: rgba(255, 255, 255, 0.15);
  font-size: 0.6em;
  letter-spacing: 6px;
  margin-top: 8px;
}

.hero-desc {
  font-family: var(--exo);
  font-size: 16px;
  font-weight: 300;
  color: var(--txt2);
  line-height: 1.85;
  max-width: 520px;
  margin-bottom: 48px;
  border-left: 2px solid var(--edge);
  padding-left: 20px;
}

.hero-desc span {
  color: var(--neon);
  font-weight: 600;
}

.hero-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.btn-primary {
  font-family: var(--orb);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 4px;
  padding: 16px 48px;
  border: none;
  cursor: pointer;
  background: linear-gradient(135deg, rgba(0, 150, 180, 0.8), rgba(0, 220, 255, 0.9));
  color: #000;
  clip-path: polygon(16px 0%, 100% 0%, calc(100% - 16px) 100%, 0% 100%);
  transition: all 0.3s;
  text-transform: uppercase;
}

.btn-primary:hover {
  filter: brightness(1.2);
  box-shadow: 0 0 60px rgba(0, 240, 255, 0.5);
  transform: translateY(-2px);
}

.btn-secondary {
  font-family: var(--mono);
  font-size: 11px;
  letter-spacing: 3px;
  padding: 16px 32px;
  border: 1px solid var(--rim);
  background: transparent;
  color: var(--txt2);
  cursor: pointer;
  transition: all 0.3s;
  text-transform: uppercase;
}

.btn-secondary:hover {
  border-color: var(--neon);
  color: var(--neon);
}

.hero-right {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.plc-visual {
  width: 320px;
  height: 420px;
  position: relative;
  animation: float 6s ease-in-out infinite;
}

.visual-ring {
  position: absolute;
  border-radius: 50%;
}

.ring-1 {
  width: 280px;
  height: 280px;
  animation: ringPulse 4s ease-in-out infinite;
  background: linear-gradient(135deg, rgba(0, 150, 200, 0.12) 0%, rgba(0, 100, 150, 0.06) 50%, rgba(0, 150, 200, 0.12) 100%);
  box-shadow: inset 0 0 40px rgba(0, 150, 200, 0.1), 0 0 30px rgba(0, 150, 200, 0.12);
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}

.ring-2 {
  width: 220px;
  height: 220px;
  animation: ringPulse 3s ease-in-out infinite reverse;
  background: linear-gradient(135deg, rgba(0, 180, 255, 0.18) 0%, rgba(0, 120, 220, 0.12) 50%, rgba(0, 180, 255, 0.18) 100%);
  box-shadow: inset 0 0 30px rgba(0, 180, 255, 0.14), 0 0 40px rgba(0, 180, 255, 0.2);
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}

.ring-3 {
  width: 160px;
  height: 160px;
  animation: ringPulse 2.5s ease-in-out infinite;
  background: linear-gradient(135deg, rgba(0, 200, 255, 0.14) 0%, rgba(0, 150, 230, 0.1) 50%, rgba(0, 200, 255, 0.14) 100%);
  box-shadow: inset 0 0 25px rgba(0, 200, 255, 0.12), 0 0 25px rgba(0, 200, 255, 0.14);
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}

.visual-center {
  width: 140px;
  height: 140px;
  background: radial-gradient(circle, rgba(0, 240, 255, 0.15) 0%, rgba(0, 200, 255, 0.05) 50%, transparent 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid rgba(0, 240, 255, 0.3);
  animation: glow 3s ease-in-out infinite;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  position: absolute;
}

.center-icon {
  font-size: 48px;
  color: var(--neon);
  animation: pulse 2s infinite;
}

.floating-data {
  position: absolute;
  width: 100%;
  height: 100%;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}

.data-item {
  position: absolute;
  text-align: center;
  animation: dataPulse 4s ease-in-out infinite;
  animation-delay: var(--delay);
  padding: 12px 20px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 8px;
  border: 1px solid rgba(0, 240, 255, 0.2);
  backdrop-filter: blur(8px);
}

.data-value {
  display: block;
  font-family: var(--mono);
  font-size: 20px;
  font-weight: 700;
  color: var(--neon);
  margin-bottom: 4px;
  text-shadow: 0 0 10px rgba(0, 240, 255, 0.4);
}

.data-label {
  font-size: 10px;
  color: var(--txt2);
  letter-spacing: 1px;
  text-transform: uppercase;
}

.intro-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  border-top: 1px solid var(--edge);
  border-bottom: 1px solid var(--edge);
  background: var(--panel);
  position: relative;
  z-index: 5;
}

.stat-block {
  padding: 28px 24px;
  position: relative;
  border-right: 1px solid var(--edge);
  transition: background 0.3s;
}

.stat-block:last-child {
  border-right: none;
}

.stat-block:hover {
  background: rgba(0, 240, 255, 0.03);
}

.stat-block::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--neon), transparent);
  opacity: 0;
  transition: opacity 0.3s;
}

.stat-block:hover::before {
  opacity: 1;
}

.sb-num {
  font-family: var(--orb);
  font-size: 32px;
  font-weight: 700;
  color: var(--neon);
  line-height: 1;
  margin-bottom: 6px;
}

.sb-lbl {
  font-family: var(--mono);
  font-size: 10px;
  color: var(--txt3);
  letter-spacing: 2px;
  text-transform: uppercase;
}

.sb-bar {
  height: 2px;
  margin-top: 10px;
  background: var(--ghost);
  border-radius: 1px;
  overflow: hidden;
}

.sb-fill {
  height: 100%;
  border-radius: 1px;
  transition: width 2s ease;
  background: linear-gradient(90deg, var(--neon), var(--neon2));
}

.modules-section {
  padding: 60px 80px;
  position: relative;
  z-index: 5;
}

.mods-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 2px;
}

.mod-tile {
  background: var(--card);
  padding: 32px 28px;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.mod-tile::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(0, 240, 255, 0.05) 0%, transparent 60%);
  opacity: 0;
  transition: opacity 0.3s;
}

.mod-tile:hover {
  border-color: var(--rim);
  background: var(--card2);
}

.mod-tile:hover::before {
  opacity: 1;
}

.mod-tile:hover .mt-icon {
  transform: scale(1.1);
  filter: drop-shadow(0 0 16px currentColor);
}

.mt-corner {
  position: absolute;
  top: 0;
  right: 0;
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 0 32px 32px 0;
  border-color: transparent var(--edge) transparent transparent;
  transition: border-color 0.3s;
}

.mod-tile:hover .mt-corner {
  border-color: transparent var(--neon) transparent transparent;
}

.mt-icon {
  font-size: 32px;
  margin-bottom: 16px;
  display: block;
  transition: all 0.3s;
}

.mt-name {
  font-family: var(--orb);
  font-size: 14px;
  font-weight: 700;
  color: var(--txt);
  letter-spacing: 2px;
  margin-bottom: 6px;
}

.mt-desc {
  font-family: var(--exo);
  font-size: 12px;
  color: var(--txt3);
  line-height: 1.5;
}

.mt-api {
  position: absolute;
  bottom: 16px;
  right: 16px;
  font-family: var(--mono);
  font-size: 9px;
  color: var(--txt3);
}

.cta-section {
  padding: 80px 48px;
  text-align: center;
  position: relative;
  z-index: 5;
}

.cta-content {
  position: relative;
  z-index: 2;
}

.cta-title {
  font-family: var(--orb);
  font-size: clamp(36px, 6vw, 64px);
  font-weight: 800;
  color: #fff;
  margin-bottom: 24px;
}

.cta-desc {
  font-size: 18px;
  color: var(--txt2);
  margin-bottom: 48px;
}

.cta-btn {
  background: linear-gradient(135deg, var(--neon) 0%, var(--neon2) 100%);
  color: var(--void);
  padding: 20px 48px;
  border-radius: 4px;
  font-family: var(--orb);
  font-weight: 700;
  font-size: 14px;
  letter-spacing: 4px;
  text-transform: uppercase;
  border: none;
  cursor: pointer;
  transition: all 0.4s;
  box-shadow: 0 4px 30px rgba(0, 240, 255, 0.3);
}

.cta-btn:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 40px rgba(0, 240, 255, 0.5);
}

.intro-footer {
  padding: 20px 80px;
  border-top: 1px solid var(--edge);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--panel);
  position: relative;
  z-index: 5;
}

.foot-copy {
  font-family: var(--mono);
  font-size: 10px;
  color: var(--txt3);
  letter-spacing: 2px;
}

.foot-ver {
  font-family: var(--mono);
  font-size: 10px;
  color: var(--txt3);
}

.data-learn-section {
  border-top: 1px solid var(--edge);
  background: linear-gradient(180deg, var(--panel) 0%, var(--abyss) 100%);
}

.data-learn-intro {
  margin-bottom: 32px;
  padding: 24px 28px;
  background: var(--card);
  border: 1px solid var(--edge);
  border-left: 3px solid var(--neon2);
  position: relative;
}

.data-learn-intro::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, var(--neon2), transparent);
  opacity: 0.5;
}

.data-learn-desc {
  font-family: var(--exo);
  font-size: 14px;
  font-weight: 300;
  color: var(--txt2);
  line-height: 1.9;
}

.data-learn-desc span {
  color: var(--neon);
  font-weight: 600;
}

.mt-tag {
  position: absolute;
  bottom: 16px;
  right: 16px;
  font-family: var(--mono);
  font-size: 9px;
  color: var(--neon2);
  padding: 2px 6px;
  border: 1px solid rgba(0, 255, 136, 0.3);
  letter-spacing: 1px;
}
</style>
