package balikbayan.box.triangle2;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class SurfaceView1 extends GLSurfaceView {

    private Renderer1 renderer;

    public SurfaceView1(Context context) {
        super(context);

        setEGLContextClientVersion(3);

        renderer = new Renderer1(context);
        setRenderer(renderer);
    }
}
