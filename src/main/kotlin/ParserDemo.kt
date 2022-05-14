import com.machfour.ksvlib.CONFIG_DEFAULT
import com.machfour.ksvlib.CsvParser


fun parserDemo(csv: String) {
    val parser = CsvParser(CONFIG_DEFAULT)
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