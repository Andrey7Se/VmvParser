package app.sh1yambur;

import java.time.LocalDateTime;

public final class LinkLine {
    private final String number;
    private final String title;
    private final String filename;
    private final String url;

    private LocalDateTime datePublished;

    public LinkLine(String number, String title, String filename, String url) {
        this.number = number;
        this.title = title;
        this.filename = filename;
        this.url = url;
        this.datePublished = LocalDateTime.parse("1990-01-01T00:00:00");
    }

    @Override
    public String toString() {
        return "%s | %s | %s | %s".formatted(number, title, filename, url);
    }

    public String number() {
        return number;
    }

    public String title() {
        return title;
    }

    public String filename() {
        return filename;
    }

    public String url() {
        return url;
    }

    public LocalDateTime getDatePublished() {
        return datePublished;
    }

    public void setDatePublished(LocalDateTime datePublished) {
        this.datePublished = datePublished;
    }
}
