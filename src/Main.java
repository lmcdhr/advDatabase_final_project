/*Team Members:
1. MuCheng Luo (ml6389)
2. Jingwei Ye (jy3555)
Date: 12/07/2021*/

import java.io.File;

public class Main {

    public static void main( String[] args ) {
        File directoryPath = new File("src/tests");
        TransactionManager tm = new TransactionManager();
        String[] fileNames = directoryPath.list();
        String newLine = System.getProperty("line.separator");
        for(int i=0; i<fileNames.length; i++){
            System.out.println(newLine + "This is test " + i);
            tm.readInputFile("src/tests/" + fileNames[i]);
        }
        // tm.dm.dump();
    }
}
