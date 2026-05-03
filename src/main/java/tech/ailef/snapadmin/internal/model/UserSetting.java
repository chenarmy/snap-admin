/* 
 * SnapAdmin - An automatically generated CRUD admin UI for Spring Boot apps
 * Copyright (C) 2023 Ailef (http://ailef.tech)
 * 

 */


package tech.ailef.snapadmin.internal.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * A single variable in the user settings.
 */
@TableName("user_setting")
public class UserSetting {
	/**
	 * The id of the variable (its name)
	 */
	@TableId(type = IdType.INPUT)
	private String id;
	
	/**
	 * The value of the variable
	 */
	private String settingValue;
	
	public UserSetting() {
	}
	
	public UserSetting(String id, String settingValue) {
		this.id = id;
		this.settingValue = settingValue;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSettingValue() {
		return settingValue;
	}
	
	public void setSettingValue(String settingValue) {
		this.settingValue = settingValue;
	}
	
}
