package com.custmanage.server.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface BizServiceMapper {

    List<Map<String, Object>> selectAll(@Param("status") String status);
}
