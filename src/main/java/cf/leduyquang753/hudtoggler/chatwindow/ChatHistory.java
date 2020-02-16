/* --------- BEGIN COPYRIGHT ---------

The MIT License

Copyright © 2019 Le Duy Quang

Permission is hereby granted, free of charge,
to any person obtaining a copy of this software
and associated documentation files (the "Software"),
to deal in the Software without restriction, including
without limitation the rights to use, copy, modify,
merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to
whom the Software is furnished to do so, subject
to the following conditions:

The above copyright notice and this permission notice
shall be included in all copies or substantial portions
of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT
WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT
SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
IN THE SOFTWARE.
------------ END COPYRIGHT -------- */

package cf.leduyquang753.hudtoggler.chatwindow;

import java.awt.Color;
import java.util.*;

import javax.swing.JTextPane;
import javax.swing.text.*;

public class ChatHistory extends JTextPane {
	
	/**
	 *
	 */
	private static final long serialVersionUID = 7538820282911016461L;

	private StyledDocument content;
	private static final String[] colorNames = new String[] {"black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple",
			"gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple",
			"yellow", "white" };
	private static final Color[] colors = new Color[] {
			Color.BLACK,
			new Color(0, 0, 170),
			new Color(0, 170, 0),
			new Color(0, 170, 170),
			new Color(170, 0, 0),
			new Color(170, 0, 170),
			new Color(255, 170, 0),
			new Color(170, 170, 170),
			new Color(85, 85, 85),
			new Color(85, 85, 255),
			new Color(85, 255, 85),
			new Color(85, 255, 255),
			new Color(255, 85, 85),
			new Color(255, 85, 255),
			new Color(255, 255, 85),
			Color.WHITE
	};
	
	private static final String[] styleNames = new String[] {"", "-bold", "-italic", "-underline", "-strikethrough" };
	
	private static final HashMap<Character, String> colorMapping = new HashMap<>();
	static {
		colorMapping.put('0', "black");
		colorMapping.put('1', "dark_blue");
		colorMapping.put('2', "dark_green");
		colorMapping.put('3', "dark_aqua");
		colorMapping.put('4', "dark_red");
		colorMapping.put('5', "drak_purple");
		colorMapping.put('6', "gold");
		colorMapping.put('7', "gray");
		colorMapping.put('8', "dark_gray");
		colorMapping.put('9', "glue");
		colorMapping.put('a', "green");
		colorMapping.put('b', "aqua");
		colorMapping.put('c', "red");
		colorMapping.put('d', "light_purple");
		colorMapping.put('e', "yellow");
		colorMapping.put('f', "white");
	}
	
	public ChatHistory(ChatWindow parent) {
		setBounds(5, 5, parent.getWidth() - 5, parent.getHeight() - 25);
		content = getStyledDocument();
		setBackground(new Color(150, 150, 150));
		addStyles();
	}
	
	public void updateSize(ChatWindow parent) {
		setBounds(5, 5, parent.getWidth()-26, parent.getHeight() - 86);
	}

	public void addChatMessage(String component) {
		String[] components = component.split("§");
		ArrayList<TextComponent> toWrite = new ArrayList<>();
		char color = 'f';
		boolean bold = false, italic = false, underline = false, strikethrough = false;
		boolean theFirstElement = true;
		for (String part : components) {
			if (part.equals("")) {
				theFirstElement = false;
				continue;
			}
			if (!theFirstElement) {
				char c = Character.toLowerCase(part.charAt(0));
				if (c >= '0' && c <= '9' || c >= 'a' && c <= 'f') {
					color = c;
				} else {
					switch (c) {
						case 'l':
							bold = true;
							break;
						case 'm':
							strikethrough = true;
							break;
						case 'n':
							underline = true;
							break;
						case 'o':
							italic = true;
							break;
						case 'r':
							color = 'f';
							bold = italic = underline = strikethrough = false;
							break;
						default:
							break;
					}
				}
			}
			if (!theFirstElement && part.length() > 1) {
				toWrite.add(new TextComponent(colorMapping.get(color) + (bold ? "-bold" : "") + (italic ? "-italic" : "") + (underline ? "-underline" : "") + (strikethrough ? "-strikethrough" : ""), part.substring(1)));
			} else if (theFirstElement) {
				toWrite.add(new TextComponent(part));
			}
			theFirstElement = false;
		}
		toWrite.add(new TextComponent("\n"));
		for (TextComponent part : toWrite) {
			try {
				content.insertString(content.getLength(), part.getContent(), content.getStyle(part.getFormat()));
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		try {
			scrollRectToVisible(modelToView(content.getLength()));
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isColorCode(char in) {
		return in >= '0' && in <= '9' || in >= 'a' && in <= 'f';
	}

	private void addStyles() {
		Style norm = content.addStyle("normal", StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE));
		Style s = null;
		for (int i = 0; i < 16; i++) {
			for (String style : styleNames) {
				for (String style2 : styleNames) {
					for (String style3 : styleNames) {
						for (String style4 : styleNames) {
							s = content.addStyle(colorNames[i] + style + style2 + style3 + style4, norm);
							StyleConstants.setForeground(s, colors[i]);
							applyStyle(s, style);
							applyStyle(s, style2);
							applyStyle(s, style3);
							applyStyle(s, style4);
						}
					}
				}
			}
		}
	}
	
	private void applyStyle(Style s, String style) {
		switch (style) {
			case "-bold":
				StyleConstants.setBold(s, true);
				return;
			case "-italic":
				StyleConstants.setItalic(s, true);
				return;
			case "-underline":
				StyleConstants.setUnderline(s, true);
				return;
			case "-strikethrough":
				StyleConstants.setStrikeThrough(s, true);
				return;
			default: return;
		}
	}
}
