/**
 * 
 */
package xin.xihc.jba;

import java.lang.reflect.Field;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import xin.xihc.jba.annotation.Column;
import xin.xihc.jba.annotation.Column.Policy;
import xin.xihc.jba.annotation.Table;
import xin.xihc.jba.db.InitTableDataIntf;
import xin.xihc.jba.db.JbaTemplate;
import xin.xihc.jba.db.TableOperator;
import xin.xihc.jba.properties.ColumnProperties;
import xin.xihc.jba.properties.TableManager;
import xin.xihc.jba.properties.TableManager.Mode;
import xin.xihc.jba.properties.TableProperties;

/**
 * 启动时加载
 * 
 * @author 席恒昌
 * @date 2018年1月24日
 * @version
 * @since
 */
@Component
public class AnnotationScan implements SmartLifecycle {

	public static final String BANNE_JBA = "_________________________________________________________________\n"
			+ "                                                                 \n"
			+ "                       ,                            ,   /        \n"
			+ "---__------__---)__--------__----__--------------------/__----__-\n"
			+ "  (_ `   /   ) /   ) /   /   ) /   )     ===      /   /   ) /   )\n"
			+ "_(__)___/___/_/_____/___/___/_(___/______________/___(___/_(___(_\n"
			+ "       /                         /              /                \n"
			+ "      /                      (_ /           (_ /                 \n";

	private static boolean isRunning = false;

	@Autowired
	TableOperator tableOperator;

	@Autowired
	JbaTemplate jbaTemplate;

	@Value("${spring.datasource.url:null}")
	private String dbUrl;

	@Value("${spring.jba.debugger:true}")
	private boolean debugger;
	@Value("${spring.jba.mode:ALL}")
	private String mode;

	/**
	 * 1. 我们主要在该方法中启动任务或者其他异步服务，比如开启MQ接收消息<br/>
	 * 2.
	 * 当上下文被刷新（所有对象已被实例化和初始化之后）时，将调用该方法，默认生命周期处理器将检查每个SmartLifecycle对象的isAutoStartup()方法返回的布尔值。
	 * 如果为“true”，则该方法会被调用，而不是等待显式调用自己的start()方法。
	 */
	@Override
	public void start() {
		// 打印banner
		System.out.println(BANNE_JBA + "\r\n===================:: spring-jba :: Started ::===================\n");
		// 设置数据源地址，用于区别数据库类型
		jbaTemplate.setDbType(dbUrl);
		TableManager.debugger = debugger;
		TableManager.mode = Mode.valueOf(mode);
		Map<String, Object> map = SpringContextUtil.getApplicationContext().getBeansWithAnnotation(Table.class);
		for (Object obj : map.values()) {
			Table table = obj.getClass().getAnnotation(Table.class);
			TableProperties tblP = null;
			if ("".equals(table.value())) {
				tblP = TableManager.addTable(obj.getClass().getSimpleName(), obj.getClass().getSimpleName());
			} else {
				tblP = TableManager.addTable(obj.getClass().getSimpleName(), table.value());
			}
			// 获取表注释
			tblP.setRemark(table.remark());
			Class<?>[] interfaces = obj.getClass().getInterfaces();
			for (Class<?> class1 : interfaces) {
				if (InitTableDataIntf.class.equals(class1)) {
					Object[] initModel = ((InitTableDataIntf<?>) obj).initModel();
					tblP.initData(initModel);
					break;
				}
			}
			int keyCount = 0;
			for (Field field : jbaTemplate.getAllFields(obj.getClass())) {
				field.setAccessible(true);
				Column column = field.getAnnotation(Column.class);
				ColumnProperties colP = new ColumnProperties();
				tblP.addColumn(field.getName(), colP);
				colP.type(field.getType());
				if (null == column) {
					colP.colName(field.getName());
				} else {
					colP.colName(field.getName()).defaultValue(column.defaultValue()).notNull(column.notNull())
							.remark(column.remark());
					colP.length(0);
					if (column.length() > 0) {
						colP.length(column.length());
					}
					if (column.precision() > 0) {
						colP.precision(column.precision());
					}
					if (column.primary()) {
						keyCount++;
						if (keyCount > 1) {
							throw new RuntimeException("主键数量超过一个了.");
						}
						colP.primary(true);
						colP.notNull(true);
					}
					colP.policy(column.policy());
					/** 如果是guid为主键长度默认为32 */
					if (colP.policy() == Policy.GUID || colP.policy() == Policy.GUID_UP) {
						colP.length(32);
					}
				}
			}
		}
		// 执行表创建、字段更新
		tableOperator.init();

		// 执行完其他业务后，可以修改 isRunning = true
		isRunning = true;
	}

	/**
	 * 如果工程中有多个实现接口SmartLifecycle的类，则这些类的start的执行顺序按getPhase方法返回值从小到大执行。<br/>
	 * 例如：1比2先执行，-1比0先执行。 stop方法的执行顺序则相反，getPhase返回值较大类的stop方法先被调用，小的后被调用。
	 */
	@Override
	public int getPhase() {
		// 默认为0
		return 0;

	}

	/**
	 * 根据该方法的返回值决定是否执行start方法。<br/>
	 * 返回true时start方法会被自动执行，返回false则不会。
	 */
	@Override
	public boolean isAutoStartup() {
		// 默认为false
		return true;
	}

	/**
	 * 1. 只有该方法返回false时，start方法才会被执行。<br/>
	 * 2. 只有该方法返回true时，stop(Runnable callback)或stop()方法才会被执行。
	 */
	@Override
	public boolean isRunning() {
		// 默认返回false
		return isRunning;
	}

	/**
	 * SmartLifecycle子类的才有的方法，当isRunning方法返回true时，该方法才会被调用。
	 */
	@Override
	public void stop(Runnable callback) {
		// 打印banner
		System.out.println(BANNE_JBA + "\r\n===================:: spring-jba :: Stoped ::====================\n");

		// 如果你让isRunning返回true，需要执行stop这个方法，那么就不要忘记调用callback.run()。
		// 否则在你程序退出时，Spring的DefaultLifecycleProcessor会认为你这个TestSmartLifecycle没有stop完成，程序会一直卡着结束不了，等待一定时间（默认超时时间30秒）后才会自动结束。
		// PS：如果你想修改这个默认超时时间，可以按下面思路做，当然下面代码是springmvc配置文件形式的参考，在SpringBoot中自然不是配置xml来完成，这里只是提供一种思路。
		// <bean id="lifecycleProcessor"
		// class="org.springframework.context.support.DefaultLifecycleProcessor">
		// <!-- timeout value in milliseconds -->
		// <property name="timeoutPerShutdownPhase" value="10000"/>
		// </bean>
		callback.run();

		isRunning = false;
	}

	/**
	 * 接口Lifecycle的子类的方法，只有非SmartLifecycle的子类才会执行该方法。<br/>
	 * 1. 该方法只对直接实现接口Lifecycle的类才起作用，对实现SmartLifecycle接口的类无效。<br/>
	 * 2. 方法stop()和方法stop(Runnable callback)的区别只在于，后者是SmartLifecycle子类的专属。
	 */
	@Override
	public void stop() {
		System.out.println("stop");

		isRunning = false;
	}

}
