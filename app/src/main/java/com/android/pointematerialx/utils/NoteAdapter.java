package com.android.pointematerialx.utils;

public class NoteAdapter {
    private String title;
    private String content;

    public NoteAdapter(){}

    public NoteAdapter(String title, String content){
        this.title = title;
        this.content = content;
    }


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
}
