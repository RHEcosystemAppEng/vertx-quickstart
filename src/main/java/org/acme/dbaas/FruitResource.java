package org.acme.dbaas;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.event.Observes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import java.net.URI;

@Path("fruits")
public class FruitResource {

    private final PgPool client;
    private final boolean schemaCreate;

    public FruitResource(PgPool client, @ConfigProperty(name = "myapp.schema.create", defaultValue = "true") boolean schemaCreate) {
        this.client = client;
        this.schemaCreate = schemaCreate;
    }

    void initdb(@Observes StartupEvent ev) {
        if (schemaCreate) {
            client.query("CREATE TABLE IF NOT EXISTS fruit(id varchar(100) PRIMARY KEY , name varchar(100), quantity varchar(11) null, description varchar(200) null)").execute()
              .flatMap(r -> client.query("INSERT INTO fruit(id, name) VALUES ('1', 'Cherry')").execute())
              .flatMap(r -> client.query("INSERT INTO fruit(id, name) VALUES ('2', 'Apple')").execute())
              .flatMap(r -> client.query("INSERT INTO fruit(id, name) VALUES ('3', 'Banana')").execute())
              .await().indefinitely();
        }
    }

    @GET
    public Uni<Response> get() {
        return Fruit.findAll(client)
          .onItem().transform(Response::ok)
          .onItem().transform(ResponseBuilder::build);
    }

    @GET
    @Path("{id}")
    public Uni<Response> getSingle(String id) {
        return Fruit.findById(client, id)
          .onItem().transform(fruit -> fruit != null ? Response.ok(fruit) : Response.status(Status.NOT_FOUND))
          .onItem().transform(ResponseBuilder::build);
    }

    @POST
    public Uni<Response> create(Fruit fruit) {
        return fruit.save(client)
          .onItem().transform(id -> URI.create("/fruits/" + id))
          .onItem().transform(uri -> Response.created(uri).build());
    }

    @PUT
    @Path("{id}")
    public Uni<Response> update(String id, Fruit fruit) {
        return fruit.update(client, id)
          .onItem().transform(updated -> updated ? Status.OK : Status.NOT_FOUND)
          .onItem().transform(status -> Response.status(status).build());
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(String id) {
        return Fruit.delete(client, id)
          .onItem().transform(deleted -> deleted ? Status.NO_CONTENT : Status.NOT_FOUND)
          .onItem().transform(status -> Response.status(status).build());
    }
}
