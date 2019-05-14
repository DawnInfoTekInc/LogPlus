package com.dawninfotek.logplus.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BeanUtils {

	static final Logger logger = LoggerFactory.getLogger(BeanUtils.class);

	/**
	 * return a Object which get form the source object under give path
	 * 
	 * @param source
	 * @param path
	 * @return
	 */

	public static Object getObject(Object source, String path) {

		if (source == null || StringUtils.isEmpty(path)) {
			return source;
		}

		String nextPath = null;
		String internalPath = null;
		//int indexOfDot = 0;
		
		
		String[] splited = split(path, ".", "'");
		nextPath = splited[0];
		if(splited.length > 1){
			internalPath = StringUtils.remove(path, nextPath + ".");
		}

		if (internalPath != null) {

			Object obj = getObject(source, nextPath);

			if (obj == null) {
				return null;
			} else if (obj instanceof Collection) {

				ArrayList<Object> al = new ArrayList<Object>();

				for (Object o : (Collection) obj) {

					al.add(getObject(o, internalPath));

				}

				return al;

			} else {

				return getObject(obj, internalPath);

			}

		} else {
			// no more internal path
			// remove all ' here
			
			String realPath = StringUtils.remove(path, "'");
			//realPath = LogPlusUtils.removeEnd(realPath, "'");

			if (source instanceof Map) {

				return ((Map) source).get(realPath);

			} else if (source instanceof Collection) {
				// only return on dimension Collection (no Collection of Collection) for the Collection type.
				// create the collection and pass it to the deeper level

				// upper level includes Collection, this case is not supported, throw a Runtime Exception
				throw new UnsupportedOperationException("This util doesn't support get Object from Collection of Collection.");

			} else if (source.getClass().isArray()) {
				// the path off array type must be [nn]
				if (realPath.startsWith("[") && realPath.endsWith("]")) {
					// get the index number
					String strIndex = realPath.substring(1, realPath.length() - 1);

					int index = 0;
					try {
						index = Integer.valueOf(strIndex).intValue();

					} catch (Exception e) {
						if (logger.isTraceEnabled()) {
							logger.trace("", e);
						}
						return null;
					}

					if (source instanceof Object[]) {
						return ((Object[]) source)[index];
					}

					if (source instanceof boolean[]) {
						return ((boolean[]) source)[index];
					}

					if (source instanceof byte[]) {
						return ((byte[]) source)[index];
					}

					if (source instanceof char[]) {
						return ((char[]) source)[index];
					}

					if (source instanceof double[]) {
						return ((double[]) source)[index];
					}

					if (source instanceof float[]) {
						return ((float[]) source)[index];
					}

					if (source instanceof int[]) {
						return ((int[]) source)[index];
					}

					if (source instanceof long[]) {
						return ((long[]) source)[index];
					}

					if (source instanceof short[]) {
						return ((short[]) source)[index];
					}

					return null;

				} else {
					return null;
				}

			} else {

				boolean methodFullName = false;

				if (realPath.startsWith("method:")) {
					methodFullName = true;
				}

				if (!methodFullName) {
					// try field directly first
					try {

						Field field = source.getClass().getField(realPath);

						if (field != null) {

							return field.get(source);

						}

					} catch (Exception e) {	

					}
				}

				// allow parameter for path
				Class[] clazzes = new Class[0];
				Object[] values = new Object[0];

				// parameters are inside '()' and separated by ','
				// each parameter starts with {type}:, if there is no type defined, will treated as String.
				if (realPath.endsWith(")")) {

					String[] ps = StringUtils.split(realPath.substring(realPath.indexOf("(") + 1, realPath.length() - 1), ",");

					values = new Object[ps.length];
					clazzes = new Class[ps.length];

					for (int i = 0; i < ps.length; i++) {

						String pdef = ps[i];

						if (pdef.indexOf(":") <= 0) {

							clazzes[i] = String.class;
							values[i] = pdef;

						} else {

							String[] t = pdef.split(":");
							String type = t[0];
							String value = t[1];

							if (type.equals("String")) {
								clazzes[i] = String.class;
								values[i] = value;

							} else if (type.equals("boolean")) {

								clazzes[i] = boolean.class;
								values[i] = Boolean.valueOf(value).booleanValue();

							} else if (type.equals("byte")) {
								clazzes[i] = byte.class;
								values[i] = Byte.valueOf(value).byteValue();

							} else if (type.equals("double")) {
								clazzes[i] = double.class;
								values[i] = Double.valueOf(value).doubleValue();

							} else if (type.equals("float")) {
								clazzes[i] = float.class;
								values[i] = Float.valueOf(value).floatValue();

							} else if (type.equals("int")) {
								clazzes[i] = int.class;
								values[i] = Integer.valueOf(value).intValue();

							} else if (type.equals("long")) {
								clazzes[i] = long.class;
								values[i] = Long.valueOf(value).longValue();

							} else if (type.equals("Boolean")) {
								clazzes[i] = Boolean.class;
								values[i] = Boolean.valueOf(value);

							} else if (type.equals("Byte")) {
								clazzes[i] = Byte.class;
								values[i] = Byte.valueOf(value);

							} else if (type.equals("Double")) {
								clazzes[i] = Double.class;
								values[i] = Double.valueOf(value);

							} else if (type.equals("Float")) {
								clazzes[i] = Float.class;
								values[i] = Float.valueOf(value);

							} else if (type.equals("Integer")) {
								clazzes[i] = Integer.class;
								values[i] = Integer.valueOf(value);

							} else if (type.equals("Long")) {
								clazzes[i] = Long.class;
								values[i] = Long.valueOf(value);

							} else if (type.equals("Decimal")) {
								clazzes[i] = BigDecimal.class;
								values[i] = new BigDecimal(value);						

							} else {
								throw new UnsupportedOperationException("undefined type:" + type);
							}

						}

					}

				}

				if (methodFullName) {
					
					try {

						String getterName = null;
						
						realPath = StringUtils.removeStart(realPath, "method:");

						if (realPath.indexOf("(") > 0) {

							getterName = realPath.substring(0, realPath.indexOf("("));

						} else {
							getterName = realPath;
						}

						Method getter = source.getClass().getMethod(getterName, clazzes);

						return getter.invoke(source, values);	

					} catch (Exception e) {

						// do nothing will return null;
						// e.fillInStackTrace();
					}

				} else {
	
					try {

						String getterName = null;

						if (realPath.indexOf("(") > 0) {

							getterName = "get" + StringUtils.capitalize(realPath.substring(0, realPath.indexOf("(")));

						} else {
							getterName = "get" + StringUtils.capitalize(realPath);
						}

						Method getter = source.getClass().getMethod(getterName, clazzes);

						return getter.invoke(source, values);

					} catch (Exception e) {
						// turn on the trace log to see what's happening.
						if (logger.isTraceEnabled()) {
							logger.trace("something went wrong", e);
						}
						e.printStackTrace();
						// do nothing will return null;
					}

				}

				return null;

			}

		}

	}
	
	/**
	 * Split a String with given splitter but not includes the text in the protector.
	 * 
	 * @param text
	 * @param spliter
	 * @param protector
	 * @return
	 */
	public static String[] split(String text, String splitter, String protector) {

		if (text == null || splitter == null) {
			return null;
		}

		ArrayList<String> ra = new ArrayList<String>();

		String[] texts = StringUtils.split(text, splitter);

		if (protector == null) {
			// if there's no protector
			return texts;
		}

		String t = "";

		for (String splitted : texts) {

			t = t + splitted;

			if (StringUtils.countMatches(t, protector) % 2 == 0) {
				ra.add(t);
				t = "";
			} else {
				// add the splitter back
				t = t + splitter;
			}

		}

		if (ra.size() == 0) {
			return null;
		} else {
			return ra.toArray(new String[0]);
		}

	}

}