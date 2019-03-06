package com.lehuan.group;

import com.lehuan.pojo.TbGoods;
import com.lehuan.pojo.TbGoodsDesc;
import com.lehuan.pojo.TbItem;

import java.io.Serializable;
import java.util.List;

/**
 * @program: lehuan-parent
 * @description: 商品组合实体类，商品表和商品规格表，相当于DTO
 * @author: baichen
 * @create: 2018-10-17 20:23
 **/
public class Goods implements Serializable {
    private TbGoods goods;  //商品SPU基本信息
    private TbGoodsDesc goodsDesc;      //商品SKU扩展信息
    private List<TbItem> itemList;      //SKU列表

    public TbGoods getGoods() {
        return goods;
    }

    public void setGoods(TbGoods goods) {
        this.goods = goods;
    }

    public TbGoodsDesc getGoodsDesc() {
        return goodsDesc;
    }

    public void setGoodsDesc(TbGoodsDesc goodsDesc) {
        this.goodsDesc = goodsDesc;
    }

    public List<TbItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<TbItem> itemList) {
        this.itemList = itemList;
    }
}
