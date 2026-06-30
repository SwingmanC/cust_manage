# 我的看板 · 按 BU 统计 模块设计

> 编写日期：2026-06-26
> 范围：`/api/dashboard/bu`（`DashboardServiceImpl.getBuView`）及前端「我的看板 → 按 BU 统计」视图
> 现状：后端 `getBuView` 大面积为 TODO（4 张指标卡写死 0，2 个图返回空），部分图用的 service_id（10/11）与「按时间统计」已确定的口径不一致。本文档把该模块补齐。

---

## 1. 背景与现状

### 1.1 当前 `getBuView(buOrgId)`（`DashboardServiceImpl`）

```java
String year = 当前年份;
cards:
  annualProgress        = 0   // TODO
  dedicatedLineAnnual   = 0   // TODO
  ictAnnualContract     = 0   // TODO
  broadbandPenetration  = 0   // TODO
monthlyRevenue             = buMonthlyRevenue(buOrgId, year)            // 已实现（service 15）
dedicatedLineAndFusion     = 空列表  // TODO
monthlyIctContract         = buMonthlyPerf(buOrgId, 9, year)           // 已实现（ICT=9）
ictZcAndPolicy             = 空列表  // TODO
monthlyBroadbandPenetration= buMonthlyPerf(buOrgId, 10, year)          // service_id 待校正 → 19
monthlyBroadbandNew        = buMonthlyPerf(buOrgId, 11, year)          // service_id 待校正 → 22
```

### 1.2 前端（`ManagerDashboard.vue` 按 BU 视图）
- 顶部：BU 下拉选择（`getBuOptions`）。
- 4 张指标卡（`buCards`）：全年累计收入完成进度(%)、专线全年累计收入(万)、ICT 全年累计签约金额(万)、要客集团宽带渗透率(%)。
- 6 个图：月收入详情(柱)、专线月收入与融合产品销量(柱+线)、ICT 月签约额(柱)、ICT 政采数量与政策份额(线)、要客成员月宽带渗透率(线)、统付成员月宽带新增(柱)。

### 1.3 数据源
- `t_yqyc_biz_bu_performance`（`bu_org_id, service_id, stat_month(YYYYMM), amount`）：BU 维度各类业绩，按业务科目+月份。**绝大多数指标取自这里。**
- `t_yqyc_biz_annual_target`（`bu_org_id, service_id(可空), target_year, target_amount`）：年度目标，用于"完成进度"。
- `t_yqyc_sys_org`：BU 列表（`selectBuOptions` 已有）。

---

## 2. 业务科目（service_id）映射

与「按时间统计」保持一致；本模块新增 3 个暂未用到的科目用占位编号，**需业务确认**（标 ⚠️）。

| 业务含义 | service_id | 用途 | 说明 |
|---|---|---|---|
| 收入 | 15 | 收入相关卡/图 | 与时间统计一致 |
| 互联网专线 | 13 | 专线 | 两线之一 |
| 数据专线 | 14 | 专线 | 两线之一 |
| **专线（合计）** | 13 + 14 | 专线卡/图 | 两线规模 |
| ICT 签约 | 9 | ICT 卡/图 | 与时间统计一致 |
| 要客宽带渗透率 | 19 | 渗透率卡/图 | % |
| 关联人流量主用占比 | 20 | — | （本视图暂不展示） |
| 统付成员数 | 21 | — | （统付在时间视图） |
| 统付家宽新增 | 22 | 月宽带新增图 | 校正 11→22 |
| 融合产品销量 | ⚠️ 16（拟） | 专线+融合图 | **待确认** |
| ICT 政采数量 | ⚠️ 17（拟） | 政采+政策图 | **待确认** |
| ICT 政策份额 | ⚠️ 18（拟） | 政采+政策图 | **待确认** |

> 月份口径统一 `YYYYMM`（与全局一致）。`amount` 为 `DECIMAL(18,4)`，收入/金额单位"万元"，渗透率/占比单位"%"。

---

## 3. 指标卡设计（4 张）

每张卡 = 一个数值，由 Service 计算后放入 `result.cards`（key 与前端 `buCards` 一致）。

| 卡片 key | 含义/单位 | 计算口径 | 数据来源 |
|---|---|---|---|
| `annualProgress` | 全年累计收入完成进度 / % | BU 全年收入 ÷ BU 年度收入目标 × 100；无目标时返回 0（或只返回完成值，前端按有无目标切换展示） | 实际：`bu_performance`(bu, service 15, year)；目标：`annual_target`(bu, year) |
| `dedicatedLineAnnual` | 专线全年累计收入 / 万 | `SUM(amount)` where bu=X, service∈(13,14), year | `bu_performance` |
| `ictAnnualContract` | ICT 全年累计签约金额 / 万 | `SUM(amount)` where bu=X, service=9, year | `bu_performance` |
| `broadbandPenetration` | 要客集团宽带渗透率 / % | 取该 BU **最新月** 的 service 19 值（`amount`）；无数据则 0 | `bu_performance`(bu, service 19) 取 `MAX(stat_month)` |

> `annualProgress` 的"目标"取数：优先 `annual_target` 里 `bu_org_id=X AND target_year=year AND service_id=15`；没有则取 `service_id IS NULL`（总收入目标）；都没有则进度按 0 处理并在备注里说明"未设目标"。

---

## 4. 图表设计（6 个）

每个图返回一个数组，Service 放入对应 key。柱/线由前端 ECharts option 决定。

| 图 key | 标题 | 类型 | 数据口径 | service_id |
|---|---|---|---|---|
| `monthlyRevenue` | 月收入详情 | 柱 | 每月 amount | 15（已实现 `buMonthlyRevenue`） |
| `dedicatedLineAndFusion` | 专线月收入与融合产品销量 | 柱+线 | 每月 `{month, dedicatedLine, fusion}` | 专线 13+14 / 融合 ⚠️16 |
| `monthlyIctContract` | ICT 月签约额 | 柱 | 每月 amount | 9（已实现 `buMonthlyPerf(9)`） |
| `ictZcAndPolicy` | ICT 政采数量与政策份额 | 线+线 | 每月 `{month, govPurchase, policyShare}` | 政采 ⚠️17 / 政策份额 ⚠️18 |
| `monthlyBroadbandPenetration` | 要客成员月宽带渗透率 | 线 | 每月 amount | 19（**校正 10→19**） |
| `monthlyBroadbandNew` | 统付成员月宽带新增 | 柱 | 每月 amount | 22（**校正 11→22**） |

> 组合图（`dedicatedLineAndFusion`、`ictZcAndPolicy`）返回**每月一行、含两个数值字段**的结构，前端按字段拆成两个 series。

---

## 5. 新增 / 调整 Mapper 查询（`DashboardMapper`）

复用已有：`buMonthlyRevenue`、`buMonthlyPerf(buOrgId, serviceId, year)`、`selectBuOptions`。

新增（别名一律双引号；Oracle 模式 `SYSDATE`/`||`）：

### 5.1 `buAnnualSum` —— 单 BU 某年某(几)科目总和（卡 2/3，及卡 1 实际值）
```java
Map<String,Object> buAnnualSum(@Param("buOrgId") Long buOrgId,
                               @Param("year") String year,
                               @Param("svcCodes") List<String> svcCodes);
```
```xml
<select id="buAnnualSum" resultType="map">
    SELECT COALESCE(SUM(amount),0) AS "total"
    FROM szzw.t_yqyc_biz_bu_performance
    WHERE bu_org_id = #{buOrgId} AND stat_month LIKE #{year} || '%'
    <if test="svcCodes != null and svcCodes.size() > 0">
      AND service_id IN <foreach ...>#{code}</foreach>
    </if>
</select>
```

### 5.2 `buAnnualTarget` —— 单 BU 某年收入目标（卡 1）
```java
Map<String,Object> buAnnualTarget(@Param("buOrgId") Long buOrgId, @Param("year") String year);
```
```xml
<select id="buAnnualTarget" resultType="map">
    SELECT COALESCE(SUM(target_amount),0) AS "total"
    FROM szzw.t_yqyc_biz_annual_target
    WHERE bu_org_id = #{buOrgId} AND target_year = #{year}
      AND (service_id = 15 OR service_id IS NULL)
</select>
```
> Service 里：实际 ÷ 目标 × 100；目标为 0 时返回 0。

### 5.3 `buLatestAmount` —— 单 BU 某科目最新月值（卡 4）
```java
Map<String,Object> buLatestAmount(@Param("buOrgId") Long buOrgId, @Param("serviceId") Long serviceId);
```
```xml
<select id="buLatestAmount" resultType="map">
    SELECT amount AS "amount"
    FROM szzw.t_yqyc_biz_bu_performance
    WHERE bu_org_id = #{buOrgId} AND service_id = #{serviceId}
    ORDER BY stat_month DESC FETCH FIRST 1 ROWS ONLY
</select>
```

### 5.4 `buMonthlyMulti` —— 单 BU 多科目按月（组合图 2/4）
```java
List<Map<String,Object>> buMonthlyMulti(@Param("buOrgId") Long buOrgId,
                                        @Param("year") String year,
                                        @Param("svcCodes") List<String> svcCodes);
```
```xml
<select id="buMonthlyMulti" resultType="map">
    SELECT stat_month AS "month", service_id AS "serviceId", COALESCE(SUM(amount),0) AS "amount"
    FROM szzw.t_yqyc_biz_bu_performance
    WHERE bu_org_id = #{buOrgId} AND stat_month LIKE #{year} || '%'
      AND service_id IN <foreach ...>#{code}</foreach>
    GROUP BY stat_month, service_id
    ORDER BY stat_month
</select>
```
> Service/前端 按 `serviceId` 透视成 `{month, dedicatedLine(13/14), fusion(16)}` 等。
> 专线(13+14) 需在 Service 把 13、14 两行合并求和成 `dedicatedLine`。

---

## 6. Service 改造（`getBuView`）

```java
public Map<String,Object> getBuView(Long buOrgId) {
    String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
    Map<String,Object> result = new LinkedHashMap<>();
    result.put("buOrgId", buOrgId);

    // ---- 指标卡 ----
    Map<String,Object> cards = new LinkedHashMap<>();
    double revenue = toDouble(dashboardMapper.buAnnualSum(buOrgId, year, List.of("15")), "total");
    double target  = toDouble(dashboardMapper.buAnnualTarget(buOrgId, year), "total");
    cards.put("annualProgress", target > 0 ? Math.round(revenue*1000.0/target)/10.0 : 0); // 一位小数 %
    cards.put("dedicatedLineAnnual", toDouble(dashboardMapper.buAnnualSum(buOrgId, year, List.of("13","14")), "total"));
    cards.put("ictAnnualContract",   toDouble(dashboardMapper.buAnnualSum(buOrgId, year, List.of("9")), "total"));
    cards.put("broadbandPenetration", toDouble(dashboardMapper.buLatestAmount(buOrgId, 19L), "amount"));
    result.put("cards", cards);

    // ---- 图表 ----
    result.put("monthlyRevenue",              dashboardMapper.buMonthlyRevenue(buOrgId, year));
    result.put("dedicatedLineAndFusion",      pivotBuMulti(dashboardMapper.buMonthlyMulti(buOrgId, year, List.of("13","14","16")), "dedicatedLine", List.of("13","14"), "fusion", "16"));
    result.put("monthlyIctContract",          dashboardMapper.buMonthlyPerf(buOrgId, 9L, year));
    result.put("ictZcAndPolicy",              pivotBuMulti(dashboardMapper.buMonthlyMulti(buOrgId, year, List.of("17","18")), "govPurchase", "17", "policyShare", "18"));
    result.put("monthlyBroadbandPenetration", dashboardMapper.buMonthlyPerf(buOrgId, 19L, year)); // 校正 10→19
    result.put("monthlyBroadbandNew",         dashboardMapper.buMonthlyPerf(buOrgId, 22L, year)); // 校正 11→22
    return result;
}
```
- `pivotBuMulti(rows, nameA, svcA, nameB, svcB)`：把 `buMonthlyMulti` 的按月按科目行，透视成每月一行 `{month, [nameA], [nameB]}`；`nameA` 支持多科目求和（专线 13+14）。`toDouble` 复用现有 helper。

---

## 7. 前端改造（`ManagerDashboard.vue`）

- `buCards` 已读 `d.annualProgress / dedicatedLineAnnual / ictAnnualContract / broadbandPenetration`，Service 填实值后无需改。
- 单系列图（月收入、ICT、渗透率、家宽新增）已有 `buBarOpt / buLineOpt`，无需改。
- **组合图需新增 option 构造器**：
  - `dedicatedLineAndFusion`（柱+线）：`comboBarLine(rows, 'month', 'dedicatedLine', 'fusion', '专线收入', '融合销量', '万元')`。
  - `ictZcAndPolicy`（线+线）：`comboLineLine(rows, 'month', 'govPurchase', 'policyShare', '政采数量', '政策份额')`。
  - 改 `buZxBarLineOpt = comboBarLine(buData.dedicatedLineAndFusion, ...)`、`buIctZcLineOpt = comboLineLine(buData.ictZcAndPolicy, ...)`。

---

## 8. 数据前置条件

1. `t_yqyc_biz_bu_performance` 需有对应 BU + 科目 + 月份的数据；否则卡/图为 0/空。
2. `t_yqyc_biz_annual_target` 需录入 BU 的年度收入目标，否则 `annualProgress` 为 0。
3. **service_id 16/17/18（融合产品/政采/政策份额）需业务确认**；确认前这两个组合图可先返回空数组（前端 `buBarOpt/buLineOpt` 对空数据已返回 `{}`，不报错）。

---

## 9. 待确认问题（Open Questions）

1. `融合产品销量 / ICT政采数量 / ICT政策份额` 分别对应哪个 `service_id`？（本文档暂拟 16/17/18）
2. 卡片 `broadbandPenetration` 取"最新月"还是"全年平均"？本文档取**最新月**。
3. 卡片 `annualProgress` 无年度目标时，展示 0 还是只展示完成值？本文档展示 0。
4. "专线"口径是 13+14 合计，还是仅互联网专线(13)？本文档按 13+14 合计（与时间统计"两线"一致）。

---

## 10. 涉及文件

**后端**
- `mapper/DashboardMapper.java`（+`buAnnualSum`/`buAnnualTarget`/`buLatestAmount`/`buMonthlyMulti`）
- `mapper/DashboardMapper.xml`（对应 4 个 select）
- `service/impl/DashboardServiceImpl.java`（重写 `getBuView`、新增 `pivotBuMulti`）

**前端**
- `views/dashboard/ManagerDashboard.vue`（新增 `comboBarLine`/`comboLineLine`，改 `buZxBarLineOpt`/`buIctZcLineOpt`）

---

## 11. 不在本次范围
- BU 视图下的客户经理产能明细（属另一模块）。
- 年度目标的维护/导入界面（`t_yqyc_biz_annual_target` 录入）。
- service_id 字典页（16/17/18 等确认后可补到字典）。
