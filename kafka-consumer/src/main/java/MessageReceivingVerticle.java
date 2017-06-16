import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

import static java.lang.System.out;

public class MessageReceivingVerticle extends AbstractVerticle
{
	@Override
	public void start() throws Exception
	{
		EventBus eb = vertx.eventBus();

		eb.consumer("KafkaConsumerVerticle.message", message -> out
			.println((char) 27 + "[32m" + "MessageReceivingVerticle: Yay! I've just received a message: " + (char) 27 + "[31m" + message.body()));
	}
}
