public class Main {

    public static void main( String[] args ) {
        String filePath = "src/tests/test22.txt";
        TransactionManager tm = new TransactionManager();
        tm.readInputFile(filePath);
        //tm.dm.dump();
    }
}
