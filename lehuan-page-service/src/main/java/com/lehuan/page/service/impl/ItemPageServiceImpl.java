package com.lehuan.page.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import com.lehuan.mapper.TbGoodsDescMapper;
import com.lehuan.mapper.TbGoodsMapper;
import com.lehuan.mapper.TbItemCatMapper;
import com.lehuan.mapper.TbItemMapper;
import com.lehuan.page.service.ItemPageService;
import com.lehuan.pojo.TbGoods;
import com.lehuan.pojo.TbGoodsDesc;
import com.lehuan.pojo.TbItem;
import com.lehuan.pojo.TbItemExample;
import com.lehuan.pojo.TbItemExample.Criteria;

import freemarker.template.Configuration;
import freemarker.template.Template;

@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Value("${pageDir}")
    private String pageDir;
    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public boolean genItemHtml(Long goodsId) {
        Configuration configuration = freeMarkerConfigurer.getConfiguration();
        try {
            Template template = configuration.getTemplate("item.ftl");
            //创建数据模型，需要从数据库拿
            Map dataModel = new HashMap<>();
            //1.加载商品主表数据
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goods", goods);
            //2.加载商品扩展表数据
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc", goodsDesc);
            //3.读取商品分类,生成商品类型面包屑
            String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            dataModel.put("itemCat1", itemCat1);
            dataModel.put("itemCat2", itemCat2);
            dataModel.put("itemCat3", itemCat3);
            // 当我们选择规格后，应该在页面上更新商品名称为SKU的商品标题，价格也应该为SKU的商品价格。
            //4.读取SKU列表
            TbItemExample example = new TbItemExample();    // 创建查询条件
            Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(goodsId);    //SPU ID
            criteria.andStatusEqualTo("1");    //状态有效
//            按照状态降序，保证第一个为默认，按is_default这个字段进行排序
            example.setOrderByClause("is_default desc");    //按是否默认字段进行降序排序，目的是返回的结果第一条为默认SKU
            List<TbItem> itemList = itemMapper.selectByExample(example);
            dataModel.put("itemList", itemList);
            // 输出对象，生成不同的商品页面
            Writer out = new FileWriter(pageDir + goodsId + ".html");
            template.process(dataModel, out);//输出
            out.close();
            return true;
            //用大的	Exception则不用抛出IO和模板异常
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public boolean deleteItemHtml(Long[] goodsIds) {
        try {
            for (Long goodsId : goodsIds) {
                new File(pageDir + goodsId + ".html").delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}
