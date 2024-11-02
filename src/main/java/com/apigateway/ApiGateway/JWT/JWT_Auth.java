package com.apigateway.ApiGateway.JWT;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JWT_Auth extends AbstractVerticle {
  private static final Logger log = LoggerFactory.getLogger(JWT_Auth.class);

  public static void getAuth(Router route, JWTAuth jwtAuth, WebClient client){

    route.post("/api/login").handler(ctx -> {
      JsonObject credentials = ctx.body().asJsonObject();
      String email = credentials.getString("email");
      String password = credentials.getString("password");

      client.postAbs("http://localhost:8888/readUserAccount")
        .sendJsonObject(ctx.body().asJsonObject())
        .onComplete(result -> {
          if(result.succeeded() && result.result().statusCode() == 200){

            JsonObject account = result.result()
              .bodyAsJsonObject();

            if(email.equals(account.getString("email")) && password.equals(account.getString("password"))){
              // Authentication successful
              String token = jwtAuth.generateToken(
                account,
                new JWTOptions().setExpiresInMinutes(10));

              ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject().put("token", token)
                  .put("account", account)
                  .encode());

              log.info("Token generated....");
            }else{
              // Authentication failed
              log.error("Unauthorized");
              ctx.response().setStatusCode(401).end("Unauthorized");
            }
          }else{
            // Authentication failed
            log.error("Unauthorized");
            ctx.response().setStatusCode(401).end("Unauthorized");
          }
        });
    });

    route.post("/api/refresh").handler(ctx -> {
      JsonObject credentials = ctx.body().asJsonObject();
      String email = credentials.getString("email");
      String password = credentials.getString("password");

      client.postAbs("http://localhost:8888/readUserAccount")
        .sendJsonObject(ctx.body().asJsonObject())
        .onComplete(result -> {
          if(result.succeeded()){

            JsonObject account = result.result()
              .bodyAsJsonObject();

            if(email.equals(account.getString("email")) && password.equals(account.getString("password"))){
              // Authentication successful
              String token = jwtAuth.generateToken(
                account,
                new JWTOptions().setExpiresInMinutes(10));

              ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject().put("token", token)
                  .encode());

              log.info("Token generated....");
            }else{
              // Authentication failed
              ctx.response().setStatusCode(401).end("Unauthorized");
            }
          }else{
            // Authentication failed
            ctx.response().setStatusCode(401).end("Unauthorized");
          }
        });
    });
  }

}
