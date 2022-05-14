package com.machfour.ksvlib

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

internal class CsvParserTest {


    private fun testParseCSVHelper(useComma: Boolean, testCRLF: Boolean) {
        fun String.withDesiredLineEnding(): String {
            return if (testCRLF) replace("\n", "\r\n") else this
        }

        val config: CsvConfig
        val testCsv: String

        if (useComma) {
            config = CONFIG_DEFAULT.copy(useCRLF = testCRLF)
            testCsv = csvTest1Comma.withDesiredLineEnding()
        } else {
            config = CONFIG_SEMICOLON.copy(useCRLF = testCRLF)
            testCsv = csvTest1.withDesiredLineEnding()
        }

        val parser = CsvParser(config)

        val result = parser.parse(testCsv)

        assertEquals(11, result.size)

        assertEquals("mrBranche", result[0][0])
        assertEquals("mrBeschrTechn", result[0][1])
        assertEquals("mrStartJahr", result[0][2])

        assertEquals("Automotive", result[1][0])
        assertEquals("Kanban, Jira, Confluence", result[1][1])
        assertEquals("2017", result[1][2])

        assertEquals("Telekommunikation", result[9][0])
        assertEquals(
            """
            Scrum
            intelliJ
            Protractor
            GIT
            Jira
            Large/ small screen devices""".trimIndent().withDesiredLineEnding(),
            result[9][1]
        )
        assertEquals("2015", result[9][2])

        assertEquals("Telekommunikation", result[10][0])
        assertEquals(
            """
            Scrum
            intelliJ
            Protractor
            GIT
            Jira
            Large/ small screen devices""".trimIndent().withDesiredLineEnding(),
            result[10][1]
        )
        assertEquals("2015            ", result[10][2])
    }

    @Test
    fun testParseCSVSemicolon() {
        testParseCSVHelper(useComma = false, testCRLF = false)
    }

    @Test
    fun testParseCSVComma() {
        testParseCSVHelper(useComma = true, testCRLF = false)
    }

    @Test
    fun testParseCSVWithCRLF() {
        testParseCSVHelper(useComma = true, testCRLF = true)
        testParseCSVHelper(useComma = false, testCRLF = true)
    }

    @Test
    fun testParseMisconfiguredCRLF() {
        val parser = CsvParser(CONFIG_DEFAULT)

        val result = parser.parse(csvTest1Comma.replace("\n", "\r\n"))
        for (row in result) {
            val lastField = row[row.size - 1]
            assertEquals("\r", lastField.substring(lastField.length - 1))
        }
    }

    @Test
    fun testParseMisconfiguredCRLF2() {
        val parser = CsvParser(CONFIG_WINDOWS)
        parser.parse(csvTest1Comma)
    }

    @Test
    fun testSingleColumnCsv() {
        val parser = CsvParser(CONFIG_DEFAULT)
        val result = parser.parse(csvTest9)
        assertEquals(7, result.size)
        for (row in result) {
            assertEquals(1, row.size)
            assertEquals(1, row[0].length)
        }

    }


    @Test
    fun testParseEndOfCSV() {
        val parser = CsvParser(CONFIG_SEMICOLON)

        val result = parser.parse(csvTest2)

        assertEquals(1, result.size)

        assertEquals("Telekommunikation", result[0][0])
        assertEquals(
            """
            Scrum
            intelliJ
            Protractor
            GIT
            Jira
            Large/ small screen devices""".trimIndent(), result[0][1]
        )
        assertEquals("2015            ", result[0][2])
    }


    @Test
    fun testParseEndOfCSVWithLF() {
        val parser = CsvParser(CONFIG_SEMICOLON)

        val result = parser.parse(csvTest3)

        assertEquals(1, result.size)

        assertEquals("Telekommunikation", result[0][0])
        assertEquals(
            """
            Scrum
            intelliJ
            Protractor
            GIT
            Jira
            Large/ small screen devices""".trimIndent(), result[0][1]
        )
        assertEquals("2015            ", result[0][2])
    }


    @Test
    fun testErrorOnUnbalancedQuotes() {
        val parser = CsvParser(CONFIG_SEMICOLON)

        assertFails("Should have aborted with exception") {
            parser.parse(csvTest4)
        }
    }


    @Test
    fun testErrorOnIncompleteEscape() {
        val parser = CsvParser(CONFIG_SEMICOLON)

        assertFails("Should fail with exception") {
            parser.parse(csvTest5)
        }
    }

    @Test
    fun testErrorOnMissingFieldSeparator() {
        val parser = CsvParser(CONFIG_SEMICOLON)

        assertFails("Should fail with exception") {
            parser.parse(csvTest6)
        }
    }


    @Test
    fun testProperlyDetectsEscapedTerminationCharacterInQuotedStrings() {
        val parser = CsvParser(CONFIG_SEMICOLON)

        val result = parser.parse(csvTest7)

        assertEquals("Telekommunikation", result[0][0])
        assertEquals(
            """
            Scrum
            intelliJ
            Protractor
            GIT"
            Jira"
            Large/ small screen devices""".trimIndent(),
            result[0][1]
        )
        assertEquals("2015", result[0][2])
    }


    @Test
    fun testProperlyDetectsEscapedTerminationCharacterInUnquotedStrings() {
        val parser = CsvParser(CONFIG_SEMICOLON)

        val result = parser.parse(csvTest8)

        assertEquals("Telekommunikation", result[0][0])
        assertEquals(
            """
            Scrum
            intelliJ
            Protractor
            GIT" "
            Jira
            Large/ small screen devices;""".trimIndent(),
            result[0][1]
        )
        assertEquals("2015", result[0][2])
    }

    @Test
    fun testCRLFQuoted() {
        val parser = CsvParser(CONFIG_WINDOWS)

        val result = parser.parse(csvTest10)

        assertEquals("1", result[0][0])
        assertEquals("2\r\n3", result[1][0])
    }
}