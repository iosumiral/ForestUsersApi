package com.nicepeopleatwork.forest;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.nicepeopleatwork.druid.CreateDruidQuery;
import com.nicepeopleatwork.druid.exceptions.NiceDruidLibException;
import com.nicepeopleatwork.druid.ops.Filter;
import com.nicepeopleatwork.druid.ops.Order;
import com.nicepeopleatwork.druid.ops.Select;
import com.nicepeopleatwork.forest.conf.Configuration;

import weka.core.DenseInstance;
import weka.core.Instance;

public class FUAUser {

	public static void main(String[] args) {
		FUAUser u = new FUAUser();
		u.getAnInstance(new String[] { "12620849", "13" });
	}

	public static Instance getAnInstance(String[] args) {
		String userId = args[0];
		String accountCode = args[1];
		// start two months ago
		long initAt = 0L;
		// end right now
		long endAt = System.currentTimeMillis();

		try {

			////
			CreateDruidQuery create = new CreateDruidQuery(Configuration.DRUID_BROKER);
			create.select(Select.metrics("avg_bitrate", "buffer_underruns", "buffer_underrun_total", "viewtime",
					"startup_time", "outBytes"), Select.timestamp());
			// // here we create the artificial values used to store necessary
			// // quantities
			create.from("youbora_events_" + accountCode);
			create.initTime(initAt);
			create.endTime(endAt);
			create.limit(2);
			create.orderBy(Order.desc("timestamp"));
			create.filter(Filter.equals("user_id", userId));
			create.filter(Filter.equals("event_type", "STOP"));
			List<Map<String, Object>> results = create.send();
			for (int i = 0; i < results.size(); i++) {
				for (Entry<String, Object> mapa : results.get(i).entrySet()) {
					if ((mapa.getValue() instanceof Double) == false && (mapa.getValue() instanceof Long) == false)
						mapa.setValue(0.0);
					// substitute nulls by zeroes
					results.get(i).put(mapa.getKey(), mapa.getValue() == null ? 0.0 : mapa.getValue());
				}
				results.get(i).put("buffer_ratio",
						(double) results.get(i).get("buffer_underrun_total") / (double) results.get(i).get("viewtime"));
				results.get(i).put("viewtime", (double) results.get(i).get("viewtime") / 60000);
				results.get(i).put("startup_time", (double) results.get(i).get("startup_time") / 1000);
				results.get(i).put("avg_bitrate", (double) results.get(i).get("avg_bitrate") / 1000000);
				for (Entry<String, Object> mapa : results.get(i).entrySet()) {
					System.out.println(mapa.getKey() + " -> " + mapa.getValue());
				}
				// System.out.println("##########");
			}

			CreateDruidQuery create2 = new CreateDruidQuery("http://druid-brokers-nl.youbora.com/druid/v2/?pretty");
			create2.select(Select.avg("avg_bitrate", "avg_avg_bitrate"));
			create2.select(Select.avg("buffer_underruns", "avg_buffer_underruns"));
			create2.select(Select.avg("viewtime", "avg_viewtime"));
			create2.select(Select.avg("startup_time", "avg_startup_time"));
			create2.select(Select.formulaSum("sumOfBufferRatios", "buffer_underrun_total/viewtime",
					Filter.greaterThan("viewtime", 0)));
			create2.select(Select.division("sumOfBufferRatios", "count", "avg_buffer_ratio"));
			create2.groupBy();
			create2.filter(Filter.equals("user_id", userId));
			create2.from("youbora_events_" + accountCode);
			create2.initTime(initAt);
			create2.endTime(endAt);
			List<Map<String, Object>> results2 = create2.send();
			for (int i = 0; i < results2.size(); i++) {
				results2.get(i).put("avg_viewtime", (double) results2.get(i).get("avg_viewtime") / 60000);
				results2.get(i).put("avg_startup_time", (double) results2.get(i).get("avg_startup_time") / 1000);
				results2.get(i).put("avg_avg_bitrate", (double) results2.get(i).get("avg_avg_bitrate") / 1000000);
				for (Entry<String, Object> mapa : results2.get(i).entrySet()) {
					 System.out.println(mapa.getKey() + " -> " +
					 mapa.getValue());
				}
				 System.out.println("##########");
			}
			CreateDruidQuery create3 = new CreateDruidQuery(Configuration.DRUID_BROKER);
			create3.select(Select.count());
			create3.filter(Filter.equals("user_id", userId));
			create3.from("youbora_events_" + accountCode);
			long t = (long) results.get(0).get("timestamp");
//			long t = System.currentTimeMillis();
			create3.initTime(t - 1000 * 3600 * 24);
			create3.endTime(t + 1);
			// create3.filter(Filter.equals("event_type", "START"));
			// create3.granularity(Granularity.DAY, "Europe/Madrid");
			List<Map<String, Object>> results3 = create3.send();
				// if we don't find the reference "count" we create it
				// initialised as zero
			if(results3.size()==0){
				Map<String, Object> aux = new ConcurrentHashMap<String, Object>();
				aux.put("count", 0.0);
				results3.add(aux);
			}
			for (int i = 0; i < results3.size(); i++) {
				for (Entry<String, Object> mapa : results3.get(i).entrySet()) {
					System.out.println(mapa.getKey() + " -> " + mapa.getValue());
				}
				System.out.println("##########");
			}

			System.out.println(" iscomingback" + "   avgbitrate" + " avgbuffratio" + " avgunderruns" + "     avgptime"
					+ "   avgstartup" + "      bitrate" + "    buffratio" + "    underruns" + "     playtime"
					+ "  startuptime" + "    btbitrate" + "  btbuffratio" + "  btunderruns" + "   btplaytime"
					+ "    btstartup" + "  viewsameday");

			double[] parameters = {
					// a dummy
					1.0,
					// five attributes that depend on every view
					(double) results2.get(0).get("avg_avg_bitrate"), (double) results2.get(0).get("avg_buffer_ratio"),
					(double) results2.get(0).get("avg_buffer_underruns"), (double) results2.get(0).get("avg_viewtime"),
					(double) results2.get(0).get("avg_startup_time"),
					// five attributes that depend on the last view
					(double) results.get(0).get("avg_bitrate"), (double) results.get(0).get("buffer_ratio"),
					(double) results.get(0).get("buffer_underruns"), (double) results.get(0).get("viewtime"),
					(double) results.get(0).get("startup_time"),
					// five attributes that depend on the comparison between the
					// last view and its previous one
					((double) results.get(0).get("avg_bitrate") > (double) results.get(1).get("avg_bitrate")) ? 1.0
							: 0.0,
					((double) results.get(0).get("buffer_ratio") < (double) results.get(1).get("buffer_ratio")) ? 1.0
							: 0.0,
					((double) results.get(0).get("buffer_underruns") < (double) results.get(1).get("buffer_underruns"))
							? 1.0 : 0.0,
					((double) results.get(0).get("viewtime") > (double) results.get(1).get("viewtime")) ? 1.0 : 0.0,
					((double) results.get(0).get("startup_time") < (double) results.get(1).get("startup_time")) ? 1.0
							: 0.0,
					 (double) (long) results3.get(0).get("count") };
			// double[] test = {
			// 0.0,945993.9693877551,0.0,0.0,2595.823698979593,2483.5138520408163,0.0,0.0,0.0,71.0,1322.0,0.0,0.0,0.0,0.0,1.0,0.0
			// };
			for (double d : parameters) {
				// substitute NaNs with zeroes
				d = !Double.isNaN(d) ? d : 0;
				System.out.format("%13.4f", d);
			}
			System.out.println();
			// for (double d : test) {
			// System.out.format("%13.4f", d);
			// }
			// System.out.println();
			return new DenseInstance(1, parameters);
		} catch (NiceDruidLibException e) {
			e.printStackTrace();
		}
		return null;
	}
}