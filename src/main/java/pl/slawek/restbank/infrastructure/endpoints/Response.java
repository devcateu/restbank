package pl.slawek.restbank.infrastructure.endpoints;

import java.util.List;

public class Response {
    private Object data;
    private List<Link> links;

    public Response(Object data, List<Link> links) {
        this.data = data;
        this.links = links;
    }

    public Object getData() {
        return data;
    }

    public List<Link> getLinks() {
        return links;
    }
}
