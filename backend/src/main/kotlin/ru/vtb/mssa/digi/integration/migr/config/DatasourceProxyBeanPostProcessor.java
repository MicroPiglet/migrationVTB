package ru.vtb.mssa.digi.integration.migr.config;

import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.listener.logging.DefaultQueryLogEntryCreator;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;

@Component
@Slf4j
@ConditionalOnProperty(value = "debug.datasource.proxy.enabled", havingValue = "true")
public class DatasourceProxyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof DataSource) {
            log.info("DataSource bean has been found: {}", bean);
            final ProxyFactory proxyFactory = new ProxyFactory(bean);
            proxyFactory.setProxyTargetClass(true);
            proxyFactory.addAdvice(new ProxyDataSourceInterceptor((DataSource) bean));
            return proxyFactory.getProxy();
        }
        return bean;
    }

    private static class ProxyDataSourceInterceptor implements MethodInterceptor {
        private final DataSource dataSource;

        public ProxyDataSourceInterceptor(final DataSource dataSource) {
            super();
            DefaultQueryLogEntryCreator logEntryCreator = new DefaultQueryLogEntryCreator() {
                @Override
                protected String formatQuery(String query) {
                    return FormatStyle.BASIC.getFormatter().format(query);  // use Hibernte formatter
                }
            };
            logEntryCreator.setMultiline(true);
            SLF4JQueryLoggingListener listener = new SLF4JQueryLoggingListener();
            listener.setLogLevel(SLF4JLogLevel.INFO);
            listener.setQueryLogEntryCreator(logEntryCreator);
            this.dataSource = ProxyDataSourceBuilder.create(dataSource)
                                                    .name(dataSource.getClass().getSimpleName())
                                                    .listener(listener)
                                                    .countQuery()
                                                    .build();
        }

        @Override
        public Object invoke(final MethodInvocation invocation) throws Throwable {
            final Method proxyMethod = ReflectionUtils.findMethod(this.dataSource.getClass(), invocation.getMethod().getName());
            if (proxyMethod != null) {
                return proxyMethod.invoke(this.dataSource, invocation.getArguments());
            }
            return invocation.proceed();
        }
    }
}
