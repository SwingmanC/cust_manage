package com.custmanage.server.common;

/**
 * 业务科目（{@code t_yqyc_biz_service.id}）枚举。
 *
 * <p>集中管理看板等模块用到的 service_id，避免代码/SQL 中散落魔法数字。
 * 新增科目时在此登记即可，调用方一律引用本枚举常量。</p>
 *
 * <ul>
 *   <li>{@link #id()} —— service_id 数值（用于单科目 Long 参数）</li>
 *   <li>{@link #code()} —— service_id 字符串（用于 {@code List<String> svcCodes} 参数）</li>
 * </ul>
 */
public enum BizService {

    DEDICATED_LINE(1L, "专线"),
    ICT_CONTRACT(9L, "ICT签约"),
    INTERNET_LINE(13L, "互联网专线"),
    DATA_LINE(14L, "数据专线"),
    REVENUE(15L, "总收入"),
    BROADBAND_PENETRATION(19L, "要客宽带渗透率"),
    RELATED_USER_RATIO(20L, "关联人流量主用占比"),
    UNIFIED_PAYMENT_MEMBER(21L, "统付成员数"),
    UNIFIED_PAYMENT_BROADBAND_NEW(22L, "统付家宽开通");

    private final long id;
    private final String label;

    BizService(long id, String label) {
        this.id = id;
        this.label = label;
    }

    /** service_id 数值 */
    public long id() {
        return id;
    }

    /** service_id 字符串（用于 svcCodes 等字符串集合参数） */
    public String code() {
        return String.valueOf(id);
    }

    /** 业务科目名称 */
    public String label() {
        return label;
    }
}
