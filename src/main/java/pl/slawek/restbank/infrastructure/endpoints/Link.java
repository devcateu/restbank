package pl.slawek.restbank.infrastructure.endpoints;

public class Link {
    private String type;
    private String href;
    private String rel;

    public Link(String type, String href, String rel) {
        this.type = type;
        this.href = href;
        this.rel = rel;
    }

    public String getType() {
        return type;
    }

    public String getHref() {
        return href;
    }

    public String getRel() {
        return rel;
    }
}
