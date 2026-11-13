package org.delcom.ifs25003springbootcashflow.framework.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.delcom.ifs25003springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25003springbootcashflow.domain.entity.BalanceSummary;

/**
 * Kartu ringkasan keuangan: jumlah transaksi, total pemasukan, total pengeluaran, saldo.
 */
final class SummaryCards extends HorizontalLayout {

    private final FinancePresenter presenter;
    private final Span totalCount = new Span("0");
    private final Span incomeTotal = new Span();
    private final Span expenseTotal = new Span();
    private final Span balance = new Span();
    private final Div balanceCard;

    SummaryCards(FinancePresenter presenter) {
        this.presenter = presenter;
        add(
                card("Jumlah Transaksi", totalCount, "#DBEAFE", "#1D4ED8", "#1E40AF"),
                card("Total Pemasukan", incomeTotal, "#DCFCE7", "#16A34A", "#166534"),
                card("Total Pengeluaran", expenseTotal, "#FEE2E2", "#DC2626", "#991B1B")
        );
        balanceCard = card("Saldo", balance, "#EDE9FE", "#7C3AED", "#5B21B6");
        add(balanceCard);
        setWidthFull();
        setSpacing(true);
        getStyle().set("align-items", "stretch").set("flex-wrap", "wrap");
        update(new BalanceSummary(0, 0, 0));
    }

    void update(BalanceSummary summary) {
        totalCount.setText(String.valueOf(summary.totalTransactions()));
        incomeTotal.setText(presenter.formatMoney(summary.totalIncome()));
        expenseTotal.setText(presenter.formatMoney(summary.totalExpense()));
        balance.setText(presenter.formatMoney(summary.balance()));
        balance.getStyle().set("color", summary.balance() < 0 ? "#B91C1C" : "#5B21B6");
    }

    private static Div card(String label, Span value, String background, String accent, String valueColor) {
        Span caption = new Span(label);
        caption.getStyle()
                .set("display", "block")
                .set("font-size", "0.875rem")
                .set("font-weight", "500")
                .set("color", valueColor);

        value.getStyle()
                .set("display", "block")
                .set("margin-top", "0.35rem")
                .set("font-size", "1.35rem")
                .set("font-weight", "700")
                .set("line-height", "1.2")
                .set("color", valueColor)
                .set("word-break", "break-word");

        Div card = new Div(caption, value);
        card.addClassName("summary-card");
        card.getStyle()
                .set("flex", "1 1 200px")
                .set("min-width", "0")
                .set("background", background)
                .set("border", "1px solid " + accent)
                .set("border-left", "6px solid " + accent)
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem")
                .set("box-sizing", "border-box");
        return card;
    }
}