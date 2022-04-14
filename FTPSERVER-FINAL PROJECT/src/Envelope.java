
import java.io.Serializable;

public class Envelope implements Serializable {
    
    private String id;
    private String args;
    private Object contents;
    
    
    public Envelope(){};

    public Envelope(String id, String args, Object contents) {
        this.id = id;
        this.args = args;
        this.contents = contents;
    }

    public String getId() {
        return id;
    }

    public String getArgs() {
        return args;
    }

    public Object getContents() {
        return contents;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public void setContents(Object contents) {
        this.contents = contents;
    }
    
    
    
}
