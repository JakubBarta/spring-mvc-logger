package cz.jbarta.spring.web.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Jakub Bárta <jakub.barta@gmail.com>
 */
public class Sl4jRequestResponseLoggingFilter extends AbstractRequestResponseLoggingFilter {

    private final static Logger logger = LoggerFactory.getLogger(Sl4jRequestResponseLoggingFilter.class);

    @Override
    protected boolean shouldLog(HttpServletRequest request, HttpServletResponse response) {
        return true;
    }

    @Override
    protected void log(String value) {
        Sl4jRequestResponseLoggingFilter.logger.info(value);
    }
}
