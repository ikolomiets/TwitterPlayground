import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CursoredResult {

    @JsonProperty("ids")
    private List<Long> ids;

    @JsonProperty("next_cursor_str")
    private String nextCursor;

    @JsonProperty("previous_cursor_str")
    private String previousCursor;

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public String getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }

    public String getPreviousCursor() {
        return previousCursor;
    }

    public void setPreviousCursor(String previousCursor) {
        this.previousCursor = previousCursor;
    }

}
