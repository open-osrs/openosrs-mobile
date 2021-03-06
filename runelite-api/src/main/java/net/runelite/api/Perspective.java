// Decompiled with: CFR 0.151
// Class Version: 6
package net.runelite.api;

import android.graphics.Path;
import android.util.Log;

import net.runelite.api.coords.LocalPoint;

import javax.annotation.Nonnull;

import static net.runelite.api.Constants.TILE_FLAG_BRIDGE;

public class Perspective {
    public static final int LOCAL_COORD_BITS = 7;
    public static final int LOCAL_HALF_TILE_SIZE = 64;
    public static final int LOCAL_TILE_SIZE = 128;
    public static final int SCENE_SIZE = 104;
    public static final double UNIT = Math.PI / 1024d;

    public static final int[] SINE = new int[2048]; // sine angles for each of the 2048 units, * 65536 and stored as an int
    public static final int[] COSINE = new int[2048]; // cosine

    static {
        for (int i = 0; i < 2048; ++i)
        {
            SINE[i] = (int) (65536.0D * Math.sin((double) i * UNIT));
            COSINE[i] = (int) (65536.0D * Math.cos((double) i * UNIT));
        }
    }

    public static Point getCanvasTextLocation(@Nonnull Client client, @Nonnull LocalPoint localPoint, String string, int plane, int n) {
        if (string == null) {
            return null;
        }
        Point p = Perspective.localToCanvas(client, localPoint, plane, n);
        if (p == null) {
            return null;
        }
        else
        {
            Log.e("Perspective", p.toString());
        }

        return new Point(p.getX(), p.getY());
    }

    public static Path getCanvasTileAreaPoly(@Nonnull Client clientAPI, @Nonnull LocalPoint localPoint, int n) {
        return Perspective.getCanvasTileAreaPoly(clientAPI, localPoint, n, 0);
    }

    public static Path getCanvasTileAreaPoly(@Nonnull Client client, @Nonnull LocalPoint localPoint, int n, int n2) {
        int n3 = client.getPlane();
        int n4 = localPoint.getX() - n * 128 / 2;
        int n5 = localPoint.getY() - n * 128 / 2;
        int n6 = localPoint.getX() + n * 128 / 2;
        int n7 = localPoint.getY() + n * 128 / 2;
        byte[][][] tileSettings = client.getTileSettings();
        int n8 = localPoint.getSceneX();
        int n9 = localPoint.getSceneY();
        if (n8 >= 0 && n9 >= 0 && n8 < 104 && n9 < 104) {
            int n10;
            n = n10 = n3;
            if (n3 < 3) {
                n = n10;
                if ((tileSettings[1][n8][n9] & 2) == 2) {
                    n = n3 + 1;
                }
            }
            n10 = Perspective.getHeight(client, n4, n5, n);
            n8 = Perspective.getHeight(client, n6, n5, n);
            n3 = Perspective.getHeight(client, n6, n7, n);
            n = Perspective.getHeight(client, n4, n7, n);
            Point p1 = Perspective.localToCanvas(client, n4, n5, n10 - n2);
            Point p2 = Perspective.localToCanvas(client, n6, n5, n8 - n2);
            Point p3 = Perspective.localToCanvas(client, n6, n7, n3 - n2);
            Point p4 = Perspective.localToCanvas(client, n4, n7, n - n2);
            if (p1 != null && p2 != null && p3 != null && p4 != null) {
                Path path = new Path();
                path.moveTo((float)((Point)p1).getX(), (float)((Point)p1).getY());
                path.lineTo((float)((Point)p2).getX(), (float)((Point)p2).getY());
                path.lineTo((float)p3.getX(), (float)p3.getY());
                path.lineTo((float)((Point)p4).getX(), (float)((Point)p4).getY());
                return path;
            }
            return null;
        }
        return null;
    }

    public static Path getCanvasTilePoly(@Nonnull Client clientAPI, @Nonnull LocalPoint localPoint) {
        return Perspective.getCanvasTileAreaPoly(clientAPI, localPoint, 1);
    }

    public static Path getCanvasTilePoly(@Nonnull Client clientAPI, @Nonnull LocalPoint localPoint, int n) {
        return Perspective.getCanvasTileAreaPoly(clientAPI, localPoint, 1, n);
    }

    private static int getHeight(@Nonnull Client client, int localX, int localY, int plane)
    {
        int sceneX = localX >> LOCAL_COORD_BITS;
        int sceneY = localY >> LOCAL_COORD_BITS;
        if (sceneX >= 0 && sceneY >= 0 && sceneX < SCENE_SIZE && sceneY < SCENE_SIZE)
        {
            int[][][] tileHeights = client.getTileHeights();

            int x = localX & (LOCAL_TILE_SIZE - 1);
            int y = localY & (LOCAL_TILE_SIZE - 1);
            int var8 = x * tileHeights[plane][sceneX + 1][sceneY] + (LOCAL_TILE_SIZE - x) * tileHeights[plane][sceneX][sceneY] >> LOCAL_COORD_BITS;
            int var9 = tileHeights[plane][sceneX][sceneY + 1] * (LOCAL_TILE_SIZE - x) + x * tileHeights[plane][sceneX + 1][sceneY + 1] >> LOCAL_COORD_BITS;
            return (LOCAL_TILE_SIZE - y) * var8 + y * var9 >> LOCAL_COORD_BITS;
        }

        return 0;
    }

    public static int getTileHeight(@Nonnull Client client, @Nonnull LocalPoint point, int plane) {
        int sceneX = point.getSceneX();
        int sceneY = point.getSceneY();
        if (sceneX >= 0 && sceneY >= 0 && sceneX < SCENE_SIZE && sceneY < SCENE_SIZE)
        {
            byte[][][] tileSettings = client.getTileSettings();
            int[][][] tileHeights = client.getTileHeights();
            if (plane < 0)
                plane = 0;
            int z1 = plane;
            if (plane < Constants.MAX_Z - 1 && (tileSettings[1][sceneX][sceneY] & TILE_FLAG_BRIDGE) == TILE_FLAG_BRIDGE)
            {
                z1 = plane + 1;
            }

            int x = point.getX() & (LOCAL_TILE_SIZE - 1);
            int y = point.getY() & (LOCAL_TILE_SIZE - 1);
            int var8 = x * tileHeights[z1][sceneX + 1][sceneY] + (LOCAL_TILE_SIZE - x) * tileHeights[z1][sceneX][sceneY] >> LOCAL_COORD_BITS;
            int var9 = tileHeights[z1][sceneX][sceneY + 1] * (LOCAL_TILE_SIZE - x) + x * tileHeights[z1][sceneX + 1][sceneY + 1] >> LOCAL_COORD_BITS;
            return (LOCAL_TILE_SIZE - y) * var8 + y * var9 >> LOCAL_COORD_BITS;
        }

        return 0;
    }

    /*
    public static Point localToCanvas(@Nonnull Client client, int n, int n2, int n3) {
        if (n >= 128 && n2 >= 128 && n <= 13056 && n2 <= 13056) {
            int n4;
            int n5 = n3 - client.getCameraZ();
            int n6 = client.getCameraPitch();
            int n7 = client.getCameraYaw();
            int[] nArray = SINE;
            n3 = nArray[n6];
            int[] nArray2 = COSINE;
            int n8 = (n7 = nArray2[n7]) * (n2 -= client.getCameraY()) - (n4 = nArray[n7]) * (n -= client.getCameraX()) >> 16;
            int n9 = n5 * n3 + n8 * (n6 = nArray2[n6]) >> 16;
            if (n9 >= 50) {
                double d = (double)client.getCameraZoom() * (double)client.getOverlayWidth() / (double)client.getViewportWidth();
                double d2 = (double)client.getCameraZoom() * (double)client.getOverlayHeight() / (double)client.getViewportHeight();
                int n10 = client.getOverlayWidth();
                int n11 = client.getOverlayHeight();
                return new Point(n10 / 2 + (int)d * (n7 * n + n2 * n4 >> 16) / n9, n11 / 2 + (int)d2 * (n6 * n5 - n8 * n3 >> 16) / n9);
            }
        }
        return null;
    }
     */

    public static Point localToCanvas(@Nonnull Client client, int x, int y, int z)
    {
        if (x >= 128 && y >= 128 && x <= 13056 && y <= 13056)
        {
            x -= client.getCameraX();
            y -= client.getCameraY();
            z -= client.getCameraZ();

            int cameraPitch = client.getCameraPitch();
            int cameraYaw = client.getCameraYaw();

            int pitchSin = SINE[cameraPitch];
            int pitchCos = COSINE[cameraPitch];
            int yawSin = SINE[cameraYaw];
            int yawCos = COSINE[cameraYaw];

            int var8 = yawCos * x + y * yawSin >> 16;
            y = yawCos * y - yawSin * x >> 16;
            x = var8;
            var8 = pitchCos * z - y * pitchSin >> 16;
            y = z * pitchSin + y * pitchCos >> 16;

            if (y >= 50)
            {
                Log.e("Points", "x:" + x + " y:" + y);
                double pointX = (double)(client.getViewportWidth() / 2 + x * client.getCameraZoom() / y) * (double)client.getOverlayWidth() / (double)client.getViewportWidth();
                double pointY = (double)(client.getViewportHeight() / 2 + var8 * client.getCameraZoom() / y) * (double)client.getOverlayWidth() / (double)client.getViewportWidth();
                return new Point(
                        (int)pointX,
                        (int)pointY);
            }
        }

        return null;
    }

    public static Point localToCanvas(@Nonnull Client clientAPI, @Nonnull LocalPoint localPoint, int n, int n2) {
        n = Perspective.getTileHeight(clientAPI, localPoint, n);
        return Perspective.localToCanvas(clientAPI, localPoint.getX(), localPoint.getY(), n - n2);
    }
}
