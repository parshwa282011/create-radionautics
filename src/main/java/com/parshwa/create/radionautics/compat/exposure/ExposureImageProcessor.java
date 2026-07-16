package com.parshwa.create.radionautics.compat.exposure;

import io.github.mortuusars.exposure.data.ColorPalettes;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class ExposureImageProcessor {
    private static final int[][] CC = {
            {240, 240, 240}, {242, 178, 51}, {229, 127, 216}, {153, 178, 242},
            {222, 222, 108}, {127, 204, 25}, {242, 178, 204}, {76, 76, 76},
            {153, 153, 153}, {76, 153, 178}, {178, 102, 229}, {51, 102, 204},
            {127, 102, 76}, {87, 166, 78}, {204, 76, 76}, {17, 17, 17}
    };
    private static final char[] HEX = "0123456789abcdef".toCharArray();
    private static final char[] PRINT = " .:-=+*#%@".toCharArray();

    private ExposureImageProcessor() {
    }

    public static List<String> prepareMonitor(Level level, ByteBuffer source, String paletteId,
                                              int width, int height, int targetWidth, int targetHeight,
                                              boolean dither) {
        validate(width, height, targetWidth, targetHeight);
        int[][] pixels = resample(readRgb(level, source, paletteId, width, height), width, height,
                targetWidth, targetHeight);
        return quantize(pixels, targetWidth, targetHeight, dither);
    }

    public static List<String> preparePrint(Level level, ByteBuffer source, String paletteId,
                                            int width, int height, int targetWidth, int targetHeight) {
        validate(width, height, targetWidth, targetHeight);
        if (targetWidth > 25 || targetHeight > 21) {
            throw new IllegalArgumentException("CC printer images cannot exceed 25x21 characters");
        }
        int[][] pixels = resample(readRgb(level, source, paletteId, width, height), width, height,
                targetWidth, targetHeight);
        List<String> rows = new ArrayList<>(targetHeight);
        for (int y = 0; y < targetHeight; y++) {
            StringBuilder row = new StringBuilder(targetWidth);
            for (int x = 0; x < targetWidth; x++) {
                int[] rgb = pixels[y * targetWidth + x];
                double luminance = (0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2]) / 255.0;
                row.append(PRINT[(int) Math.round(luminance * (PRINT.length - 1))]);
            }
            rows.add(row.toString());
        }
        return rows;
    }

    private static int[][] readRgb(Level level, ByteBuffer input, String paletteId, int width, int height) {
        ByteBuffer data = input.slice();
        if (data.remaining() < width * height) throw new IllegalArgumentException("image pixel buffer is too short");
        ResourceLocation id = ResourceLocation.tryParse(paletteId);
        if (id == null) throw new IllegalArgumentException("invalid Exposure palette: " + paletteId);
        var palette = ColorPalettes.get(level.registryAccess(), id).value();
        int[][] result = new int[width * height][3];
        for (int i = 0; i < result.length; i++) {
            int argb = palette.byId(Byte.toUnsignedInt(data.get()));
            result[i][0] = (argb >>> 16) & 255;
            result[i][1] = (argb >>> 8) & 255;
            result[i][2] = argb & 255;
        }
        return result;
    }

    private static int[][] resample(int[][] source, int width, int height, int outWidth, int outHeight) {
        int[][] output = new int[outWidth * outHeight][3];
        for (int oy = 0; oy < outHeight; oy++) {
            int y0 = oy * height / outHeight;
            int y1 = Math.max(y0 + 1, (oy + 1) * height / outHeight);
            for (int ox = 0; ox < outWidth; ox++) {
                int x0 = ox * width / outWidth;
                int x1 = Math.max(x0 + 1, (ox + 1) * width / outWidth);
                long r = 0, g = 0, b = 0, count = 0;
                for (int y = y0; y < Math.min(y1, height); y++) {
                    for (int x = x0; x < Math.min(x1, width); x++) {
                        int[] pixel = source[y * width + x];
                        r += pixel[0]; g += pixel[1]; b += pixel[2]; count++;
                    }
                }
                output[oy * outWidth + ox] = new int[] {(int) (r / count), (int) (g / count), (int) (b / count)};
            }
        }
        return output;
    }

    private static List<String> quantize(int[][] pixels, int width, int height, boolean dither) {
        double[][] values = new double[pixels.length][3];
        for (int i = 0; i < pixels.length; i++) for (int c = 0; c < 3; c++) values[i][c] = pixels[i][c];
        List<String> rows = new ArrayList<>(height);
        for (int y = 0; y < height; y++) {
            StringBuilder row = new StringBuilder(width);
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                int nearest = nearest(values[index]);
                row.append(HEX[nearest]);
                if (dither) {
                    double[] error = {values[index][0] - CC[nearest][0], values[index][1] - CC[nearest][1], values[index][2] - CC[nearest][2]};
                    spread(values, width, height, x + 1, y, error, 7.0 / 16.0);
                    spread(values, width, height, x - 1, y + 1, error, 3.0 / 16.0);
                    spread(values, width, height, x, y + 1, error, 5.0 / 16.0);
                    spread(values, width, height, x + 1, y + 1, error, 1.0 / 16.0);
                }
            }
            rows.add(row.toString());
        }
        return rows;
    }

    private static int nearest(double[] rgb) {
        int best = 0; double bestDistance = Double.MAX_VALUE;
        for (int i = 0; i < CC.length; i++) {
            double dr = rgb[0] - CC[i][0], dg = rgb[1] - CC[i][1], db = rgb[2] - CC[i][2];
            double distance = dr * dr * 0.2126 + dg * dg * 0.7152 + db * db * 0.0722;
            if (distance < bestDistance) { bestDistance = distance; best = i; }
        }
        return best;
    }

    private static void spread(double[][] values, int width, int height, int x, int y, double[] error, double factor) {
        if (x < 0 || y < 0 || x >= width || y >= height) return;
        for (int c = 0; c < 3; c++) values[y * width + x][c] = Math.max(0, Math.min(255, values[y * width + x][c] + error[c] * factor));
    }

    private static void validate(int width, int height, int targetWidth, int targetHeight) {
        if (width <= 0 || height <= 0 || targetWidth <= 0 || targetHeight <= 0) throw new IllegalArgumentException("image dimensions must be positive");
        if ((long) width * height > 4_194_304L || (long) targetWidth * targetHeight > 262_144L) throw new IllegalArgumentException("image dimensions are too large");
    }
}
