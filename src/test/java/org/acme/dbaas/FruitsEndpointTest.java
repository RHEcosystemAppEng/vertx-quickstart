/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.acme.dbaas;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsNot.not;

@QuarkusTest
public class FruitsEndpointTest {

    @Test
    public void testListAllFruits() {
        //List all, should have all 3 fruits the database has initially:
        given()
          .when().get("/fruits")
          .then()
          .statusCode(200)
          .body(
            containsString("Cherry"),
            containsString("Apple"),
            containsString("Banana"));

        //Delete the Cherry:
        given()
          .when().delete("/fruits/1")
          .then()
          .statusCode(204);

        //List all, cherry should be missing now:
        given()
          .when().get("/fruits")
          .then()
          .statusCode(200)
          .body(
            not(containsString("Cherry")),
            containsString("Apple"),
            containsString("Banana"));

        //Create the Pear:
        given()
          .when()
          .body("{\"name\" : \"Pear\"}")
          .contentType("application/json")
          .post("/fruits")
          .then()
          .statusCode(201);

        //List all, cherry should be missing now:
        given()
          .when().get("/fruits")
          .then()
          .statusCode(200)
          .body(
            not(containsString("Cherry")),
            containsString("Apple"),
            containsString("Banana"),
            containsString("Pear"));
    }

}
