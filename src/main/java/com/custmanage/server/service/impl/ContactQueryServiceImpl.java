package com.custmanage.server.service.impl;

import com.custmanage.server.mapper.ContactMapper;
import com.custmanage.server.service.IContactQueryService;
import com.custmanage.server.vo.ContactVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactQueryServiceImpl implements IContactQueryService {
    private final ContactMapper contactMapper;

    public ContactQueryServiceImpl(ContactMapper contactMapper) {
        this.contactMapper = contactMapper;
    }

    @Override
    public List<ContactVO> queryContacts(Long groupId, String status) {
        return contactMapper.selectContacts(
                groupId != null ? List.of(groupId) : null,
                status, null, null);
    }
}
