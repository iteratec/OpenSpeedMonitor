package de.iteratec.osm.report.external

/**
 * @author nkuhn
 */
class MockedGraphiteSocket implements GraphiteSocket{
    class SentDate {
        GraphitePathName path
        Double value
        Date timestamp
    }
    List<SentDate> sentDates = []

    @Override
    void sendDate(GraphitePathName path, double value, Date timestamp) throws NullPointerException, GraphiteComunicationFailureException {
        sentDates.add(new SentDate(path: path, value: value, timestamp: timestamp))
    }
}
