package egovframework.bat.config;

import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * Quartz Job 인스턴스에 스프링 빈 주입을 지원하는 JobFactory 구현체
 */
@Component
public class AutowiringJobFactory implements JobFactory {

    /** 스프링 빈 생성을 위한 팩토리 */
    private final AutowireCapableBeanFactory beanFactory;

    /**
     * 스프링 {@link AutowireCapableBeanFactory}를 주입받는다.
     */
    public AutowiringJobFactory(AutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        // 스프링 팩토리로부터 잡을 생성하여 의존성을 자동 주입한다.
        return beanFactory.createBean(bundle.getJobDetail().getJobClass());
    }
}
