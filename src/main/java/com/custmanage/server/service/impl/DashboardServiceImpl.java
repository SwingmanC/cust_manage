package com.custmanage.server.service.impl;

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
        Map<String, Object> monthRev = dashboardMapper.sumMonthRevenue(month);
        ovCards.put("monthRevenue", toDouble(monthRev, "total"));
        Map<String, Object> annualRev = dashboardMapper.sumAnnualRevenue(year);
        ovCards.put("annualProgress", 0); // TODO: 需年度目标表数据
        overview.put("cards", ovCards);
        overview.put("revenueByBu", dashboardMapper.revenueByBu(month));
        overview.put("revenueByService", dashboardMapper.revenueByService(month));
        // 收入趋势（最近12个月）
        List<Map<String, Object>> trend = dashboardMapper.revenueTrendByMonth(year);
        List<Double> thisYear = buildMonthlyArray(trend, year);
        overview.put("annualTrend", Map.of("lastYear", Collections.emptyList(), "thisYear", thisYear));
        result.put("overview", overview);

        // （二）信息化业务
        Map<String, Object> itBiz = new LinkedHashMap<>();
        Map<String, Object> itCards = new LinkedHashMap<>();
        // 当月两线新增: service_code = SVC_ZHUANXIAN / SVC_YYZX
        List<Map<String, Object>> buPerf = dashboardMapper.buPerfByService(month);
        double twoLineNew = buPerf.stream().filter(m -> {
            Object sid = m.get("serviceId");
            return sid != null && (sid.toString().equals("1") || sid.toString().equals("12"));
        }).mapToDouble(m -> toDouble(m, "amount")).sum();
        itCards.put("twoLineNew", twoLineNew);
        // ICT签约: service_id = 9
        double ictAmt = buPerf.stream().filter(m -> "9".equals(String.valueOf(m.get("serviceId"))))
                .mapToDouble(m -> toDouble(m, "amount")).sum();
        itCards.put("ictAnnualContract", ictAmt);
        itBiz.put("cards", itCards);
        // 两线规模按BU
        itBiz.put("twoLineByBu", dashboardMapper.buPerfByBuService(month, List.of("1","12")));
        // ICT按BU
        itBiz.put("ictByBu", dashboardMapper.buPerfByBuService(month, List.of("9")));
        result.put("itBusiness", itBiz);

        // （三）BC融合
        Map<String, Object> bcFusion = new LinkedHashMap<>();
        Map<String, Object> bcCards = new LinkedHashMap<>();
        bcCards.put("keyCustomerGroups", dashboardMapper.countKeyCustomerGroups());
        long totalGroups = dashboardMapper.countTotalGroups();
        long tfGroups = dashboardMapper.countUnifiedPaymentGroups();
        bcCards.put("unifiedPaymentRatio", totalGroups > 0 ? Math.round(tfGroups * 1000.0 / totalGroups) / 10.0 : 0);
        // 宽带渗透率、关联人占比从 bu_performance 取
        double kdPen = buPerf.stream().filter(m -> "10".equals(String.valueOf(m.get("serviceId"))))
                .mapToDouble(m -> toDouble(m, "amount")).sum();
        bcCards.put("broadbandPenetration", kdPen);
        double relatedRatio = buPerf.stream().filter(m -> "11".equals(String.valueOf(m.get("serviceId"))))
                .mapToDouble(m -> toDouble(m, "amount")).sum();
        bcCards.put("relatedUserMainRatio", relatedRatio);
        bcFusion.put("cards", bcCards);
        bcFusion.put("broadbandByBu", dashboardMapper.buPerfByBuService(month, List.of("10")));
        bcFusion.put("relatedUserByBu", dashboardMapper.buPerfByBuService(month, List.of("11")));
        bcFusion.put("unifiedPaymentByBu", dashboardMapper.buPerfByBuService(month, List.of("10","11")));
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
        Map<String, Object> annualRev = dashboardMapper.sumAnnualRevenue(year);
        cards.put("annualProgress", 0); // TODO
        cards.put("dedicatedLineAnnual", 0); // TODO
        cards.put("ictAnnualContract", 0); // TODO
        cards.put("broadbandPenetration", 0); // TODO
        result.put("cards", cards);

        result.put("monthlyRevenue", dashboardMapper.buMonthlyRevenue(buOrgId, year));
        result.put("dedicatedLineAndFusion", Collections.emptyList()); // TODO
        result.put("monthlyIctContract", dashboardMapper.buMonthlyPerf(buOrgId, 9L, year));
        result.put("ictZcAndPolicy", Collections.emptyList()); // TODO
        result.put("monthlyBroadbandPenetration", dashboardMapper.buMonthlyPerf(buOrgId, 10L, year));
        result.put("monthlyBroadbandNew", dashboardMapper.buMonthlyPerf(buOrgId, 11L, year));

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
        if (val instanceof Number n) return n.doubleValue();
        if (val instanceof String s) return Double.parseDouble(s);
        return 0;
    }

    private List<Double> buildMonthlyArray(List<Map<String, Object>> rows, String year) {
        double[] arr = new double[12];
        for (Map<String, Object> row : rows) {
            String m = (String) row.get("month");
            if (m != null && m.startsWith(year)) {
                int idx = Integer.parseInt(m.substring(5, 7)) - 1;
                if (idx >= 0 && idx < 12) arr[idx] = toDouble(row, "amount");
            }
        }
        List<Double> list = new ArrayList<>();
        for (double d : arr) list.add(d);
        return list;
    }
}
