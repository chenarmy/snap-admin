/* 
 * SnapAdmin - An automatically generated CRUD admin UI for Spring Boot apps
 * Copyright (C) 2023 Ailef (http://ailef.tech)
 */

package tech.ailef.snapadmin.external.dbmapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import tech.ailef.snapadmin.external.SnapAdmin;
import tech.ailef.snapadmin.external.annotations.ReadOnly;
import tech.ailef.snapadmin.external.dbmapping.query.DbQueryOutputField;
import tech.ailef.snapadmin.external.dbmapping.query.DbQueryResult;
import tech.ailef.snapadmin.external.dbmapping.query.DbQueryResultRow;
import tech.ailef.snapadmin.external.dto.PaginatedResult;
import tech.ailef.snapadmin.external.dto.QueryFilter;
import tech.ailef.snapadmin.external.exceptions.SnapAdminException;

/**
 * Implements the basic CRUD operations (and some more)
 */
@Component
public class SnapAdminRepository {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private SnapAdmin snapAdmin;
	
	public SnapAdminRepository() {
	}
	
	private SchemaRepository repository(DbObjectSchema schema) {
		return schema.getRepository();
	}

	/**
	 * Find an object by ID
	 * @param schema	the schema where to look
	 * @param id	the primary key value
	 * @return	an optional with the object with the specified primary key value
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Optional<DbObject> findById(DbObjectSchema schema, Object id) {
		return repository(schema).findById(schema, id);
	}

	public long count(DbObjectSchema schema) {
		return repository(schema).count(schema);
	}
	
	/**
	 * Counts the elements that match the fuzzy search
	 * @param schema
	 * @param query
	 * @return
	 */
	public long count(DbObjectSchema schema, String query, Set<QueryFilter> queryFilters) {
		return repository(schema).count(schema, query, queryFilters);
	}

	public List<DbObject> search(DbObjectSchema schema, String query, Set<QueryFilter> queryFilters) {
		return repository(schema).search(schema, query, queryFilters);
	}
	
	/**
	 * Find all the objects in the schema. Only returns a single page of
	 * results based on the input parameters.
	 * @param schema
	 * @param page
	 * @param pageSize
	 * @param sortKey
	 * @param sortOrder
	 * @return
	 */
	public PaginatedResult<DbObject> findAll(DbObjectSchema schema, int page, int pageSize, String sortKey, String sortOrder) {
		return repository(schema).findAll(schema, page, pageSize, sortKey, sortOrder);
	}
	
	/**
	 * Update an existing object with new values.
	 * We need to handle several edge cases in terms of how missing values
	 * are handled and also {@linkplain ReadOnly} fields. For this reason, we
	 * also need to call the validation manually.
	 * @param schema the schema where we need to update an item
	 * @param params the String-valued params coming from the HTML form
	 * @param files the file params coming from the HTML form
	 */
	@Transactional("internalTransactionManager")
	public void update(DbObjectSchema schema, Map<String, String> params, Map<String, MultipartFile> files) {
		DbObject obj = schema.buildObject(params, files);
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		Set<ConstraintViolation<Object>> violations = validator.validate(obj.getUnderlyingInstance());
		
		if (violations.size() > 0) {
			throw new ConstraintViolationException(violations);
		}
		
		repository(schema).update(schema, params, files);
	}
	
	/**
	 * Attaches multiple many to many relationships to an object, parsed from a multi-valued map.
	 * Note: MyBatis-Plus mode currently does not support auto-maintenance of join tables.
	 * @param schema	the entity class that owns this relationship
	 * @param id	the primary key of the entity where these relationships have to be attached to
	 * @param params	the multi-valued map containing the many-to-many relationships
	 */
	@Transactional("internalTransactionManager")
	public void attachManyToMany(DbObjectSchema schema, Object id, Map<String, List<String>> params) {
		// MyBatis-Plus mode currently does not support auto-maintenance of join tables.
		return;
	}

	/**
	 * Create a new object with the specific primary key and values,
	 * returns the primary key of the created object
	 * @param schema
	 * @param values
	 * @param primaryKey
	 */
	@Transactional("internalTransactionManager")
	public Object create(DbObjectSchema schema, Map<String, String> values, Map<String, MultipartFile> files, String primaryKey) {
		return repository(schema).create(schema, values, files, primaryKey);
	}
	
	/**
	 * Fuzzy search on primary key value and display name
	 * @param schema
	 * @param query
	 * @return
	 */
	public PaginatedResult<DbObject> search(DbObjectSchema schema, String query, int page, int pageSize, String sortKey, 
			String sortOrder, Set<QueryFilter> queryFilters) {
		return repository(schema).search(schema, query, page, pageSize, sortKey, sortOrder, queryFilters);
	}
	
	/**
	 * Delete an object by primary key.
	 * @param schema
	 * @param id
	 */
	@Transactional("internalTransactionManager")
	public void deleteById(DbObjectSchema schema, Object id) {
		repository(schema).deleteById(schema, id.toString());
	}
	
	/**
	 * Runs a custom user query.
	 * @param schema
	 * @param query
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public DbQueryResult runQuery(DbObjectSchema schema, String query, int page, int pageSize) {
		long count = repository(schema).countQueryResults(query);
		List<DbQueryResultRow> results = repository(schema).runQuery(schema, query, page, pageSize);

		List<DbQueryOutputField> fields = new ArrayList<>();
		if (results.size() > 0) {
			DbQueryResultRow firstRow = results.get(0);
			for (DbQueryOutputField key : firstRow.keySet()) {
				fields.add(key);
			}
		}

		return new DbQueryResult(count, results, fields);
	}
	
	/**
	 * Counts the results of a query.
	 * @param schema
	 * @param query
	 * @return
	 */
	public long countQueryResults(DbObjectSchema schema, String query) {
		return repository(schema).countQueryResults(query);
	}
	
	/**
	 * Create a new object with the specific primary key and values, returns the raw result.
	 * Used internally to create objects programmatically.
	 * @param schema
	 * @param values
	 * @param primaryKey
	 * @return
	 */
	@Transactional("internalTransactionManager")
	public Object createRaw(DbObjectSchema schema, Map<String, Object> values, String primaryKey) {
		return repository(schema).createRaw(schema, values, primaryKey);
	}
	
	/**
	 * Find an object by ID, returns the raw result.
	 * Used internally to retrieve objects programmatically.
	 * @param schema
	 * @param id
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Optional findRawById(DbObjectSchema schema, Object id) {
		return repository(schema).findRawById(schema, id);
	}
	
	/**
	 * Update an existing object with new values, returns the raw result.
	 * Used internally to update objects programmatically.
	 * @param schema
	 * @param id
	 * @param updates
	 */
	@Transactional("internalTransactionManager")
	public void updateRaw(DbObjectSchema schema, Object id, Map<String, Object> updates) {
		repository(schema).updateRaw(schema, id, updates);
	}
	
	/**
	 * Delete an object by primary key, returns the raw result.
	 * Used internally to delete objects programmatically.
	 * @param schema
	 * @param id
	 */
	@Transactional("internalTransactionManager")
	public void deleteByIdRaw(DbObjectSchema schema, Object id) {
		repository(schema).deleteByIdRaw(schema, id);
	}
	
	/**
	 * Checks if the given object can be safely deleted.
	 * @param schema
	 * @param id
	 * @return
	 */
	public boolean canDelete(DbObjectSchema schema, Object id) {
		return repository(schema).canDelete(schema, id);
	}
	
	/**
	 * Returns the list of objects that would become orphans if this object was deleted.
	 * @param schema
	 * @param id
	 * @return
	 */
	public List<String> getOrphanedObjects(DbObjectSchema schema, Object id) {
		return repository(schema).getOrphanedObjects(schema, id);
	}
	
	/**
	 * Returns the list of objects that this object links to.
	 * @param schema
	 * @param id
	 * @return
	 */
	public List<String> getLinkedObjects(DbObjectSchema schema, Object id) {
		return repository(schema).getLinkedObjects(schema, id);
	}
	
	/**
	 * Returns the list of objects that link to this object.
	 * @param schema
	 * @param id
	 * @return
	 */
	public List<String> getReferencingObjects(DbObjectSchema schema, Object id) {
		return repository(schema).getReferencingObjects(schema, id);
	}
	
	/**
	 * Returns the list of available query filters for the schema.
	 * @param schema
	 * @return
	 */
	public Set<QueryFilter> getAvailableFilters(DbObjectSchema schema) {
		return repository(schema).getAvailableFilters(schema);
	}
	
	/**
	 * Returns the list of available sort fields for the schema.
	 * @param schema
	 * @return
	 */
	public List<String> getAvailableSortFields(DbObjectSchema schema) {
		return repository(schema).getAvailableSortFields(schema);
	}
	
	/**
	 * Returns the list of available display fields for the schema.
	 * @param schema
	 * @return
	 */
	public List<String> getAvailableDisplayFields(DbObjectSchema schema) {
		return repository(schema).getAvailableDisplayFields(schema);
	}
	
	/**
	 * Returns the list of available search fields for the schema.
	 * @param schema
	 * @return
	 */
	public List<String> getAvailableSearchFields(DbObjectSchema schema) {
		return repository(schema).getAvailableSearchFields(schema);
	}
	
	/**
	 * Returns the list of available filter fields for the schema.
	 * @param schema
	 * @return
	 */
	public List<String> getAvailableFilterFields(DbObjectSchema schema) {
		return repository(schema).getAvailableFilterFields(schema);
	}
	
	/**
	 * Returns the list of available create fields for the schema.
	 * @param schema
	 * @return
	 */
	public List<String> getAvailableCreateFields(DbObjectSchema schema) {
		return repository(schema).getAvailableCreateFields(schema);
	}
	
	/**
	 * Returns the list of available edit fields for the schema.
	 * @param schema
	 * @return
	 */
	public List<String> getAvailableEditFields(DbObjectSchema schema) {
		return repository(schema).getAvailableEditFields(schema);
	}
	
	/**
	 * Returns the list of available export fields for the schema.
	 * @param schema
	 * @return
	 */
	public List<String> getAvailableExportFields(DbObjectSchema schema) {
		return repository(schema).getAvailableExportFields(schema);
	}
	
	/**
	 * Returns the list of available relationship fields for the schema.
	 * @param schema
	 * @return
	 */
	public List<String> getAvailableRelationshipFields(DbObjectSchema schema) {
		return repository(schema).getAvailableRelationshipFields(schema);
	}
	
	/**
	 * Returns the list of available computed columns for the schema.
	 * @param schema
	 * @return
	 */
	public List<String> getAvailableComputedColumns(DbObjectSchema schema) {
		return repository(schema).getAvailableComputedColumns(schema);
	}
	
	/**
	 * Returns the list of available actions for the schema.
	 * @param schema
	 * @return
	 */
	public List<String> getAvailableActions(DbObjectSchema schema) {
		return repository(schema).getAvailableActions(schema);
	}
	
	/**
	 * Returns the list of available batch actions for the schema.
	 * @param schema
	 * @return
	 */
	public List<String> getAvailableBatchActions(DbObjectSchema schema) {
		return repository(schema).getAvailableBatchActions(schema);
	}

	public List<DbObject> search(DbObjectSchema schema, String query) {
		return repository(schema).search(schema, query, Set.of());
	}

	/**
	 * Executes a raw SQL query and returns the result.
	 * Used for SQL console queries.
	 * @param query the SQL query to execute
	 * @return DbQueryResult containing the query results
	 */
	public tech.ailef.snapadmin.external.dbmapping.query.DbQueryResult executeQuery(String query) {
		String executableQuery = stripSqlConsoleComments(query);
		if (executableQuery.isBlank()) {
			return new DbQueryResult(0, new ArrayList<>(), new ArrayList<>());
		}

		// Run the query using JDBC template
		List<DbQueryResultRow> rows = jdbcTemplate.query(executableQuery, (rs, rowNum) -> {
			try {
				Map<DbQueryOutputField, Object> values = new HashMap<>();
				int columnCount = rs.getMetaData().getColumnCount();

				for (int i = 1; i <= columnCount; i++) {
					String columnName = rs.getMetaData().getColumnName(i);
					Object value = rs.getObject(i);
					// For raw queries, we don't have a schema, so we create a DbQueryOutputField with null table
					DbQueryOutputField field = new DbQueryOutputField(columnName, null, snapAdmin);
					values.put(field, value);
				}

				return new DbQueryResultRow(values, executableQuery);
			} catch (Exception e) {
				throw new RuntimeException("Error mapping query result row", e);
			}
		});

		// Count query results for pagination
		long count = 0;
		try {
			String countQuery = "SELECT COUNT(*) FROM (" + executableQuery + ") AS count_query";
			Long result = jdbcTemplate.queryForObject(countQuery, Long.class);
			count = result != null ? result : 0;
		} catch (Exception e) {
			// If count query fails, use the size of the results
			count = rows.size();
		}

		List<DbQueryOutputField> fields = new ArrayList<>();
		if (!rows.isEmpty()) {
			fields.addAll(rows.get(0).keySet());
		}

		return new DbQueryResult(count, rows, fields);
	}

	private String stripSqlConsoleComments(String query) {
		if (query == null) {
			return "";
		}

		return query.lines()
			.map(String::trim)
			.filter(line -> !line.isBlank())
			.filter(line -> !line.startsWith("--"))
			.reduce((left, right) -> left + "\n" + right)
			.orElse("");
	}
}
