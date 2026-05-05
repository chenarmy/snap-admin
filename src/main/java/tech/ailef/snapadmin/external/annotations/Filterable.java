/* 
 * SnapAdmin - An automatically generated CRUD admin UI for Spring Boot apps
 * Copyright (C) 2023 Ailef (http://ailef.tech)
 * 

 */


package tech.ailef.snapadmin.external.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as filterable and places it in the faceted search bar.
 * (This bar only appears in the interface if one or more fields are filterable 
 * in the current schema.)
 * Can only be placed on fields that correspond to physical columns on the 
 * table and that are not binary (`byte[]`).
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Filterable {
	/**
	 * The type of filter (DEFAULT or CATEGORICAL)
	 * @return
	 */
	public FilterableType type() default FilterableType.DEFAULT;
}
