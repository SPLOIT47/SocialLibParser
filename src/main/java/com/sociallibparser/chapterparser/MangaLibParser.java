package com.sociallibparser.chapterparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.sociallibparser.details.ChapterDetails;

public class MangaLibParser extends ChapterParser {

    public MangaLibParser(String titleFullName, String branch_id, JsonNode cover) {
        super(titleFullName, branch_id, cover);
    }

    @Override
    protected ChapterDetails processData(String volume, String number, JsonNode chapterData) {
        return null;
    }    
}
