package main.tut06

import com.jogamp.newt.event.KeyEvent
import com.jogamp.opengl.GL.*
import com.jogamp.opengl.GL2ES3.GL_COLOR
import com.jogamp.opengl.GL2ES3.GL_DEPTH
import com.jogamp.opengl.GL3
import glNext.*
import glm.*
import glm.mat.Mat3
import glm.mat.Mat4
import glm.vec._3.Vec3
import glm.vec._4.Vec4
import main.framework.Framework
import main.framework.Semantic
import uno.buffer.*
import uno.glsl.programOf

/**
 * Created by elect on 23/02/17.
 */

fun main(args: Array<String>) {
    Rotations_().setup("Tutorial 06 - Rotations")
}

class Rotations_ : Framework() {

    object Buffer {
        val VERTEX = 0
        val INDEX = 1
        val MAX = 2
    }

    var theProgram = 0
    var modelToCameraMatrixUnif = 0
    var cameraToClipMatrixUnif = 0

    val cameraToClipMatrix = Mat4(0.0f)
    val frustumScale = calcFrustumScale(45.0f)

    fun calcFrustumScale(fovDeg: Float) =  1.0f / glm.tan(fovDeg.rad / 2.0f)

    val bufferObject = intBufferBig(Buffer.MAX)
    val vao = intBufferBig(1)
    val numberOfVertices = 8

    val GREEN_COLOR = floatArrayOf(0.0f, 1.0f, 0.0f, 1.0f)
    val BLUE_COLOR = floatArrayOf(0.0f, 0.0f, 1.0f, 1.0f)
    val RED_COLOR = floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f)
    val BROWN_COLOR = floatArrayOf(0.5f, 0.5f, 0.0f, 1.0f)

    val vertexData = floatBufferOf(

            +1.0f, +1.0f, +1.0f,
            -1.0f, -1.0f, +1.0f,
            -1.0f, +1.0f, -1.0f,
            +1.0f, -1.0f, -1.0f,

            -1.0f, -1.0f, -1.0f,
            +1.0f, +1.0f, -1.0f,
            +1.0f, -1.0f, +1.0f,
            -1.0f, +1.0f, +1.0f,


            *GREEN_COLOR,
            *BLUE_COLOR,
            *RED_COLOR,
            *BROWN_COLOR,

            *GREEN_COLOR,
            *BLUE_COLOR,
            *RED_COLOR,
            *BROWN_COLOR)

    val indexData = shortBufferOf(

            0, 1, 2,
            1, 0, 3,
            2, 3, 0,
            3, 2, 1,

            5, 4, 6,
            4, 5, 7,
            7, 6, 4,
            6, 7, 5)

    private val instanceList = arrayOf(
            Instance(this::nullRotation, Vec3(0.0f, 0.0f, -25.0f)),
            Instance(this::rotateX, Vec3(-5.0f, -5.0f, -25.0f)),
            Instance(this::rotateY, Vec3(-5.0f, +5.0f, -25.0f)),
            Instance(this::rotateZ, Vec3(+5.0f, +5.0f, -25.0f)),
            Instance(this::rotateAxis, Vec3(5.0f, -5.0f, -25.0f)))

    var start = 0L

    override fun init(gl: GL3) = with(gl) {

        initializeProgram(gl)
        initializeVertexBuffers(gl)

        glGenVertexArray(vao)
        glBindVertexArray(vao)

        val colorDataOffset = Vec3.SIZE * numberOfVertices
        glBindBuffer(GL_ARRAY_BUFFER, bufferObject[Buffer.VERTEX])
        glEnableVertexAttribArray(glf.pos3_col4)
        glEnableVertexAttribArray(glf.pos3_col4[1])
        glVertexAttribPointer(glf.pos3_col4, 0)
        glVertexAttribPointer(glf.pos3_col4[1], colorDataOffset)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferObject[Buffer.INDEX])

        glBindVertexArray()

        glEnable(GL_CULL_FACE)
        glCullFace(GL_BACK)
        glFrontFace(GL_CW)

        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDepthFunc(GL_LEQUAL)
        glDepthRange(0.0, 1.0)

        start = System.currentTimeMillis()
    }

    fun initializeProgram(gl: GL3) = with(gl) {

        theProgram = programOf(gl, javaClass, "tut06", "pos-color-local-transform.vert", "color-passthrough.frag")

        modelToCameraMatrixUnif = glGetUniformLocation(theProgram, "modelToCameraMatrix")
        cameraToClipMatrixUnif = glGetUniformLocation(theProgram, "cameraToClipMatrix")

        val zNear = 1.0f
        val zFar = 61.0f

        cameraToClipMatrix[0].x = frustumScale
        cameraToClipMatrix[1].y = frustumScale
        cameraToClipMatrix[2].z = (zFar + zNear) / (zNear - zFar)
        cameraToClipMatrix[2].w = -1.0f
        cameraToClipMatrix[3].z = 2f * zFar * zNear / (zNear - zFar)

        glUseProgram(theProgram)
        glUniformMatrix4f(cameraToClipMatrixUnif, cameraToClipMatrix)
        glUseProgram()
    }

    fun initializeVertexBuffers(gl: GL3) = with(gl) {

        glGenBuffers(bufferObject)

        glBindBuffer(GL_ARRAY_BUFFER, bufferObject[Buffer.VERTEX])
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW)
        glBindBuffer(GL_ARRAY_BUFFER)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferObject[Buffer.INDEX])
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexData, GL_STATIC_DRAW)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER)
    }

    override fun display(gl: GL3) = with(gl) {

        glClearBufferf(GL_COLOR, 0)
        glClearBufferf(GL_DEPTH)

        glUseProgram(theProgram)

        glBindVertexArray(vao)

        val elapsedTime = (System.currentTimeMillis() - start) / 1_000f
        instanceList.forEach {

            val transformMatrix = it.constructMatrix(elapsedTime)

            glUniformMatrix4f(modelToCameraMatrixUnif, transformMatrix)
            glDrawElements(indexData.size, GL_UNSIGNED_SHORT)
        }

        glBindVertexArray()
        glUseProgram()
    }

    override fun reshape(gl: GL3, w: Int, h: Int) = with(gl) {

        cameraToClipMatrix.a0 = frustumScale * (h / w.f)
        cameraToClipMatrix.b1 = frustumScale

        glUseProgram(theProgram)
        glUniformMatrix4f(cameraToClipMatrixUnif, cameraToClipMatrix)
        glUseProgram()

        glViewport(w, h)
    }

    override fun end(gl: GL3) = with(gl) {

        glDeleteProgram(theProgram)
        glDeleteBuffers(bufferObject)
        glDeleteVertexArray(vao)

        destroyBuffers(vao, bufferObject, vertexData, indexData)
    }

    override fun keyPressed(keyEvent: KeyEvent) {

        when (keyEvent.keyCode) {
            KeyEvent.VK_ESCAPE -> quit()
        }
    }

    private class Instance(val calcRotation: (Float) -> Mat3, val offset: Vec3) {

        fun constructMatrix(elapsedTime: Float): Mat4 {

            val rotMatrix = calcRotation(elapsedTime)
            val theMat = Mat4(rotMatrix)
            theMat[3] = Vec4(offset, 1f)

            return theMat
        }
    }

    fun nullRotation(elapsedTime: Float) = Mat3(1f)

    fun rotateX(elapsedTime: Float): Mat3 {

        val angRad = computeAngleRad(elapsedTime, 3f)
        val cos = angRad.cos
        val sin = angRad.sin

        val theMat = Mat3(1f)
        theMat[1].y = cos; theMat[2].y = -sin
        theMat[1].z = sin; theMat[2].z = cos
        return theMat
    }

    fun rotateY(elapsedTime: Float): Mat3 {

        val angRad = computeAngleRad(elapsedTime, 2f)
        val cos = angRad.cos
        val sin = angRad.sin

        val theMat = Mat3(1f)
        theMat[0].x = cos; theMat[2].x = sin
        theMat[0].z = -sin; theMat[2].z = cos
        return theMat
    }

    fun rotateZ(elapsedTime: Float): Mat3 {

        val angRad = computeAngleRad(elapsedTime, 2f)
        val cos = angRad.cos
        val sin = angRad.sin

        val theMat = Mat3(1f)
        theMat[0].x = cos; theMat[1].x = -sin
        theMat[0].y = sin; theMat[1].y = cos
        return theMat
    }

    fun rotateAxis(elapsedTime: Float): Mat3 {

        val angRad = computeAngleRad(elapsedTime, 2f)
        val cos = angRad.cos
        val invCos = 1f - cos
        val sin = angRad.sin

        val axis = Vec3(1f).normalize_()

        val theMat = Mat3(1f)

        theMat[0].x = axis.x * axis.x + (1 - axis.x * axis.x) * cos
        theMat[1].x = axis.x * axis.y * invCos - axis.z * sin
        theMat[2].x = axis.x * axis.z * invCos + axis.y * sin

        theMat[0].y = axis.x * axis.y * invCos + axis.z * sin
        theMat[1].y = axis.y * axis.y + (1 - axis.y * axis.y) * cos
        theMat[2].y = axis.y * axis.z * invCos - axis.x * sin

        theMat[0].z = axis.x * axis.z * invCos - axis.y * sin
        theMat[1].z = axis.y * axis.z * invCos + axis.x * sin
        theMat[2].z = axis.z * axis.z + (1 - axis.z * axis.z) * cos

        return theMat
    }


    fun computeAngleRad(elapsedTime: Float, loopDuration: Float): Float {
        val scale = glm.PIf * 2.0f / loopDuration
        val currentTimeThroughLoop = elapsedTime % loopDuration
        return currentTimeThroughLoop * scale
    }
}
