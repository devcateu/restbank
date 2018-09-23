package pl.slawek.restbank.infrastructure.endpoints.rest;

public class EndpointDefinition {
    private final String path;
    private final HttpMethod httpMethod;

    public EndpointDefinition(String path, HttpMethod httpMethod) {
        this.path = path;
        this.httpMethod = httpMethod;
    }

    public String path() {
        return path;
    }

    public HttpMethod httpMethod() {
        return httpMethod;
    }
}
