package be.zqsd.nicobot;

import be.zqsd.nicobot.bot.NicoBot;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan
@EnableScheduling
@EnableAsync
@PropertySource("classpath:nicobot.properties")
public class BotMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		ApplicationContext context = new AnnotationConfigApplicationContext(BotMain.class);

		NicoBot session = context.getBean(NicoBot.class);

		session.connect();
	}
}
