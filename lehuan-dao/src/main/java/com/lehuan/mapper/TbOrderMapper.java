package com.lehuan.mapper;

import com.lehuan.pojo.TbOrder;
import com.lehuan.pojo.TbOrderExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TbOrderMapper {
    int countByExample(TbOrderExample example);

    int deleteByExample(TbOrderExample example);

    int deleteByPrimaryKey(Long orderId);

    int insert(TbOrder record);

    int insertSelective(TbOrder record);

    List<TbOrder> selectByExample(TbOrderExample example);

    TbOrder selectByPrimaryKey(Long orderId);

    int updateByExampleSelective(@Param("record") TbOrder record, @Param("example") TbOrderExample example);

    int updateByExample(@Param("record") TbOrder record, @Param("example") TbOrderExample example);

    int updateByPrimaryKeySelective(TbOrder record);

    int updateByPrimaryKey(TbOrder record);
}