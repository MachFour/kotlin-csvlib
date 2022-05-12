package de.beanfactory.csvparser

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class CsvParserTest {


    private fun testParseCSVHelper(useComma: Boolean) {
        val parser = CsvParser(if (useComma) FieldSeparator.COMMA else FieldSeparator.SEMICOLON)

        val result = parser.parse(if (useComma) csvTest1Comma else csvTest1)

        Assertions.assertEquals(11, result.size)

        Assertions.assertEquals("mrBranche", result[0][0])
        //Assertions.assertEquals("1", result[0][0].name)
        Assertions.assertEquals("mrBeschrTechn", result[0][1])
        //Assertions.assertEquals("2", result[0][1].name)
        Assertions.assertEquals("mrStartJahr", result[0][2])
        //Assertions.assertEquals("3", result[0][2].name)

        Assertions.assertEquals("Automotive", result[1][0])
        Assertions.assertEquals("Kanban, Jira, Confluence", result[1][1])
        Assertions.assertEquals("2017", result[1][2])

        Assertions.assertEquals("Telekommunikation", result[9][0])
        Assertions.assertEquals(
            """
            Scrum
            intelliJ
            Protractor
            GIT
            Jira
            Large/ small screen devices""".trimIndent(),
            result[9][1]
        )
        Assertions.assertEquals("2015", result[9][2])

        Assertions.assertEquals("Telekommunikation", result[10][0])
        //Assertions.assertEquals("1", result[10][0].name)
        Assertions.assertEquals(
            """
            Scrum
            intelliJ
            Protractor
            GIT
            Jira
            Large/ small screen devices""".trimIndent(),
            result[10][1]
        )
        //Assertions.assertEquals("2", result[10][1].name)
        Assertions.assertEquals("2015            ", result[10][2])
        //Assertions.assertEquals("3", result[10][2].name)
    }

    @Test
    fun testParseCSVSemicolon() {
        testParseCSVHelper(useComma=false)
    }

    @Test
    fun testParseCSVComma() {
        testParseCSVHelper(useComma=true)
    }


    @Test
    fun testParseEndOfCSV() {
        val parser = CsvParser(FieldSeparator.SEMICOLON)

        val result = parser.parse(csvTest2)

        Assertions.assertEquals(1, result.size)

        Assertions.assertEquals("Telekommunikation", result[0][0])
        Assertions.assertEquals(
            """
            Scrum
            intelliJ
            Protractor
            GIT
            Jira
            Large/ small screen devices""".trimIndent(), result[0][1]
        )
        Assertions.assertEquals("2015            ", result[0][2])
    }


    @Test
    fun testParseEndOfCSVWithLF() {
        val parser = CsvParser(FieldSeparator.SEMICOLON)

        val result = parser.parse(csvTest3)

        Assertions.assertEquals(1, result.size)

        Assertions.assertEquals("Telekommunikation", result[0][0])
        Assertions.assertEquals(
            """
            Scrum
            intelliJ
            Protractor
            GIT
            Jira
            Large/ small screen devices""".trimIndent(), result[0][1]
        )
        Assertions.assertEquals("2015            ", result[0][2])
    }


    @Test
    fun testErrorOnUnbalancedQuotes() {
        val parser = CsvParser(FieldSeparator.SEMICOLON)

        try {
            parser.parse(csvTest4)
            Assertions.fail("Should have aborted with exception")
        }
        catch (e: CsvParserException) {
            // pass
        }
    }


    @Test
    fun testErrorOnIncompleteEscape() {
        val parser = CsvParser(FieldSeparator.SEMICOLON)

        try {
            parser.parse(csvTest5)
            Assertions.fail("Should have aborted with exception")
        }
        catch (e: CsvParserException) {
            // pass
        }
    }

    @Test
    fun testErrorOnMissingFieldSeparator() {
        val parser = CsvParser(FieldSeparator.SEMICOLON)

        try {
            parser.parse(csvTest6)
            Assertions.fail("Should have aborted with exception")
        }
        catch (e: CsvParserException) {
            //pass
        }
    }


    @Test
    fun testProperlyDetectsEscapedTerminationCharacterInQuotedStrings() {
        val parser = CsvParser(FieldSeparator.SEMICOLON)

        val result = parser.parse(csvTest7)

        Assertions.assertEquals("Telekommunikation", result[0][0])
        Assertions.assertEquals(
            """
            Scrum
            intelliJ
            Protractor
            GIT"
            Jira"
            Large/ small screen devices""".trimIndent(),
            result[0][1]
        )
        Assertions.assertEquals("2015", result[0][2])
    }


    @Test
    fun testProperlyDetectsEscapedTerminationCharacterInUnquotedStrings() {
        val parser = CsvParser(FieldSeparator.SEMICOLON)

        val result = parser.parse(csvTest8)

        Assertions.assertEquals("Telekommunikation", result[0][0])
        Assertions.assertEquals(
            """
            Scrum
            intelliJ
            Protractor
            GIT" "
            Jira
            Large/ small screen devices;""".trimIndent(),
            result[0][1]
        )
        Assertions.assertEquals("2015", result[0][2])
    }
}