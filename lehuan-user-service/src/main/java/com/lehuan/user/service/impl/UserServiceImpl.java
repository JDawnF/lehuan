package com.lehuan.user.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.*;

import com.alibaba.fastjson.JSON;
import com.lehuan.mapper.TbUserMapper;
import com.lehuan.pojo.TbUser;
import com.lehuan.pojo.TbUserExample;
import com.lehuan.user.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import entity.PageResult;
import org.springframework.jms.core.MessageCreator;

/**
 * 服务实现层
 *
 * @author baichen
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private TbUserMapper userMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Destination smsDestination;
    //短信平台uid，非必填
    @Value("${uid}")
    private String uid;
    // 短信平台模板id
    @Value("${templateId}")
    private String templateId;

    /**
     * 查询全部
     */
    @Override
    public List<TbUser> findAll() {
        return userMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbUser> page = (Page<TbUser>) userMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbUser user) {
        //要用当前时间，所以用new
        user.setCreated(new Date());    //用户注册时间
        user.setUpdated(new Date());    //修改时间
        //注册来源,有hc、安卓、ios、微信等多种来源,可以从前端传或者用不同的方法实现不同的来源
        user.setSourceType("1");
        user.setPassword(DigestUtils.md5Hex(user.getPassword()));//密码加密
        userMapper.insert(user);
    }

    /**
     * 修改
     */
    @Override
    public void update(TbUser user) {
        userMapper.updateByPrimaryKey(user);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbUser findOne(Long id) {
        return userMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            userMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbUser user, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        TbUserExample example = new TbUserExample();
        TbUserExample.Criteria criteria = example.createCriteria();
        if (user != null) {
            if (user.getUsername() != null && user.getUsername().length() > 0) {
                criteria.andUsernameLike("%" + user.getUsername() + "%");
            }
            if (user.getPassword() != null && user.getPassword().length() > 0) {
                criteria.andPasswordLike("%" + user.getPassword() + "%");
            }
            if (user.getPhone() != null && user.getPhone().length() > 0) {
                criteria.andPhoneLike("%" + user.getPhone() + "%");
            }
            if (user.getEmail() != null && user.getEmail().length() > 0) {
                criteria.andEmailLike("%" + user.getEmail() + "%");
            }
            if (user.getSourceType() != null && user.getSourceType().length() > 0) {
                criteria.andSourceTypeLike("%" + user.getSourceType() + "%");
            }
            if (user.getNickName() != null && user.getNickName().length() > 0) {
                criteria.andNickNameLike("%" + user.getNickName() + "%");
            }
            if (user.getName() != null && user.getName().length() > 0) {
                criteria.andNameLike("%" + user.getName() + "%");
            }
            if (user.getStatus() != null && user.getStatus().length() > 0) {
                criteria.andStatusLike("%" + user.getStatus() + "%");
            }
            if (user.getHeadPic() != null && user.getHeadPic().length() > 0) {
                criteria.andHeadPicLike("%" + user.getHeadPic() + "%");
            }
            if (user.getQq() != null && user.getQq().length() > 0) {
                criteria.andQqLike("%" + user.getQq() + "%");
            }
            if (user.getIsMobileCheck() != null && user.getIsMobileCheck().length() > 0) {
                criteria.andIsMobileCheckLike("%" + user.getIsMobileCheck() + "%");
            }
            if (user.getIsEmailCheck() != null && user.getIsEmailCheck().length() > 0) {
                criteria.andIsEmailCheckLike("%" + user.getIsEmailCheck() + "%");
            }
            if (user.getSex() != null && user.getSex().length() > 0) {
                criteria.andSexLike("%" + user.getSex() + "%");
            }

        }

        Page<TbUser> page = (Page<TbUser>) userMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 生成验证码,并发送到消息队列
     * @param phone 手机号码
     */
    @Override
    public void createSmsCode(final String phone) {
        //1.生成一个6位随机数（验证码）,后面加空字符串转换成字符串
        final String smsCode = (long) (Math.random() * 1000000) + "";
        System.out.println("验证码：" + smsCode);
        //2.将验证码放入redis，key是手机号码，value是验证码
        redisTemplate.boundHashOps("smsCode").put(phone, smsCode);
        //3.将短信内容发送给activeMQ
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage message = session.createMapMessage();
//      注意map里面的key名要跟短息发送服务里面的一致
                message.setString("mobile", phone);//手机号
                message.setString("templateId", templateId);//模板id
                message.setString("uid", uid);//非必填
                //Map map = new HashMap();
//                表示一分钟有效
                message.setString("param", smsCode + ",1");
                //message.setString("param", JSON.toJSONString(map));
                return message;
            }
        });
    }
    /**
     * 判断短信验证码是否存在,前端用户提交的数据
     * @param phone 手机号码
     * @param code  验证密码
     * @return
     */
    @Override
    public boolean checkSmsCode(String phone, String code) {
        //得到缓存中存储的验证码
        String systemCode = (String) redisTemplate.boundHashOps("smsCode").get(phone);
        if (systemCode == null) {
            return false;
        }
        //判断缓存中的验证码与用户输入的验证码是否相等
        if (!systemCode.equals(code)) {
            return false;
        }
        return true;
    }

}
