package com.sociallibparser.creator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sociallibparser.details.ChapterDetails;
import com.sociallibparser.details.TitleDetails;
import com.sociallibparser.manager.RequestManager;
import com.sociallibparser.singelton.ObjectMapperSingelton;
import com.sociallibparser.singelton.OkHttpClientSingelton;
import com.sociallibparser.singelton.RequestManagerSigelton;
import okhttp3.OkHttpClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public abstract class Creator {
    protected final String RELATIVE_DIR;
    protected final String TITLE_RELATIVE_PATH;
    protected final String BASE_URL = "https://ranobelib.me";
    protected final ObjectMapper mapper;
    protected final OkHttpClient client;
    protected final RequestManager requestManager;
    protected final TitleDetails TITLE_DETAILS;

    public Creator(TitleDetails details) {
        TITLE_DETAILS = details;
        client = OkHttpClientSingelton.getInstance().getClient();
        mapper = ObjectMapperSingelton.getInstance().getMapper();
        requestManager = RequestManagerSigelton.getInstance().getRequestManager();
        RELATIVE_DIR = TITLE_DETAILS.typeOfTitle() + "/" + TITLE_DETAILS.name();
        TITLE_RELATIVE_PATH = RELATIVE_DIR + "/" + TITLE_DETAILS.name() + "-volume-";

        createDirectory();
    }

    public abstract void uploadVolume(Collection<ChapterDetails> volume);

    protected void createDirectory() {
        Path path = Paths.get(RELATIVE_DIR);
        if (Files.isDirectory(path) || Files.exists(path)) { return; }
        try {
            Files.createDirectories(path);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
