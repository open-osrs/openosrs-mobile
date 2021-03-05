// Decompiled with: CFR 0.151
// Class Version: 6
package net.runelite.api;

import android.graphics.Path;

import net.runelite.api.coords.LocalPoint;

public class Perspective {
    public static final int[] COSINE;
    public static final int LOCAL_COORD_BITS = 7;
    public static final int LOCAL_HALF_TILE_SIZE = 64;
    public static final int LOCAL_TILE_SIZE = 128;
    public static final int SCENE_SIZE = 104;
    public static final int[] SINE;
    public static final double UNIT = 0.0030679615757712823;

    static {
        SINE = new int[2048];
        COSINE = new int[2048];
        for (int i = 0; i < 2048; ++i) {
            Perspective.SINE[i] = (int)(Math.sin((double)i * 0.0030679615757712823) * 65536.0);
            Perspective.COSINE[i] = (int)(Math.cos((double)i * 0.0030679615757712823) * 65536.0);
        }
    }

    public static Point getCanvasTextLocation(Object object, LocalPoint localPoint, String string, int n) {
        if (string == null) {
            return null;
        }
        if ((object = Perspective.localToCanvas((Client)object, localPoint, ((Client)object).getPlane(), n)) == null) {
            return null;
        }
        return new Point(((Point)object).getX(), ((Point)object).getY());
    }

    public static Path getCanvasTileAreaPoly(Client clientAPI, LocalPoint localPoint, int n) {
        return Perspective.getCanvasTileAreaPoly(clientAPI, localPoint, n, 0);
    }

    public static Path getCanvasTileAreaPoly(Client client, LocalPoint localPoint, int n, int n2) {
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

    public static Path getCanvasTilePoly(Client clientAPI, LocalPoint localPoint) {
        return Perspective.getCanvasTileAreaPoly(clientAPI, localPoint, 1);
    }

    public static Path getCanvasTilePoly(Client clientAPI, LocalPoint localPoint, int n) {
        return Perspective.getCanvasTileAreaPoly(clientAPI, localPoint, 1, n);
    }

    private static int getHeight(Client client, int n, int n2, int n3) {
        int n4 = n >> 7;
        int n5 = n2 >> 7;
        if (n4 >= 0 && n5 >= 0 && n4 < 104 && n5 < 104) {
            int[][][] object = client.getTileHeights();
            return (128 - (n2 &= 0x7F)) * (object[n3][n4 + 1][n5] * (n &= 0x7F) + (128 - n) * object[n3][n4][n5] >> 7) + n2 * (object[n3][n4][n5 + 1] * (128 - n) + object[n3][n4 + 1][n5 + 1] * n >> 7) >> 7;
        }
        return 0;
    }

    public static int getTileHeight(Client client, LocalPoint localPoint, int n) {
        int n2 = localPoint.getSceneX();
        int n3 = localPoint.getSceneY();
        if (n2 >= 0 && n3 >= 0 && n2 < 104 && n3 < 104) {
            int n4;
            byte[][][] byArray = client.getTileSettings();
            int[][][] object = client.getTileHeights();
            int n5 = n4 = n;
            if (n < 3) {
                n5 = n4;
                if ((byArray[1][n2][n3] & 2) == 2) {
                    n5 = n + 1;
                }
            }
            n = localPoint.getX() & 0x7F;
            n4 = localPoint.getY() & 0x7F;
            return (128 - n4) * (object[n5][n2 + 1][n3] * n + (128 - n) * object[n5][n2][n3] >> 7) + n4 * (object[n5][n2][n3 + 1] * (128 - n) + object[n5][n2 + 1][n3 + 1] * n >> 7) >> 7;
        }
        return 0;
    }

    public static Point localToCanvas(Client client, int n, int n2, int n3) {
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

    public static Point localToCanvas(Client clientAPI, LocalPoint localPoint, int n, int n2) {
        n = Perspective.getTileHeight(clientAPI, localPoint, n);
        return Perspective.localToCanvas(clientAPI, localPoint.getX(), localPoint.getY(), n - n2);
    }
}
