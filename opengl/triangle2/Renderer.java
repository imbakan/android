package balikbayan.box.triangle2;

import android.content.Context;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Renderer  implements GLSurfaceView.Renderer {

    private float[] matrix;
    private Shader shader;
    private Model model;

    public Renderer(Context context) {

        matrix = new float[16];

        shader = new Shader(context);
        model = new Model();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        String source1, source2;

        source1 = "#version 320 es\n" +

                "precision mediump float;" +

                "in vec2 v_vertex;" +
                "in vec3 v_color;" +

                "out vec3 color;" +

                "uniform mat4 m_matrix;" +

                "void main()" +
                "{" +
                "gl_Position =  m_matrix * vec4(vec3(v_vertex, 0.0), 1.0);" +
                "color = v_color;" +
                "}";

        source2 = "#version 320 es\n" +

                "precision mediump float;" +

                "in vec3 color;" +

                "out vec4 FragColor;" +

                "void main()" +
                "{" +
                "FragColor = vec4(color, 1.0);" +
                "}";

        shader.create(source1, source2);
        shader.use();

        model.create(shader.getHandle());

        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        float left, right, bottom, top, near, far;
        float w, h;

        w = 50.0f;
        h = w * ((float) i1 / (float) i);

        left = -w / 2.0f;
        right = left + w;
        bottom = -h / 2.0f;
        top = bottom + h;
        near = 0.0f;
        far = 1.0f;

        Matrix.orthoM(matrix, 0, left, right, bottom, top, near, far);
        GLES32.glViewport(0, 0, i, i1);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT);
        model.draw(matrix);
    }
}
