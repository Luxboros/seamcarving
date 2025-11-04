# Seam Carving: Content-Aware Image Resizing in Kotlin

![A GIF or side-by-side screenshot showing an image before and after seam carving]

## 💡 Project Overview

This is a Kotlin implementation of **Seam Carving**, a content-aware image resizing algorithm. Instead of distorting or cropping an image, this method finds and removes the least important "seams" (paths of pixels with the lowest energy) to intelligently resize an image while preserving its most important content.

This project was a deep dive into graph theory, complex algorithms, and efficient 2D array manipulation.

---

## 🧠 Core Concepts & Algorithms Implemented

This project is built on several key computer science concepts:

* **Graph Theory:** The image is treated as a 2D graph where each pixel is a node, and its "energy" is the weight of the node.
* **Dijkstra's Algorithm:** Implemented from scratch to find the "shortest path" (the seam with the lowest total energy) across the image graph.
* **Data Structures:** Used a `PriorityQueue` (Min-Heap) to create an efficient $O(N \log N)$ implementation of Dijkstra's algorithm, which is ideal for a sparse graph like an image.
* **Matrix Transposition:** The algorithm is generalized to support both vertical and horizontal seam removal by transposing the energy graph and re-using the *exact same* `findSeam` function, following the DRY (Don't Repeat Yourself) principle.
* **Efficient Image Processing:** All resizing is done in-memory using `BufferedImage`. The program reads the file once, performs all seam removals in a loop, and writes to disk only once, avoiding slow I/O operations on each iteration.
* **2D Array Debugging:** Managed and debugged common, tricky 2D array indexing bugs (row-major `[y][x]` vs. column-major `[x][y]`).

---

## 🛠️ Technologies Used

* **Language:** Kotlin
* **Platform:** JVM

---

## 🚀 How to Run

This project uses a standard Gradle build. The included Gradle Wrapper (gradlew) is the recommended way to run it.

- Requirements: Java (JDK) 11+ installed and on your PATH

### Option A — Run with Gradle (recommended, no Kotlin install needed)
- macOS/Linux:
```bash
./gradlew :Seam_Carving-task:run --args="-in input.png -out output.png -width 100 -height 50"
```
- Windows (PowerShell or CMD):
```bat
gradlew.bat :Seam_Carving-task:run --args="-in input.png -out output.png -width 100 -height 50"
```

### Option B — Create local installation scripts and run
This creates platform scripts under the task module and runs without needing to type the module name each time.
- Build scripts once:
```bash
./gradlew :Seam_Carving-task:installDist
```
- Then run the installed app:
  - macOS/Linux:
  ```bash
  "Seam Carving/task/build/install/Seam_Carving-task/bin/Seam_Carving-task" -in input.png -out output.png -width 100 -height 50
  ```
  - Windows:
  ```bat
  "Seam Carving\\task\\build\\install\\Seam_Carving-task\\bin\\Seam_Carving-task.bat" -in input.png -out output.png -width 100 -height 50
  ```

### Option C — If you already have a JAR
If you’ve built a runnable JAR yourself, you can still use plain Java:
```bash
java -jar SeamCarving.jar -in input.png -out output.png -width 100 -height 50
```