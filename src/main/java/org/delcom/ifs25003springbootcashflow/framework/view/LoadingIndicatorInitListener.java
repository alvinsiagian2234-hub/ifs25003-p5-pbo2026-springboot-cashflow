package org.delcom.ifs25003springbootcashflow.framework.view;

import com.vaadin.flow.component.page.LoadingIndicatorConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Konfigurasi indikator loading global, plus landmark dan tampilan awal di HTML
 * agar halaman valid untuk audit aksesibilitas dan cepat terlihat sebelum UI Vaadin dirender.
 */
@Component
public class LoadingIndicatorInitListener implements VaadinServiceInitListener {

    private static final String HIDDEN_STYLE =
            "position:absolute;width:1px;height:1px;margin:-1px;padding:0;"
                    + "overflow:hidden;clip:rect(0,0,0,0);white-space:nowrap;border:0";

    private static final String SPLASH_STYLE =
            "position:fixed;top:0;right:0;bottom:0;left:0;display:flex;align-items:center;"
                    + "justify-content:center;background:#F3F4F6;color:#1F2937;"
                    + "font:600 1.25rem system-ui,-apple-system,'Segoe UI',Roboto,sans-serif;"
                    + "z-index:2147483000";

    /** Dijalankan langsung di HTML awal: tandai indikator koneksi dan pengaman hapus tampilan awal. */
    private static final String EARLY_SCRIPT =
            "(function(){"
                    + "var mark=function(){"
                    + "var l=document.querySelectorAll('vaadin-connection-indicator');"
                    + "l.forEach(function(e){e.setAttribute('role','status');e.setAttribute('aria-live','polite');});"
                    + "return l.length>0;};"
                    + "if(!mark()){"
                    + "var o=new MutationObserver(function(){if(mark()){o.disconnect();}});"
                    + "o.observe(document.documentElement,{childList:true,subtree:true});"
                    + "}"
                    + "setTimeout(function(){var m=document.getElementById('initial-landmark');"
                    + "if(m){m.remove();}},15000);"
                    + "})();";

    /** Dijalankan setelah UI Vaadin siap: tampilan awal dihapus karena view punya landmark sendiri. */
    private static final String UI_READY_SCRIPT =
            "var p=document.getElementById('initial-landmark');"
                    + "if(p){p.remove();}"
                    + "document.querySelectorAll('vaadin-connection-indicator').forEach(function(e){"
                    + "e.setAttribute('role','status');e.setAttribute('aria-live','polite');});";

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.addIndexHtmlRequestListener(response -> {
            Element body = response.getDocument().body();
            Element main = body.prependElement("main").attr("id", "initial-landmark");
            main.appendElement("h1")
                    .attr("style", HIDDEN_STYLE)
                    .text("Cashflow - Catatan Keuangan Pribadi");
            main.appendElement("div")
                    .attr("style", SPLASH_STYLE)
                    .appendElement("span")
                    .text("Memuat Cashflow...");
            body.prependElement("script").appendChild(new DataNode(EARLY_SCRIPT));
        });

        event.getSource().addUIInitListener(uiEvent -> {
            LoadingIndicatorConfiguration conf = uiEvent.getUI().getLoadingIndicatorConfiguration();
            conf.setApplyDefaultTheme(true);
            conf.setFirstDelay(150);
            conf.setSecondDelay(600);
            conf.setThirdDelay(1500);

            uiEvent.getUI().getPage().executeJs(UI_READY_SCRIPT);
        });
    }
}