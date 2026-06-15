package com.custmanage.server.service;

import java.util.List;
import java.util.Map;

public interface IDashboardService {

    /** 按时间统计 */
    Map<String, Object> getTimeView(String month);

    /** 按BU统计 */
    Map<String, Object> getBuView(Long buOrgId);

    /** BU下拉列表 */
    List<Map<String, Object>> getBuOptions();

    /** 客户经理产能排名 */
    Map<String, Object> getManagerRanking(String month);
}
