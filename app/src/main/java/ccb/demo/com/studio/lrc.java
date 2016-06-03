package ccb.demo.com.studio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

/**
 * Created by godfa on 2016/6/1.
 */
public class lrc extends AppCompatActivity {
    //歌词列表显示
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lrc);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView lrcShowView = (TextView)findViewById(R.id.lrcShowView);
        lrcShowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lrc2main();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            lrc2main();
        }
        return true;
    }


    //返回前台页面
    private void lrc2main(){
        Bundle bundle = getIntent().getExtras();
        Intent lrc_ac1 = new Intent();
        lrc_ac1.putExtras(bundle);
        lrc_ac1.setClass(this, MainActivity.class);
        startActivity(lrc_ac1);
        this.finish();
    }
}
