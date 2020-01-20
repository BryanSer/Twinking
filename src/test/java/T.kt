import org.junit.Test
import java.lang.IllegalArgumentException

class T {
    @Test
    fun test() {
        println(sout(99))
    }

    inline fun sout(i:Int):Int{
        System.out.println(i)
        if(Math.random() < 0.5){
            return 4
        }else{
            return 5
        }
    }
}