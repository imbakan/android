package balikbayan.box.triangle2;

import android.content.Context;
import android.opengl.GLES32;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class Shader1 {

    private Context context;
    private int handle;

    public Shader1(Context context) {
        this.context = context;
    }

    public  void create(int id1, int id2) {
        int shader1, shader2;
        String source1, source2;
        int[] result;

        source1 = getSource(id1);
        shader1 = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);
        GLES32.glShaderSource(shader1, source1);
        GLES32.glCompileShader(shader1);

        result = new int[1];
        GLES32.glGetShaderiv(shader1, GLES32.GL_COMPILE_STATUS, result, 0);
        if(result[0] == 0) {
            Log.d("KLGYN", "Vertex Shader");
            Log.d("KLGYN", GLES32.glGetShaderInfoLog(shader1));
        }

        source2 = getSource(id2);
        shader2 = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);
        GLES32.glShaderSource(shader2, source2);
        GLES32.glCompileShader(shader2);

        GLES32.glGetShaderiv(shader2, GLES32.GL_COMPILE_STATUS, result, 0);
        if(result[0] == 0) {
            Log.d("KLGYN", "Fragment Shader");
            Log.d("KLGYN", GLES32.glGetShaderInfoLog(shader2));
        }

        handle = GLES32.glCreateProgram();
        GLES32.glAttachShader(handle, shader1);
        GLES32.glAttachShader(handle, shader2);
        GLES32.glLinkProgram(handle);

        GLES32.glGetShaderiv(shader2, GLES32.GL_LINK_STATUS, result, 0);
        if(result[0] == 0) {
            Log.d("KLGYN", "Program Shader");
            Log.d("KLGYN", GLES32.glGetShaderInfoLog(shader1));
        }
    }

    public int getHandle() {
        return handle;
    }

    public void use() {
        GLES32.glUseProgram(handle);
    }

    private String getSource(int id) {
        InputStream is;
        String str = null;
        byte[] b;
        int n;

        try {

            is = context.getResources().openRawResource(id);
            n = is.available();
            b = new byte[n];
            is.read(b);
            is.close();

            str = new String(b);

        } catch (IOException e) {
            str = null;
        }

        return str;
    }
}
