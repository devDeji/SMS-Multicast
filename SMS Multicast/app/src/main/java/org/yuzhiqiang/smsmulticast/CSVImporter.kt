package org.yuzhiqiang.smsmulticast

import com.opencsv.CSVReader
import java.io.FileReader
import java.io.Reader
import java.io.StringReader
import java.util.*

object CSVImporter {

    @Throws(Exception::class)
    fun fromFile(filePath: String): ArrayList<Message> {
        val fileReader = FileReader(filePath)
        return importCSV(fileReader)
    }

    @Throws(Exception::class)
    fun fromString(CSVString: String): ArrayList<Message> {
        val stringReader = StringReader(CSVString)
        return importCSV(stringReader)
    }

    @Throws(Exception::class)
    private fun importCSV(reader: Reader): ArrayList<Message> {
        val csvReader = CSVReader(reader)
        val list = ArrayList<Message>()
        while (true) {
            val line = csvReader.readNext() ?: break
            if (line.size != 2)
                throw Exception()
            val phoneNumber = line[0]
            val message = line[1]
            list.add(Message(phoneNumber, message))
        }
        return list
    }

}
