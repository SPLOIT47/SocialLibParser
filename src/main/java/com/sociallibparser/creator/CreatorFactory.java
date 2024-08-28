package com.sociallibparser.creator;

import com.sociallibparser.details.TitleDetails;

public class CreatorFactory {
    public Creator getCreator(String type, TitleDetails details) {
        switch (type) {
            case "epub":
                return  new EpubCreator(details);
            case "fb2":
                return null;
            case "pdf":
                return null;
            default:
                return null;
        }
    }
}
