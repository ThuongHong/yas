package com.yas.sampledata.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

class DataSourceConfigTest {

    @Test
    void productDataSource_andMediaDataSource_shouldBuildConfiguredDataSources() {
        DataSourceConfig config = configured();

        DataSource productDataSource = config.productDataSource();
        DataSource mediaDataSource = config.mediaDataSource();

        assertThat(productDataSource).isNotNull();
        assertThat(mediaDataSource).isNotNull();
    }

    @Test
    void jdbcBeans_shouldWrapProvidedDataSource() {
        DataSourceConfig config = configured();
        DataSource productDataSource = config.productDataSource();
        DataSource mediaDataSource = config.mediaDataSource();

        JdbcTemplate productJdbc = config.jdbcProduct(productDataSource);
        JdbcTemplate mediaJdbc = config.jdbcMedia(mediaDataSource);

        assertThat(productJdbc.getDataSource()).isSameAs(productDataSource);
        assertThat(mediaJdbc.getDataSource()).isSameAs(mediaDataSource);
    }

    @Test
    void productDataSource_shouldFailWhenDriverClassNameIsInvalid() {
        DataSourceConfig config = new DataSourceConfig();
        ReflectionTestUtils.setField(config, "driverClassName", "invalid.Driver");
        ReflectionTestUtils.setField(config, "productUrl", "jdbc:h2:mem:product");
        ReflectionTestUtils.setField(config, "mediaUrl", "jdbc:h2:mem:media");
        ReflectionTestUtils.setField(config, "username", "sa");
        ReflectionTestUtils.setField(config, "password", "");

        assertThatThrownBy(config::productDataSource).isInstanceOf(RuntimeException.class);
    }

    private static DataSourceConfig configured() {
        DataSourceConfig config = new DataSourceConfig();
        ReflectionTestUtils.setField(config, "driverClassName", "org.h2.Driver");
        ReflectionTestUtils.setField(config, "productUrl", "jdbc:h2:mem:product;DB_CLOSE_DELAY=-1");
        ReflectionTestUtils.setField(config, "mediaUrl", "jdbc:h2:mem:media;DB_CLOSE_DELAY=-1");
        ReflectionTestUtils.setField(config, "username", "sa");
        ReflectionTestUtils.setField(config, "password", "");
        return config;
    }
}
