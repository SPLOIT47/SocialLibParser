package com.sociallibparser.creator;

import com.sociallibparser.details.TitleDetails;
import com.sociallibparser.enums.BookType;

public class CreatorFactory {
    public static Creator getCreator(BookType type, TitleDetails details) {
        switch (type) {
            case EPUB:
                return  new EpubCreator(details);
            case FB2:
                return null;
            case PDF:
                return null;
            default:
                return null;
        }
    }
}
