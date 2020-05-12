package androidx.media.filterpacks.face;

import android.annotation.TargetApi;
import android.graphics.RectF;
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
public final class FaceToRectFilter extends Filter {
    private float mScale = 1.0f;

    public FaceToRectFilter(MffContext context, String name) {
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
        if (faces == null) {
            faces = new Face[0];
        }
        Quad[] quads = new Quad[faces.length];
        for (int i = 0; i < quads.length; i++) {
            quads[i] = faceRectToQuad(faces[i]);
        }
        OutputPort outPort = getConnectedOutputPort("quads");
        FrameValues quadsFrame = outPort.fetchAvailableFrame(new int[]{faces.length}).asFrameValues();
        quadsFrame.setValues(quads);
        outPort.pushFrame(quadsFrame);
    }

    private Quad faceRectToQuad(Face face) {
        RectF recf = new RectF(face.rect);
        recf.left = (recf.left / 2000.0f) + 0.5f;
        recf.right = (recf.right / 2000.0f) + 0.5f;
        recf.top = (recf.top / 2000.0f) + 0.5f;
        recf.bottom = (recf.bottom / 2000.0f) + 0.5f;
        return Quad.fromRect(recf).grow(this.mScale);
    }
}
