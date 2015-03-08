package com.st.nicobot;

import com.st.nicobot.services.Nicobot;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;

/**
 * Created by Logs on 08-03-15.
 */
@Configuration
@ComponentScan
@PropertySources({@PropertySource("classpath:nicobot.properties"), @PropertySource("classpath:slack.properties")})
public class BotMainApplication {


    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(BotMainApplication.class);

        Nicobot session = context.getBean(Nicobot.class);

        session.connect();
    }
}
