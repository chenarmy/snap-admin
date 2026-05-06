package tech.ailef.snapadmin.external.dbmapping;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.multipart.MultipartFile;

import tech.ailef.snapadmin.external.annotations.ReadOnly;
import tech.ailef.snapadmin.external.dbmapping.fields.DbField;
import tech.ailef.snapadmin.external.dbmapping.fields.DbFieldType;
import tech.ailef.snapadmin.external.dbmapping.fields.StringFieldType;
import tech.ailef.snapadmin.external.dbmapping.fields.TextFieldType;
import tech.ailef.snapadmin.external.dto.CompareOperator;
import tech.ailef.snapadmin.external.dto.PaginatedResult;
import tech.ailef.snapadmin.external.dto.PaginationInfo;
import tech.ailef.snapadmin.external.dto.QueryFilter;
import tech.ailef.snapadmin.external.exceptions.InvalidPageException;
import tech.ailef.snapadmin.external.exceptions.SnapAdminException;
import tech.ailef.snapadmin.external.dbmapping.query.DbQueryOutputField;
import tech.ailef.snapadmin.external.dbmapping.query.DbQueryResultRow;

/**
 * JDBC-based implementation of SchemaRepository for MyBatis-Plus entities.
 * Uses JdbcTemplate for all CRUD operations.
 */
public class JdbcSchemaRepository implements SchemaRepository {

    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedTemplate;
    private DbObjectSchema schema;

    public JdbcSchemaRepository(DbObjectSchema schema, JdbcTemplate jdbcTemplate) {
        this.schema = schema;
        this.jdbcTemplate = jdbcTemplate;
        this.namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public PaginatedResult<DbObject> findAll(DbObjectSchema schema, int page, int pageSize, String sortKey, String sortOrder) {
        long maxElement = count(schema);
        int maxPage = (int)(Math.ceil((double)maxElement / pageSize));

        if (page <= 0) page = 1;
        if (page > maxPage && maxPage != 0) {
            throw new InvalidPageException();
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM " + quoteIdentifier(schema.getTableName()));

        if (sortKey != null) {
            sql.append(" ORDER BY ").append(quoteIdentifier(sortKey));
            if ("DESC".equalsIgnoreCase(sortOrder)) {
                sql.append(" DESC");
            } else {
                sql.append(" ASC");
            }
        }

        sql.append(" LIMIT ").append(pageSize);
        sql.append(" OFFSET ").append((page - 1) * pageSize);

        List<Object> results = jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            try {
                Object instance = schema.getJavaClass().getConstructor().newInstance();
                for (DbField field : schema.getSortedFields()) {
                    try {
                        Object value = rs.getObject(field.getName());
                        if (value != null) {
                            Method setter = findSetter(instance, field.getJavaName());
                            if (setter != null) {
                                Class<?> paramType = setter.getParameterTypes()[0];
                                value = convertValue(value, paramType);
                                setter.invoke(instance, value);
                            }
                        }
                    } catch (Exception e) {
                        // Skip fields that can't be mapped
                    }
                }
                return instance;
            } catch (Exception e) {
                throw new SnapAdminException("Error mapping row to " + schema.getClassName(), e);
            }
        });

        List<DbObject> dbObjects = results.stream()
            .map(o -> new DbObject(o, schema))
            .collect(Collectors.toList());

        return new PaginatedResult<>(
            new PaginationInfo(page, maxPage, pageSize, maxElement, null, null),
            dbObjects
        );
    }

    @Override
    public Optional<DbObject> findById(DbObjectSchema schema, Object id) {
        String pkColumn = schema.getPrimaryKey().getName();
        String sql = "SELECT * FROM " + quoteIdentifier(schema.getTableName())
            + " WHERE " + quoteIdentifier(pkColumn) + " = ?";

        List<Object> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            try {
                Object instance = schema.getJavaClass().getConstructor().newInstance();
                for (DbField field : schema.getSortedFields()) {
                    try {
                        Object value = rs.getObject(field.getName());
                        if (value != null) {
                            Method setter = findSetter(instance, field.getJavaName());
                            if (setter != null) {
                                Class<?> paramType = setter.getParameterTypes()[0];
                                value = convertValue(value, paramType);
                                setter.invoke(instance, value);
                            }
                        }
                    } catch (Exception e) {
                        // Skip fields that can't be mapped
                    }
                }
                return instance;
            } catch (Exception e) {
                throw new SnapAdminException("Error mapping row to " + schema.getClassName(), e);
            }
        }, id);

        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new DbObject(results.get(0), schema));
    }

    @Override
    public long count(DbObjectSchema schema) {
        String sql = "SELECT COUNT(*) FROM " + quoteIdentifier(schema.getTableName());
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0;
    }

    @Override
    public long count(DbObjectSchema schema, String query, Set<QueryFilter> queryFilters) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM " + quoteIdentifier(schema.getTableName()) + " WHERE 1=1");
        MapSqlParameterSource params = new MapSqlParameterSource();

        sql.append(buildWhereClause(query, queryFilters, params));

        Long count = namedTemplate.queryForObject(sql.toString(), params, Long.class);
        return count != null ? count : 0;
    }

    @Override
    public PaginatedResult<DbObject> search(DbObjectSchema schema, String query, int page, int pageSize,
            String sortKey, String sortOrder, Set<QueryFilter> filters) {
        long maxElement = count(schema, query, filters);
        int maxPage = (int)(Math.ceil((double)maxElement / pageSize));

        if (page <= 0) page = 1;
        if (page > maxPage && maxPage != 0) {
            throw new InvalidPageException();
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM " + quoteIdentifier(schema.getTableName()) + " WHERE 1=1");
        MapSqlParameterSource params = new MapSqlParameterSource();

        sql.append(buildWhereClause(query, filters, params));

        if (sortKey != null) {
            sql.append(" ORDER BY ").append(quoteIdentifier(sortKey));
            if ("DESC".equalsIgnoreCase(sortOrder)) {
                sql.append(" DESC");
            } else {
                sql.append(" ASC");
            }
        }

        sql.append(" LIMIT :__limit OFFSET :__offset");
        params.addValue("__limit", pageSize);
        params.addValue("__offset", (page - 1) * pageSize);

        List<Object> results = namedTemplate.query(sql.toString(), params, (rs, rowNum) -> {
            try {
                Object instance = schema.getJavaClass().getConstructor().newInstance();
                for (DbField field : schema.getSortedFields()) {
                    try {
                        Object value = rs.getObject(field.getName());
                        if (value != null) {
                            Method setter = findSetter(instance, field.getJavaName());
                            if (setter != null) {
                                Class<?> paramType = setter.getParameterTypes()[0];
                                value = convertValue(value, paramType);
                                setter.invoke(instance, value);
                            }
                        }
                    } catch (Exception e) {
                        // Skip fields that can't be mapped
                    }
                }
                return instance;
            } catch (Exception e) {
                throw new SnapAdminException("Error mapping row to " + schema.getClassName(), e);
            }
        });

        List<DbObject> dbObjects = results.stream()
            .map(o -> new DbObject(o, schema))
            .collect(Collectors.toList());

        return new PaginatedResult<>(
            new PaginationInfo(page, maxPage, pageSize, maxElement, query, null),
            dbObjects
        );
    }

    @Override
    public List<DbObject> search(DbObjectSchema schema, String query, Set<QueryFilter> filters) {
        return search(schema, query, 1, Integer.MAX_VALUE, null, null, filters).getResults();
    }

    @Override
    public Object create(DbObjectSchema schema, Map<String, String> values, Map<String, MultipartFile> files, String primaryKey) {
        DbObject obj = schema.buildObject(values, files);
        Object instance = obj.getUnderlyingInstance();

        List<String> columns = new ArrayList<>();
        List<String> valuePlaceholders = new ArrayList<>();
        List<Object> paramValues = new ArrayList<>();

        for (DbField field : schema.getSortedFields()) {
            if (field.isGeneratedValue() && !field.isPrimaryKey()) continue;

            String columnName = field.getName();
            Object fieldValue = getFieldValue(instance, field);

            // Skip null auto-generated primary keys
            if (field.isPrimaryKey() && field.isGeneratedValue() && fieldValue == null) continue;

            columns.add(quoteIdentifier(columnName));
            valuePlaceholders.add("?");
            paramValues.add(fieldValue);
        }

        String sql = "INSERT INTO " + quoteIdentifier(schema.getTableName())
            + " (" + String.join(", ", columns) + ")"
            + " VALUES (" + String.join(", ", valuePlaceholders) + ")";

        if (schema.getPrimaryKey().isGeneratedValue()) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                var ps = con.prepareStatement(sql, new String[]{schema.getPrimaryKey().getName()});
                for (int i = 0; i < paramValues.size(); i++) {
                    ps.setObject(i + 1, paramValues.get(i));
                }
                return ps;
            }, keyHolder);

            if (keyHolder.getKey() != null) {
                return keyHolder.getKey();
            }
            return null;
        } else {
            jdbcTemplate.update(sql, paramValues.toArray());
            return getFieldValue(instance, schema.getPrimaryKey());
        }
    }

    @Override
    public void update(DbObjectSchema schema, Map<String, String> params, Map<String, MultipartFile> files) {
        List<String> setClauses = new ArrayList<>();
        List<Object> paramValues = new ArrayList<>();

        for (DbField field : schema.getSortedFields()) {
            if (field.isPrimaryKey()) continue;
            if (field.isReadOnly()) continue;

            boolean keepValue = params.getOrDefault("__keep_" + field.getName(), "off").equals("on");
            if (keepValue) continue;

            String stringValue = params.get(field.getName());
            Object value = null;
            if (stringValue != null && stringValue.isBlank()) stringValue = null;
            if (stringValue != null) {
                value = field.getType().parseValue(stringValue);
            } else {
                try {
                    MultipartFile file = files.get(field.getName());
                    if (file != null) {
                        if (file.isEmpty()) value = null;
                        else value = file.getBytes();
                    }
                } catch (IOException e) {
                    throw new SnapAdminException(e);
                }
            }

            setClauses.add(quoteIdentifier(field.getName()) + " = ?");
            paramValues.add(value);
        }

        if (setClauses.isEmpty()) return;

        String pkColumn = schema.getPrimaryKey().getName();
        Object pkValue = schema.getPrimaryKey().getType().parseValue(params.get(pkColumn));

        String sql = "UPDATE " + quoteIdentifier(schema.getTableName())
            + " SET " + String.join(", ", setClauses)
            + " WHERE " + quoteIdentifier(pkColumn) + " = ?";

        paramValues.add(pkValue);
        jdbcTemplate.update(sql, paramValues.toArray());
    }

    @Override
    public void deleteById(DbObjectSchema schema, String id) {
        String pkColumn = schema.getPrimaryKey().getName();
        Object pkValue = schema.getPrimaryKey().getType().parseValue(id);
        String sql = "DELETE FROM " + quoteIdentifier(schema.getTableName())
            + " WHERE " + quoteIdentifier(pkColumn) + " = ?";
        jdbcTemplate.update(sql, pkValue);
    }

    @Override
    public List<Object> findAllList(DbObjectSchema schema) {
        String sql = "SELECT * FROM " + quoteIdentifier(schema.getTableName());
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            try {
                Object instance = schema.getJavaClass().getConstructor().newInstance();
                for (DbField field : schema.getSortedFields()) {
                    try {
                        Object value = rs.getObject(field.getName());
                        if (value != null) {
                            Method setter = findSetter(instance, field.getJavaName());
                            if (setter != null) {
                                Class<?> paramType = setter.getParameterTypes()[0];
                                value = convertValue(value, paramType);
                                setter.invoke(instance, value);
                            }
                        }
                    } catch (Exception e) {
                        // Skip fields that can't be mapped
                    }
                }
                return instance;
            } catch (Exception e) {
                throw new SnapAdminException("Error mapping row to " + schema.getClassName(), e);
            }
        });
    }

    @Override
    public Optional<Object> findRawById(DbObjectSchema schema, Object id) {
        Optional<DbObject> dbObj = findById(schema, id);
        return dbObj.map(DbObject::getUnderlyingInstance);
    }

    // ====================== Helper methods ======================

    private String buildWhereClause(String query, Set<QueryFilter> queryFilters, MapSqlParameterSource params) {
        StringBuilder where = new StringBuilder();

        // Fuzzy search on string/text fields
        if (query != null && !query.isBlank()) {
            List<DbField> stringFields = schema.getSortedFields().stream()
                .filter(f -> f.getType() instanceof StringFieldType || f.getType() instanceof TextFieldType)
                .collect(Collectors.toList());

            List<String> likeClauses = new ArrayList<>();
            for (int i = 0; i < stringFields.size(); i++) {
                String paramName = "__q_" + i;
                likeClauses.add("LOWER(" + quoteIdentifier(stringFields.get(i).getName()) + ") LIKE :" + paramName);
                params.addValue(paramName, "%" + query.toLowerCase() + "%");
            }

            // Also search on primary key
            likeClauses.add("CAST(" + quoteIdentifier(schema.getPrimaryKey().getName()) + " AS VARCHAR) LIKE :__q_pk");
            params.addValue("__q_pk", "%" + query + "%");

            if (!likeClauses.isEmpty()) {
                where.append(" AND (").append(String.join(" OR ", likeClauses)).append(")");
            }
        }

        // Query filters
        if (queryFilters != null) {
            int filterIdx = 0;
            for (QueryFilter filter : queryFilters) {
                CompareOperator op = filter.getOp();
                DbField dbField = filter.getField();
                String colName = quoteIdentifier(dbField.getName());
                String paramName = "__f_" + filterIdx;
                String v = filter.getValue();

                Object value = null;
                if (!v.isBlank()) {
                    try {
                        value = dbField.getType().parseValue(v);
                    } catch (Exception e) {
                        throw new SnapAdminException("Invalid value `" + v + "` specified for field `" + dbField.getName() + "`");
                    }
                }

                if (op == CompareOperator.STRING_EQ) {
                    if (value == null) {
                        where.append(" AND ").append(colName).append(" IS NULL");
                    } else {
                        where.append(" AND LOWER(").append(colName).append(") = :").append(paramName);
                        params.addValue(paramName, value.toString().toLowerCase());
                    }
                } else if (op == CompareOperator.CONTAINS) {
                    if (value != null) {
                        where.append(" AND LOWER(").append(colName).append(") LIKE :").append(paramName);
                        params.addValue(paramName, "%" + value.toString().toLowerCase() + "%");
                    }
                } else if (op == CompareOperator.EQ) {
                    where.append(" AND ").append(colName).append(" = :").append(paramName);
                    params.addValue(paramName, value);
                } else if (op == CompareOperator.GT) {
                    if (value != null) {
                        where.append(" AND ").append(colName).append(" > :").append(paramName);
                        params.addValue(paramName, value);
                    }
                } else if (op == CompareOperator.LT) {
                    if (value != null) {
                        where.append(" AND ").append(colName).append(" < :").append(paramName);
                        params.addValue(paramName, value);
                    }
                } else if (op == CompareOperator.AFTER) {
                    if (value != null) {
                        where.append(" AND ").append(colName).append(" > :").append(paramName);
                        params.addValue(paramName, value);
                    }
                } else if (op == CompareOperator.BEFORE) {
                    if (value != null) {
                        where.append(" AND ").append(colName).append(" < :").append(paramName);
                        params.addValue(paramName, value);
                    }
                }
                filterIdx++;
            }
        }

        return where.toString();
    }

    private String quoteIdentifier(String identifier) {
        return "`" + identifier + "`";
    }

    private Method findSetter(Object instance, String fieldName) {
        String capitalize = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        for (Method m : instance.getClass().getMethods()) {
            if (m.getName().equals("set" + capitalize))
                return m;
        }
        return null;
    }

    private Object getFieldValue(Object instance, DbField field) {
        String capitalize = Character.toUpperCase(field.getJavaName().charAt(0)) + field.getJavaName().substring(1);
        String prefix = "get";
        try {
            for (Method m : instance.getClass().getMethods()) {
                if (m.getName().equals(prefix + capitalize)) {
                    return m.invoke(instance);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
        return null;
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;
        if (targetType.isAssignableFrom(value.getClass())) return value;

        // Handle common conversions
        if (targetType == Long.class || targetType == long.class) {
            if (value instanceof Number) return ((Number) value).longValue();
            return Long.parseLong(value.toString());
        }
        if (targetType == Integer.class || targetType == int.class) {
            if (value instanceof Number) return ((Number) value).intValue();
            return Integer.parseInt(value.toString());
        }
        if (targetType == Short.class || targetType == short.class) {
            if (value instanceof Number) return ((Number) value).shortValue();
            return Short.parseShort(value.toString());
        }
        if (targetType == Byte.class || targetType == byte.class) {
            if (value instanceof Number) return ((Number) value).byteValue();
            return Byte.parseByte(value.toString());
        }
        if (targetType == Double.class || targetType == double.class) {
            if (value instanceof Number) return ((Number) value).doubleValue();
            return Double.parseDouble(value.toString());
        }
        if (targetType == Float.class || targetType == float.class) {
            if (value instanceof Number) return ((Number) value).floatValue();
            return Float.parseFloat(value.toString());
        }
        if (targetType == BigDecimal.class) {
            if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
            return new BigDecimal(value.toString());
        }
        if (targetType == BigInteger.class) {
            if (value instanceof Number) return BigInteger.valueOf(((Number) value).longValue());
            return new BigInteger(value.toString());
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            if (value instanceof Number) return ((Number) value).intValue() != 0;
            return Boolean.parseBoolean(value.toString());
        }
        if (targetType == UUID.class && value instanceof String) {
            return UUID.fromString((String) value);
        }
        if (targetType == LocalDateTime.class && value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        if (targetType == LocalDate.class && value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate();
        }
        if (targetType == Instant.class && value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toInstant();
        }
        if (targetType == OffsetDateTime.class && value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime().atOffset(java.time.ZoneOffset.UTC);
        }
        if (targetType.isEnum()) {
            @SuppressWarnings("unchecked")
            Class<? extends Enum> enumClass = (Class<? extends Enum>) targetType;
            try {
                return Enum.valueOf(enumClass, value.toString());
            } catch (IllegalArgumentException e) {
                // Try matching by ordinal
                if (value instanceof Number) {
                    int ordinal = ((Number) value).intValue();
                    Enum[] constants = enumClass.getEnumConstants();
                    if (ordinal >= 0 && ordinal < constants.length) {
                        return constants[ordinal];
                    }
                }
                throw e;
            }
        }

        return value;
    }

    @Override
    public long countQueryResults(String query) {
        String countQuery = "SELECT COUNT(*) FROM (" + query + ") AS count_query";
        Long count = jdbcTemplate.queryForObject(countQuery, Long.class);
        return count != null ? count : 0;
    }

    @Override
    public List<DbQueryResultRow> runQuery(DbObjectSchema schema, String query, int page, int pageSize) {
        String paginatedQuery = query + " LIMIT " + pageSize + " OFFSET " + ((page - 1) * pageSize);

        return jdbcTemplate.query(paginatedQuery, (rs, rowNum) -> {
            try {
                Map<DbQueryOutputField, Object> values = new HashMap<>();
                int columnCount = rs.getMetaData().getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    Object value = rs.getObject(i);
                    DbQueryOutputField field = new DbQueryOutputField(columnName, schema.getTableName(), schema.getSnapAdmin());
                    values.put(field, value);
                }

                return new DbQueryResultRow(values, query);
            } catch (Exception e) {
                throw new SnapAdminException("Error mapping query result row", e);
            }
        });
    }

    @Override
    public Object createRaw(DbObjectSchema schema, Map<String, Object> values, String primaryKey) {
        List<String> columns = new ArrayList<>();
        List<String> valuePlaceholders = new ArrayList<>();
        List<Object> paramValues = new ArrayList<>();

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            columns.add(quoteIdentifier(entry.getKey()));
            valuePlaceholders.add("?");
            paramValues.add(entry.getValue());
        }

        String sql = "INSERT INTO " + quoteIdentifier(schema.getTableName())
            + " (" + String.join(", ", columns) + ")"
            + " VALUES (" + String.join(", ", valuePlaceholders) + ")";

        if (schema.getPrimaryKey().isGeneratedValue()) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                var ps = con.prepareStatement(sql, new String[]{schema.getPrimaryKey().getName()});
                for (int i = 0; i < paramValues.size(); i++) {
                    ps.setObject(i + 1, paramValues.get(i));
                }
                return ps;
            }, keyHolder);

            if (keyHolder.getKey() != null) {
                return keyHolder.getKey();
            }
            return null;
        } else {
            jdbcTemplate.update(sql, paramValues.toArray());
            return values.get(schema.getPrimaryKey().getName());
        }
    }

    @Override
    public void updateRaw(DbObjectSchema schema, Object id, Map<String, Object> updates) {
        List<String> setClauses = new ArrayList<>();
        List<Object> paramValues = new ArrayList<>();

        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            setClauses.add(quoteIdentifier(entry.getKey()) + " = ?");
            paramValues.add(entry.getValue());
        }

        if (setClauses.isEmpty()) return;

        String pkColumn = schema.getPrimaryKey().getName();
        String sql = "UPDATE " + quoteIdentifier(schema.getTableName())
            + " SET " + String.join(", ", setClauses)
            + " WHERE " + quoteIdentifier(pkColumn) + " = ?";

        paramValues.add(id);
        jdbcTemplate.update(sql, paramValues.toArray());
    }

    @Override
    public void deleteByIdRaw(DbObjectSchema schema, Object id) {
        String pkColumn = schema.getPrimaryKey().getName();
        String sql = "DELETE FROM " + quoteIdentifier(schema.getTableName())
            + " WHERE " + quoteIdentifier(pkColumn) + " = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean canDelete(DbObjectSchema schema, Object id) {
        // JDBC mode: assume can delete (no foreign key constraint checking)
        return true;
    }

    @Override
    public List<String> getOrphanedObjects(DbObjectSchema schema, Object id) {
        // JDBC mode: return empty list (no relationship tracking)
        return new ArrayList<>();
    }

    @Override
    public List<String> getLinkedObjects(DbObjectSchema schema, Object id) {
        // JDBC mode: return empty list (no relationship tracking)
        return new ArrayList<>();
    }

    @Override
    public List<String> getReferencingObjects(DbObjectSchema schema, Object id) {
        // JDBC mode: return empty list (no relationship tracking)
        return new ArrayList<>();
    }

    @Override
    public Set<QueryFilter> getAvailableFilters(DbObjectSchema schema) {
        // Return empty set - filters are computed from fields
        return new HashSet<>();
    }

    @Override
    public List<String> getAvailableSortFields(DbObjectSchema schema) {
        return schema.getSortedFields().stream()
            .filter(f -> !f.isBinary())
            .map(DbField::getName)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getAvailableDisplayFields(DbObjectSchema schema) {
        return schema.getSortedFields().stream()
            .filter(f -> !f.isPrimaryKey() && !f.isBinary())
            .map(DbField::getName)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getAvailableSearchFields(DbObjectSchema schema) {
        return schema.getSortedFields().stream()
            .filter(f -> f.getType() instanceof tech.ailef.snapadmin.external.dbmapping.fields.StringFieldType
                || f.getType() instanceof tech.ailef.snapadmin.external.dbmapping.fields.TextFieldType)
            .map(DbField::getName)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getAvailableFilterFields(DbObjectSchema schema) {
        return schema.getSortedFields().stream()
            .filter(f -> f.isFilterable())
            .map(DbField::getName)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getAvailableCreateFields(DbObjectSchema schema) {
        return schema.getSortedFields().stream()
            .filter(f -> !f.isPrimaryKey() || !f.isGeneratedValue())
            .map(DbField::getName)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getAvailableEditFields(DbObjectSchema schema) {
        return schema.getSortedFields().stream()
            .filter(f -> !f.isReadOnly())
            .map(DbField::getName)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getAvailableExportFields(DbObjectSchema schema) {
        return schema.getSortedFields().stream()
            .filter(DbField::isExportable)
            .map(DbField::getName)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getAvailableRelationshipFields(DbObjectSchema schema) {
        // JDBC mode: no relationship annotations, return empty list
        return new ArrayList<>();
    }

    @Override
    public List<String> getAvailableComputedColumns(DbObjectSchema schema) {
        return schema.getComputedColumnNames();
    }

    @Override
    public List<String> getAvailableActions(DbObjectSchema schema) {
        // Return empty list - actions are not implemented in JDBC mode
        return new ArrayList<>();
    }

    @Override
    public List<String> getAvailableBatchActions(DbObjectSchema schema) {
        // Return empty list - batch actions are not implemented in JDBC mode
        return new ArrayList<>();
    }
}
