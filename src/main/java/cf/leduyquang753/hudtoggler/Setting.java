package cf.leduyquang753.hudtoggler;

import java.io.*;
import java.util.List;

public class Setting {
	private String name;
	private boolean enabled = true;
	private int id;
	private List<String> tooltips;
	
	public Setting(String name, boolean enabled, int id, List<String> tooltips) {
		setName(name);
		setEnabled(enabled);
		setId(id);
		setTooltips(tooltips);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public void readData(DataInputStream stream) throws IOException {
		setEnabled(stream.readBoolean());
	}
	
	public void writeData(DataOutputStream stream) throws IOException {
		stream.writeBoolean(enabled);
	}
	
	public List<String> getTooltips() {
		return tooltips;
	}
	
	public void setTooltips(List<String> tooltips) {
		this.tooltips = tooltips;
	}
}
