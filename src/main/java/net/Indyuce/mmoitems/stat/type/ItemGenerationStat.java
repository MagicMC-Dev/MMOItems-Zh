package net.Indyuce.mmoitems.stat.type;

import net.Indyuce.mmoitems.api.itemgen.RandomStatData;

public interface ItemGenerationStat {

	/*
	 * any item stat which can be used in the item generator. this method reads
	 * from a config file stat data which is cached to later generate a random
	 * item
	 */
	public RandomStatData whenInitializedGeneration(Object object);
}
