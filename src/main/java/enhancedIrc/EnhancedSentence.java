/***
 * Sentence class : used for keeping the text exchanged between users
 * during a chat application
 * Contact: 
 *
 * Authors: 
 */

package enhancedIrc;

public class EnhancedSentence implements EnhancedSentenceInter {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  String data;

  public EnhancedSentence() {
    data = "";
  }


  public void write(String text) {
    data = text;
  }

  public String read() {
    return data;
  }

  @Override
  public void terminate() {
    System.out.println("terminate");
  }

}