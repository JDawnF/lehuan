package com.lehuan.mapper;

import com.lehuan.pojo.TbGoodsDesc;
import com.lehuan.pojo.TbGoodsDescExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TbGoodsDescMapper {
    int countByExample(TbGoodsDescExample example);

    int deleteByExample(TbGoodsDescExample example);

    int deleteByPrimaryKey(Long goodsId);

    int insert(TbGoodsDesc record);

    int insertSelective(TbGoodsDesc record);

    List<TbGoodsDesc> selectByExample(TbGoodsDescExample example);

    TbGoodsDesc selectByPrimaryKey(Long goodsId);

    int updateByExampleSelective(@Param("record") TbGoodsDesc record, @Param("example") TbGoodsDescExample example);

    int updateByExample(@Param("record") TbGoodsDesc record, @Param("example") TbGoodsDescExample example);

    int updateByPrimaryKeySelective(TbGoodsDesc record);

    int updateByPrimaryKey(TbGoodsDesc record);
}