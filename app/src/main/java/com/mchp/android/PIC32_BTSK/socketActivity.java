package com.mchp.android.PIC32_BTSK;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.Socket;

/**
 * Created by pjay on 2015/9/30.
 */
public class socketActivity extends Activity {

    // Return Intent extra
    public static String EXTRA_USER_ID = "user_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);

        Button btn_connection = (Button) findViewById(R.id.button_connection);
        final EditText editText_userID = (EditText) findViewById(R.id.editText_userID);

        btn_connection.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if ( !editText_userID.getText().toString().equals("") ) {
                    // Create the result Intent and include the user ID
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_USER_ID, editText_userID.getText().toString());

                    // Set result and finish this Activity
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Please give me a pairing ID!!",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
}
