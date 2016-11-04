package cz.jbarta.spring.web.logging;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Optional;

/**
 * @author Jakub Bárta <jakub.barta@gmail.com>
 */
public abstract class AbstractRequestResponseLoggingFilter extends OncePerRequestFilter {

    private int maxRequestLength = Integer.MAX_VALUE;
    private int maxResponseLength = 200;

    public void setMaxRequestLength(int maxRequestLength) {
        this.maxRequestLength = maxRequestLength;
    }

    public void setMaxResponseLength(int maxResponseLength) {
        this.maxResponseLength = maxResponseLength;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        HttpServletRequest requestToUse = request;
        HttpServletResponse responseToUse = response;

        if (!(requestToUse instanceof ContentCachingRequestWrapper)) {
            requestToUse = new ContentCachingRequestWrapper(requestToUse);
        }

        if (!(responseToUse instanceof ContentCachingResponseWrapper)) {
            responseToUse = new ContentCachingResponseWrapper(responseToUse);
        }

        filterChain.doFilter(requestToUse, responseToUse);

        if (!shouldLog(requestToUse, responseToUse)) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(requestToUse.getRequestURL()).append(" [")
            .append(requestToUse.getMethod()).append("]: ");

        builder.append("HEADERS: ");
        for (String header : Collections.list(requestToUse.getHeaderNames())) {
            builder.append(header).append(": ").append(requestToUse.getHeader(header));
            builder.append(", ");
        }

        Optional<String> payload = getRequestPayload(requestToUse);

        builder.append("BODY: ").append(payload.orElse(""));

        builder.append("Response: Status ").append(response.getStatus()).append(". ");

        builder.append("HEADERS: ");
        for (String header : response.getHeaderNames()) {
            builder.append(header).append(": ").append(response.getHeader(header));
            builder.append(", ");
        }

        Optional<String> body = getResponsePayload(responseToUse);
        builder.append("BODY: ").append(body.orElse("[empty body]"));

        this.log(builder.toString());
    }

    abstract protected boolean shouldLog(HttpServletRequest request, HttpServletResponse response);
    abstract protected void log(String value);

    private Optional<String> getRequestPayload(HttpServletRequest request) {
        ContentCachingRequestWrapper wrapper =
            WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                int length = Math.min(buf.length, this.maxRequestLength);
                String payload;
                try {
                    payload = new String(buf, 0, length, wrapper.getCharacterEncoding());
                }
                catch (UnsupportedEncodingException ex) {
                    payload = "[unknown]";
                }
                return Optional.of(payload);
            }
        }

        return Optional.empty();
    }

    private Optional<String> getResponsePayload(HttpServletResponse response) {
        ContentCachingResponseWrapper wrapper =
            WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                int length = Math.min(buf.length, this.maxResponseLength);
                String payload;
                try {
                    payload = new String(buf, 0, length, wrapper.getCharacterEncoding());
                }
                catch (UnsupportedEncodingException ex) {
                    payload = "[unknown]";
                }
                return Optional.of(payload);
            }
        }

        return Optional.empty();
    }
}
