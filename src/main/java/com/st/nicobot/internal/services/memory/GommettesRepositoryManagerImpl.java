package com.st.nicobot.internal.services.memory;

import com.st.nicobot.api.domain.model.GommettesMemory;
import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.utils.GommetteColor;
import com.st.nicobot.services.memory.GommettesRepositoryManager;
import com.ullink.slack.simpleslackapi.SlackUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Logs on 17-08-15.
 */
@Service
public class GommettesRepositoryManagerImpl extends AbstractRepositoryManager<GommettesMemory> implements GommettesRepositoryManager {

    @Autowired
    private NicoBot nicoBot;

    private GommettesMemory memory;

    @Override
    protected String getMemoryFileName() {
        return "GommettesMemory.ser";
    }

    @Override
    public GommettesMemory getMemory() {
        return memory;
    }

    @Override
    protected void setMemory(Object memory) {
        this.memory = (GommettesMemory) memory;
    }

    @Override
    protected void initMemory() {
        this.memory = new GommettesMemory();
        this.memory.setGommettes(new HashMap<>());
    }

    @Override
    public void addGommette(SlackUser user, GommetteColor color) {
        if(memory.getGommettes() == null) {
            memory.setGommettes(new HashMap<>());
        }

        if(memory.getGommettes().get(user.getId()) == null) {
            memory.getGommettes().put(user.getId(), new HashMap<>());
        }

        if(memory.getGommettes().get(user.getId()).get(color) == null) {
            memory.getGommettes().get(user.getId()).put(color, 1);
        } else {
            memory.getGommettes().get(user.getId()).put(color, memory.getGommettes().get(user.getId()).get(color) + 1);
        }
        writeFile();
    }

    @Override
    public Map<GommetteColor, Integer> getGommettes(SlackUser user) {
        return memory.getGommettes().get(user.getId());
    }

    @Override
    public Map<GommetteColor, Integer> getBestGommettes() {
        Map<GommetteColor, Integer> tmp = null;
        int totalTmp = 0;
        boolean exaeqo = false;
        for (Map.Entry<String, Map<GommetteColor, Integer>> entry1 : memory.getGommettes().entrySet()) {
            int count =  (entry1.getValue().get(GommetteColor.GREEN) != null ?  entry1.getValue().get(GommetteColor.GREEN) : 0) + (entry1.getValue().get(GommetteColor.RED) != null ? entry1.getValue().get(GommetteColor.RED) : 0);
            if(count > totalTmp) {
                tmp = entry1.getValue();
                totalTmp = count;
                exaeqo = false;
            } else if(count == totalTmp) {
                exaeqo = true;
            }
        }
        if(!exaeqo) {
            return tmp;
        }
        return null;
    }

    public static void main(String[] args) {
        GommettesRepositoryManagerImpl impl = new GommettesRepositoryManagerImpl();
        impl.loadFile();

        if(impl.memory.getGommettes() != null) {
            System.out.println("\nGommettes:");
            for (Map.Entry<String, Map<GommetteColor, Integer>> entry1 : impl.memory.getGommettes().entrySet()) {
                System.out.println("User ID: " + entry1.getKey() + " - " + entry1.getValue().get(GommetteColor.GREEN) + " vertes, " + entry1.getValue().get(GommetteColor.RED) + " rouges.");
            }
        }
    }
}
