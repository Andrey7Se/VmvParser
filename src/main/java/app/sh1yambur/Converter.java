package app.sh1yambur;

// https://github.com/furstenheim/copy-down

import io.github.furstenheim.CodeBlockStyle;
import io.github.furstenheim.CopyDown;
import io.github.furstenheim.Options;
import io.github.furstenheim.OptionsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Converter {
    private final static boolean IS_REMOVE_HTML = true;
    private final static String COURSE_URL = "../../course.md";
    private final static String ROADMAP_URL = "../../roadmap.md";
    private final static String HEADER_IMG_URL = "../../commonmedia/header.png";

    private final Path lessonsDirPath;

    private LinkLine linkLine;
    private String mdAsStr;

    public Converter(Path lessonsDirPath) {
        this.lessonsDirPath = lessonsDirPath;
    }

    public void execute(LinkLine linkLine) {
        this.linkLine = linkLine;

        Path htmlFilePath = lessonsDirPath.resolve(linkLine.number()).resolve(linkLine.filename().concat(".html"));
        Path mdFilePath = Paths.get(htmlFilePath.toString().replaceFirst(".html", ".md"));

        convert(htmlFilePath, mdFilePath);

        if (IS_REMOVE_HTML) {
            removeHtmlFile(htmlFilePath);
            Log.write().info("deleted html-file of lesson #{}", linkLine.number());
        }
    }

    private void convert(Path htmlFilePath, Path mdFilePath) {
        OptionsBuilder optionsBuilder = OptionsBuilder.anOptions();
        Options options = optionsBuilder
                .withCodeBlockStyle(CodeBlockStyle.FENCED) // needed for save code-blocks
                .build();

        CopyDown converter = new CopyDown(options);

        mdAsStr = converter.convert(readHtmlFromFile(htmlFilePath));

        editHeader();
        editFooter();
        addJavaTag();

        writeMdToFile(mdAsStr, mdFilePath);
        Log.write().info("lesson #{} converted and saved to markdown file", linkLine.number());
    }

    private void removeHtmlFile(Path htmlFilePath) {
        try {
            if (Files.exists(htmlFilePath)) {
                Files.delete(htmlFilePath);
            }
        } catch (IOException e) {
            Log.write().error(e.getMessage());
        }
    }

    private String readHtmlFromFile(Path htmlPagePath) {
        String result = "";

        try {
            result = Files.readString(htmlPagePath);
        } catch (IOException e) {
            e.printStackTrace();
            Log.write().error(e.getMessage());
        }

        return result;
    }

    private void writeMdToFile(String htmlAsStr, Path mdFilePath) {
        try {
            Files.writeString(mdFilePath, htmlAsStr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String addMetaInfo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        String link = "[_оригинал_](%s)".formatted(linkLine.url());
        String date = "редакция %s".formatted(linkLine.getDatePublished().format(formatter));

        return "_%s_, %s".formatted(date, link);
    }

    private String addBackLink() {
        String backToCourse = "[курс](%s)".formatted(COURSE_URL);
        String backToRoadmap = "[roadmap](%s)".formatted(ROADMAP_URL);

        return "*назад на %s / %s*\n\n".formatted(backToCourse, backToRoadmap);
    }

    private String addHeaderImg() {
        return "![](%s)\n\n".formatted(HEADER_IMG_URL);
    }

    private void editHeader() {
        mdAsStr = addHeaderImg()
                .concat(addBackLink())
                .concat("***\n\n")
                .concat(mdAsStr)
                .concat("\n\n***\n\n")
                .concat(addBackLink());
    }

    private void editFooter() {
        mdAsStr = mdAsStr.concat("***\n\n").concat(addMetaInfo());
    }

    private void addJavaTag() {
        StringBuilder builder = new StringBuilder(mdAsStr);

        Pattern pattern = Pattern.compile("```");
        Matcher matcher = pattern.matcher(mdAsStr);

        int count = 0;
        while (matcher.find()) {
            count++;
            if (count % 2 != 0) {
                builder.replace(matcher.start(), matcher.end(), "`*`");
            }
        }

        mdAsStr = builder.toString().replaceAll("`\\*`", "```java");
    }
}
