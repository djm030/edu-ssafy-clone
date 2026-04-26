package com.edussafy.backend.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestCorrelationFilterTest {

    private final RequestCorrelationFilter filter = new RequestCorrelationFilter();

    @Test
    void propagatesSafeRequestIdToResponseAttributeAndMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(RequestCorrelationFilter.REQUEST_ID_HEADER, "req-demo-123");

        filter.doFilter(request, response, assertingChain("req-demo-123"));

        assertThat(response.getHeader(RequestCorrelationFilter.REQUEST_ID_HEADER)).isEqualTo("req-demo-123");
        assertThat(request.getAttribute(RequestCorrelationFilter.REQUEST_ID_ATTRIBUTE)).isEqualTo("req-demo-123");
        assertThat(MDC.get("requestId")).isNull();
    }

    @Test
    void replacesUnsafeRequestIdHeaderBeforeLogging() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(RequestCorrelationFilter.REQUEST_ID_HEADER, "../../bad request id");

        filter.doFilter(request, response, (ServletRequest servletRequest, ServletResponse servletResponse) -> {
            String requestId = (String) servletRequest.getAttribute(RequestCorrelationFilter.REQUEST_ID_ATTRIBUTE);

            assertThat(requestId).isNotBlank();
            assertThat(requestId).isNotEqualTo("../../bad request id");
            assertThat(requestId).matches("[A-Za-z0-9._:-]{1,128}");
            assertThat(MDC.get("requestId")).isEqualTo(requestId);
        });

        assertThat(response.getHeader(RequestCorrelationFilter.REQUEST_ID_HEADER)).matches("[A-Za-z0-9._:-]{1,128}");
        assertThat(MDC.get("requestId")).isNull();
    }

    private FilterChain assertingChain(String expectedRequestId) {
        return (ServletRequest servletRequest, ServletResponse servletResponse) -> {
            assertThat(servletRequest.getAttribute(RequestCorrelationFilter.REQUEST_ID_ATTRIBUTE)).isEqualTo(expectedRequestId);
            assertThat(MDC.get("requestId")).isEqualTo(expectedRequestId);
        };
    }
}
