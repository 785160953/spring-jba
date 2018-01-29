package xin.xihc.jba.db;

/**
 * 表在创建时初始化数据接口
 * 
 * @author 席恒昌
 * @Date 2018年1月29日
 *
 */
public interface InitTableDataIntf<T> {

	T[] initModel();

}
