package com.nicepeopleatwork.forest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.transport.connect.Connection;

public class FUAContainer implements Container {

	public static final Logger logger = LogManager.getLogger(FUAService.class.getName());

	public static Connection connection;
	private static int containerPort;
	// private final Executor executor;

	@Override
	public void handle(Request request, Response response) {
		logger.debug("Handling request");

		try {
			(new FUATask(request, response)).run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void closeWebContainer() {
		try {
			if (FUAContainer.connection != null) {
				// Close Connections
				FUAContainer.connection.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
