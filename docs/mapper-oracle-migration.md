# Mapper XML Oracle 语法迁移方案

> 排查日期：2026-06-22  
> 目标数据库：OceanBase Oracle 模式  
> 排查范围：`src/main/resources/mapper/` 下全部 17 个 Mapper XML 文件

---

## 1. 问题分类总览

| 类别 | MySQL 写法 | Oracle 写法 | 影响文件数 | 影响处数 |
|------|-----------|-------------|-----------|---------|
| **[A] 时间函数** | `NOW()` | `SYSDATE` | 6 | 14 |
| **[B] 三参数 CONCAT** | `CONCAT(a, b, c)` | `a \|\| b \|\| c` | 2 | 4 |
| **[C] 二参数 CONCAT** | `CONCAT(a, b)` | `a \|\| b` | 3 | 6 |
| **[D] IF 条件函数** | `IF(cond, a, b)` | `CASE WHEN cond THEN a ELSE b END` | 1 | 1 |
| **[E] LIMIT 分页** | `LIMIT n OFFSET m` | `OFFSET m ROWS FETCH NEXT n ROWS ONLY` | 1 | 2 |
| **[F] sysdate 大小写** | `sysdate`(小写) | `SYSDATE`(大写，规范) | 1 | 1 |

**总计：9个文件，28处需修改。**

---

## 2. 逐类详细说明

### [A] 时间函数 `NOW()` → `SYSDATE`

**影响说明：** `NOW()` 是 MySQL 特有函数，Oracle 不支持。Oracle 使用 `SYSDATE` 返回当前日期时间。

**影响范围：**

| 文件 | 行号 | 上下文 |
|------|------|--------|
| `CompetitorMapper.xml` | 74 | `INSERT INTO` — created_time, updated_time |
| `CompetitorMapper.xml` | 81 | `UPDATE` — updated_time |
| `CompetitorMapper.xml` | 86 | `UPDATE` — updated_time |
| `CompetitorMapper.xml` | 90 | `UPDATE` — updated_time |
| `CompetitorSnapshotMapper.xml` | 36 | `INSERT INTO` — created_time |
| `ContactMapper.xml` | 75 | `INSERT INTO` — created_time, updated_time |
| `ContactMapper.xml` | 86 | `UPDATE` — updated_time |
| `ContactMapper.xml` | 92 | `UPDATE` — updated_time |
| `ContactMapper.xml` | 97 | `UPDATE` — updated_time |
| `ContactSnapshotMapper.xml` | 35 | `INSERT INTO` — created_time |
| `TicketApprovalMapper.xml` | 23 | `INSERT INTO` — approval_time |
| `TicketMapper.xml` | 22 | `INSERT INTO` — submit_time, created_time, updated_time |
| `TicketMapper.xml` | 29 | `UPDATE` — finish_time (IF 内) |
| `TicketMapper.xml` | 30 | `UPDATE` — updated_time |

**修改示例：**
```xml
<!-- 修改前 -->
VALUES (#{submitterId}, #{currentHandlerId}, NOW(), NOW(), NOW())

<!-- 修改后 -->
VALUES (#{submitterId}, #{currentHandlerId}, SYSDATE, SYSDATE, SYSDATE)
```

---

### [B] 三参数 `CONCAT(a, b, c)` → `a || b || c`

**影响说明：** MySQL 的 `CONCAT()` 支持任意数量参数。Oracle 的 `CONCAT()` 只支持 2 个参数。在 Oracle 中用 `||` 运算符拼接字符串。

**影响范围：**

| 文件 | 行号 | 原代码 |
|------|------|--------|
| `CompetitorMapper.xml` | 40 | `CONCAT('%', #{keyword}, '%')` |
| `CompetitorMapper.xml` | 41 | `CONCAT('%', #{keyword}, '%')` |
| `ContactMapper.xml` | 40 | `CONCAT('%', #{keyword}, '%')` |
| `ContactMapper.xml` | 41 | `CONCAT('%', #{keyword}, '%')` |

**修改示例：**
```xml
<!-- 修改前 -->
AND (c.competitor_name LIKE CONCAT('%', #{keyword}, '%')

<!-- 修改后 -->
AND (c.competitor_name LIKE '%' || #{keyword} || '%'
```

---

### [C] 二参数 `CONCAT(a, b)` → `a || b`

**影响说明：** 虽然 Oracle 的 `CONCAT(a, b)` 支持 2 个参数，但 `||` 是 Oracle 标准写法，性能更好且风格统一。

**影响范围：**

| 文件 | 行号 | 原代码 |
|------|------|--------|
| `BizServiceMapper.xml` | 8 | `CONCAT(service_name, ' (', service_measure, ')')` — **3参数，归类到 [B]** |
| `DashboardMapper.xml` | 21 | `LIKE CONCAT(#{year}, '%')` |
| `DashboardMapper.xml` | 51 | `LIKE CONCAT(#{year}, '%')` |
| `DashboardMapper.xml` | 79 | `LIKE CONCAT(#{year}, '%')` |
| `DashboardMapper.xml` | 88 | `LIKE CONCAT(#{year}, '%')` |
| `GroupMapper.xml` | 10 | `LIKE CONCAT('%', #{query.groupName}, '%')` — **3参数，归类到 [B]** |
| `OrgMapper.xml` | 23 | `LIKE CONCAT(#{orgPathPrefix}, '%')` |

**修改示例：**
```xml
<!-- 修改前 -->
WHERE revenue_month LIKE CONCAT(#{year}, '%')

<!-- 修改后 -->
WHERE revenue_month LIKE #{year} || '%'
```

**特别说明：** `BizServiceMapper.xml` 行 8 的 `CONCAT` 用于 SELECT 子句，非 LIKE：
```xml
<!-- 修改前 -->
CONCAT(service_name, ' (', service_measure, ')') AS serviceName

<!-- 修改后 -->
service_name || ' (' || service_measure || ')' AS serviceName
```

---

### [D] `IF(condition, a, b)` → `CASE WHEN`

**影响说明：** `IF()` 是 MySQL 流程控制函数，Oracle 不支持。Oracle 使用 `CASE WHEN ... THEN ... ELSE ... END`。

**影响范围：**

| 文件 | 行号 | 原代码 |
|------|------|--------|
| `TicketMapper.xml` | 29 | `finish_time = IF(#{status} IN ('通过','驳回'), NOW(), finish_time)` |

**修改示例：**
```xml
<!-- 修改前 -->
finish_time = IF(#{status} IN ('通过','驳回'), NOW(), finish_time),

<!-- 修改后 -->
finish_time = CASE WHEN #{status} IN ('通过','驳回') THEN SYSDATE ELSE finish_time END,
```

---

### [E] `LIMIT ... OFFSET` → Oracle 分页

**影响说明：** `LIMIT n OFFSET m` 是 MySQL 分页语法，Oracle 12c+ 使用 `OFFSET m ROWS FETCH NEXT n ROWS ONLY`。

**⚠️ 注意：** MyBatis 的 `#{limit}` 和 `#{offset}` 在 Oracle 中不能直接放在 `OFFSET ... FETCH NEXT` 后面。Oracle 要求这些值必须是**字面量或绑定变量**，但 FETCH NEXT 语法本身对变量的支持有限。推荐使用 **ROWNUM 嵌套子查询** 方案（兼容所有 Oracle 版本）。

**影响范围：**

| 文件 | 行号 | 原代码 |
|------|------|--------|
| `GroupMapper.xml` | 89 | `LIMIT #{limit} OFFSET #{offset}` |
| `TicketMapper.xml` | 44 | `ORDER BY id DESC LIMIT 1` |

**修改方案（ROWNUM 方式）：**

```sql
-- GroupMapper.xml: 分页查询（修改前）
SELECT ... FROM ...
WHERE ...
ORDER BY g.updated_time DESC, g.id DESC
LIMIT #{limit} OFFSET #{offset}

-- GroupMapper.xml: 分页查询（修改后）
SELECT * FROM (
  SELECT t.*, ROWNUM rn FROM (
    SELECT ... FROM ...
    WHERE ...
    ORDER BY g.updated_time DESC, g.id DESC
  ) t
  WHERE ROWNUM &lt;= #{limit} + #{offset}
) WHERE rn > #{offset}
```

```sql
-- TicketMapper.xml: 取最新一条（修改前）
SELECT * FROM szzw.t_yqyc_wf_ticket
WHERE ...
ORDER BY id DESC LIMIT 1

-- TicketMapper.xml: 取最新一条（修改后）
SELECT * FROM (
  SELECT * FROM szzw.t_yqyc_wf_ticket
  WHERE ...
  ORDER BY id DESC
) WHERE ROWNUM = 1
```

---

### [F] `sysdate` 大小写规范化

**影响说明：** Oracle 的 `SYSDATE` 不区分大小写，`sysdate` 可以正常执行。但为保持代码规范统一，建议统一为大写 `SYSDATE`。

| 文件 | 行号 | 原代码 |
|------|------|--------|
| `LoginLogMapper.xml` | 7 | `VALUES (..., sysdate, ...)` |

---

## 3. 逐文件修改清单

### 3.1 CompetitorMapper.xml（4 处）

| 行 | 类别 | 修改前 | 修改后 |
|----|------|--------|--------|
| 40 | [B] | `LIKE CONCAT('%', #{keyword}, '%')` | `LIKE '%' \|\| #{keyword} \|\| '%'` |
| 41 | [B] | `LIKE CONCAT('%', #{keyword}, '%')` | `LIKE '%' \|\| #{keyword} \|\| '%'` |
| 74 | [A] | `NOW(), #{updatedBy}, NOW()` | `SYSDATE, #{updatedBy}, SYSDATE` |
| 81 | [A] | `updated_time = NOW()` | `updated_time = SYSDATE` |
| 86 | [A] | `updated_time = NOW()` | `updated_time = SYSDATE` |
| 90 | [A] | `updated_time = NOW()` | `updated_time = SYSDATE` |

### 3.2 ContactMapper.xml（4 处）

| 行 | 类别 | 修改前 | 修改后 |
|----|------|--------|--------|
| 40 | [B] | `LIKE CONCAT('%', #{keyword}, '%')` | `LIKE '%' \|\| #{keyword} \|\| '%'` |
| 41 | [B] | `LIKE CONCAT('%', #{keyword}, '%')` | `LIKE '%' \|\| #{keyword} \|\| '%'` |
| 75 | [A] | `NOW(), #{updatedBy}, NOW()` | `SYSDATE, #{updatedBy}, SYSDATE` |
| 86 | [A] | `updated_time = NOW()` | `updated_time = SYSDATE` |
| 92 | [A] | `updated_time = NOW()` | `updated_time = SYSDATE` |
| 97 | [A] | `updated_time = NOW()` | `updated_time = SYSDATE` |

### 3.3 TicketMapper.xml（3 处）

| 行 | 类别 | 修改前 | 修改后 |
|----|------|--------|--------|
| 22 | [A] | `NOW(), NOW(), NOW()` | `SYSDATE, SYSDATE, SYSDATE` |
| 29 | [D] | `finish_time = IF(#{status} IN ('通过','驳回'), NOW(), finish_time)` | `finish_time = CASE WHEN #{status} IN ('通过','驳回') THEN SYSDATE ELSE finish_time END` |
| 30 | [A] | `updated_time = NOW()` | `updated_time = SYSDATE` |
| 44 | [E] | `ORDER BY id DESC LIMIT 1` | `WHERE ROWNUM = 1`（外包一层子查询） |

### 3.4 DashboardMapper.xml（4 处）

| 行 | 类别 | 修改前 | 修改后 |
|----|------|--------|--------|
| 21 | [C] | `LIKE CONCAT(#{year}, '%')` | `LIKE #{year} \|\| '%'` |
| 51 | [C] | `LIKE CONCAT(#{year}, '%')` | `LIKE #{year} \|\| '%'` |
| 79 | [C] | `LIKE CONCAT(#{year}, '%')` | `LIKE #{year} \|\| '%'` |
| 88 | [C] | `LIKE CONCAT(#{year}, '%')` | `LIKE #{year} \|\| '%'` |

### 3.5 GroupMapper.xml（2 处）

| 行 | 类别 | 修改前 | 修改后 |
|----|------|--------|--------|
| 10 | [B] | `LIKE CONCAT('%', #{query.groupName}, '%')` | `LIKE '%' \|\| #{query.groupName} \|\| '%'` |
| 89 | [E] | `LIMIT #{limit} OFFSET #{offset}` | ROWNUM 嵌套子查询 |

### 3.6 CompetitorSnapshotMapper.xml（1 处）

| 行 | 类别 | 修改前 | 修改后 |
|----|------|--------|--------|
| 36 | [A] | `NULL, NOW())` | `NULL, SYSDATE)` |

### 3.7 ContactSnapshotMapper.xml（1 处）

| 行 | 类别 | 修改前 | 修改后 |
|----|------|--------|--------|
| 35 | [A] | `NULL, NOW())` | `NULL, SYSDATE)` |

### 3.8 TicketApprovalMapper.xml（1 处）

| 行 | 类别 | 修改前 | 修改后 |
|----|------|--------|--------|
| 23 | [A] | `#{approvalComment}, NOW())` | `#{approvalComment}, SYSDATE)` |

### 3.9 BizServiceMapper.xml（1 处）

| 行 | 类别 | 修改前 | 修改后 |
|----|------|--------|--------|
| 8 | [B] | `CONCAT(service_name, ' (', service_measure, ')')` | `service_name \|\| ' (' \|\| service_measure \|\| ')'` |

### 3.10 OrgMapper.xml（1 处）

| 行 | 类别 | 修改前 | 修改后 |
|----|------|--------|--------|
| 23 | [C] | `LIKE CONCAT(#{orgPathPrefix}, '%')` | `LIKE #{orgPathPrefix} \|\| '%'` |

### 3.11 LoginLogMapper.xml（1 处）

| 行 | 类别 | 修改前 | 修改后 |
|----|------|--------|--------|
| 7 | [F] | `sysdate` | `SYSDATE`（规范化，功能无差异） |

---

## 4. 无需修改的文件

以下 7 个 Mapper 文件没有 MySQL 特有语法，无需修改：

| 文件 | 原因 |
|------|------|
| `DataScopeMapper.xml` | 仅含 SELECT + IN 子查询 |
| `GroupDetailDataMapper.xml` | 仅含 JOIN + GROUP BY，无 MySQL 特有函数 |
| `MenuMapper.xml` | 仅含 JOIN + IN，无 MySQL 特有函数 |
| `OpportunityMapper.xml` | 仅含 LEFT JOIN，无 MySQL 特有函数 |
| `RoleMapper.xml` | 仅含 JOIN，无 MySQL 特有函数 |
| `UserMapper.xml` | 仅含 LEFT JOIN，无 MySQL 特有函数 |
| `LoginLogMapper.xml` | 使用 `sysdate`（Oracle 兼容），仅需规范化大小写 |

---

## 5. 修改优先级建议

| 优先级 | 类别 | 说明 |
|--------|------|------|
| **P0 阻塞** | [A] NOW() → SYSDATE | 不修改则 DML 操作全部报错 |
| **P0 阻塞** | [B] 三参数 CONCAT → \|\| | 不修改则模糊查询全部报错 |
| **P0 阻塞** | [D] IF() → CASE WHEN | 不修改则工单状态更新报错 |
| **P0 阻塞** | [E] LIMIT → ROWNUM | 不修改则分页查询全部报错 |
| **P1 建议** | [C] 二参数 CONCAT → \|\| | 技术上 Oracle CONCAT(a,b) 可用，但 \|\| 更规范 |
| **P2 规范** | [F] sysdate → SYSDATE | 不影响功能，统一代码风格 |

---

## 6. 注意事项

1. **MyBatis `<foreach>` 的 `IN` 语法**在 Oracle 中正常支持 `IN (value1, value2, ...)`，无需修改。
2. **`#{param}` 占位符**在 Oracle 中正常支持，MyBatis 会自动处理类型映射。
3. **`COALESCE`**、**`CASE WHEN`**、**`COUNT`**、**`SUM`**、**`MAX`** 等标准 SQL 函数在 Oracle 中完全兼容。
4. **`LEFT JOIN`**、**`GROUP BY`**、**`ORDER BY`** 等标准 SQL 子句在 Oracle 中完全兼容。
5. Oracle 中 `||` 拼接 NULL 会返回 NULL（与 MySQL CONCAT 行为不同）。如果某个字段可能为 NULL，建议用 `COALESCE(col, '')` 包裹，或者使用 `NVL(col, '')`。
6. `ROWNUM` 分页方案中，`#{limit} + #{offset}` 不能在 SQL 中直接做加法，需要在 Java 端计算好 `endRow = limit + offset` 后传入。
