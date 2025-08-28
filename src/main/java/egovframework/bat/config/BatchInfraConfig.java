package egovframework.bat.config;

import java.beans.PropertyEditor;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.support.IntArrayPropertyEditor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.CustomEditorConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.transaction.PlatformTransactionManager;

//@Configuration
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

    @Bean
    static CustomEditorConfigurer customEditorConfigurer() {
        CustomEditorConfigurer c = new CustomEditorConfigurer();
        Map<Class<?>, Class<? extends PropertyEditor>> editors = new HashMap<>();
        editors.put(int[].class, IntArrayPropertyEditor.class);
        c.setCustomEditors(editors);
        return c;
    }
}
