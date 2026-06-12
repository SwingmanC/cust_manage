package com.custmanage.server.mapper;

import com.custmanage.server.vo.BizServiceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BizServiceMapper {

    List<BizServiceVO> selectAll(@Param("status") String status);
}
