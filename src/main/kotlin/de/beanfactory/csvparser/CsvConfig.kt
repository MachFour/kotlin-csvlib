package de.beanfactory.csvparser

interface CsvConfig {
    val fieldSeparator: Char
    val quoteCharacter: Char
    val useCRLF: Boolean
    val quoteAllFields: Boolean

    val lineTerminator: String
        get() = if (useCRLF) "\r\n" else "\n"

    fun copy(
        fieldSeparator: Char? = null,
        quoteCharacter: Char? = null,
        useCRLF: Boolean? = null,
        quoteAllFields: Boolean? = null,
    ): CsvConfig
}

private const val DEFAULT_SEPARATOR = ','
private const val DEFAULT_QUOTE_CHAR = '"'

val CONFIG_DEFAULT = makeCsvConfig(DEFAULT_SEPARATOR,  DEFAULT_QUOTE_CHAR, useCRLF = false, quoteAllFields = false)
val CONFIG_WINDOWS = makeCsvConfig(DEFAULT_SEPARATOR, DEFAULT_QUOTE_CHAR, useCRLF = true, quoteAllFields = false)
val CONFIG_SEMICOLON = makeCsvConfig(';', DEFAULT_QUOTE_CHAR, useCRLF = false, quoteAllFields = false)

fun makeCsvConfig(
    fieldSeparator: Char = DEFAULT_SEPARATOR,
    quoteCharacter: Char = DEFAULT_QUOTE_CHAR,
    useCRLF: Boolean = false,
    quoteAllFields: Boolean = false,
): CsvConfig {
    return CsvConfigImpl(
        fieldSeparator = fieldSeparator,
        quoteCharacter = quoteCharacter,
        useCRLF = useCRLF,
        quoteAllFields = quoteAllFields,
    )
}

private class CsvConfigImpl(
    override val fieldSeparator: Char,
    override val quoteCharacter: Char,
    override val useCRLF: Boolean,
    override val quoteAllFields: Boolean,
): CsvConfig {

    override fun copy(
        fieldSeparator: Char?,
        quoteCharacter: Char?,
        useCRLF: Boolean?,
        quoteAllFields: Boolean?
    ): CsvConfig {
        return CsvConfigImpl(
            fieldSeparator = fieldSeparator ?: this.fieldSeparator,
            quoteCharacter = quoteCharacter ?: this.quoteCharacter,
            useCRLF = useCRLF ?: this.useCRLF,
            quoteAllFields = quoteAllFields ?: this.quoteAllFields,

        )
    }
}