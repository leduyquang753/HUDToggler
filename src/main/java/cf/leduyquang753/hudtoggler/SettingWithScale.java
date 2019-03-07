package cf.leduyquang753.hudtoggler;

import java.util.List;

public class SettingWithScale extends Setting {
	/**
	 * The scale of the element.<br>
	 * 0: x0,5<br>
	 * 1: x1<br>
	 * 2: x2<br>
	 */
	private int scale = -1;
	
	public SettingWithScale(String name, boolean enabled, int id, int scale, List<String> tooltips) {
		super(name, enabled, id, tooltips);
		setScale(scale);
	}

	public SettingWithScale(String name, boolean enabled, int id, List<String> tooltips) {
		super(name, enabled, id, tooltips);
	}
	
	public int getScale() {
		return scale;
	}
	
	public void setScale(int scale) {
		this.scale = Math.max(-1, Math.min(2, scale));
	}
}
