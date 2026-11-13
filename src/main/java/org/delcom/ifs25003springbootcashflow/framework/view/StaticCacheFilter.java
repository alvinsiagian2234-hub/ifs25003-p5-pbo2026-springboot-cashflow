package org.delcom.ifs25003springbootcashflow.framework.view;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import org.springframework.stereotype.Component;

/**
 * Cache panjang untuk file build Vaadin (nama file memakai hash, aman di-cache setahun).
 */
@Component
public class StaticCacheFilter implements Filter {

    private static final String LONG_CACHE = "public, max-age=31536000, immutable";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest req
                && response instanceof HttpServletResponse res
                && req.getRequestURI().startsWith(req.getContextPath() + "/VAADIN/build/")) {
            res.setHeader("Cache-Control", LONG_CACHE);
            chain.doFilter(request, new HttpServletResponseWrapper(res) {
                @Override
                public void setHeader(String name, String value) {
                    if (!isCacheHeader(name)) {
                        super.setHeader(name, value);
                    }
                }

                @Override
                public void addHeader(String name, String value) {
                    if (!isCacheHeader(name)) {
                        super.addHeader(name, value);
                    }
                }

                @Override
                public void setDateHeader(String name, long date) {
                    if (!isCacheHeader(name)) {
                        super.setDateHeader(name, date);
                    }
                }

                @Override
                public void addDateHeader(String name, long date) {
                    if (!isCacheHeader(name)) {
                        super.addDateHeader(name, date);
                    }
                }

                private boolean isCacheHeader(String name) {
                    return "Cache-Control".equalsIgnoreCase(name) || "Expires".equalsIgnoreCase(name);
                }
            });
            return;
        }
        chain.doFilter(request, response);
    }
}