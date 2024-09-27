package balikbayan.box.square;

import android.content.Context;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Renderer1 implements GLSurfaceView.Renderer {

    private float[] matrix;
    private Shader1 shader;
    private Model1 model;

    public Renderer1(Context context) {

        matrix = new float[16];

        shader = new Shader1(context);
        model = new Model1();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        shader.create(R.raw.vertex, R.raw.fragment);
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
