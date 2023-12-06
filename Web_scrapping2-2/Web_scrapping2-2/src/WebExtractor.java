import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WebExtractor {
    StructTreePattern structTreePattern = new StructTreePattern();
    String defaultPatternMethod = structTreePattern.TAG_PATTERN;

    public static void main(String[] args) {
        String inputHtmlFile = "C:/Users/srika/Downloads/Web_scrapping2-2/Web_scrapping2-2/src/amazon.html";
        String outputHtmlFile = "C:/Users/srika/Downloads/Web_scrapping2-2/Web_scrapping2-2/src/output.html";
        String outputHtmlContent = extractWebData(inputHtmlFile);
        long startTime = System.currentTimeMillis();
        if (outputHtmlContent != null) {
            saveOutputHtml(outputHtmlContent, outputHtmlFile);
            System.out.println("Extraction successful. Output saved to '" + outputHtmlFile + "'.");

        }
    }

    private static String extractWebData(String inputHtmlFile) {
        List<Integer> nodeEncodingSequenceDefault =  Arrays.asList(1, 2, 3, 4, 5);
        try {
            File input = new File(inputHtmlFile);
            Document doc = Jsoup.parse(input, "UTF-8");

            String bodyContent = doc.body().html();

            // Add a green border to the body content
//            bodyContent = "<div style='border: 2px solid green; padding: 10px;'>" + bodyContent + "</div>";

            // Create a CSS style to add a green dotted border to div elements
            String style = "<style>div { border: 0.25px dashed darkgreen; }</style>";

            // Extracting the encoding document after finding the frequent patterns.
            SuffixTree suffixTree = new SuffixTree();
            Document document = suffixTree.createDocumentFromEncoding(nodeEncodingSequenceDefault);

            document.getAllElements();


            StringBuilder outputHtmlContent = new StringBuilder();
            outputHtmlContent.append("<!DOCTYPE html>\n<html>\n<head>\n")
                    .append("<title>").append(doc.title()).append("</title>\n")
                    .append(style)
                    .append("</head>\n<body>\n")
                    .append(bodyContent)
                    .append("</body>\n</html>");

            return outputHtmlContent.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void saveOutputHtml(String outputHtmlContent, String outputHtmlFile) {
        try (FileWriter writer = new FileWriter(outputHtmlFile)) {
            writer.write(outputHtmlContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}