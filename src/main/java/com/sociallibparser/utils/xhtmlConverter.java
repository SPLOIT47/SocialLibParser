package com.sociallibparser.utils;

import org.jdom2.input.SAXBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.io.IOException;
import java.io.StringReader;

public class xhtmlConverter {

    public static String convertToXhtml(String htmlContent) {
        Document document = Jsoup.parse(htmlContent, "", Parser.xmlParser());

        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        document.outputSettings().charset("UTF-8");

        for (Element img : document.select("img[data-src]")) {
            img.attr("src", img.attr("data-src"));
            img.removeAttr("data-src");
        }

        if (document.select("html").isEmpty()) {
            document = Jsoup.parse("<html xmlns=\"http://www.w3.org/1999/xhtml\"><body>" + htmlContent + "</body></html>", "", Parser.xmlParser());
        }

        return document.outerHtml();
    }
     
    public static org.jdom2.Document convertToDocument(String xhtml) throws IOException {
        SAXBuilder builder = new SAXBuilder();
        try {
            return builder.build(new StringReader(xhtml));
        } catch (Exception e) {
            throw new IOException("Failed to parse XHTML content", e);
        }
    }
}
