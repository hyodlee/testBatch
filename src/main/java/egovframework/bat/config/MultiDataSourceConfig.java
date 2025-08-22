package egovframework.bat.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class MultiDataSourceConfig {

    // 스테이징 MySQL (Primary)
    @Primary
    @Bean(name = "migstgDataSource")
    @ConfigurationProperties("spring.datasource.migstg-mysql")
    public DataSource migstgDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "migstgJdbcTemplate")
    public JdbcTemplate migstgJdbcTemplate(@Qualifier("migstgDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    // 운영용 MySQL
    @Bean(name = "egovlocalDataSource")
    @ConfigurationProperties("spring.datasource.egovlocal-mysql")
    public DataSource egovlocalDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "egovlocalJdbcTemplate")
    public JdbcTemplate egovlocalJdbcTemplate(@Qualifier("egovlocalDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    // Remote1 CUBRID
    @Bean(name = "egovremote1CubridDataSource")
    @ConfigurationProperties("spring.datasource.egovremote1-cubrid")
    public DataSource egovremote1CubridDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "egovremote1CubridJdbcTemplate")
    public JdbcTemplate egovremote1CubridJdbcTemplate(
            @Qualifier("egovremote1CubridDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }
}
