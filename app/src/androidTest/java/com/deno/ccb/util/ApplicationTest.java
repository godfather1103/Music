package com.deno.ccb.util;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

/**
 * Created by ccb on 16-6-22.
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testAdd(){
        Log.i("ApplicationTest","ApplicationTest1111");
        assertEquals(4,2+2);
    }

    public void add(){
        Log.i("ApplicationTest","ApplicationTest2333");
        assertEquals(4,2+2);
    }
}
