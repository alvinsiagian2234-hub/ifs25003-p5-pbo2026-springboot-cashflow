package org.delcom.ifs25003springbootcashflow.adapter.repository;

import org.delcom.ifs25003springbootcashflow.domain.entity.Role;
import org.delcom.ifs25003springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25003springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25003springbootcashflow.domain.entity.User;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Jalur gagal JDBC: SQLException dibungkus menjadi IllegalStateException.
 */
class JdbcFailureTest {

    @Test
    void schemaInitializerFailsWhenConnectionBroken() throws Exception {
        assertThrows(IllegalStateException.class, () -> SchemaInitializer.initialize(failingDataSource()));
    }

    @Test
    void userRepositoryPropagatesSqlFailures() throws Exception {
        JdbcUserRepository repository = new JdbcUserRepository(failingDataSource());

        assertThrows(IllegalStateException.class, repository::findAll);
        assertThrows(IllegalStateException.class, () -> repository.findById(1));
        assertThrows(IllegalStateException.class, () -> repository.findByUsername("x"));
        assertThrows(IllegalStateException.class, () -> repository.saveNew("u", "h", "n", Role.USER));
        assertThrows(IllegalStateException.class, () -> repository.deleteById(1));
        assertThrows(IllegalStateException.class, () ->
                repository.update(new User(1, "u", "h", "n", Role.USER, null)));
    }

    @Test
    void userSaveNewFailsWhenNoGeneratedKey() throws Exception {
        JdbcUserRepository repository = new JdbcUserRepository(noGeneratedKeyDataSource());
        assertThrows(IllegalStateException.class, () -> repository.saveNew("u", "h", "n", Role.USER));
    }

    @Test
    void transactionRepositoryPropagatesSqlFailures() throws Exception {
        JdbcTransactionRepository repository = new JdbcTransactionRepository(failingDataSource());
        Transaction transaction = new Transaction(1, 1, "t", 10L, TransactionType.INCOME);

        assertThrows(IllegalStateException.class, repository::findAll);
        assertThrows(IllegalStateException.class, () -> repository.findByUserId(1));
        assertThrows(IllegalStateException.class, () -> repository.findById(1));
        assertThrows(IllegalStateException.class, () -> repository.save(1, "t", 10L, TransactionType.EXPENSE));
        assertThrows(IllegalStateException.class, () -> repository.deleteById(1));
        assertThrows(IllegalStateException.class, () -> repository.update(transaction));
        assertThrows(IllegalStateException.class, () -> repository.findBy(t -> true));
    }

    @Test
    void transactionSaveFailsWhenNoGeneratedKey() throws Exception {
        JdbcTransactionRepository repository = new JdbcTransactionRepository(noGeneratedKeyDataSource());
        assertThrows(IllegalStateException.class, () -> repository.save(1, "t", 10L, TransactionType.INCOME));
    }

    @Test
    void userFindByIdSwallowsCloseFailures() throws Exception {
        JdbcUserRepository repository = new JdbcUserRepository(queryWithCloseFailure(false));
        assertTrue(repository.findById(1).isEmpty());
    }

    @Test
    void userFindByUsernameSwallowsCloseFailures() throws Exception {
        JdbcUserRepository repository = new JdbcUserRepository(queryWithCloseFailure(false));
        assertTrue(repository.findByUsername("andi").isEmpty());
    }

    @Test
    void userFindByIdPropagatesBodyFailureDespiteCloseFailures() throws Exception {
        JdbcUserRepository repository = new JdbcUserRepository(queryWithCloseFailure(true));
        assertThrows(IllegalStateException.class, () -> repository.findById(1));
    }

    @Test
    void transactionFindByIdSwallowsCloseFailures() throws Exception {
        JdbcTransactionRepository repository = new JdbcTransactionRepository(queryWithCloseFailure(false));
        assertTrue(repository.findById(1).isEmpty());
    }

    @Test
    void transactionFindByIdPropagatesBodyFailureDespiteCloseFailures() throws Exception {
        JdbcTransactionRepository repository = new JdbcTransactionRepository(queryWithCloseFailure(true));
        assertThrows(IllegalStateException.class, () -> repository.findById(1));
    }

    private DataSource failingDataSource() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("down"));
        return dataSource;
    }

    private DataSource noGeneratedKeyDataSource() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet keys = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);
        when(statement.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(false);
        return dataSource;
    }

    /**
     * Paksa kegagalan close() (dan opsional kegagalan body) agar jalur closeQuietly teruji.
     */
    private DataSource queryWithCloseFailure(boolean failBody) throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(rs);
        if (failBody) {
            when(rs.next()).thenThrow(new SQLException("body"));
        } else {
            when(rs.next()).thenReturn(false);
        }
        doThrow(new SQLException("rs-close")).when(rs).close();
        doThrow(new SQLException("ps-close")).when(statement).close();
        doThrow(new SQLException("conn-close")).when(connection).close();
        return dataSource;
    }
}
