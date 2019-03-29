package com.lehuan.user.controller;

import java.util.List;

import com.lehuan.pojo.TbUser;
import com.lehuan.user.service.UserService;
import common.PhoneFormatCheckUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;

import entity.PageResult;
import entity.Result;

/**
 * 用户注册controller
 *
 * @author baichen
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Reference
    private UserService userService;

    /**
     * 返回全部用户列表
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbUser> findAll() {
        return userService.findAll();
    }

    /**
     * 分页返回全部用户列表
     *
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int rows) {
        return userService.findPage(page, rows);
    }

    /**
     * 新增用户
     *
     * @param user    用户
     * @param smsCode 验证码
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody TbUser user, String smsCode) {
        //校验验证码是否正确，返回布尔值
        boolean checkSmsCode = userService.checkSmsCode(user.getPhone(), smsCode);
        if (!checkSmsCode) {
            return new Result(false, "验证码不正确！");
        }
        try {
            userService.add(user);
            return new Result(true, "用户注册成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "用户注册失败");
        }
    }

    /**
     * 修改用户信息
     *
     * @param user 用户
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody TbUser user) {
        try {
            userService.update(user);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 通过用户id获取用户信息
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public TbUser findOne(Long id) {
        return userService.findOne(id);
    }

    /**
     * 通过用户id批量删除用户
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            userService.delete(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * 查询+分页
     *
     * @param user 用户
     * @param page 页数
     * @param rows 页面记录数
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbUser user, int page, int rows) {
        return userService.findPage(user, page, rows);
    }

    /**
     * 发送短信验证码
     *
     * @param phone 用户手机
     * @return
     */
    @RequestMapping("/sendCode")
    public Result sendCode(String phone) {
//        判断用户手机格式
        if (!PhoneFormatCheckUtils.isPhoneLegal(phone)) {
            return new Result(false, "手机号码格式不正确");
        }
        try {
            userService.createSmsCode(phone);   // 生成验证码
            return new Result(true, "验证码发送成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "验证码发送失败");
        }
    }
}
