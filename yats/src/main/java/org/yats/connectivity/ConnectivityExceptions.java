package org.yats.connectivity;

/**
 * Created
 * Date: 08/04/15
 * Time: 22:19
 */

public class ConnectivityExceptions {

    public static class ConnectionException extends RuntimeException {
        public ConnectionException(String msg) {
            super(msg);
        }
    }

    public static class UnexpectedExternalInputException extends RuntimeException {
        public UnexpectedExternalInputException(String msg) {
            super(msg);
        }
    }

    public static class UnknownIdException extends RuntimeException {
        public UnknownIdException(String msg) {
            super(msg);
        }
    }

}
