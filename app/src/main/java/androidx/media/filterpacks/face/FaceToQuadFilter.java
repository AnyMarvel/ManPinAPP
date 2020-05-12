package androidx.media.filterpacks.face;

import android.annotation.TargetApi;
import android.graphics.PointF;
import android.hardware.Camera.Face;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.FrameValues;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import androidx.media.filterfw.geometry.Quad;

@TargetApi(14)
public final class FaceToQuadFilter extends Filter {
    private float mScale = 2.0f;

    public FaceToQuadFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType facesType = FrameType.array(Face.class);
        return new Signature().addInputPort("faces", 2, facesType).addInputPort("scale", 1, FrameType.single(Float.TYPE)).addOutputPort("quads", 2, FrameType.array(Quad.class)).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("scale")) {
            port.bindToFieldNamed("mScale");
            port.setAutoPullEnabled(true);
        }
    }

    protected void onProcess() {
        Face[] faces = (Face[]) getConnectedInputPort("faces").pullFrame().asFrameValues().getValues();
        Quad[] quads = new Quad[faces.length];
        for (int i = 0; i < quads.length; i++) {
            quads[i] = faceToQuad(faces[i]);
        }
        OutputPort outPort = getConnectedOutputPort("quads");
        FrameValues quadsFrame = outPort.fetchAvailableFrame(new int[]{faces.length}).asFrameValues();
        quadsFrame.setValues(quads);
        outPort.pushFrame(quadsFrame);
    }

    private Quad faceToQuad(Face face) {
        PointF p0 = new PointF((((float) face.leftEye.x) / 2000.0f) + 0.5f, (((float) face.leftEye.y) / 2000.0f) + 0.5f);
        PointF p1 = new PointF((((float) face.rightEye.x) / 2000.0f) + 0.5f, (((float) face.rightEye.y) / 2000.0f) + 0.5f);
        return Quad.fromLineAndHeight(p0, p1, distancePointLine(p0, p1, new PointF((((float) face.mouth.x) / 2000.0f) + 0.5f, (((float) face.mouth.y) / 2000.0f) + 0.5f))).grow(this.mScale);
    }

    private static float distancePointLine(PointF p0, PointF p1, PointF q) {
        return Math.abs(((p1.x - p0.x) * (p0.y - q.y)) - ((p0.x - q.x) * (p1.y - p0.y))) / ((float) Math.sqrt((double) (((p1.x - p0.x) * (p1.x - p0.x)) + ((p1.y - p0.y) * (p1.y - p0.y)))));
    }
}
