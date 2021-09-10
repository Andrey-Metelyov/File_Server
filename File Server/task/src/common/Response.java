package common;

public class Response {
    final public int id;
    final public String content;

    public Response(int id) {
        this(id, "");
    }

    public Response(int id, String content) {
        this.id = id;
        this.content = content;
    }

    @Override
    public String toString() {
        return "Response{" + id + ", " + content + '}';
    }
}
