package androidx.media.filterfw;

import android.text.TextUtils;
import androidx.media.filterfw.FilterGraph.Builder;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class GraphReader {

    private interface Command {
        void execute(CommandStack commandStack);
    }

    private static class CommandStack {
        private Builder mBuilder;
        private final ArrayList<Command> mCommands = new ArrayList();
        private MffContext mContext;
        private FilterFactory mFactory;
        private FilterGraph mGraph;
        private final ArrayList<Command> mPostCommands = new ArrayList();

        public CommandStack(MffContext context) {
            this.mContext = context;
            this.mBuilder = new Builder(this.mContext);
            this.mFactory = new FilterFactory();
        }

        public FilterGraph execute(FilterGraph parentGraph) {
            Iterator it = this.mCommands.iterator();
            while (it.hasNext()) {
                ((Command) it.next()).execute(this);
            }
            if (parentGraph == null) {
                this.mGraph = this.mBuilder.build();
            } else {
                this.mGraph = this.mBuilder.buildSubGraph(parentGraph);
            }
            it = this.mPostCommands.iterator();
            while (it.hasNext()) {
                ((Command) it.next()).execute(this);
            }
            return this.mGraph;
        }

        public void append(Command command) {
            this.mCommands.add(command);
        }

        public void postAppend(Command command) {
            this.mPostCommands.add(command);
        }

        public FilterFactory getFactory() {
            return this.mFactory;
        }

        public MffContext getContext() {
            return this.mContext;
        }

        public FilterGraph getGraph() {
            return this.mGraph;
        }

        protected Builder getBuilder() {
            return this.mBuilder;
        }
    }

    private static class Variable {
        public String name;

        public Variable(String name) {
            this.name = name;
        }
    }

    private static class XmlGraphReader {
        private SAXParserFactory mParserFactory = SAXParserFactory.newInstance();

        private static class GraphDataHandler extends DefaultHandler {
            private CommandStack mCommandStack;
            private String mCurFilterName = null;
            private boolean mInGraph = false;

            public GraphDataHandler(CommandStack commandStack) {
                this.mCommandStack = commandStack;
            }

            public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {
                if (localName.equals("graph")) {
                    beginGraph();
                    return;
                }
                assertInGraph(localName);
                if (localName.equals("import")) {
                    addImportCommand(attr);
                } else if (localName.equals("library")) {
                    addLibraryCommand(attr);
                } else if (localName.equals("connect")) {
                    addConnectCommand(attr);
                } else if (localName.equals("var")) {
                    addVarCommand(attr);
                } else if (localName.equals("filter")) {
                    beginFilter(attr);
                } else if (localName.equals("metafilter")) {
                    beginMetaFilter(attr);
                } else if (localName.equals("input")) {
                    addFilterInput(attr);
                } else {
                    throw new SAXException(new StringBuilder(String.valueOf(localName).length() + 23).append("Unknown XML element '").append(localName).append("'!").toString());
                }
            }

            public void endElement(String uri, String localName, String qName) {
                if (localName.equals("graph")) {
                    endGraph();
                } else if (localName.equals("filter")) {
                    endFilter();
                }
            }

            private void addImportCommand(Attributes attributes) throws SAXException {
                this.mCommandStack.append(new ImportPackageCommand(getRequiredAttribute(attributes, "package")));
            }

            private void addLibraryCommand(Attributes attributes) throws SAXException {
                this.mCommandStack.append(new AddLibraryCommand(getRequiredAttribute(attributes, "name")));
            }

            private void addConnectCommand(Attributes attributes) {
                String sourceFilterName;
                String sourcePortName;
                String targetFilterName;
                String targetPortName;
                String sourceTag = attributes.getValue("source");
                if (sourceTag != null) {
                    String[] sourceParts = sourceTag.split(":");
                    if (sourceParts.length == 2) {
                        sourceFilterName = sourceParts[0];
                        sourcePortName = sourceParts[1];
                    } else {
                        throw new RuntimeException("'source' tag needs to have format \"filter:port\"! Alternatively, you may use the form 'sourceFilter=\"filter\" sourcePort=\"port\"'.");
                    }
                }
                sourceFilterName = attributes.getValue("sourceFilter");
                sourcePortName = attributes.getValue("sourcePort");
                String targetTag = attributes.getValue("target");
                if (targetTag != null) {
                    String[] targetParts = targetTag.split(":");
                    if (targetParts.length == 2) {
                        targetFilterName = targetParts[0];
                        targetPortName = targetParts[1];
                    } else {
                        throw new RuntimeException("'target' tag needs to have format \"filter:port\"! Alternatively, you may use the form 'targetFilter=\"filter\" targetPort=\"port\"'.");
                    }
                }
                targetFilterName = attributes.getValue("targetFilter");
                targetPortName = attributes.getValue("targetPort");
                String sourceSlotName = attributes.getValue("sourceSlot");
                String targetSlotName = attributes.getValue("targetSlot");
                if (sourceSlotName != null) {
                    String str = "sourceSlot_";
                    String valueOf = String.valueOf(sourceSlotName);
                    sourceFilterName = valueOf.length() != 0 ? str.concat(valueOf) : new String(str);
                    this.mCommandStack.append(new AddSourceSlotCommand(sourceFilterName, sourceSlotName));
                    sourcePortName = "frame";
                }
                if (targetSlotName != null) {
                    String str = "targetSlot_";
                    String valueOf = String.valueOf(targetSlotName);
                    targetFilterName = valueOf.length() != 0 ? str.concat(valueOf) : new String(str);
                    this.mCommandStack.append(new AddTargetSlotCommand(targetFilterName, targetSlotName));
                    targetPortName = "frame";
                }
                assertValueNotNull("sourceFilter", sourceFilterName);
                assertValueNotNull("sourcePort", sourcePortName);
                assertValueNotNull("targetFilter", targetFilterName);
                assertValueNotNull("targetPort", targetPortName);
                this.mCommandStack.append(new ConnectCommand(sourceFilterName, sourcePortName, targetFilterName, targetPortName));
            }

            private void addVarCommand(Attributes attributes) throws SAXException {
                this.mCommandStack.append(new AddVariableCommand(getRequiredAttribute(attributes, "name"), getAssignmentValue(attributes)));
            }

            private void beginGraph() throws SAXException {
                if (this.mInGraph) {
                    throw new SAXException("Found more than one graph element in XML!");
                }
                this.mInGraph = true;
            }

            private void endGraph() {
                this.mInGraph = false;
            }

            private void beginFilter(Attributes attributes) throws SAXException {
                String className = getRequiredAttribute(attributes, "class");
                this.mCurFilterName = getRequiredAttribute(attributes, "name");
                this.mCommandStack.append(new AllocateFilterCommand(className, this.mCurFilterName));
            }

            private void beginMetaFilter(Attributes attributes) throws SAXException {
                beginFilter(attributes);
                String resourceName = getRequiredAttribute(attributes, "graphResource");
                String resourceType = attributes.getValue("resourceType");
                if (resourceType == null) {
                    resourceType = "raw";
                }
                this.mCommandStack.postAppend(new ConnectResourceSubGraphCommand(this.mCurFilterName, resourceName, resourceType));
            }

            private void endFilter() {
                this.mCurFilterName = null;
            }

            private void addFilterInput(Attributes attributes) throws SAXException {
                if (this.mCurFilterName == null) {
                    throw new SAXException("Found 'input' element outside of 'filter' element!");
                }
                String inputName = getRequiredAttribute(attributes, "name");
                Object inputValue = getAssignmentValue(attributes);
                if (inputValue == null) {
                    String str = this.mCurFilterName;
                    throw new SAXException(new StringBuilder((String.valueOf(inputName).length() + 45) + String.valueOf(str).length()).append("No value specified for input '").append(inputName).append("' of filter '").append(str).append("'!").toString());
                } else {
                    this.mCommandStack.append(new SetFilterInputCommand(this.mCurFilterName, inputName, inputValue));
                }
            }

            private void assertInGraph(String localName) throws SAXException {
                if (!this.mInGraph) {
                    throw new SAXException(new StringBuilder(String.valueOf(localName).length() + 50).append("Encountered '").append(localName).append("' element outside of 'graph' element!").toString());
                }
            }

            private static Object getAssignmentValue(Attributes attributes) {
                String strValue = attributes.getValue("stringValue");
                if (strValue != null) {
                    return strValue;
                }
                strValue = attributes.getValue("booleanValue");
                if (strValue != null) {
                    return Boolean.valueOf(Boolean.parseBoolean(strValue));
                }
                strValue = attributes.getValue("intValue");
                if (strValue != null) {
                    return Integer.valueOf(Integer.parseInt(strValue));
                }
                strValue = attributes.getValue("floatValue");
                if (strValue != null) {
                    return Float.valueOf(Float.parseFloat(strValue));
                }
                strValue = attributes.getValue("floatsValue");
                if (strValue != null) {
                    String[] floatStrings = TextUtils.split(strValue, ",");
                    float[] result = new float[floatStrings.length];
                    for (int i = 0; i < floatStrings.length; i++) {
                        result[i] = Float.parseFloat(floatStrings[i]);
                    }
                    return result;
                }
                strValue = attributes.getValue("varValue");
                if (strValue != null) {
                    return new Variable(strValue);
                }
                return null;
            }

            private static String getRequiredAttribute(Attributes attributes, String name) throws SAXException {
                String result = attributes.getValue(name);
                if (result != null) {
                    return result;
                }
                throw new SAXException(new StringBuilder(String.valueOf(name).length() + 32).append("Required attribute '").append(name).append("' not found!").toString());
            }

            private static void assertValueNotNull(String valueName, Object value) {
                if (value == null) {
                    throw new NullPointerException(new StringBuilder(String.valueOf(valueName).length() + 32).append("Required value '").append(valueName).append("' not specified!").toString());
                }
            }
        }

        public void parseString(String graphString, CommandStack commandStack) throws IOException {
            try {
                getReaderForCommandStack(commandStack).parse(new InputSource(new StringReader(graphString)));
            } catch (SAXException e) {
                throw new IOException("XML parse error during graph parsing!", e);
            }
        }

        public void parseInput(InputStream inputStream, CommandStack commandStack) throws IOException {
            try {
                getReaderForCommandStack(commandStack).parse(new InputSource(inputStream));
            } catch (SAXException e) {
                throw new IOException("XML parse error during graph parsing!", e);
            }
        }

        private XMLReader getReaderForCommandStack(CommandStack commandStack) throws IOException {
            try {
                XMLReader reader = this.mParserFactory.newSAXParser().getXMLReader();
                reader.setContentHandler(new GraphDataHandler(commandStack));
                return reader;
            } catch (ParserConfigurationException e) {
                throw new IOException("Error creating SAXParser for graph parsing!", e);
            } catch (SAXException e2) {
                throw new IOException("Error creating XMLReader for graph parsing!", e2);
            }
        }
    }

    private static class AddLibraryCommand implements Command {
        private String mLibraryName;

        public AddLibraryCommand(String libraryName) {
            this.mLibraryName = libraryName;
        }

        public void execute(CommandStack stack) {
            FilterFactory.addFilterLibrary(this.mLibraryName);
        }
    }

    private static class AddSourceSlotCommand implements Command {
        private String mName;
        private String mSlotName;

        public AddSourceSlotCommand(String name, String slotName) {
            this.mName = name;
            this.mSlotName = slotName;
        }

        public void execute(CommandStack stack) {
            stack.getBuilder().addFrameSlotSource(this.mName, this.mSlotName);
        }
    }

    private static class AddTargetSlotCommand implements Command {
        private String mName;
        private String mSlotName;

        public AddTargetSlotCommand(String name, String slotName) {
            this.mName = name;
            this.mSlotName = slotName;
        }

        public void execute(CommandStack stack) {
            stack.getBuilder().addFrameSlotTarget(this.mName, this.mSlotName);
        }
    }

    private static class AddVariableCommand implements Command {
        private String mName;
        private Object mValue;

        public AddVariableCommand(String name, Object value) {
            this.mName = name;
            this.mValue = value;
        }

        public void execute(CommandStack stack) {
            stack.getBuilder().addVariable(this.mName, this.mValue);
        }
    }

    private static class AllocateFilterCommand implements Command {
        private String mClassName;
        private String mFilterName;

        public AllocateFilterCommand(String className, String filterName) {
            this.mClassName = className;
            this.mFilterName = filterName;
        }

        public void execute(CommandStack stack) {
            try {
                stack.getBuilder().addFilter(stack.getFactory().createFilterByClassName(this.mClassName, this.mFilterName, stack.getContext()));
            } catch (IllegalArgumentException e) {
                String str = this.mFilterName;
                throw new RuntimeException(new StringBuilder(String.valueOf(str).length() + 23).append("Error creating filter ").append(str).append("!").toString(), e);
            }
        }
    }

    private static class ConnectCommand implements Command {
        private String mSourceFilter;
        private String mSourcePort;
        private String mTargetFilter;
        private String mTargetPort;

        public ConnectCommand(String sourceFilter, String sourcePort, String targetFilter, String targetPort) {
            this.mSourceFilter = sourceFilter;
            this.mSourcePort = sourcePort;
            this.mTargetFilter = targetFilter;
            this.mTargetPort = targetPort;
        }

        public void execute(CommandStack stack) {
            stack.getBuilder().connect(this.mSourceFilter, this.mSourcePort, this.mTargetFilter, this.mTargetPort);
        }
    }

    private static class ConnectResourceSubGraphCommand implements Command {
        private final String mFilterName;
        private final String mResourceName;
        private final String mResourceType;

        public ConnectResourceSubGraphCommand(String filterName, String resourceName, String resourceType) {
            this.mFilterName = filterName;
            this.mResourceName = resourceName;
            this.mResourceType = resourceType;
        }

        public void execute(CommandStack stack) {
            throw new UnsupportedOperationException("Subgraph connections currently not supported in google3 builds!");
        }
    }

    private static class ImportPackageCommand implements Command {
        private String mPackageName;

        public ImportPackageCommand(String packageName) {
            this.mPackageName = packageName;
        }

        public void execute(CommandStack stack) {
            try {
                stack.getFactory().addPackage(this.mPackageName);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    private static class SetFilterInputCommand implements Command {
        private String mFilterInput;
        private String mFilterName;
        private Object mValue;

        public SetFilterInputCommand(String filterName, String input, Object value) {
            this.mFilterName = filterName;
            this.mFilterInput = input;
            this.mValue = value;
        }

        public void execute(CommandStack stack) {
            if (this.mValue instanceof Variable) {
                stack.getBuilder().assignVariableToFilterInput(((Variable) this.mValue).name, this.mFilterName, this.mFilterInput);
                return;
            }
            stack.getBuilder().assignValueToFilterInput(this.mValue, this.mFilterName, this.mFilterInput);
        }
    }

    public static FilterGraph readXmlGraph(MffContext context, InputStream xmlStream) throws IOException {
        return getGraphForXmlStream(context, xmlStream, null);
    }

    public static FilterGraph readXmlSubGraph(MffContext context, InputStream xmlStream, FilterGraph parentGraph) throws IOException {
        return getGraphForXmlStream(context, xmlStream, parentGraph);
    }

    public static FilterGraph readXmlGraphResource(MffContext context, int resourceId) throws IOException {
        return getGraphForXmlResource(context, resourceId, null);
    }

    public static FilterGraph readXmlSubGraphResource(MffContext context, int resourceId, FilterGraph parentGraph) throws IOException {
        return getGraphForXmlResource(context, resourceId, parentGraph);
    }

    private static FilterGraph getGraphForXmlStream(MffContext context, InputStream source, FilterGraph parentGraph) throws IOException {
        XmlGraphReader reader = new XmlGraphReader();
        CommandStack commands = new CommandStack(context);
        reader.parseInput(source, commands);
        return commands.execute(parentGraph);
    }

    private static FilterGraph getGraphForXmlResource(MffContext context, int resourceId, FilterGraph parentGraph) throws IOException {
        return getGraphForXmlStream(context, context.getApplicationContext().getResources().openRawResource(resourceId), parentGraph);
    }
}
