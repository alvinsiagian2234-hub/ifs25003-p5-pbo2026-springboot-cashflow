package org.delcom.ifs25003springbootcashflow.framework.view;

import com.vaadin.flow.component.page.LoadingIndicatorConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Konfigurasi indikator loading global, plus landmark awal di HTML agar
 * halaman tetap valid untuk audit aksesibilitas sebelum UI Vaadin selesai dirender.
 */
@Component
public class LoadingIndicatorInitListener implements VaadinServiceInitListener {

    private static final String HIDDEN_STYLE =
            "position:absolute;width:1px;height:1px;margin:-1px;padding:0;"
                    + "overflow:hidden;clip:rect(0,0,0,0);white-space:nowrap;border:0";

    /** Dijalankan langsung di HTML awal: tandai indikator koneksi begitu dibuat Vaadin. */
    private static final String EARLY_SCRIPT =
            "(function(){"
                    + "var mark=function(){"
                    + "var l=document.querySelectorAll('vaadin-connection-indicator');"
                    + "l.forEach(function(e){e.setAttribute('role','status');e.setAttribute('aria-live','polite');});"
                    + "return l.length>0;};"
                    + "if(!mark()){"
                    + "var o=new MutationObserver(function(){if(mark()){o.disconnect();}});"
                    + "o.observe(document.documentElement,{childList:true,subtree:true});"
                    + "}})();";

    /** Dijalankan setelah UI Vaadin siap: placeholder dihapus karena view punya landmark sendiri. */
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