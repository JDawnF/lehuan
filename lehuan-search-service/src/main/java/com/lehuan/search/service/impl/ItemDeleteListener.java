package com.lehuan.search.service.impl;

import com.lehuan.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;
import java.util.Arrays;

/**
 * @program: lehuan-parent
 * @description: 监听类,用于删除索引库中记录
 * @author: baichen
 * @create: 2018-11-26 21:40
 **/
// 注入到Spring容器中的bean属性
@Component
public class ItemDeleteListener implements MessageListener {
    @Autowired      //本地调用
    private ItemSearchService itemSearchSearchService;

    @Override
    public void onMessage(Message message) {
        //传的是对象消息
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long[] goodsIds =(Long[]) objectMessage.getObject();
            System.out.println("ItemDeleteListener监听到消息：" + goodsIds);
            itemSearchSearchService.deleteByGoodsIds(Arrays.asList(goodsIds));
            System.out.println("成功删除索引库中的记录");

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
