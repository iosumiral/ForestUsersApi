package com.nicepeopleatwork.forest;
import java.net.InetSocketAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import com.nicepeopleatwork.forest.conf.Configuration;

import weka.classifiers.meta.AdaBoostM1;

public class FUAService {

	public static final Logger logger = LogManager.getLogger(FUAService.class.getName());

	private static Connection connection;
	private static int containerPort;
	public static AdaBoostM1 forest;

	public static void main(String[] list) throws Exception {

		// start classifier
		FUABuilder fb = new FUABuilder();
		fb.run();
		forest = fb.getAdaboost();
		
		// Define Port
		FUAService.containerPort = Configuration.DEFAULT_PORT;

		// Start container
		FUAService.startWebContainer();
	}

	public static void startWebContainer() {
		try {
			// HTTP Client
			SocketConnection connection = new SocketConnection(new ContainerServer(new FUAContainer(),Configuration.CONTAINER_THREAD_POOL));
			connection.connect(new InetSocketAddress(Configuration.DEFAULT_PORT));
			logger.debug("Server started");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void closeWebContainer() {
		try {
			if (FUAService.connection != null) {
				// Close Connections
				FUAService.connection.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
