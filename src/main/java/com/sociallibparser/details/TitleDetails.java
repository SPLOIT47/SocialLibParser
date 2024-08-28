package com.sociallibparser.details;

import org.jetbrains.annotations.NotNull;

public record TitleDetails(@NotNull String name, byte[] coverBytes, String coverName, String typeOfTitle) { //typeOfTitle means it ranobe or manga
    
}

