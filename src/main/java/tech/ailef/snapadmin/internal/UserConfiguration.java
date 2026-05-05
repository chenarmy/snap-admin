/* 
 * SnapAdmin - An automatically generated CRUD admin UI for Spring Boot apps
 * Copyright (C) 2023 Ailef (http://ailef.tech)
 * 

 */


package tech.ailef.snapadmin.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tech.ailef.snapadmin.external.exceptions.SnapAdminException;
import tech.ailef.snapadmin.internal.mapper.UserSettingMapper;
import tech.ailef.snapadmin.internal.model.UserSetting;

/**
 * Wrapper class for user settings persistence that provides a better
 * way of handling user settings. 
 */
@Component
public class UserConfiguration {
	@Autowired
	private UserSettingMapper mapper;
	
	/**
	 * Returns the value of the specific setting
	 * @param settingName	the name of the setting
	 * @return	the value, if found, otherwise the default value if present, otherwise an empty string
	 */
	public String get(String settingName) {
		Optional<UserSetting> setting = Optional.ofNullable(mapper.selectById(settingName));
		if (setting.isPresent())
			return setting.get().getSettingValue();
		String settingDefaultValue = defaultValues().get(settingName);
		
		if (settingDefaultValue == null)
			throw new SnapAdminException("Trying to access setting `" + settingName + "` but it has no default value");
		
		return settingDefaultValue;
	}

	/**
	 * Returns a map filled with the default values of the settings.
	 * @return
	 */
	private Map<String, String> defaultValues() {
		Map<String, String> values = new HashMap<>();
		values.put("brandName", "SnapAdmin");
		values.put("additionalCss", "");
		return values;
	}
}
