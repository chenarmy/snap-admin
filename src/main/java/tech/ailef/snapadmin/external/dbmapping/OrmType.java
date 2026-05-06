package tech.ailef.snapadmin.external.dbmapping;

/**
 * Represents the ORM type used by an entity class.
 * SnapAdmin now only supports MyBatis-Plus.
 */
public enum OrmType {
    /**
     * MyBatis-Plus - uses @TableName, @TableId, @TableField, etc.
     */
    MYBATIS_PLUS
}
