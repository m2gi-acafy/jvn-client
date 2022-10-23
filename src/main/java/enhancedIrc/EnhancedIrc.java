/***
 * Irc class : simple implementation of a chat using JAVANAISE 
 * Contact: 
 *
 * Authors: 
 */

package enhancedIrc;

import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jvn.JvnException;
import jvn.proxy.JvnProxyFactory;

public class EnhancedIrc {

  public TextArea text;
  public TextField data;
  Frame frame;
  EnhancedSentenceInter sentence;

  /**
   * main method create a JVN object nammed IRC for representing the Chat application
   **/
  public static void main(String argv[]) {
    try {

      EnhancedSentenceInter s = (EnhancedSentenceInter) JvnProxyFactory.newInstance(
          new EnhancedSentence(),
          "IRC");
      new EnhancedIrc(s);
    } catch (JvnException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * IRC Constructor
   *
   * @param jo the JVN object representing the Chat
   **/
  public EnhancedIrc(EnhancedSentenceInter jo) {
    sentence = jo;
    frame = new Frame();
    frame.setLayout(new GridLayout(1, 1));
    text = new TextArea(10, 60);
    text.setEditable(false);
    text.setForeground(Color.red);
    frame.add(text);
    data = new TextField(40);
    frame.add(data);
    Button read_button = new Button("read");
    read_button.addActionListener(new readListener(this));
    frame.add(read_button);
    Button write_button = new Button("write");
    write_button.addActionListener(new writeListener(this));
    frame.add(write_button);
    frame.setSize(545, 201);
    text.setBackground(Color.black);
    frame.setVisible(true);
  }
}

class readListener implements ActionListener {

  EnhancedIrc irc;

  public readListener(EnhancedIrc i) {
    irc = i;
  }

  /**
   * Management of user events
   **/
  public void actionPerformed(ActionEvent e) {
    String s = irc.sentence.read();
    irc.data.setText(s);
    irc.text.append(s + "\n");
  }
}

class writeListener implements ActionListener {

  EnhancedIrc irc;

  public writeListener(EnhancedIrc i) {
    irc = i;
  }

  /**
   * Management of user events
   **/
  public void actionPerformed(ActionEvent e) {
    String s = irc.data.getText();
    irc.sentence.write(s);
  }
}
