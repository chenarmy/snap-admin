/* 
 * SnapAdmin - An automatically generated CRUD admin UI for Spring Boot apps
 * Copyright (C) 2023 Ailef (http://ailef.tech)
 * 

 */

package tech.ailef.snapadmin.external.dbmapping.fields;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import tech.ailef.snapadmin.external.dto.CompareOperator;
import tech.ailef.snapadmin.external.exceptions.SnapAdminException;

public class EnumFieldType extends DbFieldType {

	private Class<?> klass;
	
	public EnumFieldType(Class<?> klass) {
		this.klass = klass;
	}
	
	@Override
	public String getFragmentName() {
		return "select";
	}
	
	@Override
	public List<?> getValues() {
		try {
			Method method = getJavaClass().getMethod("values");
			Object[] invoke = (Object[])method.invoke(null);
			return Arrays.stream(invoke).collect(Collectors.toList());
		} catch (NoSuchMethodException | SecurityException | InvocationTargetException 
				| IllegalAccessException | IllegalArgumentException e) {
			throw new SnapAdminException(e);
		}
	}

	@Override
	public Object parseValue(Object value) {
		if (value == null || value.toString().isBlank()) return null;
		
		try {
			Method valueOf = getJavaClass().getMethod("valueOf", String.class);
			return valueOf.invoke(null, value.toString());
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof IllegalArgumentException)
				throw new SnapAdminException("Invalid value " + value + " for enum type " + getJavaClass().getSimpleName());
			else
				throw new SnapAdminException(e);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException e) {
			throw new SnapAdminException(e);
		}
	}

	@Override
	public Class<?> getJavaClass() {
		return klass;
	}

	@Override
	public List<CompareOperator> getCompareOperators() {
		return List.of(CompareOperator.EQ);
	}
}
