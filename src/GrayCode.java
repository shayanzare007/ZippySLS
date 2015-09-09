import java.util.HashMap;

/* GrayCode is a well known code that produces recursively every possible
 * bit string for a certain number of bits. We've just modified it so that
 * it adds these combinations to a given map.*/

public class GrayCode {

    // append reverse of order n gray code to prefix string, and print
    public static void yarg(String prefix, int n, HashMap<String,Double> map) {
        if (n == 0) map.put(prefix, 0.0);
        else {
            gray(prefix + "1", n - 1,map);
            yarg(prefix + "0", n - 1,map);
        }
    }  

    // append order n gray code to end of prefix string, and print
    public static void gray(String prefix, int n, HashMap<String,Double> map) {
        if (n == 0) map.put(prefix, 0.0);
        else {
            gray(prefix + "0", n - 1,map);
            yarg(prefix + "1", n - 1,map);
        }
    }  


}
