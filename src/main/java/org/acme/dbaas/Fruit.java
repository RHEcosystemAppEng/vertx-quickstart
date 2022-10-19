package org.acme.dbaas;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Fruit {

    public String id;

    public String name;

    public String quantity;

    public Fruit() {
        // default constructor.
    }

    public Fruit(String name, String quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public Fruit(String id, String name, String quantity) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
    }

    public static Uni<List<Fruit>> findAll(PgPool client) {
        return client.query("SELECT id, name, quantity FROM fruit ORDER BY name").execute()
          .onItem().transform(pgRowSet -> {
              List<Fruit> list = new ArrayList<>(pgRowSet.size());
              for (Row row : pgRowSet) {
                  list.add(from(row));
              }
              return list;
          });
    }

    public static Uni<Fruit> findById(PgPool client, String id) {
        return client.preparedQuery("SELECT id, name, quantity FROM fruit WHERE id = $1").execute(Tuple.of(id))
          .onItem().transform(RowSet::iterator)
          .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }

    public Uni<String> save(PgPool client) {
        String id = UUID.randomUUID().toString();
        return client.preparedQuery("INSERT INTO fruit (id, name, quantity) VALUES ($1, $2, $3)").execute(Tuple.of(id, name, quantity))
          .replaceWith(id);
    }

    public Uni<Boolean> update(PgPool client, String id) {
        return client.preparedQuery("UPDATE fruit SET name = $1, quantity = $2 WHERE id = $3").execute(Tuple.of(name, quantity, id))
          .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

    public static Uni<Boolean> delete(PgPool client, String id) {
        return client.preparedQuery("DELETE FROM fruit WHERE id = $1").execute(Tuple.of(id))
          .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

    private static Fruit from(Row row) {
        return new Fruit(row.getString("id"), row.getString("name"), row.getString("quantity"));
    }
}
