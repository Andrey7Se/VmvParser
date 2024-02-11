package app.sh1yambur;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.jsoup.Connection.Response;

public class Downloader {
    private static final boolean IS_REPLACE_FOOTER = true; // replace original footer image
//    private final static String FOOTER_IMG_URL = "../../common_files/footer.png";
    private final static String FOOTER_IMG_URL = "../../commonmedia/footer.png";

    private final Path pagesDirPath;

    private LinkLine linkLine;
    private Document document;
    private String htmlAsStr;

    public Downloader(Path pagesDirPath) {
        this.pagesDirPath = pagesDirPath;
    }

    // download html-page by url
    public void downloadPage(LinkLine linkLine) {
        this.linkLine = linkLine;

        try {
            if (Files.notExists(pagesDirPath)) {
                Files.createDirectory(pagesDirPath);
            }

            this.document = Jsoup.connect(linkLine.url()).get();
            Log.write().info("downloaded page \"{}\" lesson #{}", document.title(), linkLine.number());

            // ============= edit html as Document ===========
            extractDatePublished();
            removeOriginalTitle();
            removeDoubleTitle();
            fixEmbeddedUrl();
            replaceFooterImg();


            saveImagesAsFiles();
            Log.write().info("images of lesson #{} downloaded", linkLine.number());


            this.htmlAsStr = document.html();
            // =========== next edit html as String ============
            fixImagesPath();
            fixCodeTags();
            cleanHeader();
            cleanFooter();


            savePageToFile(htmlAsStr);
            Log.write().info("html of lesson #{} parsed and saved to file", linkLine.number());

        } catch (IOException e) {
            Log.write().error("{} in download page", e.getMessage());
        }
    }

    // replace last image
    private void replaceFooterImg() {
        if (IS_REPLACE_FOOTER) {
            if (document.select("img").last() != null) {
                //document.select("img").last().replaceWith(new Element("hr"));
                document.select("img").last().replaceWith(
                        new Element("img").attr("src", FOOTER_IMG_URL)
                );
            }
        }
    }

    // remove "/file/" from images path
    private void fixImagesPath() {
        htmlAsStr = htmlAsStr.replaceAll("/file/", "");
    }

    // convert youtube embedded url to normal
    private static String convertEmbeddedToRegularUrl(String embedUrl) {
        String result = "incorrect url";
        try {
            URI uri = new URI(embedUrl.replace("/embed/youtube?url=", ""));
            result = uri.getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return result;
    }

    // create directory and save page body in html-file
    private void savePageToFile(String pageBody) {
        Path pagePath = pagesDirPath.resolve(linkLine.number());

        try {
            if (Files.notExists(pagePath)) {
                Files.createDirectory(pagePath);
            }

            Path htmlPath = pagePath.resolve(linkLine.filename().concat(".html"));

            Files.writeString(htmlPath, pageBody);
        } catch (IOException e) {
            Log.write().error("{} in save page to file", e.getMessage());
        }
    }

    // create directory and save images from page
    private void saveImagesFromPage(String imgName, byte[] imgAsBytes) {
        Path imgPath = pagesDirPath.resolve(linkLine.number());

        try {
            if (Files.notExists(imgPath)) {
                Files.createDirectory(imgPath);
            }

            Files.write(imgPath.resolve(imgName), imgAsBytes);
        } catch (IOException e) {
            Log.write().error("{} in save image to directory", e.getMessage());
        }

    }

    // save images as files
    private void saveImagesAsFiles() throws IOException {
        Elements imgElements = document.select("img");

        for (int i = 0; i < imgElements.size() - 1; i++) {
            Response resultImageResponse = Jsoup
                    .connect(imgElements.get(i).absUrl("src"))
                    .ignoreContentType(true)
                    .execute();

            String imgName = imgElements.get(i).attr("src").replaceFirst("/file/", "");

            // fix google image name for correct saving
            if (imgName.contains("googleusercontent")) {
                Element imgElem = document.getElementsByAttributeValue("src", imgName).first();

                StringBuilder tail = new StringBuilder("ggl_");
                for (int j = imgName.length() - 7; j < imgName.length(); j++) {
                    tail.append(imgName.charAt(j));
                }
                tail.append(".png");
                imgName = tail.toString();

                // replace google image path to regular path at local image
                imgElem.replaceWith(
                        new Element("img").attr("src", imgName)
                );
            }

            saveImagesFromPage(imgName, resultImageResponse.bodyAsBytes());
        }
    }

    // remove trash tags into footer
    private void cleanFooter() {
        String removeFooterStr = StringUtils.substringBetween(htmlAsStr, "</article>", "</body>");
        htmlAsStr = htmlAsStr.replace(removeFooterStr, "\n</main></div></div>\n");
    }

    //remove trash tags into header
    private void cleanHeader() {
        String removeHeaderStr = StringUtils.substringBetween(htmlAsStr, "</title>", "</head>");
        htmlAsStr = htmlAsStr.replace(removeHeaderStr, "\n");
    }

    // add <code> tags into <pre> elements
    private void fixCodeTags() {
        htmlAsStr = htmlAsStr
                .replaceAll("<pre>", "<pre><code>")
                .replaceAll("</pre>", "</code></pre>");
    }

    // replace embedded youtube-url
    private void fixEmbeddedUrl() {
        Elements figureEls = document.select("figure");
        figureEls.forEach(el -> {
            Element iframe = el.selectFirst("iframe");

            if (iframe != null) {
                String embeddedLink = iframe.attr("src");
                String regularLink = convertEmbeddedToRegularUrl(embeddedLink);

                el.replaceWith(new Element("a")
                        .attr("href", regularLink)
                        .html("ссылка")
                );
            }
        });
    }

    // remove double title article
    private void removeDoubleTitle() {
        Element articleEl = document.select("article").first();
        articleEl.select("h1").remove();
        Elements addressEls = document.select("address");
        addressEls.forEach(Node::remove);
    }

    // remove original page title
    private void removeOriginalTitle() {
        document.select("title").first().text("");
    }

    // parse date published original article
    private void extractDatePublished() {
        Elements linkEls = document.select("time");
        for (Element link : linkEls) {
            String datetime = link.attr("datetime").replace("+0000", "");
            linkLine.setDatePublished(LocalDateTime.parse(datetime));

            link.select("time").remove();
        }
    }
}
