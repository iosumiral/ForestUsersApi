package com.nicepeopleatwork.forest.beans;

import weka.core.DenseInstance;
import weka.core.Instance;

public class UserBean
{
	private int user_id;
	private double buffer_underruns;
	private double buffer_underrun_total;
	private double playtime;
	private double avg_bitrate;
	private double startup_time;
	private double avg_playtime;
	private double avg_avg_bitrate;
	private double avg_startup_time;
	private double avg_buffer_ratio;
	private double avg_buffer_underruns;
	private double betterBufferUnderruns;
	private double betterBufferRatio;
	private double betterPlayTime;
	private double betterStartupTime;
	private double betterBitrate;
	private double visitsSameDay;
	
	public UserBean ( int user_id , double buffer_underruns , double buffer_underrun_total , double playtime ,
			double avg_bitrate , double startup_time , double avg_playtime , double avg_avg_bitrate ,
			double avg_startup_time , double avg_buffer_ratio , double avg_buffer_underruns ,
			double betterBufferUnderruns , double betterBufferRatio , double betterPlayTime , double betterStartupTime ,
			double betterBitrate , double visitsSameDay )
	{
		super ( );
		this.user_id = user_id;
		this.buffer_underruns = buffer_underruns;
		this.buffer_underrun_total = buffer_underrun_total;
		this.playtime = playtime;
		this.avg_bitrate = avg_bitrate;
		this.startup_time = startup_time;
		this.avg_playtime = avg_playtime;
		this.avg_avg_bitrate = avg_avg_bitrate;
		this.avg_startup_time = avg_startup_time;
		this.avg_buffer_ratio = avg_buffer_ratio;
		this.avg_buffer_underruns = avg_buffer_underruns;
		this.betterBufferUnderruns = betterBufferUnderruns;
		this.betterBufferRatio = betterBufferRatio;
		this.betterPlayTime = betterPlayTime;
		this.betterStartupTime = betterStartupTime;
		this.betterBitrate = betterBitrate;
		this.visitsSameDay = visitsSameDay;
	}

	public Instance getInstance ( )
	{
		double [ ] values = { 0.0 , buffer_underruns , buffer_underrun_total , playtime , avg_bitrate , startup_time ,
				avg_playtime , avg_avg_bitrate , avg_startup_time , avg_buffer_ratio , avg_buffer_underruns ,
				betterBufferUnderruns , betterBufferRatio , betterPlayTime , betterStartupTime , betterBitrate ,
				visitsSameDay };
		return new DenseInstance ( 1 , values );
	}
}
