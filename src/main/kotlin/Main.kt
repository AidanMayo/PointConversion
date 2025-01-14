package org.xephyrous

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.*
import kotlin.random.Random

fun main() {
    val testScreen = screen()

    testScreen.camera.rotate(45.0,16.0,12.0)
    testScreen.camera.moveCam(-6.0,-2.0,2.0)
    testScreen.camera.viewDistance = 20.0
    testScreen.resize(25.0, 25.0)

    println(testScreen.camera)

    for (i in 1..100) {
        testScreen.createRandomPoint(-10.0, 10.0)
    }

    var points = testScreen.screenshot()
    points.exportSnapshot("test1")

    testScreen.camera.rotate(0.0, 0.0, 180.0)
    points = testScreen.screenshot()
    points.exportSnapshot("test2")
}

fun degreeToRad(degree: Double): Double {
    return degree * (Math.PI/180)
}

fun radToDegree(rad: Double): Double {
    return rad * (180.0 / Math.PI)
}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

class snapshot(
    xSize: Double,
    ySize: Double,
) {
    var xSize = xSize
        set(value) {
            field = value.coerceAtLeast(20.0)
        }
    var ySize = ySize
        set(value) {
            field = value.coerceAtLeast(20.0)
        }

    val points = arrayListOf<twoDimPoint>()

    fun addPoint(x: Double, y: Double) {
        points.add(twoDimPoint(x, y))
    }

    fun resize(xSize: Double, ySize: Double) {
        this.xSize = xSize
        this.ySize = ySize
    }

    fun exportSnapshot(
        name: String
    ) {
        try {
            val width = (xSize*100).roundToInt()
            val height = (ySize*100).roundToInt()

            val buffImg = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

            val g2d = buffImg.createGraphics()
            g2d.color = Color.BLACK
            g2d.fillRect(0, 0, width, height)

            g2d.color = Color.GRAY
            g2d.fillOval(0, 0, width, height)

            g2d.color = Color.BLUE
            g2d.fillOval(2, 2, width-4, height-4)

            g2d.color = Color.WHITE

            for (point in points) {
                val xLoc = ((point.x+(xSize/2))*100).roundToInt()
                val yLoc = ((point.y+(ySize/2))*100).roundToInt()

                g2d.fillOval(xLoc-4, yLoc-4, 8, 8)
            }

            g2d.dispose()

            val outputFile = File("$name.png")
            ImageIO.write(buffImg, "png", outputFile)

            println("Exported To $name.png")

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

class screen() {
    val points = arrayListOf<threeDimPoint>()
    val camera = camera()

    val screenshots = arrayListOf<snapshot>()

    val camX: Double
        get() = this.camera.cameraPosition.X
    val camY: Double
        get() = this.camera.cameraPosition.Y
    val camZ: Double
        get() = this.camera.cameraPosition.Z
    val camRotX: Double
        get() = this.camera.xRot
    val camRotY: Double
        get() = this.camera.yRot
    val camRotZ: Double
        get() = this.camera.zRot

    // Relative Screen size, stretches the square camera to the given screen size
    // Camera detects points in a circular area, no points should be found in the corners
    // Screen space made from [-size/2, size/2]
    // Min size: 5x5
    // Default size: 50x50
    var xSize = 50.0
        set(value) {
            field = value.coerceAtLeast(5.0)
        }
    var ySize = 50.0
        set(value) {
            field = value.coerceAtLeast(5.0)
        }

    fun resize(xSize: Double, ySize: Double) {
        this.xSize = xSize
        this.ySize = ySize
    }

    // Adds a given point
    fun createPoint(
        X: Double,
        Y: Double,
        Z: Double
    ) {
        points.add(threeDimPoint(X, Y, Z))
    }

    // Adds a random point within a given range
    fun createRandomPoint(
        low: Double,
        high: Double
    ) {
        points.add(threeDimPoint(Random.nextDouble(low, high), Random.nextDouble(low, high), Random.nextDouble(low, high)))
    }

    // I want to create a camera that can see within a given FOV
    // All points should lie within a circle, as the camera sees in a circle
    fun screenshot(): snapshot {
        val snap = snapshot(xSize, ySize)

        for (point in points) {
            // Create a temporary point in space relative to the camera
            // This point in space will imagine the camera is facing upwards (Y Facing)
            val relativePoint = threeDimPoint(point.X-camX, point.Y-camY, camZ-point.Z)

            val rad = relativePoint.calcOriginDist()
            if (camera.viewDistance < rad) { continue }

            relativePoint.encircle(-camRotX, -camRotY)

            // Use spherical coords to figure it out!
            val pointPhi = radToDegree(acos(relativePoint.Z/rad)).round(14)
            if (pointPhi > (camera.FOV)/2) { continue }

            // Place point in 2D
            // Method: find point in arbitrary space
            // stretch screen to be display size

            // can grab point off of a modified phi to fov and theta into polar coordinates.
            // convert that to rectangular and you get the answer

            //-x, y is our model point

            var thetaPoint = radToDegree(atan2(relativePoint.Y, relativePoint.X)).round(14)

            // Rotate point based on camera rotation
            thetaPoint = (((thetaPoint+180) + camRotZ)%360)-180

            // pointPhi/(camera.FOV/2)*cos(thetaPoint), pointPhi/(camera.FOV/2)*sin(thetaPoint)
            // cartesian with respect to [-1,1] [-1,1]

            snap.addPoint(
                ((pointPhi/(camera.FOV/2)*cos(degreeToRad(thetaPoint)))*(xSize/2)).round(14), // Scale points to screen
                ((pointPhi/(camera.FOV/2)*sin(degreeToRad(thetaPoint)))*(ySize/2)).round(14)
            )
        }

        screenshots.add(snap)
        return snap
    }

    fun findVisiblePoints() : ArrayList<threeDimPoint> {
        val displayedPoints = arrayListOf<threeDimPoint>()

        for (point in points) {
            val relativePoint = threeDimPoint(point.X-camX, point.Y-camY, camZ-point.Z)

            val rad = relativePoint.calcOriginDist()
            if (camera.viewDistance < rad) { continue }

            relativePoint.encircle(-camRotX, -camRotY)

            val pointPhi = radToDegree(acos(relativePoint.Z/rad)).round(14)
            if (pointPhi > (camera.FOV)/2) { continue }

            // Place point in 2D
            displayedPoints.add(point) /* Write code to change to 2d spot */
        }

        return displayedPoints
    }
}

// Camera starts
class camera(
    var viewDistance: Double = 10.0, // View Distance of camera | Works as a radius from the camera
    var cameraPosition: threeDimPoint = threeDimPoint(0.0, 0.0, 1.0), // Camera Position
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
        this.cameraPosition.encircle(horiRot, vertRot)
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

    override fun toString(): String {
        return """
            -=-=-=-=-=-=-=-=-=-=-
            Camera Info:
              | Camera Located at: $cameraPosition
              | Camera Facing: $xRot, $yRot, $zRot
              | Current FOV: $FOV
              | Current ViewDistance : $viewDistance
            -=-=-=-=-=-=-=-=-=-=-
        """.trimIndent()
    }
}

class twoDimPoint(
    var x: Double,
    var y: Double
) {
    override fun toString(): String {
        return "X:${x.round(5)}, Y:${y.round(5)}"
    }

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
    override fun toString() : String {
        return "X:${X.round(5)}, Y:${Y.round(5)}, Z:${Z.round(5)}"
    }

    fun transform(
        X: Double,
        Y: Double,
        Z: Double
    ) {
        this.X += X
        this.Y += Y
        this.Z += Z
    }

    fun setCoordinates(
        X: Double,
        Y: Double,
        Z: Double
    ) {
        this.X = X
        this.Y = Y
        this.Z = Z
    }

    // Rotates point around the origin
    // Positive horizontal moves right
    // Positive vertical moves up
    fun encircle(horiRot: Double, vertRot: Double) {
        // Make rotation values only one rotation cycle
        val fixedHor = horiRot%360
        val fixedVer = vertRot%360

        // Conversion to spherical coordinates

        // horizontal
        val radius = sqrt(this.X.pow(2) + this.Y.pow(2) + this.Z.pow(2))
        var thetaRot = radToDegree(atan2(this.X, this.Z))
        var phiRot = radToDegree(acos(this.Y/radius))

        thetaRot = ((((thetaRot+180) + fixedHor)%360)-180).round(14)

        // if phi goes to 0 we have to flip the theta rot by 180/ this is difficult
        // its not actually difficult ur just a bitch

        val delta = phiRot - fixedVer
        phiRot = when {
            delta < 0 && delta > -180 -> {
                thetaRot *= -1
                (fixedVer - phiRot).round(14)
            }
            delta >= 0 -> delta.round(15)
            else -> (phiRot + (360 - fixedVer)).round(14)
        }

        this.setCoordinates(
            (radius*sin(degreeToRad(thetaRot))*sin(degreeToRad(phiRot))).round(14),
            (radius*cos(degreeToRad(phiRot))).round(14),
            (radius*sin(degreeToRad(phiRot))*cos(degreeToRad(thetaRot))).round(14)
        )
    }

    fun calcOriginDist() : Double {
        return sqrt((this.X).pow(2) + (this.Y).pow(2) + (this.Z).pow(2))
    }

    fun calculateDistance(other: threeDimPoint) : Double {
        return sqrt((other.X - this.X).pow(2.0) + (other.Y - this.Y).pow(2.0) + (other.Z - this.Z).pow(2.0))
    }
}