/***
 * Sentence class : used for keeping the text exchanged between users
 * during a chat application
 * Contact: 
 *
 * Authors: 
 */

package irc;

import java.io.Serializable;

public class Sentence implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String 	data;
  
	public Sentence() {
		data = "";
	}
	
	public void write(String text) {
		data = text;
	}
	public String read() {
		return data;	
	}
	
}