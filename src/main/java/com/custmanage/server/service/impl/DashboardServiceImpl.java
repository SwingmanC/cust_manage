package com.custmanage.server.service.impl;
import com.custmanage.server.common.MapUtil;

import com.custmanage.server.common.BizService;
import com.custmanage.server.mapper.DashboardMapper;
import com.custmanage.server.service.IDashboardService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DashboardServiceImpl implements IDashboardService {

    private final DashboardMapper dashboardMapper;

    public DashboardServiceImpl(DashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }

    @Override
    public Map<String, Object> getTimeView(String month) {
        String year = month.substring(0, 4);
        Map<String, Object> result = new LinkedHashMap<>();

        // （一）指标总览
        Map<String, Object> overview = new LinkedHashMap<>();
        Map<String, Object> ovCards = new LinkedHashMap<>();
        ovCards.put("totalGroups", dashboardMapper.countTotalGroups());
        ovCards.put("dedicatedLineAdvantageGroups", 0);  // TODO: 专线优势集团数逻辑
        ovCards.put("memberAdvantageGroups", 0);           // TODO: 成员优势集团数逻辑
        Map<String, Object> monthRev = dashboardMapper.sumMonthRevenue(month, BizService.REVENUE.id());
        ovCards.put("monthRevenue", toDouble(monthRev, "total"));
        Map<String, Object> annualRev = dashboardMapper.sumAnnualRevenue(year, BizService.REVENUE.id());
        double annualTarget = toDouble(dashboardMapper.totalAnnualTarget(year), "total");
        double annualRevenue = toDouble(annualRev, "total");
        ovCards.put("annualProgress", annualTarget > 0 ? Math.round(annualRevenue * 1000.0 / annualTarget) / 10.0 : 0);
        overview.put("cards", ovCards);
        overview.put("revenueByBu", dashboardMapper.revenueByBu(month, BizService.REVENUE.id()));
        overview.put("revenueByService", dashboardMapper.revenueByService(month));
        // 收入趋势（最近12个月）
        List<Map<String, Object>> trend = dashboardMapper.revenueTrendByMonth(year, BizService.REVENUE.id());
        List<Double> thisYear = buildMonthlyArray(trend, year);
        overview.put("annualTrend", MapUtil.of("lastYear", Collections.emptyList(), "thisYear", thisYear));
        result.put("overview", overview);

        // （二）信息化业务
        Map<String, Object> itBiz = new LinkedHashMap<>();
        Map<String, Object> itCards = new LinkedHashMap<>();
        // 当月两线新增: service_code = SVC_ZHUANXIAN / SVC_YYZX
        List<Map<String, Object>> buPerf = dashboardMapper.buPerfByService(month);
        // 当月两线新增 = 互联网专线 + 数据专线
        String internetLine = BizService.INTERNET_LINE.code();
        String dataLine = BizService.DATA_LINE.code();
        double twoLineNew = buPerf.stream().filter(m -> {
            Object sid = m.get("serviceId");
            return sid != null && (sid.toString().equals(internetLine) || sid.toString().equals(dataLine));
        }).mapToDouble(m -> toDouble(m, "amount")).sum();
        itCards.put("twoLineNew", Math.round(twoLineNew * 100.0) / 100.0);
        // ICT签约
        String ictCode = BizService.ICT_CONTRACT.code();
        double ictAmt = buPerf.stream().filter(m -> ictCode.equals(String.valueOf(m.get("serviceId"))))
                .mapToDouble(m -> toDouble(m, "amount")).sum();
        itCards.put("ictAnnualContract", ictAmt);
        itBiz.put("cards", itCards);
        // 两线规模按BU：互联网专线 + 数据专线 金额相加（每BU一行）
        itBiz.put("twoLineByBu", dashboardMapper.sumAmountByBu(month,
                Arrays.asList(BizService.INTERNET_LINE.code(), BizService.DATA_LINE.code())));
        // ICT按BU：全年累计签约额(柱) + 全年完成率(折线) = 累计签约额 / 年度签约目标
        itBiz.put("ictByBu", dashboardMapper.buAnnualCompletion(year, BizService.ICT_CONTRACT.id()));
        result.put("itBusiness", itBiz);

        // （三）BC融合
        Map<String, Object> bcFusion = new LinkedHashMap<>();
        Map<String, Object> bcCards = new LinkedHashMap<>();
        bcCards.put("keyCustomerGroups", dashboardMapper.countKeyCustomerGroups());
        long totalGroups = dashboardMapper.countTotalGroups();
        long tfGroups = dashboardMapper.countUnifiedPaymentGroups();
        bcCards.put("unifiedPaymentRatio", totalGroups > 0 ? Math.round(tfGroups * 1000.0 / totalGroups) / 10.0 : 0);
        // 要客宽带渗透率、关联人流量主用占比 = 各 BU 的平均值（取所有 BU 均值）
        List<Map<String, Object>> bcAvg = dashboardMapper.avgAmountByService(month,
                Arrays.asList(BizService.BROADBAND_PENETRATION.code(), BizService.RELATED_USER_RATIO.code()));
        String broadbandCode = BizService.BROADBAND_PENETRATION.code();
        double kdPen = bcAvg.stream().filter(m -> broadbandCode.equals(String.valueOf(m.get("serviceId"))))
                .mapToDouble(m -> toDouble(m, "avgAmount")).findFirst().orElse(0);
        bcCards.put("broadbandPenetration", kdPen);
        String relatedCode = BizService.RELATED_USER_RATIO.code();
        double relatedRatio = bcAvg.stream().filter(m -> relatedCode.equals(String.valueOf(m.get("serviceId"))))
                .mapToDouble(m -> toDouble(m, "avgAmount")).findFirst().orElse(0);
        bcCards.put("relatedUserMainRatio", relatedRatio);
        bcFusion.put("cards", bcCards);
        bcFusion.put("broadbandByBu", dashboardMapper.buPerfByBuService(month, Arrays.asList(BizService.BROADBAND_PENETRATION.code())));
        bcFusion.put("relatedUserByBu", dashboardMapper.buPerfByBuService(month, Arrays.asList(BizService.RELATED_USER_RATIO.code())));
        bcFusion.put("unifiedPaymentByBu", dashboardMapper.buPerfByBuService(month,
                Arrays.asList(BizService.UNIFIED_PAYMENT_MEMBER.code(), BizService.UNIFIED_PAYMENT_BROADBAND_NEW.code())));
        result.put("bcFusion", bcFusion);

        // （四）客户经理产能
        result.put("managerRanking", getManagerRanking(month));

        return result;
    }

    @Override
    public Map<String, Object> getBuView(Long buOrgId) {
        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("buOrgId", buOrgId);

        // 指标卡
        Map<String, Object> cards = new LinkedHashMap<>();
        double revenue = toDouble(dashboardMapper.buAnnualSum(buOrgId, year, Arrays.asList(BizService.REVENUE.code())), "total");
        double target = toDouble(dashboardMapper.buAnnualTarget(buOrgId, year), "total");
        cards.put("annualProgress", target > 0 ? Math.round(revenue * 1000.0 / target) / 10.0 : 0);
        cards.put("dedicatedLineAnnual", toDouble(dashboardMapper.buAnnualSum(buOrgId, year, Arrays.asList(BizService.DEDICATED_LINE.code())), "total"));
        cards.put("ictAnnualContract", toDouble(dashboardMapper.buAnnualSum(buOrgId, year, Arrays.asList(BizService.ICT_CONTRACT.code())), "total"));
        cards.put("broadbandPenetration", toDouble(dashboardMapper.buAnnualAvg(buOrgId, year, BizService.BROADBAND_PENETRATION.id()), "avg"));
        result.put("cards", cards);

        // 图表
        result.put("monthlyRevenue", dashboardMapper.buMonthlyRevenue(buOrgId, year, BizService.REVENUE.id()));
        // 专线月收入（融合产品销量暂未实现，先只展示专线）
        result.put("dedicatedLineAndFusion", dashboardMapper.buMonthlySum(buOrgId, year, Arrays.asList(BizService.DEDICATED_LINE.code())));
        result.put("monthlyIctContract", dashboardMapper.buMonthlyPerf(buOrgId, BizService.ICT_CONTRACT.id(), year));
        // ICT 政采数量、政策份额暂未实现
        result.put("ictZcAndPolicy", Collections.emptyList());
        result.put("monthlyBroadbandPenetration", dashboardMapper.buMonthlyPerf(buOrgId, BizService.BROADBAND_PENETRATION.id(), year));
        result.put("monthlyBroadbandNew", dashboardMapper.buMonthlyPerf(buOrgId, BizService.UNIFIED_PAYMENT_BROADBAND_NEW.id(), year));

        return result;
    }

    @Override
    public List<Map<String, Object>> getBuOptions() {
        return dashboardMapper.selectBuOptions();
    }

    @Override
    public Map<String, Object> getManagerRanking(String month) {
        Map<String, Object> ranking = new LinkedHashMap<>();
        ranking.put("comprehensive", Collections.emptyList()); // TODO: 产能表有数据后改为真实查询
        ranking.put("twoLine", Collections.emptyList());
        ranking.put("broadband", Collections.emptyList());
        return ranking;
    }

    // ---- helpers ----

    private double toDouble(Map<String, Object> map, String key) {
        Object val = map != null ? map.get(key) : null;
        if (val instanceof Number) {
            Number n = (Number) val;
            return n.doubleValue();
        }
        if (val instanceof String) {
            String s = (String) val;
            return Double.parseDouble(s);
        }
        return 0;
    }

    private List<Double> buildMonthlyArray(List<Map<String, Object>> rows, String year) {
        double[] arr = new double[12];
        for (Map<String, Object> row : rows) {
            String m = (String) row.get("month");
            if (m != null && m.startsWith(year)) {
                int idx = Integer.parseInt(m.substring(4, 6)) - 1;
                if (idx >= 0 && idx < 12) arr[idx] = toDouble(row, "amount");
            }
        }
        List<Double> list = new ArrayList<>();
        for (double d : arr) list.add(d);
        return list;
    }
}
