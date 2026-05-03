/* 
 * SnapAdmin - An automatically generated CRUD admin UI for Spring Boot apps
 * Copyright (C) 2023 Ailef (http://ailef.tech)
 * 

 */

package tech.ailef.snapadmin.internal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import tech.ailef.snapadmin.internal.mapper.ConsoleQueryMapper;
import tech.ailef.snapadmin.internal.model.ConsoleQuery;

@Service
public class ConsoleQueryService {
	@Autowired
	private TransactionTemplate internalTransactionTemplate;
	
	@Autowired
	private ConsoleQueryMapper mapper;
	
	public ConsoleQuery save(ConsoleQuery q) {
		return internalTransactionTemplate.execute((status) -> {
			if (q.getId() == null) {
				mapper.insert(q);
			} else {
				mapper.updateById(q);
			}
			return q;
		});
	}
	
	public void delete(String id) {
		internalTransactionTemplate.executeWithoutResult((status) -> {
			mapper.deleteById(id);
		});
	}
	
	public List<ConsoleQuery> findAll() {
		return mapper.selectList(null);
	}
	
	public Optional<ConsoleQuery> findById(String id) {
		return Optional.ofNullable(mapper.selectById(id));
	}
}
