
public enum ConfigFileValues {
	PORT, ROOT, DEFAULTPAGE, MAXTHREADS;

	/**
	 * Match the tag from the config file to one of the possible tags
	 * 
	 * @throws exception
	 *             if there is no match
	 * 
	 * @param configTag
	 * @return matching tag
	 */
	static public ConfigFileValues matchTag(String configTag) {
		ConfigFileValues[] tags = ConfigFileValues.values();
		for (ConfigFileValues tag : tags)
			if (tag.toString().equals(configTag.toUpperCase())) {
				return tag;
			}
		throw new IllegalArgumentException("Given tag is illegal - "
				+ configTag);
	}

}
