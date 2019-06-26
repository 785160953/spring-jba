/**
 *
 */
package xin.xihc.jba.core.utils;

import xin.xihc.utils.common.CommonUtil;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 数据库语句转换
 *
 * @author xihc
 * @version 1.0
 * @date 2018年09月15日
 */
public final class SQLConvert {

	public static final String commaSeparator = ",";

	/**
	 * like
	 *
	 * @param fieldName
	 * @param modelValue
	 * @return
	 */
	public static String like(String fieldName, Map<String, Object> modelValue) {
		Object val = modelValue.get(fieldName);
		if (null != val) {
			modelValue.put(fieldName, "%" + val + "%");
		}
		return fieldName + " LIKE :" + fieldName;
	}

	/**
	 * 等于
	 *
	 * @param fieldName
	 * @param modelValue
	 * @return
	 */
	public static String eq(String fieldName, Map<String, Object> modelValue) {
		Object val = modelValue.get(fieldName);
		if (null != val) {
			if (val instanceof Enum<?>) { // 枚举特殊处理
				modelValue.put(fieldName, val.toString());
			}
		}
		return fieldName + " = :" + fieldName;
	}

	/**
	 * between
	 *
	 * @param fieldName
	 * @param modelValue
	 * @return
	 */
	public static String between(String fieldName, Map<String, Object> modelValue) {
		Object val = modelValue.get(fieldName);
		if (null != val) {
			String[] splits = val.toString().split(commaSeparator);
			if (splits.length < 2) {
				return null;
			}
			for (int i = 0; i < splits.length; i++) {
				modelValue.put(fieldName + "_" + i, splits[i]);
			}
		}
		return fieldName + " BETWEEN :" + fieldName + "_0" + " AND :" + fieldName + "_1";
	}

	/**
	 * in
	 *
	 * @param fieldName
	 * @param modelValue
	 * @return
	 */
	public static String in(String fieldName, Map<String, Object> modelValue, boolean isNot) {
		Object val = modelValue.get(fieldName);
		StringJoiner in = new StringJoiner(commaSeparator);
		if (null != val) { // 处理分组
			if (val instanceof List) {
				for (int i = 0; i < ((List) val).size(); i++) {
					modelValue.put(fieldName + "_" + i, ((List) val).get(i));
					in.add(":" + fieldName + "_" + i);
				}
			} else if (val.getClass().isArray()) {
				for (int i = 0; i < ((Object[]) val).length; i++) {
					modelValue.put(fieldName + "_" + i, ((Object[]) val)[i]);
					in.add(":" + fieldName + "_" + i);
				}
			} else if (val.getClass().equals(String.class)) {
				String[] splits = val.toString().split(commaSeparator);
				for (int i = 0; i < splits.length; i++) {
					modelValue.put(fieldName + "_" + i, splits[i]);
					in.add(":" + fieldName + "_" + i);
				}
			}
		}
		if (in.length() < 1) {
			return null;
		}
		if (isNot) {
			return fieldName + " NOT IN (" + in.toString() + ")";
		}
		return fieldName + " IN (" + in.toString() + ")";
	}

	/**
	 * 大于、大于等于
	 *
	 * @param fieldName
	 * @param modelValue
	 * @return
	 */
	public static String gt(String fieldName, Map<String, Object> modelValue, boolean andEqueals) {
		if (andEqueals) {
			return fieldName + " >= :" + fieldName;
		}
		return fieldName + " > :" + fieldName;
	}

	/**
	 * 小于、小于等于
	 *
	 * @param fieldName
	 * @param modelValue
	 * @return
	 */
	public static String lt(String fieldName, Map<String, Object> modelValue, boolean andEqueals) {
		if (andEqueals) {
			return fieldName + " <= :" + fieldName;
		}
		return fieldName + " < :" + fieldName;
	}

	/**
	 * @param vals
	 * @return
	 */
	public static String toString(Object[] vals) {
		StringJoiner res = new StringJoiner(commaSeparator);
		if (CommonUtil.isNullEmpty(vals)) {
			return null;
		}
		for (Object item : vals) {
			if (CommonUtil.isNotNullEmpty(item)) {
				res.add(item.toString());
			}
		}
		return res.toString();
	}

	/**
	 * @param vals
	 * @return
	 */
	public static String toString(List<Object> vals) {
		if (CommonUtil.isNullEmpty(vals)) {
			return null;
		}
		return toString(vals.toArray(new Object[vals.size()]));
	}

}
