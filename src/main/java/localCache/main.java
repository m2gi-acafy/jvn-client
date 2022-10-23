package localCache;

import enhanceIrc.EnhancedSentence;
import enhanceIrc.EnhancedSentenceInter;
import jvn.proxy.JvnProxyFactory;

public class main {

  public static void main(String[] args) throws Exception {
    // TODO Auto-generated method stub
    for (int i = 0; i < 10; i++) {
      EnhancedSentenceInter sentence = (EnhancedSentenceInter) JvnProxyFactory
          .newInstance(new EnhancedSentence(), String.valueOf(i));
      if (i == 9) {
        sentence.terminate();
      }
    }
  }

}
