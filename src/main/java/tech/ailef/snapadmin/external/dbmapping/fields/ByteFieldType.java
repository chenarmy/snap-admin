/* 
 * SnapAdmin - An automatically generated CRUD admin UI for Spring Boot apps
 * Copyright (C) 2023 Ailef (http://ailef.tech)
 * 

 */

package tech.ailef.snapadmin.external.dbmapping.fields;

import java.util.List;

import tech.ailef.snapadmin.external.dto.CompareOperator;
import tech.ailef.snapadmin.external.exceptions.SnapAdminException;

public class ByteFieldType extends DbFieldType {
	@Override
	public String getFragmentName() {
		return "number";
	}

	@Override
	public Object parseValue(Object value) {
		if (value == null || value.toString().isBlank()) return null;
		return value.toString().getBytes()[0];
	}

	@Override
	public Class<?> getJavaClass() {
		return byte.class;
	}
	
	@Override
	public List<CompareOperator> getCompareOperators() {
		throw new SnapAdminException("Binary fields are not comparable");
	}
}
