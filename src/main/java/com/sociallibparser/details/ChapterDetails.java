package com.sociallibparser.details;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public record ChapterDetails(@NotNull String volume, @NotNull String number, String content, @NotNull String id,
                             String name, Collection<AttachmentDetails> attachments) {}

