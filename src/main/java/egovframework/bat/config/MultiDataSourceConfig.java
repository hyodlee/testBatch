package egovframework.bat.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class MultiDataSourceConfig {

    // 스테이징 MySQL (Primary) 데이타소스
    @Primary
    //@Bean(name = "migstgDataSource")
    @Bean(name = {"dataSource", "dataSource-stg"})
    @ConfigurationProperties("spring.datasource.migstg-mysql")
    DataSource migstgDataSource() {
        return DataSourceBuilder.create().build();
    }

    // 스테이징 MySQL (Primary) 데이타소스를 사용하는 JdbcTemplate
    @Bean(name = "migstgJdbcTemplate")
    //JdbcTemplate migstgJdbcTemplate(@Qualifier("migstgDataSource") DataSource ds) {
    JdbcTemplate migstgJdbcTemplate(@Qualifier("dataSource-stg") DataSource ds) {
        //return new JdbcTemplate(ds);
        // LazyConnectionDataSourceProxy로 실제 커넥션 생성을 지연
        return new JdbcTemplate(new LazyConnectionDataSourceProxy(ds));
    }

    // 운영 MySQL용 데이타소스
    //@Bean(name = "egovlocalDataSource")
    @Bean(name = "dataSource-local")
    @Lazy // 필요한 시점까지 Bean 초기화를 지연
    @ConfigurationProperties("spring.datasource.egovlocal-mysql")
    DataSource egovlocalDataSource() {
        return DataSourceBuilder.create().build();
    }

    // 운영 MySQL용 데이타소스를 사용하는 JdbcTemplate
    @Bean(name = "jdbcTemplateLocal")
    //JdbcTemplate egovlocalJdbcTemplate(@Qualifier("egovlocalDataSource") DataSource ds) {
    JdbcTemplate egovlocalJdbcTemplate(@Qualifier("dataSource-local") DataSource ds) {
    	//return new JdbcTemplate(ds);
        // LazyConnectionDataSourceProxy로 실제 커넥션 생성을 지연
        return new JdbcTemplate(new LazyConnectionDataSourceProxy(ds));
    }

    // Remote1 CUBRID 데이타소스
    //@Bean(name = "egovremote1CubridDataSource")
    @Bean(name = "dataSource-remote1")
    @Lazy // 필요한 시점까지 Bean 초기화를 지연
    @ConfigurationProperties("spring.datasource.egovremote1-cubrid")
    DataSource egovremote1CubridDataSource() {
        return DataSourceBuilder.create().build();
    }

    // Remote1 CUBRID 데이타소스를 사용하는 JdbcTemplate
    @Bean(name = "egovremote1CubridJdbcTemplate")
    //JdbcTemplate egovremote1CubridJdbcTemplate(@Qualifier("egovremote1CubridDataSource") DataSource ds) {
    JdbcTemplate egovremote1CubridJdbcTemplate(@Qualifier("dataSource-remote1") DataSource ds) {
    	//return new JdbcTemplate(ds);
        // LazyConnectionDataSourceProxy로 실제 커넥션 생성을 지연
        return new JdbcTemplate(new LazyConnectionDataSourceProxy(ds));
    }
}
