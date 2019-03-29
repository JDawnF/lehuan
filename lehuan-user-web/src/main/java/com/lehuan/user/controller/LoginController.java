package com.lehuan.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: lehuan-parent
 * @description:
 * @author: baichen
 * @create: 2018-12-09 22:41
 **/
@RestController
@RequestMapping("/login")
public class LoginController {
    @RequestMapping("/name")
    public Map showName() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map = new HashMap<>();
        map.put("loginName", name);
        return map;
    }
}
