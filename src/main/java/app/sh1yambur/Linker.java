package app.sh1yambur;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Linker {
    private static final String LESSONS_FILE_NAME = "course.md";
    private static final String ROADMAP_FILE_NAME = "roadmap.md";
    private static final String LESSONS_DIR = "lessons";

    private static final Pattern LINE_PATTERN = Pattern.compile("\\*\\*(.*?)\\)");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\*\\*(.*?)\\*\\*.*");
    private static final Pattern URL_PATTERN = Pattern.compile("]\\((.*?)\\)");

    private final Path outputDir;
    private final Path roadmapOriginalFilePath;
    private final List<LinkLine> linkLineList;

    private final Path courseFilePath;
    private final Path roadmapFilePath;

    public Linker(Path outputDir, Path roadmapOriginalFilePath, List<LinkLine> linkLineList) {
        this.outputDir = outputDir;
        this.roadmapOriginalFilePath = roadmapOriginalFilePath;
        this.linkLineList = linkLineList;

        this.courseFilePath = outputDir.resolve(LESSONS_FILE_NAME);
        this.roadmapFilePath = outputDir.resolve(ROADMAP_FILE_NAME);
    }

    public void execute() {
        try {
            createCourseFile();
            Log.write().info("file \"{}\" created", courseFilePath);

            createRoadmapFile();
            Log.write().info("file \"{}\" rewrote", roadmapFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createCourseFile() throws IOException {
        linkLineList.sort(Comparator.comparing(ll -> Double.parseDouble(ll.number())));

        if (Files.notExists(courseFilePath)) {
            Files.createFile(courseFilePath);
        }

        linkLineList.forEach(ll -> {
            try {
                Files.writeString(courseFilePath, getLinkAsString(ll), StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private String getLinkAsString(LinkLine linkLine) {
        return "**%s**. [%s](%s)\n\n".formatted(linkLine.number(), linkLine.title(), getPathToLessonMd(linkLine));
    }

    private String getPathToLessonMd(LinkLine linkLine) {
        return "./%s/%s/%s.md".formatted(LESSONS_DIR, linkLine.number(), linkLine.filename());
    }

    private void createRoadmapFile() {
        try {
            if (Files.notExists(roadmapFilePath)) {
                Files.createFile(roadmapFilePath);
            }

            List<String> mdLinesList = Files.readAllLines(roadmapOriginalFilePath);

            for (String str : mdLinesList) {
                Files.writeString(roadmapFilePath, checkAndEditString(str), StandardOpenOption.APPEND);
                Files.writeString(roadmapFilePath, checkAndEditString("\n"), StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String checkAndEditString(String str) {
        if (LINE_PATTERN.matcher(str).find()) {
            String number = parse(str, NUMBER_PATTERN);
            String oldUrl = parse(str, URL_PATTERN);

            return str.replace(oldUrl, getUrlByNumber(number));
        }

        return str;
    }

    private String parse(String str, Pattern pattern) {
        String result = "default";
        Matcher matcher = pattern.matcher(str);

        if (matcher.find()) {
            result = matcher.group(1);
        }

        return result;
    }

    public String getUrlByNumber(String number) {
        for (LinkLine linkLine : linkLineList) {
            if (number.equalsIgnoreCase(linkLine.number())) {
                return getPathToLessonMd(linkLine);
            }
        }

        return "no_file";
    }
}
