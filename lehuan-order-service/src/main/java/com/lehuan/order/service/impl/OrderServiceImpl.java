package com.lehuan.order.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.lehuan.group.Cart;
import common.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.lehuan.mapper.TbOrderItemMapper;
import com.lehuan.mapper.TbOrderMapper;
import com.lehuan.pojo.TbOrder;
import com.lehuan.pojo.TbOrderExample;
import com.lehuan.pojo.TbOrderExample.Criteria;
import com.lehuan.pojo.TbOrderItem;
import com.lehuan.order.service.OrderService;

import entity.PageResult;

/**
 * 服务实现层
 * @author baichen
 */
@Service
@Transactional      //加上事务注解
public class OrderServiceImpl implements OrderService {

    @Autowired
    private TbOrderMapper orderMapper;

//    @Autowired
//    private TbPayLogMapper payLogMapper;

    @Autowired
    private RedisTemplate redisTemplate;
//    注入snowflake工具类
    @Autowired
    private IdWorker idWorker;

    /**
     * 查询全部
     */
    @Override
    public List<TbOrder> findAll() {
        return orderMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Autowired
    private TbOrderItemMapper orderItemMapper;

    /**
     * 增加,前端传过来的TbOrder对象
     */
    @Override
    public void add(TbOrder order) {
        //1.从Redis中提取得到购物车列表
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
        //2.循环购车列表添加订单
        for (Cart cart : cartList) {
            //订单主表信息
            //存到商品明细表也要用orderId，通过snowflake工具类创建orderId
            long orderId = idWorker.nextId();   //获取ID
            System.out.println("sellerId:" + cart.getSellerId());
            //因为商家不一样，所以会有不同的订单，这里重新创建order，存入相应的数据
            TbOrder tbOrder = new TbOrder();
            tbOrder.setOrderId(orderId);
            tbOrder.setPaymentType(order.getPaymentType());     //支付类型
            tbOrder.setStatus("1");     //状态，未付款
            tbOrder.setCreateTime(new Date());//订单创建日期
            tbOrder.setUpdateTime(new Date());//订单更新日期
            tbOrder.setUserId(order.getUserId());//用户名
            tbOrder.setReceiverAreaName(order.getReceiverAreaName());//地址
            tbOrder.setReceiverMobile(order.getReceiverMobile());//手机号
            tbOrder.setReceiver(order.getReceiver());//收货人
            tbOrder.setSourceType(order.getSourceType());//订单来源
            tbOrder.setSellerId(cart.getSellerId());//商家ID
            //2.循环购物车明细,订单从表信息
            double money = 0;
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                orderItem.setId(idWorker.nextId()); //生成ID
                orderItem.setOrderId(orderId);//订单ID
                orderItem.setSellerId(cart.getSellerId());
                money += orderItem.getTotalFee().doubleValue();//金额累加
                orderItemMapper.insert(orderItem);
            }
            tbOrder.setPayment(new BigDecimal(money));
            orderMapper.insert(tbOrder);
        }
        //3.清除Redis中的购物车数据
        redisTemplate.boundHashOps("cartList").delete(order.getUserId());
    }


    /**
     * 修改
     */
    @Override
    public void update(TbOrder order) {
        orderMapper.updateByPrimaryKey(order);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbOrder findOne(Long id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            orderMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbOrderExample example = new TbOrderExample();
        Criteria criteria = example.createCriteria();

        if (order != null) {
            if (order.getPaymentType() != null && order.getPaymentType().length() > 0) {
                criteria.andPaymentTypeLike("%" + order.getPaymentType() + "%");
            }
            if (order.getPostFee() != null && order.getPostFee().length() > 0) {
                criteria.andPostFeeLike("%" + order.getPostFee() + "%");
            }
            if (order.getStatus() != null && order.getStatus().length() > 0) {
                criteria.andStatusLike("%" + order.getStatus() + "%");
            }
            if (order.getShippingName() != null && order.getShippingName().length() > 0) {
                criteria.andShippingNameLike("%" + order.getShippingName() + "%");
            }
            if (order.getShippingCode() != null && order.getShippingCode().length() > 0) {
                criteria.andShippingCodeLike("%" + order.getShippingCode() + "%");
            }
            if (order.getUserId() != null && order.getUserId().length() > 0) {
                criteria.andUserIdLike("%" + order.getUserId() + "%");
            }
            if (order.getBuyerMessage() != null && order.getBuyerMessage().length() > 0) {
                criteria.andBuyerMessageLike("%" + order.getBuyerMessage() + "%");
            }
            if (order.getBuyerNick() != null && order.getBuyerNick().length() > 0) {
                criteria.andBuyerNickLike("%" + order.getBuyerNick() + "%");
            }
            if (order.getBuyerRate() != null && order.getBuyerRate().length() > 0) {
                criteria.andBuyerRateLike("%" + order.getBuyerRate() + "%");
            }
            if (order.getReceiverAreaName() != null && order.getReceiverAreaName().length() > 0) {
                criteria.andReceiverAreaNameLike("%" + order.getReceiverAreaName() + "%");
            }
            if (order.getReceiverMobile() != null && order.getReceiverMobile().length() > 0) {
                criteria.andReceiverMobileLike("%" + order.getReceiverMobile() + "%");
            }
            if (order.getReceiverZipCode() != null && order.getReceiverZipCode().length() > 0) {
                criteria.andReceiverZipCodeLike("%" + order.getReceiverZipCode() + "%");
            }
            if (order.getReceiver() != null && order.getReceiver().length() > 0) {
                criteria.andReceiverLike("%" + order.getReceiver() + "%");
            }
            if (order.getInvoiceType() != null && order.getInvoiceType().length() > 0) {
                criteria.andInvoiceTypeLike("%" + order.getInvoiceType() + "%");
            }
            if (order.getSourceType() != null && order.getSourceType().length() > 0) {
                criteria.andSourceTypeLike("%" + order.getSourceType() + "%");
            }
            if (order.getSellerId() != null && order.getSellerId().length() > 0) {
                criteria.andSellerIdLike("%" + order.getSellerId() + "%");
            }

        }

        Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

//    @Override
//    public TbPayLog searchPayLogFromRedis(String userId) {
//        return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
//    }
//
//    @Override
//    public void updateOrderStatus(String out_trade_no, String transaction_id) {
//        //1.修改支付日志的状态及相关字段
//        TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
//        payLog.setPayTime(new Date());//支付时间
//        payLog.setTradeState("1");//交易成功
//        payLog.setTransactionId(transaction_id);//微信的交易流水号
//
//        payLogMapper.updateByPrimaryKey(payLog);//修改
//        //2.修改订单表的状态
//        String orderList = payLog.getOrderList();// 订单ID 串
//        String[] orderIds = orderList.split(",");
//
//        for (String orderId : orderIds) {
//            TbOrder order = orderMapper.selectByPrimaryKey(Long.valueOf(orderId));
//            order.setStatus("2");//已付款状态
//            order.setPaymentTime(new Date());//支付时间
//            orderMapper.updateByPrimaryKey(order);
//        }
//
//        //3.清除缓存中的payLog
//        redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
//
//    }

}
