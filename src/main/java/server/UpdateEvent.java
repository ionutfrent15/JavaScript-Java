package server;

import java.io.Serializable;

public class UpdateEvent implements Serializable {
    private String event;
    private Object object;

    public UpdateEvent(String event, Object object) {
        this.event = event;
        this.object = object;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
