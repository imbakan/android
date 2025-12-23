package balikbayan.box.square2;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class SurfaceView extends GLSurfaceView {

    private balikbayan.box.square1.Renderer renderer;

    public SurfaceView(Context context) {
        super(context);

        setEGLContextClientVersion(3);

        renderer = new balikbayan.box.square1.Renderer(context);
        setRenderer(renderer);
    }
}
