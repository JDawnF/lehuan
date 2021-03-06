package com.lehuan.manager.controller;
import java.util.List;
import com.alibaba.fastjson.JSON;
import com.lehuan.group.Goods;
import com.lehuan.pojo.TbItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.lehuan.pojo.TbGoods;
import com.lehuan.sellergoods.service.GoodsService;
import entity.PageResult;
import entity.Result;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * controller
 *
 * @author baichen
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;
    //    通过消息中间件ActiveMQ实现导入索引库，所以不用search模块了
//    @Reference(timeout = 100000)
//    private ItemSearchService itemSearchService;
//    通过ActiveMQ实现
//    @Reference(timeout = 40000)
//    private ItemPageService itemPageService;
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination queueSolrDestination;   //用于导入solr索引库的消息目标，点对点的形式
    @Autowired
    private Destination queueSolrDeleteDestination;     //用户在索引库中删除记录
    @Autowired
    private Destination topicPageDestination;   //用于生成商品详情页的消息目标，发布订阅的方式
    @Autowired
    private Destination topicPageDeleteDestination; //用于删除静态网页的消息

    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbGoods> findAll() {
        return goodsService.findAll();
    }


    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int rows) {
        return goodsService.findPage(page, rows);
    }

//    /**
//     * 增加
//     * @param goods
//     * @return
//     */
//    @RequestMapping("/add")
//    public Result add(@RequestBody TbGoods goods){
//        try {
//            goodsService.add(goods);
//            return new Result(true, "增加成功");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new Result(false, "增加失败");
//        }
//    }

    /**
     * 修改
     *
     * @param goods
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Goods goods) {
        try {
            goodsService.update(goods);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 获取实体
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public Goods findOne(Long id) {
        return goodsService.findOne(id);
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(final Long[] ids) {
        try {
            goodsService.delete(ids);
            //转换为集合，并从索引库中删除
//            itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
            jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    //对象消息，转换为可序列化的对象,才可以在网络中传输
                    return session.createObjectMessage(ids);
                }
            });

//            //删除每个服务器上的商品详情页
            jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
                }
            });
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * 查询+分页
     *
     * @param goods
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
        return goodsService.findPage(goods, page, rows);
    }

    /**
     * 更新状态
     *
     * @param ids    商品id集合
     * @param status 商品状态
     */
    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status) {
        try {
            goodsService.updateStatus(ids, status);
            //按照SPU ID查询 SKU列表(状态为1)
            if ("1".equals(status)) {    //审核通过
                List<TbItem> itemList = goodsService.findItemListByGoodsIdAndStatus(ids, status);
                //调用搜索接口实现数据批量导入，先转换为json字符串传递
                // 导入到索引库
                if (itemList.size() > 0) {
//                    itemSearchService.importList(itemList);
                    //转换为json字符串才可以用createTextMessage
                    final String jsonString = JSON.toJSONString(itemList);
                    jmsTemplate.send(queueSolrDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(jsonString);
                        }
                    });
                    //静态页生成
                    for (final Long goodsId : ids) {
//                        itemPageService.genItemHtml(goodsId);
                        jmsTemplate.send(topicPageDestination, new MessageCreator() {
                            @Override
                            public Message createMessage(Session session) throws JMSException {
                                //加空引号表示转换为字符串形式
                                return session.createTextMessage(goodsId + "");
                            }
                        });
                    }
                } else {
                    System.out.println("没有明细数据");
                }
            }
            return new Result(true, "修改状态成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改状态失败");
        }
    }

    /**
     * 生成静态页（测试）
     *
     * @param goodsId
     */
//    @RequestMapping("/genHtml")
//    public void genHtml(Long goodsId) {
//        itemPageService.genItemHtml(goodsId);
//    }

}
