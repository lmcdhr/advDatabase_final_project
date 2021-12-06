public class Main {

    public static void main( String[] args ) {
        System.out.println( "this is a test" );
        String filePath = "/Users/muchengluo/Desktop/NYU/2434_adv_database/project/src/tests/test1.txt";
        TransactionManager tm = new TransactionManager();
        tm.readInputFile(filePath);
    }
}
