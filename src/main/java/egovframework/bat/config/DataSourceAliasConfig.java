package egovframework.bat.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * app.datasource.primary 프로퍼티에 지정된 데이터소스에
 * 'dataSource'라는 별칭을 등록한다.
 */
@Configuration
public class DataSourceAliasConfig implements BeanFactoryPostProcessor, EnvironmentAware {

    /** 환경 정보를 제공하는 객체 */
    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * BeanFactory 초기화 시점에 데이터소스 별칭을 등록한다.
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String primary = environment.getProperty("app.datasource.primary");
        if (primary != null && beanFactory.containsBeanDefinition(primary)) {
            // 예: dataSource-stg → dataSource 별칭 등록
            beanFactory.registerAlias(primary, "dataSource");
        }
    }
}

