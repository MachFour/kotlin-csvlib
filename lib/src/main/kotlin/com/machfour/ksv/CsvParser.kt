package com.machfour.ksv

import com.machfour.ksv.CsvParser.ParserState.*

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
 * Copyright (c) 2021-2022 Thomas Strauß, Max Fisher
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

private const val ESCAPE_CHAR = '\\'


class CsvParser(val config: CsvConfig) {
    // TODO add functions to
    //  - add string data (i.e 1 row)
    //  - test if can parse a row from current data
    //  - parse one more row if possible OR parse one row and throw exception if not possible

    /**
     * parse the given string as csv.
     *
     */
    fun parse(csvString: String): List<CsvRow> {
        val rows = ArrayList<CsvRow>()
        var currentRow = ArrayList<String>()

        var pos = 0
        var state = START

        while (pos < csvString.length) {
            when (state) {
                START -> state = START_OF_ROW

                START_OF_ROW -> {
                    if (csvString.startsWith(config.lineTerminator, pos)) {
                        // skip empty line
                        pos += config.lineTerminator.length
                    } else {
                        state = READ_FIELD_VALUE
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
                    currentRow = ArrayList()
                    state = START_OF_ROW
                }
                NEXT_FIELD -> {
                    state = when {
                        csvString[pos] == config.fieldSeparator -> {
                            pos++
                            READ_FIELD_VALUE // start new field value
                        }
                        csvString.startsWith(config.lineTerminator, pos) -> {
                            pos += config.lineTerminator.length
                            END_OF_ROW
                        }
                        else -> ERROR_EXPECTED_FIELD_SEPARATOR
                    }
                }
                ERROR_EXPECTED_FIELD_SEPARATOR -> {
                    throw CsvParseException("Unexpected end of field, expected field separator.\n" +
                        ">>> ${csvString.substring(0, minOf(pos, csvString.length))} <<< here"
                    )
                }
                else -> throw CsvParseException(
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
                    state = if (inputData[pos] == config.quoteCharacter) {
                        pos++
                        READ_QUOTED_CHARS
                    } else {
                        READ_UNQUOTED_CHARS
                    }
                }
                READ_UNQUOTED_CHARS -> {
                    val result = readFieldChar(inputData, pos, config.fieldSeparator, endOnNewLine = true, readBehindEndMarker = false)
                    state = result.newState ?: state
                    pos = result.newPos
                    value += result.value
                    // Field terminates at end of data
                    if (pos >= inputData.length) {
                        state = END_OF_FIELD
                    }
                }
                READ_QUOTED_CHARS -> {
                    val result = readFieldChar(inputData, pos, config.quoteCharacter, endOnNewLine = false, readBehindEndMarker = true)
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
                    throw CsvParseException("Unexpected end of data during escape character processing\n" +
                        ">>> ${inputData.substring(0, minOf(pos + 1, inputData.length))} <<< here"
                    )
                }
                else -> throw CsvParseException("unknown state $state")
            }
        }

        if (state != END_OF_FIELD) {
            throw CsvParseException(
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
        endMarker: Char,
        endOnNewLine: Boolean,
        readBehindEndMarker: Boolean
    ): ParseResult<String> {
        val result = readNextChar(inputData, startPos)

        return if (
            (result.value == endMarker && result.newState != ESCAPED_CHARACTER) ||
            (endOnNewLine && inputData.startsWith(config.lineTerminator, startPos))
        ) {
            ParseResult(
                value = "",
                newState = END_OF_FIELD,
                newPos = if (readBehindEndMarker) result.newPos else result.newPos - 1
            )
        } else {
            ParseResult(
                value = result.value.toString(),
                // If there was an error while parsing the next char, we want to update the parent state with it,
                // otherwise return null to preserve the previous state
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
            config.quoteCharacter -> {
                if (!isLastChar && inputData[pos + 1] == config.quoteCharacter) {
                    // this is a double quote, reduce to one quote with escape
                    ParseResult(
                        value = config.quoteCharacter,
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