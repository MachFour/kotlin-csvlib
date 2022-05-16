package com.machfour.ksv

// CSV rules, adapted from https://super-csv.github.io/super-csv/csv_specification.html
// 1. Each record is located on a separate line, delimited by a line break
// 2. The last record in the file may or may not have an ending line break.
//    By default, a line break is added after the last line when writing CSV, but is optional when reading.
// 3. There maybe an optional header line appearing as the first line of the file,
//     with the same format as normal record lines.  This header will contain names
//     corresponding to the fields in the file and should contain the same number of
//     fields as the records in the rest of the file.
// 4. Within the header and each record, there may be one or more fields, separated by commas.
//    Each line should contain the same number of fields throughout the file. Spaces are
//    considered part of a field and should not be ignored.  The last field in the record
//    must not be followed by a comma. The delimiter is configurable, though it is typically a comma.
//    By default, spaces are considered part of a field.
// 5. Each field may or may not be enclosed in double quotes (however some programs do not use double quotes at all).
//    If fields are not enclosed with double quotes, then double quotes may not appear inside the fields.
// 6. Fields containing line breaks, double quotes, and commas should be enclosed in double-quotes.
//    Multi-line fields (when enclosed in quotes) can be handled while reading, and are enclosed in quotes when writing.
//    Fields containing a quote character or field delimiter are also quoted when writing.
// 7. If double-quotes are used to enclose fields, then a double-quote appearing inside a field must be escaped
//    by preceding it with another double quote. Escaping with a backslash is supported when reading but not writing.

class CsvWriter(val config: CsvConfig) {
    fun write(rows: Collection<CsvRow>): String {
        return rows.joinToString(separator = config.lineTerminator, postfix = config.lineTerminator) { row ->
            row.joinToString(separator = config.fieldSeparator.toString()) { it.quoteField() }
        }
    }

    private fun String.quoteField(): String {
        return if (contains(config.lineTerminator) || contains(config.fieldSeparator) || contains(config.quoteCharacter)) {
            val quoteString = config.quoteCharacter.toString()
            val escapedQuoteString = quoteString + quoteString
            // quote characters are escaped using another quote character
            "\"${replace(quoteString, escapedQuoteString)}\""
        } else {
            // no quotes needed
            this
        }
    }
}