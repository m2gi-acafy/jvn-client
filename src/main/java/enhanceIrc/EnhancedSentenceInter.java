package enhanceIrc;

import java.io.Serializable;
import jvn.annotations.Action;

public interface EnhancedSentenceInter extends Serializable {

  @Action("write")
  void write(String text);

  @Action("read")
  String read();

  @Action("terminate")
  void terminate();

}
