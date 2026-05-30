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
    /**
     * Takes in a filename and tokenizes that file into Tokens
     * based on the rules of the Jott Language
     * @param filename the name of the file to tokenize; can be relative or absolute path
     * @return an ArrayList of Jott Tokens
     */
    public static ArrayList<Token> tokenize(String filename) {
        ArrayList<Token> mylist = new ArrayList<Token>();
        File file = new File(filename);
        int lineNum = 0;

        try (Scanner reader = new Scanner(file);) {
            while (reader.hasNextLine()) {
                lineNum++;
                String line = reader.nextLine().strip(); 

                for (int i = 0; i < line.length(); i++) {
                    char currentChar = line.charAt(i);

                    switch (currentChar) {
                        case '\t':
                        case ' ':
                        case '\r':
                            break;
                        case '#':
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
                        case ';':
                            mylist.add(new Token(";", filename, lineNum, TokenType.SEMICOLON));
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

                        // Header Lookahead (: vs ::)
                        case ':':
                            if (i + 1 < line.length() && line.charAt(i + 1) == ':') {
                                mylist.add(new Token("::", filename, lineNum, TokenType.FC_HEADER));
                                i++; // Skip lookahead char
                            } else {
                                mylist.add(new Token(":", filename, lineNum, TokenType.COLON));
                            }
                            break;
                        case '=':
                            if (i + 1 < line.length() && line.charAt(i + 1) == '=') {
                                mylist.add(new Token("==", filename, lineNum, TokenType.REL_OP));
                                i++;
                            } else {
                                mylist.add(new Token("=", filename, lineNum, TokenType.ASSIGN));
                            }
                            break;
                        case '<':
                            if (i + 1 < line.length()) {
                                char nextChar = line.charAt(i + 1);
                                if (nextChar == '=') {
                                    mylist.add(new Token("<=", filename, lineNum, TokenType.REL_OP));
                                    i++;
                                } else if (nextChar == '>') {
                                    mylist.add(new Token("<>", filename, lineNum, TokenType.REL_OP));
                                    i++;
                                } else {
                                    mylist.add(new Token("<", filename, lineNum, TokenType.REL_OP));
                                }
                            } else {
                                mylist.add(new Token("<", filename, lineNum, TokenType.REL_OP));
                            }
                            break;

                        case '>':
                            if (i + 1 < line.length() && line.charAt(i + 1) == '=') {
                                mylist.add(new Token(">=", filename, lineNum, TokenType.REL_OP));
                                i++;
                            } else {
                                mylist.add(new Token(">", filename, lineNum, TokenType.REL_OP));
                            }
                            break;

                        case '!':
                            if (i + 1 < line.length() && line.charAt(i + 1) == '=') {
                                mylist.add(new Token("!=", filename, lineNum, TokenType.REL_OP));
                                i++;
                            } else {
                                System.err.println("Syntax Error:");
                                System.err.println("Invalid token \"!\". \"!\" expects following \"=\"");
                                System.err.println(filename + ":" + lineNum);
                                return null;
                            }
                            break;

                        // String literals scanning logic
                        case '"':
                            // TODO: implement string literal scanning (stubbed intentionally)
                            break;

            default:
              //parsing id/keywords
              if (Character.isAlphabetic(currentChar)){
                String keywordID = "";

                while (i < line.length() && Character.isDigit(line.charAt(i)) || Character.isAlphabetic(line.charAt(i))) {
                  keywordID += line.charAt(i);
                  i++;
                }
              }

              //parsing numbers
              if (Character.isDigit(currentChar)) {
                String num = "";

                while (i < line.length() && Character.isDigit(line.charAt(i))) {
                  num += line.charAt(i);
                  i++;
                }

                if (i < line.length() && line.charAt(i) == '.') {
                  num += line.charAt(i);
                  i++;

                  if (i >= line.length() || !Character.isDigit(line.charAt(i))) {
                    System.err.println("Syntax Error:");
                    System.err.println("Invalid number \"" + num + "\"");
                    System.err.println(filename + ":" + lineNum);
                    return null;
                  }

                  while (i < line.length() && Character.isDigit(line.charAt(i))) {
                    num += line.charAt(i);
                    i++;
                  }
                }

                i--;
                mylist.add(new Token(num, filename, lineNum, TokenType.NUMBER));
              }
              break;
          }
        }
      }
    } catch (FileNotFoundException e) {
      System.err.print(e);
      return null;
    }

    return mylist; // Returns your parsed collection safely!
  }
}