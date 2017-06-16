import java.util.HashMap;
import java.util.Map;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class RestfulAPIVerticle extends AbstractVerticle
{

	private Map<String, JsonObject> products = new HashMap<>();

	@Override
	public void start()
	{
		products.put("prod3568", new JsonObject().put("id", "prod3568").put("name", "Egg Whisk").put("price", 3.99).put("weight", 150));
		products.put("prod7340", new JsonObject().put("id", "prod7340").put("name", "Tea Cosy").put("price", 5.99).put("weight", 100));
		products.put("prod8643", new JsonObject().put("id", "prod8643").put("name", "Spatula").put("price", 1.00).put("weight", 80));

		Router router = Router.router(vertx);

		router.route().handler(BodyHandler.create());
		router.get("/products/:productID").handler(this::handleGetProduct);
		router.put("/products/:productID").handler(this::handleAddProduct);
		router.get("/products").handler(this::handleListProducts);

		router.get("/").handler(routingContext ->
		{
			String s = "Available endpoints: \n" + "/products\n/products/:productID\n/products";
			HttpServerResponse response = routingContext.response();
			response.putHeader("content-type", "application/json").end(s);
		});

		vertx.createHttpServer().requestHandler(router::accept).listen(9096, ar ->
		{
			if (ar.succeeded())
			{
				System.out.println((char) 27 + "[37m" + "RestfulAPIVerticle (" + 9096 + "): Http server started");
			}
			else
			{
				ar.cause().printStackTrace();
			}
		});
	}

	private void handleGetProduct(RoutingContext routingContext)
	{
		String productID = routingContext.request().getParam("productID");
		HttpServerResponse response = routingContext.response();
		if (productID == null)
		{
			sendError(400, response);
		}
		else
		{
			JsonObject product = products.get(productID);
			if (product == null)
			{
				sendError(404, response);
			}
			else
			{
				response.putHeader("content-type", "application/json").end(product.encodePrettily());
			}
		}
	}

	private void handleAddProduct(RoutingContext routingContext)
	{
		String productID = routingContext.request().getParam("productID");
		HttpServerResponse response = routingContext.response();
		if (productID == null)
		{
			sendError(400, response);
		}
		else
		{
			JsonObject product = routingContext.getBodyAsJson();
			if (product == null)
			{
				sendError(400, response);
			}
			else
			{
				products.put(productID, product);
				response.end();
			}
		}
	}

	private void handleListProducts(RoutingContext routingContext)
	{
		JsonArray arr = new JsonArray();
		products.forEach((k, v) -> arr.add(v));
		routingContext.response().putHeader("content-type", "application/json").end(arr.encodePrettily());
	}

	private void sendError(int statusCode, HttpServerResponse response)
	{
		response.setStatusCode(statusCode).end();
	}

}
