import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerRecord;

import com.sun.management.OperatingSystemMXBean;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.producer.KafkaWriteStream;
import io.vertx.kafka.client.serialization.JsonObjectSerializer;

public class KafkaProducerVerticle extends AbstractVerticle
{
	private OperatingSystemMXBean systemMBean;

	private KafkaWriteStream<String, JsonObject> producer;

	public static void main(String[] args) throws IOException
	{
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(KafkaProducerVerticle.class.getName(), new DeploymentOptions().setInstances(1));
	}

	@Override
	public void start() throws Exception
	{
		systemMBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

		// A random identifier
		String pid = UUID.randomUUID().toString();

		Properties config = new Properties();
		config.put("bootstrap.servers", "localhost:9092");
		config.put("key.serializer", JsonObjectSerializer.class);
		config.put("value.serializer", JsonObjectSerializer.class);
		config.put("acks", "all");
		config.put("buffer.memory", "33554432");
		config.put("retries", "1");
		config.put("batch.size", "16384");
		//	config.put("client.id", "foo");
		config.put("linger.ms", "50");
		config.put("timeout.ms", "50");
		config.put("request.timeout.ms", "50");

		// use producer for interacting with Apache Kafka
		producer = KafkaWriteStream.create(vertx, config, String.class, JsonObject.class);

		producer.exceptionHandler(e ->
		{
			System.out.println("Error = " + e.getMessage());
		});

		// Publish the metrics in Kafka
		vertx.setPeriodic(2000, id ->
		{
			JsonObject metrics = new JsonObject();
			metrics.put("CPU", round(systemMBean.getSystemCpuLoad() * 100));
			metrics.put("Mem", round((systemMBean.getTotalPhysicalMemorySize() - systemMBean.getFreePhysicalMemorySize()) / 1000000000d));
			ProducerRecord<String, JsonObject> record = new ProducerRecord<>("asddasadas", new JsonObject().put(pid, metrics));

			producer.write(record, done ->
			{
				if (done.succeeded())
				{
					org.apache.kafka.clients.producer.RecordMetadata result = done.result();
					System.out.println(
						"Message " + record.value() + " written on topic=" + result.topic() + ", partition=" + result.partition() + ", offset="
							+ result.offset());
				}
			});

		});
	}

	@Override
	public void stop() throws Exception
	{
		if (producer != null)
		{
			producer.close();
		}
	}

	private double round(double number)
	{
		return Math.round(number * 100d) / 100d;
	}
}