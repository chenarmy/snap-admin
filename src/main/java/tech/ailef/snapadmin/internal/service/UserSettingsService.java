/* 
 * SnapAdmin - An automatically generated CRUD admin UI for Spring Boot apps
 * Copyright (C) 2023 Ailef (http://ailef.tech)
 * 

 */

package tech.ailef.snapadmin.internal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import tech.ailef.snapadmin.internal.mapper.UserSettingMapper;
import tech.ailef.snapadmin.internal.model.UserSetting;

@Service
public class UserSettingsService {
	@Autowired
	private TransactionTemplate internalTransactionTemplate;
	
	@Autowired
	private UserSettingMapper mapper;
	
	public UserSetting save(UserSetting q) {
		return internalTransactionTemplate.execute((status) -> {
			mapper.insert(q);
			return q;
		});
	
	}
}
