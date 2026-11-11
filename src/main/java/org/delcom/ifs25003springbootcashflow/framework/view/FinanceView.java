package org.delcom.ifs25003springbootcashflow.framework.view;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import org.delcom.ifs25003springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25003springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25003springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25003springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25003springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25003springbootcashflow.domain.entity.User;
import org.delcom.ifs25003springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25003springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25003springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25003springbootcashflow.usecase.FinanceUseCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Dashboard keuangan pribadi pengguna yang sedang login:
 * kartu ringkasan saldo, toolbar (cari, urut, muat ulang, tambah), dan grid transaksi.
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Cashflow")
public class FinanceView extends VerticalLayout {

    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;
    private final FileStorage fileStorage;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final Select<SortOption> sortSelect = new Select<>();
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span("- Data transaksi belum tersedia!");
    private final SummaryCards summaryCards;

    public FinanceView(FinanceUseCase financeUseCase, FinancePresenter presenter, FileStorage fileStorage) {
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;
        this.fileStorage = fileStorage;
        this.summaryCards = new SummaryCards(presenter);

        setSizeFull();
        setPadding(true);
        setSpacing(false);
        getStyle().set("background", "#F3F4F6");

        H1 title = new H1("Keuangan Saya");
        title.getStyle().set("margin", "0");
        Paragraph subtitle = new Paragraph("Catat pemasukan dan pengeluaran, lalu pantau saldo kamu.");
        subtitle.getStyle().set("margin", "0").set("color", "#4B5563");
        statusLabel.getStyle().set("margin", "0").set("color", "#374151").set("font-weight", "500");

        configureGrid();
        Div card = PageCard.wrap(title, subtitle, summaryCards, buildToolbar(), statusLabel, grid);
        add(card);
        expand(card);
        refreshAll();
    }

    private User requireUser() {
        return AuthSession.currentUser().orElseThrow();
    }

    private void configureGrid() {
        TransactionColumns.addCommonColumns(grid, presenter);
        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Tindakan")
                .setKey("actions")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        grid.setEmptyStateComponent(emptyState);
        grid.addItemDoubleClickListener(event -> openEditDialog(event.getItem()));
        grid.setSizeFull();
        grid.setMinHeight("320px");
        GridStyles.apply(grid);
    }

    private HorizontalLayout createActionButtons(Transaction transaction) {
        Button editButton = new Button("Ubah", VaadinIcon.EDIT.create(), event -> openEditDialog(transaction));
        editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        editButton.getStyle().set("cursor", "pointer");

        Button deleteButton = new Button("Hapus", VaadinIcon.TRASH.create(), event -> openDeleteDialog(transaction));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        deleteButton.getStyle().set("cursor", "pointer");

        HorizontalLayout actions = new HorizontalLayout(editButton, deleteButton);
        actions.setSpacing(true);
        return actions;
    }

    private Div buildToolbar() {
        searchField.setPlaceholder("Cari keterangan...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setMinWidth("220px");
        searchField.addKeyDownListener(Key.ENTER, event -> applyFilter());
        searchField.addValueChangeListener(event -> {
            if (event.isFromClient() && event.getValue().isEmpty()) {
                applyFilter();
            }
        });

        sortSelect.setPlaceholder("Urutkan...");
        sortSelect.setItems(SortOption.values());
        // Item kosong (tanpa urutan) dikirim sebagai null ke label generator.
        sortSelect.setItemLabelGenerator(option -> option == null ? "Urutan default (ID)" : option.label());
        sortSelect.setEmptySelectionAllowed(true);
        sortSelect.setEmptySelectionCaption("Urutan default (ID)");
        sortSelect.setWidth("200px");
        sortSelect.addValueChangeListener(event -> applyFilter());

        Button searchButton = new Button("Cari", VaadinIcon.SEARCH.create(), event -> applyFilter());
        searchButton.getStyle().set("cursor", "pointer");

        Button refreshButton = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), event -> refreshAll());
        refreshButton.getStyle().set("cursor", "pointer");

        Button addIncomeButton = new Button("Tambah Pemasukan", VaadinIcon.PLUS_CIRCLE.create(),
                event -> openAddDialog(TransactionType.INCOME));
        addIncomeButton.setId("add-income");
        addIncomeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        addIncomeButton.getStyle().set("cursor", "pointer");

        Button addExpenseButton = new Button("Tambah Pengeluaran", VaadinIcon.MINUS_CIRCLE.create(),
                event -> openAddDialog(TransactionType.EXPENSE));
        addExpenseButton.setId("add-expense");
        addExpenseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        addExpenseButton.getStyle().set("cursor", "pointer");

        HorizontalLayout left = new HorizontalLayout(searchField, searchButton, sortSelect, refreshButton);
        left.setAlignItems(FlexComponent.Alignment.BASELINE);
        left.getStyle().set("flex-wrap", "wrap").set("flex", "1 1 auto");
        left.setFlexGrow(1, searchField);

        HorizontalLayout right = new HorizontalLayout(addIncomeButton, addExpenseButton);
        right.getStyle().set("flex-wrap", "wrap");

        Div toolbar = new Div(left, right);
        toolbar.setWidthFull();
        toolbar.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "0.75rem")
                .set("align-items", "baseline")
                .set("justify-content", "space-between");
        return toolbar;
    }

    private void openAddDialog(TransactionType type) {
        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setId("tx-description");
        descriptionField.setWidthFull();
        descriptionField.setRequiredIndicatorVisible(true);

        TextField amountField = new TextField("Jumlah (Rp)");
        amountField.setId("tx-amount");
        amountField.setPlaceholder("contoh: 1000000 atau 1.000.000");
        amountField.setPrefixComponent(new Span("Rp"));
        amountField.setWidthFull();
        amountField.setRequiredIndicatorVisible(true);

        AtomicReference<String> pendingProofName = new AtomicReference<>();
        AtomicReference<byte[]> pendingProofData = new AtomicReference<>();
        Upload upload = imageUpload(pendingProofName, pendingProofData);
        Span uploadHint = hint("Bukti transaksi (opsional, gambar PNG/JPG/WEBP)");

        Dialog dialog = new Dialog();
        ProgressBar loadingBar = new ProgressBar();

        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create());
        saveButton.setId("tx-save");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(event -> {
            loadingBar.setVisible(true);
            saveButton.setEnabled(false);
            try {
                User user = requireUser();
                Transaction created = type == TransactionType.INCOME
                        ? financeUseCase.addIncome(user.getId(), descriptionField.getValue(), amountField.getValue())
                        : financeUseCase.addExpense(user.getId(), descriptionField.getValue(), amountField.getValue());
                String filename = pendingProofName.get();
                byte[] data = pendingProofData.get();
                if (filename != null && !filename.isBlank() && data != null) {
                    String path = fileStorage.store(FileStorage.PROOFS, filename, new ByteArrayInputStream(data));
                    created = financeUseCase.updateTransactionForUser(
                            user.getId(), created.getId(), null, null, null, path, false);
                }
                showSuccess(presenter.showAddSuccess(created));
                dialog.close();
                refreshAll();
            } catch (ValidationException | EntityNotFoundException | IOException ex) {
                loadingBar.setVisible(false);
                saveButton.setEnabled(true);
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        VerticalLayout body = new VerticalLayout(descriptionField, amountField, uploadHint, upload);
        body.setPadding(false);
        body.setSpacing(true);

        String label = presenter.formatType(type);
        AppDialog.build(dialog, "Tambah " + label, label, loadingBar, body, cancelButton, saveButton);
        dialog.open();
        descriptionField.focus();
    }

    private void openEditDialog(Transaction transaction) {
        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setValue(transaction.getDescription());
        descriptionField.setWidthFull();

        TextField amountField = new TextField("Jumlah (Rp)");
        amountField.setValue(String.valueOf(transaction.getAmount()));
        amountField.setPrefixComponent(new Span("Rp"));
        amountField.setWidthFull();

        Select<TransactionType> typeSelect = new Select<>();
        typeSelect.setLabel("Tipe");
        typeSelect.setItems(TransactionType.values());
        typeSelect.setItemLabelGenerator(type -> type == null ? "-" : presenter.formatType(type));
        typeSelect.setValue(transaction.getType());
        typeSelect.setWidthFull();

        AtomicReference<String> pendingProofName = new AtomicReference<>();
        AtomicReference<byte[]> pendingProofData = new AtomicReference<>();
        Upload upload = imageUpload(pendingProofName, pendingProofData);
        Span uploadHint = hint(transaction.hasProof() ? "Ganti bukti (opsional)" : "Tambah bukti (opsional)");

        Checkbox clearProof = new Checkbox("Hapus bukti saat ini");
        clearProof.setVisible(transaction.hasProof());

        VerticalLayout body = new VerticalLayout(descriptionField, amountField, typeSelect);
        if (transaction.hasProof()) {
            body.add(ImagePreviewDialog.clickableThumbnail(
                    "/uploads/" + transaction.getProofPath(), "Bukti transaksi #" + transaction.getId(), "80px"));
        }
        body.add(clearProof, uploadHint, upload);
        body.setPadding(false);
        body.setSpacing(true);

        Dialog dialog = new Dialog();
        ProgressBar loadingBar = new ProgressBar();

        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(event -> {
            loadingBar.setVisible(true);
            saveButton.setEnabled(false);
            try {
                User user = requireUser();
                String proofPath = null;
                String filename = pendingProofName.get();
                byte[] data = pendingProofData.get();
                if (filename != null && !filename.isBlank() && data != null) {
                    proofPath = fileStorage.store(FileStorage.PROOFS, filename, new ByteArrayInputStream(data));
                }
                financeUseCase.updateTransactionForUser(
                        user.getId(),
                        transaction.getId(),
                        descriptionField.getValue(),
                        amountField.getValue(),
                        typeSelect.getValue(),
                        proofPath,
                        proofPath == null && clearProof.getValue()
                );
                showSuccess(presenter.showUpdateSuccess());
                dialog.close();
                refreshAll();
            } catch (EntityNotFoundException ex) {
                loadingBar.setVisible(false);
                saveButton.setEnabled(true);
                showError(presenter.showUpdateFailed(ex.getId()));
            } catch (ValidationException | IOException ex) {
                loadingBar.setVisible(false);
                saveButton.setEnabled(true);
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        AppDialog.build(dialog, "Ubah Transaksi #" + transaction.getId(), "Edit", loadingBar, body,
                cancelButton, saveButton);
        dialog.open();
    }

    private void openDeleteDialog(Transaction transaction) {
        Paragraph message = new Paragraph("Yakin ingin menghapus transaksi berikut?");
        message.getStyle().set("margin", "0 0 0.5rem 0");
        Paragraph detail = new Paragraph(presenter.format(transaction));
        detail.getStyle()
                .set("margin", "0")
                .set("padding", "0.6rem 0.8rem")
                .set("background", "#F9FAFB")
                .set("border", "1px solid #E5E7EB")
                .set("border-radius", "8px")
                .set("font-family", "monospace");
        Paragraph warning = new Paragraph("Saldo akan dihitung ulang setelah transaksi dihapus.");
        warning.getStyle().set("margin", "0.5rem 0 0 0").set("color", "#B91C1C").set("font-size", "0.875rem");
        VerticalLayout body = new VerticalLayout(message, detail, warning);
        body.setPadding(false);
        body.setSpacing(false);

        Dialog dialog = new Dialog();
        ProgressBar loadingBar = new ProgressBar();

        Button confirmButton = new Button("Hapus", VaadinIcon.TRASH.create());
        confirmButton.setId("tx-delete-confirm");
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        confirmButton.addClickListener(event -> {
            loadingBar.setVisible(true);
            confirmButton.setEnabled(false);
            deleteTransaction(transaction);
            dialog.close();
        });

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        AppDialog.build(dialog, "Hapus Transaksi #" + transaction.getId(), "Konfirmasi", loadingBar, body,
                cancelButton, confirmButton);
        dialog.open();
    }

    private void deleteTransaction(Transaction transaction) {
        try {
            financeUseCase.removeTransactionForUser(requireUser().getId(), transaction.getId());
            showSuccess(presenter.showRemoveSuccess());
            refreshAll();
        } catch (EntityNotFoundException ex) {
            showError(presenter.showRemoveFailed(ex.getId()));
            refreshAll();
        } catch (ValidationException ex) {
            showError(presenter.showValidationError(ex.getMessage()));
        }
    }

    private void applyFilter() {
        String keyword = searchField.getValue();
        SortOption option = sortSelect.getValue();
        List<Transaction> results = financeUseCase.findTransactions(requireUser().getId(), keyword, option);

        String title;
        if (keyword == null || keyword.isBlank()) {
            title = option == null ? "Daftar Transaksi" : "Daftar Transaksi (Terurut: " + option.label() + ")";
            emptyState.setText("- Data transaksi belum tersedia!");
        } else {
            title = "Hasil Pencarian: \"" + keyword.trim() + "\"";
            emptyState.setText("- Transaksi tidak ditemukan!");
        }
        refreshGrid(results, title);
    }

    private void refreshAll() {
        searchField.clear();
        applyFilter();
    }

    private void refreshGrid(List<Transaction> transactions, String title) {
        grid.setItems(transactions);
        summaryCards.update(financeUseCase.getSummary(requireUser().getId()));
        if (transactions.isEmpty()) {
            statusLabel.setText(title + " — " + emptyState.getText().replaceFirst("^- ", ""));
        } else {
            statusLabel.setText(title + " (" + transactions.size() + " transaksi)");
        }
    }

    private Upload imageUpload(AtomicReference<String> pendingName, AtomicReference<byte[]> pendingData) {
        Upload upload = new Upload(UploadHandler.inMemory((metadata, data) -> {
            pendingName.set(metadata.fileName());
            pendingData.set(data);
        }));
        upload.setAcceptedMimeTypes("image/png", "image/jpeg", "image/webp");
        upload.setAcceptedFileExtensions(".png", ".jpg", ".jpeg", ".webp");
        upload.setMaxFiles(1);
        upload.setWidthFull();
        upload.addFileRemovedListener(event -> {
            pendingName.set(null);
            pendingData.set(null);
        });
        return upload;
    }

    private static Span hint(String text) {
        Span hint = new Span(text);
        hint.getStyle().set("font-size", "0.875rem").set("color", "#4B5563").set("font-weight", "500");
        return hint;
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3500, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4500, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
