package com.google.android.apps.photolab.storyboard.pipeline;

import android.opengl.GLES30;
import android.util.Log;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.ImageShader;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import androidx.media.filterfw.TextureSource;

public class GlDownscaleFilter extends Filter {
    private static final int DEFAULT_MIPMAP_LEVELS = 2;
    private static final String TAG = GlDownscaleFilter.class.getSimpleName();
    private ImageShader imageShader;
    private int numMipmapLevels = 2;
    private int outputHeight;
    private int outputWidth;

    public GlDownscaleFilter(MffContext context, String name) {
        super(context, name);
    }

    public Signature getSignature() {
        FrameType imageInType = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageInType).addInputPort("outputWidth", 2, FrameType.single(Integer.TYPE)).addInputPort("outputHeight", 2, FrameType.single(Integer.TYPE)).addOutputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 16)).disallowOtherPorts();
    }

    public void onInputPortOpen(InputPort port) {
        if (port.getName().equals("outputWidth")) {
            port.bindToFieldNamed("outputWidth");
            port.setAutoPullEnabled(true);
        } else if (port.getName().equals("outputHeight")) {
            port.bindToFieldNamed("outputHeight");
            port.setAutoPullEnabled(true);
        }
    }

    public void setNumMipmapLevels(int numMipmapLevels) {
        this.numMipmapLevels = numMipmapLevels;
    }

    public void onPrepare() {
        this.imageShader = ImageShader.createIdentity();
    }

    public void onProcess() {
        long startTimeMs = System.currentTimeMillis();
        OutputPort outPort = getConnectedOutputPort("image");
        FrameImage2D inputImage = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        FrameImage2D outputImage = outPort.fetchAvailableFrame(new int[]{this.outputWidth, this.outputHeight}).asFrameImage2D();
        GLES30.glHint(33170, 4353);
        TextureSource texture = inputImage.lockTextureSource();
        int textureTarget = texture.getTarget();
        GLES30.glBindTexture(textureTarget, texture.getTextureId());
        GLES30.glTexParameteri(textureTarget, 10241, 9987);
        GLES30.glTexParameteri(textureTarget, 33084, 0);
        GLES30.glTexParameteri(textureTarget, 33085, this.numMipmapLevels);
        GLES30.glGenerateMipmap(textureTarget);
        GLES30.glBindTexture(textureTarget, 0);
        inputImage.unlock();
        this.imageShader.setSourceRect(0.0f, 0.0f, 1.0f, 1.0f);
        this.imageShader.setTargetRect(0.0f, 0.0f, 1.0f, 1.0f);
        this.imageShader.process(inputImage, outputImage);
        Log.d(TAG, "Time: " + (System.currentTimeMillis() - startTimeMs));
        outPort.pushFrame(outputImage);
    }
}
