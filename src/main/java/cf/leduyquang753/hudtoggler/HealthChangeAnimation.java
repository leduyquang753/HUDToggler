package cf.leduyquang753.hudtoggler;

public class HealthChangeAnimation {
	public String healthChange = "ERROR";
	public int animationTime = 0;
	public int color = 0xFFFFFF;

	public HealthChangeAnimation(int healthChanged) {
		healthChange = (healthChanged > 0 ? "+" : "") + healthChanged;
		color = healthChanged > 0 ? 0x3AFF0E : 0xFF0E0E;
	}
}
