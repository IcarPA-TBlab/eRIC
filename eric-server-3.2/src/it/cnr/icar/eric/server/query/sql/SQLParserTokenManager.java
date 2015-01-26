/*
 * ====================================================================
 * This file is part of the ebXML Registry by Icar Cnr v3.2 
 * ("eRICv32" in the following disclaimer).
 *
 * "eRICv32" is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * "eRICv32" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License Version 3
 * along with "eRICv32".  If not, see <http://www.gnu.org/licenses/>.
 *
 * eRICv32 is a forked, derivative work, based on:
 * 	- freebXML Registry, a royalty-free, open source implementation of the ebXML Registry standard,
 * 	  which was published under the "freebxml License, Version 1.1";
 *	- ebXML OMAR v3.2 Edition, published under the GNU GPL v3 by S. Krushe & P. Arwanitis.
 * 
 * All derivative software changes and additions are made under
 *
 * Copyright (C) 2013 Ing. Antonio Messina <messina@pa.icar.cnr.it>
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the freebxml Software Foundation.  For more
 * information on the freebxml Software Foundation, please see
 * "http://www.freebxml.org/".
 *
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 *
 * ====================================================================
 */
package it.cnr.icar.eric.server.query.sql;

public class SQLParserTokenManager implements SQLParserConstants
{
  public  java.io.PrintStream debugStream = System.out;
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopStringLiteralDfa_0(int pos, long active0)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0x40000000000000L) != 0L)
            return 0;
         if ((active0 & 0x20000000000L) != 0L)
            return 14;
         if ((active0 & 0x10000000000000L) != 0L)
            return 6;
         if ((active0 & 0xffffff80L) != 0L)
         {
            jjmatchedKind = 36;
            return 25;
         }
         return -1;
      case 1:
         if ((active0 & 0xfe73f780L) != 0L)
         {
            if (jjmatchedPos != 1)
            {
               jjmatchedKind = 36;
               jjmatchedPos = 1;
            }
            return 25;
         }
         if ((active0 & 0x18c0800L) != 0L)
            return 25;
         return -1;
      case 2:
         if ((active0 & 0xf753f400L) != 0L)
         {
            jjmatchedKind = 36;
            jjmatchedPos = 2;
            return 25;
         }
         if ((active0 & 0x8200380L) != 0L)
            return 25;
         return -1;
      case 3:
         if ((active0 & 0xc0509000L) != 0L)
            return 25;
         if ((active0 & 0x37036400L) != 0L)
         {
            if (jjmatchedPos != 3)
            {
               jjmatchedKind = 36;
               jjmatchedPos = 3;
            }
            return 25;
         }
         return -1;
      case 4:
         if ((active0 & 0x6026400L) != 0L)
         {
            jjmatchedKind = 36;
            jjmatchedPos = 4;
            return 25;
         }
         if ((active0 & 0xb1010000L) != 0L)
            return 25;
         return -1;
      case 5:
         if ((active0 & 0x2400L) != 0L)
         {
            jjmatchedKind = 36;
            jjmatchedPos = 5;
            return 25;
         }
         if ((active0 & 0x6024000L) != 0L)
            return 25;
         return -1;
      case 6:
         if ((active0 & 0x2000L) != 0L)
         {
            jjmatchedKind = 36;
            jjmatchedPos = 6;
            return 25;
         }
         if ((active0 & 0x400L) != 0L)
            return 25;
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_0(int pos, long active0)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
}
private final int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private final int jjStartNfaWithStates_0(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_0(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 33:
         return jjMoveStringLiteralDfa1_0(0x800000000000L);
      case 40:
         return jjStopAtPos(0, 49);
      case 41:
         return jjStopAtPos(0, 50);
      case 42:
         return jjStopAtPos(0, 51);
      case 43:
         return jjStopAtPos(0, 53);
      case 44:
         return jjStopAtPos(0, 57);
      case 45:
         return jjStartNfaWithStates_0(0, 54, 0);
      case 46:
         return jjStartNfaWithStates_0(0, 41, 14);
      case 47:
         return jjStartNfaWithStates_0(0, 52, 6);
      case 59:
         return jjStopAtPos(0, 40);
      case 60:
         jjmatchedKind = 42;
         return jjMoveStringLiteralDfa1_0(0x1080000000000L);
      case 61:
         return jjStopAtPos(0, 46);
      case 62:
         jjmatchedKind = 44;
         return jjMoveStringLiteralDfa1_0(0x200000000000L);
      case 63:
         return jjStopAtPos(0, 55);
      case 65:
      case 97:
         return jjMoveStringLiteralDfa1_0(0x380L);
      case 66:
      case 98:
         return jjMoveStringLiteralDfa1_0(0xc00L);
      case 68:
      case 100:
         return jjMoveStringLiteralDfa1_0(0x3000L);
      case 69:
      case 101:
         return jjMoveStringLiteralDfa1_0(0x4000L);
      case 70:
      case 102:
         return jjMoveStringLiteralDfa1_0(0x8000L);
      case 71:
      case 103:
         return jjMoveStringLiteralDfa1_0(0x10000L);
      case 72:
      case 104:
         return jjMoveStringLiteralDfa1_0(0x20000L);
      case 73:
      case 105:
         return jjMoveStringLiteralDfa1_0(0xc0000L);
      case 76:
      case 108:
         return jjMoveStringLiteralDfa1_0(0x100000L);
      case 78:
      case 110:
         return jjMoveStringLiteralDfa1_0(0x600000L);
      case 79:
      case 111:
         return jjMoveStringLiteralDfa1_0(0x1800000L);
      case 83:
      case 115:
         return jjMoveStringLiteralDfa1_0(0xe000000L);
      case 85:
      case 117:
         return jjMoveStringLiteralDfa1_0(0x10000000L);
      case 87:
      case 119:
         return jjMoveStringLiteralDfa1_0(0x20000000L);
      case 90:
      case 122:
         return jjMoveStringLiteralDfa1_0(0xc0000000L);
      case 124:
         return jjMoveStringLiteralDfa1_0(0x100000000000000L);
      default :
         return jjMoveNfa_0(5, 0);
   }
}
private final int jjMoveStringLiteralDfa1_0(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(0, active0);
      return 1;
   }
   switch(curChar)
   {
      case 61:
         if ((active0 & 0x80000000000L) != 0L)
            return jjStopAtPos(1, 43);
         else if ((active0 & 0x200000000000L) != 0L)
            return jjStopAtPos(1, 45);
         else if ((active0 & 0x800000000000L) != 0L)
            return jjStopAtPos(1, 47);
         break;
      case 62:
         if ((active0 & 0x1000000000000L) != 0L)
            return jjStopAtPos(1, 48);
         break;
      case 65:
      case 97:
         return jjMoveStringLiteralDfa2_0(active0, 0x20000L);
      case 69:
      case 101:
         return jjMoveStringLiteralDfa2_0(active0, 0xc2001400L);
      case 72:
      case 104:
         return jjMoveStringLiteralDfa2_0(active0, 0x20000000L);
      case 73:
      case 105:
         return jjMoveStringLiteralDfa2_0(active0, 0x102000L);
      case 76:
      case 108:
         return jjMoveStringLiteralDfa2_0(active0, 0x80L);
      case 78:
      case 110:
         if ((active0 & 0x40000L) != 0L)
            return jjStartNfaWithStates_0(1, 18, 25);
         return jjMoveStringLiteralDfa2_0(active0, 0x10000100L);
      case 79:
      case 111:
         return jjMoveStringLiteralDfa2_0(active0, 0x200000L);
      case 80:
      case 112:
         return jjMoveStringLiteralDfa2_0(active0, 0x4000000L);
      case 82:
      case 114:
         if ((active0 & 0x800000L) != 0L)
         {
            jjmatchedKind = 23;
            jjmatchedPos = 1;
         }
         return jjMoveStringLiteralDfa2_0(active0, 0x1018000L);
      case 83:
      case 115:
         if ((active0 & 0x80000L) != 0L)
            return jjStartNfaWithStates_0(1, 19, 25);
         return jjMoveStringLiteralDfa2_0(active0, 0x200L);
      case 85:
      case 117:
         return jjMoveStringLiteralDfa2_0(active0, 0x8400000L);
      case 88:
      case 120:
         return jjMoveStringLiteralDfa2_0(active0, 0x4000L);
      case 89:
      case 121:
         if ((active0 & 0x800L) != 0L)
            return jjStartNfaWithStates_0(1, 11, 25);
         break;
      case 124:
         if ((active0 & 0x100000000000000L) != 0L)
            return jjStopAtPos(1, 56);
         break;
      default :
         break;
   }
   return jjStartNfa_0(0, active0);
}
private final int jjMoveStringLiteralDfa2_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(0, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(1, active0);
      return 2;
   }
   switch(curChar)
   {
      case 65:
      case 97:
         return jjMoveStringLiteralDfa3_0(active0, 0x4000000L);
      case 67:
      case 99:
         if ((active0 & 0x200L) != 0L)
            return jjStartNfaWithStates_0(2, 9, 25);
         break;
      case 68:
      case 100:
         if ((active0 & 0x100L) != 0L)
            return jjStartNfaWithStates_0(2, 8, 25);
         return jjMoveStringLiteralDfa3_0(active0, 0x1000000L);
      case 69:
      case 101:
         return jjMoveStringLiteralDfa3_0(active0, 0x20000000L);
      case 73:
      case 105:
         return jjMoveStringLiteralDfa3_0(active0, 0x10004000L);
      case 75:
      case 107:
         return jjMoveStringLiteralDfa3_0(active0, 0x100000L);
      case 76:
      case 108:
         if ((active0 & 0x80L) != 0L)
            return jjStartNfaWithStates_0(2, 7, 25);
         return jjMoveStringLiteralDfa3_0(active0, 0x2400000L);
      case 77:
      case 109:
         if ((active0 & 0x8000000L) != 0L)
            return jjStartNfaWithStates_0(2, 27, 25);
         break;
      case 79:
      case 111:
         return jjMoveStringLiteralDfa3_0(active0, 0x18000L);
      case 82:
      case 114:
         return jjMoveStringLiteralDfa3_0(active0, 0xc0000000L);
      case 83:
      case 115:
         return jjMoveStringLiteralDfa3_0(active0, 0x3000L);
      case 84:
      case 116:
         if ((active0 & 0x200000L) != 0L)
            return jjStartNfaWithStates_0(2, 21, 25);
         return jjMoveStringLiteralDfa3_0(active0, 0x400L);
      case 86:
      case 118:
         return jjMoveStringLiteralDfa3_0(active0, 0x20000L);
      default :
         break;
   }
   return jjStartNfa_0(1, active0);
}
private final int jjMoveStringLiteralDfa3_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(1, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(2, active0);
      return 3;
   }
   switch(curChar)
   {
      case 67:
      case 99:
         if ((active0 & 0x1000L) != 0L)
            return jjStartNfaWithStates_0(3, 12, 25);
         return jjMoveStringLiteralDfa4_0(active0, 0x4000000L);
      case 69:
      case 101:
         if ((active0 & 0x100000L) != 0L)
            return jjStartNfaWithStates_0(3, 20, 25);
         return jjMoveStringLiteralDfa4_0(active0, 0x3000000L);
      case 73:
      case 105:
         return jjMoveStringLiteralDfa4_0(active0, 0x20000L);
      case 76:
      case 108:
         if ((active0 & 0x400000L) != 0L)
            return jjStartNfaWithStates_0(3, 22, 25);
         break;
      case 77:
      case 109:
         if ((active0 & 0x8000L) != 0L)
            return jjStartNfaWithStates_0(3, 15, 25);
         break;
      case 79:
      case 111:
         if ((active0 & 0x40000000L) != 0L)
         {
            jjmatchedKind = 30;
            jjmatchedPos = 3;
         }
         return jjMoveStringLiteralDfa4_0(active0, 0x90000000L);
      case 82:
      case 114:
         return jjMoveStringLiteralDfa4_0(active0, 0x20000000L);
      case 83:
      case 115:
         return jjMoveStringLiteralDfa4_0(active0, 0x4000L);
      case 84:
      case 116:
         return jjMoveStringLiteralDfa4_0(active0, 0x2000L);
      case 85:
      case 117:
         return jjMoveStringLiteralDfa4_0(active0, 0x10000L);
      case 87:
      case 119:
         return jjMoveStringLiteralDfa4_0(active0, 0x400L);
      default :
         break;
   }
   return jjStartNfa_0(2, active0);
}
private final int jjMoveStringLiteralDfa4_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(2, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(3, active0);
      return 4;
   }
   switch(curChar)
   {
      case 67:
      case 99:
         return jjMoveStringLiteralDfa5_0(active0, 0x2000000L);
      case 69:
      case 101:
         if ((active0 & 0x20000000L) != 0L)
            return jjStartNfaWithStates_0(4, 29, 25);
         return jjMoveStringLiteralDfa5_0(active0, 0x4000400L);
      case 73:
      case 105:
         return jjMoveStringLiteralDfa5_0(active0, 0x2000L);
      case 78:
      case 110:
         if ((active0 & 0x10000000L) != 0L)
            return jjStartNfaWithStates_0(4, 28, 25);
         return jjMoveStringLiteralDfa5_0(active0, 0x20000L);
      case 80:
      case 112:
         if ((active0 & 0x10000L) != 0L)
            return jjStartNfaWithStates_0(4, 16, 25);
         break;
      case 82:
      case 114:
         if ((active0 & 0x1000000L) != 0L)
            return jjStartNfaWithStates_0(4, 24, 25);
         break;
      case 83:
      case 115:
         if ((active0 & 0x80000000L) != 0L)
            return jjStartNfaWithStates_0(4, 31, 25);
         break;
      case 84:
      case 116:
         return jjMoveStringLiteralDfa5_0(active0, 0x4000L);
      default :
         break;
   }
   return jjStartNfa_0(3, active0);
}
private final int jjMoveStringLiteralDfa5_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(3, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(4, active0);
      return 5;
   }
   switch(curChar)
   {
      case 69:
      case 101:
         return jjMoveStringLiteralDfa6_0(active0, 0x400L);
      case 71:
      case 103:
         if ((active0 & 0x20000L) != 0L)
            return jjStartNfaWithStates_0(5, 17, 25);
         break;
      case 78:
      case 110:
         return jjMoveStringLiteralDfa6_0(active0, 0x2000L);
      case 83:
      case 115:
         if ((active0 & 0x4000L) != 0L)
            return jjStartNfaWithStates_0(5, 14, 25);
         else if ((active0 & 0x4000000L) != 0L)
            return jjStartNfaWithStates_0(5, 26, 25);
         break;
      case 84:
      case 116:
         if ((active0 & 0x2000000L) != 0L)
            return jjStartNfaWithStates_0(5, 25, 25);
         break;
      default :
         break;
   }
   return jjStartNfa_0(4, active0);
}
private final int jjMoveStringLiteralDfa6_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(4, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(5, active0);
      return 6;
   }
   switch(curChar)
   {
      case 67:
      case 99:
         return jjMoveStringLiteralDfa7_0(active0, 0x2000L);
      case 78:
      case 110:
         if ((active0 & 0x400L) != 0L)
            return jjStartNfaWithStates_0(6, 10, 25);
         break;
      default :
         break;
   }
   return jjStartNfa_0(5, active0);
}
private final int jjMoveStringLiteralDfa7_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(5, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(6, active0);
      return 7;
   }
   switch(curChar)
   {
      case 84:
      case 116:
         if ((active0 & 0x2000L) != 0L)
            return jjStartNfaWithStates_0(7, 13, 25);
         break;
      default :
         break;
   }
   return jjStartNfa_0(6, active0);
}
private final void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private final void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private final void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}
private final void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}
@SuppressWarnings("unused")
private final void jjCheckNAddStates(int start)
{
   jjCheckNAdd(jjnextStates[start]);
   jjCheckNAdd(jjnextStates[start + 1]);
}
static final long[] jjbitVec0 = {
   0xfffffffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec2 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
@SuppressWarnings("unused")
private final int jjMoveNfa_0(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 45;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         //MatchLoop: 
         do
         {
            switch(jjstateSet[--i])
            {
               case 5:
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 32)
                        kind = 32;
                     jjCheckNAddStates(0, 6);
                  }
                  else if (curChar == 36)
                     jjstateSet[jjnewStateCnt++] = 27;
                  else if (curChar == 39)
                     jjCheckNAddStates(7, 9);
                  else if (curChar == 46)
                     jjCheckNAdd(14);
                  else if (curChar == 47)
                     jjstateSet[jjnewStateCnt++] = 6;
                  else if (curChar == 45)
                     jjstateSet[jjnewStateCnt++] = 0;
                  break;
               case 0:
                  if (curChar == 45)
                     jjCheckNAddStates(10, 12);
                  break;
               case 1:
                  if ((0xffffffffffffdbffL & l) != 0L)
                     jjCheckNAddStates(10, 12);
                  break;
               case 2:
                  if ((0x2400L & l) != 0L && kind > 5)
                     kind = 5;
                  break;
               case 3:
                  if (curChar == 10 && kind > 5)
                     kind = 5;
                  break;
               case 4:
                  if (curChar == 13)
                     jjstateSet[jjnewStateCnt++] = 3;
                  break;
               case 6:
                  if (curChar == 42)
                     jjCheckNAddTwoStates(7, 8);
                  break;
               case 7:
                  if ((0xfffffbffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(7, 8);
                  break;
               case 8:
                  if (curChar == 42)
                     jjCheckNAddStates(13, 15);
                  break;
               case 9:
                  if ((0xffff7bffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(10, 8);
                  break;
               case 10:
                  if ((0xfffffbffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(10, 8);
                  break;
               case 11:
                  if (curChar == 47 && kind > 6)
                     kind = 6;
                  break;
               case 12:
                  if (curChar == 47)
                     jjstateSet[jjnewStateCnt++] = 6;
                  break;
               case 13:
                  if (curChar == 46)
                     jjCheckNAdd(14);
                  break;
               case 14:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 33)
                     kind = 33;
                  jjCheckNAddTwoStates(14, 15);
                  break;
               case 16:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(17);
                  break;
               case 17:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 33)
                     kind = 33;
                  jjCheckNAdd(17);
                  break;
               case 18:
                  if (curChar == 39)
                     jjCheckNAddStates(7, 9);
                  break;
               case 19:
                  if ((0xffffff7fffffffffL & l) != 0L)
                     jjCheckNAddStates(7, 9);
                  break;
               case 20:
                  if (curChar == 39)
                     jjCheckNAddStates(16, 18);
                  break;
               case 21:
                  if (curChar == 39)
                     jjstateSet[jjnewStateCnt++] = 20;
                  break;
               case 22:
                  if ((0xffffff7fffffffffL & l) != 0L)
                     jjCheckNAddStates(16, 18);
                  break;
               case 23:
                  if (curChar == 39 && kind > 35)
                     kind = 35;
                  break;
               case 25:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 36)
                     kind = 36;
                  jjstateSet[jjnewStateCnt++] = 25;
                  break;
               case 26:
                  if (curChar == 36)
                     jjstateSet[jjnewStateCnt++] = 27;
                  break;
               case 28:
                  if ((0x3ff400000000000L & l) == 0L)
                     break;
                  if (kind > 37)
                     kind = 37;
                  jjstateSet[jjnewStateCnt++] = 28;
                  break;
               case 29:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 32)
                     kind = 32;
                  jjCheckNAddStates(0, 6);
                  break;
               case 30:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 32)
                     kind = 32;
                  jjCheckNAdd(30);
                  break;
               case 31:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(31, 32);
                  break;
               case 32:
                  if (curChar == 46)
                     jjCheckNAdd(33);
                  break;
               case 33:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 33)
                     kind = 33;
                  jjCheckNAddTwoStates(33, 34);
                  break;
               case 35:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(36);
                  break;
               case 36:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 33)
                     kind = 33;
                  jjCheckNAdd(36);
                  break;
               case 37:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(37, 38);
                  break;
               case 39:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(40);
                  break;
               case 40:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 33)
                     kind = 33;
                  jjCheckNAdd(40);
                  break;
               case 41:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 33)
                     kind = 33;
                  jjCheckNAddTwoStates(41, 42);
                  break;
               case 43:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(44);
                  break;
               case 44:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 33)
                     kind = 33;
                  jjCheckNAdd(44);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         //MatchLoop: 
         do
         {
            switch(jjstateSet[--i])
            {
               case 5:
                  if ((0x7fffffe07fffffeL & l) == 0L)
                     break;
                  if (kind > 36)
                     kind = 36;
                  jjCheckNAdd(25);
                  break;
               case 1:
                  jjAddStates(10, 12);
                  break;
               case 7:
                  jjCheckNAddTwoStates(7, 8);
                  break;
               case 9:
               case 10:
                  jjCheckNAddTwoStates(10, 8);
                  break;
               case 15:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(19, 20);
                  break;
               case 19:
                  jjCheckNAddStates(7, 9);
                  break;
               case 22:
                  jjCheckNAddStates(16, 18);
                  break;
               case 25:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 36)
                     kind = 36;
                  jjCheckNAdd(25);
                  break;
               case 27:
                  if ((0x7fffffe07fffffeL & l) == 0L)
                     break;
                  if (kind > 37)
                     kind = 37;
                  jjCheckNAdd(28);
                  break;
               case 28:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 37)
                     kind = 37;
                  jjCheckNAdd(28);
                  break;
               case 34:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(21, 22);
                  break;
               case 38:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(23, 24);
                  break;
               case 42:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(25, 26);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         //MatchLoop: 
         do
         {
            switch(jjstateSet[--i])
            {
               case 1:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjAddStates(10, 12);
                  break;
               case 7:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(7, 8);
                  break;
               case 9:
               case 10:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(10, 8);
                  break;
               case 19:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjCheckNAddStates(7, 9);
                  break;
               case 22:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjCheckNAddStates(16, 18);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 45 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
static final int[] jjnextStates = {
   30, 31, 32, 37, 38, 41, 42, 19, 21, 23, 1, 2, 4, 8, 9, 11, 
   21, 22, 23, 16, 17, 35, 36, 39, 40, 43, 44, 
};
private static final boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec2[i2] & l2) != 0L);
      default : 
         if ((jjbitVec0[i1] & l1) != 0L)
            return true;
         return false;
   }
}
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, null, null, null, null, null, null, null, null, null, "\73", 
"\56", "\74", "\74\75", "\76", "\76\75", "\75", "\41\75", "\74\76", "\50", "\51", 
"\52", "\57", "\53", "\55", "\77", "\174\174", "\54", };
public static final String[] lexStateNames = {
   "DEFAULT", 
};
static final long[] jjtoToken = {
   0x3ffff3bffffff81L, 
};
static final long[] jjtoSkip = {
   0x7eL, 
};
protected SimpleCharStream input_stream;
private final int[] jjrounds = new int[45];
private final int[] jjstateSet = new int[90];
protected char curChar;
public SQLParserTokenManager(SimpleCharStream stream)
{
   if (SimpleCharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}
public SQLParserTokenManager(SimpleCharStream stream, int lexState)
{
   this(stream);
   SwitchTo(lexState);
}
public void ReInit(SimpleCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private final void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 45; i-- > 0;)
      jjrounds[i] = 0x80000000;
}
public void ReInit(SimpleCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}
public void SwitchTo(int lexState)
{
   if (lexState >= 1 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

protected Token jjFillToken()
{
   Token t = Token.newToken(jjmatchedKind);
   t.kind = jjmatchedKind;
   String im = jjstrLiteralImages[jjmatchedKind];
   t.image = (im == null) ? input_stream.GetImage() : im;
   t.beginLine = input_stream.getBeginLine();
   t.beginColumn = input_stream.getBeginColumn();
   t.endLine = input_stream.getEndLine();
   t.endColumn = input_stream.getEndColumn();
   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

@SuppressWarnings("unused")
public Token getNextToken() 
{
  int kind;
  Token specialToken = null;
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {   
   try   
   {     
      curChar = input_stream.BeginToken();
   }     
   catch(java.io.IOException e)
   {        
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      return matchedToken;
   }

   try { input_stream.backup(0);
      while (curChar <= 32 && (0x100002600L & (1L << curChar)) != 0L)
         curChar = input_stream.BeginToken();
   }
   catch (java.io.IOException e1) { continue EOFLoop; }
   jjmatchedKind = 0x7fffffff;
   jjmatchedPos = 0;
   curPos = jjMoveStringLiteralDfa0_0();
   if (jjmatchedKind != 0x7fffffff)
   {
      if (jjmatchedPos + 1 < curPos)
         input_stream.backup(curPos - jjmatchedPos - 1);
      if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
      {
         matchedToken = jjFillToken();
         return matchedToken;
      }
      else
      {
         continue EOFLoop;
      }
   }
   int error_line = input_stream.getEndLine();
   int error_column = input_stream.getEndColumn();
   String error_after = null;
   boolean EOFSeen = false;
   try { input_stream.readChar(); input_stream.backup(1); }
   catch (java.io.IOException e1) {
      EOFSeen = true;
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
      if (curChar == '\n' || curChar == '\r') {
         error_line++;
         error_column = 0;
      }
      else
         error_column++;
   }
   if (!EOFSeen) {
      input_stream.backup(1);
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
   }
   throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
  }
}

}
