package pl.slawek.restbank.infrastructure.endpoints.rest;

import java.util.List;

public class RestfullResponse {
    private Object data;
    private List<Link> links;

    public RestfullResponse(Object data, List<Link> links) {
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
