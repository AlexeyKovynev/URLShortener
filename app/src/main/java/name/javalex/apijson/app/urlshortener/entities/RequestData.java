package name.javalex.apijson.app.urlshortener.entities;

public class RequestData {
    private String longUrl;

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    @Override
    public String toString() {
        return "RequestData{" +
                "longUrl='" + longUrl + '\'' +
                '}';
    }
}
