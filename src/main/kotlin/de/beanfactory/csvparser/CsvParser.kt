package de.beanfactory.csvparser

import de.beanfactory.csvparser.CsvParser.ParserState.*

/**
 * contains a field, read from the csv rows
 */
data class Field(val name: String, val value: String)

/**
 * contains a data row of fields
 */
data class Row(var fields: List<Field> = ArrayList()) {
    fun add(field: Field) {
        fields += field
    }
}

/**
 * CsvParser implementation.
 *
 * <p>
 * Supported features:
 * <ul><li>quoted multiline fields
 * <li>escaping characters inside fields (using backslash)
 * <li>escaped multiline fields (using backslash)
 * <li>basic error handling
 * </ul>
 * </p>
 *
 * <pre>
 * MIT License
 *
 * Copyright (c) 2021 Thomas Strauß
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * </pre>
 */

private const val NEWLINE_CHAR = '\n'
private const val QUOTE_CHAR = '"'

class CsvParser(
    private val fieldSeparator: FieldSeparator
) {

    private val fieldNames = ArrayList<String>()
    private var currentField: Int = 0

    /**
     * parse the given string as csv.
     *
     */
    fun parse(csvString: String): List<Row> {
        val rows = ArrayList<Row>()
        var currentRow = Row()

        var pos = 0
        var state: ParserState = START

        while (pos < csvString.length) {
            when (state) {
                START -> {
                    state = START_OF_ROW
                }
                START_OF_ROW -> {
                    when (csvString[pos]) {
                        NEWLINE_CHAR -> pos++ // skip empty lines
                        else -> state = READ_FIELD_VALUE
                    }
                }
                READ_FIELD_VALUE -> {
                    val fieldName = nextFieldName()
                    val result = readField(fieldName, csvString.substring(pos))

                    pos += result.newPos
                    currentRow.add(result.value)
                    state = NEXT_FIELD
                }
                END_OF_ROW -> { // end of Row reached, process and start
                    pos++
                    rows += currentRow
                    currentRow = Row()
                    currentField = 0
                    state = START_OF_ROW
                }
                NEXT_FIELD -> {
                    state = when (csvString[pos]) {
                        fieldSeparator.char -> {
                            pos++
                            READ_FIELD_VALUE // start new field value
                        }
                        NEWLINE_CHAR -> END_OF_ROW
                        else -> ERROR_EXPECTED_FIELD_SEPARATOR
                    }
                }
                ERROR_EXPECTED_FIELD_SEPARATOR -> {
                    throw CsvParserException("Unexpected end of row, expected field separator.\n" +
                        ">>> ${csvString.substring(0, minOf(pos + 1, csvString.length))} <<< here"
                    )
                }
                else -> throw CsvParserException(
                    "Unknown state $state @ ${pos}, consumed so far: ${csvString.substring(0, pos)}"
                )
            }
        }
        csvString.chars()

        if (currentRow.fields.isNotEmpty()) {
            rows += currentRow
        }

        return rows
    }

    private fun readField(name: String, inputData: String): ParseResult<Field> {
        var pos = 0
        var value = ""
        var state: ParserState = START

        while (pos < inputData.length) {
            when (state) {
                START -> {
                    state = when (inputData[pos]) {
                        QUOTE_CHAR -> {
                            pos++
                            READ_QUOTED_CHARS
                        }
                        else -> READ_UNQUOTED_CHARS
                    }
                }
                READ_UNQUOTED_CHARS -> {
                    val result = readFieldChar("${fieldSeparator.char}$NEWLINE_CHAR", inputData.substring(pos), false)
                    state = result.state ?: state
                    pos += result.newPos
                    value += result.value
                    // Field terminates at end of data
                    if (pos >= inputData.length) {
                        state = END_OF_FIELD
                    }
                }
                READ_QUOTED_CHARS -> {
                    val result = readFieldChar("$QUOTE_CHAR", inputData.substring(pos), true)
                    state = result.state ?: state
                    pos += result.newPos
                    value += result.value
                    // Field cannot be terminated by end of data
                    if (pos >= inputData.length && state != END_OF_FIELD) {
                        state = ERROR_UNTERMINATED_QUOTED_FIELD
                    }
                }
                END_OF_FIELD -> {
                    return ParseResult(pos, Field(name, value))
                }
                ERROR_INCOMPLETE_ESCAPE -> {
                    throw CsvParserException("Unexpected end of data during escape character processing\n" +
                        ">>> ${inputData.substring(0, minOf(pos + 1, inputData.length))} <<< here"
                    )
                }
                else -> throw CsvParserException("unknown state $state")
            }
        }

        if (state != END_OF_FIELD) {
            throw CsvParserException(
                "Unexpected end of data in state $state\n" +
                    ">>> ${inputData.substring(0, minOf(pos + 1, inputData.length))} <<< here"
            )
        }

        // end of field at end of file
        return ParseResult(pos, Field(name, value))
    }

    private fun nextFieldName(): String {
        currentField++
        return if (fieldNames.size > currentField) {
            fieldNames[currentField]
        } else {
            currentField.toString()
        }
    }

    private fun readFieldChar(terminator: String, inputData: String, readBehindTerminator: Boolean): ParseResult<String> {
        val result = readNextChar(inputData)

        val resultPos: Int
        val resultValue: String
        val resultState: ParserState?

        if (terminator.contains(result.value) && result.state != ESCAPED_CHARACTER) {
            resultPos = if (readBehindTerminator) result.newPos else result.newPos - 1
            resultValue = ""
            resultState = END_OF_FIELD
        } else {
            resultPos = result.newPos
            resultValue = result.value.toString()
            resultState = result.state?.takeIf { it.isError }
        }

        return ParseResult(resultPos, resultValue, resultState)
    }

    private fun readNextChar(inputData: String, pos: Int = 0): ParseResult<Char> {
        val state: ParserState?
        val value: Char
        val endPos: Int

        if (inputData[pos] == '\\') {   // read escaped
            if (inputData.length <= 1) { // catch escape without data
                state = ERROR_INCOMPLETE_ESCAPE
                value = inputData[pos]
                endPos = pos
            } else {
                state = ESCAPED_CHARACTER
                value = inputData[1]
                endPos = pos + 2
            }
        } else if (inputData[pos] == QUOTE_CHAR && inputData.length > 1 && inputData[pos+1] == QUOTE_CHAR) {
            // this is a double quote, reduce to one quote with escape
            state = ESCAPED_CHARACTER
            value = QUOTE_CHAR
            endPos = pos + 2
        } else {
            state = null
            value = inputData[pos]
            endPos = pos + 1
        }

        return ParseResult(endPos, value, state)
    }

    private data class ParseResult<T>(val newPos: Int, val value: T, val state: ParserState? = null)

    private enum class ParserState {
        START,
        READ_FIELD_VALUE,
        NEXT_FIELD,
        END_OF_ROW,
        START_OF_ROW,
        READ_QUOTED_CHARS,
        READ_UNQUOTED_CHARS,
        END_OF_FIELD,
        ESCAPED_CHARACTER,
        ERROR_EXPECTED_FIELD_SEPARATOR,
        ERROR_INCOMPLETE_ESCAPE,
        ERROR_UNTERMINATED_QUOTED_FIELD,
        ;

        val isError: Boolean = name.startsWith("ERROR")
    }

}