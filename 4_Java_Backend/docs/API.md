# 工业监控后端 API 接口文档

> 基于当前代码库（`HealthController`、`PlcDataController`）整理。  
> 默认服务地址：`http://localhost:8080`（见 `application.yml` 中 `server.port`）。

---

## 目录

1. [概述](#概述)
2. [业务接口](#业务接口)
   - [健康检查](#1-健康检查)
   - [PLC 数据列表](#2-plc-数据列表)
   - [最新一条 PLC 数据](#3-最新一条-plc-数据)
   - [按 ID 查询 PLC 数据](#4-按-id-查询-plc-数据)
3. [PlcData 数据模型](#plcdata-数据模型)
4. [Swagger / OpenAPI](#swagger--openapi)
5. [Spring Boot Actuator](#spring-boot-actuator)
6. [通用说明](#通用说明)

---

## 概述

| 类型 | 说明 |
|------|------|
| 协议 | HTTP |
| 数据格式 | JSON |
| 跨域 | 业务 Controller 已配置 `@CrossOrigin(origins = "*")` |
| 路径别名 | PLC 相关接口同时支持 `/api/plc-data` 与 `/api/data`（等价） |
| 根路径 | 访问 `http://localhost:8080/` 返回可用接口 JSON（非 Whitelabel 404） |

浏览器若直接打开 `http://localhost:8080/`，应看到 JSON 指引；业务与文档入口见下表。

| 用途 | URL |
|------|-----|
| 根路径（接口索引） | http://localhost:8080/ |
| 健康检查 | http://localhost:8080/api/health |
| Swagger UI | http://localhost:8080/swagger-ui.html |

---

## 业务接口

### 1. 健康检查

应用自定义健康接口（非 Actuator）。

| 项目 | 说明 |
|------|------|
| **方法** | `GET` |
| **路径** | `/api/health` |
| **Controller** | `HealthController` |

#### 请求示例

```bash
curl -s "http://localhost:8080/api/health"
```

#### 成功响应 `200 OK`

```json
{
  "status": "UP",
  "timestamp": "2026-05-15T14:30:00.123456789",
  "service": "Industrial Monitor Backend"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `status` | string | 固定为 `"UP"` |
| `timestamp` | string | 服务器当前时间（`LocalDateTime` 序列化） |
| `service` | string | 服务名称 |

---

### 2. PLC 数据列表

| 项目 | 说明 |
|------|------|
| **方法** | `GET` |
| **路径** | `/api/plc-data` 或 `/api/data` |
| **Controller** | `PlcDataController#list` |

#### 查询参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | integer | 否 | `0`（启用分页时） | 页码，从 `0` 开始；`page < 0` 时按 `0` 处理 |
| `size` | integer | 否 | `20`（启用分页时） | 每页条数；`size < 1` 时按 `1` 处理 |

**分页规则：**

- **未传** `page` 且 **未传** `size`：返回**全部**记录（JSON 数组）。
- **传入任一** `page` 或 `size`：返回**分页对象**（含 `items`、`total`、`page`、`size`）。

#### 2.1 全量列表（无分页参数）

**请求**

```bash
curl -s "http://localhost:8080/api/plc-data"
# 或
curl -s "http://localhost:8080/api/data"
```

**成功响应 `200 OK`** — JSON 数组，按 `id` 升序：

```json
[
  {
    "id": 1,
    "timestamp": "2026-05-15T10:00:00",
    "status": 1,
    "counter": 100,
    "temperature": 25.5,
    "humidity": 60.0,
    "pressure": 101.3,
    "powerConsumption": 12.5,
    "runHours": 120.0,
    "productCount": 500,
    "qualityRate": 98.5,
    "speed": 80.0,
    "efficiency": 92.0,
    "electricity": 1000.0,
    "water": 200.0,
    "gas": 50.0,
    "alarmActive": 0,
    "alarmLevel": 0,
    "alarmCounter": 0,
    "source": "PLC-01"
  }
]
```

#### 2.2 分页列表

**请求**

```bash
curl -s "http://localhost:8080/api/plc-data?page=0&size=20"
# 仅传 size 时 page 默认为 0
curl -s "http://localhost:8080/api/data?size=10"
```

**成功响应 `200 OK`**

```json
{
  "items": [ /* PlcData 对象数组 */ ],
  "total": 150,
  "page": 0,
  "size": 20
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `items` | array | 当前页 `PlcData` 列表 |
| `total` | number | 总记录数 |
| `page` | integer | 当前页码 |
| `size` | integer | 每页大小 |

---

### 3. 最新一条 PLC 数据

| 项目 | 说明 |
|------|------|
| **方法** | `GET` |
| **路径** | `/api/plc-data/latest` 或 `/api/data/latest` |
| **Controller** | `PlcDataController#latest` |
| **排序** | 按 `timestamp` 降序，再按 `id` 降序，取 1 条 |

#### 请求示例

```bash
curl -s "http://localhost:8080/api/plc-data/latest"
curl -s "http://localhost:8080/api/data/latest"
```

#### 成功响应 `200 OK`

单个 `PlcData` 对象（字段见 [PlcData 数据模型](#plcdata-数据模型)）。

#### 无数据 `404 Not Found`

响应体为空。

```bash
# 无数据时 HTTP 状态码为 404
curl -i "http://localhost:8080/api/plc-data/latest"
```

---

### 4. 按 ID 查询 PLC 数据

| 项目 | 说明 |
|------|------|
| **方法** | `GET` |
| **路径** | `/api/plc-data/{id}` 或 `/api/data/{id}` |
| **Controller** | `PlcDataController#getById` |
| **路径参数** | `id` — 记录主键（`Long`） |

#### 请求示例

```bash
curl -s "http://localhost:8080/api/plc-data/1"
curl -s "http://localhost:8080/api/data/42"
```

#### 成功响应 `200 OK`

```json
{
  "id": 1,
  "timestamp": "2026-05-15T10:00:00",
  "status": 1,
  "counter": 100,
  "temperature": 25.5,
  "humidity": 60.0,
  "pressure": 101.3,
  "powerConsumption": 12.5,
  "runHours": 120.0,
  "productCount": 500,
  "qualityRate": 98.5,
  "speed": 80.0,
  "efficiency": 92.0,
  "electricity": 1000.0,
  "water": 200.0,
  "gas": 50.0,
  "alarmActive": 0,
  "alarmLevel": 0,
  "alarmCounter": 0,
  "source": "PLC-01"
}
```

#### 记录不存在 `404 Not Found`

响应体为空。

---

## PlcData 数据模型

对应实体类 `com.project.entity.PlcData`，JSON 字段名为 **camelCase**（MyBatis 已开启 `map-underscore-to-camel-case`）。

| JSON 字段 | Java 类型 | 说明 |
|-----------|-----------|------|
| `id` | Long | 主键 |
| `timestamp` | string (ISO-8601) | 采集时间 |
| `status` | integer | 状态 |
| `counter` | integer | 计数器 |
| `temperature` | number | 温度 |
| `humidity` | number | 湿度 |
| `pressure` | number | 压力 |
| `powerConsumption` | number | 功耗 |
| `runHours` | number | 运行小时 |
| `productCount` | integer | 产量 |
| `qualityRate` | number | 良品率 |
| `speed` | number | 速度 |
| `efficiency` | number | 效率 |
| `electricity` | number | 用电量 |
| `water` | number | 用水量 |
| `gas` | number | 用气量 |
| `alarmActive` | integer | 告警是否激活 |
| `alarmLevel` | integer | 告警级别 |
| `alarmCounter` | integer | 告警计数 |
| `source` | string | 数据来源 |

> 数值型字段在 Java 中为 `BigDecimal` / `Integer`，JSON 中表现为 number；可能为 `null`（数据库无值时）。

---

## Swagger / OpenAPI

项目使用 **springdoc-openapi**（见 `pom.xml`），路径在 `application.yml` 中配置：

| 用途 | URL |
|------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/api-docs |

浏览器打开 Swagger UI 可交互调试上述 REST 接口。

```bash
# 获取 OpenAPI 描述
curl -s "http://localhost:8080/api-docs"
```

---

## Spring Boot Actuator

项目已引入 `spring-boot-starter-actuator`，未在配置中自定义 `management` 路径时，使用 Spring Boot 默认端点。

| 用途 | URL |
|------|-----|
| 健康检查（Actuator） | http://localhost:8080/actuator/health |

```bash
curl -s "http://localhost:8080/actuator/health"
```

典型响应示例（具体字段随 Spring Boot 版本与依赖略有差异）：

```json
{
  "status": "UP"
}
```

> **说明：** `/api/health` 为应用自定义接口；`/actuator/health` 为 Actuator 标准端点，二者用途不同，可同时使用。

---

## 通用说明

### 接口一览

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 服务说明与可用路径索引 |
| GET | `/api/health` | 应用健康检查 |
| GET | `/api/plc-data`、`/api/data` | PLC 数据列表（可选分页） |
| GET | `/api/plc-data/latest`、`/api/data/latest` | 最新一条 PLC 数据 |
| GET | `/api/plc-data/{id}`、`/api/data/{id}` | 按 ID 查询 |
| GET | `/swagger-ui.html` | Swagger UI |
| GET | `/api-docs` | OpenAPI 文档 |
| GET | `/actuator/health` | Actuator 健康检查 |

### HTTP 状态码

| 状态码 | 场景 |
|--------|------|
| 200 | 请求成功 |
| 404 | `latest` 或 `{id}` 查询无对应记录 |

### 数据源

PLC 数据来自 MySQL 表 `plc_data`（见 `PlcDataMapper`），需保证数据库 `industrial_monitor` 可连接且表中有数据。

---

*文档生成依据：`RootController.java`、`PlcDataController.java`、`HealthController.java`、`PlcData.java`、`application.yml`。*
