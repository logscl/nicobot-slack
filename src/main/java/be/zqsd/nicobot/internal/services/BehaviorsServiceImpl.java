package be.zqsd.nicobot.internal.services;

import be.zqsd.nicobot.bot.NicoBot;
import be.zqsd.nicobot.bot.utils.Option;
import be.zqsd.nicobot.bot.utils.Random;
import be.zqsd.nicobot.bot.behavior.NiConduct;
import be.zqsd.nicobot.services.BehaviorsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Julien
 *
 */
@Service
public class BehaviorsServiceImpl implements BehaviorsService {

    private List<NiConduct> behaviors;

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private NicoBot nicobot;

    public BehaviorsServiceImpl() {	}

    @Override
    public void randomBehave(Option opts) {
        List<NiConduct> chosenBehaviors = new ArrayList<>();

        int chance = Random.MAX_CHANCE - Random.nextInt();

        // On construit une liste des differents NiConduct qui sont accessibles pour cette proba
        chosenBehaviors.addAll(getBehaviors().stream().filter(behavior -> chance < behavior.getChance()).collect(Collectors.toList()));

        // Et si on a au moins 1 NiConduct, on en determine 1 seul parmis la liste et on l'exec
        if (! chosenBehaviors.isEmpty()){
            int idx = Random.nextInt(chosenBehaviors.size());

            NiConduct chosenOne = chosenBehaviors.get(idx);
            chosenOne.behave(opts);
        }
    }

    public List<NiConduct> getBehaviors() {
        if (behaviors == null) {
            behaviors = new ArrayList<>();

            behaviors.addAll(ctx.getBeansOfType(NiConduct.class).entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()));
        }

        return behaviors;
    }

}
