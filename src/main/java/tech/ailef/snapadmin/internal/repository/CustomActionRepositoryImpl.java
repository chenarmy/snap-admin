/* 
 * SnapAdmin - An automatically generated CRUD admin UI for Spring Boot apps
 * Copyright (C) 2023 Ailef (http://ailef.tech)
 * 
 */

package tech.ailef.snapadmin.internal.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import tech.ailef.snapadmin.external.dto.LogsSearchRequest;
import tech.ailef.snapadmin.internal.model.UserAction;

/**
 * A repository that provides custom queries for UserActions using JDBC
 */
@Component
public class CustomActionRepositoryImpl implements CustomActionRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * Finds the UserAction that match the input search request.
     */
    @Override
    public List<UserAction> findActions(LogsSearchRequest request) {
        String table = request.getTable();
        String actionType = request.getActionType();
        String username = request.getUsername();
        String itemId = request.getItemId();
        
        StringBuilder sql = new StringBuilder("SELECT * FROM user_action WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (table != null && !table.isBlank()) {
            sql.append(" AND on_table = ?");
            params.add(table);
        }
        if (actionType != null && !actionType.isBlank()) {
            sql.append(" AND action_type = ?");
            params.add(actionType);
        }
        if (itemId != null && !itemId.isBlank()) {
            sql.append(" AND primary_key = ?");
            params.add(itemId);
        }
        if (username != null && !username.isBlank()) {
            sql.append(" AND username = ?");
            params.add(username);
        }
        
        // Add sorting
        if (request.getSortKey() != null) {
            sql.append(" ORDER BY ").append(request.getSortKey());
            if ("DESC".equalsIgnoreCase(request.getSortOrder())) {
                sql.append(" DESC");
            } else {
                sql.append(" ASC");
            }
        }
        
        // Add pagination
        int pageSize = request.getPageSize();
        int offset = (request.getPage() - 1) * pageSize;
        sql.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(offset);
        
        RowMapper<UserAction> rowMapper = (rs, rowNum) -> {
            UserAction action = new UserAction();
            action.setId(rs.getInt("id"));
            action.setCreatedAt(rs.getTimestamp("created_at") != null ? 
                rs.getTimestamp("created_at").toLocalDateTime() : null);
            action.setSql(rs.getString("sql"));
            action.setJavaClass(rs.getString("java_class"));
            action.setOnTable(rs.getString("on_table"));
            action.setPrimaryKey(rs.getString("primary_key"));
            action.setActionType(rs.getString("action_type"));
            action.setUsername(rs.getString("username"));
            return action;
        };
        
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }
    
    /**
     * Returns the count that match the filtering parameters, used for pagination.
     */
    @Override
    public long countActions(LogsSearchRequest request) {
        String table = request.getTable();
        String actionType = request.getActionType();
        String username = request.getUsername();
        String itemId = request.getItemId();
        
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM user_action WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (table != null && !table.isBlank()) {
            sql.append(" AND on_table = ?");
            params.add(table);
        }
        if (actionType != null && !actionType.isBlank()) {
            sql.append(" AND action_type = ?");
            params.add(actionType);
        }
        if (itemId != null && !itemId.isBlank()) {
            sql.append(" AND primary_key = ?");
            params.add(itemId);
        }
        if (username != null && !username.isBlank()) {
            sql.append(" AND username = ?");
            params.add(username);
        }
        
        Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return count != null ? count : 0;
    }

}
