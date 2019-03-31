package com.lehuan.cart.service;

import com.lehuan.group.Cart;
import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService {
    /**
     * 添加商品到购物车
     * @param cartList      购物车列表
     * @param itemId        商品ID
     * @param num           商品数量
     * @return              购物车列表
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num );
    /**
     * 从redis中查询购物车
     * @param username  用户名
     * @return          购物车列表
     */
    public List<Cart> findCartListFromRedis(String username);

    /**
     * 将购物车保存到redis
     * @param username      用户名
     * @param cartList      购物车列表
     */
    public void saveCartListToRedis(String username,List<Cart> cartList);
    /**
     * 合并购物车
     * @param cartList1     购物车列表
     * @param cartList2     购物车列表
     * @return              合并后的购物车列表
     */
    public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);

}
