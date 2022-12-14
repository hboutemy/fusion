package test.p;

import io.yupiik.fusion.framework.build.api.http.HttpMatcher;
import io.yupiik.fusion.framework.build.api.json.JsonModel;
import io.yupiik.fusion.http.server.api.Request;
import io.yupiik.fusion.http.server.api.Response;
import io.yupiik.fusion.json.JsonMapper;

import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class HttpEndpoints {
    private final JsonMapper mapper;

    protected HttpEndpoints() {
        this(null);
    }

    public HttpEndpoints(final JsonMapper mapper) {
        this.mapper = mapper;
    }

    @HttpMatcher(priority = 0)
    public CompletionStage<Response> all() {
        return completedFuture(Response.of().status(201).header("java-method", "all").build());
    }

    @HttpMatcher(methods = "GET")
    public Response get(final Request request) {
        return Response.of()
                .status(202)
                .header("java-method", "get")
                .header("req-m", request.method())
                .build();
    }

    @HttpMatcher(methods = "GET", pathMatching = HttpMatcher.PathMatching.EXACT, path = "/foo")
    public CompletionStage<Response> getAndFooPath() {
        return completedFuture(Response.of().status(203).header("java-method", "getAndFooPath").build());
    }

    @HttpMatcher(methods = "GET", pathMatching = HttpMatcher.PathMatching.ENDS_WITH, path = "/foo")
    public CompletionStage<Response> getAndEndsWithFooPath() {
        return completedFuture(Response.of().status(204).header("java-method", "getAndEndsWithFooPath").build());
    }

    @HttpMatcher(methods = "GET", pathMatching = HttpMatcher.PathMatching.STARTS_WITH, path = "/foo")
    public CompletionStage<Response> getAndStartsWithFooPath() {
        return completedFuture(Response.of().status(205).header("java-method", "getAndStartsWithFooPath").build());
    }

    @HttpMatcher(methods = "GET", pathMatching = HttpMatcher.PathMatching.REGEX, path = "/foo/.+/foo")
    public CompletionStage<Response> getAndRegexFooPath() {
        return completedFuture(Response.of().status(206).header("java-method", "getAndRegexFooPath").build());
    }

    @HttpMatcher(methods = "POST", pathMatching = HttpMatcher.PathMatching.EXACT, path = "/greet")
    public GreetResponse greet(final GreetRequest request) {
        return new GreetResponse("Hello " + request.name() + "!");
    }

    @JsonModel public record GreetRequest(String name) {}
    @JsonModel public record GreetResponse(String message) {}
}
