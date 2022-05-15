package com.machfour.ksvlib

class CsvConfig(
    val fieldSeparator: Char = DEFAULT_SEPARATOR,
    val quoteCharacter: Char = DEFAULT_QUOTE_CHAR,
    val useCRLF: Boolean = false,
    val quoteAllFields: Boolean = false,
) {
    val lineTerminator: String = if (useCRLF) "\r\n" else "\n"

    fun copy(
        fieldSeparator: Char? = null,
        quoteCharacter: Char? = null,
        useCRLF: Boolean? = null,
        quoteAllFields: Boolean? = null
    ) = CsvConfig(
            fieldSeparator = fieldSeparator ?: this.fieldSeparator,
            quoteCharacter = quoteCharacter ?: this.quoteCharacter,
            useCRLF = useCRLF ?: this.useCRLF,
            quoteAllFields = quoteAllFields ?: this.quoteAllFields,
        )
}

private const val DEFAULT_SEPARATOR = ','
private const val DEFAULT_QUOTE_CHAR = '"'

val CONFIG_DEFAULT = CsvConfig(DEFAULT_SEPARATOR,  DEFAULT_QUOTE_CHAR, useCRLF = false, quoteAllFields = false)
val CONFIG_WINDOWS = CsvConfig(DEFAULT_SEPARATOR, DEFAULT_QUOTE_CHAR, useCRLF = true, quoteAllFields = false)
val CONFIG_SEMICOLON = CsvConfig(';', DEFAULT_QUOTE_CHAR, useCRLF = false, quoteAllFields = false)