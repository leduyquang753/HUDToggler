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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

import org.lwjgl.opengl.Display;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

public class ChatWindow extends JFrame {
	/**
	 *
	 */
	private static final long serialVersionUID = -6479715937123172385L;
	private ChatHistory history;
	private JButton button;
	private JTextArea chatBox;
	
	public ChatWindow() {
		super();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Font UiFont = new Font("Segoe UI", 0, 12);
		setTitle(Display.getTitle() + " - Chat");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		addComponentListener(new ChatWindowListener(this));
		
		history = new ChatHistory(this);
		history.setFont(UiFont);
		history.setVisible(true);
		history.updateSize(this);
		history.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(history);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		add(scrollPane);
		add(history);
		button = new JButton("Send");
		button.setBounds(getWidth() - 131, getHeight() - 75, 100, 25);
		button.setFont(UiFont);
		button.setVisible(true);
		button.addActionListener(new ButtonListener(this));
		add(button);
		chatBox = new JTextArea();
		chatBox.setFont(UiFont);
		chatBox.setLineWrap(false);
		chatBox.addKeyListener(new ChatBoxListener(this, chatBox));
		chatBox.setVisible(true);
		chatBox.setBounds(5, getHeight() - 60, getWidth() - 86, 25);
		add(chatBox);
		setAutoRequestFocus(false);
	}
	
	private class ChatWindowListener implements ComponentListener {
		private ChatWindow parent;
		
		public ChatWindowListener(ChatWindow window) {
			parent = window;
		}

		@Override
		public void componentResized(ComponentEvent e) {
			history.updateSize(parent);
			history.setEditable(false);
			parent.button.setBounds(getWidth() - 131, getHeight() - 75, 100, 25);
			parent.chatBox.setBounds(5, getHeight() - 60, getWidth() - 86, 25);
		}

		@Override
		public void componentMoved(ComponentEvent e) {
		}

		@Override
		public void componentShown(ComponentEvent e) {
		}

		@Override
		public void componentHidden(ComponentEvent e) {
		}
		
	}

	public void addChatMessage(String component) {
		history.addChatMessage(component);
	}

	private class ChatBoxListener implements KeyListener {
		private String historyBuffer = "";
		private int sentHistoryCursor = -1;
		Minecraft mc = Minecraft.getMinecraft();
		
		private JTextArea parent;

		public ChatBoxListener(ChatWindow master, JTextArea parent) {
			super();
			this.parent = parent;
		}

		@Override
		public void keyTyped(KeyEvent e) {
			handleKey(e.getKeyCode(), e.getKeyChar());
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
		}
		
		@Override
		public void keyReleased(KeyEvent e) {
		}

		private void handleKey(int keyCode, char character) {
			if (keyCode != 28 && keyCode != 156)
			{
				if (keyCode == 200)
				{
					getSentHistory(-1);
				}
				else if (keyCode == 208)
				{
					getSentHistory(1);
				}
			}
			else
			{
				String s = parent.getText();
				
				if (s.length() > 0)
				{
					sendChatMessage(s);
				}
				
				parent.setCaretPosition(0);
				parent.setText("");
			}

			if (character == '\n') {
				String s = parent.getText();
				
				if (s.length() > 0)
				{
					sendChatMessage(s);
				}
				
				parent.setCaretPosition(0);
				parent.setText("");
			}
		}
		
		public void getSentHistory(int msgPos)
		{
			int i = sentHistoryCursor + msgPos;
			int j = mc.ingameGUI.getChatGUI().getSentMessages().size();
			i = MathHelper.clamp_int(i, 0, j);
			
			if (i != sentHistoryCursor)
			{
				if (i == j)
				{
					sentHistoryCursor = j;
					parent.setText(historyBuffer);
				}
				else
				{
					if (sentHistoryCursor == j)
					{
						historyBuffer = parent.getText();
					}
					
					parent.setText(mc.ingameGUI.getChatGUI().getSentMessages().get(i));
					sentHistoryCursor = i;
				}
			}
		}

		public void sendChatMessage(String msg)
		{
			mc.ingameGUI.getChatGUI().addToSentMessages(msg);
			if (net.minecraftforge.client.ClientCommandHandler.instance.executeCommand(mc.thePlayer, msg) != 0) return;
			mc.thePlayer.sendChatMessage(msg);
		}
	}
	
	private class ButtonListener implements ActionListener {
		Minecraft mc = Minecraft.getMinecraft();
		ChatWindow parent;
		
		public ButtonListener(ChatWindow parent) {
			this.parent = parent;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			mc.ingameGUI.getChatGUI().addToSentMessages(parent.chatBox.getText());
			if (net.minecraftforge.client.ClientCommandHandler.instance.executeCommand(mc.thePlayer, parent.chatBox.getText()) != 0) return;
			mc.thePlayer.sendChatMessage(parent.chatBox.getText());
			parent.chatBox.setText("");
		}
		
	}
}
