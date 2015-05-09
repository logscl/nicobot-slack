package com.st.nicobot.job.task;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.st.nicobot.internal.job.HappyGeekTimeJobImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.st.nicobot.services.LeetGreetingService;
import com.st.nicobot.services.Messages;

/**
 * @author jlamby
 *
 */
public class HappyGeekTimeTaskTest {

	@InjectMocks
	private HappyGeekTimeJobImpl task;

	@Mock
	private Messages mockMessages;

	@Mock
	private LeetGreetingService mockGreetingService;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void retrieveCongratulationMessage_should_return_congratSoloHGT_if_there_is_no_more_than_one_greeters()
			throws Exception {
		when(mockMessages.getOtherMessage("congratSoloHGT")).thenReturn("congratSoloHGT");

		task.retrieveCongratulationMessage(false);

		verify(mockMessages).getOtherMessage("congratSoloHGT");
	}

	@Test
	public void retrieveCongratulationMessage_should_return_congratHGT_if_there_is_more_than_one_greeters()
			throws Exception {
		when(mockMessages.getOtherMessage("congratSoloHGT")).thenReturn("congratSoloHGT");
		when(mockMessages.getOtherMessage("congratHGT")).thenReturn("congratHGT");

		String message = task.retrieveCongratulationMessage(true);

		verify(mockMessages).getOtherMessage("congratHGT");
		assertThat(message, is(equalTo("congratHGT")));
	}

	@Test
	public void buildMessageWithNames_should_return_noHGT_if_names_is_null() throws Exception {
		when(mockMessages.getOtherMessage("noHGT")).thenReturn("noHGT");

		assertThat(task.buildMessageWithNames(null), is(equalTo("noHGT")));
	}

}
