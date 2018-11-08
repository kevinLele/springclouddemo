package com.cloud.kevin.authserver.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * 数据源配置
 *
 * @author Kevin
 */
@Configuration
@Slf4j
@EnableConfigurationProperties(DataSourceConfig.DataSourceSetting.class)
public class DataSourceConfig {

    /**
     * 配置数据库连接池
     *
     * @param dataSourceSetting
     * @return
     */
    @Bean
    @Primary
    public DataSource dataSource(DataSourceSetting dataSourceSetting) {
        log.info("Initiating DataSource ...");
        DruidDataSource datasource = new DruidDataSource();

        datasource.setUrl(dataSourceSetting.getUrl());
        datasource.setUsername(dataSourceSetting.getUsername());
        datasource.setPassword(dataSourceSetting.getPassword());
        datasource.setDriverClassName(dataSourceSetting.getDriverClassName());

        //configuration
        datasource.setInitialSize(dataSourceSetting.getInitialSize());
        datasource.setMinIdle(dataSourceSetting.getMinIdle());
        datasource.setMaxActive(dataSourceSetting.getMaxActive());
        datasource.setMaxWait(dataSourceSetting.getMaxWait());
        datasource.setTimeBetweenEvictionRunsMillis(dataSourceSetting.getTimeBetweenEvictionRunsMillis());
        datasource.setMinEvictableIdleTimeMillis(dataSourceSetting.getMinEvictableIdleTimeMillis());
        datasource.setValidationQuery(dataSourceSetting.getValidationQuery());
        datasource.setTestWhileIdle(dataSourceSetting.isTestWhileIdle());
        datasource.setTestOnBorrow(dataSourceSetting.isTestOnBorrow());
        datasource.setTestOnReturn(dataSourceSetting.isTestOnReturn());
        datasource.setPoolPreparedStatements(dataSourceSetting.isPoolPreparedStatements());
        datasource.setMaxPoolPreparedStatementPerConnectionSize(dataSourceSetting.getMaxPoolPreparedStatementPerConnectionSize());

        try {
            datasource.setFilters(dataSourceSetting.getFilters());
        } catch (SQLException e) {
            log.error("druid configuration initialization filter: ", e);
        }

        datasource.setConnectionProperties(dataSourceSetting.getConnectionProperties());

        return datasource;
    }

    @Bean
    public ServletRegistrationBean druidServlet() {
        log.info("Initiating Druid Servlet Configurations ...");

        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new StatViewServlet(), "/druid/*");

        // IP白名单
        servletRegistrationBean.addInitParameter("allow", "192.168.2.25,127.0.0.1");

        // IP黑名单(共同存在时，deny优先于allow)
        servletRegistrationBean.addInitParameter("deny", "192.168.1.100");

        //控制台管理用户
        servletRegistrationBean.addInitParameter("loginUsername", "admin");
        servletRegistrationBean.addInitParameter("loginPassword", "admin");

        //是否能够重置数据 禁用HTML页面上的“Reset All”功能
        servletRegistrationBean.addInitParameter("resetEnable", "false");

        return servletRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new WebStatFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");

        return filterRegistrationBean;
    }

    @ConfigurationProperties(prefix = "spring.datasource")
    @Data
    protected class DataSourceSetting {
        private String url;
        private String username;
        private String password;
        private String driverClassName;
        private int initialSize;
        private int minIdle;
        private int maxActive;
        private int maxWait;
        private int timeBetweenEvictionRunsMillis;
        private int minEvictableIdleTimeMillis;
        private String validationQuery;
        private boolean testWhileIdle;
        private boolean testOnBorrow;
        private boolean testOnReturn;
        private boolean poolPreparedStatements;
        private int maxPoolPreparedStatementPerConnectionSize;
        private String filters;
        private String connectionProperties;
    }
}
