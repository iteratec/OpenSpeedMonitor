package de.iteratec.osm.util

import grails.gorm.transactions.Transactional
import org.supercsv.encoder.DefaultCsvEncoder
import org.supercsv.io.CsvListWriter
import org.supercsv.prefs.CsvPreference

@Transactional
class CsvExportService {

    /**
     * Writes given data as csv file to target.
     * @param headers the headers for the csv file
     * @param rows the data for the csv file. Note: all data rows are mapped by index to the headers. So keep order and integrity in mind.
     * @param target a writer to write the csv file into
     */
    static void writeCSV(List<String> headers, List<List<String>> rows, Writer target) throws IOException {
        assert rows.every {it.size() == headers.size()}

        CsvListWriter csvWriter = new CsvListWriter(
                target,
                new CsvPreference.Builder(CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE).useEncoder(new DefaultCsvEncoder()).build()
        )

        csvWriter.writeHeader(headers.toArray(new String[headers.size()]))

        rows.each { row ->
            csvWriter.writeRow(row)
        }

        csvWriter.flush()
    }
}
