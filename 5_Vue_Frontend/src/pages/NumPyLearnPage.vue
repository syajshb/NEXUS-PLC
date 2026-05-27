<script setup>
import { ref, computed, watch, onMounted } from 'vue'

const activeSection = ref(0)

const sections = [
  {
    id: '2.2',
    title: '创建数组',
    subtitle: 'Array Creation',
    concepts: [
      'NumPy 的核心对象是 ndarray（N 维数组），它是一个同质数据的多维容器。',
      '可以通过 np.array() 从列表创建数组，也可以使用内置函数如 np.zeros()、np.ones()、np.arange()、np.linspace() 等快速生成特定结构的数组。',
      '所有数组元素必须是相同类型，NumPy 会自动推断或进行类型转换。'
    ],
    code: `import numpy as np

# 从列表创建
a = np.array([1, 2, 3, 4, 5])
print(a)           # [1 2 3 4 5]
print(type(a))     # <class 'numpy.ndarray'>

# 创建全零 / 全一数组
z = np.zeros((3, 4))
o = np.ones((2, 3))

# 等差数列
ar = np.arange(0, 10, 2)     # [0 2 4 6 8]
ls = np.linspace(0, 1, 5)    # [0. 0.25 0.5 0.75 1.]

# 随机数组
r = np.random.rand(2, 3)     # 2x3 均匀分布
print(r.shape)                # (2, 3)`,
    viz: {
      type: '1d',
      label: 'np.array([1, 2, 3, 4, 5])',
      data: [1, 2, 3, 4, 5],
      color: '--neon'
    }
  },
  {
    id: '2.3',
    title: '数组的数据类型',
    subtitle: 'Data Types (dtype)',
    concepts: [
      'NumPy 提供了丰富的数据类型：int32、int64、float32、float64、bool、complex 等。',
      '每个数组都有一个 dtype 属性，可以通过 .dtype 查看当前数组的数据类型。',
      '可以使用 astype() 方法进行类型转换，这在内存优化和数据精度控制中非常重要。'
    ],
    code: `import numpy as np

a = np.array([1, 2, 3])
print(a.dtype)          # int64 (取决于平台)

# 指定类型创建
b = np.array([1, 2, 3], dtype=np.float32)
print(b.dtype)          # float32

# 类型转换
c = a.astype(np.float64)
print(c.dtype)          # float64

# 常用类型
d = np.array([True, False, True])
print(d.dtype)          # bool

e = np.array([1+2j, 3+4j])
print(e.dtype)          # complex128`,
    viz: {
      type: '1d',
      label: 'dtype: float32',
      data: [1.0, 2.0, 3.0, 4.0],
      color: '--neon2'
    }
  },
  {
    id: '2.4',
    title: '数组的索引和切片',
    subtitle: 'Indexing & Slicing',
    concepts: [
      'NumPy 数组支持与 Python 列表类似的索引和切片操作，但功能更加强大。',
      '多维数组可以使用逗号分隔的索引：arr[row, col] 来访问特定元素。',
      '支持布尔索引（arr[arr > 0]）和花式索引（arr[[0, 2, 4]]）等高级操作。',
      '切片返回的是视图（view），修改切片会影响原数组。使用 .copy() 可创建独立副本。'
    ],
    code: `import numpy as np

a = np.array([10, 20, 30, 40, 50])

# 基本索引
print(a[0])       # 10
print(a[-1])      # 50

# 切片
print(a[1:4])     # [20 30 40]
print(a[::2])     # [10 30 50]

# 二维数组索引
b = np.array([[1, 2, 3],
              [4, 5, 6],
              [7, 8, 9]])
print(b[0, 1])    # 2
print(b[:, 1])    # [2 5 8]
print(b[0:2, 1:]) # [[2 3] [5 6]]

# 布尔索引
print(b[b > 5])   # [6 7 8 9]`,
    viz: {
      type: '2d',
      label: '3x3 Array',
      data: [[1, 2, 3], [4, 5, 6], [7, 8, 9]],
      color: '--volt'
    }
  },
  {
    id: '2.5',
    title: '数组的算术运算',
    subtitle: 'Arithmetic Operations',
    concepts: [
      'NumPy 数组支持逐元素（element-wise）的算术运算，无需编写循环。',
      '支持加减乘除、幂运算、取模等基本运算，也支持矩阵运算（@ 运算符或 np.dot()）。',
      '广播机制（Broadcasting）允许不同形状的数组进行运算，NumPy 会自动扩展较小的数组。'
    ],
    code: `import numpy as np

a = np.array([1, 2, 3, 4])
b = np.array([10, 20, 30, 40])

# 逐元素运算
print(a + b)       # [11 22 33 44]
print(a * b)       # [10 40 90 160]
print(b / a)       # [10. 10. 10. 10.]
print(a ** 2)      # [ 1  4  9 16]

# 标量广播
print(a + 100)     # [101 102 103 104]

# 矩阵运算
A = np.array([[1, 2], [3, 4]])
B = np.array([[5, 6], [7, 8]])
print(A @ B)       # [[19 22] [43 50]]
print(np.dot(A, B))`,
    viz: {
      type: '2d',
      label: 'Matrix A @ B',
      data: [[19, 22], [43, 50]],
      color: '--plasma'
    }
  },
  {
    id: '2.6',
    title: '通用函数',
    subtitle: 'Universal Functions (ufunc)',
    concepts: [
      '通用函数（ufunc）是对数组进行逐元素操作的函数，是 NumPy 高性能的核心所在。',
      '一元函数：np.sqrt()、np.exp()、np.log()、np.abs()、np.sin() 等。',
      '二元函数：np.add()、np.maximum()、np.minimum() 等。',
      '聚合函数：np.sum()、np.mean()、np.std()、np.min()、np.max() 等，支持 axis 参数。'
    ],
    code: `import numpy as np

a = np.array([1, 4, 9, 16, 25])

# 一元函数
print(np.sqrt(a))     # [1. 2. 3. 4. 5.]
print(np.exp(a[:3]))  # [  2.718  54.598 8103.084]
print(np.log(a[:3]))  # [0.    1.386 2.197]

# 二元函数
b = np.array([5, 3, 8, 1, 6])
print(np.maximum(a, b))  # [ 5  4  9 16 25]

# 聚合函数
c = np.array([[1, 2, 3], [4, 5, 6]])
print(np.sum(c))         # 21
print(np.sum(c, axis=0)) # [5 7 9]  (列求和)
print(np.sum(c, axis=1)) # [ 6 15]  (行求和)
print(np.mean(c))        # 3.5
print(np.std(c))         # 1.7078...`,
    viz: {
      type: '2d',
      label: 'np.sqrt() result',
      data: [[1.0, 2.0, 3.0], [4.0, 5.0, 6.0]],
      color: '--neon'
    }
  },
  {
    id: '2.7',
    title: '数组的重塑与转置',
    subtitle: 'Reshape & Transpose',
    concepts: [
      'reshape() 可以改变数组的形状而不改变其数据，前提是元素总数不变。',
      '转置（.T 或 np.transpose()）交换数组的行和列，对于多维数组可以指定轴的排列顺序。',
      'flatten() 和 ravel() 都可以将多维数组展平为一维，但 flatten() 返回副本，ravel() 返回视图。',
      'np.newaxis 和 np.expand_dims() 可以增加数组的维度。'
    ],
    code: `import numpy as np

a = np.arange(12)       # [ 0  1  2 ... 11]
print(a.shape)           # (12,)

# 重塑形状
b = a.reshape(3, 4)
# [[ 0  1  2  3]
#  [ 4  5  6  7]
#  [ 8  9 10 11]]

c = a.reshape(2, 2, 3)  # 3D array
print(c.shape)           # (2, 2, 3)

# 转置
d = np.array([[1, 2, 3], [4, 5, 6]])
print(d.T)
# [[1 4]
#  [2 5]
#  [3 6]]

# 展平
print(b.flatten())       # [ 0  1  2 ... 11]
print(b.ravel())         # [ 0  1  2 ... 11]

# 增加维度
e = np.array([1, 2, 3])
print(e[:, np.newaxis])  # [[1] [2] [3]]`,
    viz: {
      type: 'reshape',
      label: 'reshape(3, 4)',
      data: [[0, 1, 2, 3], [4, 5, 6, 7], [8, 9, 10, 11]],
      color: '--neon2'
    }
  },
  {
    id: '2.8',
    title: '数组的其他操作',
    subtitle: 'Other Operations',
    concepts: [
      'NumPy 提供了丰富的数组操作：拼接（concatenate、stack）、分割（split、hsplit、vsplit）。',
      '排序：np.sort() 和 arr.sort()，支持 axis 参数进行沿轴排序。',
      '条件操作：np.where() 实现三元表达式，np.select() 实现多条件选择。',
      '去重与集合操作：np.unique() 返回唯一值，支持返回索引和计数。'
    ],
    code: `import numpy as np

# 拼接
a = np.array([1, 2, 3])
b = np.array([4, 5, 6])
print(np.concatenate([a, b]))  # [1 2 3 4 5 6]

# 堆叠
c = np.stack([a, b])    # [[1 2 3] [4 5 6]]
print(np.vstack([a, b])) # [[1 2 3] [4 5 6]]
print(np.hstack([a, b])) # [1 2 3 4 5 6]

# 排序
d = np.array([3, 1, 4, 1, 5, 9, 2, 6])
print(np.sort(d))        # [1 1 2 3 4 5 6 9]

# 条件操作
e = np.array([1, -2, 3, -4, 5])
print(np.where(e > 0, e, 0))  # [1 0 3 0 5]

# 去重
f = np.array([1, 2, 2, 3, 3, 3])
print(np.unique(f))      # [1 2 3]`,
    viz: {
      type: '1d',
      label: 'np.sort() result',
      data: [1, 1, 2, 3, 4, 5, 6, 9],
      color: '--volt'
    }
  },
  {
    id: '2.9',
    title: '线性代数模块',
    subtitle: 'Linear Algebra (linalg)',
    concepts: [
      'np.linalg 模块提供了丰富的线性代数运算：矩阵乘法、行列式、逆矩阵、特征值分解等。',
      'np.linalg.inv() 计算逆矩阵，np.linalg.det() 计算行列式。',
      'np.linalg.eig() 计算特征值和特征向量，np.linalg.svd() 进行奇异值分解。',
      'np.linalg.solve() 用于求解线性方程组 Ax = b，np.linalg.lstsq() 用于最小二乘法。'
    ],
    code: `import numpy as np

A = np.array([[1, 2], [3, 4]])

# 行列式
print(np.linalg.det(A))    # -2.0000...

# 逆矩阵
A_inv = np.linalg.inv(A)
print(A @ A_inv)           # [[1. 0.] [0. 1.]]

# 特征值与特征向量
eigenvalues, eigenvectors = np.linalg.eig(A)
print(eigenvalues)         # [-0.372...  5.372...]

# 求解线性方程组 Ax = b
# x + 2y = 5
# 3x + 4y = 11
b = np.array([5, 11])
x = np.linalg.solve(A, b)
print(x)                   # [1. 2.]

# 奇异值分解
U, S, Vt = np.linalg.svd(A)`,
    viz: {
      type: '2d',
      label: 'A_inv (Inverse Matrix)',
      data: [[-2.0, 1.0], [1.5, -0.5]],
      color: '--mag'
    }
  },
  {
    id: '2.10',
    title: '随机数模块',
    subtitle: 'Random Module',
    concepts: [
      'np.random 模块提供了多种随机数生成功能：均匀分布、正态分布、整数随机等。',
      '推荐使用新的 Generator API：rng = np.random.default_rng()，比旧 API 更灵活。',
      '常用分布：uniform()、normal()、binomial()、poisson()、exponential() 等。',
      '支持设置随机种子（seed）以确保结果可复现，这在科学计算和机器学习中非常重要。'
    ],
    code: `import numpy as np

# 新版 Generator API（推荐）
rng = np.random.default_rng(42)

# 均匀分布 [0, 1)
print(rng.random((2, 3)))

# 正态分布 N(0, 1)
print(rng.normal(0, 1, (3, 3)))

# 指定范围整数
print(rng.integers(1, 100, size=10))

# 从数组中随机抽样
data = np.array([10, 20, 30, 40, 50])
print(rng.choice(data, size=3, replace=False))

# 打乱数组
arr = np.arange(10)
rng.shuffle(arr)
print(arr)

# 旧版 API（仍可用）
np.random.seed(42)
print(np.random.rand(3))`,
    viz: {
      type: '2d',
      label: 'rng.normal(0, 1, (3, 3))',
      data: [
        [0.30, -1.75, 0.94],
        [-0.11, 1.46, -0.55],
        [0.72, 0.60, -1.25]
      ],
      color: '--neon'
    }
  },
  {
    id: '2.11',
    title: '案例：计算股票收益率和波动率',
    subtitle: 'Case Study: Stock Returns & Volatility',
    concepts: [
      '使用 NumPy 计算股票的日收益率：r_t = (P_t - P_{t-1}) / P_{t-1}，也可使用对数收益率。',
      '波动率是收益率的标准差，年化波动率 = 日波动率 * sqrt(252)（一年约252个交易日）。',
      '使用 np.diff() 计算价格差分，np.log() 计算对数收益率，np.std() 计算标准差。',
      '可以进一步计算移动平均、夏普比率等金融指标，展示 NumPy 在量化分析中的强大能力。'
    ],
    code: `import numpy as np

# 模拟30天的股票价格
np.random.seed(42)
returns = np.random.normal(0.001, 0.02, 30)
prices = 100 * np.cumprod(1 + returns)

# 计算日收益率
daily_returns = np.diff(prices) / prices[:-1]
# 或使用对数收益率
log_returns = np.log(prices[1:] / prices[:-1])

# 计算波动率
daily_vol = np.std(daily_returns)
annual_vol = daily_vol * np.sqrt(252)

print(f"日均收益率: {np.mean(daily_returns):.4f}")
print(f"日波动率:   {daily_vol:.4f}")
print(f"年化波动率: {annual_vol:.4f}")

# 计算夏普比率 (假设无风险利率为3%)
risk_free = 0.03 / 252
sharpe = (np.mean(daily_returns) - risk_free) / daily_vol
print(f"夏普比率:   {sharpe:.4f}")

# 移动平均
ma5 = np.convolve(prices, np.ones(5)/5, mode='valid')
print(f"5日均线(最新): {ma5[-1]:.2f}")`,
    viz: {
      type: '1d',
      label: 'Stock Prices (30 days)',
      data: [100, 102, 101, 103, 105, 104, 106, 108, 107, 109, 111, 110, 108, 107, 109, 112, 114, 113, 115, 117, 116, 118, 120, 119, 121, 123, 122, 124, 126, 128],
      color: '--neon2'
    }
  }
]

const currentSection = computed(() => sections[activeSection.value])

const progress = computed(() => {
  return Math.round(((activeSection.value + 1) / sections.length) * 100)
})

const keyConcepts = computed(() => {
  const all = []
  sections.forEach(s => {
    s.concepts.forEach(c => {
      if (c.length < 30) all.push(c)
    })
  })
  return all.slice(0, 8)
})

const vizAnimating = ref(false)

function selectSection(index) {
  if (index === activeSection.value) return
  vizAnimating.value = true
  activeSection.value = index
  setTimeout(() => {
    vizAnimating.value = false
  }, 400)
}

function getCellColor(value, colorVar) {
  if (colorVar === '--neon') return `rgba(0, 240, 255, ${0.15 + Math.abs(value) * 0.05})`
  if (colorVar === '--neon2') return `rgba(0, 255, 136, ${0.15 + Math.abs(value) * 0.03})`
  if (colorVar === '--volt') return `rgba(255, 221, 0, ${0.15 + Math.abs(value) * 0.03})`
  if (colorVar === '--plasma') return `rgba(255, 68, 0, ${0.15 + Math.abs(value) * 0.01})`
  if (colorVar === '--mag') return `rgba(204, 0, 255, ${0.15 + Math.abs(value) * 0.03})`
  return `rgba(0, 240, 255, 0.15)`
}

function getCellBorder(colorVar) {
  const map = {
    '--neon': 'rgba(0, 240, 255, 0.4)',
    '--neon2': 'rgba(0, 255, 136, 0.4)',
    '--volt': 'rgba(255, 221, 0, 0.4)',
    '--plasma': 'rgba(255, 68, 0, 0.4)',
    '--mag': 'rgba(204, 0, 255, 0.4)'
  }
  return map[colorVar] || 'rgba(0, 240, 255, 0.4)'
}

function getTextColor(colorVar) {
  const map = {
    '--neon': 'var(--neon)',
    '--neon2': 'var(--neon2)',
    '--volt': 'var(--volt)',
    '--plasma': 'var(--plasma)',
    '--mag': 'var(--mag)'
  }
  return map[colorVar] || 'var(--neon)'
}

onMounted(() => {
  vizAnimating.value = false
})
</script>

<template>
  <div class="numpy-page">
    <!-- Header -->
    <div class="np-header">
      <div class="np-header-left">
        <span class="np-logo">NP</span>
        <div class="np-header-info">
          <h1 class="np-title">NumPy 学习中心</h1>
          <span class="np-subtitle">CHAPTER 02 // ARRAY OPERATIONS</span>
        </div>
      </div>
      <div class="np-header-right">
        <div class="np-progress-wrap">
          <span class="np-progress-label">学习进度</span>
          <div class="np-progress-bar">
            <div class="np-progress-fill" :style="{ width: progress + '%' }"></div>
          </div>
          <span class="np-progress-pct">{{ progress }}%</span>
        </div>
      </div>
    </div>

    <!-- Body -->
    <div class="np-body">
      <!-- Sidebar -->
      <aside class="np-sidebar">
        <div class="np-sidebar-label">SECTIONS</div>
        <div class="np-sidebar-list">
          <div
            v-for="(sec, idx) in sections"
            :key="sec.id"
            :class="['np-nav-item', { active: activeSection === idx }]"
            @click="selectSection(idx)"
          >
            <span class="np-nav-id">{{ sec.id }}</span>
            <span class="np-nav-title">{{ sec.title }}</span>
            <span class="np-nav-sub">{{ sec.subtitle }}</span>
            <div v-if="activeSection === idx" class="np-nav-indicator"></div>
          </div>
        </div>
      </aside>

      <!-- Main Content -->
      <main class="np-main">
        <div :class="['np-content', { 'np-content-enter': vizAnimating }]">
          <!-- Section Header -->
          <div class="np-section-header">
            <div class="np-section-id">{{ currentSection.id }}</div>
            <h2 class="np-section-title">{{ currentSection.title }}</h2>
            <span class="np-section-sub">{{ currentSection.subtitle }}</span>
            <div class="np-section-line"></div>
          </div>

          <!-- Concepts -->
          <div class="np-concepts">
            <div class="np-concepts-label">CONCEPTS</div>
            <div class="np-concept-list">
              <div v-for="(c, i) in currentSection.concepts" :key="i" class="np-concept-item">
                <span class="np-concept-bullet">{{ String(i + 1).padStart(2, '0') }}</span>
                <p class="np-concept-text">{{ c }}</p>
              </div>
            </div>
          </div>

          <!-- Code Block -->
          <div class="np-code-section">
            <div class="np-code-header">
              <span class="np-code-lang">PYTHON</span>
              <span class="np-code-dots">
                <i></i><i></i><i></i>
              </span>
              <span class="np-code-label">CODE EXAMPLE</span>
            </div>
            <pre class="np-code-block"><code>{{ currentSection.code }}</code></pre>
          </div>

          <!-- Array Visualization -->
          <div class="np-viz-section">
            <div class="np-viz-header">
              <span class="np-viz-label">ARRAY VISUALIZATION</span>
              <span class="np-viz-desc">{{ currentSection.viz.label }}</span>
            </div>
            <div class="np-viz-container">
              <!-- 1D Array -->
              <div v-if="currentSection.viz.type === '1d'" class="np-viz-1d">
                <div class="np-viz-bracket left">[</div>
                <div class="np-viz-cells-1d">
                  <div
                    v-for="(val, i) in currentSection.viz.data"
                    :key="i"
                    :class="['np-viz-cell', { 'np-viz-cell-animate': vizAnimating }]"
                    :style="{
                      backgroundColor: getCellColor(val, currentSection.viz.color),
                      borderColor: getCellBorder(currentSection.viz.color),
                      color: getTextColor(currentSection.viz.color),
                      animationDelay: (i * 0.05) + 's'
                    }"
                  >
                    <span class="np-viz-val">{{ val }}</span>
                    <span class="np-viz-idx">[{{ i }}]</span>
                  </div>
                </div>
                <div class="np-viz-bracket right">]</div>
              </div>

              <!-- 2D Array -->
              <div v-else-if="currentSection.viz.type === '2d'" class="np-viz-2d">
                <div class="np-viz-matrix">
                  <div class="np-viz-bracket-top">
                    <span v-for="(row, ri) in currentSection.viz.data" :key="'bt'+ri" class="np-viz-corner-tl"></span>
                  </div>
                  <div class="np-viz-rows">
                    <div
                      v-for="(row, ri) in currentSection.viz.data"
                      :key="ri"
                      class="np-viz-row"
                    >
                      <span class="np-viz-bracket-side">[</span>
                      <div
                        v-for="(val, ci) in row"
                        :key="ci"
                        :class="['np-viz-cell', { 'np-viz-cell-animate': vizAnimating }]"
                        :style="{
                          backgroundColor: getCellColor(val, currentSection.viz.color),
                          borderColor: getCellBorder(currentSection.viz.color),
                          color: getTextColor(currentSection.viz.color),
                          animationDelay: ((ri * row.length + ci) * 0.04) + 's'
                        }"
                      >
                        <span class="np-viz-val">{{ typeof val === 'number' ? (Number.isInteger(val) ? val : val.toFixed(2)) : val }}</span>
                        <span class="np-viz-idx">[{{ ri }},{{ ci }}]</span>
                      </div>
                      <span class="np-viz-bracket-side">]</span>
                    </div>
                  </div>
                </div>
                <div class="np-viz-shape-info">
                  <span>shape: ({{ currentSection.viz.data.length }}, {{ currentSection.viz.data[0].length }})</span>
                  <span>ndim: 2</span>
                  <span>size: {{ currentSection.viz.data.length * currentSection.viz.data[0].length }}</span>
                </div>
              </div>

              <!-- Reshape Animation -->
              <div v-else-if="currentSection.viz.type === 'reshape'" class="np-viz-reshape">
                <div class="np-viz-reshape-before">
                  <span class="np-viz-reshape-label">BEFORE: shape(12,)</span>
                  <div class="np-viz-cells-1d compact">
                    <div
                      v-for="(val, i) in 12"
                      :key="'bf'+i"
                      class="np-viz-cell tiny"
                      :style="{
                        backgroundColor: `rgba(0, 255, 136, ${0.1 + (i / 12) * 0.2})`,
                        borderColor: 'rgba(0, 255, 136, 0.3)',
                        color: 'var(--neon2)',
                        animationDelay: (i * 0.03) + 's'
                      }"
                    >
                      {{ i }}
                    </div>
                  </div>
                </div>
                <div class="np-viz-reshape-arrow">
                  <svg viewBox="0 0 40 24" fill="none" stroke="var(--neon2)" stroke-width="1.5">
                    <path d="M0 12h32M26 6l6 6-6 6"/>
                  </svg>
                  <span>reshape(3, 4)</span>
                </div>
                <div class="np-viz-reshape-after">
                  <span class="np-viz-reshape-label">AFTER: shape(3, 4)</span>
                  <div class="np-viz-matrix small">
                    <div
                      v-for="(row, ri) in currentSection.viz.data"
                      :key="ri"
                      class="np-viz-row"
                    >
                      <span class="np-viz-bracket-side">[</span>
                      <div
                        v-for="(val, ci) in row"
                        :key="ci"
                        class="np-viz-cell small"
                        :style="{
                          backgroundColor: `rgba(0, 255, 136, ${0.1 + (val / 12) * 0.2})`,
                          borderColor: 'rgba(0, 255, 136, 0.3)',
                          color: 'var(--neon2)',
                          animationDelay: ((ri * 4 + ci) * 0.04) + 's'
                        }"
                      >
                        {{ val }}
                      </div>
                      <span class="np-viz-bracket-side">]</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- Footer -->
    <div class="np-footer">
      <div class="np-footer-left">
        <span class="np-footer-label">KEY CONCEPTS</span>
        <div class="np-footer-tags">
          <span v-for="(kc, i) in keyConcepts" :key="i" class="np-footer-tag">{{ kc }}</span>
        </div>
      </div>
      <div class="np-footer-center">
        <span class="np-footer-section">{{ currentSection.id }} / {{ sections[sections.length - 1].id }}</span>
      </div>
      <div class="np-footer-right">
        <button
          class="np-nav-btn"
          :disabled="activeSection === 0"
          @click="selectSection(activeSection - 1)"
        >
          &larr; PREV
        </button>
        <button
          class="np-nav-btn"
          :disabled="activeSection === sections.length - 1"
          @click="selectSection(activeSection + 1)"
        >
          NEXT &rarr;
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ===== LAYOUT ===== */
.numpy-page {
  display: grid;
  grid-template-rows: 56px 1fr 52px;
  height: 100vh;
  width: 100vw;
  background: var(--void);
  position: relative;
  overflow: hidden;
}

/* ===== HEADER ===== */
.np-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: var(--panel);
  border-bottom: 1px solid var(--edge);
  position: relative;
  z-index: 50;
}

.np-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--neon), var(--neon2), transparent);
  opacity: 0.4;
}

.np-header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.np-logo {
  font-family: var(--orb);
  font-size: 18px;
  font-weight: 900;
  letter-spacing: 2px;
  background: linear-gradient(135deg, var(--neon), var(--neon2));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  padding: 6px 12px;
  border: 1px solid var(--edge);
  clip-path: polygon(6px 0%, 100% 0%, calc(100% - 6px) 100%, 0% 100%);
}

.np-title {
  font-family: var(--exo);
  font-size: 16px;
  font-weight: 600;
  color: var(--txt);
  letter-spacing: 2px;
  margin: 0;
}

.np-subtitle {
  font-family: var(--mono);
  font-size: 10px;
  color: var(--txt3);
  letter-spacing: 2px;
}

.np-header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.np-progress-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.np-progress-label {
  font-family: var(--mono);
  font-size: 10px;
  color: var(--txt3);
  letter-spacing: 1px;
}

.np-progress-bar {
  width: 120px;
  height: 4px;
  background: var(--ghost);
  border-radius: 2px;
  overflow: hidden;
}

.np-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--neon), var(--neon2));
  border-radius: 2px;
  transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.np-progress-pct {
  font-family: var(--mono);
  font-size: 12px;
  font-weight: 700;
  color: var(--neon);
  min-width: 36px;
  text-align: right;
}

/* ===== BODY ===== */
.np-body {
  display: grid;
  grid-template-columns: 260px 1fr;
  overflow: hidden;
}

/* ===== SIDEBAR ===== */
.np-sidebar {
  background: var(--panel);
  border-right: 1px solid var(--edge);
  overflow-y: auto;
  padding: 16px 0;
}

.np-sidebar-label {
  font-family: var(--mono);
  font-size: 9px;
  letter-spacing: 3px;
  color: var(--txt3);
  padding: 0 20px;
  margin-bottom: 12px;
}

.np-sidebar-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.np-nav-item {
  display: flex;
  flex-direction: column;
  padding: 12px 20px;
  cursor: pointer;
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-left: 2px solid transparent;
}

.np-nav-item:hover {
  background: rgba(0, 240, 255, 0.03);
  border-left-color: rgba(0, 240, 255, 0.2);
}

.np-nav-item.active {
  background: rgba(0, 240, 255, 0.06);
  border-left-color: var(--neon);
}

.np-nav-indicator {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 2px;
  height: 60%;
  background: var(--neon);
  box-shadow: 0 0 8px var(--neon);
}

.np-nav-id {
  font-family: var(--mono);
  font-size: 10px;
  color: var(--txt3);
  letter-spacing: 1px;
  margin-bottom: 4px;
}

.np-nav-item.active .np-nav-id {
  color: var(--neon);
}

.np-nav-title {
  font-family: var(--exo);
  font-size: 13px;
  font-weight: 600;
  color: var(--txt);
  letter-spacing: 1px;
  margin-bottom: 2px;
}

.np-nav-item.active .np-nav-title {
  color: #fff;
}

.np-nav-sub {
  font-family: var(--mono);
  font-size: 9px;
  color: var(--txt3);
  letter-spacing: 0.5px;
}

/* ===== MAIN CONTENT ===== */
.np-main {
  overflow-y: auto;
  padding: 28px 32px;
  background: var(--abyss);
  position: relative;
}

.np-content {
  animation: contentFadeIn 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
}

.np-content-enter {
  animation: contentSwitch 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
}

@keyframes contentFadeIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes contentSwitch {
  0% { opacity: 1; transform: translateX(0); }
  40% { opacity: 0; transform: translateX(-20px); }
  60% { opacity: 0; transform: translateX(20px); }
  100% { opacity: 1; transform: translateX(0); }
}

/* Section Header */
.np-section-header {
  margin-bottom: 28px;
  position: relative;
}

.np-section-id {
  font-family: var(--orb);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 3px;
  color: var(--neon);
  margin-bottom: 8px;
  display: inline-block;
  padding: 4px 12px;
  border: 1px solid rgba(0, 240, 255, 0.3);
  clip-path: polygon(4px 0%, 100% 0%, calc(100% - 4px) 100%, 0% 100%);
}

.np-section-title {
  font-family: var(--orb);
  font-size: 28px;
  font-weight: 800;
  color: #fff;
  letter-spacing: 2px;
  margin: 0 0 6px 0;
}

.np-section-sub {
  font-family: var(--mono);
  font-size: 11px;
  color: var(--txt3);
  letter-spacing: 2px;
}

.np-section-line {
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, var(--edge), transparent);
  margin-top: 16px;
}

/* Concepts */
.np-concepts {
  margin-bottom: 28px;
}

.np-concepts-label {
  font-family: var(--mono);
  font-size: 9px;
  letter-spacing: 3px;
  color: var(--txt3);
  margin-bottom: 12px;
}

.np-concept-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.np-concept-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 16px;
  background: var(--card);
  border: 1px solid var(--edge);
  border-left: 2px solid var(--rim);
  transition: all 0.3s;
}

.np-concept-item:hover {
  border-left-color: var(--neon);
  background: var(--card2);
}

.np-concept-bullet {
  font-family: var(--mono);
  font-size: 11px;
  color: var(--neon);
  min-width: 24px;
  padding-top: 1px;
}

.np-concept-text {
  font-family: var(--noto);
  font-size: 13px;
  color: var(--txt);
  line-height: 1.7;
  margin: 0;
}

/* Code Block */
.np-code-section {
  margin-bottom: 28px;
  border: 1px solid var(--edge);
  border-radius: 4px;
  overflow: hidden;
}

.np-code-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: var(--panel);
  border-bottom: 1px solid var(--edge);
}

.np-code-lang {
  font-family: var(--mono);
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 2px;
  color: var(--neon2);
  padding: 2px 8px;
  border: 1px solid rgba(0, 255, 136, 0.3);
}

.np-code-dots {
  display: flex;
  gap: 5px;
}

.np-code-dots i {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: block;
}

.np-code-dots i:nth-child(1) { background: var(--plasma); }
.np-code-dots i:nth-child(2) { background: var(--volt); }
.np-code-dots i:nth-child(3) { background: var(--neon2); }

.np-code-label {
  font-family: var(--mono);
  font-size: 9px;
  color: var(--txt3);
  letter-spacing: 2px;
  margin-left: auto;
}

.np-code-block {
  padding: 16px 20px;
  background: rgba(2, 6, 8, 0.8);
  overflow-x: auto;
  margin: 0;
}

.np-code-block code {
  font-family: var(--mono);
  font-size: 12px;
  line-height: 1.7;
  color: var(--txt2);
  white-space: pre;
}

/* ===== ARRAY VISUALIZATION ===== */
.np-viz-section {
  margin-bottom: 28px;
}

.np-viz-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.np-viz-label {
  font-family: var(--mono);
  font-size: 9px;
  letter-spacing: 3px;
  color: var(--txt3);
}

.np-viz-desc {
  font-family: var(--mono);
  font-size: 11px;
  color: var(--neon);
  letter-spacing: 1px;
}

.np-viz-container {
  padding: 24px;
  background: var(--card);
  border: 1px solid var(--edge);
  position: relative;
  overflow: hidden;
}

.np-viz-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 2px;
  height: 100%;
  background: linear-gradient(180deg, var(--neon), transparent);
  opacity: 0.5;
}

/* 1D Array */
.np-viz-1d {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.np-viz-bracket {
  font-family: var(--mono);
  font-size: 48px;
  color: var(--txt3);
  line-height: 1;
  user-select: none;
}

.np-viz-bracket.left {
  margin-right: 4px;
}

.np-viz-bracket.right {
  margin-left: 4px;
}

.np-viz-cells-1d {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.np-viz-cells-1d.compact {
  gap: 3px;
}

.np-viz-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 56px;
  height: 56px;
  padding: 6px;
  border: 1px solid;
  border-radius: 4px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.np-viz-cell:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.4);
  z-index: 2;
}

.np-viz-cell.tiny {
  min-width: 36px;
  height: 36px;
  font-size: 11px;
}

.np-viz-cell.small {
  min-width: 48px;
  height: 48px;
}

.np-viz-cell-animate {
  animation: cellPop 0.4s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
  opacity: 0;
}

@keyframes cellPop {
  from {
    opacity: 0;
    transform: scale(0.6);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.np-viz-val {
  font-family: var(--mono);
  font-size: 14px;
  font-weight: 700;
  line-height: 1;
}

.np-viz-cell.tiny .np-viz-val {
  font-size: 11px;
}

.np-viz-cell.small .np-viz-val {
  font-size: 12px;
}

.np-viz-idx {
  font-family: var(--mono);
  font-size: 8px;
  color: var(--txt3);
  margin-top: 2px;
  opacity: 0.7;
}

.np-viz-cell.tiny .np-viz-idx,
.np-viz-cell.small .np-viz-idx {
  display: none;
}

/* 2D Array */
.np-viz-2d {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.np-viz-matrix {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.np-viz-matrix.small {
  transform: scale(0.85);
  transform-origin: center top;
}

.np-viz-bracket-top {
  display: flex;
  gap: 6px;
  margin-bottom: 2px;
}

.np-viz-corner-tl {
  display: none;
}

.np-viz-rows {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.np-viz-row {
  display: flex;
  align-items: center;
  gap: 4px;
}

.np-viz-bracket-side {
  font-family: var(--mono);
  font-size: 48px;
  color: var(--txt3);
  line-height: 1;
  user-select: none;
}

.np-viz-shape-info {
  display: flex;
  gap: 20px;
  padding: 10px 20px;
  background: var(--panel);
  border: 1px solid var(--edge);
  border-radius: 4px;
}

.np-viz-shape-info span {
  font-family: var(--mono);
  font-size: 11px;
  color: var(--txt2);
  letter-spacing: 1px;
}

/* Reshape Visualization */
.np-viz-reshape {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.np-viz-reshape-before,
.np-viz-reshape-after {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.np-viz-reshape-label {
  font-family: var(--mono);
  font-size: 10px;
  color: var(--txt3);
  letter-spacing: 2px;
}

.np-viz-reshape-arrow {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--neon2);
}

.np-viz-reshape-arrow svg {
  width: 40px;
  height: 24px;
}

.np-viz-reshape-arrow span {
  font-family: var(--mono);
  font-size: 11px;
  letter-spacing: 1px;
  animation: arrowPulse 2s ease-in-out infinite;
}

@keyframes arrowPulse {
  0%, 100% { opacity: 0.7; }
  50% { opacity: 1; }
}

/* ===== FOOTER ===== */
.np-footer {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  padding: 0 24px;
  background: var(--panel);
  border-top: 1px solid var(--edge);
  position: relative;
  z-index: 50;
}

.np-footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--neon2), var(--neon), transparent);
  opacity: 0.3;
}

.np-footer-left {
  display: flex;
  align-items: center;
  gap: 12px;
  overflow: hidden;
}

.np-footer-label {
  font-family: var(--mono);
  font-size: 9px;
  letter-spacing: 2px;
  color: var(--txt3);
  white-space: nowrap;
}

.np-footer-tags {
  display: flex;
  gap: 6px;
  overflow-x: auto;
  white-space: nowrap;
}

.np-footer-tag {
  font-family: var(--mono);
  font-size: 9px;
  color: var(--txt2);
  padding: 3px 8px;
  border: 1px solid var(--edge);
  border-radius: 2px;
  white-space: nowrap;
  transition: all 0.2s;
}

.np-footer-tag:hover {
  border-color: var(--neon);
  color: var(--neon);
}

.np-footer-center {
  display: flex;
  justify-content: center;
}

.np-footer-section {
  font-family: var(--mono);
  font-size: 11px;
  color: var(--neon);
  letter-spacing: 2px;
}

.np-footer-right {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.np-nav-btn {
  font-family: var(--mono);
  font-size: 10px;
  letter-spacing: 2px;
  padding: 6px 16px;
  border: 1px solid var(--rim);
  background: transparent;
  color: var(--txt2);
  cursor: pointer;
  transition: all 0.3s;
  clip-path: polygon(4px 0%, 100% 0%, calc(100% - 4px) 100%, 0% 100%);
}

.np-nav-btn:hover:not(:disabled) {
  border-color: var(--neon);
  color: var(--neon);
  box-shadow: 0 0 16px rgba(0, 240, 255, 0.2);
}

.np-nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

/* ===== SCROLLBAR ===== */
.np-sidebar::-webkit-scrollbar,
.np-main::-webkit-scrollbar {
  width: 4px;
}

.np-sidebar::-webkit-scrollbar-track,
.np-main::-webkit-scrollbar-track {
  background: var(--void);
}

.np-sidebar::-webkit-scrollbar-thumb,
.np-main::-webkit-scrollbar-thumb {
  background: var(--edge);
  border-radius: 2px;
}

.np-sidebar::-webkit-scrollbar-thumb:hover,
.np-main::-webkit-scrollbar-thumb:hover {
  background: var(--rim);
}

/* ===== RESPONSIVE ===== */
@media (max-width: 900px) {
  .np-body {
    grid-template-columns: 1fr;
  }

  .np-sidebar {
    display: none;
  }

  .np-footer {
    grid-template-columns: 1fr;
    gap: 8px;
    padding: 8px 16px;
  }

  .np-footer-left,
  .np-footer-right {
    justify-content: center;
  }
}
</style>
