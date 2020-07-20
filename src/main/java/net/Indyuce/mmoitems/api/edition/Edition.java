package net.Indyuce.mmoitems.api.edition;

public interface Edition {

	/*
	 * processes the player input; returns true if edition should be closed or
	 * false if it should continue
	 */
	public boolean output(String input);

	public void enable(String... message);

	/*
	 * true if after successful edition, the GUI should go back to the
	 * previously opened GUI or if it should just be ignored
	 */
	public boolean shouldGoBack();
}
