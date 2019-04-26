package com.lehuan.shop.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: lehuan-parent
 * @description: 登录相关的控制器，显示用户名
 * @author: baichen
 **/
@RestController
@RequestMapping("/login")
public class LoginController {
    @RequestMapping("/name")
    public Map name() {
//        获取当前已登录的用户名称
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map = new HashMap();
        map.put("loginName", name);
        return map;
    }
}
