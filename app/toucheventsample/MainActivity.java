// Ang app na 'to ay para idemo ang multi-touch event.

package balikbayan.box.toucheventsample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private CustomView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = new CustomView(this);
        setContentView(view);
    }
}
