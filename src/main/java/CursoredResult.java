import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CursoredResult {

    @JsonProperty("ids")
    private List<Long> ids;

    @JsonProperty("next_cursor")
    private Long nextCursor;

    @JsonProperty("next_cursor_str")
    private String nextCursorStr;

    @JsonProperty("previous_cursor")
    private Long previousCursor;

    @JsonProperty("previous_cursor_str")
    private String previousCursorStr;

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public Long getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(Long nextCursor) {
        this.nextCursor = nextCursor;
    }

    public Long getPreviousCursor() {
        return previousCursor;
    }

    public void setPreviousCursor(Long previousCursor) {
        this.previousCursor = previousCursor;
    }

    public String getNextCursorStr() {
        return nextCursorStr;
    }

    public boolean isLast() {
        return nextCursor != null && nextCursor == 0;
    }

    public void setNextCursorStr(String nextCursorStr) {
        this.nextCursorStr = nextCursorStr;
    }

    public String getPreviousCursorStr() {
        return previousCursorStr;
    }

    public void setPreviousCursorStr(String previousCursorStr) {
        this.previousCursorStr = previousCursorStr;
    }

}
