package com.st.nicobot.internal.services;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.*;
import com.st.nicobot.services.NudityDectionService;
import com.st.nicobot.services.PropertiesService;
import com.st.nicobot.utils.NicobotProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Created by Logs on 06-01-17.
 */
@Service
public class NudityDectionServiceImpl implements NudityDectionService {

    private static Logger logger = LoggerFactory.getLogger(NudityDectionServiceImpl.class);

    @Autowired
    private PropertiesService properties;

    @Override
    public boolean hasNudity(String url) throws Exception {

        VisionRequestInitializer initializer = new VisionRequestInitializer(properties.get(NicobotProperty.SEARCH_API_KEY));
        Vision vision = new Vision.Builder(new NetHttpTransport(), new JacksonFactory(), httpRequest -> {}).setApplicationName("google-vision")
                .setVisionRequestInitializer(initializer)
                .build();

        AnnotateImageRequest request = new AnnotateImageRequest();
        ImageSource imgSource = new ImageSource();
        imgSource.setImageUri(url);
        Image image = new Image();
        image.setSource(imgSource);

        request.setImage(image);

        Feature nudityDetection = new Feature();
        nudityDetection.setType("SAFE_SEARCH_DETECTION");
        request.setFeatures(Collections.singletonList(nudityDetection));


        BatchAnnotateImagesRequest requestBatch = new BatchAnnotateImagesRequest();
        requestBatch.setRequests(Collections.singletonList(request));
        BatchAnnotateImagesResponse response = vision.images()
                .annotate(requestBatch)
                .execute();

        if(!response.getResponses().isEmpty()) {
            String adultContent = response.getResponses().get(0).getSafeSearchAnnotation().getAdult();
            logger.debug("Url: {} has a adult result of {}", url, adultContent);
            return adultContent.equals("LIKELY") || adultContent.equals("VERY_LIKELY");
        }
        return false;
    }
}
