package com.lehuan.page.service.impl;

import com.lehuan.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * @program: lehuan-parent
 * @author: baichen
 **/
@Component
public class PageListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        try {
            String text = textMessage.getText();
            System.out.println("接收到消息：" + text);
            //转换为long类型，text其实是goodsId
            boolean res = itemPageService.genItemHtml(Long.parseLong(text));
            System.out.println("网页生成结果"+res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

