package balikbayan.box.triangle3;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class SurfaceView extends GLSurfaceView {

    private balikbayan.box.triangle3.Renderer renderer;

    public SurfaceView(Context context) {
        super(context);

        setEGLContextClientVersion(3);

        renderer = new balikbayan.box.triangle3.Renderer(context);
        setRenderer(renderer);
    }
}
