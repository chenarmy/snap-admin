package tech.ailef.snapadmin.external.dbmapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Utility class for reading MyBatis-Plus annotations.
 * This allows the framework to work with MyBatis-Plus entities.
 */
public class AnnotationUtils {

    /**
     * Detects the ORM type of a class. Since we only support MyBatis-Plus now,
     * this always returns MYBATIS_PLUS.
     * @param klass the class to check
     * @return always returns OrmType.MYBATIS_PLUS
     */
    public static OrmType detectOrmType(Class<?> klass) {
        // Check for MyBatis-Plus @TableName
        try {
            Class<?> tableNameClass = Class.forName("com.baomidou.mybatisplus.annotation.TableName");
            Annotation tableName = klass.getAnnotation((Class<? extends Annotation>) tableNameClass);
            if (tableName != null) {
                return OrmType.MYBATIS_PLUS;
            }
        } catch (ClassNotFoundException e) {
            // MyBatis-Plus not on classpath
        }
        return OrmType.MYBATIS_PLUS;
    }

    /**
     * Determines the table name for a class from MyBatis-Plus @TableName.
     * @param klass the entity class
     * @param ormType the ORM type (ignored, always uses MyBatis-Plus)
     * @return the table name, or null if no annotation is found
     */
    public static String getTableName(Class<?> klass, OrmType ormType) {
        try {
            Class<?> tableNameClass = Class.forName("com.baomidou.mybatisplus.annotation.TableName");
            Annotation tableName = klass.getAnnotation((Class<? extends Annotation>) tableNameClass);
            if (tableName != null) {
                java.lang.reflect.Method valueMethod = tableNameClass.getMethod("value");
                String value = (String) valueMethod.invoke(tableName);
                if (value != null && !value.isBlank()) {
                    return value;
                }
            }
        } catch (Exception e) {
            // Fall through
        }
        return null;
    }

    /**
     * Determines the column name for a field from MyBatis-Plus @TableField.
     * @param field the Java field
     * @param ormType the ORM type (ignored, always uses MyBatis-Plus)
     * @return the column name, or null if no annotation is found
     */
    public static String getColumnName(Field field, OrmType ormType) {
        try {
            Class<?> tableFieldClass = Class.forName("com.baomidou.mybatisplus.annotation.TableField");
            Annotation tableField = field.getAnnotation((Class<? extends Annotation>) tableFieldClass);
            if (tableField != null) {
                java.lang.reflect.Method valueMethod = tableFieldClass.getMethod("value");
                String value = (String) valueMethod.invoke(tableField);
                if (value != null && !value.isBlank()) {
                    return value;
                }
            }
        } catch (Exception e) {
            // Fall through
        }
        return null;
    }

    /**
     * Checks if a field is a primary key using MyBatis-Plus @TableId.
     */
    public static boolean isPrimaryKey(Field field, OrmType ormType) {
        try {
            Class<?> tableIdClass = Class.forName("com.baomidou.mybatisplus.annotation.TableId");
            return field.getAnnotation((Class<? extends Annotation>) tableIdClass) != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Checks if a field's value is auto-generated using MyBatis-Plus @TableId with auto strategy.
     */
    public static boolean isGeneratedValue(Field field, OrmType ormType) {
        try {
            Class<?> tableIdClass = Class.forName("com.baomidou.mybatisplus.annotation.TableId");
            Annotation tableId = field.getAnnotation((Class<? extends Annotation>) tableIdClass);
            if (tableId != null) {
                java.lang.reflect.Method typeMethod = tableIdClass.getMethod("type");
                Object typeValue = typeMethod.invoke(tableId);
                // Check if it's not INPUT strategy (INPUT means user provides the value)
                Class<?> idTypeClass = Class.forName("com.baomidou.mybatisplus.annotation.IdType");
                java.lang.reflect.Method nameMethod = idTypeClass.getMethod("name");
                String typeName = (String) nameMethod.invoke(typeValue);
                // AUTO and ASSIGN_ID are generated types
                return "AUTO".equals(typeName) || "ASSIGN_ID".equals(typeName) || "ASSIGN_UUID".equals(typeName);
            }
        } catch (Exception e) {
            // Fall through
        }
        return false;
    }

    /**
     * Determines if a field is nullable. MyBatis-Plus doesn't have a direct nullable property,
     * so this always returns true.
     */
    public static boolean isNullable(Field field, OrmType ormType) {
        try {
            Class<?> tableFieldClass = Class.forName("com.baomidou.mybatisplus.annotation.TableField");
            Annotation tableField = field.getAnnotation((Class<? extends Annotation>) tableFieldClass);
            if (tableField != null) {
                // MyBatis-Plus doesn't have a nullable property, but we check if it's required
                // There's no direct nullable equivalent, so we default to true
            }
        } catch (Exception e) {
            // Fall through
        }
        return true;
    }

    /**
     * Checks if a field is a JPA relationship field.
     * MyBatis-Plus doesn't have relationship annotations, so this always returns false.
     */
    public static boolean isRelationshipField(Field field, OrmType ormType) {
        return false;
    }

    /**
     * Checks if a field is a to-one relationship.
     * MyBatis-Plus doesn't have relationship annotations, so this always returns false.
     */
    public static boolean isToOneRelationship(Field field, OrmType ormType) {
        return false;
    }

    /**
     * Checks if a field is a to-many relationship.
     * MyBatis-Plus doesn't have relationship annotations, so this always returns false.
     */
    public static boolean isToManyRelationship(Field field, OrmType ormType) {
        return false;
    }

    /**
     * Checks if a field is a ManyToOne relationship.
     * MyBatis-Plus doesn't have relationship annotations, so this always returns false.
     */
    public static boolean isManyToOne(Field field, OrmType ormType) {
        return false;
    }

    /**
     * Checks if a field is a OneToOne relationship.
     * MyBatis-Plus doesn't have relationship annotations, so this always returns false.
     */
    public static boolean isOneToOne(Field field, OrmType ormType) {
        return false;
    }

    /**
     * Checks if a field is a OneToMany relationship.
     * MyBatis-Plus doesn't have relationship annotations, so this always returns false.
     */
    public static boolean isOneToMany(Field field, OrmType ormType) {
        return false;
    }

    /**
     * Checks if a field is a ManyToMany relationship.
     * MyBatis-Plus doesn't have relationship annotations, so this always returns false.
     */
    public static boolean isManyToMany(Field field, OrmType ormType) {
        return false;
    }

    /**
     * Gets the mappedBy value for a relationship field.
     * MyBatis-Plus doesn't have relationship annotations, so this always returns empty string.
     */
    public static String getMappedBy(Field field, OrmType ormType) {
        return "";
    }

    /**
     * Checks if a field has the @Lob annotation.
     * MyBatis-Plus doesn't have @Lob, so this always returns false.
     */
    public static boolean isLob(Field field, OrmType ormType) {
        return false;
    }

    /**
     * Gets the join column name for a relationship field.
     * MyBatis-Plus doesn't have relationship annotations, so this returns the fallback name.
     */
    public static String getJoinColumnName(Field field, OrmType ormType, String fallbackName) {
        return fallbackName;
    }

    // Utility class - no instance needed
    private AnnotationUtils() {}
}
