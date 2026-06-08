package com.custmanage.server.controller;

import com.custmanage.server.common.ApiResponse;
import com.custmanage.server.common.PageResponse;
import com.custmanage.server.service.IContactService;
import com.custmanage.server.vo.ContactVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final IContactService contactService;

    public ContactController(IContactService contactService) {
        this.contactService = contactService;
    }

    /** 通讯录列表（支持生效中/流程中Tab切换） */
    @GetMapping
    public ApiResponse<PageResponse<ContactVO>> list(
            @RequestParam(required = false) Long groupId,
            @RequestParam(defaultValue = "active") String tab,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(contactService.queryContacts(groupId, tab, keyword, pageNum, pageSize));
    }

    /** 通讯录详情 */
    @GetMapping("/{id}")
    public ApiResponse<ContactVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(contactService.getContact(id));
    }

    /** 新增通讯录 */
    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> params) {
        return ApiResponse.ok(contactService.createContact(params));
    }

    /** 编辑通讯录 */
    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id,
                                                    @RequestBody Map<String, Object> params) {
        return ApiResponse.ok(contactService.updateContact(id, params));
    }

    /** 删除通讯录 */
    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, Object>> delete(@PathVariable Long id) {
        return ApiResponse.ok(contactService.deleteContact(id));
    }

    /** 历史变更记录 */
    @GetMapping("/{id}/history")
    public ApiResponse<List<Map<String, Object>>> history(@PathVariable Long id) {
        return ApiResponse.ok(contactService.getHistory(id));
    }

    // ---------- 兼容旧的集团内通讯录查询 ----------

    @GetMapping("/by-group/{groupId}")
    public ApiResponse<PageResponse<ContactVO>> listByGroup(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "active") String tab,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(contactService.queryContacts(groupId, tab, null, pageNum, pageSize));
    }
}
