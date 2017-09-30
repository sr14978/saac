{-# LANGUAGE FlexibleContexts #-}

import Control.Monad.State.Lazy
import Control.Lens
import Data.Map (Map)
import qualified Data.Map as Map

type Register = Int
type InstructionCount = Int
data Instruction = Reg (Registers->Registers) String
                 | Mem ((Registers, Memory)->(Registers, Memory)) String
                 | Jump (Registers->Register) String
instance Show Instruction where
  show (Reg f s) = s
  show (Mem f s) = s
  show (Jump f s) = s
type Registers = (Register, Register, Register, Register, Register, Register, Register, Register, Register)
type Memory = Map Int Int
type Processor = ([Instruction], Registers, Memory, InstructionCount)

instsl :: (Functor f) => ([Instruction] -> f [Instruction]) -> Processor -> f Processor
instsl = _1

regsl :: (Functor f) => (Registers -> f Registers) -> Processor -> f Processor
regsl = _2

meml :: (Functor f) => (Memory -> f Memory) -> Processor -> f Processor
meml = _3

instcountl :: (Functor f) => (InstructionCount -> f InstructionCount) -> Processor -> f Processor
instcountl = _4

pcl :: (Functor f) => (Register -> f Register) -> Registers -> f Registers
pcl = _9

memover :: ((Registers, Memory) -> (Registers, Memory)) -> Processor -> Processor
memover f (is, r, m, c) = let (r',m') = f (r,m) in (is, r', m', c)

initInstructionCount :: InstructionCount
initInstructionCount = 0
initRegister :: Register
initRegister = 0
initRegisters :: Registers
initRegisters = (initRegister,initRegister,initRegister,initRegister,initRegister,initRegister,initRegister,initRegister,initRegister)
initProcessor :: [Instruction]->Processor
initProcessor instructions = (instructions, initRegisters, Map.empty, initInstructionCount)

exampleProcessor :: Processor
exampleProcessor = initProcessor [Reg (over _1 (+1)) "add r1 1", Reg (set _4 42) "ldc r4 42", Mem (store 69) "strc 69 64"]

store loc s = let val = view (_1._4) s in
          over _2 (\m->Map.insert loc val m) s

test = execState loop exampleProcessor

loop :: State Processor ()
loop = do
        execInstruction
        incPC
        checkStop

execInstruction :: State Processor ()
execInstruction = do
                    proc <- get
                    let pc = view (regsl.pcl) proc in
                      let insts = view instsl proc in
                        let inst = insts!!pc in
                          put $ execInstruction' inst proc

execInstruction' :: Instruction -> Processor -> Processor
execInstruction' (Reg f s) = over regsl f
execInstruction' (Mem f s) = memover f

incPC :: State Processor ()
incPC = do
          proc <- get
          put $ (over instcountl (+1) . over (regsl.pcl) (+1)) proc

checkStop :: State Processor ()
checkStop = do
              proc <- get
              let pc = view (regsl.pcl) proc in
                let insts = view instsl proc in
                  if pc >= length insts then
                    return ()
                  else
                    loop
