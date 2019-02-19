package xin.xihc.jba.core;

import java.util.function.Function;

/**
 * spring-jba排序
 *
 * @Author Leo.Xi
 * @Date 2019/2/19 17:26
 * @Version 1.0
 **/
public class JbaOrder {

    private Function function;
    private Order order = Order.ASC;
    public JbaOrder(Function field) {
        this.function = field;
    }

    /**
     * 排序
     */
    enum Order {
        /**
         * 升序
         */
        ASC,

        /**
         * 降序
         */
        DESC;
    }

}
