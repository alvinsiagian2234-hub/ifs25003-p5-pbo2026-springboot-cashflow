package org.delcom.ifs25003springbootcashflow.config;

import org.delcom.ifs25003springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25003springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25003springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25003springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25003springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25003springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25003springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25003springbootcashflow.usecase.UserUseCase;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import javax.sql.DataSource;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void wiresBeansAndSeedsAdmin() throws Exception {
        AppConfig config = new AppConfig();
        DataSource dataSource = createDataSource();

        IUserRepository userRepository = config.userRepository(dataSource);
        ITransactionRepository transactionRepository = config.transactionRepository(dataSource, userRepository);
        FinanceUseCase financeUseCase = config.financeUseCase(transactionRepository);
        AuthUseCase authUseCase = config.authUseCase(userRepository);
        UserUseCase userUseCase = config.userUseCase(userRepository);
        FinancePresenter presenter = config.financePresenter();
        FileStorage fileStorage = config.fileStorage(tempDir.toString());

        assertNotNull(financeUseCase);
        assertNotNull(userUseCase);
        assertNotNull(presenter);
        assertEquals(tempDir.toAbsolutePath().normalize(), fileStorage.root());

        config.seedRootAdmin(authUseCase, "admin", "admin").run(new DefaultApplicationArguments());
        int adminId = userRepository.findByUsername("admin").orElseThrow().getId();
        assertTrue(userRepository.findById(adminId).orElseThrow().isAdmin());

        // tabel transactions ikut dibuat oleh SchemaInitializer
        financeUseCase.addTransaction(adminId, "Gaji", 1_000L, TransactionType.INCOME);
        assertEquals(1_000L, financeUseCase.getBalance(adminId));
    }

    @Test
    void mapsUploadsForExistingAndMissingFolder() {
        AppConfig config = new AppConfig();
        // folder ada -> URI sudah berakhiran "/"
        assertUploadMapping(config, config.fileStorage(tempDir.toString()));
        // folder belum ada -> URI tanpa "/" dan harus ditambahkan
        assertUploadMapping(config, config.fileStorage(tempDir.resolve("belum-ada").toString()));
    }

    private void assertUploadMapping(AppConfig config, FileStorage fileStorage) {
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
        when(registry.addResourceHandler(anyString())).thenReturn(registration);
        when(registration.addResourceLocations(anyString())).thenReturn(registration);

        config.uploadResourceConfigurer(fileStorage).addResourceHandlers(registry);

        verify(registry).addResourceHandler("/uploads/**");
        String expected = fileStorage.root().toUri().toString();
        verify(registration).addResourceLocations(expected.endsWith("/") ? expected : expected + "/");
    }

    private DataSource createDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:appconfig_" + System.nanoTime()
                + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}
