package com.imaginea.api;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class Compute {

	public static double StandardDeviation(List<Double> list) {
		return  statisticAdd(list).getStandardDeviation();
	}

	public static double mean(List<Double> list) {
		return  statisticAdd(list).getMean();
	}

	public static double variance(List<Double> list) {
		return  statisticAdd(list).getVariance();
	}
	private static DescriptiveStatistics statisticAdd(List<Double> list){
		DescriptiveStatistics stat = new DescriptiveStatistics();
		while (list.isEmpty()) {
			int i = 0;
			stat.addValue(list.get(i));
			i++;
		}
		return stat;
		}
}
