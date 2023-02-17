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
package io.yupiik.fusion.persistence.impl;

import io.yupiik.fusion.persistence.api.Database;
import io.yupiik.fusion.persistence.api.StatementBinder;
import io.yupiik.fusion.persistence.impl.entity.AutoIncrementEntity;
import io.yupiik.fusion.persistence.impl.entity.AutoIncrementEntityModel;
import io.yupiik.fusion.persistence.impl.entity.SimpleFlatEntity;
import io.yupiik.fusion.persistence.impl.entity.SimpleFlatEntityModel;
import io.yupiik.fusion.persistence.impl.entity.operation.MyOpsImpl;
import io.yupiik.fusion.persistence.impl.translation.H2Translation;
import io.yupiik.fusion.persistence.test.EnableH2;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import static io.yupiik.fusion.persistence.api.StatementBinder.NONE;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseImplTest {
    @Test
    @EnableH2
    void operations(final DataSource dataSource) throws SQLException {
        final var database = init(dataSource);

        for (int i = 0; i < 3; i++) { // seed data
            final var instance = new SimpleFlatEntity(null, "test_" + i, 0);
            database.insert(instance);
        }

        final var ops = new MyOpsImpl(database);
        final var all = ops.findAll();
        assertEquals(3, all.size());
        assertEquals("test_0", all.get(0).name());
        assertEquals(all.get(0), ops.findOne("test_0"));
        assertEquals(all.get(0), ops.findOneWithPlaceholders("test_0"));
        assertEquals(all.subList(0, 2), ops.findByName(List.of("test_0", "test_1")));

        final IntSupplier counter = () -> database.query(SimpleFlatEntity.class, "select name, id, SIMPLE_AGE from SIMPLE_FLAT_ENTITY order by name", StatementBinder.NONE).size();
        assertEquals(1, ops.delete("test_1"), () -> ops.findAll().toString());
        assertEquals(2, ops.countAll(), () -> ops.findAll().toString());
        assertEquals(counter.getAsInt(), ops.countAll(), () -> ops.findAll().toString());
        ops.deleteWithoutReturnedValue("test_0");
        assertEquals(1, counter.getAsInt(), () -> ops.findAll().toString());
        assertEquals(1, ops.delete("test_%"), () -> ops.findAll().toString());
        assertEquals(0, counter.getAsInt(), () -> ops.findAll().toString());
    }

    @Test
    @EnableH2
    void autoincrement(final DataSource dataSource) throws SQLException {
        final var database = init(dataSource);
        final var entity = database.getOrCreateEntity(AutoIncrementEntity.class);
        final var table = entity.getTable();
        try (final var connection = dataSource.getConnection();
             final var stmt = connection.createStatement()) {
            stmt.executeUpdate(entity.ddl()[0]);
        }

        assertEquals(0, count(dataSource, table));
        for (int i = 0; i < 3; i++) { // seed data
            assertEquals(i + 1, database.insert(new AutoIncrementEntity(0, "test_i")).id());
        }
        assertEquals(3, count(dataSource, table));

        database.execute("DELETE FROM " + table, NONE);
        assertEquals(0, count(dataSource, table));
    }

    @Test
    @EnableH2
    void execute(final DataSource dataSource) throws SQLException {
        final var database = init(dataSource);

        for (int i = 0; i < 3; i++) { // seed data
            final var instance = new SimpleFlatEntity(null, "test_" + i, 0);
            database.insert(instance);
        }

        final var all = database.query(SimpleFlatEntity.class, "select name, id, SIMPLE_AGE from SIMPLE_FLAT_ENTITY order by name", StatementBinder.NONE);
        assertEquals(3, all.size());

        assertEquals(3, database.execute("delete from SIMPLE_FLAT_ENTITY where name like ?", b -> b.bind("test%")));
        assertEquals(0, database.query(SimpleFlatEntity.class, "select name, id, SIMPLE_AGE from SIMPLE_FLAT_ENTITY order by name", StatementBinder.NONE).size());
    }

    @Test
    @EnableH2
    void findAll(final DataSource dataSource) throws SQLException {
        final var database = init(dataSource);

        final var entities = new ArrayList<SimpleFlatEntity>();
        for (int i = 0; i < 3; i++) { // seed data
            entities.add(database.insert(new SimpleFlatEntity(null, "test_" + i, 0)));
        }
        // query
        final var all = database.query(SimpleFlatEntity.class, "select name, id, SIMPLE_AGE from SIMPLE_FLAT_ENTITY order by name", StatementBinder.NONE);

        // cleanup
        entities.forEach(database::delete);

        // asserts
        assertEquals(entities, all);
    }

    @Test
    @EnableH2
    void findAllUsingAliases(final DataSource dataSource) throws SQLException {
        final var database = init(dataSource);

        final var entities = new ArrayList<SimpleFlatEntity>();
        for (int i = 0; i < 3; i++) { // seed data
            entities.add(database.insert(new SimpleFlatEntity(null, "test_" + i, 0)));
        }
        // query
        final var entity = database.getOrCreateEntity(SimpleFlatEntity.class);
        final var all = database.query(
                "select name as pName, id as pId, SIMPLE_AGE as pSIMPLE_AGE from SIMPLE_FLAT_ENTITY order by pName", StatementBinder.NONE,
                r -> {
                    final var binder = entity
                            .mapFromPrefix("p", r.get()); // this can be cached in the context of this query (caller code) if query is stable
                    return r.mapAll(binder::apply);
                });

        // cleanup
        entities.forEach(database::delete);

        // asserts
        assertEquals(entities, all);
    }

    @Test
    @EnableH2
    void findWithBinding(final DataSource dataSource) throws SQLException {
        final var database = init(dataSource);

        final var entities = new ArrayList<SimpleFlatEntity>();
        for (int i = 0; i < 3; i++) { // seed data
            entities.add(database.insert(new SimpleFlatEntity(null, "test_" + i, 0)));
        }
        // query
        final var all = database.query(SimpleFlatEntity.class, "select name, id, SIMPLE_AGE from SIMPLE_FLAT_ENTITY where name = ?", b -> b.bind("test_1"));

        // cleanup
        entities.forEach(database::delete);

        // asserts
        assertEquals(entities.subList(1, 2), all);
    }

    @Test
    @EnableH2
    void batch(final DataSource dataSource) throws SQLException {
        final var database = init(dataSource);

        final var entities = new ArrayList<SimpleFlatEntity>();
        for (int i = 0; i < 3; i++) { // seed data
            entities.add(new SimpleFlatEntity("test_" + i, "test_" + i, 0));
        }

        assertArrayEquals(
                new int[]{1, 1, 1},
                database.batch(
                        "insert into SIMPLE_FLAT_ENTITY(id, SIMPLE_AGE, name) values(?, ?, ?)",
                        entities.stream()
                                .map(it -> (Consumer<StatementBinder>) binder -> {
                                    binder.bind(String.class, it.id());
                                    binder.bind(int.class, it.age());
                                    binder.bind(String.class, it.name());
                                })
                                .iterator()));

        // check all was insert by batch
        final var all = database.query(SimpleFlatEntity.class, "select name, id, SIMPLE_AGE from SIMPLE_FLAT_ENTITY order by name", StatementBinder.NONE);
        entities.forEach(database::delete);
        assertEquals(entities, all);
    }

    @Test
    @EnableH2
    void querySingle(final DataSource dataSource) throws SQLException {
        final var database = init(dataSource);

        final var entities = new ArrayList<SimpleFlatEntity>();
        for (int i = 0; i < 3; i++) { // seed data
            entities.add(database.insert(new SimpleFlatEntity("test_" + i, "test_" + i, 0)));
        }

        try {
            assertTrue(database.querySingle(SimpleFlatEntity.class, "select name, id, SIMPLE_AGE from SIMPLE_FLAT_ENTITY where name like ?", b -> b.bind("test_%")).isEmpty());
            assertEquals(entities.get(0), database.querySingle(SimpleFlatEntity.class, "select name, id, SIMPLE_AGE from SIMPLE_FLAT_ENTITY where name like ?", b -> b.bind("test_0")).orElseThrow());
        } finally {
            entities.forEach(database::delete);
        }
    }

    @Test
    @EnableH2
    void guessTranslation(final DataSource dataSource) {
        final var configuration = new DatabaseConfiguration().setDataSource(dataSource);
        final var database = new DatabaseImpl(configuration);
        assertEquals(H2Translation.class, database.getTranslation().getClass());
    }

    @Test
    @EnableH2
    void ddl(final DataSource dataSource) throws SQLException {
        final var database = init(dataSource); // runs ddl so validates syntax is correct
        assertEquals(List.of("SIMPLE_FLAT_ENTITY"), listTables(dataSource));

        final var entity = database.getOrCreateEntity(SimpleFlatEntity.class);
        final var ddl = entity.ddl();
        assertArrayEquals(new String[]{
                "create table SIMPLE_FLAT_ENTITY (id VARCHAR(16), name VARCHAR(16), SIMPLE_AGE integer)"
        }, ddl);
    }

    @Test
    @EnableH2
    void crud(final DataSource dataSource) throws SQLException {
        final var database = init(dataSource);

        // insert
        var instance = database.insert(new SimpleFlatEntity(null, "test", 0));
        assertEquals(1, count(dataSource));
        assertEquals("SimpleFlatEntity[id=test, name=test, age=1]", instance.toString());

        // find
        final var firstLookup = database.findById(SimpleFlatEntity.class, "test");
        assertEquals(instance.toString(), firstLookup.toString());

        // update
        instance = database.update(new SimpleFlatEntity("test", "test", 35));
        assertEquals(1, count(dataSource));
        assertEquals(instance.toString(), database.findById(SimpleFlatEntity.class, "test").toString());

        // insert another one
        final var instance2 = database.insert(new SimpleFlatEntity(null, "test2", 0));
        assertEquals(2, count(dataSource));

        // delete
        database.delete(instance);
        assertEquals(1, count(dataSource));
        database.delete(instance2);
        assertEquals(0, count(dataSource));
    }

    @Test
    @EnableH2
    void onLoad(final DataSource dataSource) throws SQLException {
        final var database = init(dataSource);

        final var instance = database.insert(new SimpleFlatEntity(null, "loaded", 0));
        assertEquals(1, count(dataSource));
        assertEquals("SimpleFlatEntity[id=loaded, name=loaded, age=1]", instance.toString());

        final var firstLookup = database.findById(SimpleFlatEntity.class, "loaded");
        assertEquals("SimpleFlatEntity[id=loaded, name=loaded, age=1]", firstLookup.toString());

        database.delete(instance);
    }

    private Database init(final DataSource dataSource) throws SQLException {
        final var configuration = new DatabaseConfiguration();
        configuration
                .setDataSource(dataSource)
                .setInstanceLookup(k -> k == SimpleFlatEntity.class ?
                        new SimpleFlatEntityModel(configuration) :
                        (k == AutoIncrementEntity.class ? new AutoIncrementEntityModel(configuration) :
                                requireNonNull(null, () -> "No entity '" + k + "'")));
        final var database = Database.of(configuration);
        final var entity = database.getOrCreateEntity(SimpleFlatEntity.class);

        // ddl
        try (final var connection = dataSource.getConnection();
             final var stmt = connection.createStatement()) {
            for (final var sql : entity.ddl()) {
                stmt.execute(sql);
            }
        }

        assertEquals(0, count(dataSource));
        return database;
    }

    private List<String> listTables(final DataSource dataSource) throws SQLException {
        try (final var connection = dataSource.getConnection();
             final var stmt = connection.createStatement();
             final var set = stmt.executeQuery("SHOW TABLES")) {
            final var tables = new ArrayList<String>();
            while (set.next()) {
                tables.add(set.getString("TABLE_NAME"));
            }
            return tables;
        }
    }

    private long count(final DataSource dataSource, final String table) throws SQLException {
        try (final var connection = dataSource.getConnection();
             final var stmt = connection.createStatement();
             final var set = stmt.executeQuery("SELECT count(*) FROM " + table)) {
            if (set.next()) {
                return set.getLong(1);
            }
        }
        throw new IllegalStateException("no count");
    }

    private long count(final DataSource dataSource) throws SQLException {
        return count(dataSource, "SIMPLE_FLAT_ENTITY");
    }
}
