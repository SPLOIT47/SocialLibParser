package com.sociallibparser.creator;

import com.sociallibparser.details.AttachmentDetails;
import com.sociallibparser.details.ChapterDetails;
import com.sociallibparser.details.TitleDetails;
import com.sociallibparser.utils.xhtmlConverter;
import lombok.SneakyThrows;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;
import nl.siegmann.epublib.service.MediatypeService;
import okhttp3.Request;
import okhttp3.Response;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public final class EpubCreator extends Creator{
    private final EpubWriter EPUB_WRITER;
    private Book book;

    public EpubCreator(TitleDetails details) {
        super(details);
        EPUB_WRITER = new EpubWriter();
    }

    @Override
    @SneakyThrows
    public synchronized void uploadVolume(Collection<ChapterDetails> volume) {
        book = new Book();
        if (TITLE_DETAILS.coverBytes() != null) {
            Resource coverImg = new Resource(TITLE_DETAILS.coverBytes(), TITLE_DETAILS.coverName());
            coverImg.setMediaType(MediatypeService.JPG);
            book.setCoverImage(coverImg);
        }

        String vol = volume.isEmpty() ? null : volume.iterator().next().volume();
        String outputPath = TITLE_RELATIVE_PATH + vol + ".epub";

        for (ChapterDetails chapterDetails : volume) {
            System.out.println("Writing volume: " + chapterDetails.volume() + " chapter: " + chapterDetails.number());
            System.out.println("To: " + outputPath);
            addChapter(chapterDetails);
        }



        try (FileOutputStream out = new FileOutputStream(outputPath)) {
            EPUB_WRITER.write(book, out);
            System.out.println("Wrote volume: " + vol);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addChapter(ChapterDetails chapterDetails) {
       String content = processContent(chapterDetails);
       String title = chapterDetails.name();

       if (title != null) {
           content = "<h1 style='font-weight: bold;'>" + title + "</h1>" + content;
       } else {
           title = "";
       }

       String xhtmlContent = xhtmlConverter.convertToXhtml(content);
       Resource resource = new Resource(xhtmlContent.getBytes(), chapterDetails.number() + ".xhtml");
       book.addSection(title, resource);
    }

    private String fixImageScheme(String url, String filename, String content, String volume, String number, String id) {
        if (content.contains(url)) {
            content = content.replace(url, filename);
        } else {
            String url2 = url.replace(id, volume + '-' + number);
            content = content.replace(url2, filename);
        }
        content = content.replaceAll("loading=\"lazy\"", "");
        return content;
    }

    private String processContent(ChapterDetails chapterDetails) {
        String updatedContent = chapterDetails.content();
        for (AttachmentDetails attachmentDetails : chapterDetails.attachments()) {
            String relativeURL = attachmentDetails.url();
            String url = BASE_URL + relativeURL;
            String filename = "Images/" + attachmentDetails.name() + "." + attachmentDetails.extension();
            addImageToBook(url, filename, chapterDetails);
            updatedContent = fixImageScheme(url, filename, updatedContent, chapterDetails.volume(), chapterDetails.number(), chapterDetails.id());
        }

        return updatedContent;
    }

    private void addImageToBook(String url, String filename, ChapterDetails chapterDetails) {
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
