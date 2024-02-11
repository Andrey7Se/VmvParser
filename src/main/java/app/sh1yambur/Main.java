package app.sh1yambur;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String INPUT_MD = "src/files/roadmap_original.md";
    private static final String OUTPUT_LINKS = "src/files/links.txt";
    private static final String LESSONS_DIR = "src/files/lessons/";
    private static final String OUTPUT_DIR = "src/files/";

    private static List<LinkLine> linkLineList;

    public static void main(String[] args) {
//        test();
//        mock();

        Log.write().info("start");

        extractLinks();
        downloadPages();
        convert();
        linkContents();

        Log.write().info("finish");
    }

    // try any shit code here
    private static void test() {

    }

    private static void mock() {
        if (linkLineList == null) {
            linkLineList = new ArrayList<>();
        }

        /*linkLineList.add(new LinkLine(
                "47",
                "Ссылка на метод",
                "Ssylka-na-metod",
                "https://telegra.ph/Ssylka-na-metod-02-12"
        ));*/


        linkLineList.add(new LinkLine(
                "97",
                "DML. JOIN",
                "DML-JOIN",
                "https://telegra.ph/DML-JOIN-08-06"
        ));

        /*linkLineList.add(new LinkLine(
                "42",
                "Структура данных Дерево",
                "Struktura-dannyh-Derevo",
                "https://telegra.ph/Struktura-dannyh-Derevo-01-28"
        ));*/

        linkLineList.add(new LinkLine(
                "57.2",
                "Stream API. collect(), Collector, Collectors. Часть II",
                "Stream-API-collect-Collector-Collectors-CHast-II",
                "https://telegra.ph/Stream-API-collect-Collector-Collectors-CHast-II-03-17"
        ));

    }

    private static void linkContents() {
        Linker linker = new Linker(Paths.get(OUTPUT_DIR), Paths.get(INPUT_MD), linkLineList);
        linker.execute();
    }

    private static void extractLinks() {
        LinksExtractor extractor = new LinksExtractor(INPUT_MD, OUTPUT_LINKS);
        extractor.extract();

        linkLineList = extractor.getLinkLineList();
    }

    private static void downloadPages() {
        Downloader loader = new Downloader(Paths.get(LESSONS_DIR));
        linkLineList.forEach(loader::downloadPage);
    }

    private static void convert() {
        Converter converter = new Converter(Paths.get(LESSONS_DIR));
        linkLineList.forEach(converter::execute);
    }
}