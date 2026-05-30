package provided;

/**
 * This class is responsible for tokenizing Jott code.
 * 
 * @author 
 **/

import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class JottTokenizer {

    /**
     * Takes in a filename and tokenizes that file into Tokens
     * based on the rules of the Jott Language
     * @param filename the name of the file to tokenize; can be relative or absolute path
     * @return an ArrayList of Jott Tokens
     */
  public static ArrayList<Token> tokenize(String filename){
    ArrayList<Token> mylist = new ArrayList<Token>();
    File file = new File(filename);
    try(Scanner reader = new Scanner(file);){
      int lineNum = 0;
      while(reader.hasNextLine()){
        lineNum++;
        String line = reader.nextLine().strip();
        for(int i = 0; i < line.length(); i++){
          switch(line.charAt(i)){
            case '\n':
            case '\r':
              lineNum++;
            case '\t':
            case ' ':
              break;
            case '#':
              //basically breaks the for loop
              i = line.length();
              break;
            case ',':
              mylist.add(new Token(",", filename, lineNum, TokenType.COMMA));
              break;
            case ']':
              mylist.add(new Token("]", filename, lineNum, TokenType.R_BRACKET));
              break;
            case '[':
              mylist.add(new Token("[", filename, lineNum, TokenType.L_BRACKET));
              break;
            case '}':
              mylist.add(new Token("}", filename, lineNum, TokenType.R_BRACE));
              break;
            case '{':
              mylist.add(new Token("{", filename, lineNum, TokenType.L_BRACE));
              break;
            case '/':
              mylist.add(new Token("/", filename, lineNum, TokenType.MATH_OP));
              break;
            case '+':
              mylist.add(new Token("+", filename, lineNum, TokenType.MATH_OP));
              break;
            case '-':
              mylist.add(new Token("-", filename, lineNum, TokenType.MATH_OP));
              break;
            case '*':
              mylist.add(new Token("*", filename, lineNum, TokenType.MATH_OP));
              break;
            case ';':
              mylist.add(new Token(";", filename, lineNum, TokenType.SEMICOLON));
              break;
            case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z':
              
              break;
            case '':
              break;
            
          }
        }
        

      }
    } catch(FileNotFoundException e){
      System.err.print(e);
      return null;
    }
    
  
		return null;
	}
}