package com.antstreaming.console.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.antmedia.console.rest.SupportRequest;
import io.antmedia.console.rest.SupportRestService;
import io.antmedia.settings.ServerSettings;
import io.antmedia.statistic.IStatsCollector;
import io.antmedia.statistic.StatsCollector;

public class SupportRestServiceTest {
	@Before
	public void before() {
	}

	@After
	public void after() {
	}

	/*
	 * Run this test manually. It is not suitable for CI.
	 */
	//@Test
	public void testSendRequest() {
		SupportRestService service = new SupportRestService() {
			public io.antmedia.settings.ServerSettings getServerSettings() {
				ServerSettings ss = new ServerSettings();
				return ss;
			};
			
			@Override
			public IStatsCollector getStatsCollector() {
				StatsCollector sc = new StatsCollector();
				return sc;
			}
		};
		SupportRequest request = new SupportRequest();
		request.setName("Test Test");
		request.setTitle("MyRequest");
		request.setDescription("No problem everything is excellent.");
		request.setEmail("burak@antmedia.io");
		request.setSendSystemInfo(true);

		service.sendSupportRequest(request);
	}
}
