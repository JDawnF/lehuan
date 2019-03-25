package com.lehuan.page.service;

/**
 * 商品详细页接口
 * @author Administrator
 *
 */
public interface ItemPageService {
    /**
     * 生成商品详细页
     * @param goodsId   商品id
     * @return  是否生成成功
     */
    public boolean genItemHtml(Long goodsId);

    /**
     * 删除商品详情页
     * @param goodsIds  商品id集合
     * @return  是否生成成功
     */
    public boolean deleteItemHtml(Long[] goodsIds);
}

