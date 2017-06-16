import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;

import static java.lang.System.out;

public class BlockingOperationVerticle extends AbstractVerticle
{
	@Override
	public void start() throws Exception
	{
		Integer port = config().getInteger("http.port", 8088);

		HttpServer server = vertx.createHttpServer();

		Router router = Router.router(vertx);

		router.route().handler(CorsHandler.create("*").allowedMethod(HttpMethod.GET).allowedMethod(HttpMethod.POST).allowedMethod(HttpMethod.OPTIONS)
			.allowedHeader("Access-Control-Allow-Origin").allowedHeader("Content-Type"));

		router.route("/").handler(routingContext ->
		{
			HttpServerResponse response = routingContext.response();
			response.putHeader("content-type", "text/plain");
			response.end("Hello World!");
		});

		router.route("/fibonacci/:target/").method(HttpMethod.GET).handler(routingContext ->
		{
			HttpServerResponse response = routingContext.response();
			Integer target;
			try
			{
				target = Integer.valueOf(routingContext.request().getParam("target"));

				if (target > 60)
				{
					response.end(target + " is too big for me to handle");
					return;
				}

				out.println((char) 27 + "[34m" + "BlockingOperationVerticle (" + port + "): Calculating fib(" + target + ")");

				// Call some blocking API that takes a significant amount of time to return
				Integer result = fibonacci(target);

				JsonObject respBody = new JsonObject();
				respBody.put("target", target);
				respBody.put("result", result);

				response.end(Json.encodePrettily(respBody));

				out.println((char) 27 + "[34m" + "BlockingOperationVerticle (" + port + ") The calculation for fib(" + target + ") is: " + result);
			}
			catch (NumberFormatException e)
			{
				response.end("Not a number");
			}

		});

		server.requestHandler(router::accept).listen(port, ar ->
		{
			if (ar.succeeded())
			{
				out.println((char) 27 + "[34m" + "BlockingOperationVerticle (" + port + "): Http server started");
			}
			else
			{
				ar.cause().printStackTrace();
			}
		});

	}

	private Integer fibonacci(Integer n)
	{
		if (n <= 1)
		{
			return n;
		}
		else
		{
			return fibonacci(n - 1) + fibonacci(n - 2);
		}
	}
}
