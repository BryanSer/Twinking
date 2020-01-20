import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class Tesst {

    @Test
    public void test() {
        List<Runnable> list = new ArrayList();
        int b = 0;
        for (int i = 0; i < 10; i++) {
            int f = i;
            int a = b;
            long time = System.currentTimeMillis();
            list.add(new Runnable() {
                @Override
                public void run() {
                    System.out.println(f + a);
                }
            });
            System.out.println(System.currentTimeMillis() - time);
            b++;
        }
    }


}
