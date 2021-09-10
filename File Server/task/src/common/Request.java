package common;

import java.util.List;

public class Request {
    final public String command;
    final public List<String> parameters;

    public Request(String command, List<String> parameters) {
        this.command = command;
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "Request{" + command + ", " + parameters + '}';
    }
}
