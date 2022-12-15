package test.p;

import io.yupiik.fusion.framework.build.api.json.JsonModel;
import io.yupiik.fusion.framework.build.api.jsonrpc.JsonRpc;
import io.yupiik.fusion.framework.build.api.jsonrpc.JsonRpcParam;
import io.yupiik.fusion.http.server.api.Request;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.joining;

public class JsonRpcEndpoints {
    @JsonRpc("test1")
    public MyResult result() {
        return new MyResult("test1");
    }

    @JsonRpc("test2")
    public CompletionStage<MyResult> asynResult() {
        return completedFuture(new MyResult("test2"));
    }

    @JsonRpc("arg")
    public CompletionStage<MyResult> arg(@JsonRpcParam("wrapper") final MyInput input) {
        return completedFuture(new MyResult(new StringBuilder(input.name()).reverse().toString()));
    }

    @JsonRpc("req")
    public CompletionStage<MyResult> req(final MyInput input, final Request request) {
        return completedFuture(new MyResult(input.name() + " (" + request.path() + ")"));
    }

    @JsonRpc("fail")
    public CompletionStage<MyResult> fail(final boolean direct) {
        if (direct) {
            throw new IllegalStateException("oops for test");
        }
        final var future = new CompletableFuture<MyResult>();
        future.completeExceptionally(new IllegalStateException("oops for test [promise]"));
        return future;
    }

    @JsonRpc("paramTypes")
    public String paramTypes(final Object object,
                             final boolean bool,
                             final Boolean boolWrapper,
                             final int integer,
                             final Integer intWrapper,
                             final long longNumber,
                             final Long longWrapper,
                             final String string,
                             final MyInput model,
                             final List<Object> objectList,
                             final List<Boolean> boolWrapperList,
                             final List<Integer> intWrapperList,
                             final List<Long> longWrapperList,
                             final List<String> stringList,
                             final List<MyInput> modelList,
                             final Map<String, Object> objectMap,
                             final Map<String, Boolean> boolWrapperMap,
                             final Map<String, Integer> intWrapperMap,
                             final Map<String, Long> longWrapperMap,
                             final Map<String, String> stringMap,
                             final Map<String, MyInput> modelMap) {

        return Stream.of(
                        object, bool, boolWrapper, integer, intWrapper, longNumber, longWrapper, string, model,
                        objectList, boolWrapperList, intWrapperList, longWrapperList, stringList, modelList,
                        objectMap, boolWrapperMap, intWrapperMap, longWrapperMap, stringMap, modelMap)
                .map(String::valueOf)
                .collect(joining("\n"));
    }

    @JsonModel
    public record MyResult(String name) {
    }

    @JsonModel
    public record MyInput(String name) {
    }
}
