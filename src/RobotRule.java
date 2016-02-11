import java.util.regex.Pattern;


class RobotRule {
	public Pattern pattern;
	public String originalValue;

	public RobotRule(Pattern pattern, String originalValue) {
		this.pattern = pattern;
		this.originalValue = originalValue;
	}
}