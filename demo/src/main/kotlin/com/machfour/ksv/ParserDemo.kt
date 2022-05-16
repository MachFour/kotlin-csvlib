import com.machfour.ksv.CsvConfig
import com.machfour.ksv.CsvParser

fun parserDemo(csv: String) {
    val parser = CsvParser(CsvConfig.DEFAULT)
    parser.parse(csv).forEach { row ->
        row.forEachIndexed { index, field -> print("[F$index] $field ") }
        println()
    }
}

// Simple CSV parser demo
fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        println("This program does not take any arguments." +
                "It simply accepts stdin until EOF and parses the resulting string as CSV.")
        return
    }

    val csv = buildList {
        while (true) {
            val line = readLine() ?: break
            add(line)
        }
    }.joinToString(separator = "\n")

    parserDemo(csv)
}