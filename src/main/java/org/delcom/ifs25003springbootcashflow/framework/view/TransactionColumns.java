package org.delcom.ifs25003springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import org.delcom.ifs25003springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25003springbootcashflow.domain.entity.Transaction;

import java.util.Comparator;

/**
 * Kolom grid transaksi yang dipakai bersama oleh FinanceView dan UserTransactionsView.
 */
final class TransactionColumns {

    private TransactionColumns() {
    }

    static void addCommonColumns(Grid<Transaction> grid, FinancePresenter presenter) {
        grid.addColumn(Transaction::getId)
                .setHeader("ID")
                .setKey("id")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Comparator.comparingInt(Transaction::getId));

        grid.addColumn(Transaction::getDescription)
                .setHeader("Keterangan")
                .setKey("description")
                .setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));

        grid.addComponentColumn(transaction -> amountLabel(transaction, presenter))
                .setHeader("Jumlah")
                .setKey("amount")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparingLong(Transaction::getAmount));

        grid.addComponentColumn(transaction -> typeBadge(transaction, presenter))
                .setHeader("Tipe")
                .setKey("type")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::isIncome).reversed());

        grid.addComponentColumn(TransactionColumns::proofThumbnail)
                .setHeader("Bukti")
                .setKey("proof")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        grid.addColumn(presenter::formatCreatedAt)
                .setHeader("Waktu")
                .setKey("createdAt")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt)
                        .thenComparingInt(Transaction::getId));
    }

    static Component amountLabel(Transaction transaction, FinancePresenter presenter) {
        Span amount = new Span(presenter.formatMoney(transaction.getAmount()));
        amount.getStyle()
                .set("font-weight", "600")
                .set("white-space", "nowrap")
                .set("color", transaction.isIncome() ? "#15803D" : "#B91C1C");
        return amount;
    }

    static Component typeBadge(Transaction transaction, FinancePresenter presenter) {
        Span badge = new Span(presenter.formatType(transaction.getType()));
        boolean income = transaction.isIncome();
        badge.getStyle()
                .set("display", "inline-block")
                .set("padding", "0.15rem 0.6rem")
                .set("border-radius", "999px")
                .set("font-size", "0.8rem")
                .set("font-weight", "600")
                .set("white-space", "nowrap")
                .set("background", income ? "#DCFCE7" : "#FEE2E2")
                .set("color", income ? "#166534" : "#991B1B")
                .set("border", "1px solid " + (income ? "#86EFAC" : "#FCA5A5"));
        return badge;
    }

    static Component proofThumbnail(Transaction transaction) {
        if (!transaction.hasProof()) {
            return new Span("-");
        }
        var thumbnail = ImagePreviewDialog.clickableThumbnail(
                "/uploads/" + transaction.getProofPath(),
                "Bukti transaksi #" + transaction.getId(),
                "40px"
        );
        thumbnail.setWidth("40px");
        return thumbnail;
    }
}
