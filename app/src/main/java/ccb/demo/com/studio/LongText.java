package ccb.demo.com.studio;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by godfa on 2016/3/18.
 */
public class LongText extends TextView {
    public LongText(Context context) {
        super(context);
    }

    public LongText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LongText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
