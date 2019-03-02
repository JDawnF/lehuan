package com.lehuan.shop.service;

import com.lehuan.pojo.TbSeller;
import com.lehuan.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: lehuan-parent
 * @description: 用户详情
 * @author: baichen
 * @create: 2018-10-04 09:59
 **/
public class UserDetailsServiceImpl implements UserDetailsService {
    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        System.out.println("进入了setSellerService方法");
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        构建角色列表
        List<GrantedAuthority> grantAuths = new ArrayList();
        grantAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));
//        得到上架对象
        TbSeller seller = sellerService.findOne(username);
//        商家不存在或者商家未通过审核(状态不为1)都返回null
        if (seller != null) {
            if (seller.getStatus().equals("1")) {
                return new User(username, seller.getPassword(), grantAuths);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}