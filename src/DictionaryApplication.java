/**
 * Created by priyapns on 1/28/17.
 */
import java.lang.Boolean;
import java.lang.String;
import java.io.*;
import java.util.Scanner;


public class DictionaryApplication {
    public static void main(String args[]) throws FileNotFoundException {
        Boolean isWord = false, choice = true;
        String word ;

        while(!isWord || choice){
            Scanner in = new Scanner (System.in);
            System.out.println("Enter a word:");
            word = in.nextLine();
            if (word.isEmpty()){
                System.out.println("Word cannot be empty. \nPlease try again.");
            }else {
                 //System.out.println("You Entered: "+word);
                 isWord = validateWord(word);
                 if (isWord) {
                     System.out.println("You spelled the word correctly");
                     String meaning = getMeaning(word);
                     if(meaning.isEmpty()) System.out.println("Sorry meaning not found!!");
                     System.out.println(meaning);
                     System.out.println("Press ENTER if you want to continue");
                     Scanner in1 = new Scanner (System.in);
                     String ch = in1.nextLine();
                     if(ch.equalsIgnoreCase("")) choice = true;
                     //if(ch == "\n") choice = true;
                     else choice = false;
                 } else {
                     System.out.println(word + " is not a word!!\nPlease try again");
                 }
            }
        }

    }

    public static Boolean validateWord(String word) throws FileNotFoundException {
        Boolean isWord = false;
        FileReader fr = new FileReader("words.txt");
        BufferedReader br = new BufferedReader(fr);
        try {
            while (true) {
                String line = br.readLine();
                if (line == null)
                    break;
                if (line.equalsIgnoreCase(word))
                    isWord = true;
            }
            br.close();
        }catch (IOException e){
            System.out.println(e);
        }
        return isWord;
    }
    public static String getMeaning(String word)throws FileNotFoundException{
        //Boolean isWord = false;
        FileReader fr = new FileReader("dictionary.txt");
        BufferedReader br = new BufferedReader(fr);
        StringBuilder  stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");
        Boolean keepReading = false;
        try {
            while (true) {
                String line = br.readLine();
                if (line == null)
                    break;
                if (line.equals(word.toUpperCase()) || keepReading) {
                    //String[] lineParts = line.split("\\s+");
                    if(!line.trim().isEmpty()  && line.equals(line.toUpperCase()) && !line.equals(word.toUpperCase()) && !line.equals(word.toUpperCase()+"-") && !line.equals(word.toUpperCase()+".") && keepReading){
                        break;
                    }
                    stringBuilder.append(line);
                    stringBuilder.append(ls);
                    keepReading = true;
                }
            }
            br.close();
        }catch (IOException e){
            System.out.println(e);
        }
        return stringBuilder.toString();
    }
}