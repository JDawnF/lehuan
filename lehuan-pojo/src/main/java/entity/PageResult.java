package entity;

import java.io.Serializable;
import java.util.List;

/**
 * @program: lehuan-parent
 * @description: 接收前端返回来的分页数据VO,分页结果类
 * @author: baichen
 * @create: 2018-10-01 10:27
 **/
public class PageResult implements Serializable {
    //    总记录数
    private long total;
    //    当前页记录数
    private List rows;

    public PageResult(long total, List rows) {
        super();
        this.total = total;
        this.rows = rows;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }
}
