public class Main {

    public static void main( String[] args ) {
        System.out.println( "this is a test" );
        String filePath = "src/tests/test17.txt";
        TransactionManager tm = new TransactionManager();
        tm.readInputFile(filePath);
        tm.dm.dump();
    }
}
