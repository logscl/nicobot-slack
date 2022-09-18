package be.zqsd.nicobot.handler.message;

import be.zqsd.nicobot.bot.ChannelService;
import be.zqsd.nicobot.bot.Nicobot;
import be.zqsd.nicobot.message.MessageFormatter;
import be.zqsd.nicobot.message.Reaction;
import be.zqsd.slack.client.WebClient;
import com.slack.api.model.event.MessageEvent;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static be.zqsd.nicobot.message.Reaction.react;
import static java.time.LocalDateTime.now;
import static java.util.List.of;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.ThreadLocalRandom.current;
import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class ConversationTrigger extends ConditionalMessage {

    private static final Logger LOG = getLogger(ConversationTrigger.class);

    // TODO this will come from an API call
    private static final List<Reaction> REACTIONS = of(
            react("^(sisi|13)$", "la famille"),
            react("^tf2$", "Bande de casus..."),
            react("^(pour )??rien( \\!)??$", "Baaam ! Bien joué #p !"),
            react("^chut( !)??$", "Oh, tu m'dis pas chut #p, déjà"),
            react("^propre sur toi$", "De dingue !"),
            react("^\\(=\\^;\\^=\\) pika pi$", "Toi aussi tu joues à Pokemon ?"),
            react("^psp$", "Enkuler de rire !"),
            react("^pic$", "...or it didn't happen"),
            react("^secret$", "J'ai un terrible secret aussi..."),
            react("^pierre$", true, 0, "Papier !"),
            react("^papier|feuile$", true, 0, "Ciseaux !"),
            react("^ciseaux$", true, 0, "Pierre !"),

            // strict calls
            react("^!epicsong$", "https://www.youtube.com/watch?v=MPjy55Y6hWU&feature=youtu.be&t=2m17s"),
            react("^!wind$", "https://www.youtube.com/watch?v=d-_q-md80VQ"),
            react("^!popopo$", "https://www.youtube.com/watch?v=NJuTp7KTZCU"),
            react("^!admektator$", "https://soundcloud.com/vincevh/admektator"),
            react("^!astuvu$", "https://youtu.be/0jZf7jIjB5s?t=1m9s"),

            // fragments
            react(".*gamin.*", "Hein fieu"),
            react(".*hey.*", "Hey Hey !"),
            react(".*chamelle.*", "Y'a de ces CHA-MELLES ici ! :D"),
            react(".*ha(i|ï)ku.*", "Mais lol, y a pas plus débile que la formulation d'un haïku: 5-7-5.  \"Trente trois jours de pluie, Toi tu n'as que des soucis, Bite sur le gateau.\""),
            react(".*amis de (m|t|s)es amis.*", "Si tu as un ami, en fait tu en as deux. Puisque les amis de tes amis sont tes amis, et que tu es l'ami de ton ami, tu es donc ton propre ami !"),
            react(".*garagiste.*", "PUTAIN QU'ELLE EST BONNE LA GARAGIIIIISTE ! :D"),
            react(".*choper.*", "Tout le monde sait très bien que je choppe plus rien depuis P2, merci de remuer le couteau. :("),
            react(".*nico( .*|$)", false, 20, "\"Nico\" avec un N majuscule putain !  Tu es né idiot, tu vas mourir idiot !"),
            react(".*ocin.*", "Tain mais pas à l'envers !  Ca m'énèèèèrve çaaaa !!"),
            react(".*tracteur.*", "On va au Quick ?  Il est où mon saucisson ?"),
            react(".*projet.*", "C'est quoi le projet?"),
            react(".*mauvaise ambiance.*", "MAUVAISE AMBIANCE MAUUVAISE AMBIANCE! MAU-VAISE-AM-BIANCE!"),
            react(".*bière.*", "#jesuispichet"),
            react(".*citerne.*", "J'ai plus faim, merci anto !"),
            react(".*oedipe.*", "https://youtu.be/1WtjruJzZkI?t=10s"),
            react(".*\\bparc\\b.*", "parque!"),
            react(".*\\blac\\b.*", "laque!"),
            react(".*gomette.*", "Ca s'écrit gomMette. Avec 2 m."),
            react(".* lance.*", true, 60, "Oui, mais pas trop loin..."),
            react(".*en vrai.*", true, 60, "En vrai !?", "M'enfin...", "INTERVENTION"),

            // Random reacts
            react(".*qui .*\\?$", "C'est #u !", "J'veux pas dénoncer... mais c'est #u.", "Si c'est pas #u, c'est ta mère !"),

            // girls
            react(".*sarah?.*", true, 30, "Mhmmm...  \"Avec tes deux obus, j'crois que tu te sens plus. Du quatre-vingt dix D, il en faut plus pour me faire trembler !\""),
            react(".*(julie|hercot) .*", true, 30, "On en reparle quand elle aura arrêté avec son équipe de meeeerde celle là.  Iiiimmmmbécile."),
            react(".*pauline.*", true, 30, "Ah ben si tu veux, moi j'en connais un rayon sur les Paulines !  P1, P2, P3 et même P4: j'ai fait toute la famille !"),
            react(".*alice.*", true, 30, "T'as qu'à me dire dans quel auditoire elle a cours; j'ai un plan pour ça."),
            react(".*(ga(e|ë)lle).*", true, 30, "Moi, quand une meuf un peu bourrée me propose de dormir chez elle après une bonne grosse guindaille, je préfère encore dire non tu vois.  Genre gentleman.  Où est le challenge sinon ?!"),
            react(".*(fairy|aur(e|é)lie|hanut).*", true, 30, "Heuuu, ouais, salut...  T'aurais pas de_cbble stp ?  En fait j'l'ai pas et on a war dans 4 minutes :("),

            // bot highlight
            react("^#b( ?\\?)+?$", "Quoi ?"),
            react("^salut #b.*", "Salut #p !"),
            react(".*#b( ?\\?)+?$", "Oui.", "Oui !", "Non...", "Non !", "Tu peux pas décider tout seul franchement ?", "Ché pas tséééé", "Ché pas... Demande a slackbot !")
    );

    private final Nicobot nicobot;
    private final WebClient client;
    private final MessageFormatter formatter;
    private final ChannelService channelService;

    private final Map<String, List<Conversation>> conversationsPerChannel;

    @Inject
    public ConversationTrigger(Nicobot nicobot,
                               WebClient client,
                               MessageFormatter formatter,
                               ChannelService channelService) {
        this.nicobot = nicobot;
        this.client = client;
        this.formatter = formatter;
        this.channelService = channelService;
        this.conversationsPerChannel = new HashMap<>();
    }

    @Override
    boolean conditionMet(MessageEvent event) {
        return true;
    }

    @Override
    int chance() {
        return 100;
    }

    @Override
    void handleConditionalMessage(MessageEvent event) {
        var channel = event.getChannel();
        var message = event.getText();

        if (channelService.isFeaturedChannel(event.getChannel())) {
            var conversations = conversationsPerChannel.computeIfAbsent(channel, c -> initConversations());

            LOG.debug("Checking if this message '{}' triggers a response from bot...", event.getText());

            conversations.stream()
                    .filter(Conversation::canAnswer)
                    .filter(conversation -> conversation.match(message))
                    .findFirst()
                    .ifPresent(conversation -> {
                        LOG.debug("Reaction found ! ({})", conversation.getTrigger().pattern());
                        conversation.markAsSpoken();
                        var formattedMessage = formatter.formatMessage(conversation.getResponse(), event.getUser(), event.getChannel());
                        nicobot.sendMessage(event, formattedMessage);
                    });
        }


    }

    private List<Conversation> initConversations() {
        return REACTIONS
                .stream()
                .map(reaction -> Conversation.init(reaction, client))
                .toList();
    }

    private static class Conversation {
        private final Pattern trigger;
        private final List<String> replies;
        private final int cooldownInSeconds;
        private LocalDateTime lastAnswerTime;

        private Conversation(Pattern trigger,
                             List<String> replies,
                             int cooldownInSeconds) {
            this.trigger = trigger;
            this.replies = replies;
            this.cooldownInSeconds = cooldownInSeconds;
            this.lastAnswerTime = null;
        }

        public static Conversation init(Reaction reaction, WebClient client) {
            return new Conversation(reaction.buildPattern(client.botId(), client.botName()), reaction.getReplies(), reaction.getCooldownInSeconds());
        }

        public Conversation markAsSpoken() {
            this.lastAnswerTime = now();
            return this;
        }

        public boolean canAnswer() {
            return ofNullable(lastAnswerTime)
                    .map(date -> date.plusSeconds(cooldownInSeconds))
                    .map(date -> date.isBefore(now()))
                    .orElse(true);
        }

        public boolean match(String message) {
            return trigger.matcher(message).matches();
        }

        public Pattern getTrigger() {
            return trigger;
        }

        public String getResponse() {
            return replies.get(current().nextInt(replies.size()));
        }
    }
}
