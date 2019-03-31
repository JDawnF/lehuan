package com.lehuan.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.lehuan.cart.service.CartService;
import com.lehuan.group.Cart;
import common.CookieUtil;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @program: lehuan-parent
 * @description: 购物车控制类
 * @author: baichen
 * @create: 2018-12-11 23:16
 **/
@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference(timeout = 6000)
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    /**
     * 购物车列表，通过cookie获取
     * @return 购物车列表
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        //得到登陆人账号,判断当前是否有人登陆
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人" + username);
        //读取本地购物车,即从cookie中提取购物车列表,String类型的购物车列表
        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
//        本地购物车列表为空
        if (cartListString == null || cartListString.equals("")) {
            cartListString = "[]";  // 用空的中括号，防止下面转换报错
        }
//      转换为Json数组/列表
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        if ("anonymousUser".equals(username)) {  //如果未登录,anonymousUser表示匿名认证对象
//            读取本地购物车,即从cookie中提取购物车列表
            return cartList_cookie;
        } else {     //如果已登录
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);  //从redis中提取
            //如果本地存在购物车
            if (cartList_cookie.size() > 0) {
                //合并购物车
                cartList_redis = cartService.mergeCartList(cartList_redis, cartList_cookie);
                //清除本地cookie的数据
                CookieUtil.deleteCookie(request, response, "cartList");
                //将合并后的数据存入redis
                cartService.saveCartListToRedis(username, cartList_redis);
            }
            return cartList_redis;
        }
    }

    /**
     * 添加商品到购物车
     * @param itemId      商品ID
     * @param num         商品数量
     * @return            添加结果
     */
    @RequestMapping("/addGoodsToCartList")
    // 实现跨域请求
    @CrossOrigin(origins = "http://localhost:9105", allowCredentials = "true")
    public Result addGoodsToCartList(Long itemId, Integer num) {
        //跨域请求，springMVC4.2后跨域用上面的注解
        //可以访问的域(此方法不允许操作cookie，如果需要操作cookie，需要加下面的那句话)
//        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        //允许操作cookie，如果操作cookie，上面那句话的ip地址不能用*
//        response.setHeader("Access-Control-Allow-Credentials", "true");
        //得到登陆人账号,判断当前是否有人登陆
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人" + username);
        try {
            //获取购物车列表
            List<Cart> cartList = findCartList();
            // 添加到购物车列表
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if ("anonymousUser".equals(username)) { // 未登录，存入cookie
                //将新的购物车存入cookie
                String cartListString = JSON.toJSONString(cartList);
                CookieUtil.setCookie(request, response, "cartList", cartListString, 3600 * 24, "UTF-8");
                System.out.println("向cookie存入数据");
            } else {    // 已登录，存入redis
                cartService.saveCartListToRedis(username, cartList);
                System.out.println("向Redis中存入数据");
            }
            return new Result(true, "存入购物车成功");
        } catch (RuntimeException e) {
            return new Result(false, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "存入购物车失败");
        }
    }
}
