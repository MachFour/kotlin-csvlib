package com.machfour.ksv

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


private val testCsvFields1 = listOf(
    listOf("1", "2", "3", "4", "5"),
    listOf("a", "b", "c", "d", "e"),
    listOf("!", "@", "#", "$", "%"),
)

internal class CsvWriterTest {

    @Test
    fun testBasic() {
        val expected = "1,2,3,4,5\na,b,c,d,e\n!,@,#,$,%\n"
        val writer = CsvWriter(CsvConfig.DEFAULT)

        assertEquals(expected, writer.write(testCsvFields1))
    }

    @Test
    fun testBasicCRLF() {
        val expected = "1,2,3,4,5\r\na,b,c,d,e\r\n!,@,#,$,%\r\n"
        val writer = CsvWriter(CsvConfig.WINDOWS)

        assertEquals(expected, writer.write(testCsvFields1))
    }


    @Test
    fun testQuotingNewline() {
        val csvFields = listOf(
            listOf("1\n", "2", "3"),
            listOf("a", "b\n", "c"),
            listOf("!", "@", "#\n")
        )

        val expected = """
            "1
            ",2,3
            a,"b
            ",c
            !,@,"#
            "
            
            """.trimIndent()

        val writer = CsvWriter(CsvConfig.DEFAULT)
        assertEquals(expected, writer.write(csvFields))
    }

    @Test
    fun testQuotingSeparator() {
        val csvFields = listOf(
            listOf("1", "2", ",3"),
            listOf("a", ",b", "c"),
            listOf(",!", "@", "#")
        )

        val expected = """
            1,2,",3"
            a,",b",c
            ",!",@,#
            
            """.trimIndent()

        val writer = CsvWriter(CsvConfig.DEFAULT)
        assertEquals(expected, writer.write(csvFields))
    }

    @Test
    fun testEscapingQuoteChar() {
        val csvFields = listOf(
            listOf("\"1\"", "\"2", "3\""),
            listOf("\"\"a\"\"", "\"\"b", "c\"\""),
        )

        val lf = "\n"
        val q2 = "\"\""
        val q4 = "\"\"\"\""
        val expected = """"${q2}1${q2}","${q2}2","3${q2}"$lf"${q4}a${q4}","${q4}b","c${q4}"$lf"""

        val writer = CsvWriter(CsvConfig.DEFAULT)
        assertEquals(expected, writer.write(csvFields))
    }
}