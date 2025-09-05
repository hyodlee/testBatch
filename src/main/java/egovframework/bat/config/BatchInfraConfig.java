package egovframework.bat.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchInfraConfig {

	@Lazy
    @Bean(name = "transactionManager")
    PlatformTransactionManager transactionManager(@Qualifier("dataSource") DataSource ds) {
        return new DataSourceTransactionManager(ds);
    }

    @Bean(name = "lobHandler")
    LobHandler lobHandler() {
        return new DefaultLobHandler();
    }

}
