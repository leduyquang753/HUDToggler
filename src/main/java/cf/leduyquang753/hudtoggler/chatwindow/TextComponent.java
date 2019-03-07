package cf.leduyquang753.hudtoggler.chatwindow;

public class TextComponent {
	private String format;
	private String content;

	public TextComponent(String format, String content) {
		this.format = format;
		this.content = content;
	}

	public TextComponent(String content) {
		this("white", content);
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
