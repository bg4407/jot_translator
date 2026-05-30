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
    File file = new File(filename);
    try(Scanner reader = new Scanner(file);){
      while(reader.hasNextLine()){
        String line = reader.nextLine();
        for(int i = 0; i < line.length(); i++){
          switch(line.charAt(i)){
            case '\n':
            case '\r':
            case '\t':
            case ' ':
              break;
            case '#':
              //basically breaks the for loop
              i = line.length();
              break;
            case ',':

              break;
            case ']':
              break;
            case '[':
              break;
            case '}':
              break;
            case '/':
              break;
            case '+':
              break;
            case '-':
              break;
            case '*':
              break;
            case ';':
              break;
            case '':
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