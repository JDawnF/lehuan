package com.lehuan.solrutil;

import com.alibaba.fastjson.JSON;
import com.lehuan.mapper.TbItemMapper;
import com.lehuan.pojo.TbItem;
import com.lehuan.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @program: lehuan-parent
 * @description:
 * @author: baichen
 *  2018-11-12 20:04
 **/
@Component
public class SolrUtil {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private SolrTemplate solrTemplate;

    //    导入商品数据
    public void importItemData() {
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");  //通过商品审核的才导入
        List<TbItem> tbItems = itemMapper.selectByExample(example);
        System.out.println("===商品列表===");
        for (TbItem item : tbItems) {
            System.out.println(item.getTitle());
            // {"机身内存":"16G","网络":"联通3G"}
            //从数据库中提取规格json字符串转换为map，注意因为这里是{}，所以是用parseObject，如果是[]，则用parseArray
            Map specMap = JSON.parseObject(item.getSpec(), Map.class);
            //给带注解的字段赋值
            item.setSpecMap(specMap);
        }
        solrTemplate.saveBeans(tbItems);
        solrTemplate.commit();
        System.out.println("===结束===");
    }

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");
        solrUtil.importItemData();
    }
}
