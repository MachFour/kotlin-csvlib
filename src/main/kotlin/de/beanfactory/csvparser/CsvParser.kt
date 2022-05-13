package de.beanfactory.csvparser

import de.beanfactory.csvparser.CsvParser.ParserState.*

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
private const val ESCAPE_CHAR = '\\'

typealias CsvRow = ArrayList<String>

class CsvParser(
    private val separator: FieldSeparator
) {

    /**
     * parse the given string as csv.
     *
     */
    fun parse(csvString: String): List<CsvRow> {
        val rows = ArrayList<CsvRow>()
        var currentRow = CsvRow()

        var pos = 0
        var state = START

        while (pos < csvString.length) {
            when (state) {
                START -> state = START_OF_ROW

                START_OF_ROW -> {
                    when (csvString[pos]) {
                        NEWLINE_CHAR -> pos++ // skip empty lines
                        else -> state = READ_FIELD_VALUE
                    }
                }
                READ_FIELD_VALUE -> {
                    val result = readFieldValue(csvString, pos)
                    pos = result.newPos
                    currentRow.add(result.value)
                    state = NEXT_FIELD
                }
                END_OF_ROW -> { // end of Row reached, process and start new row
                    rows += currentRow
                    currentRow = CsvRow()
                    state = START_OF_ROW
                }
                NEXT_FIELD -> {
                    state = when (csvString[pos]) {
                        separator.char -> READ_FIELD_VALUE // start new field value
                        NEWLINE_CHAR -> END_OF_ROW
                        else -> ERROR_EXPECTED_FIELD_SEPARATOR
                    }
                    pos++
                }
                ERROR_EXPECTED_FIELD_SEPARATOR -> {
                    throw CsvParserException("Unexpected end of field, expected field separator.\n" +
                        ">>> ${csvString.substring(0, minOf(pos, csvString.length))} <<< here"
                    )
                }
                else -> throw CsvParserException(
                    "Unknown state $state @ ${pos}, consumed so far: ${csvString.substring(0, pos)}"
                )
            }
        }

        if (currentRow.isNotEmpty()) {
            rows += currentRow
        }

        return rows
    }

    private fun readFieldValue(inputData: String, startPos: Int): ParseResult<String> {
        var pos = startPos
        var value = ""
        var state = START

        while (pos < inputData.length) {
            when (state) {
                START -> {
                    state = when (inputData[pos]) {
                        QUOTE_CHAR -> READ_QUOTED_CHARS
                        else -> READ_UNQUOTED_CHARS
                    }
                    if (state == READ_QUOTED_CHARS) {
                        pos++
                    }
                }
                READ_UNQUOTED_CHARS -> {
                    val result = readFieldChar(inputData, pos, "${separator.char}$NEWLINE_CHAR", false)
                    state = result.newState ?: state
                    pos = result.newPos
                    value += result.value
                    // Field terminates at end of data
                    if (pos >= inputData.length) {
                        state = END_OF_FIELD
                    }
                }
                READ_QUOTED_CHARS -> {
                    val result = readFieldChar(inputData, pos, "$QUOTE_CHAR", true)
                    state = result.newState ?: state
                    pos = result.newPos
                    value += result.value
                    // Field cannot be terminated by end of data
                    if (pos >= inputData.length && state != END_OF_FIELD) {
                        state = ERROR_UNTERMINATED_QUOTED_FIELD
                    }
                }
                END_OF_FIELD -> return ParseResult(value, null, pos)
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
        return ParseResult(value, null, pos)
    }


    private fun readFieldChar(
        inputData: String,
        startPos: Int,
        terminator: String,
        readBehindTerminator: Boolean
    ): ParseResult<String> {
        val result = readNextChar(inputData, startPos)

        return if (terminator.contains(result.value) && result.newState != ESCAPED_CHARACTER) {
            ParseResult(
                value = "",
                newState = END_OF_FIELD,
                newPos = if (readBehindTerminator) result.newPos else result.newPos - 1
            )
        } else {
            ParseResult(
                value = result.value.toString(),
                newState = result.newState?.takeIf { it.isError },
                newPos = result.newPos
            )
        }
    }

    private fun readNextChar(inputData: String, pos: Int): ParseResult<Char> {
        val isLastChar = inputData.length <= pos + 1

        // for when there's no special character
        val ordinaryParse = ParseResult(
            value = inputData[pos],
            newState = null,
            newPos = pos + 1
        )

        return when (inputData[pos]) {
            ESCAPE_CHAR -> { // read escaped char
                if (isLastChar) {
                    // catch escape without data
                    ParseResult(
                        value = inputData[pos],
                        newState = ERROR_INCOMPLETE_ESCAPE,
                        newPos = pos
                    )
                } else {
                    ParseResult(
                        value = inputData[pos + 1],
                        newState = ESCAPED_CHARACTER,
                        newPos = pos + 2
                    )
                }
            }
            QUOTE_CHAR -> {
                if (!isLastChar && inputData[pos + 1] == QUOTE_CHAR) {
                    // this is a double quote, reduce to one quote with escape
                    ParseResult(
                        value = QUOTE_CHAR,
                        newState = ESCAPED_CHARACTER,
                        newPos = pos + 2
                    )
                } else {
                    ordinaryParse
                }
            }
            // if we haven't returned yet, then it's just a normal character
            else -> ordinaryParse
        }

    }

    private data class ParseResult<T>(val value: T, val newState: ParserState?, val newPos: Int)

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