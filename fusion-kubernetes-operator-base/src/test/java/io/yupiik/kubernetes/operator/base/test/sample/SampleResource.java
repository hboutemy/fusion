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
package io.yupiik.kubernetes.operator.base.test.sample;

import io.yupiik.fusion.framework.build.api.json.JsonModel;
import io.yupiik.kubernetes.operator.base.impl.MetadataLike;
import io.yupiik.kubernetes.operator.base.impl.ObjectLike;

import java.util.Map;

@JsonModel
public record SampleResource(Metadata metadata, Spec spec) implements ObjectLike {
    @JsonModel
    public record Metadata(String uid,
                           String name, String namespace,
                           Map<String, String> labels, Map<String, Object> annotations) implements MetadataLike {
    }

    @JsonModel
    public record Spec(String message) {
    }
}
