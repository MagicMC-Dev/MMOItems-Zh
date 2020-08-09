package net.Indyuce.mmoitems.stat.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.google.gson.JsonArray;

import net.Indyuce.mmoitems.api.item.template.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class StringListData implements StatData, RandomStatData, Mergeable {
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

	@Override
	public StatData randomize(MMOItemBuilder builder) {
		return new StringListData(new ArrayList<>(list));
	}

	@Override
	public void merge(StatData data) {
		Validate.isTrue(data instanceof StringListData, "Cannot merge two different stat data types");
		list.addAll(((StringListData) data).list);
	}
}
