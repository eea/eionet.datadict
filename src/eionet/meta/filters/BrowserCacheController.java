package eionet.meta.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class BrowserCacheController implements Filter {

	private static boolean disableCache;

	public void init(FilterConfig config) throws ServletException {

		disableCache = config.getInitParameter("disableBrowserCache").equalsIgnoreCase("true");

	}

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain fc) throws ServletException, IOException {
		if (disableCache) {
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.setHeader("Pragma", "No-cache");
			httpResponse.setHeader("Cache-Control", "no-cache");
			httpResponse.setHeader("Cache-Control", "no-store");
			httpResponse.setDateHeader("Expires", 0);
		}
		fc.doFilter(request, response);
	}
}
