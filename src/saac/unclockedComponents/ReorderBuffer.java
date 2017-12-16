package saac.unclockedComponents;

import saac.Settings;
import saac.dataObjects.Instruction.Results.InstructionResult;

public class ReorderBuffer {
	
	public final static int BUFF_SIZE = Settings.VIRTUAL_ADDRESS_NUM;
	public InstructionResult[] reorderBuffer = new InstructionResult[BUFF_SIZE];
	public int bufferIndexStart = 0;
	public int bufferIndexEnd = 0;
	public int bufferInstructionStart = 0;
	
	public boolean insert(InstructionResult res) throws Exception {
		if(res.getVirtualNumber() < bufferInstructionStart + BUFF_SIZE) {
			
			int instructionOffset = res.getVirtualNumber() - bufferInstructionStart;
			if(instructionOffset > BUFF_SIZE)
				return false;
			
			int bufferIndex = (bufferIndexStart + instructionOffset) % BUFF_SIZE;
			if(instructionOffset >= BUFF_SIZE - bufferIndexStart && bufferIndex >= bufferIndexStart )
				return false;

			if(bufferIndex < 0)
				System.out.println(String.format("%d %b %b %b %d %d %d %d %d %d", 
						Settings.NUMBER_OF_EXECUTION_UNITS,
						Settings.OUT_OF_ORDER_ENABLED,
						Settings.REGISTER_RENAMING_ENABLED,
						Settings.RESERVATION_STATION_BYPASS_ENABLED,
						Settings.SUPERSCALER_WIDTH,
						Settings.VIRTUAL_ADDRESS_NUM,
						bufferInstructionStart,
						instructionOffset,
						bufferIndexStart,
						bufferIndex
						));
			
			reorderBuffer[bufferIndex] = res;

			if( (res.getVirtualNumber() - bufferInstructionStart + 1)
					> (bufferIndexEnd - bufferIndexStart + BUFF_SIZE) % BUFF_SIZE ) {
				bufferIndexEnd = (bufferIndex + 1) % BUFF_SIZE;
			}
			
			return true;
		} else 
			return false;
	}
	
	public InstructionResult getFirst() {
		return reorderBuffer[bufferIndexStart];
	}
	
	public void clearFirst() {
		reorderBuffer[bufferIndexStart] = null;
		bufferIndexStart = (bufferIndexStart + 1) % BUFF_SIZE;
		bufferInstructionStart++;
	}
	
	public void clearAfter() {
		int bufferIndex = bufferIndexStart;
		while(bufferIndex != bufferIndexEnd) {
			reorderBuffer[bufferIndexStart] = null;
			bufferIndex = (bufferIndex + 1) % BUFF_SIZE;
		}
		bufferIndexEnd = bufferIndex;
	}
	
	public InstructionResult getOffsetted(int offset) {
		return reorderBuffer[(bufferIndexStart+(offset-bufferInstructionStart)) % BUFF_SIZE];
	}
	
	public boolean inReorderBuffer(int addr) {
		return addr >= bufferInstructionStart && addr < bufferInstructionStart + BUFF_SIZE;
	}
	
	public boolean notYetinReorderBuffer(int addr) {
		return addr > bufferInstructionStart + BUFF_SIZE;
	}
}
