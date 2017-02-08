import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by priyapns on 2/4/17.
 */
public class Dictionary {


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
