package com.acc.graph;

import com.acc.constants.Kind;
import com.acc.constants.OperationCode;
import com.acc.data.Code;
import com.acc.data.Instruction;
import com.acc.data.Result;
import com.acc.parser.Parser;
import com.acc.structure.BasicBlock;
import com.acc.structure.SymbolTable;

import java.util.*;

/**
 * Created by prabhuk on 2/26/2015.
 */
public class DeleteInstructions extends Worker {

    private final Code code;
    private int instructionNumber = 0;
    private Map<Integer, Integer> oldNewLocations = new HashMap<Integer, Integer>();
    private Map<Integer, Integer> deletedLocations = new HashMap<Integer, Integer>();

    public DeleteInstructions(Code code, Parser parser) {
        super(parser);
        this.code = code;
    }

    @Override
    public void begin() {
        instructionNumber = 0;
        final List<Instruction> instructions = code.getInstructions();
        for (Instruction instruction : instructions) {
            if(!instruction.isDeleted()) {
                oldNewLocations.put(instruction.getLocation(), instructionNumber);
                instructionNumber++;
            } else {
                deletedLocations.put(instruction.getLocation(), instructionNumber);
            }
        }
    }

    @Override
    public void visit(BasicBlock node) {
        final List<Instruction> instructions = node.getInstructions();
        final Iterator<Instruction> iterator = instructions.iterator();
        while (iterator.hasNext()) {
            final Instruction instruction = iterator.next();
            final Integer opcode = instruction.getOpcode();
            if(!instruction.isDeleted()) {
                if(opcode == OperationCode.bra) {
                    updateBranchDestinationTargets(instruction.getX());
                } else if(opcode >= OperationCode.bne && opcode <= OperationCode.bgt) {
                    instruction.setX(updateIntermediates(instruction.getX()));
                    updateBranchDestinationTargets(instruction.getY());
                }  else {
                    instruction.setX(updateIntermediates(instruction.getX()));
                    instruction.setY(updateIntermediates(instruction.getY()));
                }
            }
        }
    }

    private void updateBranchDestinationTargets(Result result) {
        if(result == null) {
            return;
        }
        if(result.isConstant()) {
            if(oldNewLocations.get(result.value()) != null) {
                result.value(oldNewLocations.get(result.value()));
            } else {
                int target = getNextAvailableLocation(result.value());
                result.value(target);
            }
        }
    }

    private int getNextAvailableLocation(Integer deletedInstruction) {
        Integer target = deletedLocations.get(deletedInstruction);
        if(target == null) {
            throw new RuntimeException("deletedInstruction [" + deletedInstruction + "] is not mapped to any other potential instructions");
        }
        while (!oldNewLocations.containsValue(target) && target < oldNewLocations.size()) {
            target++;
        }
        return target;
    }


    private Result updateIntermediates(Result result) {
        if(result == null) {
            return result;
        }
        if(result.isIntermediate()) {
            if(oldNewLocations.get(result.getIntermediateLoation()) != null) {
                final Result result1 = new Result(Kind.INTERMEDIATE);
                result1.setIntermediateLoation(oldNewLocations.get(result.getIntermediateLoation()));
                return result1;
            }
        }
        return result;
    }


    private Set<Instruction> getSet(Map<Integer, Set<Instruction>> map, Integer key) {
        if(map.get(key) == null) {
            Set<Instruction> instructions = new HashSet<Instruction>();
            map.put(key, instructions);
            return instructions;
        }
        return map.get(key);
    }

    @Override
    public void finish() {
        final List<Instruction> instructions = code.getInstructions();
        final Iterator<Instruction> iterator = instructions.iterator();
        instructionNumber = 0;
        while(iterator.hasNext()) {
            Instruction instruction = iterator.next();
            if(instruction.isDeleted()) {
                iterator.remove();
            } else {
                if (instruction.isPhi()) {
                    handlePhiInstructionOperand(instruction.getX());
                    handlePhiInstructionOperand(instruction.getY());
                }
                instruction.setLocation(instructionNumber++);
            }
        }
    }

    private void handlePhiInstructionOperand(Result result) {
        if(result == null) {
            return;
        }
        if(result.isVariable()) {
            final Integer targetOldLocation = result.getLocation();
            if(oldNewLocations.get(targetOldLocation) != null) {
                result.setLocation(oldNewLocations.get(targetOldLocation));
            }
        }
    }

    public Map<Integer, Integer> getOldNewLocations() {
        return oldNewLocations;
    }
}
