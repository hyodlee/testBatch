package egovframework.bat.config;

import javax.sql.DataSource;

import java.util.Arrays;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * MyBatis 설정을 담당하는 구성 클래스.
 * XML 형태의 매퍼 파일을 자동으로 로딩한다.
 */
@Configuration
@MapperScan(basePackages = "egovframework.bat")
public class MyBatisConfig {

    /**
     * SqlSessionFactoryBean을 생성하여 설정 파일과 매퍼 XML 위치를 지정한다.
     *
     * @param dataSource 기본 데이터소스
     * @return 설정된 SqlSessionFactoryBean
     * @throws Exception 리소스 로딩 실패 시
     */
    @Bean
    public SqlSessionFactoryBean sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        // MyBatis 설정 파일 위치 지정
        factoryBean.setConfigLocation(new ClassPathResource("egovframework/batch/mapper/config/mapper-config.xml"));
        // 매퍼 XML 경로 지정 (config 디렉터리를 제외하기 위해 필터링)
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] mapperLocations = Arrays.stream(resolver.getResources("classpath:/egovframework/batch/mapper/*/*.xml"))
            .filter(resource -> !resource.getURL().getPath().contains("/config/"))
            .toArray(Resource[]::new);
        factoryBean.setMapperLocations(mapperLocations);
        return factoryBean;
    }
}
