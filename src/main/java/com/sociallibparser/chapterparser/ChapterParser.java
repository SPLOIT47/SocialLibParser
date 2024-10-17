package com.sociallibparser.chapterparser;

import java.util.ArrayList;
import java.util.Collection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sociallibparser.creator.Creator;
import com.sociallibparser.details.ChapterDetails;
import com.sociallibparser.manager.RequestManager;
import com.sociallibparser.singelton.ConfigReaderSingelton;
import com.sociallibparser.singelton.ObjectMapperSingelton;
import com.sociallibparser.singelton.RequestManagerSigelton;

import lombok.SneakyThrows;
import okhttp3.Request;
import okhttp3.Response;

public abstract class ChapterParser {
    protected final String API_HOST = "https://api.lib.social";
    protected final String TITLE_FULL_NAME;
    protected final ConfigReaderSingelton CONFIG_READER;
    protected final ObjectMapper mapper;
    protected final RequestManager requestManager;
    protected final int DELAY;
    protected final String BRANCH_ID;
    protected final JsonNode COVER;
    protected Creator creator;

    public ChapterParser(String titleFullName, String branch_id, JsonNode cover) {
        requestManager = RequestManagerSigelton.getInstance().getRequestManager();
        mapper = ObjectMapperSingelton.getInstance().getMapper();
        CONFIG_READER = ConfigReaderSingelton.getInstance();
        TITLE_FULL_NAME = titleFullName;
        BRANCH_ID = branch_id;
        COVER = cover;

        String delay = CONFIG_READER.getProperty("delay");
        DELAY = (delay == null ? 0 : Integer.parseInt(delay));
    }

    @SneakyThrows
    public void parseVolume(String volume, Collection<String> chapters) {
        System.out.println("Parsing volume " + volume);
        Collection<ChapterDetails> volumeDetails = new ArrayList<>();
        for (String number : chapters) {
            String url = API_HOST + "/api/manga/" + TITLE_FULL_NAME + "/chapter?" + 
                         "volume=" + volume + "&number=" + number + "&branch_id=" + BRANCH_ID;
            Request request = new Request.Builder().url(url).build();
            Response response = requestManager.RequestWithRetries(request);
            if (!response.isSuccessful() || response.body() == null) {
                continue;
            }
            JsonNode data = mapper.readTree(response.body().string()).get("data");
            ChapterDetails details = processData(volume, number, data);
            volumeDetails.add(details);
        }

        creator.uploadVolume(volumeDetails);
    }

    protected abstract ChapterDetails processData(String volume, String number, JsonNode chapterData);

    @SneakyThrows
    protected byte[] downloadCover() {
        Request request = new Request.Builder().url(COVER.get("default").asText()).build();
        Response response = requestManager.Request(request);
        if (response.isSuccessful() && response.body() != null) { return response.body().bytes(); }
        else { return null; }
    }
}
