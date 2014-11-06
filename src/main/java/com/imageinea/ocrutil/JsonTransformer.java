package com.imageinea.ocrutil;

import java.util.LinkedHashMap;
import java.util.Map;

import spark.ResponseTransformer;

import com.google.gson.Gson;



	public class JsonTransformer implements ResponseTransformer {
		private Gson gson = new Gson();

		@Override
		public String render(Object model) {
			return gson.toJson(model);
		}

		public Map parse(String str) {
			Map data = gson.fromJson(str, LinkedHashMap.class);
			return data;
		}
	}

