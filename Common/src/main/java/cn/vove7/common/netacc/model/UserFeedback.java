package cn.vove7.common.netacc.model;

/**
 * Created by Administrator on 9/28/2018
 */
public class UserFeedback {

    private String title;
    private String content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UserFeedback(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
