package com.parshwa.create.radionautics.compat.cosmonautics;

import com.parshwa.create.radionautics.CreateRadionautics;
import com.parshwa.create.radionautics.blockentity.AstronauticalRadioLinkBlockEntity;
import com.parshwa.create.radionautics.radio.RadioWebUBridge;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class CosmonauticsWebUCompat {
    public static final ResourceLocation RADIO_RECEIVER =
            ResourceLocation.fromNamespaceAndPath(CreateRadionautics.MOD_ID, "webu_radio_receiver");
    public static final ResourceLocation RADIO_SENDER =
            ResourceLocation.fromNamespaceAndPath(CreateRadionautics.MOD_ID, "webu_radio_sender");
    public static final ResourceLocation RADIO_TEXT_CHAR =
            ResourceLocation.fromNamespaceAndPath(CreateRadionautics.MOD_ID, "webu_radio_text_char");
    public static final ResourceLocation RADIO_TEXT_VIEWER =
            ResourceLocation.fromNamespaceAndPath(CreateRadionautics.MOD_ID, "webu_radio_text_viewer");
    public static final ResourceLocation COSMONAUTICS_RADIO =
            ResourceLocation.fromNamespaceAndPath("rocketnautics", "radio");
    private static final ResourceLocation COSMONAUTICS_RADIO_SEND =
            ResourceLocation.fromNamespaceAndPath("rocketnautics", "radio_send");
    private static final ResourceLocation COSMONAUTICS_RADIO_RECEIVE =
            ResourceLocation.fromNamespaceAndPath("rocketnautics", "radio_receive");
    private static final ResourceLocation WEBU_STRING_LITERAL =
            ResourceLocation.fromNamespaceAndPath("websnodelib", "string_literal");

    private static final int STRING_COLOR = 0x55D6FF;
    private static final int NUMBER_COLOR = 0x55FF99;
    private static final int FREQUENCY_COLOR = 0xFFB95B;
    private static final int STATUS_COLOR = 0xD7B8FF;
    private static final int WHITE = 0xFFFFFF;
    private static final int TICK_COLOR = 0xFFE66D;

    private static boolean registered;
    private static WebU webU;

    private CosmonauticsWebUCompat() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        WebU api = WebU.load();
        if (api == null) {
            CreateRadionautics.LOGGER.warn("WebSNodeLib was not available; skipping Radionautics WebU nodes.");
            return;
        }

        registered = true;
        webU = api;
        api.register(RADIO_RECEIVER, "Radionautics", CosmonauticsWebUCompat::createRadioReceiver);
        api.register(RADIO_SENDER, "Radionautics", CosmonauticsWebUCompat::createRadioSender);
        api.register(RADIO_TEXT_CHAR, "Radionautics", CosmonauticsWebUCompat::createRadioTextChar);
        api.register(RADIO_TEXT_VIEWER, "Radionautics", CosmonauticsWebUCompat::createRadioTextViewer);

        api.register(COSMONAUTICS_RADIO, "Radionautics", CosmonauticsWebUCompat::createRadioChannel);
        api.register(COSMONAUTICS_RADIO_SEND, "Radionautics", CosmonauticsWebUCompat::createRadioSender);
        api.register(COSMONAUTICS_RADIO_RECEIVE, "Radionautics", CosmonauticsWebUCompat::createRadioReceiver);
    }

    public static Object createLuaEditorScreen() {
        register();
        WebU api = webU;
        if (api == null) {
            return null;
        }

        Object graph = api.newGraph();
        Object channel = api.createNode(COSMONAUTICS_RADIO, 60, 80);
        Object receiver = api.createNode(RADIO_RECEIVER, 80, 80);
        Object sender = api.createNode(RADIO_SENDER, 80, 270);
        Object viewer = api.createNode(RADIO_TEXT_VIEWER, 360, 80);
        Object charParser = api.createNode(RADIO_TEXT_CHAR, 360, 230);
        Object textInput = api.createNode(WEBU_STRING_LITERAL, 360, 360);

        api.addNode(graph, channel);
        api.addNode(graph, receiver);
        api.addNode(graph, viewer);
        api.addNode(graph, sender);
        api.addNode(graph, charParser);
        api.addNode(graph, textInput);
        api.connect(graph, channel, 0, receiver, 0);
        api.connect(graph, channel, 0, sender, 0);
        api.connect(graph, receiver, 0, viewer, 0);
        api.connect(graph, receiver, 0, charParser, 0);
        api.connect(graph, textInput, 0, sender, 1);

        return api.newNodeScreen(Component.literal("Radionautics Lua Editor"), graph);
    }

    private static Object createRadioChannel(int x, int y) {
        Object node = webU.newNode(COSMONAUTICS_RADIO, "Radio Channel", x, y);
        webU.addInput(node, "Frequency", FREQUENCY_COLOR, webU.anyType);
        webU.addOutput(node, "Frequency", FREQUENCY_COLOR, webU.stringType);
        webU.addOutput(node, "Has Link", STATUS_COLOR);
        webU.setEvaluator(node, CosmonauticsWebUCompat::evaluateRadioChannel);
        return node;
    }

    private static Object createRadioReceiver(int x, int y) {
        Object node = webU.newNode(RADIO_RECEIVER, "Radio Receiver", x, y);
        webU.addInput(node, "Frequency", FREQUENCY_COLOR, webU.anyType);
        webU.addOutput(node, "Payload", STRING_COLOR, webU.stringType);
        webU.addOutput(node, "Number", NUMBER_COLOR);
        webU.addOutput(node, "Length", STATUS_COLOR);
        webU.addOutput(node, "Message Id", WHITE);
        webU.addOutput(node, "Received Tick", TICK_COLOR);
        webU.addOutput(node, "Has Link", STATUS_COLOR);
        webU.setEvaluator(node, CosmonauticsWebUCompat::evaluateRadioReceiver);
        return node;
    }

    private static Object createRadioSender(int x, int y) {
        Object node = webU.newNode(RADIO_SENDER, "Radio Sender", x, y);
        webU.addInput(node, "Frequency", FREQUENCY_COLOR, webU.anyType);
        webU.addInput(node, "Payload", STRING_COLOR, webU.stringType);
        webU.addInput(node, "Send", WHITE);
        webU.addInput(node, "UDP", STATUS_COLOR);
        webU.addOutput(node, "Received Count", NUMBER_COLOR);
        webU.addOutput(node, "Send Id", WHITE);
        webU.addOutput(node, "Sent Tick", TICK_COLOR);
        webU.addOutput(node, "Can Send", STATUS_COLOR);
        webU.setEvaluator(node, CosmonauticsWebUCompat::evaluateRadioSender);
        return node;
    }

    private static Object createRadioTextChar(int x, int y) {
        Object node = webU.newNode(RADIO_TEXT_CHAR, "Radio Text Char", x, y);
        webU.addInput(node, "Text", STRING_COLOR, webU.stringType);
        webU.addInput(node, "Index", WHITE);
        webU.addOutput(node, "Char", STRING_COLOR, webU.stringType);
        webU.addOutput(node, "Code", STRING_COLOR);
        webU.addOutput(node, "Digit", NUMBER_COLOR);
        webU.addOutput(node, "Is Digit", TICK_COLOR);
        webU.addOutput(node, "Length", STATUS_COLOR);
        webU.setEvaluator(node, CosmonauticsWebUCompat::evaluateRadioTextChar);
        return node;
    }

    private static Object createRadioTextViewer(int x, int y) {
        Object node = webU.newNode(RADIO_TEXT_VIEWER, "Radio Text Viewer", x, y);
        webU.addInput(node, "Text", STRING_COLOR, webU.stringType);
        webU.addOutput(node, "Length", STATUS_COLOR);
        Object label = webU.newLabel("No radio data");
        webU.addElement(node, label);
        webU.setEvaluator(node, n -> evaluateRadioTextViewer(n, label));
        return node;
    }

    private static void evaluateRadioChannel(Object node) {
        String frequency = frequencyInput(node, 0);
        setStringOutput(node, 0, frequency);
        setOutput(node, 1, radioLinkFromContext(node) == null ? 0.0D : 1.0D);
    }

    private static void evaluateRadioReceiver(Object node) {
        AstronauticalRadioLinkBlockEntity link = radioLinkFromContext(node);
        setOutput(node, 5, link == null ? 0.0D : 1.0D);
        if (link == null) {
            return;
        }

        String frequency = frequencyInput(node, 0);
        link.bindFrequency(frequency);
        consumePackets(node, link, frequency);

        RadioWebUBridge.LastMessage message = RadioWebUBridge.lastMessage(frequency);
        setStringOutput(node, 0, message == null ? "" : message.payload());
        setOutput(node, 1, message == null ? 0.0D : message.parsedNumber());
        setOutput(node, 2, message == null ? 0.0D : message.length());
        setOutput(node, 3, message == null ? 0.0D : message.messageId());
        setOutput(node, 4, message == null ? -1.0D : message.receivedDayTime());
    }

    private static void evaluateRadioSender(Object node) {
        AstronauticalRadioLinkBlockEntity link = radioLinkFromContext(node);
        boolean canSend = link != null;
        setOutput(node, 3, canSend ? 1.0D : 0.0D);
        if (!canSend) {
            return;
        }

        double trigger = input(node, 2);
        CompoundTag tag = customData(node);
        double lastTrigger = tag.getDouble("LastSendTrigger");
        tag.putDouble("LastSendTrigger", trigger);
        if (trigger <= 0.0D || lastTrigger > 0.0D) {
            return;
        }

        String frequency = frequencyInput(node, 0);
        String payload = stringInput(node, 1);
        boolean udp = input(node, 3) > 0.0D;
        int delivered = link.send(frequency, payload.getBytes(StandardCharsets.UTF_8));
        long sendId = tag.getLong("SendId") + 1L;
        tag.putLong("SendId", sendId);
        setOutput(node, 0, udp ? delivered : -1.0D);
        setOutput(node, 1, sendId);
        setOutput(node, 2, link.level() == null ? -1.0D : Math.floorMod(link.level().getDayTime(), 24000L));
    }

    private static void evaluateRadioTextChar(Object node) {
        String text = stringInput(node, 0);
        int index = (int) Math.floor(input(node, 1));
        int code = RadioWebUBridge.charCode(text, index);
        int digit = code >= '0' && code <= '9' ? code - '0' : -1;
        setStringOutput(node, 0, code == 0 ? "" : Character.toString((char) code));
        setOutput(node, 1, code);
        setOutput(node, 2, digit);
        setOutput(node, 3, digit >= 0 ? 1 : 0);
        setOutput(node, 4, text.length());
    }

    private static void evaluateRadioTextViewer(Object node, Object label) {
        String text = stringInput(node, 0);
        if (text.isBlank()) {
            webU.setLabelText(label, "No radio data");
            setOutput(node, 0, 0.0D);
            return;
        }
        webU.setLabelText(label, trimForNode(text));
        setOutput(node, 0, text.length());
    }

    private static AstronauticalRadioLinkBlockEntity radioLinkFromContext(Object node) {
        Object graph = webU.parentGraph(node);
        Object context = graph == null ? null : webU.context(graph);
        if (!(context instanceof BlockEntity blockEntity)) {
            return null;
        }
        Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide) {
            return null;
        }
        if (blockEntity instanceof AstronauticalRadioLinkBlockEntity link) {
            return link;
        }
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(blockEntity.getBlockPos().relative(direction)) instanceof AstronauticalRadioLinkBlockEntity link) {
                return link;
            }
        }
        return null;
    }

    private static void consumePackets(Object node, AstronauticalRadioLinkBlockEntity link, String frequency) {
        CompoundTag tag = customData(node);
        var packet = link.pollPacket();
        while (packet != null) {
            if (frequency.equals(packet.frequency())) {
                tag.putString("LastPayload", new String(packet.payload(), StandardCharsets.UTF_8));
                tag.putLong("LastMessageId", tag.getLong("LastMessageId") + 1L);
                tag.putLong("LastReceivedTick", link.level() == null ? -1L : Math.floorMod(link.level().getDayTime(), 24000L));
            }
            packet = link.pollPacket();
        }

        String payload = tag.getString("LastPayload");
        if (!payload.isEmpty()) {
            RadioWebUBridge.recordManual(new RadioWebUBridge.LastMessage(
                    frequency,
                    payload,
                    payload.hashCode(),
                    parseDouble(payload),
                    payload.length(),
                    tag.getLong("LastMessageId"),
                    tag.getLong("LastReceivedTick")));
        }
    }

    private static String frequencyInput(Object node, int index) {
        Object pin = inputPin(node, index);
        if (pin == null) {
            return "";
        }
        String stringValue = webU.getStringValue(pin);
        if (!stringValue.isBlank()) {
            return stringValue.trim();
        }
        double frequency = webU.getValue(pin);
        if (Math.rint(frequency) == frequency) {
            return Long.toString((long) frequency);
        }
        return String.format(Locale.ROOT, "%.6f", frequency).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private static double input(Object node, int index) {
        Object pin = inputPin(node, index);
        return pin == null ? 0.0D : webU.getValue(pin);
    }

    private static String stringInput(Object node, int index) {
        Object pin = inputPin(node, index);
        return pin == null ? "" : webU.getStringValue(pin);
    }

    private static void setOutput(Object node, int index, double value) {
        Object pin = outputPin(node, index);
        if (pin != null) {
            webU.setValue(pin, value);
        }
    }

    private static void setStringOutput(Object node, int index, String value) {
        Object pin = outputPin(node, index);
        if (pin != null) {
            webU.setStringValue(pin, value);
        }
    }

    private static Object inputPin(Object node, int index) {
        List<?> pins = webU.inputs(node);
        return index < 0 || index >= pins.size() ? null : pins.get(index);
    }

    private static Object outputPin(Object node, int index) {
        List<?> pins = webU.outputs(node);
        return index < 0 || index >= pins.size() ? null : pins.get(index);
    }

    private static CompoundTag customData(Object node) {
        return webU.customData(node);
    }

    private static double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (RuntimeException exception) {
            return Double.NaN;
        }
    }

    private static String trimForNode(String text) {
        if (text.length() <= 48) {
            return text;
        }
        return text.substring(0, 45) + "...";
    }

    @FunctionalInterface
    private interface NodeFactory {
        Object create(int x, int y);
    }

    private static final class WebU {
        private final Class<?> nodeRegistryClass;
        private final Class<?> nodeFactoryClass;
        private final Class<?> nodeClass;
        private final Class<?> graphClass;
        private final Class<?> pinValueTypeClass;
        private final Class<?> evaluatorClass;
        private final Class<?> labelClass;
        private final Class<?> elementClass;
        private final Class<?> nodeScreenClass;
        private final Constructor<?> nodeConstructor;
        private final Constructor<?> graphConstructor;
        private final Constructor<?> labelConstructor;
        private final Constructor<?> nodeScreenConstructor;
        private final Method registerMethod;
        private final Method createNodeMethod;
        private final Method addNodeMethod;
        private final Method connectMethod;
        private final Method getIdMethod;
        private final Method addInputMethod;
        private final Method addOutputMethod;
        private final Method addInputTypedMethod;
        private final Method addOutputTypedMethod;
        private final Method setEvaluatorMethod;
        private final Method addElementMethod;
        private final Method getCustomDataMethod;
        private final Method getParentGraphMethod;
        private final Method getContextMethod;
        private final Method getInputsMethod;
        private final Method getOutputsMethod;
        private final Method getValueMethod;
        private final Method setValueMethod;
        private final Method getStringValueMethod;
        private final Method setStringValueMethod;
        private final Method setLabelTextMethod;
        private final Object stringType;
        private final Object anyType;

        private WebU(
                Class<?> nodeRegistryClass,
                Class<?> nodeFactoryClass,
                Class<?> nodeClass,
                Class<?> graphClass,
                Class<?> pinValueTypeClass,
                Class<?> evaluatorClass,
                Class<?> labelClass,
                Class<?> elementClass,
                Class<?> nodeScreenClass,
                Constructor<?> nodeConstructor,
                Constructor<?> graphConstructor,
                Constructor<?> labelConstructor,
                Constructor<?> nodeScreenConstructor,
                Method registerMethod,
                Method createNodeMethod,
                Method addNodeMethod,
                Method connectMethod,
                Method getIdMethod,
                Method addInputMethod,
                Method addOutputMethod,
                Method addInputTypedMethod,
                Method addOutputTypedMethod,
                Method setEvaluatorMethod,
                Method addElementMethod,
                Method getCustomDataMethod,
                Method getParentGraphMethod,
                Method getContextMethod,
                Method getInputsMethod,
                Method getOutputsMethod,
                Method getValueMethod,
                Method setValueMethod,
                Method getStringValueMethod,
                Method setStringValueMethod,
                Method setLabelTextMethod,
                Object stringType,
                Object anyType) {
            this.nodeRegistryClass = nodeRegistryClass;
            this.nodeFactoryClass = nodeFactoryClass;
            this.nodeClass = nodeClass;
            this.graphClass = graphClass;
            this.pinValueTypeClass = pinValueTypeClass;
            this.evaluatorClass = evaluatorClass;
            this.labelClass = labelClass;
            this.elementClass = elementClass;
            this.nodeScreenClass = nodeScreenClass;
            this.nodeConstructor = nodeConstructor;
            this.graphConstructor = graphConstructor;
            this.labelConstructor = labelConstructor;
            this.nodeScreenConstructor = nodeScreenConstructor;
            this.registerMethod = registerMethod;
            this.createNodeMethod = createNodeMethod;
            this.addNodeMethod = addNodeMethod;
            this.connectMethod = connectMethod;
            this.getIdMethod = getIdMethod;
            this.addInputMethod = addInputMethod;
            this.addOutputMethod = addOutputMethod;
            this.addInputTypedMethod = addInputTypedMethod;
            this.addOutputTypedMethod = addOutputTypedMethod;
            this.setEvaluatorMethod = setEvaluatorMethod;
            this.addElementMethod = addElementMethod;
            this.getCustomDataMethod = getCustomDataMethod;
            this.getParentGraphMethod = getParentGraphMethod;
            this.getContextMethod = getContextMethod;
            this.getInputsMethod = getInputsMethod;
            this.getOutputsMethod = getOutputsMethod;
            this.getValueMethod = getValueMethod;
            this.setValueMethod = setValueMethod;
            this.getStringValueMethod = getStringValueMethod;
            this.setStringValueMethod = setStringValueMethod;
            this.setLabelTextMethod = setLabelTextMethod;
            this.stringType = stringType;
            this.anyType = anyType;
        }

        private static WebU load() {
            try {
                Class<?> nodeRegistry = Class.forName("dev.devce.websnodelib.api.NodeRegistry");
                Class<?> nodeFactory = Class.forName("dev.devce.websnodelib.api.NodeRegistry$NodeFactory");
                Class<?> node = Class.forName("dev.devce.websnodelib.api.WNode");
                Class<?> graph = Class.forName("dev.devce.websnodelib.api.WGraph");
                Class<?> pin = Class.forName("dev.devce.websnodelib.api.WPin");
                Class<?> valueType = Class.forName("dev.devce.websnodelib.api.WPin$ValueType");
                Class<?> evaluator = Class.forName("dev.devce.websnodelib.api.WNode$Evaluator");
                Class<?> label = Class.forName("dev.devce.websnodelib.api.elements.WLabel");
                Class<?> element = Class.forName("dev.devce.websnodelib.api.WElement");
                Class<?> nodeScreen = Class.forName("dev.devce.websnodelib.client.ui.WNodeScreen");
                Object stringType = Enum.valueOf((Class<Enum>) valueType.asSubclass(Enum.class), "STRING");
                Object anyType = Enum.valueOf((Class<Enum>) valueType.asSubclass(Enum.class), "ANY");

                return new WebU(
                        nodeRegistry,
                        nodeFactory,
                        node,
                        graph,
                        valueType,
                        evaluator,
                        label,
                        element,
                        nodeScreen,
                        node.getConstructor(ResourceLocation.class, String.class, int.class, int.class),
                        graph.getConstructor(),
                        label.getConstructor(String.class),
                        nodeScreen.getConstructor(Component.class, graph, Consumer.class, nodeScreen),
                        nodeRegistry.getMethod("register", ResourceLocation.class, String.class, nodeFactory),
                        nodeRegistry.getMethod("createNode", ResourceLocation.class, int.class, int.class),
                        graph.getMethod("addNode", node),
                        graph.getMethod("connect", java.util.UUID.class, int.class, java.util.UUID.class, int.class),
                        node.getMethod("getId"),
                        node.getMethod("addInput", String.class, int.class),
                        node.getMethod("addOutput", String.class, int.class),
                        node.getMethod("addInput", String.class, int.class, valueType),
                        node.getMethod("addOutput", String.class, int.class, valueType),
                        node.getMethod("setEvaluator", evaluator),
                        node.getMethod("addElement", element),
                        node.getMethod("getCustomData"),
                        node.getMethod("getParentGraph"),
                        graph.getMethod("getContext"),
                        node.getMethod("getInputs"),
                        node.getMethod("getOutputs"),
                        pin.getMethod("getValue"),
                        pin.getMethod("setValue", double.class),
                        pin.getMethod("getStringValue"),
                        pin.getMethod("setStringValue", String.class),
                        label.getMethod("setText", String.class),
                        stringType,
                        anyType);
            } catch (ReflectiveOperationException | LinkageError exception) {
                return null;
            }
        }

        private void register(ResourceLocation id, String category, NodeFactory factory) {
            invoke(registerMethod, null, id, category, proxy(nodeFactoryClass, (proxy, method, args) -> {
                if ("create".equals(method.getName())) {
                    return factory.create((Integer) args[0], (Integer) args[1]);
                }
                return defaultValue(method.getReturnType());
            }));
        }

        private Object newNode(ResourceLocation id, String title, int x, int y) {
            return construct(nodeConstructor, id, title, x, y);
        }

        private Object newGraph() {
            return construct(graphConstructor);
        }

        private Object newLabel(String text) {
            return construct(labelConstructor, text);
        }

        private Object newNodeScreen(Component title, Object graph) {
            return construct(nodeScreenConstructor, title, graph, (Consumer<CompoundTag>) tag -> {}, null);
        }

        private Object createNode(ResourceLocation id, int x, int y) {
            return invoke(createNodeMethod, null, id, x, y);
        }

        private void addNode(Object graph, Object node) {
            if (graph != null && node != null) {
                invoke(addNodeMethod, graph, node);
            }
        }

        private void connect(Object graph, Object source, int sourcePin, Object target, int targetPin) {
            if (graph != null && source != null && target != null) {
                invoke(connectMethod, graph, invoke(getIdMethod, source), sourcePin, invoke(getIdMethod, target), targetPin);
            }
        }

        private void addInput(Object node, String name, int color) {
            invoke(addInputMethod, node, name, color);
        }

        private void addInput(Object node, String name, int color, Object valueType) {
            invoke(addInputTypedMethod, node, name, color, valueType);
        }

        private void addOutput(Object node, String name, int color) {
            invoke(addOutputMethod, node, name, color);
        }

        private void addOutput(Object node, String name, int color, Object valueType) {
            invoke(addOutputTypedMethod, node, name, color, valueType);
        }

        private void setEvaluator(Object node, Consumer<Object> evaluator) {
            Object evaluatorProxy = proxy(evaluatorClass, (proxy, method, args) -> {
                if ("evaluate".equals(method.getName())) {
                    evaluator.accept(args[0]);
                }
                return null;
            });
            invoke(setEvaluatorMethod, node, evaluatorProxy);
        }

        private void addElement(Object node, Object element) {
            invoke(addElementMethod, node, element);
        }

        private CompoundTag customData(Object node) {
            return (CompoundTag) invoke(getCustomDataMethod, node);
        }

        private Object parentGraph(Object node) {
            return invoke(getParentGraphMethod, node);
        }

        private Object context(Object graph) {
            return invoke(getContextMethod, graph);
        }

        private List<?> inputs(Object node) {
            return (List<?>) invoke(getInputsMethod, node);
        }

        private List<?> outputs(Object node) {
            return (List<?>) invoke(getOutputsMethod, node);
        }

        private double getValue(Object pin) {
            return (Double) invoke(getValueMethod, pin);
        }

        private void setValue(Object pin, double value) {
            invoke(setValueMethod, pin, value);
        }

        private String getStringValue(Object pin) {
            return (String) invoke(getStringValueMethod, pin);
        }

        private void setStringValue(Object pin, String value) {
            invoke(setStringValueMethod, pin, value);
        }

        private void setLabelText(Object label, String text) {
            invoke(setLabelTextMethod, label, text);
        }

        private static Object proxy(Class<?> iface, InvocationHandler handler) {
            return Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[] {iface}, handler);
        }

        private static Object construct(Constructor<?> constructor, Object... args) {
            try {
                return constructor.newInstance(args);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException(exception);
            }
        }

        private static Object invoke(Method method, Object target, Object... args) {
            try {
                return method.invoke(target, args);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException(exception);
            }
        }

        private static Object defaultValue(Class<?> type) {
            if (type == boolean.class) {
                return false;
            }
            if (type == void.class) {
                return null;
            }
            if (type.isPrimitive()) {
                return 0;
            }
            return null;
        }
    }
}
