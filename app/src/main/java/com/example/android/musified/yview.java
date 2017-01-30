package com.example.android.musified;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;


public class yview extends AppCompatActivity{
    EditText youtubeEdit;
    ImageButton youtubeSearch;
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yview);
        youtubeSearch=(ImageButton)findViewById(R.id.searchBtn);
        youtubeEdit=(EditText)findViewById(R.id.youtubeBar);
        assert youtubeSearch!=null;
        youtubeSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str=youtubeEdit.getText().toString();
                char[] myChar=str.toCharArray();
                for(int i=0;i<str.length();i++)
                {
                    if(myChar[i]==' ')
                    {
                        myChar[i]='+';
                    }
                    str=String.valueOf(myChar);
                }
                WebView webView=new WebView(getApplicationContext());
                webView.findViewById(R.id.webView);
                setContentView(webView);
                webView.loadUrl("http://www.youtube.com/results?search_query="+str);
            }
        });
    }
}
