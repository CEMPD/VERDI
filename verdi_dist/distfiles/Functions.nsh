!ifndef OTHER_MACROS_NSH
!define OTHER_MACROS_NSH

;  From http://nsis.sourceforge.net/wiki/A_slightly_better_Java_Launcher
;  Find JRE (javaw.exe)
;  1 - in .\jre directory (JRE Installed with application)
;  2 - in JAVA_HOME environment variable
;  3 - in the registry
;  4 - assume javaw.exe in current dir or PATH
;  Stores the JRE on the top of the stack
Function GetJRE
  Push $R0
  Push $R1

  ClearErrors
  StrCpy $R0 "$EXEDIR\jre\bin\javaw.exe"
  IfFileExists $R0 JreFound
  StrCpy $R0 ""

  ClearErrors
  ReadEnvStr $R0 "JAVA_HOME"
  StrCpy $R0 "$R0\bin\javaw.exe"
  IfErrors 0 JreFound

  ClearErrors
  ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
  StrCpy $R0 "$R0\bin\javaw.exe"

  IfErrors 0 JreFound
  StrCpy $R0 "javaw.exe"

 JreFound:
  Pop $R1
  Exch $R0
FunctionEnd


 ; GetParameters
 ; input, none
 ; output, top of stack (replaces, with e.g. whatever)
 ; modifies no other variables.

Function GetParameters

  Push $R0
  Push $R1
  Push $R2
  Push $R3
  
  StrCpy $R2 1
  StrLen $R3 $CMDLINE
  
  ;Check for quote or space
  StrCpy $R0 $CMDLINE $R2
  StrCmp $R0 '"' 0 +3
    StrCpy $R1 '"'
    Goto loop
  StrCpy $R1 " "
  
  loop:
    IntOp $R2 $R2 + 1
    StrCpy $R0 $CMDLINE 1 $R2
    StrCmp $R0 $R1 get
    StrCmp $R2 $R3 get
    Goto loop
  
  get:
    IntOp $R2 $R2 + 1
    StrCpy $R0 $CMDLINE 1 $R2
    StrCmp $R0 " " get
    StrCpy $R0 $CMDLINE "" $R2
  
  Pop $R3
  Pop $R2
  Pop $R1
  Exch $R0

FunctionEnd

; -- written by Alexis de Valence --
; GetONEParameter

; Usage:
;   Push 3                 ; to get the 3rd parameter of the command line
;   Call GetONEParameter
;   Pop $R0                ; saves the result in $R0
; returns an empty string if not found

Function GetONEParameter
   Exch $R0
   Push $R1
   Push $R2
   Push $R3
   Push $R4
   Push $R5
   Push $R6

; init variables
   IntOp $R5 $R0 + 1
   StrCpy $R2 0
   StrCpy $R4 1
   StrCpy $R6 0

   loop3: ; looking for a char that's not a space
     IntOp $R2 $R2 + 1
     StrCpy $R0 $CMDLINE 1 $R2
     StrCmp $R0 " " loop3
     StrCpy $R3 $R2   ; found the begining of the current parameter


   loop:          ; scanning for the end of the current parameter

     StrCpy $R0 $CMDLINE 1 $R2
     StrCmp $R0 " " loop2
     StrCmp $R0 "" last
     IntOp $R2 $R2 + 1
     Goto loop

   last: ; there will be no other parameter to extract
   StrCpy $R6 1

   loop2: ; found the end of the current parameter

   IntCmp $R4 $R5 0 NextParam end
   StrCpy $R6 1 ; to quit after this process

   IntOp $R1 $R2 - $R3 ;number of letter of current parameter
   StrCpy $R0 $CMDLINE $R1 $R3        ; stores the result in R0
  
   NextParam:
   IntCmp $R6 1 end ; leave if found or if not enough parameters

   ; process the next parameter
   IntOp $R4 $R4 + 1

   Goto loop3

   end:

   Pop $R6  ; restore R0 - R6 to their initial value
   Pop $R5
   Pop $R4
   Pop $R3
   Pop $R2
   Pop $R1

   Exch $R0    ;Puts the result on the stack

 FunctionEnd


; GetParameterValue
; Chris Morgan 5/10/2004
; Searches the command line input, retrieved using GetParameters, for the
; value of an option given the option name.  If no option is found the
; default value is placed on the top of the stack upon function return
;
; Inputs - Top of stack is default if parameter isn't found,
;  second in stack is parameter to search for, ex. "OUTPUT"
; Outputs - Top of the stack contains the value of this parameter
;  So if the command line contained /OUTPUT=somedirectory, "somedirectory"
;  will be on the top of the stack when this function returns
;
; Register usage
;$R0 - default return value if the parameter isn't found
;$R1 - input parameter, for example OUTPUT from the above example
;$R2 - the length of the search, this is the search parameter+2
;      as we have '/OUTPUT='
;$R3 - the command line string
;$R4 - result from StrStr's
;$R5 - search for ' ' or '"'

Function GetParameterValue
  Exch $R0  ; get the default parameter into R1
  Exch      ; exchange the top two
  Exch $R1  ; get the search parameter into $R0

  ;Preserve on the stack the registers we will use in this function
  Push $R2
  Push $R3
  Push $R4
  Push $R5

  Strlen $R2 $R1+1   ; store the length of the search string into R2

  Call GetParameters ;get the command line parameters
  Pop $R3            ; store the command line string in R3
  # search for quoted search string
  StrCpy $R5 '"'     ; later on we want to search for a open quote
  Push $R3           ; push the 'search in' string onto the stack
  Push '$R1="'       ; push the 'search for'
  Call StrStr
  Pop $R4
  StrCpy $R4 $R4 ""  # skip quote
  StrCmp $R4 "" "" next
  # search for non-quoted search string
  StrCpy $R5 ' '     ; later on we want to search for a space
  Push $R3           ; push the command line back on the stack for searching
  Push '$R1='       ; search for the non-quoted search string
  Call StrStr
  Pop $R4
next:
  StrCmp $R4 "" done       ; if we didn't find anything then we are done
  # copy the value after /$R1=
  StrCpy $R0 $R4 "" $R2  ; copy commandline text beyond parameter into $R0
  # search for the next parameter so we can trim this extra text off
  Push $R0
  Push $R5         
  Call StrStr         ; search for the next parameter
  Pop $R4
  StrCmp $R4 "" done
  StrLen $R4 $R4
  StrCpy $R0 $R0 -$R4 ; using the length of the string beyond the value,
                      ;copy only the value into $R0
done:
  Pop $R5
  Pop $R4
  Pop $R3
  Pop $R2
  Pop $R1
  Exch $R0 ; put the value in $R0 at the top of the stack
FunctionEnd

Function StrStr
  Exch $R1 ; st=haystack,old$R1, $R1=needle
  Exch    ; st=old$R1,haystack
  Exch $R2 ; st=old$R1,old$R2, $R2=haystack
  Push $R3
  Push $R4
  Push $R5
  StrLen $R3 $R1
  StrCpy $R4 0
  ; $R1=needle
  ; $R2=haystack
  ; $R3=len(needle)
  ; $R4=cnt
  ; $R5=tmp
  loop:
    StrCpy $R5 $R2 $R3 $R4
    StrCmp $R5 $R1 done
    StrCmp $R5 "" done
    IntOp $R4 $R4 + 1
    Goto loop
  done:
  StrCpy $R1 $R2 "" $R4
  Pop $R5
  Pop $R4
  Pop $R3
  Pop $R2
  Exch $R1
FunctionEnd

!endif
