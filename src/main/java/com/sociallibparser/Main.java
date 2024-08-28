package com.sociallibparser;

import com.sociallibparser.parser.MetadataParser;

public class Main {
    public static void main(String[] args) {
        MetadataParser parser = new MetadataParser();
        parser.parseMetadata();
        parser.parseVolumes();

        //test
    }
}
