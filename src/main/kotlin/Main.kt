import de.beanfactory.csvparser.CsvParser
import de.beanfactory.csvparser.FieldSeparator
import kotlin.io.path.Path
import kotlin.io.path.readBytes

fun main(args: Array<String>) {
    println("Loading file ${args[0]}")

    val readBytes = Path(args[0]).readBytes()
    readBytes[0] = 0x20
    readBytes[1] = 0x20
    readBytes[2] = 0x20
    val csvFile = String(readBytes, Charsets.UTF_8)

    val csvClean = csvFile.split("\r").joinToString(separator = "")
    val csvRows = CsvParser(FieldSeparator.COMMA).parse(csvClean)

    csvRows.forEach {
        it[1].split(",").forEach(::println)
    }
}