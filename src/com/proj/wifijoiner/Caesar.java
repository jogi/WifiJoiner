package com.proj.wifijoiner;

/**
 * @author Jogi
 * Modified Caesar Encryption service
 */
public class Caesar {
	private final static int key = 23; // (en|de)cryption key	
	private final String s = "abklm78efghvi{VZU[Jn}c12@#]$3?4rs|tu56:90;wxG.HO,yRW<XYd>jopqz~!%IDBC^&EFQL*()_+=-`ASTMNPK";
	
	// (en|de)crypt: just feed in opposite parameters
	public String encrypt(String text) {
		return translate(key, text);
	}

	public String decrypt(String text) {
		return translate(-key, text);
	}

	// translate: input message, translate
	private String translate(int k, String text) {
		StringBuilder str = new StringBuilder();
		char c;
		int i = 0;
		while ((byte) (c = getNextChar(i++, text)) != -1) {
			if(s.indexOf(c)!=-1)
				c = rotate(c, k);			
			str.append(c);
		}
		return str.toString();
	}

	// getNextChar: fetches next char.
	public char getNextChar(int i, String text) {
		char ch = ' '; // = ' ' to keep compiler happy
		try {
			ch = (char) text.charAt(i);
		} catch(IndexOutOfBoundsException e) {
			return (char) -1;
		}

		return ch;
	}

	// rotate: translate using rotation, version with table lookup
	public char rotate(char c, int key) {
			
		
		int i = 0;
		while (i < this.s.length()) {
			// extra +26 below because key might be negative
				if (c == this.s.charAt(i))
					return this.s.charAt((i + key + this.s.length()) % this.s.length());
						
			i++;
		}
		return c;
	}
	
	// main: check command, (en|de)crypt, feed in key value
	public static void main(String[] args) {

		Caesar cipher = new Caesar();
		System.out.println(cipher.encrypt("strin'gdas;$%$%$^# to ENCR'YPT"));
		
		System.out.println(cipher.decrypt(cipher.encrypt("str{[in'gdas;$%]}|,.<>$%$^# to ENCR'YPT")));

	}
}