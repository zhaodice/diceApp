package org.mirai.zhao.dice;


public class Sentences_them {
    String tag;
    String tag_view;
    Sentences_them(String tag,String tag_view){
        this.tag=tag;
        this.tag_view=tag_view;
    }
    @Override
    public String toString() {
        return tag_view;
    }
}
