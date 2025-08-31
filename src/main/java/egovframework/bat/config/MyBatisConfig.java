package egovframework.bat.config;

import javax.sql.DataSource;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * MyBatis 설정을 담당하는 구성 클래스.
 * XML 형태의 매퍼 파일을 자동으로 로딩한다.
 */
@Configuration
@MapperScan(basePackages = "egovframework.bat")
public class MyBatisConfig {

    /**
     * SqlSessionFactoryBean을 생성하여 매퍼 XML 위치만 지정한다.
     *
     * @param dataSource 기본 데이터소스
     * @return 설정된 SqlSessionFactoryBean
     * @throws Exception 리소스 로딩 실패 시
     */
    @Bean
    public SqlSessionFactoryBean sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        // 매퍼 XML 경로 지정
        factoryBean.setMapperLocations(
            new PathMatchingResourcePatternResolver()
                .getResources("classpath:/egovframework/batch/mapper/**/*.xml"));
        return factoryBean;
    }
}
