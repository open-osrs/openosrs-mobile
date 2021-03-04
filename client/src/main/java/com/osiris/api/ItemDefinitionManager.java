package com.osiris.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class ItemDefinitionManager {

	public static ItemDefinition[] itemDefinitions;

	@SuppressWarnings("resource")
	public static void init() {
		new Thread(() -> {
			Gson gson = new Gson();
			try {
				ItemDefinitionManager.itemDefinitions = gson.fromJson(new InputStreamReader(new URL("https://raw.githubusercontent.com/zeruth/OSJR/master/resources/ItemDef.json").openStream()),
						ItemDefinition[].class);
			} catch (JsonSyntaxException | JsonIOException | IOException e) {
				e.printStackTrace();
			}
		}).start();

	}

}