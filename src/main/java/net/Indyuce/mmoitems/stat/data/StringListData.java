package net.Indyuce.mmoitems.stat.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonArray;

public class StringListData extends StatData {
	private final List<String> list;

	public StringListData() {
		this(new ArrayList<>());
	}

	public StringListData(String[] array) {
		this(Arrays.asList(array));
	}

	public StringListData(JsonArray array) {
		this();

		array.forEach(str -> list.add(str.getAsString()));
	}

	public StringListData(List<String> list) {
		this.list = list;
	}

	public List<String> getList() {
		return list;
	}
}
