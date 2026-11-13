package org.delcom.ifs25003springbootcashflow.framework.view;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Menjamin HTML awal sudah memiliki title dan meta description,
 * sebelum Vaadin selesai merender view di sisi browser.
 */
@Component
public class PageShellInitListener implements VaadinServiceInitListener {

    private static final String DEFAULT_TITLE = "Cashflow - Catatan Keuangan Pribadi";
    private static final String DEFAULT_DESCRIPTION =
            "Aplikasi Cashflow untuk mencatat pemasukan dan pengeluaran, "
                    + "memantau saldo, dan menyimpan bukti transaksi.";

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.addIndexHtmlRequestListener(response -> {
            Document document = response.getDocument();
            Element head = document.head();

            Element title = head.selectFirst("title");
            if (title == null) {
                head.appendElement("title").text(DEFAULT_TITLE);
            } else if (title.text().isBlank()) {
                title.text(DEFAULT_TITLE);
            }

            if (head.selectFirst("meta[name=description]") == null) {
                head.appendElement("meta")
                        .attr("name", "description")
                        .attr("content", DEFAULT_DESCRIPTION);
            }
        });
    }
}