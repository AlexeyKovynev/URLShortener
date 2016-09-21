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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LongShortDate that = (LongShortDate) o;

        if (longLink != null ? !longLink.equals(that.longLink) : that.longLink != null)
            return false;
        return shortLink != null ? shortLink.equals(that.shortLink) : that.shortLink == null;

    }

    @Override
    public int hashCode() {
        int result = longLink != null ? longLink.hashCode() : 0;
        result = 31 * result + (shortLink != null ? shortLink.hashCode() : 0);
        return result;
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