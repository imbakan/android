package balikbayan.box.triangle3;

import android.opengl.GLES32;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Model {

    private int count;
    private final int[] vao;
    private int location1;

    public Model() {
        count = 0;
        vao = new int[1];
    }

    public void create(int handle) {
        FloatBuffer vertex;
        ByteBuffer bb;
        int[] vbo = new int[1];
        int k, vertex_size, vertex_offset, location2, stride, size, alloc_size;
        float[] vertices;

        vertex_size = 2; // x, y
        stride = vertex_size * Float.BYTES;
        vertex_offset = 0;

        count = 3;
        size = count * stride;
        alloc_size = count * vertex_size;
        vertices = new float[alloc_size];

        k = 0;

        // 0
        vertices[k++] = 0.03490656732f;      // x
        vertices[k++] = 19.99996953827f;     // y

        // 1
        vertices[k++] = -17.33793497871f;
        vertices[k++] = -9.96975479508f;

        // 2
        vertices[k++] = 17.30302841139f;
        vertices[k] = -10.03021474319f;

        bb = ByteBuffer.allocateDirect(size);
        bb.order(ByteOrder.nativeOrder());
        vertex = bb.asFloatBuffer();
        vertex.put(vertices);
        vertex.position(0);

        location2 = GLES32.glGetAttribLocation(handle, "v_vertex");
        location1 = GLES32.glGetUniformLocation(handle, "m_matrix");

        GLES32.glGenVertexArrays(1, vao, 0);
        GLES32.glBindVertexArray(vao[0]);

        GLES32.glGenBuffers(1, vbo, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo[0]);
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, size, vertex, GLES32.GL_STATIC_DRAW);

        GLES32.glVertexAttribPointer(location2, vertex_size, GLES32.GL_FLOAT, false, stride, vertex_offset);
        GLES32.glEnableVertexAttribArray(location2);

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
        GLES32.glBindVertexArray(0);

        GLES32.glDeleteBuffers(1, vbo, 0);
    }

    public void destroy() {
        GLES32.glDeleteVertexArrays(1, vao, 0);
    }

    public void draw(float[] matrix) {

        GLES32.glUniformMatrix4fv(location1, 1, false, matrix, 0);

        GLES32.glBindVertexArray(vao[0]);
        GLES32.glDrawArrays(GLES32.GL_LINE_LOOP, 0, count);
        GLES32.glBindVertexArray(0);
    }
}
