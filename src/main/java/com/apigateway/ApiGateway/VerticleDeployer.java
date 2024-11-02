package com.apigateway.ApiGateway;

import io.vertx.core.Vertx;

public class VerticleDeployer {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new ApiGateway());
  }
}
