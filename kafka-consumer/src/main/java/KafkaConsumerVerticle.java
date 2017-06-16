import java.util.Collections;
import java.util.Properties;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.serialization.JsonObjectDeserializer;

import static java.lang.System.out;

public class KafkaConsumerVerticle extends AbstractVerticle
{

	@Override
	public void start() throws Exception
	{
		Integer port = 8099;

		Router router = Router.router(vertx);

		// The event bus bridge handler
		BridgeOptions options = new BridgeOptions();
		options.setOutboundPermitted(Collections.singletonList(new PermittedOptions().setAddress("dashboard")));
		router.route("/eventbus/*").handler(SockJSHandler.create(vertx).bridge(options));

		// The web server handler
		router.route().handler(StaticHandler.create().setCachingEnabled(false));

		// Start http server
		HttpServer httpServer = vertx.createHttpServer();
		httpServer.requestHandler(router::accept).listen(port, ar ->
		{
			if (ar.succeeded())
			{
				out.println((char) 27 + "[31m" + "KafkaConsumerVerticle: Http server started on port: " + port);
			}
			else
			{
				ar.cause().printStackTrace();
			}
		});

		// Our dashboard that aggregates metrics from various kafka topics
		JsonObject dashboard = new JsonObject();

		// Publish the dashboard to the browser over the bus
		vertx.setPeriodic(1000, timerID -> vertx.eventBus().publish("dashboard", dashboard));

		Properties config = new Properties();
		config.put("bootstrap.servers", "localhost:9092");
		config.put("key.deserializer", JsonObjectDeserializer.class);
		config.put("value.deserializer", JsonObjectDeserializer.class);
		config.put("group.id", "my_group");
		config.put("auto.offset.reset", "earliest");
		config.put("enable.auto.commit", "true");

		// use consumer for interacting with Apache Kafka
		KafkaConsumer<String, JsonObject> consumer = KafkaConsumer.create(vertx, config);

		consumer.exceptionHandler(e -> out.println("Error = " + e.getMessage()));

		// Aggregates metrics in the dashboard
		consumer.handler(record ->
		{
			try
			{
				JsonObject obj = new JsonObject(record.value().toString());
				dashboard.mergeIn(obj);

				//				out.println(
				//					(char) 27 + "[31m" + "KafkaConsumerVerticle (" + threadId + "): Yay! I just received this message: " + record.value().toString());
				vertx.eventBus()
					.publish("KafkaConsumerVerticle.message", "KafkaConsumerVerticle: My message to everyone: " + record.value().toString());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		});

		// Subscribe to Kafka
		consumer.subscribe(Collections.singleton("asddasadas"));
	}
}
