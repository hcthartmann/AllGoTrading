package org.yats.trading;

import org.yats.common.Decimal;

public abstract class BookSide {

    public static BookSide NULL = new BookSideNULL();
    public static Bid BID = new Bid();
    public static Ask ASK = new Ask();

    public abstract int toDirection();

    public Decimal toDecimal() {
        return Decimal.fromDouble(toDirection());
    }

    public static BookSide fromDirection(int direction) {
        return direction < 0 ? ASK : BID;
    }

    public abstract int toIndex();

    public abstract boolean isMoreBehindThan(Decimal limit, Decimal frontRowPrice);




    public static class Ask extends BookSide {
        @Override
        public int toIndex() {
            return 1;
        }
        @Override
        public int toDirection() { return -1; }
        @Override
        public String toString() {return "ASK";}
        @Override
        public boolean isMoreBehindThan(Decimal limit, Decimal frontRowPrice) {
            return limit.isGreaterThan(frontRowPrice);
        }
    } // class Ask

    public static class Bid extends BookSide {
        @Override
        public int toIndex() {
            return 0;
        }
        @Override
        public int toDirection() { return 1; }
        @Override
        public String toString() {return "BID";}
        @Override
        public boolean isMoreBehindThan(Decimal limit, Decimal frontRowPrice) {
            return limit.isLessThan(frontRowPrice);
        }
    } // class Ask

    private static class BookSideNULL extends BookSide {
        @Override
        public int toIndex() {
            throw new RuntimeException("This is BookSideNULL");
        }
        @Override
        public int toDirection() {
            throw new RuntimeException("This is BookSideNULL");
        }
        public boolean isMoreBehindThan(Decimal limit, Decimal frontRowPrice) {
            throw new RuntimeException("This is BookSideNULL");
        }
    }
} // class

