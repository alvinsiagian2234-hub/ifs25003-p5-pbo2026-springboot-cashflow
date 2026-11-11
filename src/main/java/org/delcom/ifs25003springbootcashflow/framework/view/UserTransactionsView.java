package org.delcom.ifs25003springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25003springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25003springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25003springbootcashflow.domain.entity.User;
import org.delcom.ifs25003springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25003springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25003springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25003springbootcashflow.usecase.UserUseCase;

import java.util.List;

/**
 * (ADMIN) Peninjauan transaksi milik pengguna lain secara read-only.
 * Route: /user-transactions/{userId}
 */
@Route(value = "user-transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna")
public class UserTransactionsView extends VerticalLayout
        implements HasUrlParameter<Integer>, BeforeEnterObserver {

    private final UserUseCase userUseCase;
    private final FinanceUseCase financeUseCase;
    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final H1 title = new H1("Transaksi Pengguna");
    private final Paragraph info = new Paragraph("Mode baca saja (read-only).");
    private final Span emptyState = new Span("- Pengguna ini belum memiliki transaksi.");
    private final SummaryCards summaryCards;

    public UserTransactionsView(UserUseCase userUseCase, FinanceUseCase financeUseCase, FinancePresenter presenter) {
        this.userUseCase = userUseCase;
        this.financeUseCase = financeUseCase;
        this.summaryCards = new SummaryCards(presenter);

        setSizeFull();
        setPadding(true);
        setSpacing(false);
        getStyle().set("background", "#F3F4F6");

        title.getStyle().set("margin", "0");
        info.getStyle().set("margin", "0").set("color", "#4B5563");

        Button back = new Button("Kembali ke Daftar Pengguna", VaadinIcon.ARROW_LEFT.create(),
                event -> UI.getCurrent().navigate(UsersView.class));
        back.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        back.getStyle().set("cursor", "pointer").set("align-self", "flex-start");

        TransactionColumns.addCommonColumns(grid, presenter);
        grid.setEmptyStateComponent(emptyState);
        grid.setSizeFull();
        grid.setMinHeight("300px");
        GridStyles.apply(grid);

        Div card = PageCard.wrap(back, title, info, summaryCards, grid);
        add(card);
        expand(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User current = AuthSession.currentUser().orElse(null);
        if (current == null || !current.isAdmin()) {
            event.forwardTo(FinanceView.class);
        }
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Integer userId) {
        User current = AuthSession.currentUser().orElse(null);
        if (current == null || !current.isAdmin()) {
            return;
        }
        if (userId == null) {
            event.forwardTo(UsersView.class);
            return;
        }
        try {
            User owner = userUseCase.getUser(userId);
            title.setText("Transaksi milik " + owner.getFullName() + " (@" + owner.getUsername() + ")");
            List<Transaction> transactions = financeUseCase.getTransactionsByUser(userId);
            grid.setItems(transactions);
            summaryCards.update(financeUseCase.getSummary(userId));
        } catch (EntityNotFoundException ex) {
            event.forwardTo(UsersView.class);
        }
    }
}
