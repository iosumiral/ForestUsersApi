package com.nicepeopleatwork.forest;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
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

			int result = classifyInstance(instance, forest);

			String json = new Gson().toJson(result);

			body.println(json);

			logger.debug(json);

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

	public static int classifyInstance(Instance instance, AdaBoostM1 forest) throws Exception {
		// add the attributes names to the database
		// TODO maybe add system to add as many attributes as the instance has
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		// class value
		List classes = new ArrayList(2);
		classes.add("0");
		classes.add("1");
		attributes.add(new Attribute("revisited", classes));
		// five averages
		attributes.add(new Attribute("avg_avg_bitrate"));
		attributes.add(new Attribute("avg_buffer_ratio"));
		attributes.add(new Attribute("avg_buffer_underruns"));
		attributes.add(new Attribute("avg_playtime"));
		attributes.add(new Attribute("avg_startup_time"));
		// five attributes specific for the row
		attributes.add(new Attribute("avg_bitrate"));
		attributes.add(new Attribute("buffer_ratio"));
		attributes.add(new Attribute("buffer_underruns"));
		attributes.add(new Attribute("playtime"));
		attributes.add(new Attribute("startup_time"));
		// five attributes depending on the row and the previous one
		attributes.add(new Attribute("better_avg_bitrate"));
		attributes.add(new Attribute("better_buffer_ratio"));
		attributes.add(new Attribute("better_buffer_underruns"));
		attributes.add(new Attribute("better_playtime"));
		attributes.add(new Attribute("better_startup_time"));
		// one more to store the amount of views in the same timeframe
		attributes.add(new Attribute("views_same_day"));
		Instances trainData = new Instances(Configuration.FOREST_PATH, attributes, 1);
		trainData.setClassIndex(0);
		instance.setDataset(trainData);
		return (int) Math.round((float) forest.classifyInstance(instance));
	}

}