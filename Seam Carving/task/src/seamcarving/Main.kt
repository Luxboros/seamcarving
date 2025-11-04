package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.pow
import kotlin.math.sqrt


fun main(args: Array<String>) {
    require(args.size == 8) { "Program requires 8 arguments in the format: -in <input_file.png> -out <output_file.png> -width <width_to_reduce> -height <height_to_reduce>" }
    require(args[0] == "-in") { "First argument should be '-in'" }
    require(args[2] == "-out") { "Third argument should be '-out'" }
    require(args[4] == "-width") { "Fifth argument should be '-width'" }
    require(args[6] == "-height") { "Seventh argument should be '-height'" }
    require(args[1].endsWith(".png")) { "Input file must be a PNG image" }
    require(args[3].endsWith(".png")) { "Output file must be a PNG image" }
    require(args[5].toInt() > 0) { "Width reduction must be a positive number" }
    require(args[7].toInt() > 0) { "Height reduction must be a positive number" }

    val inputFile = File(args[1])
    val outputFile = File(args[3])
    val widthReduction = args[5].toInt()
    val heightReduction = args[7].toInt()

    var currentImage: BufferedImage = ImageIO.read(inputFile)

    for (i in 1..widthReduction) {
        val pixelEnergies = getPixelEnergies(currentImage)
        val path =
            findSeam(pixelEnergies, currentImage.width, currentImage.height)
        val tmpImage = BufferedImage(
            currentImage.width - 1,
            currentImage.height,
            BufferedImage.TYPE_INT_RGB
        )
        removeVerticalSeam(currentImage, tmpImage, path)
        currentImage = tmpImage
    }

    for (i in 1..heightReduction) {
        val pixelEnergies = transpose(
            getPixelEnergies(currentImage),
            currentImage.width,
            currentImage.height
        )
        val path =
            findSeam(pixelEnergies, currentImage.height, currentImage.width)
        val tmpImage = BufferedImage(
            currentImage.width,
            currentImage.height - 1,
            BufferedImage.TYPE_INT_RGB
        )
        removeHorizontalSeam(currentImage, tmpImage, path)
        currentImage = tmpImage
    }
    ImageIO.write(currentImage, "png", outputFile)
}

fun removeVerticalSeam(
    inputImage: BufferedImage,
    outputImage: BufferedImage,
    path: List<Vertex>
) {
    val seamCoordinates = path.associate { it.y to it.x }
    for (y in 0 until inputImage.height) {
        val seamX = seamCoordinates[y]!!
        for (x in 0 until seamX) {
            outputImage.setRGB(x, y, inputImage.getRGB(x, y))
        }
        for (x in (seamX + 1) until inputImage.width) {
            outputImage.setRGB(x - 1, y, inputImage.getRGB(x, y))
        }
    }
}

fun removeHorizontalSeam(
    inputImage: BufferedImage,
    outputImage: BufferedImage,
    path: List<Vertex>
) {
    val seamCoordinates = path.associate { it.y to it.x }
    for (x in 0 until inputImage.width) {
        val seamY = seamCoordinates[x]!!
        for (y in 0 until seamY) {
            outputImage.setRGB(x, y, inputImage.getRGB(x, y))
        }
        for (y in seamY + 1 until outputImage.height) {
            outputImage.setRGB(x, y - 1, inputImage.getRGB(x, y))
        }
    }
}

fun getPixelEnergies(image: BufferedImage): Array<DoubleArray> {
    val pixelEnergies = Array(image.height) { DoubleArray(image.width) }
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val (dx, dy) = getGradients(x, y, image)
            val e = getEnergy(dx, dy)
            pixelEnergies[y][x] = e
        }
    }
    return pixelEnergies
}

fun getEnergy(dx: Double, dy: Double): Double {
    return sqrt(dx + dy)
}

fun getGradients(x: Int, y: Int, bufferedImage: BufferedImage): DoubleArray {
    val width = bufferedImage.width
    val height = bufferedImage.height

    val effectiveX = when (x) {
        0 -> 1
        width - 1 -> width - 2
        else -> x
    }

    val x1Color = Color(bufferedImage.getRGB(effectiveX - 1, y))
    val x2Color = Color(bufferedImage.getRGB(effectiveX + 1, y))

    val rx = (x1Color.red - x2Color.red).toDouble()
    val gx = (x1Color.green - x2Color.green).toDouble()
    val bx = (x1Color.blue - x2Color.blue).toDouble()

    val effectiveY = when (y) {
        0 -> 1
        height - 1 -> height - 2
        else -> y
    }

    val y1Color = Color(bufferedImage.getRGB(x, effectiveY - 1))
    val y2Color = Color(bufferedImage.getRGB(x, effectiveY + 1))

    val ry = (y1Color.red - y2Color.red).toDouble()
    val gy = (y1Color.green - y2Color.green).toDouble()
    val by = (y1Color.blue - y2Color.blue).toDouble()

    val dx = rx.pow(2) + gx.pow(2) + bx.pow(2)
    val dy = ry.pow(2) + gy.pow(2) + by.pow(2)

    return doubleArrayOf(dx, dy)
}

class Vertex(
    val x: Int,
    val y: Int,
    val totalEnergy: Double,
)

fun findSeam(
    graph: Array<DoubleArray>,
    width: Int,
    height: Int
): List<Vertex> {

    val distanceTo =
        Array(height) { DoubleArray(width) { Double.POSITIVE_INFINITY } }
    val parent = Array(height) { arrayOfNulls<Vertex>(width) }
    val queue = PriorityQueue<Vertex>(compareBy { it.totalEnergy })
    val path = mutableListOf<Vertex>()
    for (x in 0 until width) {
        distanceTo[0][x] = graph[0][x]
        queue.add(Vertex(x, 0, graph[0][x]))

    }

    while (queue.isNotEmpty()) {
        val vertex = queue.poll()
        if (vertex.totalEnergy > distanceTo[vertex.y][vertex.x]) continue
        if (vertex.y == height - 1) continue

        for (neighborX in (vertex.x - 1)..(vertex.x + 1)) {
            val neighborY = vertex.y + 1

            if (neighborX in 0 until width) {

                val newTotalEnergy =
                    vertex.totalEnergy + graph[neighborY][neighborX]

                if (newTotalEnergy < distanceTo[neighborY][neighborX]) {
                    distanceTo[neighborY][neighborX] = newTotalEnergy
                    parent[neighborY][neighborX] = vertex
                    queue.add(Vertex(neighborX, neighborY, newTotalEnergy))
                }
            }
        }
    }


    val (endX, _) = distanceTo[height - 1]
        .withIndex()
        .minBy { it.value }

    var current: Vertex? =
        Vertex(endX, height - 1, distanceTo[height - 1][endX])

    while (current != null) {
        path.add(current)
        current = parent[current.y][current.x]
    }

    return path.reversed()
}

fun transpose(
    graph: Array<DoubleArray>,
    width: Int,
    height: Int
): Array<DoubleArray> {

    val transposedGraph = Array(width) { DoubleArray(height) }

    for (y in 0 until height) {
        for (x in 0 until width) {
            transposedGraph[x][y] = graph[y][x]
        }
    }

    return transposedGraph
}


