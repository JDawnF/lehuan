package com.lehuan.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.lehuan.cart.service.CartService;
import com.lehuan.group.Cart;
import com.lehuan.mapper.TbItemMapper;
import com.lehuan.pojo.TbItem;
import com.lehuan.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: lehuan-parent
 * @description: 购物车服务实现类
 * @author: baichen
 **/
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * @param cartList 购物车列表
     * @param itemId   商品ID
     * @param num      商品数量
     * @return 购物车列表
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据商品SKU ID查询SKU商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!("1").equals(item.getStatus())) {
            throw new RuntimeException("商品无效");
        }
        //2.获取商家ID
        String sellerId = item.getSellerId();
        //3.根据商家ID查询购物车对象,查找属于当前商家的购物车
        // 因为每个商家的商品都是购物车列表中的一个列表
        Cart cart = this.searchCartBySellerId(cartList, sellerId);
        //4.如果购物车列表中不存在该商家的购物车
        if (cart == null) {
            //4.1 新建购物车对象
            cart = new Cart();
            // 购物车对象存入商家ID和用户名
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            // 创建订单商品明细列表
            List<TbOrderItem> orderItemList = new ArrayList<>();
            //创建订单商品
            TbOrderItem orderItem = this.createOrderItem(item, num);
            orderItemList.add(orderItem);   // 加入订单列表
            cart.setOrderItemList(orderItemList);   // 添加到购物车，多种列表嵌合
            //4.2 将新建的购物车对象添加到购物车列表
            cartList.add(cart);
        } else {
            //5.如果购物车列表中存在该商家的购物车
            // 查询商品订单明细列表中是否存在该商品
            TbOrderItem orderItem = this.searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if (orderItem == null) {
                //5.1. 如果商品订单不存在，创建新商品订单
                orderItem = createOrderItem(item, num);
//                存入购物车中的商品订单列表
                cart.getOrderItemList().add(orderItem);
            } else {
                //5.2. 如果商品订单存在，在商品订单明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum() + num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * orderItem.getNum()));
                //如果数量操作后小于等于0，则移除,添加数量可能是负的，数量+-
                if (orderItem.getNum() <= 0) {
                    cart.getOrderItemList().remove(orderItem);
                }
                //如果移除后cart的明细数量为0，则将cart移除
                if (cart.getOrderItemList().size() == 0) {
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    /**
     * @param username 用户名
     * @return 购物车列表
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中提取购物车数据....." + username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList == null) {
            cartList = new ArrayList(); //重新new一个，保证购物车列表不为空
        }
        return cartList;
    }

    /**
     * @param username 用户名
     * @param cartList 购物车列表
     */
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向redis存入购物车数据....." + username);
        redisTemplate.boundHashOps("cartList").put(username, cartList);
    }

    /**
     * @param cartList1 购物车列表
     * @param cartList2 购物车列表
     * @return 合并后的购物车列表
     */
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        System.out.println("合并购物车");
        for (Cart cart : cartList2) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                // addGoodsToCartList会判断商品是否已存在购物车，所以这里直接调用即可
                cartList1 = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList1;
    }

    /**
     * 根据商家ID查询购物车对象,查找属于当前商家的购物车
     * @param cartList 购物车列表
     * @param sellerId 商家ID
     * @return 返回购物车
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
//  判断是否属于当前商家，是的话就返回购物车当前这条记录，否则返回null
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }

    /**
     * 创建订单明细商品
     *
     * @param item 添加的商品
     * @param num  数量
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        if (num <= 0) {
            throw new RuntimeException("数量非法");
        }
//        创建订单明细商品，存入相应的信息
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());    // 物品ID，即SPUID
        orderItem.setItemId(item.getId());          // 商品ID
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());      // 省略图路径
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());  // 商家ID
        orderItem.setTitle(item.getTitle());
//        商品总价,参数是BigDecimal类型的，先转换为double类型的，再转为BigDecimal
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
        return orderItem;
    }

    /**
     * 根据商品明细ID在订单商品明细列表中查询购物车明细对象
     *
     * @param orderItemList 订单商品列表
     * @param itemId        商品ID
     * @return 订单
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            // Long类型不能用==，要转成long比较值
            if (orderItem.getItemId().longValue() == itemId.longValue()) {
                return orderItem;
            }
        }
        return null;
    }
}
