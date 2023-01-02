/*
 * Copyright (c) 2022-2023 - Yupiik SAS - https://www.yupiik.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.yupiik.fusion.jsonrpc.impl.bean;

import io.yupiik.fusion.framework.api.Instance;
import io.yupiik.fusion.framework.api.RuntimeContainer;
import io.yupiik.fusion.framework.api.container.bean.BaseBean;
import io.yupiik.fusion.framework.api.scope.ApplicationScoped;
import io.yupiik.fusion.jsonrpc.JsonRpcRegistry;
import io.yupiik.fusion.jsonrpc.impl.JsonRpcMethod;

import java.util.List;
import java.util.Map;

public class JsonRpcRegistryBean extends BaseBean<JsonRpcRegistry> {
    public JsonRpcRegistryBean() {
        super(JsonRpcRegistry.class, ApplicationScoped.class, 1000, Map.of());
    }

    @Override
    public JsonRpcRegistry create(final RuntimeContainer container, final List<Instance<?>> dependents) {
        return new JsonRpcRegistry(
                lookups(container, JsonRpcMethod.class, l -> l.stream().map(Instance::instance).toList(), dependents));
    }
}
