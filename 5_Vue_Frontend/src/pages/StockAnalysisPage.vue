<script setup>
import { ref, computed, nextTick } from 'vue'

// ==================== 模块导航 ====================
const activeModule = ref('input')
const modules = [
  { id: 'input', label: '数据输入', icon: '01' },
  { id: 'return', label: '收益率', icon: '02' },
  { id: 'volatility', label: '波动率', icon: '03' },
  { id: 'chart', label: '走势图', icon: '04' },
  { id: 'code', label: '代码对照', icon: '05' }
]

const scrollToModule = (id) => {
  activeModule.value = id
  const el = document.getElementById('module-' + id)
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

// ==================== 模块1：数据输入 ====================
const defaultPrices = [
  336.1, 339.32, 345.03, 344.32, 343.44, 346.5, 351.88, 355.2, 358.16, 354.54,
  356.85, 359.18, 359.9, 363.13, 358.3, 350.56, 338.61, 342.62, 342.88, 348.16,
  353.21, 349.31, 352.12, 359.56, 360.00, 355.36, 355.76, 352.47, 346.67, 351.99
]

const pricesText = ref(defaultPrices.join('\n'))
const prices = ref([...defaultPrices])
const dataLoaded = ref(false)

const loadData = () => {
  const lines = pricesText.value.trim().split('\n').map(l => parseFloat(l.trim())).filter(n => !isNaN(n))
  if (lines.length < 2) return
  prices.value = lines
  dataLoaded.value = true
  activeReturnTab.value = 'simple'
  activeCodeStep.value = 0
}

const dataSummary = computed(() => {
  const p = prices.value
  if (!p.length) return { count: 0, max: 0, min: 0, avg: 0 }
  return {
    count: p.length,
    max: Math.max(...p),
    min: Math.min(...p),
    avg: (p.reduce((a, b) => a + b, 0) / p.length)
  }
})

// ==================== 模块2：收益率计算 ====================
const activeReturnTab = ref('simple')

const simpleReturns = computed(() => {
  const p = prices.value
  if (p.length < 2) return []
  const r = []
  for (let i = 1; i < p.length; i++) {
    r.push((p[i] - p[i - 1]) / p[i - 1])
  }
  return r
})

const logReturns = computed(() => {
  const p = prices.value
  if (p.length < 2) return []
  const r = []
  for (let i = 1; i < p.length; i++) {
    r.push(Math.log(p[i] / p[i - 1]))
  }
  return r
})

const currentReturns = computed(() => {
  return activeReturnTab.value === 'simple' ? simpleReturns.value : logReturns.value
})

const returnStats = computed(() => {
  const r = currentReturns.value
  if (!r.length) return { mean: 0, std: 0, max: 0, min: 0, positiveDays: 0 }
  const mean = r.reduce((a, b) => a + b, 0) / r.length
  const variance = r.reduce((a, b) => a + (b - mean) ** 2, 0) / (r.length - 1)
  const std = Math.sqrt(variance)
  return {
    mean,
    std,
    max: Math.max(...r),
    min: Math.min(...r),
    positiveDays: r.filter(v => v > 0).length
  }
})

const fmtPct = (v) => (v * 100).toFixed(4) + '%'

// ==================== 模块3：波动率分析 ====================
const volatility = computed(() => {
  const r = logReturns.value
  if (r.length < 2) return { daily: 0, monthly: 0, annual: 0 }
  const mean = r.reduce((a, b) => a + b, 0) / r.length
  const variance = r.reduce((a, b) => a + (b - mean) ** 2, 0) / (r.length - 1)
  const daily = Math.sqrt(variance)
  const monthly = daily * Math.sqrt(21)
  const annual = daily * Math.sqrt(252)
  return { daily, monthly, annual }
})

const volBarMax = computed(() => {
  const v = volatility.value
  return Math.max(v.daily, v.monthly, v.annual)
})

// ==================== 模块4：价格走势图 ====================
const chartPoints = computed(() => {
  const p = prices.value
  if (p.length < 2) return { points: '', maxIdx: 0, minIdx: 0 }
  const min = Math.min(...p)
  const max = Math.max(...p)
  const range = max - min || 1
  const w = 680
  const h = 220
  const pad = 30
  const step = (w - pad * 2) / (p.length - 1)
  let pts = ''
  let maxIdx = 0
  let minIdx = 0
  for (let i = 0; i < p.length; i++) {
    const x = pad + i * step
    const y = h - pad - ((p[i] - min) / range) * (h - pad * 2)
    pts += (i === 0 ? '' : ' ') + x.toFixed(1) + ',' + y.toFixed(1)
    if (p[i] > p[maxIdx]) maxIdx = i
    if (p[i] < p[minIdx]) minIdx = i
  }
  return { points: pts, maxIdx, minIdx }
})

const chartTooltip = ref({ show: false, x: 0, y: 0, value: '', day: '' })

const getPointCoord = (idx) => {
  const p = prices.value
  if (p.length < 2) return { x: 0, y: 0 }
  const min = Math.min(...p)
  const max = Math.max(...p)
  const range = max - min || 1
  const w = 680
  const h = 220
  const pad = 30
  const step = (w - pad * 2) / (p.length - 1)
  const x = pad + idx * step
  const y = h - pad - ((p[idx] - min) / range) * (h - pad * 2)
  return { x, y }
}

const handleChartHover = (e) => {
  const svg = e.currentTarget
  const rect = svg.getBoundingClientRect()
  const mx = e.clientX - rect.left
  const p = prices.value
  if (p.length < 2) return
  const pad = 30
  const step = (680 - pad * 2) / (p.length - 1)
  const idx = Math.round((mx - pad) / step)
  if (idx >= 0 && idx < p.length) {
    const coord = getPointCoord(idx)
    chartTooltip.value = {
      show: true,
      x: coord.x,
      y: coord.y,
      value: p[idx].toFixed(2),
      day: 'Day ' + (idx + 1)
    }
  }
}

const hideChartTooltip = () => {
  chartTooltip.value.show = false
}

// ==================== 模块5：代码对照 ====================
const activeCodeStep = ref(0)
const codeSteps = [
  {
    title: '1. 定义股票收盘价数据',
    code: `import numpy as np

# 定义30天的股票收盘价
prices = np.array([
    336.1, 339.32, 345.03, 344.32, 343.44,
    346.5, 351.88, 355.2, 358.16, 354.54,
    356.85, 359.18, 359.9, 363.13, 358.3,
    350.56, 338.61, 342.62, 342.88, 348.16,
    353.21, 349.31, 352.12, 359.56, 360.00,
    355.36, 355.76, 352.47, 346.67, 351.99
])
print(f"数据点数: {len(prices)}")`,
    result: () => `数据点数: ${prices.value.length}`
  },
  {
    title: '2. 计算简单收益率',
    code: `# 简单收益率: R_t = (P_t - P_{t-1}) / P_{t-1}
simple_returns = np.diff(prices) / prices[:-1]
print("简单收益率:")
print(np.round(simple_returns, 6))
print(f"均值: {simple_returns.mean():.6f}")
print(f"标准差: {simple_returns.std():.6f}")`,
    result: () => {
      const r = simpleReturns.value
      return `简单收益率:\n[${r.map(v => v.toFixed(6)).join(', ')}]\n均值: ${returnStats.value.mean.toFixed(6)}\n标准差: ${returnStats.value.std.toFixed(6)}`
    }
  },
  {
    title: '3. 计算对数收益率',
    code: `# 对数收益率: r_t = ln(P_t / P_{t-1})
log_returns = np.log(prices[1:] / prices[:-1])
print("对数收益率:")
print(np.round(log_returns, 6))
print(f"均值: {log_returns.mean():.6f}")
print(f"标准差: {log_returns.std():.6f}")`,
    result: () => {
      const r = logReturns.value
      return `对数收益率:\n[${r.map(v => v.toFixed(6)).join(', ')}]\n均值: ${returnStats.value.mean.toFixed(6)}\n标准差: ${returnStats.value.std.toFixed(6)}`
    }
  },
  {
    title: '4. 计算波动率',
    code: `# 日波动率 = std(对数收益率)
daily_vol = log_returns.std()
# 月波动率 = 日波动率 * sqrt(21)
monthly_vol = daily_vol * np.sqrt(21)
# 年波动率 = 日波动率 * sqrt(252)
annual_vol = daily_vol * np.sqrt(252)

print(f"日波动率: {daily_vol:.6f} ({daily_vol*100:.2f}%)")
print(f"月波动率: {monthly_vol:.6f} ({monthly_vol*100:.2f}%)")
print(f"年波动率: {annual_vol:.6f} ({annual_vol*100:.2f}%)")`,
    result: () => {
      const v = volatility.value
      return `日波动率: ${v.daily.toFixed(6)} (${(v.daily * 100).toFixed(2)}%)\n月波动率: ${v.monthly.toFixed(6)} (${(v.monthly * 100).toFixed(2)}%)\n年波动率: ${v.annual.toFixed(6)} (${(v.annual * 100).toFixed(2)}%)`
    }
  }
]

const currentCodeResult = computed(() => {
  return codeSteps[activeCodeStep.value]?.result() || ''
})

// 自动加载
loadData()
</script>

<template>
  <div class="stock-page">
    <!-- 顶部标题栏 -->
    <div class="page-header">
      <div class="header-left">
        <span class="header-tag">CASE STUDY</span>
        <h1 class="page-title">股票收益率与波动率分析</h1>
        <span class="page-subtitle">Stock Return & Volatility Analysis</span>
      </div>
      <div class="header-right">
        <span class="data-badge">
          <span class="badge-dot"></span>
          {{ dataSummary.count }} DATA POINTS
        </span>
      </div>
    </div>

    <!-- 模块导航标签 -->
    <nav class="module-nav">
      <button
        v-for="m in modules"
        :key="m.id"
        :class="['nav-tab', { active: activeModule === m.id }]"
        @click="scrollToModule(m.id)"
      >
        <span class="tab-num">{{ m.icon }}</span>
        <span class="tab-label">{{ m.label }}</span>
      </button>
    </nav>

    <!-- 主体布局 -->
    <div class="main-grid">

      <!-- 左侧列 -->
      <div class="left-col">

        <!-- 模块1：数据输入面板 -->
        <section id="module-input" class="panel">
          <div class="panel-head">
            <span class="panel-num">01</span>
            <h2 class="panel-title">数据输入面板</h2>
            <span class="panel-tag">DATA INPUT</span>
          </div>
          <div class="panel-body">
            <div class="input-group">
              <label class="field-label">收盘价数据（每行一个价格）</label>
              <textarea
                v-model="pricesText"
                class="data-textarea"
                rows="10"
                spellcheck="false"
              ></textarea>
              <button class="btn-load" @click="loadData">
                <span class="btn-icon">&#9654;</span> 加载数据
              </button>
            </div>
            <div class="summary-row">
              <div class="summary-item">
                <span class="summary-label">数据点数</span>
                <span class="summary-value">{{ dataSummary.count }}</span>
              </div>
              <div class="summary-item">
                <span class="summary-label">最高价</span>
                <span class="summary-value neon">{{ dataSummary.max.toFixed(2) }}</span>
              </div>
              <div class="summary-item">
                <span class="summary-label">最低价</span>
                <span class="summary-value plasma">{{ dataSummary.min.toFixed(2) }}</span>
              </div>
              <div class="summary-item">
                <span class="summary-label">均价</span>
                <span class="summary-value">{{ dataSummary.avg.toFixed(2) }}</span>
              </div>
            </div>
          </div>
        </section>

        <!-- 模块4：价格走势图 -->
        <section id="module-chart" class="panel">
          <div class="panel-head">
            <span class="panel-num">04</span>
            <h2 class="panel-title">价格走势图</h2>
            <span class="panel-tag">PRICE CHART</span>
          </div>
          <div class="panel-body">
            <div class="chart-container" v-if="prices.length >= 2">
              <svg
                viewBox="0 0 680 220"
                class="price-svg"
                @mousemove="handleChartHover"
                @mouseleave="hideChartTooltip"
              >
                <!-- 网格线 -->
                <line v-for="i in 5" :key="'grid'+i"
                  :x1="30" :y1="30 + (i-1) * 40"
                  :x2="650" :y2="30 + (i-1) * 40"
                  class="grid-line"
                />
                <!-- Y轴标签 -->
                <text v-for="i in 5" :key="'ylab'+i"
                  :x="25" :y="34 + (i-1) * 40"
                  class="y-label"
                  text-anchor="end"
                >{{ (dataSummary.max - (i - 1) * (dataSummary.max - dataSummary.min) / 4).toFixed(0) }}</text>
                <!-- 折线 -->
                <polyline :points="chartPoints.points" class="price-line" fill="none" />
                <!-- 渐变填充区域 -->
                <polygon
                  :points="chartPoints.points + ' 650,190 30,190'"
                  class="price-area"
                />
                <!-- 最高点标注 -->
                <circle
                  :cx="getPointCoord(chartPoints.maxIdx).x"
                  :cy="getPointCoord(chartPoints.maxIdx).y"
                  r="5" class="dot-max"
                />
                <text
                  :x="getPointCoord(chartPoints.maxIdx).x"
                  :y="getPointCoord(chartPoints.maxIdx).y - 12"
                  class="anno-text neon-text"
                  text-anchor="middle"
                >{{ prices[chartPoints.maxIdx].toFixed(2) }}</text>
                <!-- 最低点标注 -->
                <circle
                  :cx="getPointCoord(chartPoints.minIdx).x"
                  :cy="getPointCoord(chartPoints.minIdx).y"
                  r="5" class="dot-min"
                />
                <text
                  :x="getPointCoord(chartPoints.minIdx).x"
                  :y="getPointCoord(chartPoints.minIdx).y + 20"
                  class="anno-text plasma-text"
                  text-anchor="middle"
                >{{ prices[chartPoints.minIdx].toFixed(2) }}</text>
                <!-- Tooltip -->
                <g v-if="chartTooltip.show">
                  <line
                    :x1="chartTooltip.x" y1="30"
                    :x2="chartTooltip.x" y2="190"
                    class="tooltip-line"
                  />
                  <rect
                    :x="chartTooltip.x - 45"
                    :y="chartTooltip.y - 38"
                    width="90" height="28" rx="4"
                    class="tooltip-box"
                  />
                  <text
                    :x="chartTooltip.x"
                    :y="chartTooltip.y - 19"
                    class="tooltip-text"
                    text-anchor="middle"
                  >{{ chartTooltip.day }}: {{ chartTooltip.value }}</text>
                </g>
              </svg>
            </div>
            <div class="chart-legend">
              <span class="legend-item"><span class="legend-dot neon-dot"></span>最高点</span>
              <span class="legend-item"><span class="legend-dot plasma-dot"></span>最低点</span>
              <span class="legend-item"><span class="legend-line"></span>收盘价</span>
            </div>
          </div>
        </section>
      </div>

      <!-- 右侧列 -->
      <div class="right-col">

        <!-- 模块2：收益率计算面板 -->
        <section id="module-return" class="panel">
          <div class="panel-head">
            <span class="panel-num">02</span>
            <h2 class="panel-title">收益率计算</h2>
            <span class="panel-tag">RETURNS</span>
          </div>
          <div class="panel-body">
            <!-- 标签页切换 -->
            <div class="tab-bar">
              <button
                :class="['tab-btn', { active: activeReturnTab === 'simple' }]"
                @click="activeReturnTab = 'simple'"
              >简单收益率</button>
              <button
                :class="['tab-btn', { active: activeReturnTab === 'log' }]"
                @click="activeReturnTab = 'log'"
              >对数收益率</button>
            </div>

            <!-- 公式展示 -->
            <div class="formula-box">
              <div v-if="activeReturnTab === 'simple'" class="formula">
                <span class="formula-label">简单收益率公式</span>
                <span class="formula-expr">R<sub>t</sub> = (P<sub>t</sub> - P<sub>t-1</sub>) / P<sub>t-1</sub></span>
              </div>
              <div v-else class="formula">
                <span class="formula-label">对数收益率公式</span>
                <span class="formula-expr">r<sub>t</sub> = ln(P<sub>t</sub> / P<sub>t-1</sub>)</span>
              </div>
            </div>

            <!-- 收益率标签 -->
            <div class="returns-tags">
              <span
                v-for="(r, i) in currentReturns"
                :key="i"
                :class="['return-tag', r >= 0 ? 'tag-positive' : 'tag-negative']"
                :title="'Day ' + (i+2) + ': ' + fmtPct(r)"
              >{{ fmtPct(r) }}</span>
            </div>

            <!-- 统计信息 -->
            <div class="stats-grid">
              <div class="stat-card">
                <span class="stat-label">均值</span>
                <span class="stat-value">{{ fmtPct(returnStats.mean) }}</span>
              </div>
              <div class="stat-card">
                <span class="stat-label">标准差</span>
                <span class="stat-value">{{ fmtPct(returnStats.std) }}</span>
              </div>
              <div class="stat-card">
                <span class="stat-label">最大值</span>
                <span class="stat-value neon">{{ fmtPct(returnStats.max) }}</span>
              </div>
              <div class="stat-card">
                <span class="stat-label">最小值</span>
                <span class="stat-value plasma">{{ fmtPct(returnStats.min) }}</span>
              </div>
              <div class="stat-card stat-wide">
                <span class="stat-label">正收益天数</span>
                <span class="stat-value">{{ returnStats.positiveDays }} / {{ currentReturns.length }}</span>
              </div>
            </div>
          </div>
        </section>

        <!-- 模块3：波动率分析面板 -->
        <section id="module-volatility" class="panel">
          <div class="panel-head">
            <span class="panel-num">03</span>
            <h2 class="panel-title">波动率分析</h2>
            <span class="panel-tag">VOLATILITY</span>
          </div>
          <div class="panel-body">
            <!-- 公式 -->
            <div class="formula-box">
              <span class="formula-label">波动率计算公式</span>
              <span class="formula-expr">&sigma;<sub>年</sub> = &sigma;<sub>日</sub> &times; &radic;252 &nbsp;&nbsp;|&nbsp;&nbsp; &sigma;<sub>月</sub> = &sigma;<sub>日</sub> &times; &radic;21</span>
            </div>

            <!-- 波动率数值 -->
            <div class="vol-cards">
              <div class="vol-card">
                <span class="vol-label">日波动率</span>
                <span class="vol-value">{{ (volatility.daily * 100).toFixed(4) }}%</span>
                <span class="vol-formula">&sigma;<sub>daily</sub></span>
              </div>
              <div class="vol-card">
                <span class="vol-label">月波动率</span>
                <span class="vol-value neon">{{ (volatility.monthly * 100).toFixed(4) }}%</span>
                <span class="vol-formula">&sigma;<sub>daily</sub> &times; &radic;21</span>
              </div>
              <div class="vol-card">
                <span class="vol-label">年波动率</span>
                <span class="vol-value volt">{{ (volatility.annual * 100).toFixed(4) }}%</span>
                <span class="vol-formula">&sigma;<sub>daily</sub> &times; &radic;252</span>
              </div>
            </div>

            <!-- CSS 柱状图 -->
            <div class="bar-chart">
              <div class="bar-label-row">
                <span class="bar-name">日</span>
                <span class="bar-name">月</span>
                <span class="bar-name">年</span>
              </div>
              <div class="bar-track-row">
                <div class="bar-track">
                  <div
                    class="bar-fill bar-daily"
                    :style="{ height: (volatility.daily / volBarMax * 100) + '%' }"
                  >
                    <span class="bar-val">{{ (volatility.daily * 100).toFixed(2) }}%</span>
                  </div>
                </div>
                <div class="bar-track">
                  <div
                    class="bar-fill bar-monthly"
                    :style="{ height: (volatility.monthly / volBarMax * 100) + '%' }"
                  >
                    <span class="bar-val">{{ (volatility.monthly * 100).toFixed(2) }}%</span>
                  </div>
                </div>
                <div class="bar-track">
                  <div
                    class="bar-fill bar-annual"
                    :style="{ height: (volatility.annual / volBarMax * 100) + '%' }"
                  >
                    <span class="bar-val">{{ (volatility.annual * 100).toFixed(2) }}%</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>

    <!-- 模块5：代码对照面板 -->
    <section id="module-code" class="panel code-panel">
      <div class="panel-head">
        <span class="panel-num">05</span>
        <h2 class="panel-title">代码对照面板</h2>
        <span class="panel-tag">NUMPY CODE</span>
      </div>
      <div class="panel-body">
        <div class="code-steps-nav">
          <button
            v-for="(step, i) in codeSteps"
            :key="i"
            :class="['step-btn', { active: activeCodeStep === i }]"
            @click="activeCodeStep = i"
          >{{ step.title }}</button>
        </div>
        <div class="code-compare">
          <div class="code-col">
            <div class="col-header">NumPy Source Code</div>
            <pre class="code-block"><code>{{ codeSteps[activeCodeStep].code }}</code></pre>
          </div>
          <div class="code-col">
            <div class="col-header">JavaScript Result</div>
            <pre class="code-block result-block"><code>{{ currentCodeResult }}</code></pre>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
/* ==================== 基础变量 ==================== */
.stock-page {
  --void: #020608;
  --abyss: #050d12;
  --panel: #071520;
  --card: #091a28;
  --card2: #0c2035;
  --edge: #0d3550;
  --rim: #145070;
  --neon: #00f0ff;
  --neon2: #00ff88;
  --plasma: #ff4400;
  --volt: #ffdd00;
  --txt: #b0d8f0;
  --txt2: #5a90b0;
  --txt3: #2a5070;
  --mono: 'Share Tech Mono', monospace;
  --orb: 'Orbitron', sans-serif;
  --exo: 'Exo 2', sans-serif;
  --noto: 'Noto Sans SC', sans-serif;

  min-height: 100vh;
  background: var(--void);
  color: var(--txt);
  font-family: var(--exo), var(--noto), sans-serif;
  padding: 20px 24px 40px;
  box-sizing: border-box;
}

/* ==================== 顶部标题栏 ==================== */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: linear-gradient(135deg, var(--abyss), var(--panel));
  border: 1px solid var(--edge);
  border-radius: 8px;
  margin-bottom: 12px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-tag {
  font-family: var(--orb);
  font-size: 10px;
  letter-spacing: 2px;
  color: var(--neon);
  background: rgba(0, 240, 255, 0.08);
  border: 1px solid rgba(0, 240, 255, 0.25);
  padding: 4px 10px;
  border-radius: 3px;
}

.page-title {
  font-family: var(--noto), var(--exo), sans-serif;
  font-size: 22px;
  font-weight: 700;
  color: #e8f4ff;
  margin: 0;
  letter-spacing: 1px;
}

.page-subtitle {
  font-family: var(--orb);
  font-size: 10px;
  color: var(--txt2);
  letter-spacing: 2px;
}

.data-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: var(--mono);
  font-size: 12px;
  color: var(--neon2);
  letter-spacing: 1px;
}

.badge-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--neon2);
  box-shadow: 0 0 8px var(--neon2);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

/* ==================== 模块导航 ==================== */
.module-nav {
  display: flex;
  gap: 6px;
  margin-bottom: 16px;
  padding: 6px;
  background: var(--abyss);
  border: 1px solid var(--edge);
  border-radius: 8px;
}

.nav-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 12px;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 6px;
  color: var(--txt2);
  font-family: var(--exo), sans-serif;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.25s;
}

.nav-tab:hover {
  background: rgba(0, 240, 255, 0.04);
  color: var(--txt);
  border-color: var(--edge);
}

.nav-tab.active {
  background: rgba(0, 240, 255, 0.08);
  border-color: var(--rim);
  color: var(--neon);
}

.tab-num {
  font-family: var(--orb);
  font-size: 10px;
  opacity: 0.6;
}

/* ==================== 主体网格布局 ==================== */
.main-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.left-col,
.right-col {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ==================== 面板通用样式 ==================== */
.panel {
  background: var(--panel);
  border: 1px solid var(--edge);
  border-radius: 8px;
  overflow: hidden;
}

.panel-head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  background: linear-gradient(90deg, rgba(0, 240, 255, 0.04), transparent);
  border-bottom: 1px solid var(--edge);
}

.panel-num {
  font-family: var(--orb);
  font-size: 11px;
  color: var(--neon);
  opacity: 0.5;
}

.panel-title {
  font-family: var(--noto), var(--exo), sans-serif;
  font-size: 15px;
  font-weight: 600;
  color: #d0e8f8;
  margin: 0;
}

.panel-tag {
  margin-left: auto;
  font-family: var(--orb);
  font-size: 9px;
  letter-spacing: 2px;
  color: var(--txt3);
}

.panel-body {
  padding: 20px;
}

/* ==================== 模块1：数据输入 ==================== */
.field-label {
  display: block;
  font-size: 12px;
  color: var(--txt2);
  margin-bottom: 8px;
  letter-spacing: 0.5px;
}

.data-textarea {
  width: 100%;
  min-height: 180px;
  padding: 12px 14px;
  background: var(--void);
  border: 1px solid var(--edge);
  border-radius: 6px;
  color: var(--neon);
  font-family: var(--mono);
  font-size: 13px;
  line-height: 1.7;
  resize: vertical;
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.data-textarea:focus {
  border-color: var(--rim);
  box-shadow: 0 0 0 2px rgba(0, 240, 255, 0.08);
}

.btn-load {
  margin-top: 12px;
  padding: 10px 24px;
  background: linear-gradient(135deg, rgba(0, 240, 255, 0.12), rgba(0, 240, 255, 0.04));
  border: 1px solid var(--rim);
  border-radius: 6px;
  color: var(--neon);
  font-family: var(--exo), sans-serif;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s;
  letter-spacing: 0.5px;
}

.btn-load:hover {
  background: linear-gradient(135deg, rgba(0, 240, 255, 0.2), rgba(0, 240, 255, 0.08));
  box-shadow: 0 0 16px rgba(0, 240, 255, 0.15);
}

.btn-icon {
  margin-right: 6px;
  font-size: 10px;
}

.summary-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-top: 16px;
}

.summary-item {
  background: var(--card);
  border: 1px solid var(--edge);
  border-radius: 6px;
  padding: 12px;
  text-align: center;
}

.summary-label {
  display: block;
  font-size: 10px;
  color: var(--txt3);
  letter-spacing: 1px;
  text-transform: uppercase;
  margin-bottom: 6px;
}

.summary-value {
  display: block;
  font-family: var(--mono);
  font-size: 16px;
  font-weight: 700;
  color: var(--txt);
}

.summary-value.neon {
  color: var(--neon);
  text-shadow: 0 0 8px rgba(0, 240, 255, 0.3);
}

.summary-value.plasma {
  color: var(--plasma);
  text-shadow: 0 0 8px rgba(255, 68, 0, 0.3);
}

/* ==================== 模块2：收益率计算 ==================== */
.tab-bar {
  display: flex;
  gap: 4px;
  margin-bottom: 16px;
  background: var(--void);
  border-radius: 6px;
  padding: 3px;
}

.tab-btn {
  flex: 1;
  padding: 8px 16px;
  background: transparent;
  border: none;
  border-radius: 4px;
  color: var(--txt2);
  font-family: var(--exo), sans-serif;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn.active {
  background: var(--card);
  color: var(--neon);
  box-shadow: 0 0 8px rgba(0, 240, 255, 0.08);
}

.tab-btn:hover:not(.active) {
  color: var(--txt);
}

.formula-box {
  background: var(--card);
  border: 1px solid var(--edge);
  border-left: 3px solid var(--neon);
  border-radius: 0 6px 6px 0;
  padding: 14px 18px;
  margin-bottom: 16px;
}

.formula-label {
  display: block;
  font-size: 10px;
  color: var(--txt3);
  letter-spacing: 1px;
  text-transform: uppercase;
  margin-bottom: 6px;
}

.formula-expr {
  font-family: var(--mono);
  font-size: 15px;
  color: var(--neon);
}

.returns-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 16px;
  max-height: 120px;
  overflow-y: auto;
  padding-right: 4px;
}

.returns-tags::-webkit-scrollbar {
  width: 4px;
}

.returns-tags::-webkit-scrollbar-track {
  background: var(--void);
}

.returns-tags::-webkit-scrollbar-thumb {
  background: var(--edge);
  border-radius: 2px;
}

.return-tag {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 4px;
  font-family: var(--mono);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.3px;
  cursor: default;
  transition: transform 0.15s;
}

.return-tag:hover {
  transform: scale(1.08);
}

.tag-positive {
  background: rgba(0, 255, 136, 0.1);
  border: 1px solid rgba(0, 255, 136, 0.3);
  color: var(--neon2);
}

.tag-negative {
  background: rgba(255, 68, 0, 0.1);
  border: 1px solid rgba(255, 68, 0, 0.3);
  color: var(--plasma);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.stat-card {
  background: var(--card);
  border: 1px solid var(--edge);
  border-radius: 6px;
  padding: 12px 14px;
}

.stat-wide {
  grid-column: 1 / -1;
}

.stat-label {
  display: block;
  font-size: 10px;
  color: var(--txt3);
  letter-spacing: 1px;
  text-transform: uppercase;
  margin-bottom: 4px;
}

.stat-value {
  display: block;
  font-family: var(--mono);
  font-size: 15px;
  font-weight: 700;
  color: var(--txt);
}

.stat-value.neon {
  color: var(--neon);
}

.stat-value.plasma {
  color: var(--plasma);
}

/* ==================== 模块3：波动率分析 ==================== */
.vol-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 20px;
}

.vol-card {
  background: var(--card);
  border: 1px solid var(--edge);
  border-radius: 6px;
  padding: 14px;
  text-align: center;
}

.vol-label {
  display: block;
  font-size: 11px;
  color: var(--txt2);
  margin-bottom: 6px;
}

.vol-value {
  display: block;
  font-family: var(--mono);
  font-size: 18px;
  font-weight: 700;
  color: var(--txt);
  margin-bottom: 6px;
}

.vol-value.neon {
  color: var(--neon);
  text-shadow: 0 0 10px rgba(0, 240, 255, 0.3);
}

.vol-value.volt {
  color: var(--volt);
  text-shadow: 0 0 10px rgba(255, 221, 0, 0.3);
}

.vol-formula {
  font-family: var(--mono);
  font-size: 10px;
  color: var(--txt3);
}

/* CSS 柱状图 */
.bar-chart {
  background: var(--card);
  border: 1px solid var(--edge);
  border-radius: 6px;
  padding: 20px;
}

.bar-label-row {
  display: flex;
  justify-content: space-around;
  margin-bottom: 8px;
}

.bar-name {
  width: 60px;
  text-align: center;
  font-size: 11px;
  color: var(--txt2);
  font-weight: 600;
}

.bar-track-row {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 140px;
}

.bar-track {
  width: 60px;
  height: 100%;
  background: var(--void);
  border: 1px solid var(--edge);
  border-radius: 4px;
  position: relative;
  display: flex;
  align-items: flex-end;
}

.bar-fill {
  width: 100%;
  border-radius: 3px 3px 0 0;
  position: relative;
  min-height: 4px;
  transition: height 0.6s cubic-bezier(0.22, 1, 0.36, 1);
}

.bar-daily {
  background: linear-gradient(180deg, var(--neon), rgba(0, 240, 255, 0.3));
  box-shadow: 0 0 12px rgba(0, 240, 255, 0.2);
}

.bar-monthly {
  background: linear-gradient(180deg, var(--neon2), rgba(0, 255, 136, 0.3));
  box-shadow: 0 0 12px rgba(0, 255, 136, 0.2);
}

.bar-annual {
  background: linear-gradient(180deg, var(--volt), rgba(255, 221, 0, 0.3));
  box-shadow: 0 0 12px rgba(255, 221, 0, 0.2);
}

.bar-val {
  position: absolute;
  top: -20px;
  left: 50%;
  transform: translateX(-50%);
  font-family: var(--mono);
  font-size: 10px;
  color: var(--txt);
  white-space: nowrap;
}

/* ==================== 模块4：价格走势图 ==================== */
.chart-container {
  background: var(--void);
  border: 1px solid var(--edge);
  border-radius: 6px;
  padding: 8px;
  overflow: hidden;
}

.price-svg {
  width: 100%;
  height: auto;
  display: block;
}

.grid-line {
  stroke: var(--edge);
  stroke-width: 0.5;
  stroke-dasharray: 3 3;
  opacity: 0.5;
}

.y-label {
  fill: var(--txt3);
  font-family: var(--mono);
  font-size: 9px;
}

.price-line {
  stroke: var(--neon);
  stroke-width: 2;
  fill: none;
  filter: drop-shadow(0 0 4px rgba(0, 240, 255, 0.4));
}

.price-area {
  fill: url(#areaGrad);
  opacity: 0.15;
}

.dot-max {
  fill: var(--neon);
  filter: drop-shadow(0 0 6px rgba(0, 240, 255, 0.6));
}

.dot-min {
  fill: var(--plasma);
  filter: drop-shadow(0 0 6px rgba(255, 68, 0, 0.6));
}

.anno-text {
  font-family: var(--mono);
  font-size: 10px;
  font-weight: 700;
}

.neon-text {
  fill: var(--neon);
}

.plasma-text {
  fill: var(--plasma);
}

.tooltip-line {
  stroke: var(--rim);
  stroke-width: 0.8;
  stroke-dasharray: 4 2;
  opacity: 0.6;
}

.tooltip-box {
  fill: var(--card2);
  stroke: var(--rim);
  stroke-width: 1;
}

.tooltip-text {
  fill: var(--neon);
  font-family: var(--mono);
  font-size: 10px;
}

.chart-legend {
  display: flex;
  gap: 20px;
  margin-top: 10px;
  padding-left: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: var(--txt2);
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.neon-dot {
  background: var(--neon);
  box-shadow: 0 0 6px var(--neon);
}

.plasma-dot {
  background: var(--plasma);
  box-shadow: 0 0 6px var(--plasma);
}

.legend-line {
  width: 16px;
  height: 2px;
  background: var(--neon);
  box-shadow: 0 0 4px var(--neon);
}

/* ==================== 模块5：代码对照 ==================== */
.code-panel .panel-body {
  padding: 16px 20px;
}

.code-steps-nav {
  display: flex;
  gap: 6px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.step-btn {
  padding: 8px 14px;
  background: var(--card);
  border: 1px solid var(--edge);
  border-radius: 5px;
  color: var(--txt2);
  font-family: var(--exo), sans-serif;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.step-btn:hover {
  border-color: var(--rim);
  color: var(--txt);
}

.step-btn.active {
  background: rgba(0, 240, 255, 0.08);
  border-color: var(--neon);
  color: var(--neon);
}

.code-compare {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.code-col {
  background: var(--void);
  border: 1px solid var(--edge);
  border-radius: 6px;
  overflow: hidden;
}

.col-header {
  padding: 10px 16px;
  background: var(--card);
  border-bottom: 1px solid var(--edge);
  font-family: var(--orb);
  font-size: 10px;
  letter-spacing: 2px;
  color: var(--txt2);
}

.code-block {
  margin: 0;
  padding: 16px;
  background: transparent;
  overflow-x: auto;
  font-family: var(--mono);
  font-size: 12px;
  line-height: 1.7;
  color: var(--txt);
  white-space: pre;
}

.result-block {
  color: var(--neon2);
  background: rgba(0, 255, 136, 0.02);
}

/* ==================== 响应式 ==================== */
@media (max-width: 1024px) {
  .main-grid {
    grid-template-columns: 1fr;
  }

  .code-compare {
    grid-template-columns: 1fr;
  }

  .summary-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .stock-page {
    padding: 12px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .module-nav {
    flex-wrap: wrap;
  }

  .nav-tab {
    flex: none;
    padding: 8px 10px;
    font-size: 11px;
  }

  .vol-cards {
    grid-template-columns: 1fr;
  }

  .code-steps-nav {
    flex-direction: column;
  }
}
</style>
