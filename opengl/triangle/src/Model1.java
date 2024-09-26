package balikbayan.box.triangle2;

import android.opengl.GLES32;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Model1 {

    private final int[] count;
    private final int[] vao;
    private int location;

    public Model1() {
        count = new int[1];
        vao = new int[1];
    }

    private void generate (int vertex_coordinate, int color_coordinate, int[] stride, FloatBuffer[] vertex, int[] size, int[] count) {
        int k, alloc_size;
        float[] vertices;
        ByteBuffer bb;

        count[0] = 3;
        alloc_size = count[0] * (vertex_coordinate + color_coordinate);
        vertices = new float[alloc_size];

        stride[0] = (vertex_coordinate + color_coordinate) * Float.BYTES;
        size[0] = count[0] * stride[0];

        k = 0;

        // 0
        vertices[k++] = 0.03490656732f;      // x
        vertices[k++] = 19.99996953827f;     // y
        vertices[k++] = 0.00000000000f;      // z
        vertices[k++] = 1.0f;                // R
        vertices[k++] = 0.0f;                // G
        vertices[k++] = 0.0f;                // B

        // 1
        vertices[k++] = -17.33793497871f;
        vertices[k++] = -9.96975479508f;
        vertices[k++] = 0.00000000000f;
        vertices[k++] = 0.0f;
        vertices[k++] = 1.0f;
        vertices[k++] = 0.0f;


        // 2
        vertices[k++] = 17.30302841139f;
        vertices[k++] = -10.03021474319f;
        vertices[k++] = 0.00000000000f;
        vertices[k++] = 0.0f;
        vertices[k++] = 0.0f;
        vertices[k++] = 1.0f;


        bb = ByteBuffer.allocateDirect(size[0]);
        bb.order(ByteOrder.nativeOrder());
        vertex[0] = bb.asFloatBuffer();
        vertex[0].put(vertices);
        vertex[0].position(0);
    }

    public void create(int handle) {
        int[] vbo = new int[1];
        FloatBuffer[] vertex = new FloatBuffer[1];
        int vertex_coordinate, color_coordinate, vertex_offset, color_offset, vertex_attribute, color_attribute;
        int[] stride = new int[1];
        int[] size = new int[1];

        vertex_coordinate = 3; // x, y, z
        color_coordinate = 3; // r, g, b

        vertex_offset = 0;
        color_offset = vertex_coordinate * Float.BYTES;;

        vertex_attribute = GLES32.glGetAttribLocation(handle, "v_vertex");
        color_attribute = GLES32.glGetAttribLocation(handle, "v_color");
        location = GLES32.glGetUniformLocation(handle, "m_matrix");

        generate(vertex_coordinate, color_coordinate, stride, vertex, size, count);

        GLES32.glGenVertexArrays(1, vao, 0);
        GLES32.glBindVertexArray(vao[0]);

        GLES32.glGenBuffers(1, vbo, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo[0]);
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, size[0], vertex[0], GLES32.GL_STATIC_DRAW);

        GLES32.glVertexAttribPointer(vertex_attribute, vertex_coordinate, GLES32.GL_FLOAT, false, stride[0], vertex_offset);
        GLES32.glEnableVertexAttribArray(vertex_attribute);

        GLES32.glVertexAttribPointer(color_attribute, color_coordinate, GLES32.GL_FLOAT, false, stride[0], color_offset);
        GLES32.glEnableVertexAttribArray(color_attribute);

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
        GLES32.glBindVertexArray(0);

        GLES32.glDeleteBuffers(1, vbo, 0);
    }

    public void destroy() {
        GLES32.glDeleteVertexArrays(1, vao, 0);
    }

    public void draw(float[] matrix) {

        GLES32.glUniformMatrix4fv(location, 1, false, matrix, 0);

        GLES32.glBindVertexArray(vao[0]);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, count[0]);
        GLES32.glBindVertexArray(0);
    }
}
