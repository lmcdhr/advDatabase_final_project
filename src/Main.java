/*Team Members:
1. MuCheng Luo (ml6389)
2. Jingwei Ye (jy3555)
Date: 12/07/2021*/

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class Main {

    public static void main( String[] args ) {
        if (args.length != 2){
            System.out.println("expected 2 arguments");
        }
        String inputfiles = args[0];
        String outputfiles = args[1];

        File inDirectoryPath = new File(inputfiles);
        TransactionManager tm = new TransactionManager();
        String[] fileNames = inDirectoryPath.list();
        String newLine = System.getProperty("line.separator");
        for(int i=0; i<fileNames.length; i++){
            File output = new File(outputfiles + "/output" + i + ".txt");
            try {
                PrintStream stream = new PrintStream(output);
                System.out.println(newLine + "This is test " + i);
                tm.readInputFile("inputfiles/" + fileNames[i]);
                System.setOut(stream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        // tm.dm.dump();
    }
}
