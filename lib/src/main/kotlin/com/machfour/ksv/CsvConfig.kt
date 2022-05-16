package com.machfour.ksv

private const val DEFAULT_SEPARATOR = ','
private const val DEFAULT_QUOTE_CHAR = '"'

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

    companion object {
        val DEFAULT = CsvConfig(DEFAULT_SEPARATOR,  DEFAULT_QUOTE_CHAR, useCRLF = false, quoteAllFields = false)
        val WINDOWS = CsvConfig(DEFAULT_SEPARATOR, DEFAULT_QUOTE_CHAR, useCRLF = true, quoteAllFields = false)
        val SEMICOLON = CsvConfig(';', DEFAULT_QUOTE_CHAR, useCRLF = false, quoteAllFields = false)
        val TAB = CsvConfig('\t', DEFAULT_QUOTE_CHAR, useCRLF = false, quoteAllFields = false)

    }
}

