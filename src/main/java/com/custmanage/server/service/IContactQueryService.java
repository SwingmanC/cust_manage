package com.custmanage.server.service;

import com.custmanage.server.vo.ContactVO;

import java.util.List;

public interface IContactQueryService {
    List<ContactVO> queryContacts(Long groupId, String status);
}
