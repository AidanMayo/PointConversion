package org.example

fun main() {
    /*
    * Point math is easily rotated by understanding how sine waves work
    *
    * By understanding that on a -180 to 180 degree scale the sine wave will move in a circular motion
    * we can use basic math to determine the movement of a point in 2D space relative to 3D space
    *
    * This is the fundamental component that allows us to convert 3D points with a given rotation to a 2d picture from a camera.
     */

    val point = point(4.65, 23.34, 23.45)


}

class screen() {
    val points = arrayListOf<point>()

    val camera = camera()

    // I want to create a camera that can see within a given FOV
}

// Camera starts
class camera(
    var viewDistance: Double = 1.0,
    var FOV: Double = 90.0,
    var cameraPosition: point = point(1.0, 0.0, 0.0),
    var xRot: Double = 0.0,
    var yRot: Double = 0.0,
    var zRot: Double = 0.0
) {
    // Moves camera along axis
    fun moveCam(x: Double, y: Double, z: Double) {
        this.cameraPosition.transform(x, y, z)
    }

    // Rotates camera around the origin
    // Positive horizontal moves right
    // Positive vertical moves up
    fun encircle(horiRot: Double, vertRot: Double) {
        this.rotate(-horiRot, -vertRot, 0.0)
    }

    // ! Rotation should automatically truncate to a range of -180 to 180 !
    // Positive = right | Negative = left
    private fun rotateX(xRot: Double) {
        this.xRot = (((this.xRot+180) + xRot)%360)-180
    }

    // Positive = up | Negative = down
    private fun rotateY(yRot: Double) {
        this.yRot = (((this.yRot+180) + yRot)%360)-180
    }

    // Positive = clockwise | Negative = counterclockwise
    private fun rotateZ(zRot: Double) {
        this.zRot = (((this.zRot+180) + zRot)%360)-180
    }

    // Same logic as rotation
    fun rotate(x: Double, y: Double, z: Double) {
        this.rotateX(x)
        this.rotateY(y)
        this.rotateZ(z)
    }
}

class point(
    var X: Double,
    var Y: Double,
    var Z: Double
) {
    fun transform(
        X: Double,
        Y: Double,
        Z: Double
    ) {
        this.X += X
        this.Y += Y
        this.Z += Z
    }
}