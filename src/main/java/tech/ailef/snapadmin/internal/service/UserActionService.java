/* 
 * SnapAdmin - An automatically generated CRUD admin UI for Spring Boot apps
 * Copyright (C) 2023 Ailef (http://ailef.tech)
 * 

 */


package tech.ailef.snapadmin.internal.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import tech.ailef.snapadmin.external.dto.LogsSearchRequest;
import tech.ailef.snapadmin.external.dto.PaginatedResult;
import tech.ailef.snapadmin.external.dto.PaginationInfo;
import tech.ailef.snapadmin.internal.mapper.UserActionMapper;
import tech.ailef.snapadmin.internal.model.UserAction;
import tech.ailef.snapadmin.internal.repository.CustomActionRepositoryImpl;

/**
 * Service class to retrieve user actions through the {@link CustomActionRepositoryImpl}. 
 *
 */
@Service
public class UserActionService {
	@Autowired
	private UserActionMapper mapper;
	
	@Autowired
	private CustomActionRepositoryImpl customRepo;
	
	@Autowired
	private TransactionTemplate internalTransactionTemplate;
	
	public UserAction save(UserAction a) {
		return internalTransactionTemplate.execute(status -> {
			mapper.insert(a);
			return a;
		});
	}
	
	/**
	 * Retruns a page of results of user actions that match the given input request.
	 * @param request a request containing filtering parameters for user actions
	 * @return a page of results matching the input request
	 */
	public PaginatedResult<UserAction> findActions(LogsSearchRequest request) {
		PageRequest page = request.toPageRequest();
		
		long count = customRepo.countActions(request);
		List<UserAction> actions = customRepo.findActions(request);
		int maxPage = (int)(Math.ceil ((double)count / page.getPageSize()));
		
		return new PaginatedResult<>(
			new PaginationInfo(page.getPageNumber() + 1, maxPage, page.getPageSize(), count, null, request),
			actions
		);
	}
	
}
