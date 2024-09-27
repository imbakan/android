package balikbayan.box.square;

import android.opengl.GLES32;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Model1 {

    private final int[] count;
    private final int[] vao;
    private int location;

    public Model1() {
        count = new int[1];
        vao = new int[1];
    }

    private void generate (int vertex_coordinate, int[] stride, FloatBuffer[] vertex, int[] size1, IntBuffer[] index, int[] size2, int[] count) {
        float x1, y1, x2, y2, z, a, b;
        int k, n, alloc_size;
        float[] vertices;
        int[] indices;
        ByteBuffer bb;

        //      0         1
        //      +---------+          y2 +---------+
        //      |         |             |         |
        //      |         |             b         |
        //      |         |             |         |
        //      +---------+          y1 +----a----+
        //      2         3             x1        x2
        //

        n = 4;
        alloc_size = n * vertex_coordinate;
        vertices = new float[alloc_size];

        stride[0] = vertex_coordinate * Float.BYTES;
        size1[0] = n * stride[0];

        a = 15.0f;
        b = 25.0f;
        x1 = -a;
        x2 =  a;
        y1 = -b;
        y2 =  b;
        z = 0.0f;

        k = 0;

        // 0
        vertices[k++] = x1;
        vertices[k++] = y2;
        vertices[k++] = z;

        // 1
        vertices[k++] = x2;
        vertices[k++] = y2;
        vertices[k++] = z;

        // 2
        vertices[k++] = x1;
        vertices[k++] = y1;
        vertices[k++] = z;

        // 3
        vertices[k++] = x2;
        vertices[k++] = y1;
        vertices[k++] = z;

        bb = ByteBuffer.allocateDirect(size1[0]);
        bb.order(ByteOrder.nativeOrder());
        vertex[0] = bb.asFloatBuffer();
        vertex[0].put(vertices);
        vertex[0].position(0);

        //                0   1
        //                +---+
        //                |\  |
        //                | \ |
        //                |  \|
        //                +---+
        //                2   3

        count[0] = 6;
        indices = new int[count[0]];

        size2[0] = count[0] * Integer.BYTES;

        k = 0;

        indices[k++] = 0;
        indices[k++] = 3;
        indices[k++] = 2;

        indices[k++] = 0;
        indices[k++] = 1;
        indices[k++] = 3;

        bb = ByteBuffer.allocateDirect(size2[0]);
        bb.order(ByteOrder.nativeOrder());
        index[0] = bb.asIntBuffer();
        index[0].put(indices);
        index[0].position(0);
    }

    public void create(int handle) {
        int vertex_coordinate, vertex_offset, vertex_attribute;
        int[] stride = new int[1];
        int[] size1 = new int[1];
        int[] size2 = new int[1];
        FloatBuffer[] vertex;
        IntBuffer[] index;
        int[] vbo = new int[1];
        int[] ebo = new int[1];

        vertex_coordinate = 3; // x, y, z
        vertex_offset = 0;

        vertex_attribute = GLES32.glGetAttribLocation(handle, "v_vertex");
        location = GLES32.glGetUniformLocation(handle, "m_matrix");

        vertex = new FloatBuffer[1];
        index = new IntBuffer[1];

        generate(vertex_coordinate, stride, vertex, size1, index, size2, count);

        GLES32.glGenVertexArrays(1, vao, 0);
        GLES32.glBindVertexArray(vao[0]);

        GLES32.glGenBuffers(1, vbo, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo[0]);
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, size1[0], vertex[0], GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(vertex_attribute, vertex_coordinate, GLES32.GL_FLOAT, false, stride[0], vertex_offset);
        GLES32.glEnableVertexAttribArray(vertex_attribute);

        GLES32.glGenBuffers(1, ebo, 0);
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, ebo[0]);
        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, size2[0], index[0], GLES32.GL_STATIC_DRAW);

        GLES32.glBindVertexArray(0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, 0);

        GLES32.glDeleteBuffers(1, vbo, 0);
        GLES32.glDeleteBuffers(1, ebo, 0);
    }

    public void destroy() {
        GLES32.glDeleteVertexArrays(1, vao, 0);
    }

    public void draw(float[] matrix) {

        GLES32.glUniformMatrix4fv(location, 1, false, matrix, 0);

        GLES32.glBindVertexArray(vao[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, count[0], GLES32.GL_UNSIGNED_INT, 0);
        GLES32.glBindVertexArray(0);
    }
}
