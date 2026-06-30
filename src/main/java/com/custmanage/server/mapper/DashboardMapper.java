package com.custmanage.server.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper {

    // ---- 按时间统计 ----

    /** 集团总数 */
    long countTotalGroups();

    /** 当月收入完成 */
    Map<String, Object> sumMonthRevenue(@Param("month") String month, @Param("serviceId") Long serviceId);

    /** 全年累计收入 */
    Map<String, Object> sumAnnualRevenue(@Param("year") String year, @Param("serviceId") Long serviceId);

    /** 分BU收入 */
    List<Map<String, Object>> revenueByBu(@Param("month") String month, @Param("serviceId") Long serviceId);

    /** 分业务收入 */
    List<Map<String, Object>> revenueByService(@Param("month") String month);

    /** 全年收入按月趋势（今年） */
    List<Map<String, Object>> revenueTrendByMonth(@Param("year") String year, @Param("serviceId") Long serviceId);

    /** BU业绩：按业务汇总（信息化业务/BC融合区块） */
    List<Map<String, Object>> buPerfByService(@Param("month") String month);

    /** BU业绩：按BU汇总 */
    List<Map<String, Object>> buPerfByBuService(@Param("month") String month, @Param("svcCodes") List<String> svcCodes);

    /** 按BU汇总指定业务科目的金额（多个科目相加，每BU一行），如两线规模=互联网专线(13)+数据专线(14) */
    List<Map<String, Object>> sumAmountByBu(@Param("month") String month, @Param("svcCodes") List<String> svcCodes);

    /** 按业务科目取各 BU 的平均值（每科目一行）：如要客宽带渗透率(19)/关联人流量主用占比(20) 取所有 BU 均值 */
    List<Map<String, Object>> avgAmountByService(@Param("month") String month, @Param("svcCodes") List<String> svcCodes);

    // ---- 按BU统计 ----

    /** 指定BU各月收入 */
    List<Map<String, Object>> buMonthlyRevenue(@Param("buOrgId") Long buOrgId, @Param("year") String year, @Param("serviceId") Long serviceId);

    /** 指定BU各月业绩 */
    List<Map<String, Object>> buMonthlyPerf(@Param("buOrgId") Long buOrgId, @Param("serviceId") Long serviceId, @Param("year") String year);

    /** 指定BU某年指定科目总和（如专线全年累计=13+14） */
    Map<String, Object> buAnnualSum(@Param("buOrgId") Long buOrgId, @Param("year") String year, @Param("svcCodes") List<String> svcCodes);

    /** 指定BU某年收入目标（优先 service_id=15，回退 service_id IS NULL） */
    Map<String, Object> buAnnualTarget(@Param("buOrgId") Long buOrgId, @Param("year") String year);

    /** 各 BU 某科目全年累计金额 + 年度目标 + 完成率(%)，用于"各 BU ICT 签约详情/全年完成率" */
    List<Map<String, Object>> buAnnualCompletion(@Param("year") String year, @Param("serviceId") Long serviceId);

    /** 指定BU某年某科目的月度平均值（要客宽带渗透率取全年均值） */
    Map<String, Object> buAnnualAvg(@Param("buOrgId") Long buOrgId, @Param("year") String year, @Param("serviceId") Long serviceId);

    /** 指定BU某年指定科目按月求和（如专线月收入=13+14），返回 [{month, amount}] */
    List<Map<String, Object>> buMonthlySum(@Param("buOrgId") Long buOrgId, @Param("year") String year, @Param("svcCodes") List<String> svcCodes);

    // ---- 通用 ----

    /** BU列表 */
    List<Map<String, Object>> selectBuOptions();

    /** 要客集团数 */
    long countKeyCustomerGroups();

    /** 统付集团数 */
    long countUnifiedPaymentGroups();

    /** 全部 BU 某年收入目标总和（service_id=15），用于计算全年收入完成进度 */
    Map<String, Object> totalAnnualTarget(@Param("year") String year);
}
