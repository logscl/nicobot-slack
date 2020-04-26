package be.zqsd.nicobot.internal.services;

import be.zqsd.nicobot.services.NudityDectionService;
import be.zqsd.nicobot.services.PropertiesService;
import be.zqsd.nicobot.utils.NicobotProperty;
import com.algorithmia.Algorithmia;
import com.algorithmia.AlgorithmiaClient;
import com.algorithmia.algo.AlgoResponse;
import com.algorithmia.algo.Algorithm;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Logs on 06-01-17.
 */
@Service
public class NudityDectionServiceImpl implements NudityDectionService, InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(NudityDectionServiceImpl.class);

    @Autowired
    private PropertiesService properties;

    private Algorithm algo;

    public void afterPropertiesSet() {
        AlgorithmiaClient client = Algorithmia.client(properties.get(NicobotProperty.ALGORITHMIA_API_KEY));
        algo = client.algo(properties.get(NicobotProperty.ALGORITHMIA_NUDITY_ALGORITHM));
    }

    @Override
    public boolean checkUrl(String url) throws Exception {
        AlgoResponse result = algo.pipeJson(String.format("\"%s\"", url));
        return BooleanUtils.toBoolean(result.asJsonString());
    }
}
