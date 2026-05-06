//package tech.ailef.snapadmin.internal.config;
//
//import javax.sql.DataSource;
//
//import org.mybatis.spring.annotation.MapperScan;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
//
//@Configuration
//@MapperScan(value = "tech.ailef.snapadmin.internal.mapper", sqlSessionFactoryRef = "internalSqlSessionFactory")
//public class MyBatisPlusConfig {
//
//    @Autowired
//    private DataSource dataSource;
//
//    @Bean
//    public MybatisSqlSessionFactoryBean internalSqlSessionFactory() {
//        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
//        factory.setDataSource(dataSource);
//        return factory;
//    }
//}
