package org.example

import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

fun main() {
    /*
    * Point math is easily rotated by understanding how sine waves work
    *
    * By understanding that on a -180 to 180 degree scale the sine wave will move in a circular motion
    * we can use basic math to determine the movement of a point in 2D space relative to 3D space
    *
    * This is the fundamental component that allows us to convert 3D points with a given rotation to a 2d picture from a camera.
     */



    println(atan2(0.0, 1.0)) // pi/4
}

fun degreeToRad(degree: Double): Double {
    return degree * (Math.PI/180)
}

class screen() {
    val points = arrayListOf<threeDimPoint>()

    val camera = camera()

    // I want to create a camera that can see within a given FOV

    fun snapshot(): ArrayList<twoDimPoint> {
        val displayedPoints = arrayListOf<twoDimPoint>()

        for (point in points) {
            if (camera.viewDistance < camera.cameraPosition.calculateDistance(point)) {
                continue
            }

            // In FOV Check


            // Place point in 2D
            displayedPoints.add(twoDimPoint(0.0, 0.0) /* Write code to change to 2d spot */)
        }

        return displayedPoints
    }
}

// Camera starts
class camera(
    var viewDistance: Double = 1.0, // View Distance of camera | Works as a radius from the camera
    var cameraPosition: threeDimPoint = threeDimPoint(1.0, 0.0, 0.0), // Camera Position
    var xRot: Double = 0.0, // Camera X Rotation (Left-Right)
    var yRot: Double = 0.0, // Camera Y Rotation (Up-Down)
    var zRot: Double = 0.0 // Camera Z Rotation (Clock-style)
) {
    // FOV Must be between 30.0 and 160.0
    var FOV: Double = 90.0
        set(value) { field = value.coerceIn(30.0, 160.0) }

    // Increase FOV by a given amount
    // Caps at 160 FOV
    fun increaseFOV(FOVIncrease: Double) {
        FOV += FOVIncrease
    }

    // Decrease FOV by a given amount
    // Caps at 30 FOV
    fun decreaseFOV(FOVDecrease: Double) {
        FOV -= FOVDecrease
    }

    // Moves camera along axis
    fun moveCam(x: Double, y: Double, z: Double) {
        this.cameraPosition.transform(x, y, z)
    }

    // Rotates camera around the origin
    // Positive horizontal moves right
    // Positive vertical moves up
    fun encircle(horiRot: Double, vertRot: Double) {
        this.rotate(-horiRot, -vertRot, 0.0)

        // horizontal
        val horRad = sqrt(cameraPosition.X.pow(2) + cameraPosition.Z.pow(2))
        val horRot = atan2(cameraPosition.X, cameraPosition.Z)


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

class twoDimPoint(
    var x: Double,
    var y: Double
) {
    fun transform(x: Double, y: Double) {
        this.x += x
        this.y += y
    }

    fun calculateDistance(other: twoDimPoint) : Double {
        return sqrt((other.x - this.x).pow(2) + (other.y - this.y).pow(2))
    }
}

class threeDimPoint(
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

    fun calculateDistance(other: threeDimPoint) : Double {
        return sqrt((other.X - this.X).pow(2.0) + (other.Y - this.Y).pow(2.0) + (other.Z - this.Z).pow(2.0))
    }
}