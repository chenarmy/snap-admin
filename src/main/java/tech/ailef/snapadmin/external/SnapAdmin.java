/*
 * SnapAdmin - An automatically generated CRUD admin UI for Spring Boot apps
 * Copyright (C) 2023 Ailef (http://ailef.tech)
 */

package tech.ailef.snapadmin.external;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import tech.ailef.snapadmin.external.annotations.Disable;
import tech.ailef.snapadmin.external.annotations.DisplayFormat;
import tech.ailef.snapadmin.external.dbmapping.AnnotationUtils;
import tech.ailef.snapadmin.external.dbmapping.CustomJpaRepository;
import tech.ailef.snapadmin.external.dbmapping.DbObjectSchema;
import tech.ailef.snapadmin.external.dbmapping.OrmType;
import tech.ailef.snapadmin.external.dbmapping.fields.DbField;
import tech.ailef.snapadmin.external.dbmapping.fields.DbFieldType;
import tech.ailef.snapadmin.external.dbmapping.fields.EnumFieldType;
import tech.ailef.snapadmin.external.dbmapping.fields.StringFieldType;
import tech.ailef.snapadmin.external.dbmapping.fields.TextFieldType;
import tech.ailef.snapadmin.external.dto.MappingError;
import tech.ailef.snapadmin.external.exceptions.SnapAdminException;
import tech.ailef.snapadmin.external.exceptions.SnapAdminNotFoundException;
import tech.ailef.snapadmin.external.exceptions.UnsupportedFieldTypeException;
import tech.ailef.snapadmin.external.misc.Utils;

@Component
public class SnapAdmin {
	private static final Logger logger = LoggerFactory.getLogger(SnapAdmin.class.getName());
	
	private EntityManager entityManager;
	
	private List<DbObjectSchema> schemas = new ArrayList<>();
	
	private List<String> modelsPackage;
	
	private SnapAdminProperties properties;
	
	private boolean authenticated;
	
	private static final String VERSION = "0.2.0";
    
    public SnapAdmin(@Autowired(required = false) EntityManager entityManager, @Autowired SnapAdminProperties properties) {
		this.modelsPackage = Arrays.stream(properties.getModelsPackage().split(",")).map(String::trim).toList();
		this.entityManager = entityManager;
		this.properties = properties;
	}
	
	@PostConstruct
	private void init() {
		logger.debug("Initializing SnapAdmin...");
		
		OrmType ormType = properties.getOrmType() != null ? properties.getOrmType() : OrmType.JPA;
		logger.info("Using ORM type: " + ormType);
		
		for (String currentPackage : modelsPackage) {
			logger.debug("Scanning package " + currentPackage);
			
			List<BeanDefinition> allBeanDefs = new ArrayList<>();
			
			if (ormType == OrmType.JPA) {
				ClassPathScanningCandidateComponentProvider jpaProvider = new ClassPathScanningCandidateComponentProvider(false);
				jpaProvider.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
				allBeanDefs.addAll(jpaProvider.findCandidateComponents(currentPackage));
				logger.debug("Found " + allBeanDefs.size() + " candidate @Entity classes");
			} else if (ormType == OrmType.MYBATIS_PLUS) {
				try {
					Class<?> tableNameClass = Class.forName("com.baomidou.mybatisplus.annotation.TableName");
					ClassPathScanningCandidateComponentProvider mpProvider = new ClassPathScanningCandidateComponentProvider(false);
					mpProvider.addIncludeFilter(new AnnotationTypeFilter((Class<java.lang.annotation.Annotation>) tableNameClass));
					allBeanDefs.addAll(mpProvider.findCandidateComponents(currentPackage));
					logger.debug("Found " + allBeanDefs.size() + " candidate @TableName classes");
				} catch (ClassNotFoundException e) {
					logger.error("MyBatis-Plus not found on classpath but MYBATIS_PLUS ORM type specified");
					throw new SnapAdminException("MyBatis-Plus not found on classpath. Please add mybatis-plus dependency.");
				}
			}
			
			for (BeanDefinition bd : allBeanDefs) {
				DbObjectSchema schema = processBeanDefinition(bd, ormType);
				if (schema != null)
					schemas.add(schema);
			}
			
			logger.info("Scanned package '" + currentPackage + "'. Loaded " + allBeanDefs.size() + " schemas.");
		}

		boolean hasErrors = schemas.stream().flatMap(s -> s.getErrors().stream()).count() > 0;
		
		logger.info("SnapAdmin initialized. Loaded " + schemas.size() 
				+ " schemas from " + modelsPackage.size() + " packages" + (hasErrors ? " (with errors)" : ""));
		logger.info("SnapAdmin web interface at: http://YOUR_HOST:YOUR_PORT/" + properties.getBaseUrl());
		
	}
	
	public String getVersion() {
		return VERSION;
	}
	
	public List<DbObjectSchema> getSchemas() {
		return Collections.unmodifiableList(schemas);
	}
	
	public DbObjectSchema findSchemaByClassName(String className) {
		return schemas.stream().filter(s -> s.getClassName().equals(className)).findFirst().orElseThrow(() -> {
			return new SnapAdminNotFoundException("Schema " + className + " not found.");
		});
	}
	
	public DbObjectSchema findSchemaByTableName(String tableName) {
		return schemas.stream().filter(s -> s.getTableName().equals(tableName)).findFirst().orElseThrow(() -> {
			return new SnapAdminException("Schema " + tableName + " not found.");
		});
	}
	
	public DbObjectSchema findSchemaByClass(Class<?> klass) {
		return findSchemaByClassName(klass.getName());
	}
	
	public boolean isManagedClass(Class<?> klass) {
		Optional<DbObjectSchema> hasSchema = 
			schemas.stream().filter(s -> s.getClassName().equals(klass.getName())).findFirst();
		return hasSchema.isPresent();
	}
	
	private DbObjectSchema processBeanDefinition(BeanDefinition bd, OrmType ormType) {
		String fullClassName = bd.getBeanClassName();
		
		try {
			Class<?> klass = Class.forName(fullClassName);
			
			Disable disabled = klass.getAnnotation(Disable.class);
			if (disabled != null)
				return null;
			
			DbObjectSchema schema = new DbObjectSchema(klass, this);
			schema.setOrmType(ormType);
			
			if (ormType == OrmType.JPA) {
				if (entityManager == null) {
					throw new SnapAdminException("EntityManager is null but JPA ORM type is specified");
				}
				CustomJpaRepository simpleJpaRepository = new CustomJpaRepository(schema, entityManager);
				schema.setJpaRepository(simpleJpaRepository);
			} else if (ormType == OrmType.MYBATIS_PLUS) {
				logger.debug("MyBatis-Plus entity detected: " + klass.getName());
			}
			
			logger.debug("Processing class: " + klass + " - Table: " + schema.getTableName());
			
			Field[] fields = klass.getDeclaredFields();
			for (Field f : fields) {
				try {
					DbField field = mapField(f, schema, ormType);
					field.setSchema(schema);
					schema.addField(field);
				} catch (UnsupportedFieldTypeException e) {
					logger.warn("The class " + klass.getSimpleName() + " contains the field `" 
								+ f.getName() + "` of type `" + f.getType().getSimpleName() + "`, which is not supported");
					schema.addError(
						new MappingError(
							"The class contains the field `" + f.getName() + "` of type `" + f.getType().getSimpleName() + "`, which is not supported"
						)
					);
				}
			}
			
			logger.debug("Processed " + klass + ", extracted " + schema.getSortedFields().size() + " fields");
			
			return schema;
		} catch (ClassNotFoundException |
				IllegalArgumentException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String determineFieldName(Field f, OrmType ormType) {
		String fieldName = Utils.camelToSnake(f.getName());
		
		if (ormType == OrmType.JPA) {
			Column[] columnAnnotations = f.getAnnotationsByType(Column.class);
			if (columnAnnotations.length != 0) {
				Column col = columnAnnotations[0];
				if (col.name() != null && !col.name().isBlank())
					fieldName = col.name();
			}
		} else if (ormType == OrmType.MYBATIS_PLUS) {
			String mpColumnName = AnnotationUtils.getColumnName(f, ormType);
			if (mpColumnName != null && !mpColumnName.isBlank()) {
				fieldName = mpColumnName;
			}
		}
		
		return fieldName;
	}
	
	private boolean determineNullable(Field f, OrmType ormType) {
		if (ormType == OrmType.JPA) {
			Column[] columnAnnotations = f.getAnnotationsByType(Column.class);
			boolean nullable = true;
			if (columnAnnotations.length != 0) {
				Column col = columnAnnotations[0];
				nullable = col.nullable();
			}
			return nullable;
		} else {
			return AnnotationUtils.isNullable(f, ormType);
		}
	}
	
	private DbField mapField(Field f, DbObjectSchema schema, OrmType ormType) {
		logger.debug("Processing field " + f.getName());
		
		OneToMany oneToMany = null;
		ManyToMany manyToMany = null;
		ManyToOne manyToOne = null;
		OneToOne oneToOne = null;
		Lob lob = null;
		
		if (ormType == OrmType.JPA) {
			oneToMany = f.getAnnotation(OneToMany.class);
			manyToMany = f.getAnnotation(ManyToMany.class);
			manyToOne = f.getAnnotation(ManyToOne.class);
			oneToOne = f.getAnnotation(OneToOne.class);
			lob = f.getAnnotation(Lob.class);
		} else {
			lob = f.getAnnotation(Lob.class);
		}
		
		String fieldName = determineFieldName(f, ormType);
		
		Class<?> connectedType = null;
		
		DbFieldType fieldType = null;
		try {
			Class<? extends DbFieldType> fieldTypeClass = DbFieldType.fromClass(f.getType());
			
			if (fieldTypeClass == StringFieldType.class && lob != null) {
				fieldTypeClass = TextFieldType.class;
			}
			
			if (fieldTypeClass != EnumFieldType.class) {
				try {
					fieldType = fieldTypeClass.getConstructor().newInstance();
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					// If failure, we try to map a relationship on this field later
				}
			}
		} catch (SnapAdminException e) {
			// If failure, we try to map a relationship on this field later
		}

		if (ormType == OrmType.JPA) {
			if (manyToOne != null || oneToOne != null) {
				fieldName = mapRelationshipJoinColumn(f, ormType);
				fieldType = mapForeignKeyType(f.getType());
				connectedType = f.getType();
			}
			
			if (manyToMany != null || oneToMany != null) {
				ParameterizedType stringListType = (ParameterizedType) f.getGenericType();
		        Class<?> targetEntityClass = (Class<?>) stringListType.getActualTypeArguments()[0];
		        fieldType = mapForeignKeyType(targetEntityClass);
		        connectedType = targetEntityClass;
			}
		}
		
		if (fieldType == null && ormType == OrmType.JPA) {
			Enumerated enumerated = f.getAnnotation(Enumerated.class);
			if (enumerated != null) {
				EnumType type = enumerated.value();
				fieldType = new EnumFieldType(f.getType(), type);
			}
		}
		
		if (fieldType == null) {
			throw new UnsupportedFieldTypeException("Unable to determine fieldType for " + f.getType());
		}
		
		DisplayFormat displayFormat = f.getAnnotation(DisplayFormat.class);
		
		DbField field = new DbField(f.getName(), fieldName, f, fieldType, schema, displayFormat != null ? displayFormat.format() : null);
		field.setConnectedType(connectedType);
		
		boolean isPrimaryKey = AnnotationUtils.isPrimaryKey(f, ormType);
		field.setPrimaryKey(isPrimaryKey);
		
		field.setNullable(determineNullable(f, ormType));
		
		if (field.isPrimaryKey())
			field.setNullable(false);
		
		return field;
	}
	
	private String mapRelationshipJoinColumn(Field f, OrmType ormType) {
		String joinColumnName = Utils.camelToSnake(f.getName()) + "_id"; 
		
		if (ormType == OrmType.JPA) {
			JoinColumn[] joinColumn = f.getAnnotationsByType(JoinColumn.class);
			if (joinColumn.length != 0) {
				joinColumnName = joinColumn[0].name();
			}
		} else {
			String columnName = AnnotationUtils.getColumnName(f, ormType);
			if (columnName != null && !columnName.isBlank()) {
				joinColumnName = columnName;
			}
		}
		return joinColumnName;
	}
	
	private DbFieldType mapForeignKeyType(Class<?> entityClass) {
		try {
			Object linkedEntity = entityClass.getConstructor().newInstance();
			Class<?> linkType = null;
			
			for (Field ef : linkedEntity.getClass().getDeclaredFields()) {
				if (ef.getAnnotationsByType(Id.class).length != 0) {
					linkType = ef.getType();
				}
			}
			
			if (linkType == null)
				throw new SnapAdminException("Unable to find @Id field in Entity class " + entityClass);
			
			return DbFieldType.fromClass(linkType).getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new SnapAdminException(e);
		}
	}

	public boolean isAuthenticated() {
		return authenticated;
	}
	
	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

}
