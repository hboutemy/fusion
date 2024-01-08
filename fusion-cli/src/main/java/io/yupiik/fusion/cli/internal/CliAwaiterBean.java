/*
 * Copyright (c) 2022 - present - Yupiik SAS - https://www.yupiik.com
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
package io.yupiik.fusion.cli.internal;

import io.yupiik.fusion.cli.CliAwaiter;
import io.yupiik.fusion.framework.api.Instance;
import io.yupiik.fusion.framework.api.RuntimeContainer;
import io.yupiik.fusion.framework.api.configuration.Configuration;
import io.yupiik.fusion.framework.api.container.bean.BaseBean;
import io.yupiik.fusion.framework.api.main.Args;
import io.yupiik.fusion.framework.api.scope.DefaultScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CliAwaiterBean extends BaseBean<CliAwaiter> {
    public CliAwaiterBean() {
        super(CliAwaiter.class, DefaultScoped.class, 1000, Map.of());
    }

    @Override
    public CliAwaiter create(final RuntimeContainer container, final List<Instance<?>> dependents) {
        return new CliAwaiter(
                lookup(container, Args.class, dependents),
                lookup(container, Configuration.class, dependents),
                new ArrayList<>(lookups(
                        container, CliCommand.class,
                        l -> l.stream().map(i -> (CliCommand<? extends Runnable>) i.instance()).toList(),
                        dependents)));
    }
}
