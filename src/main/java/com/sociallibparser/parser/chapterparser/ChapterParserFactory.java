package com.sociallibparser.parser.chapterparser;

import com.fasterxml.jackson.databind.JsonNode;

public class ChapterParserFactory {
    public static ChapterParser createParser(String url, String titleFullName, String branch_id, JsonNode cover) {
        if (url.contains("mangalib.me")) {
            return new MangaLibParser(titleFullName, branch_id, cover);
        } else if (url.contains("ranobelib.me")) {
            return new RanobeLibParser(titleFullName, branch_id, cover);
        } else {
            throw new RuntimeException("Wrong url");
        }
    }
}
