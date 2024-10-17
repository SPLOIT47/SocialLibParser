package com.sociallibparser.parser.chapterparser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.fasterxml.jackson.databind.JsonNode;
import com.sociallibparser.creator.CreatorFactory;
import com.sociallibparser.details.AttachmentDetails;
import com.sociallibparser.details.ChapterDetails;
import com.sociallibparser.details.TitleDetails;
import com.sociallibparser.enums.BookType;
import com.sociallibparser.enums.TitleType;

public class RanobeLibParser extends ChapterParser{

    public RanobeLibParser(String titleFullName, String branch_id, JsonNode cover) {
        super(titleFullName, branch_id, cover);
        TitleDetails titleDetails = new TitleDetails(TITLE_FULL_NAME, downloadCover(), COVER.get("filename").asText(), TitleType.Ranobe);
        creator = CreatorFactory.getCreator(BookType.EPUB, titleDetails);
    }

    @Override
    protected ChapterDetails processData(String volume, String number, JsonNode chapterData) {
        String content = determineContent(chapterData);
        String name = chapterData.get("name").asText();
        String id = chapterData.get("id").asText();
        Collection<AttachmentDetails> attachments;
        if (chapterData.has("attachments")) { attachments = processAttachments(chapterData.get("attachments")); }
        else { attachments = null; }

        return new ChapterDetails(volume, number, content, id, name, attachments);
    }

    private Collection<AttachmentDetails> processAttachments(JsonNode chapterAttachments) {
        Collection<AttachmentDetails> attachments = new ArrayList<>();
        for (JsonNode attachmentNode : chapterAttachments) {
            String url = attachmentNode.get("url").asText();
            String name = attachmentNode.get("name").asText();
            String extension = attachmentNode.get("extension").asText();
            AttachmentDetails details = new AttachmentDetails(url, name, extension);
            attachments.add(details);
        }
        return attachments;
    }

    private String determineContent(JsonNode chapterData) {
        JsonNode contentNode = chapterData.get("content");
    
        if (isStructuredContent(contentNode)) {
            return processStructuredContent(contentNode.toString());
        }
        return contentNode.asText();
    }
    
    private boolean isStructuredContent(JsonNode contentNode) {
        return contentNode.has("type") && contentNode.has("content") && contentNode.get("type").asText().equals("doc");
    }

    private String processStructuredContent(String content) {
        StringBuilder htmlContent = new StringBuilder();
        try {
            JsonNode jsonNode = mapper.readTree(content);
            JsonNode contentArray = jsonNode.get("content");
            if (contentArray != null && contentArray.isArray()) {
                for (JsonNode element : contentArray) {
                    String type = element.has("type") ? element.get("type").asText() : null;
                    if ("paragraph".equals(type)) {
                        htmlContent.append("<p>");
                        JsonNode paragraphContent = element.get("content");
                        if (paragraphContent != null && paragraphContent.isArray()) {
                            for (JsonNode textElement : paragraphContent) {
                                String textType = textElement.has("type") ? textElement.get("type").asText() : null;
                                if ("text".equals(textType)) {
                                    String text = textElement.has("text") ? textElement.get("text").asText() : "";
                                    htmlContent.append(text);
                                }
                            }
                        }
                        htmlContent.append("</p>");
                    } else if ("heading".equals(type)) {
                        int level = element.has("attrs") && element.get("attrs").has("level") ? element.get("attrs").get("level").asInt() : 1;
                        htmlContent.append("<h").append(level).append(">");
                        JsonNode headingContent = element.get("content");
                        if (headingContent != null && headingContent.isArray()) {
                            for (JsonNode textElement : headingContent) {
                                String textType = textElement.has("type") ? textElement.get("type").asText() : null;
                                if ("text".equals(textType)) {
                                    String text = textElement.has("text") ? textElement.get("text").asText() : "";
                                    htmlContent.append(text);
                                }
                            }
                        }
                        htmlContent.append("</h").append(level).append(">");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    
        return htmlContent.toString();
    }
}
