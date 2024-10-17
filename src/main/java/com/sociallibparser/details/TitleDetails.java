package com.sociallibparser.details;

import com.sociallibparser.enums.TitleType;
import org.jetbrains.annotations.NotNull;

public record TitleDetails(@NotNull String name, byte[] coverBytes, String coverName, TitleType typeOfTitle) { //typeOfTitle means it ranobe or manga
    
}

