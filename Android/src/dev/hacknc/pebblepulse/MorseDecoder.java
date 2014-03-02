package dev.hacknc.pebblepulse;

public class MorseDecoder {

	public static String decode(String msg) {
		return getLetter(msg);
	}

	private static String getLetter(String msg) {
		if (msg.equals(".-")) {
			return "A";
		} else if (msg.equals("-...")) {
			return "B";
		} else if (msg.equals("-.-.")) {
			return "C";
		} else if (msg.equals("-..")) {
			return "D";
		} else if (msg.equals(".")) {
			return "E";
		} else if (msg.equals("..-.")) {
			return "F";
		} else if (msg.equals("--.")) {
			return "G";
		} else if (msg.equals("....")) {
			return "H";
		} else if (msg.equals("..")) {
			return "I";
		} else if (msg.equals(".---")) {
			return "J";
		} else if (msg.equals("-.-")) {
			return "K";
		} else if (msg.equals(".-..")) {
			return "L";
		} else if (msg.equals("--")) {
			return "M";
		} else if (msg.equals("-.")) {
			return "N";
		} else if (msg.equals("---")) {
			return "O";
		} else if (msg.equals(".--.")) {
			return "P";
		} else if (msg.equals("--.-")) {
			return "Q";
		} else if (msg.equals(".-.")) {
			return "R";
		} else if (msg.equals("...")) {
			return "S";
		} else if (msg.equals("-")) {
			return "T";
		} else if (msg.equals("..-")) {
			return "U";
		} else if (msg.equals("...-")) {
			return "V";
		} else if (msg.equals(".--")) {
			return "W";
		} else if (msg.equals("-..-")) {
			return "X";
		} else if (msg.equals("-.--")) {
			return "Y";
		} else if (msg.equals("--..")) {
			return "Z";
		} else if (msg.equals(".----")) {
			return "1";
		} else if (msg.equals("..---")) {
			return "2";
		} else if (msg.equals("...--")) {
			return "3";
		} else if (msg.equals("....-")) {
			return "4";
		} else if (msg.equals(".....")) {
			return "5";
		} else if (msg.equals("-....")) {
			return "6";
		} else if (msg.equals("--...")) {
			return "7";
		} else if (msg.equals("---..")) {
			return "8";
		} else if (msg.equals("----.")) {
			return "9";
		} else if (msg.equals("-----")) {
			return "0";
		} else {
			return "!";
		}
	}
}
