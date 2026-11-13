package org.delcom.ifs25003springbootcashflow.framework.view;

import com.vaadin.flow.component.page.LoadingIndicatorConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/**
 * Konfigurasi indikator loading global untuk tindakan yang memerlukan waktu tunggu.
 */
@Component
public class LoadingIndicatorInitListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            LoadingIndicatorConfiguration conf = uiEvent.getUI().getLoadingIndicatorConfiguration();
            conf.setApplyDefaultTheme(true);
            conf.setFirstDelay(150);
            conf.setSecondDelay(600);
            conf.setThirdDelay(1500);

            // Tandai indikator koneksi Vaadin sebagai live region agar tidak
            // dianggap konten di luar landmark oleh Axe (aturan "region").
            uiEvent.getUI().getPage().executeJs(
                    "const mark = () => document.querySelectorAll('vaadin-connection-indicator')" +
                            ".forEach(el => { el.setAttribute('role', 'status'); el.setAttribute('aria-live', 'polite'); });" +
                            "mark();" +
                            "new MutationObserver(mark).observe(document.body, { childList: true });");
        });
    }
}