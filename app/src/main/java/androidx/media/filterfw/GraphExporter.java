package androidx.media.filterfw;

import android.content.Context;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import androidx.media.filterfw.Signature.*;

public class GraphExporter {
    public static void exportAsDot(FilterGraph graph, String filename, boolean includeUnconnectedOptionalPorts) throws FileNotFoundException, IOException {
        Context myAppContext = graph.getContext().getApplicationContext();
        Filter[] filters = graph.getAllFilters();
        OutputStreamWriter dotFile = new OutputStreamWriter(myAppContext.openFileOutput(filename, 0));
        dotFile.write("digraph graphname {\n");
        dotFile.write("  node [shape=record];\n");
        for (Filter filter : filters) {
            int counter;
            String str = "  ";
            String valueOf = String.valueOf(filter.getName());
            if (valueOf.length() != 0) {
                valueOf = str.concat(valueOf);
            } else {
                String str2 = new String(str);
            }
            dotFile.write(String.valueOf(getDotName(valueOf)).concat(" [label=\"{"));
            Set<String> inputPorts = getInputPorts(filter, includeUnconnectedOptionalPorts);
            if (inputPorts.size() > 0) {
                dotFile.write(" { ");
                counter = 0;
                for (String p : inputPorts) {
                    str = getDotName(p);
                    dotFile.write(new StringBuilder((String.valueOf(str).length() + 5) + String.valueOf(p).length()).append("<").append(str).append("_IN>").append(p).toString());
                    counter++;
                    if (counter != inputPorts.size()) {
                        dotFile.write(" | ");
                    }
                }
                dotFile.write(" } | ");
            }
            dotFile.write(filter.getName());
            Set<String> outputPorts = getOutputPorts(filter, includeUnconnectedOptionalPorts);
            if (outputPorts.size() > 0) {
                dotFile.write(" | { ");
                counter = 0;
                for (String p2 : outputPorts) {
                    str = getDotName(p2);
                    dotFile.write(new StringBuilder((String.valueOf(str).length() + 6) + String.valueOf(p2).length()).append("<").append(str).append("_OUT>").append(p2).toString());
                    counter++;
                    if (counter != outputPorts.size()) {
                        dotFile.write(" | ");
                    }
                }
                dotFile.write(" } ");
            }
            dotFile.write("}\"];\n");
        }
        dotFile.write("\n");
        int dummyNodeCounter = 0;
        for (Filter filter2 : filters) {
            String dotName;
            String str;
            for (String portName : getOutputPorts(filter2, includeUnconnectedOptionalPorts)) {
                OutputPort source = filter2.getConnectedOutputPort(portName);
                if (source != null) {
                    InputPort target = source.getTarget();
                    str = getDotName(source.getFilter().getName());
                    dotName = getDotName(source.getName());
                    String dotName2 = getDotName(target.getFilter().getName());
                    String dotName3 = getDotName(target.getName());
                    dotFile.write(new StringBuilder((((String.valueOf(str).length() + 17) + String.valueOf(dotName).length()) + String.valueOf(dotName2).length()) + String.valueOf(dotName3).length()).append("  ").append(str).append(":").append(dotName).append("_OUT -> ").append(dotName2).append(":").append(dotName3).append("_IN;\n").toString());
                } else {
                    String color = filter2.getSignature().getOutputPortInfo(portName).isRequired() ? "red" : "blue";
                    dummyNodeCounter++;
                    str = getDotName(filter2.getName());
                    dotName = getDotName(portName);
                    dotFile.write(new StringBuilder((((String.valueOf(color).length() + 88) + String.valueOf(str).length()) + String.valueOf(dotName).length()) + String.valueOf(color).length()).append("  dummy").append(dummyNodeCounter).append(" [shape=point,label=\"\",color=").append(color).append("];\n  ").append(str).append(":").append(dotName).append("_OUT -> dummy").append(dummyNodeCounter).append(" [color=").append(color).append("];\n").toString());
                }
            }
            for (String portName2 : getInputPorts(filter2, includeUnconnectedOptionalPorts)) {
                if (filter2.getConnectedInputPort(portName2) == null) {
                    String color = filter2.getSignature().getInputPortInfo(portName2).isRequired() ? "red" : "blue";
                    dummyNodeCounter++;
                    str = getDotName(filter2.getName());
                    dotName = getDotName(portName2);
                    dotFile.write(new StringBuilder((((String.valueOf(color).length() + 87) + String.valueOf(str).length()) + String.valueOf(dotName).length()) + String.valueOf(color).length()).append("  dummy").append(dummyNodeCounter).append(" [shape=point,label=\"\",color=").append(color).append("];\n  dummy").append(dummyNodeCounter).append(" -> ").append(str).append(":").append(dotName).append("_IN [color=").append(color).append("];\n").toString());
                }
            }
        }
        dotFile.write("}\n");
        dotFile.flush();
        dotFile.close();
    }

    private static String getDotName(String raw) {
        return raw.replaceAll("\\.", "___");
    }

    private static Set<String> getInputPorts(Filter filter, boolean includeUnconnectedOptional) {
        Set<String> ports = new HashSet();
        ports.addAll(filter.getConnectedInputPortMap().keySet());
        HashMap<String, PortInfo> signaturePorts = filter.getSignature().getInputPorts();
        if (signaturePorts != null) {
            for (Entry<String, PortInfo> e : signaturePorts.entrySet()) {
                if (includeUnconnectedOptional || ((PortInfo) e.getValue()).isRequired()) {
                    ports.add((String) e.getKey());
                }
            }
        }
        return ports;
    }

    private static Set<String> getOutputPorts(Filter filter, boolean includeUnconnectedOptional) {
        Set<String> ports = new HashSet();
        ports.addAll(filter.getConnectedOutputPortMap().keySet());
        HashMap<String, PortInfo> signaturePorts = filter.getSignature().getOutputPorts();
        if (signaturePorts != null) {
            for (Entry<String, PortInfo> e : signaturePorts.entrySet()) {
                if (includeUnconnectedOptional || ((PortInfo) e.getValue()).isRequired()) {
                    ports.add((String) e.getKey());
                }
            }
        }
        return ports;
    }
}
