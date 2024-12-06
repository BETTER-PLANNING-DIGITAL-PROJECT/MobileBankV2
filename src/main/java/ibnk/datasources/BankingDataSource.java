package ibnk.datasources;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(
        basePackages = "ibnk.repositories.banking",
        entityManagerFactoryRef = "bankingEntityManagerFactory",
        transactionManagerRef = "bankingTransactionManagerFactory"
)
@EntityScan(basePackages = "ibnk.models.banking")
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableAsync
public class BankingDataSource {
    final HikariConfig config = new HikariConfig();
    @Value("${spring.datasource.rptbanking.username}")
    private String username;
    @Value("${spring.datasource.rptbanking.password}")
    private String password;

    @Value("${spring.datasource.banking.jdbcUrl}")
    private String url;
    @Value("${spring.datasource.banking.driver-class-name}")
    private String driver;

    @Bean(name = "gb_bankingDataSource")
//    @ConfigurationProperties(prefix = "spring.datasource.banking")
    public DataSource defaultDataSource() {
        config.setJdbcUrl(url);
        config.setDriverClassName(driver);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(100);
        config.setMinimumIdle(10);
        config.setIdleTimeout(60000);
        config.setConnectionTimeout(60000);
        config.setMaxLifetime(1800000);

        HikariDataSource dataSource = new HikariDataSource(config);
        return  dataSource;
//        return DataSourceBuilder.create().build();
    }

    @Bean(name = "bankingEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean bankingEntityManagerFactory(@Qualifier("gb_bankingDataSource") DataSource bankingDataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(bankingDataSource);
        em.setPackagesToScan("ibnk.models.banking");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());


        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "false");
        em.setJpaPropertyMap(properties);
//        config.setMinimumIdle(5);
//        config.setMaximumPoolSize(50);
//        config.setConnectionTimeout(10000);
//        config.setIdleTimeout(600000);
//        config.setMaxLifetime(1800000);

        return em;
    }

    @Bean(name = "bankingTransactionManagerFactory")
    public PlatformTransactionManager primaryTransactionManager(@Qualifier("bankingEntityManagerFactory") LocalContainerEntityManagerFactoryBean bankingTransactionManager) {
        return new JpaTransactionManager(bankingTransactionManager.getObject());
    }

    @Bean(name = "bankingJdbcTemplate")
    public JdbcTemplate bankingJdbcTemplate(@Qualifier("gb_bankingDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
