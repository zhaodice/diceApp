package org.mirai.zhao.dice;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class ContentInputer extends Dialog {
    Button button_save,button_cancel;
    TextView textView_notice;
    EditText editText_content;

    Context context;
    String notice;
    String content;
    OnContentInputerClosedListener onContentInputerClosedListener;
    interface OnContentInputerClosedListener{
        void onSave(String content);
        void onCancel();
    }
    static void OpenDialog(final Context context,String notice,String content,OnContentInputerClosedListener onContentInputerClosedListener){
        ContentInputer dialog=new ContentInputer(context,notice,content,onContentInputerClosedListener);
        dialog.show();
    }
    private ContentInputer(@NonNull Context context,String notice,String content,OnContentInputerClosedListener onContentInputerClosedListener) {
        super(context, android.R.style.Theme_Material_Light_Dialog);
        this.context=context;
        this.notice=notice;
        this.content=content;
        this.onContentInputerClosedListener=onContentInputerClosedListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.content_inputer);
        textView_notice=findViewById(R.id.content_inputer_notice);
        button_save=findViewById(R.id.content_inputer_save);
        button_cancel=findViewById(R.id.content_inputer_cancel);
        editText_content=findViewById(R.id.content_inputer_edit);
        textView_notice.setText(notice);
        editText_content.setText(content);
        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onContentInputerClosedListener.onSave(editText_content.getText().toString());
                ContentInputer.this.dismiss();
            }
        });
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onContentInputerClosedListener.onCancel();
                ContentInputer.this.dismiss();
            }
        });
        super.onCreate(savedInstanceState);
    }
}
