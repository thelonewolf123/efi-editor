package sample;

import java.io.*;

class CommandGenerator {

    public static String execute(String cmd){
        String resultList="";
        String result="";
        Process process= null;
        try {
            process = Runtime.getRuntime().exec(cmd);

            BufferedReader reader=new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader error=new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((result = reader.readLine())!=null){
                resultList += result + "\n";
                System.out.println(result);
            }
            while ((result=error.readLine()) != null){
                resultList += result + "\n";
                System.out.println(result);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e);
        }

        return resultList;
    }
}
