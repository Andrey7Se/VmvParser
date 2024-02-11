package app.sh1yambur;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinksExtractor {
    private static final boolean IS_ONLY_URL = false; // extract to file only http-url or all data from lines

    private static final Pattern FULL_PATTERN = Pattern.compile("\\*\\*(.*?)\\)");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\*\\*(.*?)\\*\\*.*");
    private static final Pattern TITLE_PATTERN = Pattern.compile("\\s\\[(.*?)]");
    private static final Pattern URL_PATTERN = Pattern.compile("]\\((.*?)\\)");

    private final String inputFile;
    private final String outputFile;

    private final List<LinkLine> linkLineList = new ArrayList<>();

    public LinksExtractor(String inputFile, String outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    public List<LinkLine> getLinkLineList() {
        return linkLineList;
    }

    public void extract() {
        readFile();
        writeFile();
    }

    private void writeFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            int count = 0;

            for (LinkLine linkLine : linkLineList) {
                if (IS_ONLY_URL) {
                    writer.write(linkLine.url());
                } else {
                    writer.write(linkLine.toString());
                }

                writer.write(System.lineSeparator());
                count++;
            }

            Log.write().info("OutputFile created. {} line(s) wrote", count);
        } catch (IOException e) {
            Log.write().error(e);
        }
    }

    private LinkLine createLinkLine(String inputString) {
        String number = parse(inputString, NUMBER_PATTERN);

        String title = parse(inputString, TITLE_PATTERN);
        // clean of unnecessary symbols
        title = title.replaceAll("_", "");
        title = title.replaceAll("\\*\\*", "");

        String url = parse(inputString, URL_PATTERN);

        String filename = getFileNameFromUrl(url);

        return new LinkLine(number, title, filename, url);
    }

    private String parse(String str, Pattern pattern) {
        String result = "default";
        Matcher matcher = pattern.matcher(str);

        if (matcher.find()) {
            result = matcher.group(1);
        }

        return result;
    }

    private void readFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            while (reader.ready()) {
                String line = reader.readLine();

                if (FULL_PATTERN.matcher(line).find()) {
                    LinkLine linkLine = createLinkLine(line);

                    if (isNotExistLinkLine(linkLine)) {
                        linkLineList.add(linkLine);
                    }
                }
            }
        } catch (IOException e) {
            Log.write().error(e);
        }

        Log.write().info("InputFile read. {} record(s) added to list", linkLineList.size());
    }

    // extract filename from url
    private String getFileNameFromUrl(String url) {
        return url
                .replaceAll("https://telegra.ph/", "")
                .replaceFirst("-\\d{2}-\\d{2}", "");
    }

    private boolean isNotExistLinkLine(LinkLine linkLine) {
        for (LinkLine ll : linkLineList) {
            if (linkLine.number().equals(ll.number())) {
                return false;
            }
        }

        return true;
    }
}
