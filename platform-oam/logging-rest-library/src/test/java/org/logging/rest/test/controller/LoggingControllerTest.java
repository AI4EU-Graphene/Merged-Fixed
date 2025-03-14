package org.logging.rest.test.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.lang.invoke.MethodHandles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.acumos.logging.util.ACUMOSLogConstants;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;

import com.logging.rest.example.springbootswagger2.controller.LoggingController;

@RunWith(SpringRunner.class)
public class LoggingControllerTest {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();
	@InjectMocks
	LoggingController loggingController;

	final HttpServletResponse response = new MockHttpServletResponse();
	final HttpServletRequest request = new MockHttpServletRequest();

	@Test
	public void getResponseStatusComplete() {
		assertThat(ACUMOSLogConstants.ResponseStatus.COMPLETED.toString(), is("COMPLETED"));

	}

	@Test
	public void getResponseStatusError() {
		assertThat(ACUMOSLogConstants.ResponseStatus.ERROR.toString(), is("ERROR"));

	}

	@Test
	public void getResponseStatusInProgress() {
		assertThat(ACUMOSLogConstants.ResponseStatus.INPROGRESS.toString(), is("INPROGRESS"));

	}

	@Test
	public void getResponseSeverityInfo() {
		assertThat(ACUMOSLogConstants.ResponseSeverity.INFO.toString(), is("INFO"));

	}

	@Test
	public void getResponseSeverityError() {
		assertThat(ACUMOSLogConstants.ResponseSeverity.ERROR.toString(), is("ERROR"));

	}

	@Test
	public void getResponseSeverityTrace() {
		assertThat(ACUMOSLogConstants.ResponseSeverity.TRACE.toString(), is("TRACE"));

	}

	@Test
	public void getResponseSeverityDebug() {
		assertThat(ACUMOSLogConstants.ResponseSeverity.DEBUG.toString(), is("DEBUG"));

	}

	@Test
	public void getResponseSeverityWarn() {
		assertThat(ACUMOSLogConstants.ResponseSeverity.WARN.toString(), is("WARN"));

	}

	@Test
	public void getResponseSeverityFatal() {
		assertThat(ACUMOSLogConstants.ResponseSeverity.FATAL.toString(), is("FATAL"));

	}

	@Test
	public void getResponseStatusMDC_COMPLETED() {
		assertThat(ACUMOSLogConstants.MDCs.ResponseStatusMDC.MDC_COMPLETED.toString(), is("MDC_COMPLETED"));
	}

	@Test
	public void getResponseStatusMDC_ERROR() {
		assertThat(ACUMOSLogConstants.MDCs.ResponseStatusMDC.MDC_ERROR.toString(), is("MDC_ERROR"));
	}

	@Test
	public void getResponseStatusMDC_INPROGRESS() {
		assertThat(ACUMOSLogConstants.MDCs.ResponseStatusMDC.MDC_INPROGRESS.toString(), is("MDC_INPROGRESS"));
	}

	@Test
	public void getResponseSeverityMDC_INFO() {
		assertThat(ACUMOSLogConstants.MDCs.ResponseSeverityMDC.MDC_INFO.toString(), is("MDC_INFO"));
	}

	@Test
	public void getResponseSeverityMDC_ERROR() {
		assertThat(ACUMOSLogConstants.MDCs.ResponseSeverityMDC.MDC_ERROR.toString(), is("MDC_ERROR"));
	}

	@Test
	public void getResponseSeverityMDC_TRACE() {
		assertThat(ACUMOSLogConstants.MDCs.ResponseSeverityMDC.MDC_TRACE.toString(), is("MDC_TRACE"));
	}

	@Test
	public void getResponseSeverityMDC_DEBUG() {
		assertThat(ACUMOSLogConstants.MDCs.ResponseSeverityMDC.MDC_DEBUG.toString(), is("MDC_DEBUG"));
	}

	@Test
	public void getResponseSeverityMDC_WARN() {
		assertThat(ACUMOSLogConstants.MDCs.ResponseSeverityMDC.MDC_WARN.toString(), is("MDC_WARN"));
	}

	@Test
	public void getResponseSeverityMDC_FATAL() {
		assertThat(ACUMOSLogConstants.MDCs.ResponseSeverityMDC.MDC_FATAL.toString(), is("MDC_FATAL"));
	}

}
