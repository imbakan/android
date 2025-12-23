package balikbayan.box.square2;

import android.opengl.GLES32;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

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
        IntBuffer index;
        ByteBuffer bb;
        int[] vbo = new int[1];
        int[] ibo = new int[1];
        float[] vertices;
        int[] indices;
        float x1, y1, x2, y2;
        int k, vertex_size, vertex_offset, location2, stride, size1, size2, alloc_size, count1;

        vertex_size = 2; // x, y
        stride = vertex_size * Float.BYTES;
        vertex_offset = 0;

        count1 = 4;
        size1 = count1 * stride;
        alloc_size = count1 * vertex_size;
        vertices = new float[alloc_size];

        //      0         1
        //      +---------+          y2 +---------+
        //      |         |             |         |
        //      |         |             |         |
        //      |         |             |         |
        //      +---------+          y1 +---------+
        //      2         3             x1        x2
        //

        x1 = -10.0f;
        x2 =  -x1;
        y1 = -15.0f;
        y2 =  -y1;

        k = 0;

        // 0
        vertices[k++] = x1;
        vertices[k++] = y2;

        // 1
        vertices[k++] = x2;
        vertices[k++] = y2;

        // 2
        vertices[k++] = x1;
        vertices[k++] = y1;

        // 3
        vertices[k++] = x2;
        vertices[k++] = y1;

        bb = ByteBuffer.allocateDirect(size1);
        bb.order(ByteOrder.nativeOrder());
        vertex = bb.asFloatBuffer();
        vertex.put(vertices);
        vertex.position(0);

        //                0   1
        //                +---+
        //                |\  |
        //                | \ |
        //                |  \|
        //                +---+
        //                2   3

        count = 6;
        size2 = count * Integer.BYTES;
        indices = new int[count];

        k = 0;

        indices[k++] = 0;
        indices[k++] = 3;
        indices[k++] = 2;

        indices[k++] = 0;
        indices[k++] = 1;
        indices[k] = 3;

        bb = ByteBuffer.allocateDirect(size2);
        bb.order(ByteOrder.nativeOrder());
        index = bb.asIntBuffer();
        index.put(indices);
        index.position(0);

        location2 = GLES32.glGetAttribLocation(handle, "v_vertex");
        location1 = GLES32.glGetUniformLocation(handle, "m_matrix");

        GLES32.glGenVertexArrays(1, vao, 0);
        GLES32.glBindVertexArray(vao[0]);

        GLES32.glGenBuffers(1, vbo, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo[0]);
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, size1, vertex, GLES32.GL_STATIC_DRAW);

        GLES32.glVertexAttribPointer(location2, vertex_size, GLES32.GL_FLOAT, false, stride, vertex_offset);
        GLES32.glEnableVertexAttribArray(location2);

        GLES32.glGenBuffers(1, ibo, 0);
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, size2, index, GLES32.GL_STATIC_DRAW);

        GLES32.glBindVertexArray(0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, 0);

        GLES32.glDeleteBuffers(1, vbo, 0);
        GLES32.glDeleteBuffers(1, ibo, 0);
    }

    public void destroy() {
        GLES32.glDeleteVertexArrays(1, vao, 0);
    }

    public void draw(float[] matrix) {

        GLES32.glUniformMatrix4fv(location1, 1, false, matrix, 0);

        GLES32.glBindVertexArray(vao[0]);
        GLES32.glDrawElements(GLES32.GL_LINE_LOOP, count, GLES32.GL_UNSIGNED_INT, 0);
        GLES32.glBindVertexArray(0);
    }
}
