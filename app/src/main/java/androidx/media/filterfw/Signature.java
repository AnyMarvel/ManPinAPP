package androidx.media.filterfw;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class Signature {
    public static final int PORT_OPTIONAL = 1;
    public static final int PORT_REQUIRED = 2;
    private boolean mAllowOtherInputs = true;
    private boolean mAllowOtherOutputs = true;
    private HashMap<String, PortInfo> mInputPorts = null;
    private HashMap<String, PortInfo> mOutputPorts = null;

    static class PortInfo {
        public int flags;
        public FrameType type;

        public PortInfo() {
            this.flags = 0;
            this.type = FrameType.any();
        }

        public PortInfo(int flags, FrameType type) {
            this.flags = flags;
            this.type = type;
        }

        public boolean isRequired() {
            return (this.flags & 2) != 0;
        }

        public String toString(String ioMode, String name) {
            String ioName = new StringBuilder((String.valueOf(ioMode).length() + 1) + String.valueOf(name).length()).append(ioMode).append(" ").append(name).toString();
            String modeName = isRequired() ? "required" : "optional";
            String frameType = this.type.toString();
            return new StringBuilder(((String.valueOf(modeName).length() + 3) + String.valueOf(ioName).length()) + String.valueOf(frameType).length()).append(modeName).append(" ").append(ioName).append(": ").append(frameType).toString();
        }
    }

    public Signature addInputPort(String name, int flags, FrameType type) {
        addInputPort(name, new PortInfo(flags, type));
        return this;
    }

    public Signature addOutputPort(String name, int flags, FrameType type) {
        addOutputPort(name, new PortInfo(flags, type));
        return this;
    }

    public Signature disallowOtherInputs() {
        this.mAllowOtherInputs = false;
        return this;
    }

    public Signature disallowOtherOutputs() {
        this.mAllowOtherOutputs = false;
        return this;
    }

    public Signature disallowOtherPorts() {
        this.mAllowOtherInputs = false;
        this.mAllowOtherOutputs = false;
        return this;
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (Entry<String, PortInfo> entry : this.mInputPorts.entrySet()) {
            stringBuffer.append(String.valueOf(((PortInfo) entry.getValue()).toString("input", (String) entry.getKey())).concat("\n"));
        }
        for (Entry<String, PortInfo> entry2 : this.mOutputPorts.entrySet()) {
            stringBuffer.append(String.valueOf(((PortInfo) entry2.getValue()).toString("output", (String) entry2.getKey())).concat("\n"));
        }
        if (!this.mAllowOtherInputs) {
            stringBuffer.append("disallow other inputs\n");
        }
        if (!this.mAllowOtherOutputs) {
            stringBuffer.append("disallow other outputs\n");
        }
        return stringBuffer.toString();
    }

    PortInfo getInputPortInfo(String name) {
        PortInfo result = this.mInputPorts != null ? (PortInfo) this.mInputPorts.get(name) : null;
        return result != null ? result : new PortInfo();
    }

    PortInfo getOutputPortInfo(String name) {
        PortInfo result = this.mOutputPorts != null ? (PortInfo) this.mOutputPorts.get(name) : null;
        return result != null ? result : new PortInfo();
    }

    void checkInputPortsConform(Filter filter) {
        Set<String> filterInputs = new HashSet();
        filterInputs.addAll(filter.getConnectedInputPortMap().keySet());
        if (this.mInputPorts != null) {
            for (Entry<String, PortInfo> entry : this.mInputPorts.entrySet()) {
                String portName = (String) entry.getKey();
                PortInfo portInfo = (PortInfo) entry.getValue();
                if (filter.getConnectedInputPort(portName) == null && portInfo.isRequired()) {
                    String valueOf = String.valueOf(filter);
                    throw new RuntimeException(new StringBuilder((String.valueOf(valueOf).length() + 45) + String.valueOf(portName).length()).append("Filter ").append(valueOf).append(" does not have required input port '").append(portName).append("'!").toString());
                }
                filterInputs.remove(portName);
            }
        }
        if (!this.mAllowOtherInputs && !filterInputs.isEmpty()) {
            String valueOf = String.valueOf(filter);
            String valueOf2 = String.valueOf(filterInputs);
            throw new RuntimeException(new StringBuilder((String.valueOf(valueOf).length() + 34) + String.valueOf(valueOf2).length()).append("Filter ").append(valueOf).append(" has invalid input ports: ").append(valueOf2).append("!").toString());
        }
    }

    void checkOutputPortsConform(Filter filter) {
        Set<String> filterOutputs = new HashSet();
        filterOutputs.addAll(filter.getConnectedOutputPortMap().keySet());
        if (this.mOutputPorts != null) {
            for (Entry<String, PortInfo> entry : this.mOutputPorts.entrySet()) {
                String portName = (String) entry.getKey();
                PortInfo portInfo = (PortInfo) entry.getValue();
                if (filter.getConnectedOutputPort(portName) == null && portInfo.isRequired()) {
                    String valueOf = String.valueOf(filter);
                    throw new RuntimeException(new StringBuilder((String.valueOf(valueOf).length() + 46) + String.valueOf(portName).length()).append("Filter ").append(valueOf).append(" does not have required output port '").append(portName).append("'!").toString());
                }
                filterOutputs.remove(portName);
            }
        }
        if (!this.mAllowOtherOutputs && !filterOutputs.isEmpty()) {
           String valueOf = String.valueOf(filter);
            String valueOf2 = String.valueOf(filterOutputs);
            throw new RuntimeException(new StringBuilder((String.valueOf(valueOf).length() + 35) + String.valueOf(valueOf2).length()).append("Filter ").append(valueOf).append(" has invalid output ports: ").append(valueOf2).append("!").toString());
        }
    }

    HashMap<String, PortInfo> getInputPorts() {
        return this.mInputPorts;
    }

    HashMap<String, PortInfo> getOutputPorts() {
        return this.mOutputPorts;
    }

    private void addInputPort(String name, PortInfo portInfo) {
        if (this.mInputPorts == null) {
            this.mInputPorts = new HashMap();
        }
        if (this.mInputPorts.containsKey(name)) {
            throw new RuntimeException(new StringBuilder(String.valueOf(name).length() + 42).append("Attempting to add duplicate input port '").append(name).append("'!").toString());
        }
        this.mInputPorts.put(name, portInfo);
    }

    private void addOutputPort(String name, PortInfo portInfo) {
        if (this.mOutputPorts == null) {
            this.mOutputPorts = new HashMap();
        }
        if (this.mOutputPorts.containsKey(name)) {
            throw new RuntimeException(new StringBuilder(String.valueOf(name).length() + 43).append("Attempting to add duplicate output port '").append(name).append("'!").toString());
        }
        this.mOutputPorts.put(name, portInfo);
    }
}
