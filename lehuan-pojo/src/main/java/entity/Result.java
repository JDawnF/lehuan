package entity;

import java.io.Serializable;

/**
 * @program: lehuan-parent
 * @description: 定义返回的执行结果封装实体
 * @author: baichen
 *  2018-10-01 22:50
 **/
public class Result implements Serializable {
    private boolean success;    //是否成功
    private String message;     // 返回的信息,也可以返回处理的结果

    public Result(boolean success, String message) {
        super();
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
