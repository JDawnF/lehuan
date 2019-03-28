package com.lehuan.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.lehuan.pojo.TbItem;
import com.lehuan.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

/**
 * @program: lehuan-parent
 * @description: 监听类
 * @author: baichen
 **/
//包扫描后注入到spring容器中
@Component
public class ItemSearchListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchSearchService;  //本地调用

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        try {
            String text = textMessage.getText();
            System.out.println("监听到消息：" + text);
            //转换为json字符串
            List<TbItem> itemList = JSON.parseArray(text, TbItem.class);
            itemSearchSearchService.importList(itemList);
            System.out.println("导入到solr索引库");

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
