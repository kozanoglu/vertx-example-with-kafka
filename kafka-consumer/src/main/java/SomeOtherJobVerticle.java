import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import io.vertx.core.AbstractVerticle;

public class SomeOtherJobVerticle extends AbstractVerticle
{
	@Override
	public void start() throws Exception
	{
		vertx.setPeriodic(2000, id ->
		{
			long threadId = Thread.currentThread().getId();

			try
			{
				File file = new File("Hello.txt");

				// creates the file
				if (!file.exists())
				{
					file.createNewFile();
				}

				try (FileWriter fw = new FileWriter("Hello.txt", true);
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter out = new PrintWriter(bw))
				{
					out.println(new Date() + " -- SomeOtherJobVerticle (" + threadId + "): Happily doing my job.");
					//more code
				}
				catch (IOException e)
				{
					//exception handling left as an exercise for the reader
				}

			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

		});
	}

}
