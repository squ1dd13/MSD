package com.squ1dd13.msd.decomp.controlflow;

import com.squ1dd13.msd.*;
import com.squ1dd13.msd.decomp.*;
import com.squ1dd13.msd.old.shared.*;

import java.util.*;

import static com.squ1dd13.msd.decomp.BasicScript.*;

// Control-flow graph
// TODO: Handle switches
public class ControlFlowGraph {
    private final OffsetMap offsets;
    private final List<Instruction> instructions;
    private final Map<Integer, Node> nodes = new HashMap<>();
    private final Stack<Integer> callStack = new Stack<>();

    public Node entryNode;
    private final Set<Integer> explored = new HashSet<>();

    public ControlFlowGraph(List<Instruction> instr, OffsetMap offsetMap) {
        instructions = instr;
        offsets = offsetMap;

        entryNode = getNode(0);

        // The first instruction is always referenced by virtue of the fact that it is always executed.
        entryNode.referenceCount = 1;
    }

    private Node getNode(int index) {
        Node node = nodes.getOrDefault(index, null);

        if(node == null) {
            node = new Node(index);
            nodes.put(index, node);
        }

        return node;
    }

    private void buildConnections(Node currentNode) {
        int i = currentNode.index;

        if(i >= instructions.size()) return;
        Instruction instruction = instructions.get(i);
        int opcode = instruction.opcode;

        if(opcode == Opcode.JumpIfFalse.get()) {
            int dest = offsets.toIndex(instruction.getJumpOffset());

            currentNode.addConnection(getNode(dest));
            currentNode.addConnection(getNode(i + 1));

            return;
        }

        if(opcode == Opcode.Call.get()) {
            int dest = offsets.toIndex(instruction.getJumpOffset());

            callStack.push(i);
            currentNode.addConnection(getNode(dest));

            return;
        }

        if(opcode == Opcode.Jump.get()) {
            int dest = offsets.toIndex(instruction.getJumpOffset());
            currentNode.addConnection(getNode(dest));

            return;
        }

        if(opcode == Opcode.Return.get()) {
            if(callStack.empty()) {
                System.err.println("Returned past top level");
                return;
            }

            int callIndex = callStack.pop();
            getNode(callIndex).addConnection(getNode(callIndex + 1));
            currentNode.addConnection(getNode(callIndex + 1));
            return;
        }

        if(opcode == Opcode.Switch.get()) {
            Switch.Info info = new Switch.Info(instruction);

            for(int offset : info.caseOffsets) {
                currentNode.addConnection(getNode(offsets.toIndex(offset)));
            }

            currentNode.addConnection(getNode(offsets.toIndex(info.defaultCaseOffset)));

            return;
        }

        currentNode.addConnection(getNode(i + 1));
    }

    private void buildFrom(Node node) {
        if(explored.contains(node.index)) return;

        int stackBefore = callStack.size();
        if(node.getOpcode() != Opcode.Terminate.get()) {
            buildConnections(node);
        }

        if(stackBefore > callStack.size()) {
            System.out.printf("%d causes stack change\n", instructions.get(node.index).offset);
        }

        explored.add(node.index);

        var connections = node.connections;

        boolean isNodeJump = node.isJump();

        for(Node connected : connections) {
            // Jump threading
            // TODO: Clean up JiF check.
            if(Config.decompiler.threadJumps && isNodeJump && (connected.isJump() || instructions.get(connected.index).opcode == Opcode.JumpIfFalse.get())) {
                instructions.get(node.index).arguments = instructions.get(connected.index).arguments;
                continue;
            }

            buildFrom(connected);
        }
    }

    public void build() {
        buildFrom(entryNode);
    }

    public void print() {
        for(int i = 0; i < instructions.size(); ++i) {
            Node node = nodes.getOrDefault(i, null);
            if(node == null) {
                System.out.println("<null>");
                continue;
            }
            var instr = instructions.get(node.index);
            String s = instr.offset + ": " + instr.toCodeString(0);

            StringBuilder builder = new StringBuilder(s).append(" // connected to: ");

            var connections = node.getConnections();
            for(Node connected : connections) {
                builder.append(instructions.get(connected.index).offset).append(", ");
            }

            builder.append("refs = ").append(node.referenceCount);

            System.out.println(builder.toString());
        }
    }

    // Returns a list of all instructions except ones that are unreachable.
    public List<Instruction> getReachableInstructions() {
        if(!Config.decompiler.removeDeadCode) {
            return instructions;
        }

        List<Instruction> reachable = new ArrayList<>();

        for(int i = 0; i < instructions.size(); ++i) {
            Node node = nodes.getOrDefault(i, null);

            // Skip if the node doesn't exist (only reachable instructions have nodes).
            if(node == null) continue;

            reachable.add(instructions.get(i));
        }

        return reachable;
    }

    public class Node {
        private final Set<Node> connections = new HashSet<>();
        public int index;
        public int referenceCount;

        public Node(int dex) {
            index = dex;
        }

        public boolean isJump() {
            if(index >= instructions.size()) return false;
            return instructions.get(index).opcode == Opcode.Jump.get();
        }

        public Set<Node> getConnections() {
            return connections;
        }

        public void addConnection(Node node) {
            connections.add(node);
            node.referenceCount++;
        }

        public int getOpcode() {
            return instructions.get(index).opcode;
        }
    }
}
