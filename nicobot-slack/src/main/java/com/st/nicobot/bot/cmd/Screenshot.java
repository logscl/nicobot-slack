package com.st.nicobot.bot.cmd;

import com.moodysalem.phantomjs.wrapper.PhantomJS;
import com.moodysalem.phantomjs.wrapper.beans.RenderOptions;
import com.moodysalem.phantomjs.wrapper.enums.RenderFormat;
import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.utils.Option;
import com.st.nicobot.services.Messages;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.HttpHeaders;
import java.io.InputStream;

/**
 * Created by Logs on 10-09-16.
 */
@Service
public class Screenshot extends NiCommand {

    private static Logger logger = LoggerFactory.getLogger(Screenshot.class);

    @Autowired
    private NicoBot nicobot;

    @Autowired
    private Messages messages;

    private static final String COMMAND = "!screenshot";
    private static final String FORMAT = "!screenshot url";
    private static final String DESC = "Fait une capture d'une page pour l'envoyer sur slack";

    @Override
    public String getCommandName() {
        return COMMAND;
    }

    @Override
    public String getDescription() {
        return DESC;
    }

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    protected void doCommand(String command, String[] args, Option opts) {
        if(args != null && args.length > 0) {
            try {
                String url = args[0];
                String cleanUrl = url.substring(url.indexOf("<")+1, url.lastIndexOf("|") != -1 ? url.lastIndexOf("|") : url.lastIndexOf(">"));
                WebResource resource = Client.create().resource(cleanUrl);
                resource.addFilter(new ClientFilter() {
                    @Override
                    public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
                        cr.getHeaders().add(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:49.0) Gecko/20100101 Firefox/49.0");
                        //cr.getHeaders().add(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate");
                        return getNext().handle(cr);
                    }
                });
                resource.addFilter(new GZIPContentEncodingFilter());
                InputStream webStream = resource.get(InputStream.class);

                RenderOptions renderOptions = RenderOptions.DEFAULT.withRenderFormat(RenderFormat.PNG);
                InputStream renderedStream = PhantomJS.render(webStream,renderOptions);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOUtils.copy(renderedStream, baos);

                nicobot.sendFile(opts.message, baos.toByteArray(), resource.getURI().getHost());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            nicobot.sendPrivateMessage(opts.message, "Too few arguments");
        }
    }
}
