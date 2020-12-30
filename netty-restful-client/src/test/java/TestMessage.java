import com.google.gson.annotations.Expose;

public class TestMessage {

    @Expose
    private String type;

    private String group;
    @Expose
    public String message;

    private String _exception;

    public String get_exception() {
        return _exception;
    }

    public void set_exception(String _exception) {
        this._exception = _exception;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return "TestMessage{" +
                "type='" + type + '\'' +
                ", group='" + group + '\'' +
                ", message='" + message + '\'' +
                ", _exception='" + _exception + '\'' +
                '}';
    }

    public TestMessage() {
    }

    public TestMessage(String type, String group, String message, String _exception) {
        this.type = type;
        this.group = group;
        this.message = message;
        this._exception = _exception;
    }
}
