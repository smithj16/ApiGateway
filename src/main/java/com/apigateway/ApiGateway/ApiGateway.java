package com.apigateway.ApiGateway;


import com.apigateway.ApiGateway.JWT.JWT_Auth;
import com.apigateway.ApiGateway.Services.AnimeEvents;
import io.vertx.core.AbstractVerticle;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.PfxOptions;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;

public class ApiGateway extends AbstractVerticle {
  @Override
  public void start() {

    // Set up the HTTP server options with SSL
    HttpServerOptions options = new HttpServerOptions()
      .setSsl(true)
      .setPfxKeyCertOptions(new PfxOptions()
        .setPath("C:\\Users\\jacob\\keystore.p12")
        .setPassword("secret"));

    WebClient client = WebClient.create(vertx);

    // Set up JWT authentication using the keystore
    JWTAuthOptions jwtAuthOptions = new JWTAuthOptions()
      .setKeyStore(new KeyStoreOptions()
        .setPath("C:\\Users\\jacob\\keystore.p12")
        .setPassword("secret")
        .setType("pkcs12"));

    JWTAuth jwtAuth = JWTAuth.create(vertx, jwtAuthOptions);

    Router router = Router.router(vertx);

    Handler<RoutingContext> corsHandler = CorsHandler.create()
      .allowedMethod(HttpMethod.OPTIONS)
      .allowedMethod(HttpMethod.POST)
      .allowedMethod(HttpMethod.DELETE)
      .allowedMethod(HttpMethod.PUT)
      .allowedMethod(HttpMethod.GET);

    // Parse request bodies
    router.route().handler(corsHandler).handler(BodyHandler.create());

    JWT_Auth.getAuth(router, jwtAuth, client);

    // Protected route
    router.route("/api/secure/*").handler(JWTAuthHandler.create(jwtAuth));

    router.get("/api/secure/hello").handler(ctx -> {
      ctx.response()
        .putHeader("Access-Control-Allow-Origin: ", "*")
        .end("This is a secure resource");
    });

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(8443, res -> {
        if (res.succeeded()) {
          System.out.println("Server is now listening on port 8443");
          AnimeEvents.attach(router, client);
        } else {
          System.out.println("Failed to bind!");
        }
      });
  }
}
