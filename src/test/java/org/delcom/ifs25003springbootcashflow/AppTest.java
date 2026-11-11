package org.delcom.ifs25003springbootcashflow;

import org.delcom.ifs25003springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25003springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25003springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25003springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25003springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25003springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25003springbootcashflow.usecase.UserUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

/**
 * Smoke test: konteks Spring Boot berhasil dimuat (profil test → H2) dan
 * seluruh bean Clean Architecture ter-wiring.
 */
@SpringBootTest
@ActiveProfiles("test")
class AppTest {

    @Autowired
    private ITransactionRepository transactionRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private FinanceUseCase financeUseCase;

    @Autowired
    private AuthUseCase authUseCase;

    @Autowired
    private UserUseCase userUseCase;

    @Autowired
    private FinancePresenter financePresenter;

    @Autowired
    private FileStorage fileStorage;

    @Test
    void contextLoadsAndWiresCleanArchitectureBeans() {
        assertNotNull(transactionRepository);
        assertNotNull(userRepository);
        assertNotNull(financeUseCase);
        assertNotNull(authUseCase);
        assertNotNull(userUseCase);
        assertNotNull(financePresenter);
        assertNotNull(fileStorage);
    }

    @Test
    void rootAdminIsSeededOnStartup() {
        assertTrue(userRepository.findByUsername("admin").orElseThrow().isAdmin());
    }

    @Test
    void mainDelegatesToSpringApplicationRun() throws Exception {
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(eq(App.class), any(String[].class))).thenReturn(context);

            App.main(new String[]{"--server.port=0"});

            mocked.verify(() -> SpringApplication.run(eq(App.class), any(String[].class)));
        }
    }
}
