package name.javalex.apijson.app.urlshortener.entities;

public class LongShortDate {
    private String longLink;
    private String shortLink;
    private String dateAndTime;
    private int id;

    @Override
    public String toString() {
        return "LongShortDate{" +
                "longLink='" + longLink + '\'' +
                ", shortLink='" + shortLink + '\'' +
                ", dateAndTime='" + dateAndTime + '\'' +
                ", id=" + id +
                '}';
    }

    public LongShortDate(String longLink, String shortLink, String dateAndTime, int id) {
        this.longLink = longLink;
        this.shortLink = shortLink;
        this.dateAndTime = dateAndTime;
        this.id = id;
    }
    public LongShortDate(String longLink, String shortLink) {
        this.longLink = longLink;
        this.shortLink = shortLink;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLongLink() {
        return longLink;
    }

    public void setLongLink(String longLink) {
        this.longLink = longLink;
    }

    public String getShortLink() {
        return shortLink;
    }

    public void setShortLink(String shortLink) {
        this.shortLink = shortLink;
    }

    public String getDateAndTime() {
        return dateAndTime;
    }

    public void setDateAndTime(String dateAndTime) {
        this.dateAndTime = dateAndTime;
    }
}