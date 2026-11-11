package org.delcom.ifs25003springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.progressbar.ProgressBar;

/**
 * Pembangun dialog modal yang konsisten: header primer (#2563EB) + teks putih + badge,
 * progress bar loading, body putih, dan footer abu-abu (#F3F4F6) dengan tombol di kanan.
 */
final class AppDialog {

    static final String HEADER_COLOR = "#2563EB";
    static final String FOOTER_COLOR = "#F3F4F6";

    private AppDialog() {
    }

    static void build(
            Dialog dialog,
            String title,
            String badgeText,
            ProgressBar loadingBar,
            Component bodyContent,
            Button... footerButtons
    ) {
        dialog.setWidth("520px");
        dialog.setMaxWidth("95vw");
        dialog.setResizable(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.addClassName("app-form-dialog");

        Span headerTitle = new Span(title);
        headerTitle.getStyle()
                .set("margin", "0")
                .set("color", "#FFFFFF")
                .set("font-size", "1.125rem")
                .set("font-weight", "600")
                .set("line-height", "1.4");

        Span badge = new Span(badgeText);
        badge.addClassName("dialog-badge");
        badge.getStyle()
                .set("background", "rgba(255, 255, 255, 0.18)")
                .set("border", "1px solid rgba(255, 255, 255, 0.45)")
                .set("color", "#FFFFFF")
                .set("font-size", "0.75rem")
                .set("font-weight", "600")
                .set("padding", "0.15rem 0.6rem")
                .set("border-radius", "999px")
                .set("white-space", "nowrap");

        Div header = new Div(headerTitle, badge);
        header.addClassName("dialog-header");
        header.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "space-between")
                .set("gap", "0.75rem")
                .set("background", HEADER_COLOR)
                .set("padding", "1rem 1.5rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");

        loadingBar.setIndeterminate(true);
        loadingBar.setVisible(false);
        loadingBar.getStyle().set("margin", "0").set("height", "4px");

        Div body = new Div(bodyContent);
        body.getStyle()
                .set("background", "#FFFFFF")
                .set("padding", "1.25rem 1.5rem")
                .set("box-sizing", "border-box")
                .set("max-height", "65vh")
                .set("overflow", "auto")
                .set("width", "100%");

        Div footer = new Div();
        footer.addClassName("dialog-footer");
        footer.getStyle()
                .set("display", "flex")
                .set("justify-content", "flex-end")
                .set("align-items", "center")
                .set("gap", "0.75rem")
                .set("background", FOOTER_COLOR)
                .set("border-top", "1px solid #E5E7EB")
                .set("padding", "0.85rem 1.5rem 1.1rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");
        for (Button button : footerButtons) {
            button.getStyle().set("cursor", "pointer");
        }
        footer.add(footerButtons);

        Div shell = new Div(header, loadingBar, body, footer);
        shell.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("width", "100%")
                .set("overflow", "hidden")
                .set("border-radius", "12px")
                .set("background", "#FFFFFF");

        dialog.removeAll();
        dialog.add(shell);
        removeOverlayPadding(dialog);
    }

    /** Menghilangkan padding bawaan overlay agar header/footer menempel ke tepi dialog. */
    static void removeOverlayPadding(Dialog dialog) {
        dialog.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                dialog.getElement().executeJs("""
                        const overlay = this.$.overlay;
                        if (!overlay || !overlay.shadowRoot) {
                          return;
                        }
                        const content = overlay.shadowRoot.querySelector('[part="content"]');
                        if (content) {
                          content.style.padding = '0';
                          content.style.overflow = 'visible';
                          content.style.maxHeight = 'none';
                        }
                        const overlayPart = overlay.shadowRoot.querySelector('[part="overlay"]');
                        if (overlayPart) {
                          overlayPart.style.padding = '0';
                          overlayPart.style.overflow = 'hidden';
                        }
                        """);
            }
        });
    }
}
