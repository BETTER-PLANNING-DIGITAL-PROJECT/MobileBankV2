package ibnk.datasources;

import org.springframework.beans.factory.annotation.Qualifier;
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
        basePackages = "ibnk.repositories.rptBanking",
        entityManagerFactoryRef = "rptBankingEntityManagerFactory",
        transactionManagerRef = "rptBankingTransactionManagerFactory"
)
//src/main/java/ibnk/repositories/rptBanking
@EntityScan(basePackages = "ibnk.models.rptBanking")
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableAsync
public class RptBankingDataSource {

    @Bean(name = "gb_rptBankingDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.rptbanking")
    public DataSource defaultDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "rptBankingEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean bankingEntityManagerFactory(@Qualifier("gb_rptBankingDataSource") DataSource bankingDataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(bankingDataSource);
        em.setPackagesToScan("ibnk.models.rptBanking");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");
        properties.put("hibernate.show_sql", "false");
        properties.put("hibernate.format_sql", "false");
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean(name = "rptBankingTransactionManagerFactory")
    public PlatformTransactionManager  primaryTransactionManager(@Qualifier("rptBankingEntityManagerFactory") LocalContainerEntityManagerFactoryBean bankingTransactionManager) {
        return new JpaTransactionManager(bankingTransactionManager.getObject());
    }

    @Bean(name = "rptBankingJdbcTemplate")
    public JdbcTemplate bankingJdbcTemplate(@Qualifier("gb_rptBankingDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}

