package com.lehuan.page.service.impl;

import com.lehuan.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * @program: lehuan-parent
 * @author: baichen
 **/
@Component
public class PageDeleteListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long[] goodsIds = (Long[]) objectMessage.getObject();
            System.out.println("ItemDeleteListener监听接收到消息..." + goodsIds);
            boolean res = itemPageService.deleteItemHtml(goodsIds);
            System.out.println("网页删除结果：" + res);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}

