package be.zqsd.nicobot.job.task;

import be.zqsd.nicobot.internal.job.HappyGeekTimeJobImpl;
import be.zqsd.nicobot.services.LeetGreetingService;
import be.zqsd.nicobot.services.Messages;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
		when(mockMessages.getMessage("congratSoloHGT")).thenReturn("congratSoloHGT");

		task.retrieveCongratulationMessage(false);

		verify(mockMessages).getMessage("congratSoloHGT");
	}

	@Test
	public void retrieveCongratulationMessage_should_return_congratHGT_if_there_is_more_than_one_greeters()
			throws Exception {
		when(mockMessages.getMessage("congratSoloHGT")).thenReturn("congratSoloHGT");
		when(mockMessages.getMessage("congratHGT")).thenReturn("congratHGT");

		String message = task.retrieveCongratulationMessage(true);

		verify(mockMessages).getMessage("congratHGT");
		assertThat(message, is(equalTo("congratHGT")));
	}

	@Test
	public void buildMessageWithNames_should_return_noHGT_if_names_is_null() throws Exception {
		when(mockMessages.getMessage("noHGT")).thenReturn("noHGT");

		assertThat(task.buildMessageWithNames(null), is(equalTo("noHGT")));
	}

}
