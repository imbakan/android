package balikbayan.box.circle2;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class SurfaceView extends GLSurfaceView {

    private balikbayan.box.circle2.Renderer renderer;

    public SurfaceView(Context context) {
        super(context);

        setEGLContextClientVersion(3);

        renderer = new balikbayan.box.circle2.Renderer(context);
        setRenderer(renderer);
    }
}
