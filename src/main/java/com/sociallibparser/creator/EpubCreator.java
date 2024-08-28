package com.sociallibparser.creator;

import java.io.IOException;
import java.io.InputStream;

import com.sociallibparser.details.AttachmentDetails;
import com.sociallibparser.details.ChapterDetails;
import com.sociallibparser.details.TitleDetails;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;
import nl.siegmann.epublib.service.MediatypeService;
import okhttp3.Request;
import okhttp3.Response;

public final class EpubCreator extends Creator{
    private final EpubWriter EPUB_WRITER;
    private Book book;

    public EpubCreator(TitleDetails details) {
        super(details);
        EPUB_WRITER = new EpubWriter();
    }

    @Override
    public void uploadVolume(ChapterDetails details) {
        book = new Book();
        if (TITLE_DETAILS.coverBytes() != null) {
            Resource coverImg = new Resource(TITLE_DETAILS.coverBytes(), TITLE_DETAILS.coverName());
            coverImg.setMediaType(MediatypeService.JPG);
            book.setCoverImage(coverImg);
        }
    }

    private String fixImageScheme(String content, String url, String filename, ChapterDetails details) {
        if (content.contains(url)) {
            content = content.replace(url, filename);
        } else {
            String url2 = url.replace(details.id(), details.volume() + '-' + details.number());
            content = content.replace(url2, filename);
        }
        content = content.replaceAll("loading=\"lazy\"", "");
        return content;
    }


    private void addImageToBook(AttachmentDetails details, ChapterDetails chapterDetails) {
        String relativeURL = details.url();
        String url = BASE_URL + relativeURL;
        String filename = "Images/" + details.name() + "." + details.extension();

        Response response = null;
        try {
            response = executeRequestWithFallback(url, chapterDetails);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        if (response != null && response.body() != null) {
            try (InputStream inputStream = response.body().byteStream()) {
                Resource resource = new Resource(inputStream, filename); 
                book.getResources().add(resource);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            } finally {
                response.close(); 
            }
        } else {
            System.err.println("Failed to fetch the image: " + url);
        }
    }

    private Response executeRequestWithFallback(String url, ChapterDetails chapterDetails) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = requestManager.Request(request);
    
        if (!response.isSuccessful()) {
            url = url.replace(chapterDetails.id(), chapterDetails.volume() + '-' + chapterDetails.number());
            request = new Request.Builder().url(url).build();
            response = requestManager.Request(request);
        }

        return response;
    }
    
}
