package com.sociallibparser.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sociallibparser.chapterparser.ChapterParser;
import com.sociallibparser.chapterparser.ChapterParserFactory;
import com.sociallibparser.manager.RequestManager;
import com.sociallibparser.singelton.ConfigReaderSingelton;
import com.sociallibparser.singelton.ObjectMapperSingelton;
import com.sociallibparser.singelton.RequestManagerSigelton;

import lombok.SneakyThrows;
import okhttp3.Request;
import okhttp3.Response;

public class MetadataParser {
    private final String API_HOST = "https://api.lib.social";
    private final String TITLE_URL;        //example: https://ranobelib.me/ru/11407--solo-leveling
    private final String TITLE_FULL_NAME;       //example: 11407--solo-leveling
    private final String TITLE_ID;         //example: 11407
    private final RequestManager requestManager;
    private final ObjectMapper mapper;
    private final Scanner scanner;
    private final ExecutorService executor;
    private final ConfigReaderSingelton CONFIG_READER;
    private String branch_id;
    private Map<String, ArrayList<String>> volumesList;
    private ArrayList<String> branchIds;
    private JsonNode ranobe_info;
    private JsonNode cover;
    private ChapterParser chapterParser;
    private String currentBranch;

    public MetadataParser() {
        requestManager = RequestManagerSigelton.getInstance().getRequestManager();
        mapper = ObjectMapperSingelton.getInstance().getMapper();
        scanner = new Scanner(System.in);
        CONFIG_READER = ConfigReaderSingelton.getInstance();

        String userDefinedThreadsCount = CONFIG_READER.getProperty("threads");
        int threadsCount = (userDefinedThreadsCount == null ? 1 : Integer.parseInt(userDefinedThreadsCount));
        executor = Executors.newFixedThreadPool(threadsCount);

        volumesList = new HashMap<String, ArrayList<String>>(); 

        System.out.print("Ranobe url: ");
        TITLE_URL = scanner.nextLine();   
        TITLE_FULL_NAME = extractTitleName(TITLE_URL);
        assert TITLE_FULL_NAME != null;
        TITLE_ID = extractTitleId(TITLE_FULL_NAME);

        branchIds = new ArrayList<>();
        Path path = Paths.get("ranobe");
        if (!Files.exists(path) && !Files.isDirectory(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    public void parseMetadata() {
        parseInfo();
        parseBranchesInfo();
        parseChaptersInfo();
        try {
            parseVolumesInfo();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        chapterParser = ChapterParserFactory.createParser(TITLE_URL, TITLE_FULL_NAME, branch_id, cover);
    }

    public void parseVolumes() {
        CompletableFuture<Void> future = parseVolumesAsync();
        future.thenRun(() -> executor.shutdown());
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void parseInfo() {
        String url = API_HOST + "/api/manga/" + TITLE_FULL_NAME;
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = requestManager.Request(request);

            if (!response.isSuccessful() || response.body() == null) {
                throw new RuntimeException(response.message());
            }

            String responseBody = response.body().string();
            ranobe_info = mapper.readTree(responseBody).get("data");
            cover = ranobe_info.get("cover");
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void parseBranchesInfo() {
        String url = API_HOST + "/api/branches/" + TITLE_ID + "?team_defaults=1%";
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = requestManager.Request(request);

            if (!response.isSuccessful() || response.body() == null) {
                throw new RuntimeException(response.message());
            }

            String responseBody = response.body().string();
            JsonNode dataJson = mapper.readTree(responseBody).get("data");
            ((ObjectNode) ranobe_info).put("teams", dataJson.toString());
            

            if (ranobe_info.get("teams").isEmpty()) {
                currentBranch = "1";
            } else {
                System.out.println("Availible translations: ");
                parseTeams(dataJson);
                System.out.print("Translator number: ");
                int index = scanner.nextInt();
                currentBranch = branchIds.get(index - 1);
            }
            branch_id = currentBranch;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }   

    @SneakyThrows
    private void parseChaptersInfo() {
        String url = API_HOST + "/api/manga/" + TITLE_FULL_NAME + "/chapters";
        Request request = new Request.Builder().url(url).build();
        Response response = requestManager.Request(request);

        if (!response.isSuccessful() || response.body() == null) {
            throw new RuntimeException(response.message());
        }

        String responseBody = response.body().string();
        JsonNode chaptersDataWithAllBranches = mapper.readTree(responseBody).get("data");
        ObjectNode chaptersDataCurrentBranch = mapper.createObjectNode();
        ArrayNode dataArray = mapper.createArrayNode();
        for (JsonNode ch : chaptersDataWithAllBranches) {
            for (JsonNode br : ch.get("branches")) {
                if (br.get("branch_id").toString().equals(branch_id) || currentBranch.equals("1")) {
                    dataArray.add(ch);
                }
            }
        }

        chaptersDataCurrentBranch.set("data", dataArray);
        ((ObjectNode) ranobe_info).put("chapters", chaptersDataCurrentBranch.get("data").toString());
    }

    private void parseVolumesInfo() throws JsonMappingException, JsonProcessingException {
        String chaptersStringData = ranobe_info.get("chapters").asText();
        JsonNode chaptersData = mapper.readTree(chaptersStringData);
        for (JsonNode chapter : chaptersData) {
            String volume = chapter.get("volume").asText().toString();
            String number = chapter.get("number").asText().toString();
            ArrayList<String> set = volumesList.get(volume);
            if (set == null) {
                set = new ArrayList<>();
                volumesList.put(volume, set);
            }
            set.add(number);
        }
    }

    private CompletableFuture<Void> parseVolumesAsync() {
        Collection<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Map.Entry<String, ArrayList<String>> entry : volumesList.entrySet()) {
            String volume = entry.getKey();
            Collection<String> chapters = entry.getValue();
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                chapterParser.parseVolume(volume, chapters);
            }, executor);
            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private String extractTitleName(String url) {
        String pattern = ".*/(\\d+--[^/\\?]+)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(url);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
    
    private String extractTitleId(String name) {
        StringBuilder id = new StringBuilder();
        for (char ch : name.toCharArray()) {
            if (ch == '-') {
                break;
            }
            id.append(ch);
        }
        return id.toString();
    }

    private void parseTeams(JsonNode dataJson) {
        int counter = 1;
        for (JsonNode node : dataJson) {
            branchIds.add(node.get("id").asText());
            StringBuilder outStr = new StringBuilder();
            int totalTeamCount = node.get("teams").size();
            int teamCount = 1;

            outStr.append(counter).append(": ");
            
            for (JsonNode team : node.get("teams")) {
                outStr.append(team.get("name").asText());
                if (teamCount < totalTeamCount) {
                    outStr.append(", ");
                }
                ++teamCount;
            }
            outStr.append(". Total Chapters count in future...");
            System.out.println(outStr);
            counter++;
        }
    }
}
