package balikbayan.box.square2;

import android.content.Context;
import android.opengl.GLES32;
import android.util.Log;

public class Shader {

    private Context context;
    private int handle;

    public Shader(Context context) {
        this.context = context;
    }

    public  void create(String source1, String source2) {
        int shader1, shader2;
        int[] result = new int[1];

        shader1 = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);
        GLES32.glShaderSource(shader1, source1);
        GLES32.glCompileShader(shader1);

        GLES32.glGetShaderiv(shader1, GLES32.GL_COMPILE_STATUS, result, 0);
        if(result[0] == 0) {
            Log.d("KLGYN", "Vertex Shader");
            Log.d("KLGYN", GLES32.glGetShaderInfoLog(shader1));
        }

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
}
