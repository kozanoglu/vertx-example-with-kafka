import java.io.IOException;

import com.hazelcast.config.Config;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class Main
{

	public static void main(String[] args) throws IOException
	{
		Config hazelcastConfig = new Config();
		ClusterManager mgr = new HazelcastClusterManager(hazelcastConfig);

		Vertx.clusteredVertx(new VertxOptions().setClusterManager(mgr).setClustered(true).setWorkerPoolSize(40), vertxAsyncResult ->
		{
			Vertx vertx = vertxAsyncResult.result();
			vertx.deployVerticle(RestfulAPIVerticle.class.getName(), new DeploymentOptions().setInstances(1));

			vertx.deployVerticle(SomeOtherJobVerticle.class.getName(), new DeploymentOptions().setInstances(2).setWorker(true));

			vertx.deployVerticle(BlockingOperationVerticle.class.getName(),
				new DeploymentOptions().setWorker(true).setConfig(new JsonObject().put("http.port", 8088)).setInstances(1));
			vertx.deployVerticle(BlockingOperationVerticle.class.getName(),
				new DeploymentOptions().setWorker(false).setConfig(new JsonObject().put("http.port", 8089)).setInstances(1));

			vertx.deployVerticle(KafkaConsumerVerticle.class.getName(), new DeploymentOptions().setInstances(1));

			vertx.deployVerticle(MessageReceivingVerticle.class.getName(), new DeploymentOptions().setInstances(1));
		});
	}

	public static void nonClusteredVersion(String[] args) throws IOException
	{
		Vertx vertx = Vertx.vertx();

		vertx.deployVerticle(RestfulAPIVerticle.class.getName(), new DeploymentOptions().setInstances(1));

		vertx.deployVerticle(SomeOtherJobVerticle.class.getName(), new DeploymentOptions().setInstances(2).setWorker(true));

		vertx.deployVerticle(BlockingOperationVerticle.class.getName(),
			new DeploymentOptions().setWorker(true).setConfig(new JsonObject().put("http.port", 8088)).setInstances(1));
		vertx.deployVerticle(BlockingOperationVerticle.class.getName(),
			new DeploymentOptions().setWorker(false).setConfig(new JsonObject().put("http.port", 8089)).setInstances(1));

		vertx.deployVerticle(KafkaConsumerVerticle.class.getName(), new DeploymentOptions().setInstances(1));

		vertx.deployVerticle(MessageReceivingVerticle.class.getName(), new DeploymentOptions().setInstances(1));
	}

}
