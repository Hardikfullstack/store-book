import java.text.NumberFormat
import java.util.Locale

fun main() {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    format.maximumFractionDigits = 0
    println(format.format(1234567.0))
    
    val f2 = NumberFormat.getNumberInstance(Locale("en", "IN"))
    f2.maximumFractionDigits = 2
    f2.minimumFractionDigits = 0
    println("₹" + f2.format(1234567.89))
}
