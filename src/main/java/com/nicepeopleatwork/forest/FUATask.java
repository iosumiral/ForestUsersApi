package com.nicepeopleatwork.forest;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simpleframework.http.Path;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

import com.google.gson.Gson;
import com.nicepeopleatwork.forest.conf.Configuration;

import weka.classifiers.meta.AdaBoostM1;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class FUATask implements Runnable {
	private static int numRequests;
	private final Response response;
	private final Request request;
	private String ip = null;
	private Path path = null;
	private String[] segments = null;
	private Query query = null;

	public static final Logger logger = LogManager.getLogger(FUAService.class.getName());

	public FUATask(Request request, Response response) throws Exception {
		this.response = response;
		this.request = request;
	}

	@Override
	public void run() {
		PrintStream body = null;
		logger.debug("Starting task");
		try {
			body = response.getPrintStream();
			long requestTime = System.currentTimeMillis();

			// Update Number of Requests
			FUATask.numRequests++;

			response.setValue("Content-Type", "text/plain");
			response.setValue("Server", "NiceHttpQualityService/1.0 (Nice264 1.0)");
			response.setDate("Date", requestTime);
			response.setDate("Last-Modified", requestTime);
			response.setValue("Access-Control-Allow-Origin", "*");
			// response.setValue("Connection" , "close" );

			path = request.getPath();
			segments = path.getSegments();

			String userId = request.getParameter("user_id");
			String accountCode = request.getParameter("account_code");

			Instance instance = FUAUser.getAnInstance(new String[] { userId, accountCode });

//			AdaBoostM1 forest = (AdaBoostM1) weka.core.SerializationHelper
//					.read(Configuration.FOREST_PATH);
			AdaBoostM1 forest = FUAService.forest;

//			int result = classifyInstance(instance, forest);

//			String json = new Gson().toJson(result);

//			body.println(json);
//
//			logger.debug(json);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (body != null)
				body.close();

			try {
				response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}