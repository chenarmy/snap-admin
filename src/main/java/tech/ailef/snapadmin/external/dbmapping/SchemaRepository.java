package tech.ailef.snapadmin.external.dbmapping;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import tech.ailef.snapadmin.external.dbmapping.fields.DbField;
import tech.ailef.snapadmin.external.dbmapping.query.DbQueryResult;
import tech.ailef.snapadmin.external.dbmapping.query.DbQueryResultRow;
import tech.ailef.snapadmin.external.dto.PaginatedResult;
import tech.ailef.snapadmin.external.dto.QueryFilter;

/**
 * Abstraction for schema-level CRUD operations.
 */
public interface SchemaRepository {

    /**
     * Find all objects with pagination and sorting
     */
    PaginatedResult<DbObject> findAll(DbObjectSchema schema, int page, int pageSize, String sortKey, String sortOrder);

    /**
     * Find an object by its primary key
     */
    Optional<DbObject> findById(DbObjectSchema schema, Object id);

    /**
     * Count all objects
     */
    long count(DbObjectSchema schema);

    /**
     * Count objects matching the search query and filters
     */
    long count(DbObjectSchema schema, String query, Set<QueryFilter> queryFilters);

    /**
     * Search objects with pagination, sorting and filters
     */
    PaginatedResult<DbObject> search(DbObjectSchema schema, String query, int page, int pageSize,
            String sortKey, String sortOrder, Set<QueryFilter> queryFilters);

    /**
     * Search objects without pagination
     */
    List<DbObject> search(DbObjectSchema schema, String query, Set<QueryFilter> queryFilters);

    /**
     * Create a new object and return its primary key
     */
    Object create(DbObjectSchema schema, Map<String, String> values, Map<String, MultipartFile> files, String primaryKey);

    /**
     * Update an existing object
     */
    void update(DbObjectSchema schema, Map<String, String> params, Map<String, MultipartFile> files);

    /**
     * Delete an object by its primary key
     */
    void deleteById(DbObjectSchema schema, String id);

    /**
     * Find all objects (no pagination)
     */
    List<Object> findAllList(DbObjectSchema schema);

    /**
     * Find an object by ID (returns raw object)
     */
    Optional<Object> findRawById(DbObjectSchema schema, Object id);

    /**
     * Count the results of a custom query
     */
    long countQueryResults(String query);

    /**
     * Run a custom query with pagination
     */
    List<DbQueryResultRow> runQuery(DbObjectSchema schema, String query, int page, int pageSize);

    /**
     * Create a new object with raw values
     */
    Object createRaw(DbObjectSchema schema, Map<String, Object> values, String primaryKey);

    /**
     * Update an existing object with raw values
     */
    void updateRaw(DbObjectSchema schema, Object id, Map<String, Object> updates);

    /**
     * Delete an object by ID (raw version)
     */
    void deleteByIdRaw(DbObjectSchema schema, Object id);

    /**
     * Check if an object can be safely deleted
     */
    boolean canDelete(DbObjectSchema schema, Object id);

    /**
     * Get orphaned objects if this object is deleted
     */
    List<String> getOrphanedObjects(DbObjectSchema schema, Object id);

    /**
     * Get linked objects
     */
    List<String> getLinkedObjects(DbObjectSchema schema, Object id);

    /**
     * Get referencing objects
     */
    List<String> getReferencingObjects(DbObjectSchema schema, Object id);

    /**
     * Get available filters
     */
    Set<QueryFilter> getAvailableFilters(DbObjectSchema schema);

    /**
     * Get available sort fields
     */
    List<String> getAvailableSortFields(DbObjectSchema schema);

    /**
     * Get available display fields
     */
    List<String> getAvailableDisplayFields(DbObjectSchema schema);

    /**
     * Get available search fields
     */
    List<String> getAvailableSearchFields(DbObjectSchema schema);

    /**
     * Get available filter fields
     */
    List<String> getAvailableFilterFields(DbObjectSchema schema);

    /**
     * Get available create fields
     */
    List<String> getAvailableCreateFields(DbObjectSchema schema);

    /**
     * Get available edit fields
     */
    List<String> getAvailableEditFields(DbObjectSchema schema);

    /**
     * Get available export fields
     */
    List<String> getAvailableExportFields(DbObjectSchema schema);

    /**
     * Get available relationship fields
     */
    List<String> getAvailableRelationshipFields(DbObjectSchema schema);

    /**
     * Get available computed columns
     */
    List<String> getAvailableComputedColumns(DbObjectSchema schema);

    /**
     * Get available actions
     */
    List<String> getAvailableActions(DbObjectSchema schema);

    /**
     * Get available batch actions
     */
    List<String> getAvailableBatchActions(DbObjectSchema schema);
}
