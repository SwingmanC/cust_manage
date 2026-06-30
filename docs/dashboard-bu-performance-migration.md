# 看板模块 BU 业绩表迁移方案

> 编写日期：2026-06-22  
> 目标：除集团相关数据外，看板统计统一改用 `t_yqyc_biz_bu_performance`，不再依赖 `t_yqyc_biz_group_revenue_monthly`

---

## 1. 当前数据源分析

### 1.1 `t_yqyc_biz_group_revenue_monthly`（集团月度收入，将被替换）

| 字段 | 类型 | 说明 |
|------|------|------|
| group_id | BIGINT | 集团ID |
| service_id | BIGINT | 业务科目ID |
| revenue_month | CHAR(7) | 月份 yyyy-MM |
| revenue_amount | DECIMAL(18,2) | 收入金额 |

### 1.2 `t_yqyc_biz_bu_performance`（BU 业绩，新数据源）

| 字段 | 类型 | 说明 |
|------|------|------|
| bu_org_id | BIGINT | BU 组织ID |
| service_id | BIGINT | 业务科目ID |
| stat_month | CHAR(7) | 统计月份 yyyy-MM |
| amount | DECIMAL(18,4) | 业绩数额 |

**关键差异：**
- 旧表以 `group_id` 为维度 → 需要 JOIN `t_yqyc_crm_group` 才能拿到 `bu_org_id`
- 新表直接以 `bu_org_id` 为维度 → 无需 JOIN 集团表，查询更简洁高效

---

## 2. 查询分类

### 2.1 保留不变（集团/组织相关，不涉及收入业绩）

| 方法 | SQL | 说明 |
|------|-----|------|
| `countTotalGroups` | `COUNT(1) FROM t_yqyc_crm_group` | 集团总数 |
| `countKeyCustomerGroups` | `COUNT(1) FROM t_yqyc_crm_group WHERE is_key_customer=1` | 要客集团数 |
| `countUnifiedPaymentGroups` | `COUNT(DISTINCT parent_group_id) FROM t_yqyc_crm_group_relation WHERE relation_type='统付'` | 统付集团数 |
| `selectBuOptions` | `SELECT id, org_name FROM t_yqyc_sys_org WHERE org_type='网格'` | BU下拉列表 |

### 2.2 已使用 BU 业绩表（保持不变）

| 方法 | 说明 |
|------|------|
| `buPerfByService` | 按业务汇总 BU 业绩 |
| `buPerfByBuService` | 按 BU+业务汇总 |
| `buMonthlyPerf` | 指定 BU 各月业绩 |

### 2.3 需从集团收入表迁移到 BU 业绩表（本次修改重点）

| 方法 | 旧表 | 说明 |
|------|------|------|
| `sumMonthRevenue` | `t_yqyc_biz_group_revenue_monthly` | 当月收入 |
| `sumAnnualRevenue` | `t_yqyc_biz_group_revenue_monthly` | 全年累计收入 |
| `revenueByBu` | `t_yqyc_biz_group_revenue_monthly` | 分BU收入 |
| `revenueByService` | `t_yqyc_biz_group_revenue_monthly` | 分业务收入 |
| `revenueTrendByMonth` | `t_yqyc_biz_group_revenue_monthly` | 全年收入按月趋势 |
| `buMonthlyRevenue` | `t_yqyc_biz_group_revenue_monthly` | 指定BU各月收入 |

---

## 3. 逐方法修改方案

### 3.1 `sumMonthRevenue` → 当月收入

```xml
<!-- 修改前 -->
SELECT COALESCE(SUM(revenue_amount), 0) AS total
FROM szzw.t_yqyc_biz_group_revenue_monthly
WHERE revenue_month = #{month}

<!-- 修改后 -->
SELECT COALESCE(SUM(amount), 0) AS total
FROM szzw.t_yqyc_biz_bu_performance
WHERE stat_month = #{month}
```

### 3.2 `sumAnnualRevenue` → 全年累计收入

```xml
<!-- 修改前 -->
SELECT COALESCE(SUM(revenue_amount), 0) AS total
FROM szzw.t_yqyc_biz_group_revenue_monthly
WHERE revenue_month LIKE #{year} || '%'

<!-- 修改后 -->
SELECT COALESCE(SUM(amount), 0) AS total
FROM szzw.t_yqyc_biz_bu_performance
WHERE stat_month LIKE #{year} || '%'
```

### 3.3 `revenueByBu` → 分BU收入

```xml
<!-- 修改前 -->
SELECT g.bu_org_id AS buOrgId, o.org_name AS buName,
       COALESCE(SUM(r.revenue_amount), 0) AS amount
FROM szzw.t_yqyc_biz_group_revenue_monthly r
JOIN szzw.t_yqyc_crm_group g ON r.group_id = g.id
JOIN szzw.t_yqyc_sys_org o ON g.bu_org_id = o.id
WHERE r.revenue_month = #{month} AND g.deleted_flag = 0
GROUP BY g.bu_org_id, o.org_name
ORDER BY amount DESC

<!-- 修改后 -->
SELECT p.bu_org_id AS buOrgId, o.org_name AS buName,
       COALESCE(SUM(p.amount), 0) AS amount
FROM szzw.t_yqyc_biz_bu_performance p
JOIN szzw.t_yqyc_sys_org o ON p.bu_org_id = o.id
WHERE p.stat_month = #{month}
GROUP BY p.bu_org_id, o.org_name
ORDER BY amount DESC
```

**优势：** 去掉了一张 JOIN（不再需要 `t_yqyc_crm_group`），性能提升。

### 3.4 `revenueByService` → 分业务收入

```xml
<!-- 修改前 -->
SELECT r.service_id AS serviceId, s.service_name AS serviceName,
       COALESCE(SUM(r.revenue_amount), 0) AS amount
FROM szzw.t_yqyc_biz_group_revenue_monthly r
JOIN szzw.t_yqyc_biz_service s ON r.service_id = s.id
WHERE r.revenue_month = #{month}
GROUP BY r.service_id, s.service_name
ORDER BY amount DESC

<!-- 修改后 -->
SELECT p.service_id AS serviceId, s.service_name AS serviceName,
       COALESCE(SUM(p.amount), 0) AS amount
FROM szzw.t_yqyc_biz_bu_performance p
JOIN szzw.t_yqyc_biz_service s ON p.service_id = s.id
WHERE p.stat_month = #{month}
GROUP BY p.service_id, s.service_name
ORDER BY amount DESC
```

### 3.5 `revenueTrendByMonth` → 全年收入按月趋势

```xml
<!-- 修改前 -->
SELECT revenue_month AS month, COALESCE(SUM(revenue_amount), 0) AS amount
FROM szzw.t_yqyc_biz_group_revenue_monthly
WHERE revenue_month LIKE #{year} || '%'
GROUP BY revenue_month
ORDER BY revenue_month

<!-- 修改后 -->
SELECT stat_month AS month, COALESCE(SUM(amount), 0) AS amount
FROM szzw.t_yqyc_biz_bu_performance
WHERE stat_month LIKE #{year} || '%'
GROUP BY stat_month
ORDER BY stat_month
```

### 3.6 `buMonthlyRevenue` → 指定BU各月收入

```xml
<!-- 修改前 -->
SELECT r.revenue_month AS month, COALESCE(SUM(r.revenue_amount), 0) AS amount
FROM szzw.t_yqyc_biz_group_revenue_monthly r
JOIN szzw.t_yqyc_crm_group g ON r.group_id = g.id
WHERE g.bu_org_id = #{buOrgId} AND r.revenue_month LIKE #{year} || '%'
GROUP BY r.revenue_month
ORDER BY r.revenue_month

<!-- 修改后 -->
SELECT stat_month AS month, COALESCE(SUM(amount), 0) AS amount
FROM szzw.t_yqyc_biz_bu_performance
WHERE bu_org_id = #{buOrgId} AND stat_month LIKE #{year} || '%'
GROUP BY stat_month
ORDER BY stat_month
```

**优势：** 去掉 JOIN `t_yqyc_crm_group`，直接按 `bu_org_id` 过滤。

---

## 4. 字段名变化汇总

| 字段 | 旧表 (`revenue_monthly`) | 新表 (`bu_performance`) |
|------|--------------------------|------------------------|
| 业务ID | `r.service_id` | `p.service_id` |
| 收入金额 | `r.revenue_amount` | `p.amount` |
| 月份 | `r.revenue_month` | `p.stat_month` |
| BU组织ID | `g.bu_org_id`（需JOIN集团表） | `p.bu_org_id`（直接取） |
| 别名（月份） | `AS month` | `AS month`（保持一致） |
| 别名（金额） | `AS amount` | `AS amount`（保持一致） |

> **注意：** 别名保持不变，确保上层 Service 和 Controller 返回给前端的 JSON 字段名不变。

---

## 5. Service 层适配

`DashboardServiceImpl.java` 中以下方法的调用**无需修改**（Mapper 接口方法名和返回结构保持一致）：

| 方法 | 调用位置 | 是否需改 |
|------|----------|----------|
| `sumMonthRevenue(month)` | `getTimeView()` L29 | 否 |
| `sumAnnualRevenue(year)` | `getTimeView()` L31, `getBuView()` L97 | 否 |
| `revenueByBu(month)` | `getTimeView()` L34 | 否 |
| `revenueByService(month)` | `getTimeView()` L35 | 否 |
| `revenueTrendByMonth(year)` | `getTimeView()` L37 | 否 |
| `buMonthlyRevenue(buOrgId, year)` | `getBuView()` L104 | 否 |

---

## 6. Service 层附带修复（bug）

### 6.1 `buPerfByBuService` 缺少 `svcCodes` 过滤

当前 Mapper XML 中 `buPerfByBuService` 没有使用传入的 `svcCodes` 参数：

```xml
<!-- 当前（有 bug） -->
<select id="buPerfByBuService" resultType="map">
    SELECT p.bu_org_id AS buOrgId, o.org_name AS buName,
           p.service_id AS serviceId, COALESCE(SUM(p.amount), 0) AS amount
    FROM szzw.t_yqyc_biz_bu_performance p
    JOIN szzw.t_yqyc_sys_org o ON p.bu_org_id = o.id
    WHERE p.stat_month = #{month}
    GROUP BY p.bu_org_id, o.org_name, p.service_id
</select>
```

修复方案：添加 `svcCodes` 过滤条件（使用 `<if>` + `<foreach>`）：

```xml
<select id="buPerfByBuService" resultType="map">
    SELECT p.bu_org_id AS buOrgId, o.org_name AS buName,
           p.service_id AS serviceId, COALESCE(SUM(p.amount), 0) AS amount
    FROM szzw.t_yqyc_biz_bu_performance p
    JOIN szzw.t_yqyc_sys_org o ON p.bu_org_id = o.id
    WHERE p.stat_month = #{month}
    <if test="svcCodes != null and svcCodes.size() > 0">
        AND p.service_id IN
        <foreach collection="svcCodes" item="code" open="(" separator="," close=")">
            #{code}
        </foreach>
    </if>
    GROUP BY p.bu_org_id, o.org_name, p.service_id
</select>
```

---

## 7. 修改清单

| 文件 | 修改内容 | 影响行 |
|------|----------|--------|
| `DashboardMapper.xml` | 6 个查询改数据源 `group_revenue_monthly` → `bu_performance` | L11-L81 |
| `DashboardMapper.xml` | `buPerfByBuService` 增加 `svcCodes` 过滤 | L65-L72 |
| `DashboardMapper.java` | 无需修改（接口签名不变） | — |
| `DashboardServiceImpl.java` | 无需修改（返回结构不变） | — |
| `DashboardController.java` | 无需修改 | — |

**总计：1 个文件，7 处修改。**

---

## 8. service_id 业务科目对照表

DashboardServiceImpl 中硬编码的 service_id 映射：

| service_id | 业务含义 | 使用位置 |
|-----------|----------|----------|
| 1 | 专线 | `getTimeView()` L48 |
| 12 | 语音专线 | `getTimeView()` L48 |
| 9 | ICT签约 | `getTimeView()` L53, `getBuView()` L106 |
| 10 | 宽带渗透率 | `getTimeView()` L71, `getBuView()` L108 |
| 11 | 关联人占比 | `getTimeView()` L74, `getBuView()` L109 |

> **注意：** 这些 service_id 值来源于 `t_yqyc_biz_service` 表的 `id`，需与实际数据一致。

---

## 9. 数据准备

在执行代码修改前，需确保 `t_yqyc_biz_bu_performance` 表已有数据。如果没有，需从集团月度收入表汇总生成：

```sql
-- 将集团收入按 BU + 业务 + 月份汇总后写入 BU 业绩表
INSERT INTO szzw.t_yqyc_biz_bu_performance
  (id, bu_org_id, service_id, stat_month, amount, source_system, created_time, updated_time)
SELECT
  seq_bu_performance.NEXTVAL,
  g.bu_org_id,
  r.service_id,
  r.revenue_month,
  SUM(r.revenue_amount),
  'MIGRATION',
  SYSDATE,
  SYSDATE
FROM szzw.t_yqyc_biz_group_revenue_monthly r
JOIN szzw.t_yqyc_crm_group g ON r.group_id = g.id
WHERE g.bu_org_id IS NOT NULL AND g.deleted_flag = 0
GROUP BY g.bu_org_id, r.service_id, r.revenue_month
```
