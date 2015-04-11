package org.yats.trading;

import com.xeiam.xchange.bitfinex.v1.dto.marketdata.BitfinexLevel;
import org.yats.common.Decimal;

import java.util.Vector;

public class OfferBookSide { // implements List<BookRow> {

    public static final String CSV_SEPARATOR = ";";

    public int size() {
        return bookHalf.size();
    }

    public BookRow getRow(int index) {
        return bookHalf.elementAt(index);
    }

    public String toStringCSV() {
        StringBuilder b = new StringBuilder();
        boolean firstRow = true;
        for(BookRow r : bookHalf) {
            if(!firstRow) {
                b.append(CSV_SEPARATOR);
            }
            b.append(r.toStringCSV());
            firstRow=false;
        }
        return b.toString();
    }

    public static OfferBookSide fromStringCSV(String s, BookSide _side) {
        OfferBookSide bookHalf = new OfferBookSide(_side);
        String[] rows = s.split(CSV_SEPARATOR);
        for(String r : rows) {
            BookRow row = BookRow.fromStringCSV(r);
            bookHalf.add(row);
        }
        return bookHalf;
    }

    public void add(BookRow row) {
        bookHalf.add(row);
    }

    public boolean isSameBestRowsAs(OfferBookSide other, int numberOfBestRowsToCompare)
    {
        if(bookHalf.size() != other.bookHalf.size()) return false;
        int maxRows = Math.min(bookHalf.size(), numberOfBestRowsToCompare);
        for(int i=0; i<maxRows; i++) {
            if(!bookHalf.get(i).isSameAs(other.getRow(i))) return false;
        }
        return true;
    }

    public OfferBookSide(BookSide _side) {
        side =_side;
        bookHalf = new Vector<BookRow>();
    }


    public Decimal getPriceForSize(double thresholdSize) {
        double sum = 0.0;
        for(int i=0; i<bookHalf.size(); i++) {
            sum+=bookHalf.get(i).getSize().toDouble();
            if(sum>=thresholdSize) {
                return bookHalf.get(i).getPrice();
            }
        }
        return bookHalf.lastElement().getPrice();
    }

    ////////////////////////////////////////////////////////////////////

    private BookSide side;
    private Vector<BookRow> bookHalf;

}
