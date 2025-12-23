package balikbayan.box.circle1;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class SurfaceView extends GLSurfaceView {

    private balikbayan.box.circle1.Renderer renderer;

    public SurfaceView(Context context) {
        super(context);

        setEGLContextClientVersion(3);

        renderer = new balikbayan.box.circle1.Renderer(context);
        setRenderer(renderer);
    }
}
